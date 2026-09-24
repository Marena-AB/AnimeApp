package com.anime.app.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import animeapp.shared.generated.resources.Res
import animeapp.shared.generated.resources.space_grotesk_variable
import animeapp.shared.generated.resources.unbounded_variable
import org.jetbrains.compose.resources.Font

private val NeonNightColors = darkColorScheme(
    primary = Color(0xFFFF3DAD),
    onPrimary = Color(0xFF210013),
    primaryContainer = Color(0xFF4A0B32),
    onPrimaryContainer = Color(0xFFFFD8EC),
    secondary = Color(0xFF2DEBFF),
    onSecondary = Color(0xFF001F24),
    secondaryContainer = Color(0xFF073A42),
    onSecondaryContainer = Color(0xFFBDF5FF),
    tertiary = Color(0xFFB69CFF),
    onTertiary = Color(0xFF20104D),
    background = Color(0xFF050508),
    onBackground = Color(0xFFF8F5FA),
    surface = Color(0xFF0D0D13),
    onSurface = Color(0xFFF8F5FA),
    surfaceVariant = Color(0xFF1B1922),
    onSurfaceVariant = Color(0xFFCDC5D2),
    surfaceContainer = Color(0xFF111118),
    surfaceContainerHigh = Color(0xFF1A1821),
    outline = Color(0xFF4B4452),
    outlineVariant = Color(0xFF2C2831),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val InkColors = darkColorScheme(
    primary = Color(0xFFFF5B45),
    onPrimary = Color(0xFF260300),
    primaryContainer = Color(0xFF41130D),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFF4EFE6),
    onSecondary = Color(0xFF1C1B18),
    secondaryContainer = Color(0xFF2A2925),
    onSecondaryContainer = Color(0xFFF4EFE6),
    tertiary = Color(0xFFC9C2B8),
    onTertiary = Color(0xFF24211D),
    background = Color.Black,
    onBackground = Color(0xFFF6F0E7),
    surface = Color(0xFF090909),
    onSurface = Color(0xFFF6F0E7),
    surfaceVariant = Color(0xFF1A1917),
    onSurfaceVariant = Color(0xFFD1CBC2),
    surfaceContainer = Color(0xFF10100F),
    surfaceContainerHigh = Color(0xFF1A1917),
    outline = Color(0xFF5B5751),
    outlineVariant = Color(0xFF302E2A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val StudioColors = darkColorScheme(
    primary = Color(0xFFE8C98C),
    onPrimary = Color(0xFF261A04),
    primaryContainer = Color(0xFF3B3020),
    onPrimaryContainer = Color(0xFFFFE3AD),
    secondary = Color(0xFFD2D0CB),
    onSecondary = Color(0xFF232321),
    secondaryContainer = Color(0xFF30302E),
    onSecondaryContainer = Color(0xFFE8E5DF),
    tertiary = Color(0xFFAEBAC6),
    onTertiary = Color(0xFF17212A),
    background = Color(0xFF0B0C0D),
    onBackground = Color(0xFFF1F0ED),
    surface = Color(0xFF111213),
    onSurface = Color(0xFFF1F0ED),
    surfaceVariant = Color(0xFF202123),
    onSurfaceVariant = Color(0xFFC6C5C1),
    surfaceContainer = Color(0xFF151617),
    surfaceContainerHigh = Color(0xFF1C1D1F),
    outline = Color(0xFF4A4B4D),
    outlineVariant = Color(0xFF2B2C2E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun AnimeAppTheme(
    direction: DesignDirection,
    reducedMotion: Boolean,
    pageAccent: Color? = null,
    content: @Composable () -> Unit,
) {
    val displayFont = FontFamily(
        Font(Res.font.unbounded_variable, weight = FontWeight.Normal),
        Font(Res.font.unbounded_variable, weight = FontWeight.Bold),
        Font(Res.font.unbounded_variable, weight = FontWeight.Black),
    )
    val bodyFont = FontFamily(
        Font(Res.font.space_grotesk_variable, weight = FontWeight.Normal),
        Font(Res.font.space_grotesk_variable, weight = FontWeight.Medium),
        Font(Res.font.space_grotesk_variable, weight = FontWeight.Bold),
    )
    val baseColors = when (direction) {
        DesignDirection.NEON_NIGHT -> NeonNightColors
        DesignDirection.INK -> InkColors
        DesignDirection.STUDIO -> StudioColors
    }
    val colors = pageAccent?.let(baseColors::withAccent) ?: baseColors
    val radii = when (direction) {
        DesignDirection.NEON_NIGHT -> AnimeRadii(12.dp, 18.dp, 28.dp)
        DesignDirection.INK -> AnimeRadii(2.dp, 4.dp, 6.dp)
        DesignDirection.STUDIO -> AnimeRadii(8.dp, 12.dp, 16.dp)
    }
    val elevation = when (direction) {
        DesignDirection.NEON_NIGHT -> AnimeElevation(
            card = 3.dp,
            raised = 10.dp,
            overlay = 20.dp,
            glowColor = colors.primary,
            glowAlpha = 0.28f,
        )
        DesignDirection.INK -> AnimeElevation(
            card = 0.dp,
            raised = 2.dp,
            overlay = 8.dp,
            glowColor = Color.Transparent,
            glowAlpha = 0f,
        )
        DesignDirection.STUDIO -> AnimeElevation(
            card = 0.dp,
            raised = 4.dp,
            overlay = 12.dp,
            glowColor = Color.Black,
            glowAlpha = 0.12f,
        )
    }
    val texture = when (direction) {
        DesignDirection.NEON_NIGHT -> AnimeTexture(0.02f, 0f, 0.2f)
        DesignDirection.INK -> AnimeTexture(0.035f, 0.09f, 0f)
        DesignDirection.STUDIO -> AnimeTexture(0.012f, 0f, 0f)
    }
    val motion = AnimeMotion(
        quickMillis = if (reducedMotion) 0 else 140,
        standardMillis = if (reducedMotion) 0 else 260,
        expressiveMillis = if (reducedMotion) 0 else 480,
        easing = AnimeEasing,
        reduced = reducedMotion,
    )
    val shapes = Shapes(
        extraSmall = RoundedCornerShape((radii.control / 2)),
        small = RoundedCornerShape(radii.control),
        medium = RoundedCornerShape(radii.card),
        large = RoundedCornerShape(radii.hero),
        extraLarge = RoundedCornerShape(radii.hero + 8.dp),
    )

    CompositionLocalProvider(
        LocalDesignDirection provides direction,
        LocalAnimeSpacing provides AnimeSpacing(),
        LocalAnimeRadii provides radii,
        LocalAnimeElevation provides elevation,
        LocalAnimeMotion provides motion,
        LocalAnimeTexture provides texture,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = animeTypography(displayFont, bodyFont, direction),
            shapes = shapes,
            content = content,
        )
    }
}

private fun ColorScheme.withAccent(accent: Color): ColorScheme {
    val onAccent = if (accent.luminance() > 0.45f) Color(0xFF120B0E) else Color.White
    return copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent.copy(alpha = 0.24f).compositeOver(surface),
        onPrimaryContainer = onSurface,
    )
}

private fun animeTypography(
    displayFont: FontFamily,
    bodyFont: FontFamily,
    direction: DesignDirection,
) = Typography(
    displaySmall = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.Black,
        fontSize = if (direction == DesignDirection.STUDIO) 38.sp else 40.sp,
        lineHeight = 43.sp,
        letterSpacing = if (direction == DesignDirection.INK) (-1.4).sp else (-0.8).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 35.sp,
        letterSpacing = (-0.45).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 29.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = displayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    ),
    bodyLarge = TextStyle(fontFamily = bodyFont, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = bodyFont, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = bodyFont, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.4.sp,
    ),
)
