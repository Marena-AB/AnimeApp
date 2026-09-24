package com.anime.app.data

import com.anime.app.model.Episode
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
)

data class NewEpisode(
    val seriesId: String,
    val number: Int,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val publishedAt: Instant,
    val introStartSeconds: Int? = null,
    val introEndSeconds: Int? = null,
)

interface ContentRepository {
    fun observeSeries(): Flow<List<Series>>
    fun observeSeries(seriesId: String): Flow<Series?>
    fun observeEpisodes(seriesId: String): Flow<List<Episode>>
    fun observeAllEpisodes(): Flow<List<Episode>>
    fun observeEpisode(episodeId: String): Flow<Episode?>

    suspend fun createSeries(series: NewSeries): Series
    suspend fun updateSeries(series: Series)
    suspend fun deleteSeries(seriesId: String)

    suspend fun addEpisode(episode: NewEpisode): Episode
    suspend fun updateEpisode(episode: Episode)
    suspend fun deleteEpisode(episodeId: String)
}
