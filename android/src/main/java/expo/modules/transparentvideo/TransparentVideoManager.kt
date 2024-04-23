package expo.modules.transparentvideo

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi

// Helper class used to keep track of all existing VideoViews and VideoPlayers
@OptIn(UnstableApi::class)
object TransparentVideoManager {
  const val INTENT_PLAYER_KEY = "player_uuid"

  // Used for sharing videoViews between VideoView and FullscreenPlayerActivity
  private var videoViews = mutableMapOf<String, TransparentVideoView>()

  // Keeps track of all existing VideoPlayers, and whether they are attached to a VideoView
  private var videoPlayersToVideoViews = mutableMapOf<VideoPlayer, ArrayList<TransparentVideoView>>()

  fun registerVideoView(videoView: TransparentVideoView) {
    videoViews[videoView.id] = videoView
  }

  fun getVideoView(id: String): TransparentVideoView {
    return videoViews[id] ?: throw VideoViewNotFoundException(id)
  }

  fun unregisterVideoView(videoView: TransparentVideoView) {
    videoViews.remove(videoView.id)
  }

  fun registerVideoPlayer(videoPlayer: VideoPlayer) {
    videoPlayersToVideoViews[videoPlayer] = videoPlayersToVideoViews[videoPlayer] ?: arrayListOf()
  }

  fun unregisterVideoPlayer(videoPlayer: VideoPlayer) {
    videoPlayersToVideoViews.remove(videoPlayer)
  }

  fun onVideoPlayerAttachedToView(videoPlayer: VideoPlayer, videoView: TransparentVideoView) {
    if (videoPlayersToVideoViews[videoPlayer]?.contains(videoView) == true) {
      return
    }
    videoPlayersToVideoViews[videoPlayer]?.add(videoView) ?: run {
      videoPlayersToVideoViews[videoPlayer] = arrayListOf(videoView)
    }
  }

  fun onVideoPlayerDetachedFromView(videoPlayer: VideoPlayer, videoView: TransparentVideoView) {
    videoPlayersToVideoViews[videoPlayer]?.remove(videoView)
  }

  fun onAppForegrounded() {
    // TODO: Left here for future use
  }

  fun onAppBackgrounded() {
    for (videoView in videoViews.values) {
      videoView.videoPlayer?.player?.pause()
    }
  }
}
