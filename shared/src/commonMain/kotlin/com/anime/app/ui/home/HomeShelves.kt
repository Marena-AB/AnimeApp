package com.anime.app.ui.home

import com.anime.app.model.Creator
import com.anime.app.model.Film
import com.anime.app.model.Series
import com.anime.app.model.WatchProgress

data class BecauseYouWatched(
    val watchedTitle: String,
    val entries: List<FollowingEntry>,
)

private const val SHELF_LIMIT = 8

/**
 * Next episode, then the same creator's other series and one-offs.
 * Hidden until something has been played. In-progress films stay on Continue watching.
 */
fun becauseYouWatched(
    films: List<Film>,
    series: List<Series>,
    progress: Collection<WatchProgress>,
    creatorsById: Map<String, Creator>,
): BecauseYouWatched? {
    val latest = progress.maxByOrNull { it.updatedAtEpochMs } ?: return null
    val anchor = films.find { it.id == latest.filmId } ?: return null
    val inProgress = progress.filter { !it.completed }.map { it.filmId }.toSet()
    val nextEpisodes = films
        .filter { film ->
            film.seriesId != null &&
                film.seriesId == anchor.seriesId &&
                film.id != anchor.id &&
                film.id !in inProgress
        }
        .sortedBy { it.episodeNumber ?: Int.MAX_VALUE }
        .map { it.toEntry(creatorsById) }
    val sameCreatorSeries = series
        .filter { it.creatorId == anchor.creatorId && it.id != anchor.seriesId }
        .map { it.toEntry(creatorsById) }
    val sameCreatorShorts = films
        .filter { film ->
            film.isOneOff &&
                film.creatorId == anchor.creatorId &&
                film.id != anchor.id &&
                film.id !in inProgress
        }
        .map { it.toEntry(creatorsById) }
    val entries = (nextEpisodes + sameCreatorSeries + sameCreatorShorts)
        .distinctBy { it.key }
        .take(SHELF_LIMIT)
    if (entries.isEmpty()) return null
    return BecauseYouWatched(watchedTitle = anchor.title, entries = entries)
}

/** Series you have not started, other than the featured one. */
fun recommendedShelf(
    films: List<Film>,
    series: List<Series>,
    featuredSeriesId: String?,
    progress: Collection<WatchProgress>,
    excludeKeys: Set<String>,
    creatorsById: Map<String, Creator>,
): List<FollowingEntry> {
    val started = progress.map { it.filmId }.toSet()
    return series
        .filter { show ->
            show.id != featuredSeriesId &&
                films.none { it.seriesId == show.id && it.id in started }
        }
        .map { it.toEntry(creatorsById) }
        .filter { it.key !in excludeKeys }
        .distinctBy { it.key }
        .take(SHELF_LIMIT)
}

private fun Film.toEntry(creatorsById: Map<String, Creator>): FollowingEntry =
    FollowingEntry.FilmEntry(this, creatorsById[creatorId]?.displayName.orEmpty())

private fun Series.toEntry(creatorsById: Map<String, Creator>): FollowingEntry =
    FollowingEntry.SeriesEntry(this, creatorsById[creatorId]?.displayName.orEmpty())
