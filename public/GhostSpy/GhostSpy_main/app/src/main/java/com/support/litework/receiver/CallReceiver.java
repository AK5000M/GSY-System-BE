package com.support.litework.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(intent.getAction())) {

        }
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        // Incoming call
                        Log.d("CallReceiver", "Incoming call from: " + incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // Outgoing call or Call answered
                        Log.d("CallReceiver", "Outgoing call to: " + incomingNumber);
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        Log.d("CallReceiver", "Call ended");
                        break;
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }
}
