package com.anime.app.data

import com.anime.app.model.WatchProgress
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Stores progress as one JSON blob in SharedPreferences (Android) / NSUserDefaults (iOS). */
class SettingsWatchProgressRepository(
    private val settings: Settings = Settings(),
) : WatchProgressRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val state = MutableStateFlow(load())

    override fun observeAll(): Flow<Map<String, WatchProgress>> = state

    override fun get(episodeId: String): WatchProgress? = state.value[episodeId]

    override fun save(progress: WatchProgress) {
        state.update { it + (progress.episodeId to progress) }
        settings.putString(KEY, json.encodeToString(state.value.values.toList()))
    }

    private fun load(): Map<String, WatchProgress> {
        val stored = settings.getStringOrNull(KEY) ?: return emptyMap()
        return try {
            json.decodeFromString<List<WatchProgress>>(stored).associateBy { it.episodeId }
        } catch (e: SerializationException) {
            emptyMap()
        } catch (e: IllegalArgumentException) {
            emptyMap()
        }
    }

    private companion object {
        const val KEY = "watch_progress_v1"
    }
}
