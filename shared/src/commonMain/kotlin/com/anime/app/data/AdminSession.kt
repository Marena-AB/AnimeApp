package com.anime.app.data

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Prototype stand-in for creator sign-in: a device-local flag that only unlocks editing UI.
 * It is not a security boundary; in Phase 5 writes are enforced server-side by Supabase RLS.
 */
class AdminSession(
    private val settings: Settings = Settings(),
) {
    private val state = MutableStateFlow(settings.getBoolean(KEY, false))
    val isAdmin: StateFlow<Boolean> = state

    fun setAdmin(enabled: Boolean) {
        state.value = enabled
        settings.putBoolean(KEY, enabled)
    }

    private companion object {
        const val KEY = "admin_mode_v1"
    }
}
