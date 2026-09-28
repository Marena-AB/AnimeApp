package com.anime.app.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsSocialRepositoryTest {
    @Test
    fun followsAndLikesPersistAcrossANewRepository() = runTest {
        val settings = MemorySettings()
        val repository = SettingsSocialRepository(settings)
        repository.setFollowing("relay", true)
        repository.setFollowing("peach", true)
        repository.setSeriesLiked("sintel", true)
        repository.setFilmLiked("oneoff-fence", true)
        repository.setFollowing("relay", false)
        repository.setSeriesLiked("sintel", false)

        val graph = SettingsSocialRepository(settings).observe().first()
        assertEquals(listOf("peach"), graph.followedCreatorIds)
        assertFalse(graph.isSeriesLiked("sintel"))
        assertTrue(graph.isFilmLiked("oneoff-fence"))
    }
}

private class MemorySettings : Settings {
    private val strings = mutableMapOf<String, String>()

    override val keys: Set<String> get() = strings.keys
    override val size: Int get() = strings.size

    override fun clear() = strings.clear()
    override fun remove(key: String) { strings.remove(key) }
    override fun hasKey(key: String): Boolean = key in strings

    override fun putString(key: String, value: String) { strings[key] = value }
    override fun getString(key: String, defaultValue: String): String = strings[key] ?: defaultValue
    override fun getStringOrNull(key: String): String? = strings[key]

    override fun putInt(key: String, value: Int) = unused()
    override fun getInt(key: String, defaultValue: Int): Int = unused()
    override fun getIntOrNull(key: String): Int? = unused()
    override fun putLong(key: String, value: Long) = unused()
    override fun getLong(key: String, defaultValue: Long): Long = unused()
    override fun getLongOrNull(key: String): Long? = unused()
    override fun putFloat(key: String, value: Float) = unused()
    override fun getFloat(key: String, defaultValue: Float): Float = unused()
    override fun getFloatOrNull(key: String): Float? = unused()
    override fun putDouble(key: String, value: Double) = unused()
    override fun getDouble(key: String, defaultValue: Double): Double = unused()
    override fun getDoubleOrNull(key: String): Double? = unused()
    override fun putBoolean(key: String, value: Boolean) = unused()
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = unused()
    override fun getBooleanOrNull(key: String): Boolean? = unused()

    private fun unused(): Nothing = error("unused")
}
