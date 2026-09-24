package com.anime.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anime.app.model.SeriesStatus
import com.anime.app.ui.components.SectionHeader
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher

@Composable
fun SeriesEditorScreen(
    viewModel: SeriesEditorViewModel,
    onBack: () -> Unit,
    onSaved: (seriesId: String, isNew: Boolean) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSeriesId) {
        uiState.savedSeriesId?.let { onSaved(it, uiState.isNew) }
    }

    val coverPicker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file != null) viewModel.updateForm { it.copy(coverUrl = file.toMediaUrl()) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        EditorTopBar(
            title = if (uiState.isNew) "New series" else "Edit series",
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
            ImagePickerField(
                label = "Cover",
                imageUrl = form.coverUrl,
                aspectRatio = 16f / 9f,
                onPick = { coverPicker.launch() },
            )
            SectionHeader(title = "Series details", eyebrow = "Publishing")
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
            OutlinedTextField(
                value = form.genres,
                onValueChange = { value -> viewModel.updateForm { it.copy(genres = value) } },
                label = { Text("Genres") },
                supportingText = { Text("Separate with commas") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SeriesStatus.entries.forEach { status ->
                        FilterChip(
                            selected = form.status == status,
                            onClick = { viewModel.updateForm { it.copy(status = status) } },
                            label = { Text(status.label) },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = form.attribution,
                onValueChange = { value -> viewModel.updateForm { it.copy(attribution = value) } },
                label = { Text("Credits / license (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val SeriesStatus.label: String
    get() = when (this) {
        SeriesStatus.ONGOING -> "Ongoing"
        SeriesStatus.COMPLETED -> "Completed"
        SeriesStatus.HIATUS -> "Hiatus"
    }
