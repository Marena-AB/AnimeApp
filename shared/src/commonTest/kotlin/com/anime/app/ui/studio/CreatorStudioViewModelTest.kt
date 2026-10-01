package com.anime.app.ui.studio

import com.anime.app.data.FakeContentRepository
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CreatorStudioViewModelTest {
    private val repository = FakeContentRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun studioListsOnlyTheSignedInCreatorsWork() = runTest {
        val viewModel = CreatorStudioViewModel(repository, PrototypeCreator.ID)
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals("Pampas Pictures", state.creator?.displayName)
        assertEquals(listOf("Caminandes"), state.series.map { it.series.title })
        assertEquals(3, state.series.single().episodeCount)
        assertEquals(listOf("The Search"), state.films.map { it.title })
    }

    @Test
    fun deletingASeriesKeepsOneOffs() = runTest {
        val viewModel = CreatorStudioViewModel(repository, PrototypeCreator.ID)
        viewModel.deleteSeries("caminandes")
        val state = viewModel.uiState.first {
            !it.isLoading && it.series.none { series -> series.series.id == "caminandes" }
        }
        assertTrue(state.films.any { it.id == "oneoff-search" })
        assertTrue(repository.observeFilms("caminandes").first().isEmpty())
    }

    @Test
    fun deletingAOneOffLeavesTheSeries() = runTest {
        val viewModel = CreatorStudioViewModel(repository, PrototypeCreator.ID)
        viewModel.deleteFilm("oneoff-search")
        val state = viewModel.uiState.first {
            !it.isLoading && it.films.none { film -> film.id == "oneoff-search" }
        }
        assertEquals(listOf("Caminandes"), state.series.map { it.series.title })
    }
}
