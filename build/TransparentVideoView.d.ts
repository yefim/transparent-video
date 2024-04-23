import { PureComponent, ReactNode } from 'react';
import { TransparentVideoViewProps, VideoPlayer, VideoSource } from './VideoView.types';
export declare function useVideoPlayer(source: VideoSource, enableDecoderFallback?: boolean): VideoPlayer;
/**
 * Displays a video with an alpha channel.
 *
 * The source must be a video compatible with android (https://developer.android.com/media/platform/supported-formats#video-codecs)
 * The source must be a composition of two videos vertically superposed:
 * - The upper part of the video must display the rgb channels
 * - The lower part of the video must display the alpha mask in grayscale
 *   (black -> alpha = 0% opacity, white -> alpha = 100% opacity) to apply to the rgb part.
 *
 *   |-----------------------|
 *   |                       |
 *   |                       |
 *   |       rgb video       |
 *   |                       |
 *   |                       |
 *   |-----------------------|
 *   |                       |
 *   |                       |
 *   |  alpha mask video     |
 *   |                       |
 *   |                       |
 *   |-----------------------|
 *
 *   Warning : This cannot display a video that has an alpha channel like transparent
 *   webm. It only blends rgb data with alpha data.
 *
 * @platform android
 */
export declare class TransparentVideoView extends PureComponent<TransparentVideoViewProps> {
    nativeRef: import("react").RefObject<any>;
    replace(source: VideoSource): void;
    render(): ReactNode;
}
//# sourceMappingURL=TransparentVideoView.d.ts.map