package com.anime.app.ui.studio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anime.app.model.Film
import com.anime.app.model.Series
import com.anime.app.ui.admin.ConfirmDeleteDialog
import com.anime.app.ui.components.DesignedState
import com.anime.app.ui.components.SectionHeader
import com.anime.app.ui.components.SkeletonBlock

@Composable
fun CreatorStudioScreen(
    viewModel: CreatorStudioViewModel,
    onBack: () -> Unit,
    onNewFilm: () -> Unit,
    onNewSeries: () -> Unit,
    onEditSeries: (Series) -> Unit,
    onAddEpisode: (Series) -> Unit,
    onEditFilm: (Film) -> Unit,
    onOpenSeries: (Series) -> Unit,
    onOpenFilm: (Film) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var seriesPendingDelete by remember { mutableStateOf<StudioSeries?>(null) }
    var filmPendingDelete by remember { mutableStateOf<Film?>(null) }
    val creator = uiState.creator

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        when {
            uiState.isLoading -> {
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.6f))
                SkeletonBlock(modifier = Modifier.fillMaxWidth())
            }
            creator == null -> {
                DesignedState(
                    title = "Studio unavailable",
                    message = "This creator is not in the catalog.",
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = creator.displayName,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    if (creator.bio.isNotBlank()) {
                        Text(
                            text = creator.bio,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onNewFilm) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("New film", modifier = Modifier.padding(start = 6.dp))
                    }
                    FilledTonalButton(onClick = onNewSeries) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("New series", modifier = Modifier.padding(start = 6.dp))
                    }
                }
                if (uiState.isEmpty) {
                    DesignedState(
                        title = "Nothing published yet",
                        message = "Publish a film on its own, or start a series and add episodes.",
                    )
                }
                if (uiState.films.isNotEmpty()) {
                    SectionHeader(title = "Films", eyebrow = "${uiState.films.size} one-offs")
                    uiState.films.forEach { film ->
                        StudioWorkCard(
                            title = film.title,
                            detail = "One-off",
                            onOpen = { onOpenFilm(film) },
                            onEdit = { onEditFilm(film) },
                            onDelete = { filmPendingDelete = film },
                        )
                    }
                }
                if (uiState.series.isNotEmpty()) {
                    SectionHeader(title = "Series", eyebrow = "${uiState.series.size} titles")
                    uiState.series.forEach { item ->
                        StudioWorkCard(
                            title = item.series.title,
                            detail = "${item.episodeCount} episodes · ${item.series.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            onOpen = { onOpenSeries(item.series) },
                            onEdit = { onEditSeries(item.series) },
                            onAdd = { onAddEpisode(item.series) },
                            onDelete = { seriesPendingDelete = item },
                        )
                    }
                }
            }
        }
    }

    seriesPendingDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Delete series?",
            message = "\"${item.series.title}\" and its ${item.episodeCount} episode(s) will be removed.",
            onConfirm = {
                seriesPendingDelete = null
                viewModel.deleteSeries(item.series.id)
            },
            onDismiss = { seriesPendingDelete = null },
        )
    }
    filmPendingDelete?.let { film ->
        ConfirmDeleteDialog(
            title = "Delete film?",
            message = "\"${film.title}\" will be removed.",
            onConfirm = {
                filmPendingDelete = null
                viewModel.deleteFilm(film.id)
            },
            onDismiss = { filmPendingDelete = null },
        )
    }
}

@Composable
private fun StudioWorkCard(
    title: String,
    detail: String,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdd: (() -> Unit)? = null,
) {
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onAdd != null) {
                IconButton(onClick = onAdd) {
                    Icon(Icons.Filled.Add, contentDescription = "Add episode")
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
