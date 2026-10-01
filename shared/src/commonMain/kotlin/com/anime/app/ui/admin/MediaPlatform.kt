package com.anime.app.ui.admin

import androidx.compose.runtime.Composable
import io.github.vinceglb.filekit.PlatformFile

/**
 * A URL for a picked file that the video players and Coil can load:
 * a `content://` URI on Android, a `file://` URL on iOS.
 * Only valid on this device, which is fine while content lives in the fake repository.
 */
expect fun PlatformFile.toMediaUrl(): String

interface MediaInspector {
    /** Duration of the video at [url], or null if it can't be read. */
    suspend fun videoDurationSeconds(url: String): Int?
}

@Composable
expect fun rememberMediaInspector(): MediaInspector
