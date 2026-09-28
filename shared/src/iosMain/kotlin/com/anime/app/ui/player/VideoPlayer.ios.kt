package com.anime.app.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFoundation.AVLayerVideoGravityResizeAspect
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.timeControlStatus
import platform.CoreGraphics.CGRectMake
import platform.CoreMedia.CMTime
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIInterfaceOrientationMask
import platform.UIKit.UIInterfaceOrientationMaskLandscape
import platform.UIKit.UIInterfaceOrientationMaskPortrait
import platform.UIKit.UIView
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindowSceneGeometryPreferencesIOS
import platform.UIKit.setNeedsUpdateOfSupportedInterfaceOrientations

private const val POSITION_POLL_MS = 250L

@OptIn(ExperimentalForeignApi::class)
private class AvVideoPlayerController(url: String, startPositionMs: Long) : VideoPlayerController {
    val player: AVPlayer = AVPlayer(uRL = requireNotNull(NSURL.URLWithString(url)) { "Invalid video URL: $url" })

    override var isPlaying by mutableStateOf(false)
        private set
    override var isBuffering by mutableStateOf(true)
        private set
    override var isEnded by mutableStateOf(false)
        private set
    override var positionMs by mutableLongStateOf(0L)
        private set
    override var durationMs by mutableLongStateOf(0L)
        private set
    override var error by mutableStateOf<String?>(null)
        private set

    private val endObserver = NSNotificationCenter.defaultCenter.addObserverForName(
        name = AVPlayerItemDidPlayToEndTimeNotification,
        `object` = player.currentItem,
        queue = NSOperationQueue.mainQueue,
    ) { _ -> isEnded = true }

    init {
        if (startPositionMs > 0L) seekTo(startPositionMs)
        player.play()
    }

    fun syncState() {
        val status = player.timeControlStatus
        isPlaying = status == AVPlayerTimeControlStatusPlaying
        isBuffering = status == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
        positionMs = player.currentTime().toMillis()
        val item = player.currentItem ?: return
        durationMs = item.duration.toMillis()
        if (item.status == AVPlayerItemStatusFailed) {
            error = item.error?.localizedDescription ?: "Playback failed"
        }
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(positionMs: Long) {
        val zero = CMTimeMake(value = 0, timescale = 1)
        player.seekToTime(
            time = CMTimeMakeWithSeconds(positionMs / 1000.0, preferredTimescale = 600),
            toleranceBefore = zero,
            toleranceAfter = zero,
        )
        this.positionMs = positionMs
        isEnded = false
    }

    fun release() {
        NSNotificationCenter.defaultCenter.removeObserver(endObserver)
        player.pause()
        player.replaceCurrentItemWithPlayerItem(null)
    }

    private fun CValue<CMTime>.toMillis(): Long {
        val seconds = CMTimeGetSeconds(this)
        return if (seconds.isNaN() || seconds.isInfinite()) 0L else (seconds * 1000).toLong()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class PlayerContainerView(player: AVPlayer) : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val playerLayer = AVPlayerLayer.playerLayerWithPlayer(player).apply {
        videoGravity = AVLayerVideoGravityResizeAspect
    }

    init {
        backgroundColor = UIColor.blackColor
        layer.addSublayer(playerLayer)
    }

    fun attach(player: AVPlayer) {
        if (playerLayer.player != player) playerLayer.player = player
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        playerLayer.frame = bounds
        CATransaction.commit()
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberVideoPlayerController(url: String, startPositionMs: Long): VideoPlayerController {
    val controller = remember(url) {
        // Play audio even when the ringer switch is silent, as users expect from a video app.
        AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, error = null)
        AvVideoPlayerController(url, startPositionMs)
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    LaunchedEffect(controller) {
        while (true) {
            controller.syncState()
            delay(POSITION_POLL_MS)
        }
    }
    return controller
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun VideoSurface(controller: VideoPlayerController, modifier: Modifier) {
    val player = (controller as AvVideoPlayerController).player
    key(player) {
        UIKitView(
            factory = { PlayerContainerView(player) },
            update = { it.attach(player) },
            modifier = modifier,
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = false,
            ),
        )
    }
}

@Composable
actual fun FullscreenEffect(enabled: Boolean) {
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }
        requestInterfaceOrientation(UIInterfaceOrientationMaskLandscape)
        onDispose { requestInterfaceOrientation(UIInterfaceOrientationMaskPortrait) }
    }
}

private fun requestInterfaceOrientation(mask: UIInterfaceOrientationMask) {
    val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene ?: return
    scene.keyWindow?.rootViewController?.setNeedsUpdateOfSupportedInterfaceOrientations()
    scene.requestGeometryUpdateWithPreferences(
        UIWindowSceneGeometryPreferencesIOS(interfaceOrientations = mask),
        errorHandler = null,
    )
}
