package com.anime.app.model

import kotlin.time.Instant

data class Film(
    val id: String,
    val creatorId: String,
    /** Null when this film is a one-off and does not belong to a series. */
    val seriesId: String? = null,
    /** Set only when [seriesId] is set. */
    val episodeNumber: Int? = null,
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
) {
    val isOneOff: Boolean
        get() = seriesId == null
}

/** The next episode in the same series. One-offs and the last episode have none. */
fun Film.nextIn(films: List<Film>): Film? {
    val seriesId = seriesId ?: return null
    val number = episodeNumber ?: return null
    return films
        .filter { it.seriesId == seriesId && (it.episodeNumber ?: -1) > number }
        .minByOrNull { it.episodeNumber ?: Int.MAX_VALUE }
}
