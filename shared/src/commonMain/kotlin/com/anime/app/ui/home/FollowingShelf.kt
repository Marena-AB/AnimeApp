package com.anime.app.ui.home

import com.anime.app.model.Creator
import com.anime.app.model.Film
import com.anime.app.model.Series

sealed interface FollowingEntry {
    val key: String
    val creatorName: String
    val title: String
    val imageUrl: String

    data class SeriesEntry(
        val series: Series,
        override val creatorName: String,
    ) : FollowingEntry {
        override val key: String = "series-${series.id}"
        override val title: String = series.title
        override val imageUrl: String = series.coverUrl
    }

    data class FilmEntry(
        val film: Film,
        override val creatorName: String,
    ) : FollowingEntry {
        override val key: String = "film-${film.id}"
        override val title: String = film.title
        override val imageUrl: String = film.thumbnailUrl
    }
}

/** Series and one-off films from followed creators, newest follow first. */
fun followingShelf(
    followedCreatorIds: List<String>,
    series: List<Series>,
    oneOffs: List<Film>,
    creatorsById: Map<String, Creator>,
): List<FollowingEntry> = followedCreatorIds.flatMap { creatorId ->
    val name = creatorsById[creatorId]?.displayName.orEmpty()
    val seriesEntries = series
        .filter { it.creatorId == creatorId }
        .map { FollowingEntry.SeriesEntry(it, name) }
    val filmEntries = oneOffs
        .filter { it.creatorId == creatorId && it.isOneOff }
        .map { FollowingEntry.FilmEntry(it, name) }
    seriesEntries + filmEntries
}
