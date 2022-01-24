package com.cmorrell.myobandcompanionapp;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.companion.AssociationRequest;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.regex.Pattern;

public class BluetoothLeService extends Service {

    public static final String ACTION_GATT_CONNECTED = "com.cmorrell.myobandcompanionapp.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.cmorrell.myobandcompanionapp.ACTION_GATT_DISCONNECTED";
    public static final int SELECT_DEVICE_REQUEST_CODE = 1;    // request code for BLE bonding

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;

    private final Binder binder = new LocalBinder();
    public static final String LOG_TAG = "BluetoothLeService";
    private BluetoothAdapter bluetoothAdapter;
    private static String deviceAddress;
    private BluetoothGatt bluetoothGatt;
    private MainActivity main;


    public void setMain(MainActivity main) {
        this.main = main;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Successfully connected to the GATT Server
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from the GATT Server
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            MyoReceiver myoReceiver = main.getMyoReceiver();
            registerReceiver(myoReceiver, makeGattUpdateIntentFilter());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (main.getMyoReceiver() != null)
            unregisterReceiver(main.getMyoReceiver());
        close();
    }


    /**
     * Setup BluetoothAdapter for Bluetooth connection.
     * @return false if BluetoothAdapter could not be found, otherwise true.
     */
    public boolean initialize() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }


    private void checkForBTPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            main.checkLocationPermission(Manifest.permission.BLUETOOTH_CONNECT);
//            main.checkForPermission(Manifest.permission.BLUETOOTH_ADVERTISE);
//            main.checkForPermission(Manifest.permission.BLUETOOTH_SCAN);
        } else {
//            main.checkForPermission(Manifest.permission.BLUETOOTH);
//            main.checkForPermission(Manifest.permission.ACCESS_FINE_LOCATION);
//            main.checkForPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                main.checkForPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }



    }

    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.e(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Check that user has the required Bluetooth permissions enabled
//        checkForBTPermissions();

        main.checkLocationPermission();


        try {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // Connect to the GATT server on the device
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
            bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
//            else
//                bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback);
            return true;
        } catch (IllegalArgumentException exception) {
            Log.e(LOG_TAG, "Device not found with provided address.");
            return false;
        }
    }

    /**
     * Broadcasts update to send information from service to activity.
     * @param action - String that describes the action that has occurred.
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }


    public void setDeviceAddress(final String deviceAddress) {
        BluetoothLeService.deviceAddress = deviceAddress;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        return intentFilter;
    }

    public void pairDevice() {

        // Create device filter
        BluetoothLeDeviceFilter deviceFilter = new BluetoothLeDeviceFilter.Builder()
                // Match only Bluetooth devices whose name matches the pattern
//                .setNamePattern(Pattern.compile("Myo")).build();
        .setNamePattern(Pattern.compile("hello")).build();

        // Set a DeviceFilter to an AssociationRequest so the device manager can determine what type of device to seek.
        AssociationRequest pairingRequest = new AssociationRequest.Builder()
                // Find only devices that match this filter
                .addDeviceFilter(deviceFilter)
                // Stop scanning as soon as one device matching the filter is found
                .setSingleDevice(false).build();


        CompanionDeviceManager deviceManager = (CompanionDeviceManager)
                main.getSystemService(Context.COMPANION_DEVICE_SERVICE);

        deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
            @Override
            public void onDeviceFound(IntentSender chooserLauncher) {
                try {
                    main.startIntentSenderForResult(
                            chooserLauncher, SELECT_DEVICE_REQUEST_CODE, null, 0,
                            0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Log.e(LOG_TAG, "Failed to send intent");
                }
            }

            @Override
            public void onFailure(CharSequence error) {
                // Handle failure
                Toast.makeText(main, "Could not find compatible Myoband device.", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    /**
     * Close the BluetoothGatt connection.
     */
    private void close() {
        if (bluetoothGatt == null)
            return;
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}