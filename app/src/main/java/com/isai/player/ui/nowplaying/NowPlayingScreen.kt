package com.isai.player.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.isai.player.R
import com.isai.player.data.repository.LiftBehavior
import com.isai.player.data.repository.ThemeMode
import com.isai.player.playback.PlayerState
import com.isai.player.playback.RepeatMode
import com.isai.player.ui.components.StageTrack
import com.isai.player.ui.components.SwipeDirection
import com.isai.player.ui.components.VinylStage
import com.isai.player.ui.theme.IsaiTheme
import com.isai.player.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val stageTrack by viewModel.stageTrack.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()
    val liftBehavior by viewModel.liftBehavior.collectAsStateWithLifecycle()
    val scrubPreviewProgress by viewModel.scrubPreviewProgress.collectAsStateWithLifecycle()
    val lastSwipeDirection by viewModel.lastSwipeDirection.collectAsStateWithLifecycle()

    var showQueueSheet by remember { mutableStateOf(false) }
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    NowPlayingContent(
        playerState = playerState,
        stageTrack = stageTrack,
        isFavorite = isFavorite,
        liftBehavior = liftBehavior,
        scrubPreviewProgress = scrubPreviewProgress,
        lastSwipeDirection = lastSwipeDirection,
        onCollapseClick = onCollapseClick,
        onTogglePlayPause = { viewModel.togglePlayPause() },
        onScrubStart = { viewModel.onScrubStart() },
        onScrubPreview = { viewModel.onScrubPreview(it) },
        onScrubEnd = { commit, progress -> viewModel.onScrubEnd(commit, progress) },
        onLiftedStop = { viewModel.onLiftedStop() },
        onSkipNext = { viewModel.skipToNext() },
        onSkipPrevious = { viewModel.skipToPrevious() },
        onToggleFavorite = { viewModel.toggleFavorite() },
        onToggleShuffle = { viewModel.toggleShuffle() },
        onCycleRepeat = { viewModel.cycleRepeatMode() },
        onOpenQueueClick = { showQueueSheet = true },
        modifier = modifier
    )

    if (showQueueSheet) {
        QueueBottomSheet(
            sheetState = queueSheetState,
            queue = playerState.queue,
            currentQueueIndex = playerState.currentQueueIndex,
            isPlaying = playerState.isPlaying,
            onTrackClick = { index ->
                viewModel.playTrackFromQueue(index)
            },
            onRemoveTrack = { index ->
                viewModel.removeFromQueue(index)
            },
            onClearQueue = {
                viewModel.clearQueue()
            },
            onDismissRequest = {
                showQueueSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingContent(
    playerState: PlayerState,
    stageTrack: StageTrack?,
    isFavorite: Boolean,
    liftBehavior: LiftBehavior,
    scrubPreviewProgress: Float?,
    lastSwipeDirection: SwipeDirection,
    onCollapseClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onScrubStart: () -> Unit,
    onScrubPreview: (Float) -> Unit,
    onScrubEnd: (commit: Boolean, progress: Float) -> Unit,
    onLiftedStop: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onOpenQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMs = playerState.durationMs
    val currentProgress = scrubPreviewProgress ?: playerState.progress
    val currentPositionMs = (currentProgress * durationMs).toLong().coerceIn(0L, durationMs)
    val remainingMs = (durationMs - currentPositionMs).coerceAtLeast(0L)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.nav_screen_now_playing).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IsaiTheme.colors.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCollapseClick) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.global_action_cancel),
                            tint = IsaiTheme.colors.onBackground,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (isFavorite) IsaiTheme.colors.accent else IsaiTheme.colors.onBackground
                        )
                    }
                    IconButton(onClick = onOpenQueueClick) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = IsaiTheme.colors.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IsaiTheme.colors.background,
                    titleContentColor = IsaiTheme.colors.onBackground
                )
            )
        },
        containerColor = IsaiTheme.colors.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Vinyl Stage (Interactive Vinyl Record + Tonearm + Ambient Background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            ) {
                VinylStage(
                    track = stageTrack,
                    isPlaying = playerState.isPlaying,
                    progress = { currentProgress },
                    durationMs = durationMs,
                    lastSwipeDirection = lastSwipeDirection,
                    liftBehavior = liftBehavior,
                    onTogglePlay = onTogglePlayPause,
                    onScrubStart = onScrubStart,
                    onScrubPreview = onScrubPreview,
                    onScrubEnd = onScrubEnd,
                    onLiftedStop = onLiftedStop,
                    onSwipeNext = onSkipNext,
                    onSwipePrevious = onSkipPrevious,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Track Title, Artist & Album Information
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = playerState.currentTrack?.title ?: stringResource(id = R.string.miniplayer_placeholder_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = IsaiTheme.colors.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val subtitle = if (playerState.currentTrack != null) {
                    val track = playerState.currentTrack
                    "${track.artist} · ${track.album}" + if (track.year > 0) " (${track.year})" else ""
                } else {
                    stringResource(id = R.string.miniplayer_placeholder_artist)
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IsaiTheme.colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. Seek Bar with Elapsed and Remaining Time
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Formatters.formatDurationMs(currentPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = IsaiTheme.colors.accent
                    )
                    Text(
                        text = "-${Formatters.formatDurationMs(remainingMs)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = IsaiTheme.colors.onSurfaceVariant
                    )
                }

                Slider(
                    value = currentProgress,
                    onValueChange = { preview ->
                        onScrubPreview(preview)
                    },
                    onValueChangeFinished = {
                        onScrubEnd(true, currentProgress)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = IsaiTheme.colors.accent,
                        activeTrackColor = IsaiTheme.colors.accent,
                        inactiveTrackColor = IsaiTheme.colors.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 4. Control Cluster Dial (Shuffle, Prev, Play/Pause, Next, Repeat)
            ControlDial(
                isPlaying = playerState.isPlaying,
                shuffleEnabled = playerState.shuffleModeEnabled,
                repeatMode = playerState.repeatMode,
                onTogglePlayPause = onTogglePlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onToggleShuffle = onToggleShuffle,
                onCycleRepeat = onCycleRepeat,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(name = "NowPlayingContent - Dark Theme")
@Composable
private fun NowPlayingContentDarkPreview() {
    IsaiTheme(themeMode = ThemeMode.DARK) {
        Surface(color = IsaiTheme.colors.background) {
            NowPlayingContent(
                playerState = PlayerState(
                    currentPositionMs = 45000L,
                    durationMs = 217000L,
                    isPlaying = true
                ),
                stageTrack = StageTrack(
                    id = "1",
                    title = "Sundari Kannal Oru Sethi",
                    artist = "Ilaiyaraaja",
                    album = "Thalapathi",
                    year = 1991,
                    art = null
                ),
                isFavorite = true,
                liftBehavior = LiftBehavior.PAUSE,
                scrubPreviewProgress = null,
                lastSwipeDirection = SwipeDirection.NONE,
                onCollapseClick = {},
                onTogglePlayPause = {},
                onScrubStart = {},
                onScrubPreview = {},
                onScrubEnd = { _, _ -> },
                onLiftedStop = {},
                onSkipNext = {},
                onSkipPrevious = {},
                onToggleFavorite = {},
                onToggleShuffle = {},
                onCycleRepeat = {},
                onOpenQueueClick = {}
            )
        }
    }
}
