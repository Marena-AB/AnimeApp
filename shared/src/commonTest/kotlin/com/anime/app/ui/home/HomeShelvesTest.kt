package com.anime.app.ui.home

import com.anime.app.data.FakeContentRepository
import com.anime.app.model.WatchProgress
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeShelvesTest {
    @Test
    fun noHistoryRecommendsEverySeriesExceptTheFeaturedOne() = runTest {
        val repository = FakeContentRepository()
        val series = repository.observeSeries().first()
        val films = repository.observeAllFilms().first()
        val creators = repository.observeCreators().first().associateBy { it.id }

        assertNull(
            becauseYouWatched(
                films = films,
                series = series,
                progress = emptyList(),
                creatorsById = creators,
            ),
        )
        assertEquals(
            listOf("Caminandes", "Big Buck Bunny", "Sintel", "Elephants Dream"),
            recommendedShelf(
                films = films,
                series = series,
                featuredSeriesId = series.first().id,
                progress = emptyList(),
                excludeKeys = emptySet(),
                creatorsById = creators,
            ).map { it.title },
        )
    }

    @Test
    fun watchingYutaSuggestsTheNextEpisodeAndThatCreatorsShorts() = runTest {
        val repository = FakeContentRepository()
        val series = repository.observeSeries().first()
        val films = repository.observeAllFilms().first()
        val creators = repository.observeCreators().first().associateBy { it.id }
        val progress = listOf(
            WatchProgress(
                filmId = "jjk-ep1",
                positionSeconds = 241,
                durationSeconds = 241,
                completed = true,
                updatedAtEpochMs = 10L,
            ),
        )

        val shelf = becauseYouWatched(
            films = films,
            series = series,
            progress = progress,
            creatorsById = creators,
        )

        assertEquals("Yuta", shelf?.watchedTitle)
        assertEquals(listOf("Suki", "MusicTest", "AI video"), shelf?.entries?.map { it.title })
        assertEquals(
            listOf("Caminandes", "Big Buck Bunny", "Sintel", "Elephants Dream"),
            recommendedShelf(
                films = films,
                series = series,
                featuredSeriesId = "jjk",
                progress = progress,
                excludeKeys = shelf?.entries?.map { it.key }?.toSet().orEmpty(),
                creatorsById = creators,
            ).map { it.title },
        )
    }
}
