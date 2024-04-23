@file:OptIn(EitherType::class)
package expo.modules.video

import android.app.Activity
import android.view.View
import androidx.media3.common.PlaybackParameters
import expo.modules.kotlin.apifeatures.EitherType
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.REPEAT_MODE_OFF
import com.facebook.react.uimanager.PixelUtil
import com.facebook.react.uimanager.Spacing
import com.facebook.react.uimanager.ViewProps
import com.facebook.yoga.YogaConstants
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.types.Either
import expo.modules.video.records.VideoSource
import expo.modules.kotlin.views.ViewDefinitionBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// https://developer.android.com/guide/topics/media/media3/getting-started/migration-guide#improvements_in_media3
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class VideoModule : Module() {
  private val activity: Activity
    get() = appContext.activityProvider?.currentActivity ?: throw Exceptions.MissingActivity()

  override fun definition() = ModuleDefinition {
    Name("ExpoVideo")
    Class(VideoPlayer::class) {
      Constructor { source: VideoSource, enableDecoderFallback: Boolean? ->
        VideoPlayer(activity.applicationContext, appContext, source.toMediaItem(), enableDecoderFallback)
      }

      Property("playing")
          .get { ref: VideoPlayer ->
            ref.playing
          }

      Property("isLoading")
          .get { ref: VideoPlayer ->
            ref.isLoading
          }

      Property("muted")
          .get { ref: VideoPlayer ->
            ref.muted
          }
          .set { ref: VideoPlayer, muted: Boolean ->
            appContext.mainQueue.launch {
              ref.muted = muted
            }
          }

      Property("volume")
          .get { ref: VideoPlayer ->
            ref.volume
          }
          .set { ref: VideoPlayer, volume: Float ->
            appContext.mainQueue.launch {
              ref.userVolume = volume
              ref.volume = volume
            }
          }

      Property("currentTime")
          .get { ref: VideoPlayer ->
            // TODO: we shouldn't block the thread, but there are no events for the player position change,
            //  so we can't update the currentTime in a non-blocking way like the other properties.
            //  Until we think of something better we can temporarily do it this way
            runBlocking(appContext.mainQueue.coroutineContext) {
              ref.player.currentPosition / 1000f
            }
          }
          .set { ref: VideoPlayer, currentTime: Double ->
            appContext.mainQueue.launch {
              ref.player.seekTo((currentTime * 1000).toLong())
            }
          }

      Property("playbackRate")
          .get { ref: VideoPlayer ->
            ref.playbackParameters.speed
          }
          .set { ref: VideoPlayer, playbackRate: Float ->
            appContext.mainQueue.launch {
              val pitch = if (ref.preservesPitch) 1f else playbackRate
              ref.playbackParameters = PlaybackParameters(playbackRate, pitch)
            }
          }

      Property("preservesPitch")
          .get { ref: VideoPlayer ->
            ref.preservesPitch
          }
          .set { ref: VideoPlayer, preservesPitch: Boolean ->
            appContext.mainQueue.launch {
              ref.preservesPitch = preservesPitch
            }
          }

      Property("staysActiveInBackground")
          .get { ref: VideoPlayer ->
            ref.staysActiveInBackground
          }
          .set { ref: VideoPlayer, staysActive: Boolean ->
            ref.staysActiveInBackground = staysActive
          }

      Property("loop")
          .get { ref: VideoPlayer ->
            ref.player.repeatMode == REPEAT_MODE_ONE
          }
          .set { ref: VideoPlayer, loop: Boolean ->
            appContext.mainQueue.launch {
              ref.player.repeatMode = if (loop) {
                REPEAT_MODE_ONE
              } else {
                REPEAT_MODE_OFF
              }
            }
          }

      Function("play") { ref: VideoPlayer ->
        appContext.mainQueue.launch {
          ref.player.play()
        }
      }

      Function("deallocate") { ref: VideoPlayer ->
        ref.deallocate()
      }

      Function("pause") { ref: VideoPlayer ->
        appContext.mainQueue.launch {
          ref.player.pause()
        }
      }

      Function("replace") { ref: VideoPlayer, source: Either<String, VideoSource> ->
        val videoSource = if (source.`is`(VideoSource::class)) {
          source.get(VideoSource::class)
        } else {
          VideoSource(source.get(String::class))
        }

        appContext.mainQueue.launch {
          ref.player.setMediaItem(videoSource.toMediaItem())
        }
      }

      Function( "getAspectRatio") { ref: VideoPlayer ->
        ref.aspectRatio
      }

      Function("seekBy") { ref: VideoPlayer, seekTime: Double ->
        appContext.mainQueue.launch {
          val seekPos = ref.player.currentPosition + (seekTime * 1000).toLong()
          ref.player.seekTo(seekPos)
        }
      }

      Function("replay") { ref: VideoPlayer ->
        appContext.mainQueue.launch {
          ref.player.seekTo(0)
          ref.player.play()
        }
      }
    }

    OnActivityEntersForeground {
      VideoManager.onAppForegrounded()
    }

    OnActivityEntersBackground {
      VideoManager.onAppBackgrounded()
    }
  }
}

@Suppress("FunctionName")
private inline fun <reified T : View, reified PropType, reified CustomValueType> ViewDefinitionBuilder<T>.PropGroup(
    vararg props: Pair<String, CustomValueType>,
    noinline body: (view: T, value: CustomValueType, prop: PropType) -> Unit
) {
  for ((name, value) in props) {
    Prop<T, PropType>(name) { view, prop -> body(view, value, prop) }
  }
}
