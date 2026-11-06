package com.isai.player.data.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

enum class LiftBehavior {
    PAUSE,
    STOP
}

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val liftBehavior: Flow<LiftBehavior>
    val selectedFolderUri: Flow<String?>
    val selectedFolderName: Flow<String?>
    val dynamicAccentColor: Flow<Boolean>
    val sleepTimerEndTimestamp: Flow<Long>
    val sleepTimerStopAtEnd: Flow<Boolean>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLiftBehavior(behavior: LiftBehavior)
    suspend fun setSelectedFolder(uri: String, name: String)
    suspend fun clearSelectedFolder()
    suspend fun setDynamicAccentColor(enabled: Boolean)
    suspend fun setSleepTimer(endEpochMs: Long, stopAtEndOfTrack: Boolean)
    suspend fun clearSleepTimer()
}

