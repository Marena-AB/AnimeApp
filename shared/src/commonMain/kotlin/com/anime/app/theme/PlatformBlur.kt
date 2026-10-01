package com.anime.app.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * Applies a backdrop blur when the platform supports it.
 * Android 11 and below ignore [androidx.compose.ui.draw.blur]; callers should
 * still paint a darkened gradient so the fallback reads as cinematic.
 */
expect fun Modifier.platformBlur(radius: Dp): Modifier
