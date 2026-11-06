package com.isai.player.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT
}

/**
 * In-memory stub for application settings during UI design and prototype phases.
 * Will be replaced by DataStore-backed SettingsRepository in Prompt E.
 */
@Singleton
class SettingsRepositoryStub @Inject constructor() {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }
}

