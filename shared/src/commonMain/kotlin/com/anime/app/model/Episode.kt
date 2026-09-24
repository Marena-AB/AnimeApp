package com.anime.app.model

import kotlin.time.Instant

data class Episode(
    val id: String,
    val seriesId: String,
    val number: Int,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val durationSeconds: Int,
    val publishedAt: Instant,
    val introStartSeconds: Int? = null,
    val introEndSeconds: Int? = null,
)
