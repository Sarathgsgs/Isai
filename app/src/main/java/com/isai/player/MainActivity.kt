package com.isai.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isai.player.data.repository.SettingsRepositoryStub
import com.isai.player.ui.navigation.MainScaffold
import com.isai.player.ui.theme.IsaiTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepositoryStub

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentThemeMode by settingsRepository.themeMode.collectAsStateWithLifecycle()

            IsaiTheme(themeMode = currentThemeMode) {
                MainScaffold(
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = { newMode ->
                        settingsRepository.setThemeMode(newMode)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
