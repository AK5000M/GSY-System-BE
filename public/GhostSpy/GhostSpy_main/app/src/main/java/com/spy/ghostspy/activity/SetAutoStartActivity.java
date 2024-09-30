package com.spy.ghostspy.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
        if (manufacturer.equals("xiaomi") && Integer.parseInt(Build.VERSION.RELEASE) >= 12) {
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", getPackageName());
            try {
                startActivity(intent);
                Common.getInstance().setAutostartEnable(true);
            } catch (ActivityNotFoundException e1){
                Common.getInstance().setAutostartEnable(false);
                Intent intent_perm = new Intent(this, PermissionSetActivity.class);
                intent_perm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent_perm);
                finish();
            }

        } else {
            Intent intent = new Intent(this, PermissionSetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
    }
}