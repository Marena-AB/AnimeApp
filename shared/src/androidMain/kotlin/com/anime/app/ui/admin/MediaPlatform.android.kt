package com.anime.app.ui.admin

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.vinceglb.filekit.AndroidFile
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun PlatformFile.toMediaUrl(): String = when (val file = androidFile) {
    is AndroidFile.UriWrapper -> file.uri.toString()
    is AndroidFile.FileWrapper -> Uri.fromFile(file.file).toString()
}

private class AndroidMediaInspector(private val context: Context) : MediaInspector {
    override suspend fun videoDurationSeconds(url: String): Int? = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, Uri.parse(url))
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.let { (it / 1000).toInt() }
        } catch (e: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }
}

@Composable
actual fun rememberMediaInspector(): MediaInspector {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidMediaInspector(context) }
}
