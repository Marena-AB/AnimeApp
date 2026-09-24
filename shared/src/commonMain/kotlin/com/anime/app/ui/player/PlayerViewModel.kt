package com.anime.app.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anime.app.data.ContentRepository
import com.anime.app.data.WatchProgressRepository
import com.anime.app.model.Episode
import com.anime.app.model.Series
import com.anime.app.model.WatchProgress
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import kotlin.time.Clock

data class PlayerUiState(
    val episode: Episode? = null,
    val series: Series? = null,
    val nextEpisode: Episode? = null,
    val isFullscreen: Boolean = false,
)

private const val SAVE_INTERVAL_MS = 5_000L
private const val MIN_SAVED_POSITION_MS = 5_000L
private const val COMPLETED_FRACTION = 0.95

class PlayerViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: WatchProgressRepository,
    episodeId: String,
) : ViewModel() {
    private val currentEpisodeId = MutableStateFlow(episodeId)
    private val isFullscreen = MutableStateFlow(false)

    private var trackedEpisodeId: String? = null
    private var lastPositionMs = 0L
    private var lastDurationMs = 0L
    private var lastSavedPositionMs = 0L
    private var wasPlaying = false

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PlayerUiState> = currentEpisodeId
        .flatMapLatest { id -> contentRepository.observeEpisode(id) }
        .flatMapLatest { episode ->
            if (episode == null) {
                flowOf(PlayerUiState())
            } else {
                combine(
                    contentRepository.observeSeries(episode.seriesId),
                    contentRepository.observeEpisodes(episode.seriesId),
                ) { series, episodes ->
                    PlayerUiState(
                        episode = episode,
                        series = series,
                        nextEpisode = episodes.firstOrNull { it.number > episode.number },
                    )
                }
            }
        }
        .combine(isFullscreen) { state, fullscreen -> state.copy(isFullscreen = fullscreen) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlayerUiState(),
        )

    /**
     * Where a newly created player should start: the live position if this episode was already playing
     * (the platform player was recreated), otherwise the saved resume point unless the episode was finished.
     */
    fun startPositionMs(episodeId: String): Long {
        if (episodeId == trackedEpisodeId) return lastPositionMs
        val saved = progressRepository.get(episodeId) ?: return 0L
        return if (saved.completed) 0L else saved.positionSeconds * 1000L
    }

    fun onPlaybackUpdate(
        episodeId: String,
        positionMs: Long,
        durationMs: Long,
        isPlaying: Boolean,
        isEnded: Boolean,
    ) {
        if (episodeId != trackedEpisodeId) {
            saveProgress()
            trackedEpisodeId = episodeId
            lastDurationMs = 0L
            lastSavedPositionMs = positionMs
            wasPlaying = false
        }
        lastPositionMs = positionMs
        if (durationMs > 0) lastDurationMs = durationMs

        val paused = wasPlaying && !isPlaying
        wasPlaying = isPlaying
        when {
            isEnded -> saveProgress(ended = true)
            paused || abs(positionMs - lastSavedPositionMs) >= SAVE_INTERVAL_MS -> saveProgress()
        }
    }

    fun playEpisode(episodeId: String) {
        saveProgress()
        currentEpisodeId.value = episodeId
    }

    fun toggleFullscreen() {
        isFullscreen.update { !it }
    }

    fun exitFullscreen() {
        isFullscreen.value = false
    }

    override fun onCleared() {
        saveProgress()
    }

    private fun saveProgress(ended: Boolean = false) {
        val episodeId = trackedEpisodeId ?: return
        val durationMs = lastDurationMs
        if (durationMs <= 0L) return
        val completed = ended || lastPositionMs >= durationMs * COMPLETED_FRACTION
        if (!completed && lastPositionMs < MIN_SAVED_POSITION_MS) return

        progressRepository.save(
            WatchProgress(
                episodeId = episodeId,
                positionSeconds = if (completed) (durationMs / 1000).toInt() else (lastPositionMs / 1000).toInt(),
                durationSeconds = (durationMs / 1000).toInt(),
                completed = completed,
                updatedAtEpochMs = Clock.System.now().toEpochMilliseconds(),
            ),
        )
        lastSavedPositionMs = lastPositionMs
    }
}
