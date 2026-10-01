package com.anime.app.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * Platform playback engine (Media3 on Android, AVPlayer on iOS).
 * Properties are backed by Compose snapshot state, so reading them in a composable recomposes on change.
 */
@Stable
interface VideoPlayerController {
    val isPlaying: Boolean
    val isBuffering: Boolean
    val isEnded: Boolean
    val positionMs: Long
    val durationMs: Long
    val error: String?

    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
}

/** Creates a controller for [url] that starts playing at [startPositionMs] and is released when it leaves composition. */
@Composable
expect fun rememberVideoPlayerController(url: String, startPositionMs: Long = 0L): VideoPlayerController

/** Renders the video frames of [controller]. Draws no controls and consumes no touches. */
@Composable
expect fun VideoSurface(controller: VideoPlayerController, modifier: Modifier = Modifier)

/** While [enabled], locks the screen to landscape and hides the system bars. */
@Composable
expect fun FullscreenEffect(enabled: Boolean)
