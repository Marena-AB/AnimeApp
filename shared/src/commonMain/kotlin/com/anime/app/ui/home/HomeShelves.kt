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
 * Next episode, then that creator's other work, then series that share a genre.
 * Hidden until something has been played. The film just watched is left off the row.
 */
fun becauseYouWatched(
    films: List<Film>,
    series: List<Series>,
    progress: Collection<WatchProgress>,
    creatorsById: Map<String, Creator>,
): BecauseYouWatched? {
    val latest = progress.maxByOrNull { it.updatedAtEpochMs } ?: return null
    val anchor = films.find { it.id == latest.filmId } ?: return null
    val anchorSeries = series.find { it.id == anchor.seriesId }
    val nextEpisodes = films
        .filter { film ->
            film.seriesId != null &&
                film.seriesId == anchor.seriesId &&
                film.id != anchor.id
        }
        .sortedBy { it.episodeNumber ?: Int.MAX_VALUE }
        .map { it.toEntry(creatorsById) }
    val sameCreatorSeries = series
        .filter { it.creatorId == anchor.creatorId && it.id != anchor.seriesId }
        .map { it.toEntry(creatorsById) }
    val sameCreatorShorts = films
        .filter { film -> film.isOneOff && film.creatorId == anchor.creatorId && film.id != anchor.id }
        .map { it.toEntry(creatorsById) }
    val closeMatches = (nextEpisodes + sameCreatorSeries + sameCreatorShorts)
        .distinctBy { it.key }
    val genres = anchorSeries?.genres?.toSet().orEmpty()
    val similarSeries = if (closeMatches.size >= 3) {
        emptyList()
    } else {
        series
            .filter { show ->
                show.id != anchor.seriesId &&
                    show.creatorId != anchor.creatorId &&
                    show.genres.any { it in genres }
            }
            .map { it.toEntry(creatorsById) }
    }
    val entries = (closeMatches + similarSeries)
        .distinctBy { it.key }
        .take(SHELF_LIMIT)
    if (entries.isEmpty()) return null
    return BecauseYouWatched(watchedTitle = anchor.title, entries = entries)
}

/** Other series, unstarted ones first. The featured series stays on the banner. */
fun recommendedShelf(
    films: List<Film>,
    series: List<Series>,
    featuredSeriesId: String?,
    progress: Collection<WatchProgress>,
    excludeKeys: Set<String>,
    creatorsById: Map<String, Creator>,
): List<FollowingEntry> {
    val started = progress.map { it.filmId }.toSet()
    val others = series.filter { it.id != featuredSeriesId }
    val unstarted = others.filter { show -> films.none { it.seriesId == show.id && it.id in started } }
    val startedShows = others.filter { it !in unstarted }
    return (unstarted + startedShows)
        .map { it.toEntry(creatorsById) }
        .filter { it.key !in excludeKeys }
        .distinctBy { it.key }
        .take(SHELF_LIMIT)
}

private fun Film.toEntry(creatorsById: Map<String, Creator>): FollowingEntry =
    FollowingEntry.FilmEntry(this, creatorsById[creatorId]?.displayName.orEmpty())

private fun Series.toEntry(creatorsById: Map<String, Creator>): FollowingEntry =
    FollowingEntry.SeriesEntry(this, creatorsById[creatorId]?.displayName.orEmpty())
