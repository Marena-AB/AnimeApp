package com.anime.app.ui.studio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.ContentRepository
import com.anime.app.model.Creator
import com.anime.app.model.Film
import com.anime.app.model.PrototypeCreator
import com.anime.app.model.Series
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StudioSeries(
    val series: Series,
    val episodeCount: Int,
)

data class CreatorStudioUiState(
    val creator: Creator? = null,
    val series: List<StudioSeries> = emptyList(),
    val films: List<Film> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isEmpty: Boolean
        get() = series.isEmpty() && films.isEmpty()
}

class CreatorStudioViewModel(
    private val repository: ContentRepository,
    private val creatorId: String = PrototypeCreator.ID,
) : ViewModel() {
    val uiState: StateFlow<CreatorStudioUiState> = combine(
        repository.observeCreator(creatorId),
        repository.observeSeries(),
        repository.observeAllFilms(),
    ) { creator, series, films ->
        val episodeCounts = films.groupingBy { it.seriesId }.eachCount()
        CreatorStudioUiState(
            creator = creator,
            series = series
                .filter { it.creatorId == creatorId }
                .map { StudioSeries(it, episodeCounts[it.id] ?: 0) },
            films = films.filter { it.isOneOff && it.creatorId == creatorId },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreatorStudioUiState(),
    )

    fun deleteSeries(seriesId: String) {
        viewModelScope.launch { repository.deleteSeries(seriesId) }
    }

    fun deleteFilm(filmId: String) {
        viewModelScope.launch { repository.deleteFilm(filmId) }
    }
}
