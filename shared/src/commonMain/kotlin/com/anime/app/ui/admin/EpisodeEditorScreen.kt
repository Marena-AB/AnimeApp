package com.anime.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anime.app.ui.components.SectionHeader
import com.anime.app.ui.player.formatPlaybackTime
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

@Composable
fun EpisodeEditorScreen(
    viewModel: EpisodeEditorViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    val videoPicker = rememberFilePickerLauncher(type = FileKitType.Video) { file ->
        if (file != null) viewModel.onVideoPicked(file.toMediaUrl())
    }
    val thumbnailPicker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file != null) viewModel.updateForm { it.copy(thumbnailUrl = file.toMediaUrl()) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        EditorTopBar(
            title = if (uiState.isNew) "New episode" else "Edit episode",
            canSave = uiState.canSave,
            onBack = onBack,
            onSave = viewModel::save,
        )

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val form = uiState.form
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (uiState.seriesTitle.isNotBlank()) {
                Text(
                    text = uiState.seriesTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            SectionHeader(title = "Media", eyebrow = "Source file")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Video", style = MaterialTheme.typography.labelLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalButton(onClick = { videoPicker.launch() }) {
                        Text(if (form.videoUrl.isBlank()) "Choose video" else "Replace video")
                    }
                    when {
                        uiState.isReadingVideo -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        form.videoUrl.isNotBlank() -> Text(
                            text = if (form.durationSeconds > 0) {
                                "Selected · ${formatPlaybackTime(form.durationSeconds * 1000L)}"
                            } else {
                                "Selected"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            ImagePickerField(
                label = "Thumbnail (defaults to the series cover)",
                imageUrl = form.thumbnailUrl,
                aspectRatio = 16f / 9f,
                onPick = { thumbnailPicker.launch() },
            )
            SectionHeader(title = "Episode details", eyebrow = "Publishing")
            OutlinedTextField(
                value = form.number,
                onValueChange = { value ->
                    viewModel.updateForm { it.copy(number = value.filter(Char::isDigit).take(4)) }
                },
                label = { Text("Episode number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.title,
                onValueChange = { value -> viewModel.updateForm { it.copy(title = value) } },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.description,
                onValueChange = { value -> viewModel.updateForm { it.copy(description = value) } },
                label = { Text("Description") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
            CreditFields(
                tools = form.tools,
                modelName = form.modelName,
                origin = form.origin,
                onToolsChange = { value -> viewModel.updateForm { it.copy(tools = value) } },
                onModelNameChange = { value -> viewModel.updateForm { it.copy(modelName = value) } },
                onOriginChange = { value -> viewModel.updateForm { it.copy(origin = value) } },
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Intro range (optional)", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = form.introStartSeconds,
                        onValueChange = { value ->
                            viewModel.updateForm {
                                it.copy(introStartSeconds = value.filter(Char::isDigit).take(5))
                            }
                        },
                        label = { Text("Start (sec)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !uiState.introRangeValid,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = form.introEndSeconds,
                        onValueChange = { value ->
                            viewModel.updateForm {
                                it.copy(introEndSeconds = value.filter(Char::isDigit).take(5))
                            }
                        },
                        label = { Text("End (sec)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = !uiState.introRangeValid,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(
                    text = if (uiState.introRangeValid) {
                        "Skip Intro appears only while playback is inside this range."
                    } else {
                        "Enter both values, with the end after the start."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (uiState.introRangeValid) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
        }
    }
}
