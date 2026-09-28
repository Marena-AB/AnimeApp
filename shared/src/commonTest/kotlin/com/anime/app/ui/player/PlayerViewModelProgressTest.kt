package com.anime.app.ui.player

import com.anime.app.data.FakeContentRepository
import com.anime.app.data.WatchProgressRepository
import com.anime.app.model.WatchProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class InMemoryProgressRepository : WatchProgressRepository {
    val state = MutableStateFlow<Map<String, WatchProgress>>(emptyMap())
    override fun observeAll(): Flow<Map<String, WatchProgress>> = state
    override fun get(filmId: String): WatchProgress? = state.value[filmId]
    override fun save(progress: WatchProgress) {
        state.value = state.value + (progress.filmId to progress)
    }
}

class PlayerViewModelProgressTest {
    private val progress = InMemoryProgressRepository()
    private val viewModel = PlayerViewModel(FakeContentRepository(), progress, "caminandes-ep1")

    private fun update(positionMs: Long, isPlaying: Boolean = true, isEnded: Boolean = false, filmId: String = "caminandes-ep1") =
        viewModel.onPlaybackUpdate(filmId, positionMs, durationMs = 90_000, isPlaying = isPlaying, isEnded = isEnded)

    @Test
    fun doesNotSaveInTheFirstFewSeconds() {
        update(0)
        update(3_000, isPlaying = false)
        assertNull(progress.get("caminandes-ep1"))
    }

    @Test
    fun savesPeriodicallyAndResumesFromSavedPosition() {
        update(0)
        update(6_000)
        assertEquals(6, progress.get("caminandes-ep1")?.positionSeconds)
        assertEquals(6_000, PlayerViewModel(FakeContentRepository(), progress, "caminandes-ep1").startPositionMs("caminandes-ep1"))
    }

    @Test
    fun savesOnPause() {
        update(0)
        update(7_000)
        update(8_500, isPlaying = false)
        assertEquals(8, progress.get("caminandes-ep1")?.positionSeconds)
    }

    @Test
    fun endedEpisodeIsCompletedAndRestartsFromBeginning() {
        update(0)
        update(90_000, isPlaying = false, isEnded = true)
        assertTrue(progress.get("caminandes-ep1")!!.completed)
        assertEquals(0, PlayerViewModel(FakeContentRepository(), progress, "caminandes-ep1").startPositionMs("caminandes-ep1"))
    }

    @Test
    fun switchingEpisodesSavesThePreviousOne() {
        update(0)
        update(20_000)
        update(23_000)
        update(0, filmId = "caminandes-ep2")
        assertEquals(23, progress.get("caminandes-ep1")?.positionSeconds)
    }
}
