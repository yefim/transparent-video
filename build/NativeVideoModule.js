import { requireNativeModule } from 'expo-modules-core';
import { Platform } from "react-native";
const module = Platform.OS === "android" ? requireNativeModule('TransparentExpoVideo') : undefined;
export default module;
//# sourceMappingURL=NativeVideoModule.js.map