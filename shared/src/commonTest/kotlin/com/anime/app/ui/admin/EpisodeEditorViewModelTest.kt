package com.anime.app.ui.admin

import com.anime.app.data.FakeContentRepository
import com.anime.app.model.FilmOrigin
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
        viewModel.updateForm {
            it.copy(title = "New one", tools = "Kling", modelName = "1.6", origin = FilmOrigin.FULLY_GENERATED)
        }
        assertEquals(123, viewModel.uiState.value.form.durationSeconds)
        assertTrue(viewModel.uiState.value.canSave)

        viewModel.save()
        assertTrue(viewModel.uiState.value.isSaved)

        val saved = repository.observeFilms("caminandes").first().last()
        val cover = repository.observeSeries("caminandes").first()!!.coverUrl
        assertEquals(4, saved.episodeNumber)
        assertEquals("New one", saved.title)
        assertEquals(123, saved.durationSeconds)
        assertEquals(cover, saved.thumbnailUrl)
        assertEquals("Kling", saved.tools)
        assertEquals("1.6", saved.modelName)
        assertEquals(FilmOrigin.FULLY_GENERATED, saved.origin)
    }

    @Test
    fun editingKeepsIdAndUpdatesFields() = runTest {
        val viewModel = EpisodeEditorViewModel(repository, inspector, "caminandes", "caminandes-ep2")
        assertFalse(viewModel.uiState.value.isNew)
        viewModel.updateForm { it.copy(title = "Retitled") }
        viewModel.save()

        val film = repository.observeFilm("caminandes-ep2").first()!!
        assertEquals("Retitled", film.title)
        assertEquals(3, repository.observeFilms("caminandes").first().size)
    }
}
