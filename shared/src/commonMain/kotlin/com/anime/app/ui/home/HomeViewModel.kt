package com.anime.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.ContentRepository
import com.anime.app.data.SocialRepository
import com.anime.app.data.WatchProgressRepository
import com.anime.app.model.Creator
import com.anime.app.model.Film
import com.anime.app.model.Series
import com.anime.app.model.WatchProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ContinueWatchingItem(
    val film: Film,
    val series: Series?,
    val creatorName: String,
    val progress: WatchProgress,
)

data class HomeUiState(
    val featured: Series? = null,
    val featuredCreatorName: String = "",
    val featuredCreatorId: String = "",
    val isFeaturedCreatorFollowed: Boolean = false,
    val following: List<FollowingEntry> = emptyList(),
    val followedCreatorIds: List<String> = emptyList(),
    /** The film the featured banner's Play button starts: the first one not yet finished. */
    val featuredFilm: Film? = null,
    val continueWatching: List<ContinueWatchingItem> = emptyList(),
    val becauseYouWatched: BecauseYouWatched? = null,
    val recommended: List<FollowingEntry> = emptyList(),
    val series: List<Series> = emptyList(),
    val creatorsById: Map<String, Creator> = emptyMap(),
    val oneOffs: List<Film> = emptyList(),
    val isLoading: Boolean = true,
)

class HomeViewModel(
    contentRepository: ContentRepository,
    progressRepository: WatchProgressRepository,
    private val socialRepository: SocialRepository,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        contentRepository.observeSeries(),
        contentRepository.observeAllFilms(),
        contentRepository.observeCreators(),
        progressRepository.observeAll(),
        socialRepository.observe(),
    ) { series, films, creators, progress, social ->
        val seriesById = series.associateBy { it.id }
        val filmsById = films.associateBy { it.id }
        val creatorsById = creators.associateBy { it.id }
        val featured = series.firstOrNull()
        val featuredFilms = films
            .filter { it.seriesId == featured?.id }
            .sortedBy { it.episodeNumber ?: Int.MAX_VALUE }
        val because = becauseYouWatched(
            films = films,
            series = series,
            progress = progress.values,
            creatorsById = creatorsById,
        )
        val recommended = recommendedShelf(
            films = films,
            series = series,
            featuredSeriesId = featured?.id,
            progress = progress.values,
            excludeKeys = because?.entries?.map { it.key }?.toSet().orEmpty(),
            creatorsById = creatorsById,
        )

        HomeUiState(
            featured = featured,
            featuredCreatorName = creatorsById[featured?.creatorId]?.displayName.orEmpty(),
            featuredCreatorId = featured?.creatorId.orEmpty(),
            isFeaturedCreatorFollowed = featured?.creatorId?.let(social::isFollowing) == true,
            following = followingShelf(
                followedCreatorIds = social.followedCreatorIds,
                series = series,
                oneOffs = films,
                creatorsById = creatorsById,
            ),
            followedCreatorIds = social.followedCreatorIds,
            featuredFilm = featuredFilms.firstOrNull { progress[it.id]?.completed != true }
                ?: featuredFilms.firstOrNull(),
            continueWatching = progress.values
                .filter { !it.completed }
                .sortedByDescending { it.updatedAtEpochMs }
                .mapNotNull { entry ->
                    val film = filmsById[entry.filmId] ?: return@mapNotNull null
                    val filmSeries = film.seriesId?.let { seriesById[it] }
                    ContinueWatchingItem(
                        film = film,
                        series = filmSeries,
                        creatorName = creatorsById[film.creatorId]?.displayName.orEmpty(),
                        progress = entry,
                    )
                },
            becauseYouWatched = because,
            recommended = recommended,
            series = series,
            creatorsById = creatorsById,
            oneOffs = films.filter { it.isOneOff },
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun toggleFollow(creatorId: String) {
        if (creatorId.isBlank()) return
        socialRepository.setFollowing(creatorId, creatorId !in uiState.value.followedCreatorIds)
    }
}
