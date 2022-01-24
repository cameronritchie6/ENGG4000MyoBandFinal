package com.cmorrell.myobandcompanionapp;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private BluetoothLeService bluetoothLeService;
    private final MyoReceiver myoReceiver = new MyoReceiver();
    private static final String LOG_TAG = "MainActivity";
    public static final String address = "CD:46:77:23:DE:11";

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    Log.d(LOG_TAG, "They granted the permission!");
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                    Toast.makeText(this, "Cannot find Bluetooth device without proper permissions.", Toast.LENGTH_SHORT).show();
                }
            });


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
//                    bluetoothLeService.connect(bluetoothLeService.getDeviceAddress());
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


    // NEED TO CHECK FOR BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT, AND BLUETOOTH_SCAN
    // https://developer.android.com/guide/topics/connectivity/bluetooth/permissions
    // https://developer.android.com/training/permissions/requesting#allow-system-manage-request-code
    // NEED TO FIX, IT ISN'T REQUESTING BUT IT SAYS DENIED
    public boolean checkForPermission(String permission) {
        if (ContextCompat.checkSelfPermission(
                MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else if (shouldShowRequestPermissionRationale(permission)) {
            // 1. Instantiate an AlertDialog builder with its constructor
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setMessage("This is the alert message CBM has set.")
                    .setTitle("Title for AlertDialog set by CBM");
            builder.setPositiveButton("OK", (dialog, which) -> {
                requestPermissionLauncher.launch(permission);
            });
            builder.setNegativeButton("I do not give permission", (dialog, which) -> {
            });

            // 3. Get the AlertDialog from its builder
            AlertDialog dialog = builder.create();

            dialog.show();
        }
        return true;
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