import { requireNativeModule } from 'expo-modules-core';
import { Platform } from "react-native";


const module = Platform.OS === "android" ? requireNativeModule('ExpoVideo') : undefined;
export default module;
