package com.isai.player.data.scanner

import android.net.Uri
import com.isai.player.data.db.dao.TrackDao
import com.isai.player.data.db.entity.TrackEntity
import com.isai.player.data.repository.FolderEntry
import com.isai.player.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryScanner @Inject constructor(
    private val folderScanner: FolderScanner,
    private val metadataExtractor: MetadataExtractor,
    private val trackDao: TrackDao,
    private val settingsRepository: SettingsRepository
) {
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val scanMutex = Mutex()
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(4)

    /**
     * Scans all configured folder entries (both SAF trees and MediaStore downloads),
     * merging newly discovered tracks and updating database state.
     */
    suspend fun scanAllFolders(): ScanState = scanMutex.withLock {
        val entries = settingsRepository.folderEntries.first()
        if (entries.isEmpty()) {
            val legacyUri = settingsRepository.selectedFolderUri.first()
            if (legacyUri.isNullOrBlank()) {
                val error = ScanState.Error("No music folders selected")
                _scanState.value = error
                return error
            }
        }

        return performScanInternal(entries)
    }

    suspend fun startScan(treeUri: Uri): ScanState = scanMutex.withLock {
        val isDownloads = treeUri.toString() == FolderEntry.DOWNLOADS_URI
        val entries = listOf(
            FolderEntry(
                uriString = treeUri.toString(),
                displayName = if (isDownloads) "Downloads" else "Selected Folder",
                isMediaStore = isDownloads
            )
        )
        return performScanInternal(entries)
    }

    private suspend fun performScanInternal(entries: List<FolderEntry>): ScanState {
        _scanState.value = ScanState.Scanning(scannedCount = 0, totalDiscovered = 0, currentFileName = "")

        try {
            // 1. Fetch existing tracks for incremental diffing
            val existingTracks = withContext(Dispatchers.IO) {
                trackDao.getAllTrackUriAndLastModified()
            }
            val existingMap = existingTracks.associate { it.uri to it.lastModified }

            // 2. Discover files across all configured folders
            val discoveredFilesMap = mutableMapOf<String, DiscoveredFile>()

            for (entry in entries) {
                if (entry.isMediaStore || entry.uriString == FolderEntry.DOWNLOADS_URI) {
                    val files = folderScanner.scanMediaStore("Download")
                    for (file in files) {
                        discoveredFilesMap[file.uriString] = file
                    }
                } else {
                    val uri = Uri.parse(entry.uriString)
                    val files = folderScanner.scanTree(uri)
                    for (file in files) {
                        discoveredFilesMap[file.uriString] = file
                    }
                }
            }

            val discoveredFiles = discoveredFilesMap.values.toList()
            val discoveredUris = discoveredFilesMap.keys

            // 3. Diff files: unmodified vs (new or modified)
            val filesToExtract = mutableListOf<DiscoveredFile>()
            var unmodifiedCount = 0

            for (file in discoveredFiles) {
                val existingModified = existingMap[file.uriString]
                if (existingModified != null && existingModified == file.lastModified) {
                    unmodifiedCount++
                } else {
                    filesToExtract.add(file)
                }
            }

            val totalDiscovered = discoveredFiles.size
            val processedCounter = AtomicInteger(unmodifiedCount)

            _scanState.value = ScanState.Scanning(
                scannedCount = unmodifiedCount,
                totalDiscovered = totalDiscovered,
                currentFileName = if (filesToExtract.isNotEmpty()) filesToExtract.first().displayName else ""
            )

            // 4. Extract metadata for new/modified files in parallel batches
            var addedCount = 0
            var updatedCount = 0
            val batchSize = 25
            val extractedBatch = mutableListOf<TrackEntity>()

            for (chunk in filesToExtract.chunked(8)) {
                val extractedList = withContext(workerDispatcher) {
                    chunk.map { file ->
                        async {
                            val track = metadataExtractor.extractTrack(file)
                            val count = processedCounter.incrementAndGet()
                            _scanState.value = ScanState.Scanning(
                                scannedCount = count,
                                totalDiscovered = totalDiscovered,
                                currentFileName = file.displayName
                            )
                            track
                        }
                    }.awaitAll()
                }

                for (track in extractedList) {
                    if (existingMap.containsKey(track.uri)) {
                        updatedCount++
                    } else {
                        addedCount++
                    }
                    extractedBatch.add(track)

                    if (extractedBatch.size >= batchSize) {
                        withContext(Dispatchers.IO) {
                            trackDao.insertTracks(extractedBatch)
                        }
                        extractedBatch.clear()
                    }
                }
            }

            if (extractedBatch.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    trackDao.insertTracks(extractedBatch)
                }
                extractedBatch.clear()
            }

            // 5. Identify deleted or missing tracks
            val missingUris = existingMap.keys.filterNot { it in discoveredUris }
            var missingCount = 0
            if (missingUris.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    trackDao.markTracksMissing(missingUris)
                }
                missingCount = missingUris.size
            }

            val totalTracks = withContext(Dispatchers.IO) {
                trackDao.observeTrackCount().first()
            }

            val completed = ScanState.Completed(
                addedCount = addedCount,
                updatedCount = updatedCount,
                missingCount = missingCount,
                totalTracks = totalTracks
            )
            _scanState.value = completed
            return completed
        } catch (e: Exception) {
            val error = ScanState.Error(e.message ?: "Failed to scan folder")
            _scanState.value = error
            return error
        }
    }

    fun resetState() {
        _scanState.value = ScanState.Idle
    }
}
