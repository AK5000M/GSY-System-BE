package com.support.litework.utils;

import android.os.Build;

import java.util.Arrays;
import android.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DeviceUtils {
    private static final String ALGORITHM = "AES";
    private static final String FIXED_KEY = "mysecretkey12345";
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

    public static SecretKey getFixedSecretKey() {
        return new SecretKeySpec(FIXED_KEY.getBytes(), ALGORITHM);
    }

    // Decrypt the text
    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes, "UTF-8");
    }
}
