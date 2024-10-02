package com.support.litework.utils;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MyPermissions {
    public static final String[] ALL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public static final String[] ALL_phone = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.POST_NOTIFICATIONS
    };

    public static final String[] MEDIA_PERMISSION = {
            Manifest.permission.READ_MEDIA_IMAGES
    };

    // Method to check if all permissions are granted
    public static boolean hasPermissions(Context context, String permission) {
        if (context != null) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
