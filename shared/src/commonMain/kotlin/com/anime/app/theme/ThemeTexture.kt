package com.anime.app.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun ThemeTextureOverlay(modifier: Modifier = Modifier) {
    val direction = AnimeTheme.direction
    val texture = AnimeTheme.texture
    val accent = androidx.compose.material3.MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        when (direction) {
            DesignDirection.NEON_NIGHT -> {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = texture.glowAlpha),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.08f),
                        radius = size.maxDimension * 0.62f,
                    ),
                )
            }
            DesignDirection.INK -> {
                val step = 10f
                var y = 0f
                var row = 0
                while (y < size.height) {
                    var x = if (row % 2 == 0) 0f else step / 2f
                    while (x < size.width) {
                        drawCircle(
                            color = Color.White.copy(alpha = texture.halftoneAlpha),
                            radius = 1.15f,
                            center = Offset(x, y),
                        )
                        x += step
                    }
                    row++
                    y += step
                }
            }
            DesignDirection.STUDIO -> Unit
        }

        if (texture.grainAlpha > 0f) {
            val step = 29f
            var x = 7f
            var index = 0
            while (x < size.width) {
                val y = ((index * 47) % size.height.toInt().coerceAtLeast(1)).toFloat()
                drawCircle(
                    color = Color.White.copy(alpha = texture.grainAlpha),
                    radius = 0.55f,
                    center = Offset(x, y),
                )
                index++
                x += step
            }
        }
    }
}
