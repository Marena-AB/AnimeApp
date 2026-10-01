package com.anime.app.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class SettingsSocialRepository(
    private val settings: Settings = Settings(),
) : SocialRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val state = MutableStateFlow(load())

    override fun observe(): Flow<SocialGraph> = state

    override fun setFollowing(creatorId: String, following: Boolean) {
        update { graph ->
            val without = graph.followedCreatorIds.filterNot { it == creatorId }
            graph.copy(
                followedCreatorIds = if (following) listOf(creatorId) + without else without,
            )
        }
    }

    override fun setSeriesLiked(seriesId: String, liked: Boolean) {
        update { graph ->
            graph.copy(
                likedSeriesIds = if (liked) graph.likedSeriesIds + seriesId else graph.likedSeriesIds - seriesId,
            )
        }
    }

    override fun setFilmLiked(filmId: String, liked: Boolean) {
        update { graph ->
            graph.copy(
                likedFilmIds = if (liked) graph.likedFilmIds + filmId else graph.likedFilmIds - filmId,
            )
        }
    }

    private fun update(transform: (SocialGraph) -> SocialGraph) {
        state.update(transform)
        settings.putString(KEY, json.encodeToString(StoredSocial.from(state.value)))
    }

    private fun load(): SocialGraph {
        val stored = settings.getStringOrNull(KEY) ?: return SocialGraph()
        return try {
            json.decodeFromString<StoredSocial>(stored).toGraph()
        } catch (e: SerializationException) {
            SocialGraph()
        } catch (e: IllegalArgumentException) {
            SocialGraph()
        }
    }

    @Serializable
    private data class StoredSocial(
        val followedCreatorIds: List<String> = emptyList(),
        val likedSeriesIds: List<String> = emptyList(),
        val likedFilmIds: List<String> = emptyList(),
    ) {
        fun toGraph() = SocialGraph(
            followedCreatorIds = followedCreatorIds.distinct(),
            likedSeriesIds = likedSeriesIds.toSet(),
            likedFilmIds = likedFilmIds.toSet(),
        )

        companion object {
            fun from(graph: SocialGraph) = StoredSocial(
                followedCreatorIds = graph.followedCreatorIds,
                likedSeriesIds = graph.likedSeriesIds.toList(),
                likedFilmIds = graph.likedFilmIds.toList(),
            )
        }
    }

    private companion object {
        const val KEY = "social_graph_v1"
    }
}
