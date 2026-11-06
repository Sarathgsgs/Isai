package com.isai.player.ui.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.isai.player.playback.RepeatMode
import com.isai.player.ui.theme.DialShape
import com.isai.player.ui.theme.IsaiTheme

@Composable
fun ControlDial(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = DialShape,
        color = IsaiTheme.colors.surface,
        shadowElevation = if (IsaiTheme.colors.isDark) 4.dp else 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Toggle Shuffle",
                    tint = if (shuffleEnabled) IsaiTheme.colors.accent else IsaiTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Previous (Honors 3s restart rule in ViewModel)
            IconButton(onClick = onSkipPrevious) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = IsaiTheme.colors.onBackground,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Play / Pause Main Button with spring scale animation
            val playButtonScale by animateFloatAsState(
                targetValue = if (isPlaying) 1.0f else 0.95f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "PlayButtonScale"
            )

            Button(
                onClick = onTogglePlayPause,
                colors = ButtonDefaults.buttonColors(containerColor = IsaiTheme.colors.accent),
                shape = CircleShape,
                modifier = Modifier
                    .size(68.dp)
                    .scale(playButtonScale)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Next
            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Track",
                    tint = IsaiTheme.colors.onBackground,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Repeat (Cycles OFF -> ALL -> ONE)
            IconButton(onClick = onCycleRepeat) {
                val repeatIcon = when (repeatMode) {
                    RepeatMode.ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                val repeatTint = when (repeatMode) {
                    RepeatMode.OFF -> IsaiTheme.colors.onSurfaceVariant
                    else -> IsaiTheme.colors.accent
                }

                Icon(
                    imageVector = repeatIcon,
                    contentDescription = "Cycle Repeat Mode",
                    tint = repeatTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
