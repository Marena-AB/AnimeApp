package com.anime.app.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.ContentRepository
import com.anime.app.data.SocialRepository
import com.anime.app.data.WatchProgressRepository
import com.anime.app.model.Film
import com.anime.app.model.Series
import com.anime.app.model.WatchProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SeriesDetailUiState(
    val series: Series? = null,
    val creatorName: String = "",
    val creatorId: String = "",
    val isFollowing: Boolean = false,
    val isLiked: Boolean = false,
    val films: List<Film> = emptyList(),
    val progress: Map<String, WatchProgress> = emptyMap(),
    val isLoading: Boolean = true,
)

class SeriesDetailViewModel(
    private val contentRepository: ContentRepository,
    progressRepository: WatchProgressRepository,
    private val socialRepository: SocialRepository,
    private val seriesId: String,
) : ViewModel() {
    val uiState: StateFlow<SeriesDetailUiState> = combine(
        contentRepository.observeSeries(seriesId),
        contentRepository.observeFilms(seriesId),
        contentRepository.observeCreators(),
        progressRepository.observeAll(),
        socialRepository.observe(),
    ) { series, films, creators, progress, social ->
        SeriesDetailUiState(
            series = series,
            creatorName = creators.find { it.id == series?.creatorId }?.displayName.orEmpty(),
            creatorId = series?.creatorId.orEmpty(),
            isFollowing = series?.creatorId?.let(social::isFollowing) == true,
            isLiked = social.isSeriesLiked(seriesId),
            films = films,
            progress = progress,
            isLoading = series == null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SeriesDetailUiState(),
    )

    fun toggleFollow() {
        val creatorId = uiState.value.creatorId
        if (creatorId.isBlank()) return
        socialRepository.setFollowing(creatorId, !uiState.value.isFollowing)
    }

    fun toggleLike() {
        socialRepository.setSeriesLiked(seriesId, !uiState.value.isLiked)
    }
}
