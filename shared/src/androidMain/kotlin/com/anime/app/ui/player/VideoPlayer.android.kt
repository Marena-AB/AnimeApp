package com.anime.app.ui.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

private const val POSITION_POLL_MS = 250L

private class ExoVideoPlayerController(val player: ExoPlayer) : VideoPlayerController {
    override var isPlaying by mutableStateOf(false)
        private set
    override var isBuffering by mutableStateOf(true)
        private set
    override var isEnded by mutableStateOf(false)
        private set
    override var positionMs by mutableLongStateOf(0L)
        private set
    override var durationMs by mutableLongStateOf(0L)
        private set
    override var error by mutableStateOf<String?>(null)
        private set

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            isBuffering = playbackState == Player.STATE_BUFFERING
            isEnded = playbackState == Player.STATE_ENDED
            syncPosition()
        }

        override fun onPlayerError(playbackError: PlaybackException) {
            error = playbackError.message ?: "Playback failed"
        }
    }

    init {
        player.addListener(listener)
    }

    fun syncPosition() {
        positionMs = player.currentPosition
        durationMs = player.duration.takeIf { it != C.TIME_UNSET } ?: 0L
    }

    override fun play() = player.play()

    override fun pause() = player.pause()

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        this.positionMs = positionMs
    }

    fun release() {
        player.removeListener(listener)
        player.release()
    }
}

@Composable
actual fun rememberVideoPlayerController(url: String, startPositionMs: Long): VideoPlayerController {
    val context = LocalContext.current
    val controller = remember(url) {
        val player = ExoPlayer.Builder(context.applicationContext).build().apply {
            setMediaItem(MediaItem.fromUri(url), startPositionMs)
            playWhenReady = true
            prepare()
        }
        ExoVideoPlayerController(player)
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    LaunchedEffect(controller) {
        while (true) {
            controller.syncPosition()
            delay(POSITION_POLL_MS)
        }
    }
    return controller
}

@Composable
actual fun VideoSurface(controller: VideoPlayerController, modifier: Modifier) {
    val player = (controller as ExoVideoPlayerController).player
    key(player) {
        AndroidView(
            factory = { context ->
                object : PlayerView(context) {
                    override fun dispatchKeyEvent(event: KeyEvent): Boolean = false
                }.apply {
                    useController = false
                    isFocusable = false
                    isFocusableInTouchMode = false
                    descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                    this.player = player
                    isFocusable = false
                }
            },
            onRelease = { it.player = null },
            modifier = modifier,
        )
    }
}

@Composable
actual fun FullscreenEffect(enabled: Boolean) {
    val activity = LocalContext.current.findActivity() ?: return
    DisposableEffect(enabled, activity) {
        if (!enabled) return@DisposableEffect onDispose { }
        val insetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
