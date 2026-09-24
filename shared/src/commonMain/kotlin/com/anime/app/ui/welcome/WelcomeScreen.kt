package com.anime.app.ui.welcome

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anime.app.ui.components.BrandMark
import com.anime.app.theme.AnimeTheme

@Composable
fun WelcomeScreen(onEnter: () -> Unit) {
    val motion = AnimeTheme.motion
    val haptics = LocalHapticFeedback.current
    var entered by remember { mutableStateOf(motion.reduced) }
    LaunchedEffect(Unit) { entered = true }
    val reveal by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(motion.expressiveMillis, easing = motion.easing),
        label = "welcome-logo-reveal",
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        BrandMark(
            modifier = Modifier.graphicsLayer {
                alpha = reveal
                translationY = (1f - reveal) * -18.dp.toPx()
                scaleX = 0.9f + reveal * 0.1f
                scaleY = 0.9f + reveal * 0.1f
            },
        )
        Spacer(modifier = Modifier.weight(0.7f))
        WelcomeArtwork(reveal = reveal)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Animation,\nwithout the gatekeepers.",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Discover original worlds and follow every story from its first frame.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 14.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onEnter()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            ),
        ) {
            Text("Start watching", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
        Text(
            text = "INDEPENDENT STORIES · NEW PERSPECTIVES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 14.dp),
        )
    }
}

@Composable
private fun WelcomeArtwork(reveal: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
            .graphicsLayer {
                alpha = reveal
                scaleX = 0.94f + reveal * 0.06f
                scaleY = 0.94f + reveal * 0.06f
                rotationZ = (1f - reveal) * -2.5f
            }
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.72f)
                .align(Alignment.CenterEnd)
                .background(MaterialTheme.colorScheme.primary),
        )
        Box(
            modifier = Modifier
                .fillMaxSize(0.48f)
                .align(Alignment.BottomStart)
                .background(MaterialTheme.colorScheme.secondaryContainer),
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.28f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.78f)),
        )
        Row(
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "01",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "ORIGINAL MOTION",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = "PLAY",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
