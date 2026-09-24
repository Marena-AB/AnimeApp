package com.anime.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object WelcomeRoute

@Serializable
data object HomeRoute

@Serializable
data object ComponentGalleryRoute

@Serializable
data class SeriesDetailRoute(val seriesId: String)

@Serializable
data class PlayerRoute(val episodeId: String)

/** `seriesId == null` creates a new series. */
@Serializable
data class SeriesEditorRoute(val seriesId: String? = null)

/** `episodeId == null` adds a new episode to [seriesId]. */
@Serializable
data class EpisodeEditorRoute(val seriesId: String, val episodeId: String? = null)
