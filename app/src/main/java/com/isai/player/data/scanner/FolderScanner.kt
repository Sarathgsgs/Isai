package com.isai.player.data.scanner

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.ArrayDeque
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val audioExtensions = setOf(
        "mp3", "flac", "wav", "m4a", "aac", "ogg", "opus", "wma", "aiff"
    )

    private val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        DocumentsContract.Document.COLUMN_SIZE
    )

    /**
     * Recursively traverses a SAF document tree starting at [treeUri] using direct
     * [DocumentsContract] child queries.
     *
     * @param onFileDiscovered Callback invoked each time an audio file is discovered.
     * @return List of all discovered audio files.
     */
    suspend fun scanTree(
        treeUri: Uri,
        onFileDiscovered: ((DiscoveredFile) -> Unit)? = null
    ): List<DiscoveredFile> {
        val contentResolver = context.contentResolver
        val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val discoveredFiles = mutableListOf<DiscoveredFile>()

        data class FolderQueueItem(val docId: String, val relativePath: String)
        val queue = ArrayDeque<FolderQueueItem>()
        queue.add(FolderQueueItem(rootDocId, ""))

        while (queue.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val (currentDocId, parentPath) = queue.removeFirst()

            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, currentDocId)

            try {
                contentResolver.query(
                    childrenUri,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val modCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

                    while (cursor.moveToNext()) {
                        currentCoroutineContext().ensureActive()

                        val docId = if (idCol >= 0) cursor.getString(idCol) ?: continue else continue
                        val mimeType = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "" else ""
                        val displayName = if (nameCol >= 0) cursor.getString(nameCol) ?: "Unknown" else "Unknown"
                        val lastModified = if (modCol >= 0) cursor.getLong(modCol) else 0L
                        val sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L

                        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                            val nextPath = if (parentPath.isEmpty()) displayName else "$parentPath/$displayName"
                            queue.add(FolderQueueItem(docId, nextPath))
                        } else if (isAudioFile(mimeType, displayName)) {
                            val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                            val file = DiscoveredFile(
                                uriString = documentUri.toString(),
                                documentId = docId,
                                displayName = displayName,
                                mimeType = mimeType.ifBlank { guessMimeType(displayName) },
                                sizeBytes = sizeBytes,
                                lastModified = lastModified,
                                folderPath = if (parentPath.isEmpty()) "Root" else parentPath
                            )
                            discoveredFiles.add(file)
                            onFileDiscovered?.invoke(file)
                        }
                    }
                }
            } catch (e: Exception) {
                // Safeguard against inaccessible or permission-restricted subdirectories
                continue
            }
        }

        return discoveredFiles
    }

    /**
     * Scans audio files via MediaStore.
     * Allows scanning directories like the system Downloads folder which Android 11+ SAF blocks.
     */
    suspend fun scanMediaStore(
        folderFilter: String? = "Download",
        onFileDiscovered: ((DiscoveredFile) -> Unit)? = null
    ): List<DiscoveredFile> {
        val discoveredFiles = mutableListOf<DiscoveredFile>()
        val contentResolver = context.contentResolver

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val mediaProjection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.SIZE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mediaProjection.add(MediaStore.Audio.Media.RELATIVE_PATH)
        } else {
            @Suppress("DEPRECATION")
            mediaProjection.add(MediaStore.Audio.Media.DATA)
        }

        val selection: String?
        val selectionArgs: Array<String>?

        if (!folderFilter.isNullOrBlank()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                selection = "${MediaStore.Audio.Media.RELATIVE_PATH} LIKE ?"
                selectionArgs = arrayOf("%$folderFilter%")
            } else {
                @Suppress("DEPRECATION")
                selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
                selectionArgs = arrayOf("%/$folderFilter/%")
            }
        } else {
            selection = null
            selectionArgs = null
        }

        try {
            contentResolver.query(
                collection,
                mediaProjection.toTypedArray(),
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val modCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val pathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                } else {
                    @Suppress("DEPRECATION")
                    cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                }

                while (cursor.moveToNext()) {
                    currentCoroutineContext().ensureActive()

                    val id = if (idCol >= 0) cursor.getLong(idCol) else continue
                    val displayName = if (nameCol >= 0) cursor.getString(nameCol) ?: "Unknown" else "Unknown"
                    val mimeType = if (mimeCol >= 0) cursor.getString(mimeCol) ?: "" else ""
                    val dateModifiedSec = if (modCol >= 0) cursor.getLong(modCol) else 0L
                    val sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val rawPath = if (pathCol >= 0) cursor.getString(pathCol) ?: folderFilter ?: "Download" else "Download"

                    if (isAudioFile(mimeType, displayName)) {
                        val contentUri = ContentUris.withAppendedId(collection, id)
                        val file = DiscoveredFile(
                            uriString = contentUri.toString(),
                            documentId = "mediastore_$id",
                            displayName = displayName,
                            mimeType = mimeType.ifBlank { guessMimeType(displayName) },
                            sizeBytes = sizeBytes,
                            lastModified = dateModifiedSec * 1000L,
                            folderPath = rawPath.trim('/')
                        )
                        discoveredFiles.add(file)
                        onFileDiscovered?.invoke(file)
                    }
                }
            }
        } catch (e: Exception) {
            // Permission not granted or query error handled gracefully
        }

        return discoveredFiles
    }

    fun isAudioFile(mimeType: String?, displayName: String?): Boolean {
        if (mimeType != null && mimeType.startsWith("audio/", ignoreCase = true)) {
            return true
        }
        val extension = displayName?.substringAfterLast('.', "")?.lowercase(Locale.ROOT) ?: ""
        return extension in audioExtensions
    }

    private fun guessMimeType(displayName: String): String {
        val ext = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when (ext) {
            "mp3" -> "audio/mpeg"
            "flac" -> "audio/flac"
            "wav" -> "audio/wav"
            "m4a", "aac" -> "audio/mp4"
            "ogg" -> "audio/ogg"
            "opus" -> "audio/opus"
            else -> "audio/*"
        }
    }
}
