package com.anime.app.ui.admin

import com.anime.app.data.FakeContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EpisodeEditorViewModelTest {
    private val repository = FakeContentRepository()
    private val inspector = object : MediaInspector {
        override suspend fun videoDurationSeconds(url: String): Int = 123
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun newEpisodeDefaultsToNextNumber() {
        val viewModel = EpisodeEditorViewModel(repository, inspector, "caminandes", episodeId = null)
        assertEquals("4", viewModel.uiState.value.form.number)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun pickingVideoReadsDurationAndSaveFallsBackToSeriesCover() = runTest {
        val viewModel = EpisodeEditorViewModel(repository, inspector, "caminandes", episodeId = null)
        viewModel.onVideoPicked("file:///new.mp4")
        viewModel.updateForm { it.copy(title = "New one") }
        assertEquals(123, viewModel.uiState.value.form.durationSeconds)
        assertTrue(viewModel.uiState.value.canSave)

        viewModel.save()
        assertTrue(viewModel.uiState.value.isSaved)

        val saved = repository.observeEpisodes("caminandes").first().last()
        val cover = repository.observeSeries("caminandes").first()!!.coverUrl
        assertEquals(4, saved.number)
        assertEquals("New one", saved.title)
        assertEquals(123, saved.durationSeconds)
        assertEquals(cover, saved.thumbnailUrl)
    }

    @Test
    fun editingKeepsIdAndUpdatesFields() = runTest {
        val viewModel = EpisodeEditorViewModel(repository, inspector, "caminandes", "caminandes-ep2")
        assertFalse(viewModel.uiState.value.isNew)
        viewModel.updateForm { it.copy(title = "Retitled") }
        viewModel.save()

        val episode = repository.observeEpisode("caminandes-ep2").first()!!
        assertEquals("Retitled", episode.title)
        assertEquals(3, repository.observeEpisodes("caminandes").first().size)
    }
}
