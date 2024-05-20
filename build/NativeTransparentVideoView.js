import { requireNativeViewManager } from 'expo-modules-core';
import { Platform } from "react-native";
const NativeView = Platform.OS === "android" ? requireNativeViewManager('ExpoTransparentVideo') : (_) => null;
export default NativeView;
//# sourceMappingURL=NativeTransparentVideoView.js.map