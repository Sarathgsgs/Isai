package com.isai.player.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isai.player.R
import com.isai.player.data.repository.ThemeMode
import com.isai.player.ui.components.AlbumCard
import com.isai.player.ui.components.EmptyState
import com.isai.player.ui.components.SectionHeader
import com.isai.player.ui.theme.IsaiTheme

@Composable
fun HomeScreen(
    onAlbumClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SectionHeader(
            title = stringResource(id = R.string.home_section_recently_played),
            actionLabel = stringResource(id = R.string.global_action_view_more),
            onActionClick = {}
        )

        val sampleAlbums = listOf(
            "Thalapathi" to "Ilaiyaraaja",
            "Roja" to "A. R. Rahman",
            "Mouna Ragam" to "Ilaiyaraaja",
            "Bombay" to "A. R. Rahman"
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(sampleAlbums) { (title, artist) ->
                AlbumCard(
                    title = title,
                    subtitle = artist,
                    artUri = null,
                    onClick = { onAlbumClick(title) },
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }

        EmptyState(
            icon = Icons.Default.Home,
            title = stringResource(id = R.string.nav_tab_home),
            message = stringResource(id = R.string.empty_state_default_message),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "HomeScreen - Dark Theme")
@Composable
private fun HomeScreenDarkPreview() {
    IsaiTheme(themeMode = ThemeMode.DARK) {
        Surface(color = IsaiTheme.colors.background) {
            HomeScreen(onAlbumClick = {})
        }
    }
}

@Preview(name = "HomeScreen - Light Theme")
@Composable
private fun HomeScreenLightPreview() {
    IsaiTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = IsaiTheme.colors.background) {
            HomeScreen(onAlbumClick = {})
        }
    }
}
