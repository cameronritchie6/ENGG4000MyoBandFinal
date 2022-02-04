package com.cmorrell.myobandcompanionapp;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class BluetoothLeService extends Service {

    public static final String ACTION_GATT_CONNECTED = "com.cmorrell.myobandcompanionapp.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "com.cmorrell.myobandcompanionapp.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "com.cmorrell.myobandcompanionapp.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_SENT = "com.cmorrell.myobandcompanionapp.ACTION_DATA_SENT";
    public static final String ACTION_DATA_AVAILABLE = "com.cmorrell.myobandcompanionapp.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "com.cmorrell.myobandcompanionapp.EXTRA_DATA";
    public static final int SELECT_DEVICE_REQUEST_CODE = 1;    // request code for BLE bonding
    public static final int REQUEST_ENABLE_BT = 2;  // request code to enable Bluetooth
    public static final int PERMISSION_REQUEST_CODE = 3;    // request code for background location permission

    //    private static final String UART_SERVICE_UUID = "B2B9D06E-60D4-4511-91A8-20E2E77CFA4B";
    private static final String UART_SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String RX_CHARACTERISTIC_UUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String TX_CHARACTERISTIC_UUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    //private static final String CHARACTERISTIC_UUID = "90B6107B-0AD4-45DC-AD35-9C1F84811ABF";
    private static final String CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";

//    public static final int STATE_DISCONNECTED = 0;
//    public static final int STATE_CONNECTED = 2;

    private boolean connected;    // current connection state

    private final Binder binder = new LocalBinder();
    public static final String LOG_TAG = "BluetoothLeService";
    private BluetoothAdapter bluetoothAdapter;
    private String deviceAddress;    // address of connected BLE device
    private BluetoothDevice myoDevice;
    private BluetoothGatt bluetoothGatt;
    private MainActivity main;
    private List<BluetoothGattService> services;


    public void setMain(MainActivity main) {
        this.main = main;
    }


    public BluetoothDevice getMyoDevice() {
        return myoDevice;
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Successfully connected to the GATT Server
                connected = true;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                // Attempts to discover services after successful connection
                if (checkForBTPermissions()) {
                    bluetoothGatt.discoverServices();
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Disconnected from the GATT Server
                connected = false;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.e(LOG_TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_SENT, characteristic);
            } else if (status == BluetoothGatt.GATT_FAILURE) {
                Log.e(LOG_TAG, "Bluetooth GATT failure when sending data.");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
//            MyoReceiver myoReceiver = main.getMyoReceiver();
            MyoReceiver myoReceiver = MainActivity.myoReceiver;
            registerReceiver(myoReceiver, makeGattUpdateIntentFilter());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (MainActivity.myoReceiver != null)
            unregisterReceiver(MainActivity.myoReceiver);
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

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(main, permission) == PackageManager.PERMISSION_GRANTED;
    }


    @SuppressLint("MissingPermission")
    public boolean checkForBTPermissions() {

        boolean noPermissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            noPermissions = !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ||
                    !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !hasPermission(Manifest.permission.BLUETOOTH_CONNECT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            noPermissions = !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ||
                    !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            noPermissions = !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (noPermissions) {
            // Need to request permission
            String[] permissions;
            String message;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_CONNECT};
                message = "Background location is required for Bluetooth connections on this device. Please select \"Allow all the time\"";
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions = new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION};
                message = "Background location is required for Bluetooth connections on this device. Please select \"Allow all the time\"";
            } else {
                permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                message = "Fine location is required for Bluetooth connections on this device. Please select \"Allow while using\"";
            }

            new AlertDialog.Builder(main)
                    .setTitle("Location Permission")
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        // Prompt the user once explanation has been shown
//                                main.requestPermissions(permissions, PERMISSION_REQUEST_CODE);
                        ActivityCompat.requestPermissions(main, permissions, PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("No thanks", (dialog, which) -> {
                        // Close dialog
                        dialog.dismiss();
                    })
                    .create()
                    .show();
            return false;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // Ask user to enable Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            main.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.e(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (connected) {
            close();
        }


        // Check that user has the required Bluetooth permissions enabled
        if (checkForBTPermissions()) {
            try {
                final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                // Connect to the GATT server on the device
                bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                return true;
            } catch (IllegalArgumentException exception) {
                Log.e(LOG_TAG, "Device not found with provided address.");
                return false;
            }
        }
        return false;
    }

    /**
     * Broadcasts update to send information from service to activity.
     * @param action - String that describes the action that has occurred.
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Broadcast update for broadcast receiver
     *
     * @param action         action to broadcast
     * @param characteristic characteristic whose data will be read or sent
     */
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();

        if (data != null) {
            intent.putExtra(EXTRA_DATA, data);
        }

        sendBroadcast(intent);
    }


    public void setDeviceAddress(final String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public boolean getConnected() {
        return connected;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_SENT);
        intentFilter.setPriority(500);
        return intentFilter;
    }

    /**
     * Checks for Bluetooth devices that are paired to the device, but not bonded.
     * If a device contains the name "EasyVR", a BLE connection between the two
     * devices is attempted.
     *
     * @return device that contains the name "EasyVR" if it is found, otherwise return null
     */
    @SuppressLint("MissingPermission")
    public boolean checkForPairedDevices() {
        if (checkForBTPermissions()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    if (deviceName.contains("UART")) {
                        close();    // Prevent multiple Bluetooth connections
                        if (connect(device.getAddress())) {
                            // Successful pairing
                            myoDevice = device;
                            deviceAddress = device.getAddress();
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

//    public List<BluetoothGattService> getSupportedGattServices() {
//        if (bluetoothGatt == null) return null;
//        services = bluetoothGatt.getServices();
////        services.get(1).
//        return bluetoothGatt.getServices();
//    }

    @SuppressLint("MissingPermission")
    public void setCharacteristicNotification() {
        if (bluetoothGatt == null) {
            Log.e(LOG_TAG, "BluetoothGATT not initialized");
            return;
        }

        // Check if the service is available on the device
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(UART_SERVICE_UUID));
        if (service == null) {
            Log.e(LOG_TAG, "Custom BLE service not found");
            return;
        }

        if (checkForBTPermissions()) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(TX_CHARACTERISTIC_UUID));
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));

            bluetoothGatt.setCharacteristicNotification(characteristic, true);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CONFIG_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
        // Get the read characteristic from the service

    }

    @SuppressLint("MissingPermission")
    public void write(String value) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        // Check if service is available on device
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(UART_SERVICE_UUID));
        if (service == null) {
            Log.w(LOG_TAG, "ESP32 service not found");
            return;
        }

        if (checkForBTPermissions()) {
            // Get read characteristic
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(RX_CHARACTERISTIC_UUID));
//        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
            String data = value + "\n";
            characteristic.setValue(data.getBytes());

            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void pairDevice() {

        // Create device filter
        BluetoothLeDeviceFilter deviceFilter = new BluetoothLeDeviceFilter.Builder()
                // Match only Bluetooth devices whose name matches the pattern
                .setNamePattern(Pattern.compile("UART")).build();


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
                Toast.makeText(main, "Failed to bond.", Toast.LENGTH_SHORT).show();
            }
        }, null);
    }



    /**
     * Close the BluetoothGatt connection.
     */
    @SuppressLint("MissingPermission")
    private void close() {
        if (bluetoothGatt == null)
            return;
        if (checkForBTPermissions()) {
            bluetoothGatt.close();
        }
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