package com.cmorrell.myobandcompanionapp;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.unity3d.player.UnityPlayer;

import java.util.Arrays;
import java.util.function.ToDoubleBiFunction;

public class MainActivity extends AppCompatActivity {

    private BluetoothLeService bluetoothLeService;
    public static MyoReceiver myoReceiver = new MyoReceiver();
    //    private static final int PERMISSION_REQUEST_CODE = 1;
//    public static final int PERMISSION_CODE_FINE = 2;  // Request code for fine location permission
//    public static final int PERMISSION_CODE_BACKGROUND = 3;    // Request code for background location permission
//    public static final int SELECT_DEVICE_REQUEST_CODE = 4;    // Request code for bonding device
    private static final String LOG_TAG = "MainActivity";
    public static final String ACTION_QUIT_UNITY = "com.cmorrell.myobandcompanionapp.ACTION_QUIT_UNITY";

    public UnityPlayer unityPlayer;


    /*
    Todo: Fix BLE permissions
     Todo: Fix the bottom navigation bar disappearing once returning to main menu from Unity
     Todo: Fix orientation change (turning screen sideways)
     Todo: Possibly change scan filter to find service UUID instead of device name.
    */



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

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == BluetoothLeService.SELECT_DEVICE_REQUEST_CODE && data != null) {
            ScanResult scanResult = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            BluetoothDevice device = scanResult.getDevice();

            if (device != null && bluetoothLeService.checkForBTPermissions()) {
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
//        bluetoothLeService.setMain(MainActivity.this);
//        myoReceiver.setMain(MainActivity.this);
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



//    public MyoReceiver getMyoReceiver() {
//        return myoReceiver;
//    }

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