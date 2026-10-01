package com.anime.app.data

import kotlinx.coroutines.flow.Flow

data class SocialGraph(
    /** Newest follow first. */
    val followedCreatorIds: List<String> = emptyList(),
    val likedSeriesIds: Set<String> = emptySet(),
    val likedFilmIds: Set<String> = emptySet(),
) {
    fun isFollowing(creatorId: String): Boolean = creatorId in followedCreatorIds

    fun isSeriesLiked(seriesId: String): Boolean = seriesId in likedSeriesIds

    fun isFilmLiked(filmId: String): Boolean = filmId in likedFilmIds
}

/** Device-local follows and likes. There is no account yet, so this never leaves the phone. */
interface SocialRepository {
    fun observe(): Flow<SocialGraph>
    fun setFollowing(creatorId: String, following: Boolean)
    fun setSeriesLiked(seriesId: String, liked: Boolean)
    fun setFilmLiked(filmId: String, liked: Boolean)
}
