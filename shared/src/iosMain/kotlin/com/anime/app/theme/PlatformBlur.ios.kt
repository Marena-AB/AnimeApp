package com.anime.app.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.Dp

actual fun Modifier.platformBlur(radius: Dp): Modifier = blur(radius)
