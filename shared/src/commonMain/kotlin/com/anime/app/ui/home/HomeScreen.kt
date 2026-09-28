package com.anime.app.ui.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.anime.app.model.Film
import com.anime.app.model.Series
import com.anime.app.ui.components.BrandMark
import com.anime.app.ui.components.DesignedState
import com.anime.app.ui.components.MetadataPill
import com.anime.app.ui.components.PressScaleSurface
import com.anime.app.ui.components.SecondaryAction
import com.anime.app.ui.components.SectionHeader
import com.anime.app.ui.components.SkeletonBlock
import com.anime.app.theme.AnimeTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onSeriesClick: (Series) -> Unit,
    onFilmClick: (Film) -> Unit,
    onPlayFilm: (Film) -> Unit,
    onOpenStudio: () -> Unit,
    onOpenDesignLab: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    if (uiState.isLoading) {
        HomeSkeleton()
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val gridState = rememberLazyGridState()
        val motion = AnimeTheme.motion
        val parallaxOffset by remember(gridState, motion.reduced) {
            derivedStateOf {
                if (!motion.reduced && gridState.firstVisibleItemIndex == 0) {
                    gridState.firstVisibleItemScrollOffset * 0.32f
                } else {
                    0f
                }
            }
        }
        val screenWidth = maxWidth
        val heroHeight = maxHeight * 0.72f

        Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            state = gridState,
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
        uiState.featured?.let { featured ->
            item(key = "featured", span = { GridItemSpan(maxLineSpan) }) {
                FeaturedBanner(
                    series = featured,
                    creatorName = uiState.featuredCreatorName,
                    isFollowing = uiState.isFeaturedCreatorFollowed,
                    onToggleFollow = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.toggleFollow(uiState.featuredCreatorId)
                    },
                    film = uiState.featuredFilm,
                    onOpen = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSeriesClick(featured)
                    },
                    onPlay = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onPlayFilm(it)
                    },
                    parallaxOffset = parallaxOffset,
                    modifier = Modifier
                        .requiredWidth(screenWidth)
                        .graphicsLayer { translationX = -20.dp.toPx() }
                        .height(heroHeight),
                )
            }
        }

        if (uiState.following.isNotEmpty()) {
            item(key = "following", span = { GridItemSpan(maxLineSpan) }) {
                FollowingRow(
                    entries = uiState.following,
                    onOpen = { entry ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        when (entry) {
                            is FollowingEntry.SeriesEntry -> onSeriesClick(entry.series)
                            is FollowingEntry.FilmEntry -> onFilmClick(entry.film)
                        }
                    },
                )
            }
        }

        if (uiState.continueWatching.isNotEmpty()) {
            item(key = "continue-watching", span = { GridItemSpan(maxLineSpan) }) {
                ContinueWatchingRow(
                    items = uiState.continueWatching,
                    onPlay = onPlayFilm,
                )
            }
        }

        uiState.becauseYouWatched?.let { shelf ->
            item(key = "because-you-watched", span = { GridItemSpan(maxLineSpan) }) {
                ShelfRow(
                    title = shelf.watchedTitle,
                    eyebrow = "Because you watched",
                    entries = shelf.entries,
                    onOpen = { entry ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        openShelfEntry(entry, onSeriesClick, onFilmClick)
                    },
                )
            }
        }

        if (uiState.recommended.isNotEmpty()) {
            item(key = "recommended", span = { GridItemSpan(maxLineSpan) }) {
                ShelfRow(
                    title = "Recommended",
                    eyebrow = "More series",
                    entries = uiState.recommended,
                    onOpen = { entry ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        openShelfEntry(entry, onSeriesClick, onFilmClick)
                    },
                )
            }
        }

        if (uiState.oneOffs.isNotEmpty()) {
            item(key = "films", span = { GridItemSpan(maxLineSpan) }) {
                FilmsRow(
                    films = uiState.oneOffs,
                    creatorName = { film -> uiState.creatorsById[film.creatorId]?.displayName.orEmpty() },
                    onOpen = { film ->
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFilmClick(film)
                    },
                )
            }
        }

        item(key = "all-series-header", span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title = "All series",
                eyebrow = "${uiState.series.size} titles",
            )
        }
        items(uiState.series, key = { it.id }) { series ->
            SeriesCard(
                series = series,
                creatorName = uiState.creatorsById[series.creatorId]?.displayName.orEmpty(),
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSeriesClick(series)
                },
            )
        }
        if (uiState.series.isEmpty()) {
            item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                DesignedState(
                    title = "The marquee is quiet",
                    message = "New animated worlds will appear here as soon as they premiere.",
                )
            }
        }
        }
        HomeHeader(
            onOpenStudio = onOpenStudio,
            onOpenDesignLab = onOpenDesignLab,
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        )
        }
    }
}

@Composable
private fun HomeHeader(
    onOpenStudio: () -> Unit,
    onOpenDesignLab: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandMark()
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onOpenStudio) {
            Text("MyStudio")
        }
        IconButton(
            onClick = onOpenDesignLab,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Design lab")
        }
    }
}

@Composable
private fun FeaturedBanner(
    series: Series,
    creatorName: String,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    film: Film?,
    onOpen: () -> Unit,
    onPlay: (Film) -> Unit,
    parallaxOffset: Float,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        AsyncImage(
            model = series.coverUrl,
            contentDescription = series.title,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = parallaxOffset
                    scaleX = 1.08f
                    scaleY = 1.08f
                },
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.28f),
                        0.42f to Color.Transparent,
                        0.72f to Color.Black.copy(alpha = 0.55f),
                        1f to MaterialTheme.colorScheme.background,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetadataPill("FEATURED", accent = true)
            Text(
                text = series.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
            )
            if (creatorName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = creatorName,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                    TextButton(
                        onClick = onToggleFollow,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                    ) {
                        Text(if (isFollowing) "Following" else "Follow")
                    }
                }
            }
            Text(
                text = series.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (film != null) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { onPlay(film) },
                        modifier = Modifier.height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                        ),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Text("Play", modifier = Modifier.padding(start = 4.dp))
                    }
                    SecondaryAction(text = "Details", onClick = onOpen)
                }
            }
        }
    }
}

private fun openShelfEntry(
    entry: FollowingEntry,
    onSeriesClick: (Series) -> Unit,
    onFilmClick: (Film) -> Unit,
) {
    when (entry) {
        is FollowingEntry.SeriesEntry -> onSeriesClick(entry.series)
        is FollowingEntry.FilmEntry -> onFilmClick(entry.film)
    }
}

@Composable
private fun FollowingRow(
    entries: List<FollowingEntry>,
    onOpen: (FollowingEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    ShelfRow(
        title = "Following",
        eyebrow = "Creators you follow",
        entries = entries,
        onOpen = onOpen,
        modifier = modifier,
    )
}

@Composable
private fun ShelfRow(
    title: String,
    eyebrow: String,
    entries: List<FollowingEntry>,
    onOpen: (FollowingEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = title, eyebrow = eyebrow)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(entries, key = { it.key }) { entry ->
                FollowingCard(entry = entry, onClick = { onOpen(entry) })
            }
        }
    }
}

@Composable
private fun FollowingCard(
    entry: FollowingEntry,
    onClick: () -> Unit,
) {
    PressScaleSurface(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            AsyncImage(
                model = entry.imageUrl.ifBlank { null },
                contentDescription = entry.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = entry.creatorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun ContinueWatchingRow(
    items: List<ContinueWatchingItem>,
    onPlay: (Film) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Continue watching", eyebrow = "Your queue")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.film.id }) { item ->
                ContinueWatchingCard(item = item, onClick = { onPlay(item.film) })
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    item: ContinueWatchingItem,
    onClick: () -> Unit,
) {
    PressScaleSurface(
        onClick = onClick,
        modifier = Modifier.width(252.dp),
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Box {
                AsyncImage(
                    model = item.film.thumbnailUrl,
                    contentDescription = item.film.title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop,
                )
                LinearProgressIndicator(
                    progress = { item.progress.fraction },
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    trackColor = Color.Black.copy(alpha = 0.5f),
                    drawStopIndicator = {},
                )
            }
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = item.series?.title ?: item.creatorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.film.episodeNumber?.let { "Ep. $it: ${item.film.title}" } ?: item.film.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun FilmsRow(
    films: List<Film>,
    creatorName: (Film) -> String,
    onOpen: (Film) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Short films", eyebrow = "On their own")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(films, key = { it.id }) { film ->
                FilmCard(
                    film = film,
                    creatorName = creatorName(film),
                    onClick = { onOpen(film) },
                )
            }
        }
    }
}

@Composable
private fun FilmCard(
    film: Film,
    creatorName: String,
    onClick: () -> Unit,
) {
    PressScaleSurface(
        onClick = onClick,
        modifier = Modifier.width(220.dp),
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            AsyncImage(
                model = film.thumbnailUrl,
                contentDescription = film.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = creatorName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SeriesCard(
    series: Series,
    creatorName: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
    PressScaleSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = AnimeTheme.elevation.card),
        ) {
            Box {
            AsyncImage(
                model = series.coverUrl,
                contentDescription = series.title,
                modifier = Modifier
                    .then(sharedPosterModifier)
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.52f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.92f),
                        ),
                    ),
            )
            MetadataPill(
                text = series.status.name.lowercase().replaceFirstChar { it.uppercase() },
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            )
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                )
                if (creatorName.isNotBlank()) {
                    Text(
                        text = creatorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (series.genres.isNotEmpty()) {
                    Text(
                        text = series.genres.take(2).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun HomeSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.56f)
                .aspectRatio(5f)
                .clip(MaterialTheme.shapes.small),
        )
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10.5f)
                .clip(MaterialTheme.shapes.large),
        )
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.52f)
                .aspectRatio(5f)
                .clip(MaterialTheme.shapes.small),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(2) {
                SkeletonBlock(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(2f / 3f)
                        .clip(MaterialTheme.shapes.medium),
                )
            }
        }
    }
}
