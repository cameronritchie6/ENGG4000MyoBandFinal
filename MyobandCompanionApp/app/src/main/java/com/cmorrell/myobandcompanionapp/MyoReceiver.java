package com.cmorrell.myobandcompanionapp;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

public class MyoReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "MyoReceiver";
    private MainActivity main;

    public void setMain(MainActivity main) {
        this.main = main;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            Log.d(LOG_TAG, "Device has been connected.");
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            Log.d(LOG_TAG, "Device has been disconnected.");
        } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//            List<BluetoothGattService> services = main.getBluetoothLeService().getSupportedGattServices();
//            System.out.print(services);
            Log.d(LOG_TAG, "GATT services discovered.");
            main.getBluetoothLeService().setCharacteristicNotification();
        }
    }
}