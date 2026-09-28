package com.anime.app.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anime.app.theme.AnimeTheme
import com.anime.app.ui.components.DesignedState
import kotlinx.coroutines.delay

private const val CONTROLS_AUTO_HIDE_MS = 3_000L
private const val DOUBLE_TAP_SEEK_MS = 10_000L

private enum class SeekFeedback(val label: String) {
    BACK("-10"),
    FORWARD("+10"),
}

@Composable
fun VideoPlayer(
    controller: VideoPlayerController,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var interactionCount by remember { mutableIntStateOf(0) }
    var seekFeedback by remember { mutableStateOf<SeekFeedback?>(null) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(controlsVisible, controller.isPlaying, interactionCount) {
        if (controlsVisible && controller.isPlaying) {
            delay(CONTROLS_AUTO_HIDE_MS)
            controlsVisible = false
        }
    }
    LaunchedEffect(seekFeedback) {
        if (seekFeedback != null) {
            delay(650)
            seekFeedback = null
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        VideoSurface(controller = controller, modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(controller) {
                    detectTapGestures(
                        onTap = { controlsVisible = true },
                        onDoubleTap = { offset ->
                            val direction = if (offset.x < size.width / 2f) {
                                SeekFeedback.BACK
                            } else {
                                SeekFeedback.FORWARD
                            }
                            val delta = if (direction == SeekFeedback.BACK) {
                                -DOUBLE_TAP_SEEK_MS
                            } else {
                                DOUBLE_TAP_SEEK_MS
                            }
                            val upperBound = controller.durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE
                            controller.seekTo((controller.positionMs + delta).coerceIn(0L, upperBound))
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            seekFeedback = direction
                            interactionCount++
                        },
                    )
                },
        )

        if (controller.isBuffering && !controller.isEnded) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        controller.error?.let { message ->
            DesignedState(
                title = "Playback lost the thread",
                message = message,
                actionLabel = "Try again",
                onAction = controller::play,
                isError = true,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            PlayerControls(
                controller = controller,
                isFullscreen = isFullscreen,
                onToggleFullscreen = onToggleFullscreen,
                onInteraction = { interactionCount++ },
                onDismiss = { controlsVisible = false },
            )
        }

        val motion = AnimeTheme.motion
        AnimatedVisibility(
            visible = seekFeedback != null,
            enter = fadeIn(tween(motion.quickMillis, easing = motion.easing)) +
                scaleIn(
                    animationSpec = tween(motion.standardMillis, easing = motion.easing),
                    initialScale = 0.35f,
                ),
            exit = fadeOut(tween(motion.standardMillis, easing = motion.easing)) +
                scaleOut(
                    animationSpec = tween(motion.standardMillis, easing = motion.easing),
                    targetScale = 1.45f,
                ),
            modifier = Modifier.align(
                if (seekFeedback == SeekFeedback.BACK) Alignment.CenterStart else Alignment.CenterEnd,
            ),
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.62f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${seekFeedback?.label.orEmpty()}s",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }

        overlay()

        if (showBackButton) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .zIndex(1f)
                    .then(if (isFullscreen) Modifier.safeDrawingPadding() else Modifier)
                    .padding(4.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    }
}

@Composable
private fun PlayerControls(
    controller: VideoPlayerController,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onInteraction: () -> Unit,
    onDismiss: () -> Unit,
) {
    var scrubPositionMs by remember { mutableStateOf<Float?>(null) }
    val haptics = LocalHapticFeedback.current
    val durationMs = controller.durationMs.coerceAtLeast(0L)
    val displayedPositionMs = scrubPositionMs?.toLong() ?: controller.positionMs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .then(if (isFullscreen) Modifier.safeDrawingPadding() else Modifier),
    ) {
        IconButton(
            onClick = {
                onInteraction()
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                when {
                    controller.isPlaying -> controller.pause()
                    controller.isEnded -> {
                        controller.seekTo(0L)
                        controller.play()
                    }
                    else -> controller.play()
                }
            },
            modifier = Modifier.align(Alignment.Center).size(72.dp),
        ) {
            Icon(
                imageVector = if (controller.isPlaying) PlayerIcons.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (controller.isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(56.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Slider(
                value = displayedPositionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                onValueChange = {
                    onInteraction()
                    scrubPositionMs = it
                },
                onValueChangeFinished = {
                    scrubPositionMs?.let { controller.seekTo(it.toLong()) }
                    scrubPositionMs = null
                },
                valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
                enabled = durationMs > 0L,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                ),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${formatPlaybackTime(displayedPositionMs)} / ${formatPlaybackTime(durationMs)}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        onInteraction()
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleFullscreen()
                    },
                ) {
                    Icon(
                        imageVector = if (isFullscreen) PlayerIcons.FullscreenExit else PlayerIcons.Fullscreen,
                        contentDescription = if (isFullscreen) "Exit fullscreen" else "Fullscreen",
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

internal fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return if (hours > 0) "$hours:${minutes.toString().padStart(2, '0')}:$seconds" else "$minutes:$seconds"
}
