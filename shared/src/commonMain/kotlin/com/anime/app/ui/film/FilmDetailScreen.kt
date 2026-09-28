package com.anime.app.ui.film

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.anime.app.model.Film
import com.anime.app.ui.components.DesignedState
import com.anime.app.ui.components.MetadataPill
import com.anime.app.ui.components.SkeletonBlock

@Composable
fun FilmDetailScreen(
    viewModel: FilmDetailViewModel,
    onBack: () -> Unit,
    onPlay: (Film) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val film = uiState.film

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        when {
            uiState.isLoading -> {
                SkeletonBlock(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium))
                SkeletonBlock(modifier = Modifier.fillMaxWidth(0.5f).padding(vertical = 8.dp))
            }
            film == null -> {
                DesignedState(
                    title = "This film is gone",
                    message = "It is no longer in the catalog.",
                )
            }
            else -> {
                AsyncImage(
                    model = film.thumbnailUrl.ifBlank { null },
                    contentDescription = film.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop,
                )
                uiState.creator?.let { creator ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = creator.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        FilledTonalButton(onClick = viewModel::toggleFollow) {
                            Text(if (uiState.isFollowing) "Following" else "Follow")
                        }
                    }
                }
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.headlineMedium,
                )
                MetadataPill(text = "FILM", accent = true)
                if (film.description.isNotBlank()) {
                    Text(
                        text = film.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = { onPlay(film) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text("Play", modifier = Modifier.padding(start = 6.dp))
                }
                FilledTonalButton(
                    onClick = viewModel::toggleLike,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.isLiked) "Liked" else "Like")
                }
            }
        }
    }
}
