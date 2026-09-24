package com.anime.app.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchProgress(
    val episodeId: String,
    val positionSeconds: Int,
    val durationSeconds: Int,
    val completed: Boolean,
    val updatedAtEpochMs: Long,
) {
    val fraction: Float
        get() = if (durationSeconds > 0) (positionSeconds.toFloat() / durationSeconds).coerceIn(0f, 1f) else 0f
}
