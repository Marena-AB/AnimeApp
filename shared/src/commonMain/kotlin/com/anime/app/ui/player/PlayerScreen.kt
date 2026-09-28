package com.anime.app.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.anime.app.model.Film
import com.anime.app.ui.components.MetadataPill
import com.anime.app.ui.components.SkeletonBlock
import kotlinx.coroutines.delay

private const val AUTO_NEXT_SECONDS = 5

private data class PlaybackSnapshot(
    val positionMs: Long,
    val durationMs: Long,
    val isPlaying: Boolean,
    val isEnded: Boolean,
)

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val film = uiState.film

    if (film == null) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SkeletonBlock(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth(0.5f).height(30.dp).padding(horizontal = 20.dp),
            )
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth(0.82f).height(20.dp).padding(horizontal = 20.dp),
            )
        }
        return
    }

    FullscreenEffect(enabled = uiState.isFullscreen)
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = uiState.isFullscreen,
        onBackCompleted = viewModel::exitFullscreen,
    )

    val controller = rememberVideoPlayerController(
        url = film.videoUrl,
        startPositionMs = remember(film.id) { viewModel.startPositionMs(film.id) },
    )
    LaunchedEffect(controller, film.id) {
        snapshotFlow {
            PlaybackSnapshot(controller.positionMs, controller.durationMs, controller.isPlaying, controller.isEnded)
        }.collect { snapshot ->
            viewModel.onPlaybackUpdate(
                filmId = film.id,
                positionMs = snapshot.positionMs,
                durationMs = snapshot.durationMs,
                isPlaying = snapshot.isPlaying,
                isEnded = snapshot.isEnded,
            )
        }
    }

    val nextFilm = uiState.nextFilm
    var autoNextCancelled by remember(film.id) { mutableStateOf(false) }
    var secondsRemaining by remember(film.id) { mutableIntStateOf(AUTO_NEXT_SECONDS) }
    val showUpNext = controller.isEnded && nextFilm != null && !autoNextCancelled

    LaunchedEffect(controller.isEnded) {
        if (!controller.isEnded) autoNextCancelled = false
    }
    LaunchedEffect(showUpNext, nextFilm) {
        secondsRemaining = AUTO_NEXT_SECONDS
        if (!showUpNext) return@LaunchedEffect
        while (secondsRemaining > 0) {
            delay(1_000)
            secondsRemaining--
        }
        viewModel.playFilm(nextFilm.id)
    }

    val handleBack = {
        if (uiState.isFullscreen) viewModel.exitFullscreen() else onBack()
    }

    // The player stays at the same position in the tree in both modes so the surface isn't recreated.
    Column(
        modifier = if (uiState.isFullscreen) {
            Modifier.fillMaxSize().background(Color.Black)
        } else {
            Modifier.fillMaxSize().safeDrawingPadding()
        },
    ) {
        VideoPlayer(
            controller = controller,
            isFullscreen = uiState.isFullscreen,
            onToggleFullscreen = viewModel::toggleFullscreen,
            onBack = handleBack,
            modifier = if (uiState.isFullscreen) {
                Modifier.fillMaxSize()
            } else {
                Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            },
            overlay = {
                if (showUpNext) {
                    UpNextOverlay(
                        nextFilm = nextFilm,
                        secondsRemaining = secondsRemaining,
                        onPlayNow = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.playFilm(nextFilm.id)
                        },
                        onCancel = { autoNextCancelled = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                val introStart = film.introStartSeconds
                val introEnd = film.introEndSeconds
                val positionSeconds = controller.positionMs / 1000L
                if (
                    !showUpNext &&
                    introStart != null &&
                    introEnd != null &&
                    positionSeconds in introStart.toLong() until introEnd.toLong()
                ) {
                    FilledTonalButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            controller.seekTo(introEnd * 1000L)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 18.dp, bottom = 74.dp),
                    ) {
                        Text("Skip Intro")
                    }
                }
            },
        )

        if (!uiState.isFullscreen) {
            FilmDetails(
                film = film,
                seriesTitle = uiState.series?.title,
                attribution = uiState.series?.attribution,
                nextFilm = nextFilm,
                onPlayNext = { next -> viewModel.playFilm(next.id) },
            )
        }
    }
}

@Composable
private fun UpNextOverlay(
    nextFilm: Film,
    secondsRemaining: Int,
    onPlayNow: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.76f))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 380.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "UP NEXT IN $secondsRemaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = nextFilm.episodeNumber?.let { "Ep. $it: ${nextFilm.title}" } ?: nextFilm.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Button(onClick = onPlayNow) { Text("Play now") }
                }
            }
        }
    }
}

@Composable
private fun FilmDetails(
    film: Film,
    seriesTitle: String?,
    attribution: String?,
    nextFilm: Film?,
    onPlayNext: (Film) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        seriesTitle?.let {
            Text(
                text = it.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetadataPill(
                text = film.episodeNumber?.let { "EPISODE $it" } ?: "FILM",
                accent = true,
            )
            if (film.durationSeconds > 0) {
                MetadataPill(text = formatPlaybackTime(film.durationSeconds * 1000L))
            }
        }
        Text(
            text = film.title,
            style = MaterialTheme.typography.headlineMedium,
        )
        if (film.description.isNotBlank()) {
            Text(
                text = film.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        attribution?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        nextFilm?.let { next ->
            FilledTonalButton(
                onClick = { onPlayNext(next) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    text = next.episodeNumber?.let { "Next · Ep. $it: ${next.title}" } ?: "Next · ${next.title}",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
