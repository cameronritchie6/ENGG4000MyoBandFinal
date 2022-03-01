package com.cmorrell.myobandcompanionapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.ToDoubleBiFunction;

public class MainActivity extends AppCompatActivity {

    public static final int SELECT_DEVICE_REQUEST_CODE = 1;    // request code for BLE bonding
    public static final int REQUEST_ENABLE_BT = 2;  // request code to enable Bluetooth


    private BluetoothLeService bluetoothLeService;
    public static MyoReceiver myoReceiver = new MyoReceiver();

    private static final String LOG_TAG = "MainActivity";
    public static final String ACTION_QUIT_UNITY = "com.cmorrell.myobandcompanionapp.ACTION_QUIT_UNITY";

    public UnityPlayer unityPlayer;



    /*
    Todo: Saving data in a database
     Todo: Fix the bottom navigation bar disappearing once returning to main menu from Unity
     Todo: Fix orientation change (turning screen sideways)
     Todo: Look at putting calibration screen progress bars on separate thread
    */


    ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {

                        String[] permissions = getRequiredPermissions();
                        for (String permission : permissions) {
                            Boolean permissionGranted = result.getOrDefault(permission, false);
                            if (permissionGranted != null && !permissionGranted) {
                                String message = "Location permissions are required for Bluetooth connections on" +
                                        " this device. Please navigate to location permissions in settings and " +
                                        "select \"Allow while using.\"";
                                new AlertDialog.Builder(this)
                                        .setTitle("Location Permission")
                                        .setMessage(message)
                                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                        })
                                        .create()
                                        .show();
                            }
                        }
                    }
            );


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

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == SELECT_DEVICE_REQUEST_CODE && data != null) {
            ScanResult scanResult = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            BluetoothDevice device = scanResult.getDevice();
            ParcelUuid[] uuids = device.getUuids();

            if (device != null && checkForBTPermissions()) {
                // Bond with device
                String address = device.getAddress();
                device.createBond();
                bluetoothLeService.setDeviceAddress(address);
                // Navigate to main menu
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_global_menuFragment);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);

    }


    /**
     * Unity method
     *
     * @param message message from Unity
     */
    public void quitUnity(String message) {
        Log.d("UNITY", message);

        Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).navigate(R.id.action_global_menuFragment);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Bind Bluetooth service to activity
        bluetoothLeService = new BluetoothLeService();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        startService(gattServiceIntent);
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        unityPlayer = new UnityPlayer(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set MainActivity context for broadcast receiver and service
        bluetoothLeService.setMain(MainActivity.this);
        myoReceiver.setMain(MainActivity.this);

    }

    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT};

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        } else {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        }
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public boolean checkForBTPermissions() {

        String[] permissions = getRequiredPermissions();


        // Check for all appropriate permissions
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                String message = "Location permissions are required for Bluetooth connections on this device. Please select \"Allow while using.\"";



                new AlertDialog.Builder(this)
                        .setTitle("Location Permission")
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            // Prompt the user once explanation has been shown
                            locationPermissionRequest.launch(permissions);
                        })
                        .setNegativeButton("No thanks", (dialog, which) -> {
                            // Close dialog
                            dialog.dismiss();
                        })
                        .create()
                        .show();
                return false;
            }

        }

        if (!bluetoothLeService.getBluetoothAdapter().isEnabled()) {
            // Ask user to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }



    public BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }

//        @Override
//    public void onUserInteraction() {
//        super.onUserInteraction();
//        Log.d("MAIN", "You touched the screen.");
//        UnityPlayer.UnitySendMessage("Canvas", "ShowMessage", "Hello Unity, this message is from Android!");
//    }
}