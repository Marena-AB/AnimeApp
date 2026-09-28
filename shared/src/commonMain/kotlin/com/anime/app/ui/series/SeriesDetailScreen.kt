package com.anime.app.ui.series

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.anime.app.model.Film
import com.anime.app.model.WatchProgress
import com.anime.app.ui.components.MetadataPill
import com.anime.app.ui.components.SectionHeader
import com.anime.app.ui.components.DesignedState
import com.anime.app.ui.components.SkeletonBlock
import com.anime.app.theme.AnimeTheme
import com.anime.app.theme.platformBlur
import com.kmpalette.extensions.network.rememberNetworkDominantColorState
import io.ktor.http.Url

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SeriesDetailScreen(
    viewModel: SeriesDetailViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    onFilmClick: (Film) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val series = uiState.series

    if (uiState.isLoading || series == null) {
        SeriesDetailSkeleton()
        return
    }

    val baseColors = MaterialTheme.colorScheme
    val dominantColor = rememberNetworkDominantColorState(
        defaultColor = baseColors.primary,
        defaultOnColor = baseColors.onPrimary,
    )
    LaunchedEffect(series.coverUrl) {
        if (series.coverUrl.startsWith("http")) {
            runCatching { dominantColor.updateFrom(Url(series.coverUrl)) }
        }
    }
    val accentColors = baseColors.copy(
        primary = dominantColor.color,
        onPrimary = dominantColor.onColor,
        primaryContainer = dominantColor.color.copy(alpha = 0.22f),
        onPrimaryContainer = baseColors.onSurface,
    )
    val sharedPosterModifier = if (AnimeTheme.motion.reduced) {
        Modifier
    } else {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState("series-poster-${series.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    }

    MaterialTheme(colorScheme = accentColors) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = series.coverUrl.ifBlank { null },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(430.dp)
                    .platformBlur(34.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.48f,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.28f),
                            0.62f to baseColors.background.copy(alpha = 0.78f),
                            1f to baseColors.background,
                        ),
                    ),
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                }
                item {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        AsyncImage(
                            model = series.coverUrl.ifBlank { null },
                            contentDescription = series.title,
                            modifier = Modifier
                                .then(sharedPosterModifier)
                                .width(132.dp)
                                .aspectRatio(2f / 3f)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = ContentScale.Crop,
                        )
                        Column(
                            modifier = Modifier.weight(1f).padding(bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            MetadataPill(
                                text = series.status.name.lowercase().replaceFirstChar { it.uppercase() },
                                accent = true,
                            )
                            Text(
                                text = series.title,
                                style = MaterialTheme.typography.headlineLarge,
                            )
                            if (uiState.creatorName.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = uiState.creatorName,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    FilledTonalButton(onClick = viewModel::toggleFollow) {
                                        Text(if (uiState.isFollowing) "Following" else "Follow")
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                series.genres.take(2).forEach { genre -> MetadataPill(genre) }
                            }
                        }
                    }
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (series.description.isNotBlank()) {
                            Text(
                                text = series.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (series.attribution.isNotBlank()) {
                            Text(
                                text = series.attribution,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                val playFilm = uiState.films.firstOrNull {
                    uiState.progress[it.id]?.completed != true
                } ?: uiState.films.firstOrNull()
                if (playFilm != null) {
                    item {
                        val progress = uiState.progress[playFilm.id]
                        Button(
                            onClick = { onFilmClick(playFilm) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Text(
                                text = if (progress != null && !progress.completed) {
                                    "Resume episode ${playFilm.episodeNumber}"
                                } else {
                                    "Play episode ${playFilm.episodeNumber}"
                                },
                                modifier = Modifier.padding(start = 6.dp),
                            )
                        }
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = viewModel::toggleLike,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (uiState.isLiked) "Liked" else "Like")
                    }
                }
                item {
                    SectionHeader(
                        title = "Episodes",
                        eyebrow = "${uiState.films.size} available",
                    )
                }
                if (uiState.films.isEmpty()) {
                    item {
                        DesignedState(
                            title = "The reel is waiting",
                            message = "Episodes will appear here when they are ready to premiere.",
                        )
                    }
                }
                items(uiState.films, key = { it.id }) { film ->
                    EpisodeRow(
                        film = film,
                        progress = uiState.progress[film.id],
                        onClick = { onFilmClick(film) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(
    film: Film,
    progress: WatchProgress?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = film.thumbnailUrl.ifBlank { null },
                contentDescription = film.title,
                modifier = Modifier
                    .weight(0.4f)
                    .aspectRatio(16f / 9f)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.weight(0.6f)) {
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = when {
                        progress?.completed == true -> "EP ${film.episodeNumber}  ·  ${formatDuration(film.durationSeconds)}  ·  WATCHED"
                        else -> "EP ${film.episodeNumber}  ·  ${formatDuration(film.durationSeconds)}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                if (progress != null && !progress.completed) {
                    LinearProgressIndicator(
                        progress = { progress.fraction },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        drawStopIndicator = {},
                    )
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    if (totalSeconds <= 0) return "--:--"
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

@Composable
private fun SeriesDetailSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(MaterialTheme.shapes.small),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            SkeletonBlock(
                modifier = Modifier
                    .width(132.dp)
                    .aspectRatio(2f / 3f)
                    .clip(MaterialTheme.shapes.medium),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth().height(32.dp).clip(MaterialTheme.shapes.small),
                )
                SkeletonBlock(
                    modifier = Modifier.fillMaxWidth(0.72f).height(22.dp).clip(MaterialTheme.shapes.small),
                )
            }
        }
        repeat(3) {
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth().height(92.dp).clip(MaterialTheme.shapes.medium),
            )
        }
    }
}
