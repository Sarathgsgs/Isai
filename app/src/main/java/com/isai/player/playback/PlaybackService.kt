package com.isai.player.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.isai.player.MainActivity
import com.isai.player.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject
    lateinit var queueStore: QueueStore

    @Inject
    lateinit var playHistoryTracker: PlayHistoryTracker

    @Inject
    lateinit var sleepTimer: SleepTimer

    @Inject
    lateinit var audioEffectsManager: com.isai.player.playback.audio.AudioEffectsManager

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)

        // Configure audio attributes for music playback with automatic focus handling
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // Build ExoPlayer with gapless support, audio focus & becoming noisy auto-pause
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exoPlayer = player
        audioEffectsManager.attachPlayer(player)

        // Activity launch PendingIntent when user taps notification
        val sessionActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build MediaSession
        val session = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        mediaSession = session

        // Configure system notification provider
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(NotificationHelper.CHANNEL_ID)
            .setChannelName(R.string.playback_notification_channel_name)
            .setNotificationId(NotificationHelper.NOTIFICATION_ID)
            .build()

        setMediaNotificationProvider(notificationProvider)

        // Player event listener for play history, queue sync, and corrupt track auto-skip
        player.addListener(object : Player.Listener {
            private var consecutiveErrorCount = 0

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                consecutiveErrorCount = 0
                val track = mediaItem?.let(MediaItemMapper::toTrack)
                playHistoryTracker.onTrackChanged(track)
            }

            override fun onPlayerError(error: PlaybackException) {
                consecutiveErrorCount++
                val queueSize = player.mediaItemCount
                if (queueSize > 1 && consecutiveErrorCount < queueSize) {
                    // Automatically skip corrupt or unplayable track
                    if (player.hasNextMediaItem()) {
                        player.seekToNextMediaItem()
                    } else {
                        player.seekToDefaultPosition(0)
                    }
                    player.prepare()
                    player.play()
                } else {
                    // Terminate loop when all items fail or single track is corrupt
                    player.stop()
                    consecutiveErrorCount = 0
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    sleepTimer.onTrackEnded {
                        player.pause()
                    }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                audioEffectsManager.onAudioSessionIdChanged(audioSessionId)
            }
        })

        // Restore previous queue on cold start
        serviceScope.launch {
            val restoredQueue = queueStore.restoreQueue()
            if (restoredQueue.isNotEmpty()) {
                val mediaItems = restoredQueue.map(MediaItemMapper::toMediaItem)
                player.setMediaItems(mediaItems)
                player.prepare()
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        audioEffectsManager.releaseEffects()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        exoPlayer = null
        super.onDestroy()
    }
}

