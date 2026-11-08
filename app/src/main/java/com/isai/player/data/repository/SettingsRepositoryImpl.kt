package com.isai.player.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LIFT_BEHAVIOR = stringPreferencesKey("lift_behavior")
        val SELECTED_FOLDER_URI = stringPreferencesKey("selected_folder_uri")
        val SELECTED_FOLDER_NAME = stringPreferencesKey("selected_folder_name")
        val FOLDER_ENTRIES = stringPreferencesKey("folder_entries_list")
        val DYNAMIC_ACCENT_COLOR = booleanPreferencesKey("dynamic_accent_color")
        val SLEEP_TIMER_END = longPreferencesKey("sleep_timer_end")
        val SLEEP_TIMER_STOP_AT_END = booleanPreferencesKey("sleep_timer_stop_at_end")
        val SEEK_BAR_STYLE = stringPreferencesKey("seek_bar_style")
        val PRE_GENERATE_WAVEFORMS = booleanPreferencesKey("pre_generate_waveforms")
        val LYRICS_TEXT_SIZE = stringPreferencesKey("lyrics_text_size")
        val KEEP_SCREEN_ON_LYRICS = booleanPreferencesKey("keep_screen_on_lyrics")
    }

    private fun serializeFolderEntries(entries: List<FolderEntry>): String {
        return entries.joinToString("\n") { "${it.uriString}\t${it.displayName}\t${it.isMediaStore}" }
    }

    private fun deserializeFolderEntries(raw: String?): List<FolderEntry> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lines().mapNotNull { line ->
            val parts = line.split("\t")
            if (parts.size >= 3) {
                FolderEntry(
                    uriString = parts[0],
                    displayName = parts[1],
                    isMediaStore = parts[2].toBoolean()
                )
            } else if (parts.size == 2) {
                FolderEntry(
                    uriString = parts[0],
                    displayName = parts[1],
                    isMediaStore = parts[0] == FolderEntry.DOWNLOADS_URI
                )
            } else null
        }
    }

    private val safePreferences: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    override val themeMode: Flow<ThemeMode> = safePreferences.map { prefs ->
        val raw = prefs[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    override val liftBehavior: Flow<LiftBehavior> = safePreferences.map { prefs ->
        val raw = prefs[PreferencesKeys.LIFT_BEHAVIOR] ?: LiftBehavior.PAUSE.name
        runCatching { LiftBehavior.valueOf(raw) }.getOrDefault(LiftBehavior.PAUSE)
    }

    override val folderEntries: Flow<List<FolderEntry>> = safePreferences.map { prefs ->
        val raw = prefs[PreferencesKeys.FOLDER_ENTRIES]
        val list = deserializeFolderEntries(raw)
        if (list.isEmpty()) {
            val legacyUri = prefs[PreferencesKeys.SELECTED_FOLDER_URI]
            val legacyName = prefs[PreferencesKeys.SELECTED_FOLDER_NAME] ?: "Music"
            if (!legacyUri.isNullOrBlank()) {
                listOf(FolderEntry(legacyUri, legacyName, legacyUri == FolderEntry.DOWNLOADS_URI))
            } else {
                emptyList()
            }
        } else {
            list
        }
    }

    override val selectedFolderUri: Flow<String?> = safePreferences.map { prefs ->
        val direct = prefs[PreferencesKeys.SELECTED_FOLDER_URI]
        if (!direct.isNullOrBlank()) return@map direct
        val entries = deserializeFolderEntries(prefs[PreferencesKeys.FOLDER_ENTRIES])
        entries.firstOrNull()?.uriString
    }

    override val selectedFolderName: Flow<String?> = safePreferences.map { prefs ->
        val direct = prefs[PreferencesKeys.SELECTED_FOLDER_NAME]
        if (!direct.isNullOrBlank()) return@map direct
        val entries = deserializeFolderEntries(prefs[PreferencesKeys.FOLDER_ENTRIES])
        entries.firstOrNull()?.displayName
    }

    override val dynamicAccentColor: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[PreferencesKeys.DYNAMIC_ACCENT_COLOR] ?: true
    }

    override val sleepTimerEndTimestamp: Flow<Long> = safePreferences.map { prefs ->
        prefs[PreferencesKeys.SLEEP_TIMER_END] ?: 0L
    }

    override val sleepTimerStopAtEnd: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[PreferencesKeys.SLEEP_TIMER_STOP_AT_END] ?: false
    }

    override val seekBarStyle: Flow<SeekBarStyle> = safePreferences.map { prefs ->
        val raw = prefs[PreferencesKeys.SEEK_BAR_STYLE] ?: SeekBarStyle.AUTO.name
        runCatching { SeekBarStyle.valueOf(raw) }.getOrDefault(SeekBarStyle.AUTO)
    }

    override val preGenerateWaveforms: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[PreferencesKeys.PRE_GENERATE_WAVEFORMS] ?: false
    }

    override val lyricsTextSize: Flow<LyricsTextSize> = safePreferences.map { prefs ->
        val raw = prefs[PreferencesKeys.LYRICS_TEXT_SIZE] ?: LyricsTextSize.MEDIUM.name
        runCatching { LyricsTextSize.valueOf(raw) }.getOrDefault(LyricsTextSize.MEDIUM)
    }

    override val keepScreenOnWhileLyrics: Flow<Boolean> = safePreferences.map { prefs ->
        prefs[PreferencesKeys.KEEP_SCREEN_ON_LYRICS] ?: false
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    override suspend fun setLiftBehavior(behavior: LiftBehavior) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LIFT_BEHAVIOR] = behavior.name
        }
    }

    override suspend fun setSelectedFolder(uri: String, name: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_FOLDER_URI] = uri
            prefs[PreferencesKeys.SELECTED_FOLDER_NAME] = name
            val currentEntries = deserializeFolderEntries(prefs[PreferencesKeys.FOLDER_ENTRIES]).toMutableList()
            val isMedia = uri == FolderEntry.DOWNLOADS_URI
            val newEntry = FolderEntry(uri, name, isMedia)
            val index = currentEntries.indexOfFirst { it.uriString == uri }
            if (index >= 0) {
                currentEntries[index] = newEntry
            } else {
                currentEntries.add(newEntry)
            }
            prefs[PreferencesKeys.FOLDER_ENTRIES] = serializeFolderEntries(currentEntries)
        }
    }

    override suspend fun addFolder(entry: FolderEntry) {
        dataStore.edit { prefs ->
            val current = deserializeFolderEntries(prefs[PreferencesKeys.FOLDER_ENTRIES]).toMutableList()
            val index = current.indexOfFirst { it.uriString == entry.uriString }
            if (index >= 0) {
                current[index] = entry
            } else {
                current.add(entry)
            }
            prefs[PreferencesKeys.FOLDER_ENTRIES] = serializeFolderEntries(current)
            if (prefs[PreferencesKeys.SELECTED_FOLDER_URI].isNullOrBlank()) {
                prefs[PreferencesKeys.SELECTED_FOLDER_URI] = entry.uriString
                prefs[PreferencesKeys.SELECTED_FOLDER_NAME] = entry.displayName
            }
        }
    }

    override suspend fun removeFolder(uriString: String) {
        dataStore.edit { prefs ->
            val current = deserializeFolderEntries(prefs[PreferencesKeys.FOLDER_ENTRIES]).filterNot { it.uriString == uriString }
            prefs[PreferencesKeys.FOLDER_ENTRIES] = serializeFolderEntries(current)
            if (prefs[PreferencesKeys.SELECTED_FOLDER_URI] == uriString) {
                val first = current.firstOrNull()
                if (first != null) {
                    prefs[PreferencesKeys.SELECTED_FOLDER_URI] = first.uriString
                    prefs[PreferencesKeys.SELECTED_FOLDER_NAME] = first.displayName
                } else {
                    prefs.remove(PreferencesKeys.SELECTED_FOLDER_URI)
                    prefs.remove(PreferencesKeys.SELECTED_FOLDER_NAME)
                }
            }
        }
    }

    override suspend fun toggleDownloadsFolder(enabled: Boolean) {
        if (enabled) {
            addFolder(
                FolderEntry(
                    uriString = FolderEntry.DOWNLOADS_URI,
                    displayName = "Downloads",
                    isMediaStore = true
                )
            )
        } else {
            removeFolder(FolderEntry.DOWNLOADS_URI)
        }
    }

    override suspend fun clearSelectedFolder() {
        dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.SELECTED_FOLDER_URI)
            prefs.remove(PreferencesKeys.SELECTED_FOLDER_NAME)
            prefs.remove(PreferencesKeys.FOLDER_ENTRIES)
        }
    }

    override suspend fun setDynamicAccentColor(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.DYNAMIC_ACCENT_COLOR] = enabled
        }
    }

    override suspend fun setSleepTimer(endEpochMs: Long, stopAtEndOfTrack: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SLEEP_TIMER_END] = endEpochMs
            prefs[PreferencesKeys.SLEEP_TIMER_STOP_AT_END] = stopAtEndOfTrack
        }
    }

    override suspend fun clearSleepTimer() {
        dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.SLEEP_TIMER_END)
            prefs.remove(PreferencesKeys.SLEEP_TIMER_STOP_AT_END)
        }
    }

    override suspend fun setSeekBarStyle(style: SeekBarStyle) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SEEK_BAR_STYLE] = style.name
        }
    }

    override suspend fun setPreGenerateWaveforms(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.PRE_GENERATE_WAVEFORMS] = enabled
        }
    }

    override suspend fun setLyricsTextSize(size: LyricsTextSize) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LYRICS_TEXT_SIZE] = size.name
        }
    }

    override suspend fun setKeepScreenOnWhileLyrics(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.KEEP_SCREEN_ON_LYRICS] = enabled
        }
    }
}
