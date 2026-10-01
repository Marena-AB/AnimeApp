package com.anime.app.ui.admin

import com.anime.app.data.FakeContentRepository
import com.anime.app.model.FilmOrigin
import com.anime.app.model.PrototypeCreator
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FilmEditorViewModelTest {
    private val repository = FakeContentRepository()
    private val inspector = object : MediaInspector {
        override suspend fun videoDurationSeconds(url: String): Int = 90
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun newFilmRequiresCoverAndDoesNotAskForASeries() {
        val viewModel = FilmEditorViewModel(repository, inspector, filmId = null)
        assertTrue(viewModel.uiState.value.isNew)
        assertFalse(viewModel.uiState.value.canSave)
        viewModel.onVideoPicked("file:///short.mp4")
        viewModel.updateForm { it.copy(title = "Night Drive") }
        assertFalse(viewModel.uiState.value.canSave)
        viewModel.updateForm { it.copy(thumbnailUrl = "file:///cover.jpg") }
        assertTrue(viewModel.uiState.value.canSave)
    }

    @Test
    fun savePublishesAOneOffWithCredits() = runTest {
        val viewModel = FilmEditorViewModel(repository, inspector, filmId = null)
        viewModel.onVideoPicked("file:///short.mp4")
        viewModel.updateForm {
            it.copy(
                title = "Night Drive",
                description = "A single scene.",
                thumbnailUrl = "file:///cover.jpg",
                tools = "Runway",
                modelName = "Gen-3",
                origin = FilmOrigin.AI_ASSISTED,
            )
        }
        viewModel.save()

        val saved = repository.observeOneOffs().first().last()
        assertEquals("Night Drive", saved.title)
        assertNull(saved.seriesId)
        assertNull(saved.episodeNumber)
        assertEquals(PrototypeCreator.ID, saved.creatorId)
        assertEquals("Runway", saved.tools)
        assertEquals("Gen-3", saved.modelName)
        assertEquals(FilmOrigin.AI_ASSISTED, saved.origin)
        assertEquals(90, saved.durationSeconds)
        assertTrue(viewModel.uiState.value.isSaved)
    }
}
