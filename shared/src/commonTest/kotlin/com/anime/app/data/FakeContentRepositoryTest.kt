package com.anime.app.data

import com.anime.app.model.Film
import com.anime.app.model.FilmOrigin
import com.anime.app.model.SeriesStatus
import com.anime.app.model.nextIn
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

    private fun newFilm(seriesId: String?, number: Int?, creatorId: String = "pampas") = NewFilm(
        creatorId = creatorId,
        seriesId = seriesId,
        episodeNumber = number,
        title = "Film $number",
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
        assertEquals("pampas", created.creatorId)
        assertTrue(repository.observeSeries().first().any { it.id == created.id })
    }

    @Test
    fun updateSeriesReplacesById() = runTest {
        val original = repository.observeSeries("bbb").first()!!
        repository.updateSeries(original.copy(title = "Renamed"))
        assertEquals("Renamed", repository.observeSeries("bbb").first()?.title)
    }

    @Test
    fun deleteSeriesCascadesToItsFilmsAndKeepsOneOffs() = runTest {
        assertTrue(repository.observeFilms("caminandes").first().isNotEmpty())
        val oneOffIds = repository.observeOneOffs().first().map { it.id }
        repository.deleteSeries("caminandes")
        assertNull(repository.observeSeries("caminandes").first())
        assertTrue(repository.observeFilms("caminandes").first().isEmpty())
        assertTrue(repository.observeAllFilms().first().none { it.seriesId == "caminandes" })
        assertEquals(oneOffIds, repository.observeOneOffs().first().map { it.id })
    }

    @Test
    fun filmsInASeriesAreSortedByEpisodeNumber() = runTest {
        val series = repository.createSeries(NewSeries("S", "", "", emptyList(), SeriesStatus.ONGOING))
        repository.addFilm(newFilm(series.id, 2))
        repository.addFilm(newFilm(series.id, 1))
        assertEquals(listOf(1, 2), repository.observeFilms(series.id).first().map { it.episodeNumber })
    }

    @Test
    fun updateAndDeleteFilm() = runTest {
        val series = repository.createSeries(NewSeries("S", "", "", emptyList(), SeriesStatus.ONGOING))
        val film = repository.addFilm(newFilm(series.id, 1))
        repository.updateFilm(film.copy(title = "Pilot"))
        assertEquals("Pilot", repository.observeFilm(film.id).first()?.title)

        repository.deleteFilm(film.id)
        assertNull(repository.observeFilm(film.id).first())
        assertNotNull(repository.observeSeries(series.id).first())
    }

    @Test
    fun seedHasSeveralCreatorsAndStandaloneFilms() = runTest {
        val creators = repository.observeCreators().first()
        assertTrue(creators.size >= 3)
        val oneOffs = repository.observeOneOffs().first()
        assertTrue(oneOffs.size >= 2)
        assertTrue(oneOffs.all { it.seriesId == null && it.episodeNumber == null })
        val creatorIds = creators.map { it.id }.toSet()
        assertTrue(repository.observeSeries().first().all { it.creatorId in creatorIds })
        assertTrue(oneOffs.all { it.creatorId in creatorIds })
    }

    @Test
    fun oneOffCreateAndDeleteDoesNotTouchSeries() = runTest {
        val created = repository.addFilm(newFilm(seriesId = null, number = null, creatorId = "peach"))
        assertTrue(created.isOneOff)
        assertTrue(repository.observeOneOffs().first().any { it.id == created.id })
        assertTrue(repository.observeFilms("caminandes").first().none { it.id == created.id })

        repository.deleteFilm(created.id)
        assertNull(repository.observeFilm(created.id).first())
        assertNotNull(repository.observeSeries("caminandes").first())
    }

    @Test
    fun nextFilmStaysInsideTheSeries() {
        val first = film(id = "a", seriesId = "show", episodeNumber = 1)
        val second = film(id = "b", seriesId = "show", episodeNumber = 2)
        val oneOff = film(id = "c", seriesId = null, episodeNumber = null)
        val catalog = listOf(second, oneOff, first)

        assertEquals("b", first.nextIn(catalog)?.id)
        assertNull(second.nextIn(catalog))
        assertNull(oneOff.nextIn(catalog))
    }

    private fun film(id: String, seriesId: String?, episodeNumber: Int?) = Film(
        id = id,
        creatorId = "pampas",
        seriesId = seriesId,
        episodeNumber = episodeNumber,
        title = id,
        description = "",
        thumbnailUrl = "",
        videoUrl = "",
        durationSeconds = 1,
        publishedAt = Instant.parse("2026-01-01T00:00:00Z"),
        origin = FilmOrigin.UNCLEAR,
    )
}
