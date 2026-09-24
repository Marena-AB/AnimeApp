package com.anime.app.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.AdminSession
import com.anime.app.data.ContentRepository
import com.anime.app.data.WatchProgressRepository
import com.anime.app.model.Episode
import com.anime.app.model.Series
import com.anime.app.model.WatchProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SeriesDetailUiState(
    val series: Series? = null,
    val episodes: List<Episode> = emptyList(),
    val progress: Map<String, WatchProgress> = emptyMap(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
)

class SeriesDetailViewModel(
    private val contentRepository: ContentRepository,
    progressRepository: WatchProgressRepository,
    adminSession: AdminSession,
    private val seriesId: String,
) : ViewModel() {
    val uiState: StateFlow<SeriesDetailUiState> = combine(
        contentRepository.observeSeries(seriesId),
        contentRepository.observeEpisodes(seriesId),
        progressRepository.observeAll(),
        adminSession.isAdmin,
    ) { series, episodes, progress, isAdmin ->
        SeriesDetailUiState(
            series = series,
            episodes = episodes,
            progress = progress,
            isAdmin = isAdmin,
            isLoading = series == null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SeriesDetailUiState(),
    )

    fun deleteSeries(onDeleted: () -> Unit) {
        viewModelScope.launch {
            contentRepository.deleteSeries(seriesId)
            onDeleted()
        }
    }

    fun deleteEpisode(episodeId: String) {
        viewModelScope.launch { contentRepository.deleteEpisode(episodeId) }
    }
}
