package com.anime.app.ui.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anime.app.theme.AnimeTheme
import com.anime.app.theme.DesignDirection
import com.anime.app.ui.components.BrandMark
import com.anime.app.ui.components.DesignedState
import com.anime.app.ui.components.MetadataPill
import com.anime.app.ui.components.PrimaryAction
import com.anime.app.ui.components.SecondaryAction
import com.anime.app.ui.components.SectionHeader
import com.anime.app.ui.components.SkeletonBlock

@Composable
fun ComponentGalleryScreen(
    direction: DesignDirection,
    onDirectionSelected: (DesignDirection) -> Unit,
    onBack: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                BrandMark(modifier = Modifier.padding(start = 4.dp))
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "Design lab", eyebrow = "Debug only")
                Text(
                    text = "Switch the complete application theme, then review every reusable building block below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DesignDirection.entries.forEach { option ->
                    FilterChip(
                        selected = direction == option,
                        onClick = { onDirectionSelected(option) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(option.label, style = MaterialTheme.typography.labelLarge)
                                Text(
                                    option.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        item {
            GallerySection("Color tokens") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ColorToken("Primary", MaterialTheme.colorScheme.primary)
                    ColorToken("Secondary", MaterialTheme.colorScheme.secondary)
                    ColorToken("Surface", MaterialTheme.colorScheme.surfaceContainerHigh)
                    ColorToken("Error", MaterialTheme.colorScheme.error)
                }
            }
        }
        item {
            GallerySection("Typography") {
                Text("Display / Original worlds", style = MaterialTheme.typography.headlineLarge)
                Text("Title / Continue watching", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Body / Independent animation deserves a stage built around the art.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("LABEL / EPISODE 04", style = MaterialTheme.typography.labelMedium)
            }
        }
        item {
            GallerySection("Actions & metadata") {
                PrimaryAction("Primary action", onClick = {}, modifier = Modifier.fillMaxWidth())
                SecondaryAction("Secondary action", onClick = {}, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetadataPill("FEATURED", accent = true)
                    MetadataPill("12 EPISODES")
                    MetadataPill("SCI-FI")
                }
            }
        }
        item {
            GallerySection("Poster card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .aspectRatio(2f / 3f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                ) {
                    Text(
                        "COVER ART",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(14.dp),
                    ) {
                        Text("Signal Bloom", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Drama · Sci-fi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            GallerySection("Progress & loading") {
                LinearProgressIndicator(progress = { 0.62f }, modifier = Modifier.fillMaxWidth())
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 5f)
                        .clip(MaterialTheme.shapes.medium),
                )
            }
        }
        item {
            GallerySection("Form controls") {
                OutlinedTextField(
                    value = "Episode title",
                    onValueChange = {},
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            SectionHeader(title = "Designed states", eyebrow = "Empty")
            DesignedState(
                title = "The reel is waiting",
                message = "Published episodes will appear here when they are ready.",
            )
        }
        item {
            DesignedState(
                title = "The signal dropped",
                message = "We couldn't load this collection. Try again when you're ready.",
                actionLabel = "Try again",
                onAction = {},
                isError = true,
            )
        }
    }
}

@Composable
private fun GallerySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionHeader(title = title, eyebrow = AnimeTheme.direction.label)
        content()
    }
}

@Composable
private fun ColorToken(label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color),
        )
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
