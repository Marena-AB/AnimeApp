package com.anime.app.ui.home

import com.anime.app.data.FakeContentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FollowingShelfTest {
    @Test
    fun shelfListsSeriesAndOneOffsForFollowedCreatorsNewestFirst() = runTest {
        val repository = FakeContentRepository()
        val series = repository.observeSeries().first()
        val films = repository.observeAllFilms().first()
        val creators = repository.observeCreators().first().associateBy { it.id }

        val shelf = followingShelf(
            followedCreatorIds = listOf("peach", "relay"),
            series = series,
            oneOffs = films,
            creatorsById = creators,
        )

        assertEquals(
            listOf("Big Buck Bunny", "The Fence", "Sintel", "Elephants Dream"),
            shelf.map { it.title },
        )
        assertTrue(shelf.none { it.title == "Caminandes" })
        assertTrue(shelf.none { it.title == "Llama Drama" })
    }

    @Test
    fun emptyFollowListYieldsAnEmptyShelf() = runTest {
        val repository = FakeContentRepository()
        val shelf = followingShelf(
            followedCreatorIds = emptyList(),
            series = repository.observeSeries().first(),
            oneOffs = repository.observeAllFilms().first(),
            creatorsById = emptyMap(),
        )
        assertTrue(shelf.isEmpty())
    }
}
