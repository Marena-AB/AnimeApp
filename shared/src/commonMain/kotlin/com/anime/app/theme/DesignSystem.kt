package com.anime.app.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class DesignDirection(val label: String, val description: String) {
    NEON_NIGHT(
        label = "Neon Night",
        description = "Electric color, focused glow, Tokyo-after-dark energy.",
    ),
    INK(
        label = "Ink",
        description = "Manga contrast, halftone texture, bold editorial type.",
    ),
    STUDIO(
        label = "Studio",
        description = "Quiet gallery framing that lets cover art lead.",
    ),
}

class ThemeController(private val settings: Settings = Settings()) {
    private val state = MutableStateFlow(
        DesignDirection.entries.firstOrNull {
            it.name == settings.getString(KEY, DesignDirection.NEON_NIGHT.name)
        } ?: DesignDirection.NEON_NIGHT,
    )
    val direction: StateFlow<DesignDirection> = state

    fun select(direction: DesignDirection) {
        state.value = direction
        settings.putString(KEY, direction.name)
    }

    private companion object {
        const val KEY = "design_direction_v1"
    }
}

@Immutable
data class AnimeSpacing(
    val hairline: Dp = 1.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp,
    val hero: Dp = 64.dp,
)

@Immutable
data class AnimeRadii(
    val control: Dp,
    val card: Dp,
    val hero: Dp,
    val pill: Dp = 999.dp,
)

@Immutable
data class AnimeElevation(
    val card: Dp,
    val raised: Dp,
    val overlay: Dp,
    val glowColor: Color,
    val glowAlpha: Float,
)

@Immutable
data class AnimeMotion(
    val quickMillis: Int,
    val standardMillis: Int,
    val expressiveMillis: Int,
    val easing: Easing,
    val reduced: Boolean,
) {
    fun duration(normal: Int): Int = if (reduced) 0 else normal
}

@Immutable
data class AnimeTexture(
    val grainAlpha: Float,
    val halftoneAlpha: Float,
    val glowAlpha: Float,
)

val AnimeEasing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

val LocalAnimeSpacing = staticCompositionLocalOf { AnimeSpacing() }
val LocalAnimeRadii = staticCompositionLocalOf {
    AnimeRadii(control = 12.dp, card = 18.dp, hero = 28.dp)
}
val LocalAnimeElevation = staticCompositionLocalOf {
    AnimeElevation(
        card = 0.dp,
        raised = 8.dp,
        overlay = 16.dp,
        glowColor = Color.Transparent,
        glowAlpha = 0f,
    )
}
val LocalAnimeMotion = staticCompositionLocalOf {
    AnimeMotion(
        quickMillis = 140,
        standardMillis = 260,
        expressiveMillis = 480,
        easing = AnimeEasing,
        reduced = false,
    )
}
val LocalAnimeTexture = staticCompositionLocalOf {
    AnimeTexture(grainAlpha = 0f, halftoneAlpha = 0f, glowAlpha = 0f)
}
val LocalDesignDirection = staticCompositionLocalOf { DesignDirection.NEON_NIGHT }

object AnimeTheme {
    val spacing: AnimeSpacing
        @androidx.compose.runtime.Composable get() = LocalAnimeSpacing.current
    val radii: AnimeRadii
        @androidx.compose.runtime.Composable get() = LocalAnimeRadii.current
    val elevation: AnimeElevation
        @androidx.compose.runtime.Composable get() = LocalAnimeElevation.current
    val motion: AnimeMotion
        @androidx.compose.runtime.Composable get() = LocalAnimeMotion.current
    val texture: AnimeTexture
        @androidx.compose.runtime.Composable get() = LocalAnimeTexture.current
    val direction: DesignDirection
        @androidx.compose.runtime.Composable get() = LocalDesignDirection.current
}
