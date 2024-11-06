package com.support.litework.utils;

import android.os.Build;

import java.util.Arrays;
import java.util.List;

public class DeviceUtils {
    public static String getDeviceArchitecture() {
        List<String> supportedAbis = Arrays.asList(Build.SUPPORTED_ABIS);

        if (supportedAbis.contains("arm64-v8a")) {
            return "ARM64";
        } else if (supportedAbis.contains("armeabi-v7a")) {
            return "ARM";
        } else if (supportedAbis.contains("x86_64")) {
            return "x86_64";
        } else if (supportedAbis.contains("x86")) {
            return "x86";
        } else {
            return "";
        }
    }
}
