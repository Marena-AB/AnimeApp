package com.anime.app.data

import com.anime.app.model.WatchProgress
import kotlinx.coroutines.flow.Flow

/** Per-device watch progress. Viewers are anonymous, so this never leaves the device. */
interface WatchProgressRepository {
    fun observeAll(): Flow<Map<String, WatchProgress>>
    fun get(filmId: String): WatchProgress?
    fun save(progress: WatchProgress)
}
