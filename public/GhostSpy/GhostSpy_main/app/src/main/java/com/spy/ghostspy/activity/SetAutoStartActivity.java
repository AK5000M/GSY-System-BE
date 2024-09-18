package com.spy.ghostspy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.spy.ghostspy.R;
import com.spy.ghostspy.utils.Common;

public class SetAutoStartActivity extends AppCompatActivity {
    Boolean isEnable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_auto_start);
        setReady();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Common.getInstance().getAutosel()) {
            Intent intent = new Intent(this, PermissionSetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setReady() {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        if (manufacturer.equals("xiaomi")) {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", getPackageName());
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, PermissionSetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}