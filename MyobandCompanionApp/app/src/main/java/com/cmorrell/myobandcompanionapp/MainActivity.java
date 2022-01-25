package com.cmorrell.myobandcompanionapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private BluetoothLeService bluetoothLeService;
    public static MyoReceiver myoReceiver = new MyoReceiver();
//    private static final int PERMISSION_REQUEST_CODE = 1;
//    public static final int PERMISSION_CODE_FINE = 2;  // Request code for fine location permission
//    public static final int PERMISSION_CODE_BACKGROUND = 3;    // Request code for background location permission
//    public static final int SELECT_DEVICE_REQUEST_CODE = 4;    // Request code for bonding device
    private static final String LOG_TAG = "MainActivity";

//    public static final String address = "CD:46:77:23:DE:11";

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
//    private ActivityResultLauncher<String[]> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
//
//
//                if (isGranted.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                    // Permission is granted. Continue the action or workflow in your
//                    // app.
//                    Log.d(LOG_TAG, "They granted the permission!");
//                } else {
//                    // Explain to the user that the feature is unavailable because the
//                    // features requires a permission that the user has denied. At the
//                    // same time, respect the user's decision. Don't link to system
//                    // settings in an effort to convince the user to change their
//                    // decision.
//                    Toast.makeText(this, "Cannot find Bluetooth device without proper permissions.", Toast.LENGTH_SHORT).show();
//                }
//            });


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == BluetoothLeService.PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0) || !Arrays.stream(grantResults).allMatch(n -> n == PackageManager.PERMISSION_GRANTED)) {
                // All permissions granted
                Toast.makeText(this, "You must grant all permissions", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

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
                String address = device.getAddress();
                device.createBond();
                bluetoothLeService.setDeviceAddress(address);
                // PROBABLY WRONG RIGHT HERE
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_bondingFragment_to_menuFragment);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind Bluetooth service to activity
        bluetoothLeService = new BluetoothLeService();
//        bluetoothLeService.setMain(MainActivity.this);
//        myoReceiver.setMain(MainActivity.this);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set MainActivity context for broadcast receiver and service
        bluetoothLeService.setMain(MainActivity.this);
        myoReceiver.setMain(MainActivity.this);

    }



//    public MyoReceiver getMyoReceiver() {
//        return myoReceiver;
//    }

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