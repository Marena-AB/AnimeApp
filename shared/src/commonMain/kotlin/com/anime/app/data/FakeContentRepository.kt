package com.anime.app.data

import com.anime.app.model.Creator
import com.anime.app.model.Film
import com.anime.app.model.Series
import com.anime.app.model.SeriesStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

class FakeContentRepository : ContentRepository {
    private val creatorsState = MutableStateFlow(seedCreators())
    private val seriesState = MutableStateFlow(seedSeries())
    private val filmsState = MutableStateFlow(seedFilms())

    override fun observeCreators(): Flow<List<Creator>> = creatorsState

    override fun observeCreator(creatorId: String): Flow<Creator?> =
        creatorsState.map { creators -> creators.find { it.id == creatorId } }

    override fun observeSeries(): Flow<List<Series>> = seriesState

    override fun observeSeries(seriesId: String): Flow<Series?> =
        seriesState.map { series -> series.find { it.id == seriesId } }

    override fun observeFilms(seriesId: String): Flow<List<Film>> =
        filmsState.map { films ->
            films
                .filter { it.seriesId == seriesId }
                .sortedBy { it.episodeNumber ?: Int.MAX_VALUE }
        }

    override fun observeOneOffs(): Flow<List<Film>> =
        filmsState.map { films -> films.filter { it.isOneOff } }

    override fun observeAllFilms(): Flow<List<Film>> = filmsState

    override fun observeFilm(filmId: String): Flow<Film?> =
        filmsState.map { films -> films.find { it.id == filmId } }

    override suspend fun createSeries(series: NewSeries): Series {
        val created = Series(
            id = newId(),
            creatorId = series.creatorId,
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
        filmsState.update { it.filterNot { film -> film.seriesId == seriesId } }
    }

    override suspend fun addFilm(film: NewFilm): Film {
        val created = Film(
            id = newId(),
            creatorId = film.creatorId,
            seriesId = film.seriesId,
            episodeNumber = film.episodeNumber,
            title = film.title,
            description = film.description,
            thumbnailUrl = film.thumbnailUrl,
            videoUrl = film.videoUrl,
            durationSeconds = film.durationSeconds,
            publishedAt = film.publishedAt,
            tools = film.tools,
            modelName = film.modelName,
            origin = film.origin,
            introStartSeconds = film.introStartSeconds,
            introEndSeconds = film.introEndSeconds,
        )
        filmsState.update { it + created }
        return created
    }

    override suspend fun updateFilm(film: Film) {
        filmsState.update { current ->
            current.map { if (it.id == film.id) film else it }
        }
    }

    override suspend fun deleteFilm(filmId: String) {
        filmsState.update { it.filterNot { film -> film.id == filmId } }
    }

    private fun newId(): String = buildString {
        repeat(8) { append(Random.nextInt(16).toString(16)) }
    }

    companion object {
        private val seedInstant = Instant.parse("2010-01-01T00:00:00Z")

        private fun seedCreators(): List<Creator> = listOf(
            Creator(
                id = "pampas",
                displayName = "Pampas Pictures",
                avatarUrl = "",
                bio = "Short comedies about animals who refuse to learn.",
            ),
            Creator(
                id = "peach",
                displayName = "Peach Tree",
                avatarUrl = "",
                bio = "Bright forest films and one-off experiments.",
            ),
            Creator(
                id = "relay",
                displayName = "Relay",
                avatarUrl = "",
                bio = "Fantasy and mechanical worlds.",
            ),
            Creator(
                id = "og123",
                displayName = "OG123",
                avatarUrl = "",
                bio = "Uploads hosted on Mux.",
            ),
        )

        private fun seedSeries(): List<Series> = listOf(
            Series(
                id = "jjk",
                creatorId = "og123",
                title = "JJK",
                description = "Two episodes hosted on Mux.",
                coverUrl = "https://image.mux.com/6Tx7rD01KmWlfwOqEpHtghxVFC2vMfXPjEokm01cGN4RI/thumbnail.jpg",
                genres = listOf("Animation"),
                status = SeriesStatus.ONGOING,
                createdAt = Instant.parse("2026-09-28T00:00:00Z"),
                attribution = "",
            ),
            Series(
                id = "caminandes",
                creatorId = "pampas",
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
                creatorId = "peach",
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
                creatorId = "relay",
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
                creatorId = "relay",
                title = "Elephants Dream",
                description = "Two men explore a surreal mechanical world and the secrets it hides.",
                coverUrl = "https://archive.org/services/img/ElephantsDream",
                genres = listOf("Sci-Fi", "Animation"),
                status = SeriesStatus.COMPLETED,
                createdAt = seedInstant,
                attribution = "Elephants Dream © Blender Foundation / Netherlands Media Art Institute | CC BY 2.5",
            ),
        )

        private fun seedFilms(): List<Film> = listOf(
            Film(
                id = "caminandes-ep1",
                creatorId = "pampas",
                seriesId = "caminandes",
                episodeNumber = 1,
                title = "Llama Drama",
                description = "Koro wants the grass on the other side of the road. The road has other plans.",
                thumbnailUrl = "https://archive.org/services/img/Caminandes1LlamaDrama",
                videoUrl = "https://archive.org/download/Caminandes1LlamaDrama/01_llama_drama_1080p.mp4",
                durationSeconds = 90,
                publishedAt = Instant.parse("2013-01-01T00:00:00Z"),
                introStartSeconds = 0,
                introEndSeconds = 8,
            ),
            Film(
                id = "caminandes-ep2",
                creatorId = "pampas",
                seriesId = "caminandes",
                episodeNumber = 2,
                title = "Gran Dillama",
                description = "An electric fence stands between Koro and the greener pastures beyond.",
                thumbnailUrl = "https://archive.org/services/img/Caminandes2GranDillama",
                videoUrl = "https://archive.org/download/Caminandes2GranDillama/02_gran_dillama_1080p.mp4",
                durationSeconds = 146,
                publishedAt = Instant.parse("2013-11-01T00:00:00Z"),
            ),
            Film(
                id = "caminandes-ep3",
                creatorId = "pampas",
                seriesId = "caminandes",
                episodeNumber = 3,
                title = "Llamigos",
                description = "Winter arrives, berries are scarce, and Koro meets a penguin with the same idea.",
                thumbnailUrl = "https://archive.org/services/img/CaminandesLlamigos",
                videoUrl = "https://archive.org/download/CaminandesLlamigos/Caminandes_%20Llamigos-1080p.mp4",
                durationSeconds = 150,
                publishedAt = Instant.parse("2016-01-29T00:00:00Z"),
            ),
            Film(
                id = "bbb-ep1",
                creatorId = "peach",
                seriesId = "bbb",
                episodeNumber = 1,
                title = "Big Buck Bunny",
                description = "The full open movie.",
                thumbnailUrl = "https://archive.org/services/img/BigBuckBunny_124",
                videoUrl = "https://archive.org/download/BigBuckBunny_124/Content/big_buck_bunny_720p_surround.mp4",
                durationSeconds = 596,
                publishedAt = seedInstant,
            ),
            Film(
                id = "sintel-ep1",
                creatorId = "relay",
                seriesId = "sintel",
                episodeNumber = 1,
                title = "Sintel",
                description = "The full open movie.",
                thumbnailUrl = "https://archive.org/services/img/Sintel",
                videoUrl = "https://archive.org/download/Sintel/sintel-2048-surround.mp4",
                durationSeconds = 888,
                publishedAt = seedInstant,
            ),
            Film(
                id = "elephants-dream-ep1",
                creatorId = "relay",
                seriesId = "elephants-dream",
                episodeNumber = 1,
                title = "Elephants Dream",
                description = "The full open movie.",
                thumbnailUrl = "https://archive.org/services/img/ElephantsDream",
                videoUrl = "https://archive.org/download/ElephantsDream/ed_hd.mp4",
                durationSeconds = 653,
                publishedAt = seedInstant,
            ),
            Film(
                id = "jjk-ep1",
                creatorId = "og123",
                seriesId = "jjk",
                episodeNumber = 1,
                title = "Yuta",
                description = "Episode 1. Mux title JJK.",
                thumbnailUrl = "https://image.mux.com/6Tx7rD01KmWlfwOqEpHtghxVFC2vMfXPjEokm01cGN4RI/thumbnail.jpg",
                videoUrl = "https://stream.mux.com/6Tx7rD01KmWlfwOqEpHtghxVFC2vMfXPjEokm01cGN4RI.m3u8",
                durationSeconds = 241,
                publishedAt = Instant.parse("2026-09-28T04:22:34Z"),
            ),
            Film(
                id = "jjk-ep2",
                creatorId = "og123",
                seriesId = "jjk",
                episodeNumber = 2,
                title = "Suki",
                description = "Episode 2. Mux title JJK.",
                thumbnailUrl = "https://image.mux.com/6LiNulQ3c7uKlCK1bzvWjK4BfPwG7KxeNZauZNITdQY/thumbnail.jpg",
                videoUrl = "https://stream.mux.com/6LiNulQ3c7uKlCK1bzvWjK4BfPwG7KxeNZauZNITdQY.m3u8",
                durationSeconds = 168,
                publishedAt = Instant.parse("2026-09-28T04:24:58Z"),
            ),
            Film(
                id = "oneoff-music",
                creatorId = "og123",
                title = "MusicTest",
                description = "Standalone short hosted on Mux.",
                thumbnailUrl = "https://image.mux.com/gEIsW3u5ZhNERRb01yd73QReSeV02t2V5UBKuXXWIdVYs/thumbnail.jpg",
                videoUrl = "https://stream.mux.com/gEIsW3u5ZhNERRb01yd73QReSeV02t2V5UBKuXXWIdVYs.m3u8",
                durationSeconds = 252,
                publishedAt = Instant.parse("2026-09-28T04:26:10Z"),
            ),
            Film(
                id = "oneoff-ai",
                creatorId = "og123",
                title = "AI video",
                description = "Standalone short hosted on Mux.",
                thumbnailUrl = "https://image.mux.com/wC5lxvyKyo2JjIzR2QY3H7O7pYDU1HkaN6zyrRHyyp8/thumbnail.jpg",
                videoUrl = "https://stream.mux.com/wC5lxvyKyo2JjIzR2QY3H7O7pYDU1HkaN6zyrRHyyp8.m3u8",
                durationSeconds = 641,
                publishedAt = Instant.parse("2026-09-28T04:25:52Z"),
            ),
            Film(
                id = "oneoff-fence",
                creatorId = "peach",
                title = "The Fence",
                description = "A standalone short. Gran Dillama © Blender Foundation | CC BY 3.0.",
                thumbnailUrl = "https://archive.org/services/img/Caminandes2GranDillama",
                videoUrl = "https://archive.org/download/Caminandes2GranDillama/02_gran_dillama_1080p.mp4",
                durationSeconds = 146,
                publishedAt = Instant.parse("2013-11-01T00:00:00Z"),
            ),
            Film(
                id = "oneoff-search",
                creatorId = "pampas",
                title = "The Search",
                description = "A standalone film. Sintel © Blender Foundation | CC BY 3.0.",
                thumbnailUrl = "https://archive.org/services/img/Sintel",
                videoUrl = "https://archive.org/download/Sintel/sintel-2048-surround.mp4",
                durationSeconds = 888,
                publishedAt = seedInstant,
            ),
        )
    }
}
