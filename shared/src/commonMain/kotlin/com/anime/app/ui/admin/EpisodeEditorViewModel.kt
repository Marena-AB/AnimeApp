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

data class EpisodeForm(
    val number: String = "",
    val title: String = "",
    val description: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val durationSeconds: Int = 0,
    val introStartSeconds: String = "",
    val introEndSeconds: String = "",
    val tools: String = "",
    val modelName: String = "",
    val origin: FilmOrigin = FilmOrigin.UNCLEAR,
)

data class EpisodeEditorUiState(
    val isNew: Boolean,
    val seriesTitle: String = "",
    val form: EpisodeForm = EpisodeForm(),
    val isLoading: Boolean = true,
    val isReadingVideo: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    val introRangeValid: Boolean
        get() {
            if (form.introStartSeconds.isBlank() && form.introEndSeconds.isBlank()) return true
            val start = form.introStartSeconds.toIntOrNull() ?: return false
            val end = form.introEndSeconds.toIntOrNull() ?: return false
            return start >= 0 && end > start
        }

    val canSave: Boolean
        get() = !isLoading && !isSaving && !isReadingVideo &&
            form.title.isNotBlank() &&
            form.videoUrl.isNotBlank() &&
            (form.number.toIntOrNull() ?: 0) > 0 &&
            introRangeValid
}

class EpisodeEditorViewModel(
    private val repository: ContentRepository,
    private val mediaInspector: MediaInspector,
    private val seriesId: String,
    episodeId: String?,
) : ViewModel() {
    private var original: Film? = null
    private var seriesCoverUrl = ""
    private var seriesCreatorId = PrototypeCreator.ID
    private val state = MutableStateFlow(EpisodeEditorUiState(isNew = episodeId == null))
    val uiState: StateFlow<EpisodeEditorUiState> = state

    init {
        viewModelScope.launch {
            val series = repository.observeSeries(seriesId).first()
            seriesCoverUrl = series?.coverUrl.orEmpty()
            seriesCreatorId = series?.creatorId ?: PrototypeCreator.ID
            val films = repository.observeFilms(seriesId).first()
            val existing = episodeId?.let { id -> films.find { it.id == id } }
            original = existing
            val form = existing?.toForm()
                ?: EpisodeForm(number = ((films.maxOfOrNull { it.episodeNumber ?: 0 } ?: 0) + 1).toString())
            state.update {
                it.copy(seriesTitle = series?.title.orEmpty(), form = form, isLoading = false)
            }
        }
    }

    fun updateForm(transform: (EpisodeForm) -> EpisodeForm) {
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
        val thumbnail = form.thumbnailUrl.ifBlank { seriesCoverUrl }
        viewModelScope.launch {
            val existing = original
            if (existing == null) {
                repository.addFilm(
                    NewFilm(
                        creatorId = seriesCreatorId,
                        seriesId = seriesId,
                        episodeNumber = form.number.toInt(),
                        title = form.title.trim(),
                        description = form.description.trim(),
                        thumbnailUrl = thumbnail,
                        videoUrl = form.videoUrl,
                        durationSeconds = form.durationSeconds,
                        publishedAt = Clock.System.now(),
                        introStartSeconds = form.introStartSeconds.toIntOrNull(),
                        introEndSeconds = form.introEndSeconds.toIntOrNull(),
                        tools = form.tools.trim(),
                        modelName = form.modelName.trim(),
                        origin = form.origin,
                    ),
                )
            } else {
                repository.updateFilm(
                    existing.copy(
                        episodeNumber = form.number.toInt(),
                        title = form.title.trim(),
                        description = form.description.trim(),
                        thumbnailUrl = thumbnail,
                        videoUrl = form.videoUrl,
                        durationSeconds = form.durationSeconds,
                        introStartSeconds = form.introStartSeconds.toIntOrNull(),
                        introEndSeconds = form.introEndSeconds.toIntOrNull(),
                        tools = form.tools.trim(),
                        modelName = form.modelName.trim(),
                        origin = form.origin,
                    ),
                )
            }
            state.update { it.copy(isSaving = false, isSaved = true) }
        }
    }

    private fun Film.toForm() = EpisodeForm(
        number = episodeNumber?.toString().orEmpty(),
        title = title,
        description = description,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        introStartSeconds = introStartSeconds?.toString().orEmpty(),
        introEndSeconds = introEndSeconds?.toString().orEmpty(),
        tools = tools,
        modelName = modelName,
        origin = origin,
    )
}
