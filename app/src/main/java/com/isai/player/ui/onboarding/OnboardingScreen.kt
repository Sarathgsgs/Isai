package com.isai.player.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.isai.player.R
import com.isai.player.data.repository.ThemeMode
import com.isai.player.ui.components.IsaiPrimaryButton
import com.isai.player.ui.theme.IsaiTheme

@Composable
fun OnboardingScreen(
    onChooseFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = IsaiTheme.colors.accent,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Isai",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = IsaiTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "An offline music player featuring an interactive vinyl record and tonearm. Choose a folder on your device to build your offline music library.",
            style = MaterialTheme.typography.bodyLarge,
            color = IsaiTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        IsaiPrimaryButton(
            onClick = onChooseFolderClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Choose Music Folder",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(name = "OnboardingScreen - Dark Theme")
@Composable
private fun OnboardingScreenDarkPreview() {
    IsaiTheme(themeMode = ThemeMode.DARK) {
        Surface(color = IsaiTheme.colors.background) {
            OnboardingScreen(onChooseFolderClick = {})
        }
    }
}

@Preview(name = "OnboardingScreen - Light Theme")
@Composable
private fun OnboardingScreenLightPreview() {
    IsaiTheme(themeMode = ThemeMode.LIGHT) {
        Surface(color = IsaiTheme.colors.background) {
            OnboardingScreen(onChooseFolderClick = {})
        }
    }
}
