package com.anime.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun BrandMark(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 20.dp else 24.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 7.dp else 9.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary),
            )
        }
        Text(
            text = "ANIME / PLATFORM",
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = if (compact) MaterialTheme.typography.labelLarge.letterSpacing else MaterialTheme.typography.titleMedium.letterSpacing,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        androidx.compose.foundation.layout.Column {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun MetadataPill(
    text: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = if (accent) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.88f)
        },
        contentColor = if (accent) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = CircleShape,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
fun DotDivider(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.outline) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(2.dp))
        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(2.dp))
    }
}
