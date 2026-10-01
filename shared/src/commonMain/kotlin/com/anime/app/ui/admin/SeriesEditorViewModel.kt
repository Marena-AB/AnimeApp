package com.anime.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.ContentRepository
import com.anime.app.data.NewSeries
import com.anime.app.model.Series
import com.anime.app.model.SeriesStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SeriesForm(
    val title: String = "",
    val description: String = "",
    /** Comma-separated in the form; split on save. */
    val genres: String = "",
    val status: SeriesStatus = SeriesStatus.ONGOING,
    val coverUrl: String = "",
    val attribution: String = "",
)

data class SeriesEditorUiState(
    val isNew: Boolean,
    val form: SeriesForm = SeriesForm(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val savedSeriesId: String? = null,
) {
    val canSave: Boolean
        get() = !isLoading && !isSaving && form.title.isNotBlank()
}

class SeriesEditorViewModel(
    private val repository: ContentRepository,
    seriesId: String?,
) : ViewModel() {
    private var original: Series? = null
    private val state = MutableStateFlow(SeriesEditorUiState(isNew = seriesId == null, isLoading = seriesId != null))
    val uiState: StateFlow<SeriesEditorUiState> = state

    init {
        if (seriesId != null) {
            viewModelScope.launch {
                val series = repository.observeSeries(seriesId).first()
                original = series
                state.update {
                    it.copy(form = series?.toForm() ?: SeriesForm(), isLoading = false)
                }
            }
        }
    }

    fun updateForm(transform: (SeriesForm) -> SeriesForm) {
        state.update { it.copy(form = transform(it.form)) }
    }

    fun save() {
        val current = state.value
        if (!current.canSave) return
        state.update { it.copy(isSaving = true) }
        val form = current.form
        val genres = form.genres.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        viewModelScope.launch {
            val existing = original
            val savedId = if (existing == null) {
                repository.createSeries(
                    NewSeries(
                        title = form.title.trim(),
                        description = form.description.trim(),
                        coverUrl = form.coverUrl,
                        genres = genres,
                        status = form.status,
                        attribution = form.attribution.trim(),
                    ),
                ).id
            } else {
                repository.updateSeries(
                    existing.copy(
                        title = form.title.trim(),
                        description = form.description.trim(),
                        coverUrl = form.coverUrl,
                        genres = genres,
                        status = form.status,
                        attribution = form.attribution.trim(),
                    ),
                )
                existing.id
            }
            state.update { it.copy(isSaving = false, savedSeriesId = savedId) }
        }
    }

    private fun Series.toForm() = SeriesForm(
        title = title,
        description = description,
        genres = genres.joinToString(", "),
        status = status,
        coverUrl = coverUrl,
        attribution = attribution,
    )
}
