package expo.modules.transparentvideo

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.sharedobjects.SharedObject
import kotlinx.coroutines.launch

// https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide#improvements_in_media3
@UnstableApi
class VideoPlayer(context: Context, private val appContext: AppContext, private val mediaItem: MediaItem, enableDecoderFallback: Boolean?) : AutoCloseable, SharedObject() {
  // This improves the performance of playing DRM-protected content
  private var renderersFactory = DefaultRenderersFactory(context)
      .forceEnableMediaCodecAsynchronousQueueing()
      .setEnableDecoderFallback(enableDecoderFallback ?: false)

  private var loadControl = DefaultLoadControl.Builder()
      .setPrioritizeTimeOverSizeThresholds(false)
      .build()

  val player = ExoPlayer
      .Builder(context, renderersFactory)
      .setLooper(context.mainLooper)
      .setLoadControl(loadControl)
      .build()

  // We duplicate some properties of the player, because we don't want to always use the mainQueue to access them.
  var playing = false
  var isLoading = true

  // Volume of the player if there was no mute applied.
  var userVolume = 1f
  var requiresLinearPlayback = false
  var staysActiveInBackground = false
  var preservesPitch = false
    set(preservesPitch) {
      applyPitchCorrection()
      field = preservesPitch
    }

//  private var serviceConnection: ServiceConnection
  lateinit var timeline: Timeline

  var aspectRatio: Float? = null


  var volume = 1f
    set(volume) {
      if (player.volume == volume) return
      player.volume = if (muted) 0f else volume
      field = volume
    }

  var muted = false
    set(muted) {
      field = muted
      volume = if (muted) 0f else userVolume
    }

  var playbackParameters: PlaybackParameters = PlaybackParameters.DEFAULT
    set(value) {
      if (player.playbackParameters == value) return
      player.playbackParameters = value
      field = value
      applyPitchCorrection()
    }

  private val playerListener = object : Player.Listener {
    override fun onIsPlayingChanged(isPlaying: Boolean) {
      this@VideoPlayer.playing = isPlaying
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
      this@VideoPlayer.timeline = timeline
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
      this@VideoPlayer.isLoading = isLoading
    }

    override fun onVolumeChanged(volume: Float) {
      this@VideoPlayer.volume = volume
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
      this@VideoPlayer.aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
      super.onVideoSizeChanged(videoSize)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
      this@VideoPlayer.playbackParameters = playbackParameters
      super.onPlaybackParametersChanged(playbackParameters)
    }
  }

  init {
    player.addListener(playerListener)
    TransparentVideoManager.registerVideoPlayer(this)
  }

  override fun close() {
    TransparentVideoManager.unregisterVideoPlayer(this@VideoPlayer)

    appContext.mainQueue.launch {
      player.removeListener(playerListener)
      player.release()
    }
  }

  override fun deallocate() {
    super.deallocate()
    close()
  }

  fun prepare() {
    player.setMediaItem(mediaItem)
    player.prepare()
  }

  private fun applyPitchCorrection() {
    val speed = playbackParameters.speed
    val pitch = if (preservesPitch) 1f else speed
    playbackParameters = PlaybackParameters(speed, pitch)
  }
}
