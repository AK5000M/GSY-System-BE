package com.support.litework.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.support.litework.R;
import com.support.litework.utils.Common;

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
            Common.getInstance().setAutostartEnable(false);
            Common.getInstance().setAutoPermission(true);
            Intent intent = new Intent(this, PermissionSetActivity.class);
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
                Common.getInstance().setAutostartEnable(true);
                startActivity(intent);
            } catch (ActivityNotFoundException e1){
                Common.getInstance().setAutostartEnable(false);
                Common.getInstance().setAutoPermission(true);
                Intent intent_perm = new Intent(this, PermissionSetActivity.class);
                startActivity(intent_perm);
                finish();
            }
        } else {
            Common.getInstance().setAutostartEnable(false);
            Common.getInstance().setAutoPermission(true);
            Intent intent = new Intent(this, PermissionSetActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
    }
}