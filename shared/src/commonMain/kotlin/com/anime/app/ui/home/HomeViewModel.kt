package com.anime.app.ui.home

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

data class ContinueWatchingItem(
    val episode: Episode,
    val series: Series?,
    val progress: WatchProgress,
)

data class HomeUiState(
    val featured: Series? = null,
    /** The episode the featured banner's Play button starts: the first one not yet finished. */
    val featuredEpisode: Episode? = null,
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val series: List<Series> = emptyList(),
    val isAdmin: Boolean = false,
    val isLoading: Boolean = true,
)

class HomeViewModel(
    contentRepository: ContentRepository,
    progressRepository: WatchProgressRepository,
    private val adminSession: AdminSession,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        contentRepository.observeSeries(),
        contentRepository.observeAllEpisodes(),
        progressRepository.observeAll(),
        adminSession.isAdmin,
    ) { series, episodes, progress, isAdmin ->
        val seriesById = series.associateBy { it.id }
        val episodesById = episodes.associateBy { it.id }
        val featured = series.firstOrNull()
        val featuredEpisodes = episodes.filter { it.seriesId == featured?.id }.sortedBy { it.number }

        HomeUiState(
            featured = featured,
            featuredEpisode = featuredEpisodes.firstOrNull { progress[it.id]?.completed != true }
                ?: featuredEpisodes.firstOrNull(),
            continueWatching = progress.values
                .filter { !it.completed }
                .sortedByDescending { it.updatedAtEpochMs }
                .mapNotNull { entry ->
                    val episode = episodesById[entry.episodeId] ?: return@mapNotNull null
                    ContinueWatchingItem(episode, seriesById[episode.seriesId], entry)
                },
            series = series,
            isAdmin = isAdmin,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun toggleAdmin() {
        adminSession.setAdmin(!adminSession.isAdmin.value)
    }

    fun exitAdmin() {
        adminSession.setAdmin(false)
    }
}
