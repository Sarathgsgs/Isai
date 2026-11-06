package com.isai.player.ui.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isai.player.data.model.Track
import com.isai.player.data.repository.FavoritesRepository
import com.isai.player.data.repository.LiftBehavior
import com.isai.player.data.repository.SettingsRepository
import com.isai.player.playback.PlayerController
import com.isai.player.playback.PlayerState
import com.isai.player.playback.RepeatMode
import com.isai.player.ui.components.StageTrack
import com.isai.player.ui.components.SwipeDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val favoritesRepository: FavoritesRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerController.playerState

    val stageTrack: StateFlow<StageTrack?> = playerState.map { state ->
        state.currentTrack?.toStageTrack()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isFavorite: StateFlow<Boolean> = playerState
        .flatMapLatest { state ->
            val trackId = state.currentTrack?.id
            if (trackId != null) {
                favoritesRepository.observeIsFavorite(trackId)
            } else {
                flowOf(false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val liftBehavior: StateFlow<LiftBehavior> = settingsRepository.liftBehavior
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LiftBehavior.PAUSE)

    private val _scrubPreviewProgress = MutableStateFlow<Float?>(null)
    val scrubPreviewProgress: StateFlow<Float?> = _scrubPreviewProgress.asStateFlow()

    private val _lastSwipeDirection = MutableStateFlow(SwipeDirection.NONE)
    val lastSwipeDirection: StateFlow<SwipeDirection> = _lastSwipeDirection.asStateFlow()

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun onScrubStart() {
        // User initiated scrubbing
    }

    fun onScrubPreview(progress: Float) {
        _scrubPreviewProgress.value = progress
    }

    fun onScrubEnd(commit: Boolean, progress: Float) {
        _scrubPreviewProgress.value = null
        if (commit) {
            playerController.seekToProgress(progress)
            playerController.play()
        }
    }

    fun onLiftedStop() {
        _scrubPreviewProgress.value = null
        val behavior = liftBehavior.value
        if (behavior == LiftBehavior.STOP) {
            playerController.pause()
            playerController.seekTo(0L)
        } else {
            playerController.pause()
        }
    }

    fun skipToNext() {
        _lastSwipeDirection.value = SwipeDirection.NEXT
        playerController.skipToNext()
    }

    fun skipToPrevious() {
        _lastSwipeDirection.value = SwipeDirection.PREVIOUS
        playerController.skipToPrevious()
    }

    fun toggleFavorite() {
        val currentId = playerState.value.currentTrack?.id ?: return
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(currentId)
        }
    }

    fun toggleShuffle() {
        playerController.setShuffleModeEnabled(!playerState.value.shuffleModeEnabled)
    }

    fun cycleRepeatMode() {
        playerController.cycleRepeatMode()
    }

    fun playTrackFromQueue(index: Int) {
        val queue = playerState.value.queue
        val target = queue.getOrNull(index) ?: return
        playerController.playTrack(target, queue, index)
    }

    fun removeFromQueue(index: Int) {
        playerController.removeFromQueue(index)
    }

    fun clearQueue() {
        playerController.clearQueue()
    }

    private fun Track.toStageTrack(): StageTrack = StageTrack(
        id = id.toString(),
        title = title,
        artist = artist,
        album = album,
        year = year,
        art = artPath
    )
}
