package com.anime.app.model

import kotlin.time.Instant

data class Series(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val genres: List<String>,
    val status: SeriesStatus,
    val createdAt: Instant,
    val attribution: String,
)
