package com.isai.player.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.isai.player.R
import com.isai.player.data.repository.ThemeMode
import com.isai.player.ui.components.EmptyState
import com.isai.player.ui.components.SectionHeader
import com.isai.player.ui.components.TrackRow
import com.isai.player.ui.theme.IsaiTheme

@Composable
fun LibraryScreen(
    onTrackClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SectionHeader(
            title = stringResource(id = R.string.nav_tab_library),
            actionLabel = stringResource(id = R.string.global_action_play_all),
            onActionClick = {}
        )

        TrackRow(
            title = "Sundari Kannal Oru Sethi",
            artist = "S. P. B. & S. Janaki",
            durationMs = 432000L,
            artUri = null,
            isPlaying = true,
            onClick = { onTrackClick("track_1") },
            onMoreClick = {}
        )

        TrackRow(
            title = "Chinna Chinna Aasai",
            artist = "Minmini · Roja",
            durationMs = 295000L,
            artUri = null,
            isPlaying = false,
            onClick = { onTrackClick("track_2") },
            onMoreClick = {}
        )

        EmptyState(
            icon = Icons.Default.LibraryMusic,
            title = stringResource(id = R.string.nav_tab_library),
            message = stringResource(id = R.string.empty_state_default_message),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "LibraryScreen - Dark Theme")
@Composable
private fun LibraryScreenDarkPreview() {
    IsaiTheme(themeMode = ThemeMode.DARK) {
        Surface(color = IsaiTheme.colors.background) {
            LibraryScreen(onTrackClick = {})
        }
    }
}

@Preview(name = "LibraryScreen - Light Theme")
@Composable
private fun LibraryScreenLightPreview() {
    IsaiTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = IsaiTheme.colors.background) {
            LibraryScreen(onTrackClick = {})
        }
    }
}

