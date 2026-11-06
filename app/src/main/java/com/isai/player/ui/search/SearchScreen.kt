package com.isai.player.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
fun SearchScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        SectionHeader(
            title = stringResource(id = R.string.nav_tab_search)
        )

        EmptyState(
            icon = Icons.Default.Search,
            title = stringResource(id = R.string.nav_tab_search),
            message = stringResource(id = R.string.empty_state_default_message),
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(name = "SearchScreen - Dark Theme")
@Composable
private fun SearchScreenDarkPreview() {
    IsaiTheme(themeMode = ThemeMode.DARK) {
        Surface(color = IsaiTheme.colors.background) {
            SearchScreen()
        }
    }
}

@Preview(name = "SearchScreen - Light Theme")
@Composable
private fun SearchScreenLightPreview() {
    IsaiTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = IsaiTheme.colors.background) {
            SearchScreen()
        }
    }
}
