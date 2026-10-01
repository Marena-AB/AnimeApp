package com.anime.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.ContentRepository
import com.anime.app.data.NewFilm
import com.anime.app.model.Film
import com.anime.app.model.FilmOrigin
import com.anime.app.model.PrototypeCreator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class OneOffFilmForm(
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val durationSeconds: Int = 0,
    val tools: String = "",
    val modelName: String = "",
    val origin: FilmOrigin = FilmOrigin.UNCLEAR,
)

data class FilmEditorUiState(
    val isNew: Boolean,
    val form: OneOffFilmForm = OneOffFilmForm(),
    val isLoading: Boolean = true,
    val isReadingVideo: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    val canSave: Boolean
        get() = !isLoading && !isSaving && !isReadingVideo &&
            form.title.isNotBlank() &&
            form.videoUrl.isNotBlank() &&
            form.thumbnailUrl.isNotBlank()
}

class FilmEditorViewModel(
    private val repository: ContentRepository,
    private val mediaInspector: MediaInspector,
    filmId: String?,
) : ViewModel() {
    private var original: Film? = null
    private val state = MutableStateFlow(FilmEditorUiState(isNew = filmId == null, isLoading = filmId != null))
    val uiState: StateFlow<FilmEditorUiState> = state

    init {
        if (filmId != null) {
            viewModelScope.launch {
                val existing = repository.observeFilm(filmId).first()
                original = existing
                state.update {
                    it.copy(form = existing?.toForm() ?: OneOffFilmForm(), isLoading = false)
                }
            }
        }
    }

    fun updateForm(transform: (OneOffFilmForm) -> OneOffFilmForm) {
        state.update { it.copy(form = transform(it.form)) }
    }

    fun onVideoPicked(url: String) {
        state.update { it.copy(form = it.form.copy(videoUrl = url), isReadingVideo = true) }
        viewModelScope.launch {
            val duration = mediaInspector.videoDurationSeconds(url)
            state.update {
                it.copy(
                    form = it.form.copy(durationSeconds = duration ?: 0),
                    isReadingVideo = false,
                )
            }
        }
    }

    fun save() {
        val current = state.value
        if (!current.canSave) return
        state.update { it.copy(isSaving = true) }
        val form = current.form
        viewModelScope.launch {
            val existing = original
            if (existing == null) {
                repository.addFilm(
                    NewFilm(
                        creatorId = PrototypeCreator.ID,
                        seriesId = null,
                        episodeNumber = null,
                        title = form.title.trim(),
                        description = form.description.trim(),
                        thumbnailUrl = form.thumbnailUrl,
                        videoUrl = form.videoUrl,
                        durationSeconds = form.durationSeconds,
                        publishedAt = Clock.System.now(),
                        tools = form.tools.trim(),
                        modelName = form.modelName.trim(),
                        origin = form.origin,
                    ),
                )
            } else {
                repository.updateFilm(
                    existing.copy(
                        title = form.title.trim(),
                        description = form.description.trim(),
                        thumbnailUrl = form.thumbnailUrl,
                        videoUrl = form.videoUrl,
                        durationSeconds = form.durationSeconds,
                        tools = form.tools.trim(),
                        modelName = form.modelName.trim(),
                        origin = form.origin,
                    ),
                )
            }
            state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun Film.toForm() = OneOffFilmForm(
        title = title,
        description = description,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        tools = tools,
        modelName = modelName,
        origin = origin,
    )
}
