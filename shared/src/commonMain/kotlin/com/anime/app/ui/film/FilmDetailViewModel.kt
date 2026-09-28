package com.anime.app.ui.film

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.ContentRepository
import com.anime.app.data.SocialRepository
import com.anime.app.model.Creator
import com.anime.app.model.Film
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FilmDetailUiState(
    val film: Film? = null,
    val creator: Creator? = null,
    val isFollowing: Boolean = false,
    val isLiked: Boolean = false,
    val isLoading: Boolean = true,
)

class FilmDetailViewModel(
    contentRepository: ContentRepository,
    private val socialRepository: SocialRepository,
    private val filmId: String,
) : ViewModel() {
    val uiState: StateFlow<FilmDetailUiState> = combine(
        contentRepository.observeFilm(filmId),
        contentRepository.observeCreators(),
        socialRepository.observe(),
    ) { film, creators, social ->
        FilmDetailUiState(
            film = film,
            creator = creators.find { it.id == film?.creatorId },
            isFollowing = film?.creatorId?.let(social::isFollowing) == true,
            isLiked = social.isFilmLiked(filmId),
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FilmDetailUiState(),
    )

    fun toggleFollow() {
        val creatorId = uiState.value.creator?.id ?: return
        socialRepository.setFollowing(creatorId, !uiState.value.isFollowing)
    }

    fun toggleLike() {
        socialRepository.setFilmLiked(filmId, !uiState.value.isLiked)
    }
}
