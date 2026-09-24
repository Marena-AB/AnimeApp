package com.anime.app.data

import com.anime.app.model.Episode
import com.anime.app.model.Series
import com.anime.app.model.SeriesStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.random.Random

class FakeContentRepository : ContentRepository {
    private val seriesState = MutableStateFlow(seedSeries())
    private val episodesState = MutableStateFlow(seedEpisodes())

    override fun observeSeries(): Flow<List<Series>> = seriesState

    override fun observeSeries(seriesId: String): Flow<Series?> =
        seriesState.map { series -> series.find { it.id == seriesId } }

    override fun observeEpisodes(seriesId: String): Flow<List<Episode>> =
        episodesState.map { episodes ->
            episodes.filter { it.seriesId == seriesId }.sortedBy { it.number }
        }

    override fun observeAllEpisodes(): Flow<List<Episode>> = episodesState

    override fun observeEpisode(episodeId: String): Flow<Episode?> =
        episodesState.map { episodes -> episodes.find { it.id == episodeId } }

    override suspend fun createSeries(series: NewSeries): Series {
        val created = Series(
            id = newId(),
            title = series.title,
            description = series.description,
            coverUrl = series.coverUrl,
            genres = series.genres,
            status = series.status,
            createdAt = Clock.System.now(),
            attribution = series.attribution,
        )
        seriesState.update { it + created }
        return created
    }

    override suspend fun updateSeries(series: Series) {
        seriesState.update { current ->
            current.map { if (it.id == series.id) series else it }
        }
    }

    override suspend fun deleteSeries(seriesId: String) {
        seriesState.update { it.filterNot { series -> series.id == seriesId } }
        episodesState.update { it.filterNot { episode -> episode.seriesId == seriesId } }
    }

    override suspend fun addEpisode(episode: NewEpisode): Episode {
        val created = Episode(
            id = newId(),
            seriesId = episode.seriesId,
            number = episode.number,
            title = episode.title,
            description = episode.description,
            thumbnailUrl = episode.thumbnailUrl,
            videoUrl = episode.videoUrl,
            durationSeconds = episode.durationSeconds,
            publishedAt = episode.publishedAt,
            introStartSeconds = episode.introStartSeconds,
            introEndSeconds = episode.introEndSeconds,
        )
        episodesState.update { it + created }
        return created
    }

    override suspend fun updateEpisode(episode: Episode) {
        episodesState.update { current ->
            current.map { if (it.id == episode.id) episode else it }
        }
    }

    override suspend fun deleteEpisode(episodeId: String) {
        episodesState.update { it.filterNot { episode -> episode.id == episodeId } }
    }

    private fun newId(): String = buildString {
        repeat(8) { append(Random.nextInt(16).toString(16)) }
    }

    companion object {
        private val seedInstant = Instant.parse("2010-01-01T00:00:00Z")

        private fun seedSeries(): List<Series> = listOf(
            Series(
                id = "caminandes",
                title = "Caminandes",
                description = "Koro the stubborn llama battles the harsh Patagonian landscape, one bad idea at a time.",
                coverUrl = "https://archive.org/services/img/CaminandesLlamigos",
                genres = listOf("Comedy", "Animation"),
                status = SeriesStatus.COMPLETED,
                createdAt = Instant.parse("2013-01-01T00:00:00Z"),
                attribution = "Caminandes © Blender Foundation | caminandes.com | CC BY 3.0",
            ),
            Series(
                id = "bbb",
                title = "Big Buck Bunny",
                description = "A large-hearted rabbit takes on three bullying rodents in a lush forest.",
                coverUrl = "https://archive.org/services/img/BigBuckBunny_124",
                genres = listOf("Comedy", "Animation"),
                status = SeriesStatus.COMPLETED,
                createdAt = seedInstant,
                attribution = "Big Buck Bunny © Blender Foundation | CC BY 3.0",
            ),
            Series(
                id = "sintel",
                title = "Sintel",
                description = "A young woman searches for a dragon she raised from infancy.",
                coverUrl = "https://archive.org/services/img/Sintel",
                genres = listOf("Fantasy", "Adventure", "Animation"),
                status = SeriesStatus.COMPLETED,
                createdAt = seedInstant,
                attribution = "Sintel © Blender Foundation | CC BY 3.0",
            ),
            Series(
                id = "elephants-dream",
                title = "Elephants Dream",
                description = "Two men explore a surreal mechanical world and the secrets it hides.",
                coverUrl = "https://archive.org/services/img/ElephantsDream",
                genres = listOf("Sci-Fi", "Animation"),
                status = SeriesStatus.COMPLETED,
                createdAt = seedInstant,
                attribution = "Elephants Dream © Blender Foundation / Netherlands Media Art Institute | CC BY 2.5",
            ),
        )

        private fun seedEpisodes(): List<Episode> = listOf(
            Episode(
                id = "caminandes-ep1",
                seriesId = "caminandes",
                number = 1,
                title = "Llama Drama",
                description = "Koro wants the grass on the other side of the road. The road has other plans.",
                thumbnailUrl = "https://archive.org/services/img/Caminandes1LlamaDrama",
                videoUrl = "https://archive.org/download/Caminandes1LlamaDrama/01_llama_drama_1080p.mp4",
                durationSeconds = 90,
                publishedAt = Instant.parse("2013-01-01T00:00:00Z"),
                introStartSeconds = 0,
                introEndSeconds = 8,
            ),
            Episode(
                id = "caminandes-ep2",
                seriesId = "caminandes",
                number = 2,
                title = "Gran Dillama",
                description = "An electric fence stands between Koro and the greener pastures beyond.",
                thumbnailUrl = "https://archive.org/services/img/Caminandes2GranDillama",
                videoUrl = "https://archive.org/download/Caminandes2GranDillama/02_gran_dillama_1080p.mp4",
                durationSeconds = 146,
                publishedAt = Instant.parse("2013-11-01T00:00:00Z"),
            ),
            Episode(
                id = "caminandes-ep3",
                seriesId = "caminandes",
                number = 3,
                title = "Llamigos",
                description = "Winter arrives, berries are scarce, and Koro meets a penguin with the same idea.",
                thumbnailUrl = "https://archive.org/services/img/CaminandesLlamigos",
                videoUrl = "https://archive.org/download/CaminandesLlamigos/Caminandes_%20Llamigos-1080p.mp4",
                durationSeconds = 150,
                publishedAt = Instant.parse("2016-01-29T00:00:00Z"),
            ),
            Episode(
                id = "bbb-ep1",
                seriesId = "bbb",
                number = 1,
                title = "Big Buck Bunny",
                description = "The full open movie.",
                thumbnailUrl = "https://archive.org/services/img/BigBuckBunny_124",
                videoUrl = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                durationSeconds = 596,
                publishedAt = seedInstant,
            ),
            Episode(
                id = "sintel-ep1",
                seriesId = "sintel",
                number = 1,
                title = "Sintel",
                description = "The full open movie.",
                thumbnailUrl = "https://archive.org/services/img/Sintel",
                videoUrl = "https://archive.org/download/Sintel/sintel-2048-surround.mp4",
                durationSeconds = 888,
                publishedAt = seedInstant,
            ),
            Episode(
                id = "elephants-dream-ep1",
                seriesId = "elephants-dream",
                number = 1,
                title = "Elephants Dream",
                description = "The full open movie.",
                thumbnailUrl = "https://archive.org/services/img/ElephantsDream",
                videoUrl = "https://archive.org/download/ElephantsDream/ed_hd.mp4",
                durationSeconds = 653,
                publishedAt = seedInstant,
            ),
        )
    }
}
