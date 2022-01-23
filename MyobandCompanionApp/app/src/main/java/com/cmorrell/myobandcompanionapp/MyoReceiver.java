package com.cmorrell.myobandcompanionapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyoReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "MyoReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            Log.d(LOG_TAG, "Device has been connected.");
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            Log.d(LOG_TAG, "Device has been disconnected.");
        }
    }
}