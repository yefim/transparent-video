import { ViewProps } from 'react-native';
import { VideoViewProps } from 'expo-video/build/VideoView.types';


export type TransparentVideoViewProps = Omit<
  VideoViewProps,
  | 'onPictureInPictureStart'
  | 'onPictureInPictureStop'
  | 'allowsPictureInPicture'
  | 'startsPictureInPictureAutomatically'
  | 'allowsFullscreen'
  | 'nativeControls'
  | 'contentFit'
> & { videoAspectRatio?: number };
