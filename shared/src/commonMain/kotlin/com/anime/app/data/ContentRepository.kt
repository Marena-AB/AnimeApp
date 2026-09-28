package com.anime.app.data

import com.anime.app.model.Creator
import com.anime.app.model.Film
import com.anime.app.model.FilmOrigin
import com.anime.app.model.PrototypeCreator
import com.anime.app.model.Series
import com.anime.app.model.SeriesStatus
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

data class NewSeries(
    val title: String,
    val description: String,
    val coverUrl: String,
    val genres: List<String>,
    val status: SeriesStatus,
    val attribution: String = "",
    val creatorId: String = PrototypeCreator.ID,
)

data class NewFilm(
    val creatorId: String,
    val seriesId: String?,
    val episodeNumber: Int?,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val publishedAt: Instant,
    val tools: String = "",
    val modelName: String = "",
    val origin: FilmOrigin = FilmOrigin.UNCLEAR,
    val introStartSeconds: Int? = null,
    val introEndSeconds: Int? = null,
)

interface ContentRepository {
    fun observeCreators(): Flow<List<Creator>>
    fun observeCreator(creatorId: String): Flow<Creator?>

    fun observeSeries(): Flow<List<Series>>
    fun observeSeries(seriesId: String): Flow<Series?>
    fun observeFilms(seriesId: String): Flow<List<Film>>
    fun observeOneOffs(): Flow<List<Film>>
    fun observeAllFilms(): Flow<List<Film>>
    fun observeFilm(filmId: String): Flow<Film?>

    suspend fun createSeries(series: NewSeries): Series
    suspend fun updateSeries(series: Series)
    suspend fun deleteSeries(seriesId: String)

    suspend fun addFilm(film: NewFilm): Film
    suspend fun updateFilm(film: Film)
    suspend fun deleteFilm(filmId: String)
}
