package com.cmorrell.myobandcompanionapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private BluetoothLeService bluetoothLeService;
    private final MyoReceiver myoReceiver = new MyoReceiver();
    private static final String LOG_TAG = "MainActivity";


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            bluetoothLeService.setMain(MainActivity.this);
            if (bluetoothLeService != null) {
                // Call functions on service to check connection and connect devices
                if (!bluetoothLeService.initialize()) {
                    // BluetoothAdapter not found
                    Log.e(BluetoothLeService.LOG_TAG, "Unable to initialize Bluetooth");
                    finish();   // destroy activity
                }
                // Perform device connection
//                bluetoothLeService.connect(bluetoothLeService.getDeviceAddress());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothLeService = null;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == BluetoothLeService.SELECT_DEVICE_REQUEST_CODE && data != null) {
            ScanResult scanResult = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            BluetoothDevice device = scanResult.getDevice();

            if (device != null) {
                // Bond with device
                if (device.createBond()) {
                    Log.d(LOG_TAG, "Successful bond");
                    bluetoothLeService.setDeviceAddress(device.getAddress());
                    // NEED TO FIGURE OUT
                    bluetoothLeService.connect(bluetoothLeService.getDeviceAddress());
                }
                else
                    Log.e(LOG_TAG, "Failed to bond");
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        bluetoothLeService.initialize();
        // Bind Bluetooth service to activity
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);


    }


    public MyoReceiver getMyoReceiver() {
        return myoReceiver;
    }

    public BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }

    //    @Override
//    public void onUserInteraction() {
//        super.onUserInteraction();
//        Log.d("MAIN", "You touched the screen.");
//        UnityPlayer.UnitySendMessage("Canvas", "ShowMessage", "Hello Unity, this message is from Android!");
//    }
}