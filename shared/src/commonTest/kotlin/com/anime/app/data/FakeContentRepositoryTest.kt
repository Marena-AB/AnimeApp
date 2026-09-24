package com.anime.app.data

import com.anime.app.model.SeriesStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class FakeContentRepositoryTest {
    private val repository = FakeContentRepository()

    private fun newEpisode(seriesId: String, number: Int) = NewEpisode(
        seriesId = seriesId,
        number = number,
        title = "Episode $number",
        description = "",
        thumbnailUrl = "",
        videoUrl = "file:///video$number.mp4",
        durationSeconds = 60,
        publishedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    @Test
    fun createdSeriesIsObservable() = runTest {
        val created = repository.createSeries(
            NewSeries("My Show", "desc", "", listOf("Action"), SeriesStatus.ONGOING),
        )
        assertEquals(created, repository.observeSeries(created.id).first())
        assertTrue(repository.observeSeries().first().any { it.id == created.id })
    }

    @Test
    fun updateSeriesReplacesById() = runTest {
        val original = repository.observeSeries("bbb").first()!!
        repository.updateSeries(original.copy(title = "Renamed"))
        assertEquals("Renamed", repository.observeSeries("bbb").first()?.title)
    }

    @Test
    fun deleteSeriesCascadesToEpisodes() = runTest {
        assertTrue(repository.observeEpisodes("caminandes").first().isNotEmpty())
        repository.deleteSeries("caminandes")
        assertNull(repository.observeSeries("caminandes").first())
        assertTrue(repository.observeEpisodes("caminandes").first().isEmpty())
        assertTrue(repository.observeAllEpisodes().first().none { it.seriesId == "caminandes" })
    }

    @Test
    fun episodesAreSortedByNumber() = runTest {
        val series = repository.createSeries(NewSeries("S", "", "", emptyList(), SeriesStatus.ONGOING))
        repository.addEpisode(newEpisode(series.id, 2))
        repository.addEpisode(newEpisode(series.id, 1))
        assertEquals(listOf(1, 2), repository.observeEpisodes(series.id).first().map { it.number })
    }

    @Test
    fun updateAndDeleteEpisode() = runTest {
        val series = repository.createSeries(NewSeries("S", "", "", emptyList(), SeriesStatus.ONGOING))
        val episode = repository.addEpisode(newEpisode(series.id, 1))
        repository.updateEpisode(episode.copy(title = "Pilot"))
        assertEquals("Pilot", repository.observeEpisode(episode.id).first()?.title)

        repository.deleteEpisode(episode.id)
        assertNull(repository.observeEpisode(episode.id).first())
        assertNotNull(repository.observeSeries(series.id).first())
    }
}
