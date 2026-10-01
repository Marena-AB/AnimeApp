package com.anime.app.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.duration
import platform.CoreMedia.CMTimeGetSeconds
import platform.Foundation.NSURL

actual fun PlatformFile.toMediaUrl(): String = nsUrl.absoluteString ?: nsUrl.path.orEmpty()

private object IosMediaInspector : MediaInspector {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun videoDurationSeconds(url: String): Int? = withContext(Dispatchers.Default) {
        val nsUrl = NSURL.URLWithString(url) ?: return@withContext null
        @Suppress("DEPRECATION")
        val seconds = CMTimeGetSeconds(AVURLAsset(uRL = nsUrl, options = null).duration)
        if (seconds.isNaN() || seconds.isInfinite() || seconds <= 0.0) null else seconds.toInt()
    }
}

@Composable
actual fun rememberMediaInspector(): MediaInspector = remember { IosMediaInspector }
