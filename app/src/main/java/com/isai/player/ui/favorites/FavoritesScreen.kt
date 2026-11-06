package com.isai.player.ui.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.isai.player.R
import com.isai.player.data.repository.ThemeMode
import com.isai.player.ui.components.EmptyState
import com.isai.player.ui.components.SectionHeader
import com.isai.player.ui.theme.IsaiTheme

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SectionHeader(
            title = stringResource(id = R.string.nav_tab_favorites)
        )

        EmptyState(
            icon = Icons.Default.Favorite,
            title = stringResource(id = R.string.nav_tab_favorites),
            message = stringResource(id = R.string.empty_state_default_message),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "FavoritesScreen - Dark Theme")
@Composable
private fun FavoritesScreenDarkPreview() {
    IsaiTheme(themeMode = ThemeMode.DARK) {
        Surface(color = IsaiTheme.colors.background) {
            FavoritesScreen()
        }
    }
}

@Preview(name = "FavoritesScreen - Light Theme")
@Composable
private fun FavoritesScreenLightPreview() {
    IsaiTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = IsaiTheme.colors.background) {
            FavoritesScreen()
        }
    }
}

