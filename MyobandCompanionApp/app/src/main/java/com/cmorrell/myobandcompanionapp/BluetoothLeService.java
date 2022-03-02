package com.cmorrell.myobandcompanionapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.companion.AssociationRequest;
import android.companion.BluetoothLeDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

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

    //    private static final String UART_SERVICE_UUID = "B2B9D06E-60D4-4511-91A8-20E2E77CFA4B";
    private static final String GAMEPAD_SERVICE_UUID = "4A981812-1CC4-E7C1-C757-F1267DD021E8";
    private static final String SERVICE_UUID = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String RX_CHARACTERISTIC_UUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String TX_CHARACTERISTIC_UUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String CONFIG_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private static final String defaultDeviceName = "G";

    public static final String TIME = "TIME";

    private boolean connected;    // current connection state

    private final Binder binder = new LocalBinder();
    public static final String LOG_TAG = "BluetoothLeService";
    private BluetoothAdapter bluetoothAdapter;
//    private String deviceAddress;    // address of connected BLE device
    private BluetoothDevice myoDevice;
    private BluetoothGatt bluetoothGatt;
    private MainActivity main;
    private List<BluetoothGattService> services;
    private BluetoothHidDevice bluetoothHidDevice;


    public void setMain(MainActivity main) {
        this.main = main;
    }


    public BluetoothDevice getMyoDevice() {
        return myoDevice;
    }

    public void setMyoDevice(BluetoothDevice myoDevice) {
        this.myoDevice = myoDevice;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
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
                if (main.checkForBTPermissions()) {
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

    private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHidDevice = (BluetoothHidDevice) proxy;
            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                bluetoothHidDevice = null;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            MyoReceiver myoReceiver = MainActivity.myoReceiver;
            registerReceiver(myoReceiver, makeMyoReceiverFilter());
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
        if (main.checkForBTPermissions()) {
            try {
                final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                // Connect to the GATT server on the device
//                bluetoothGatt = device.connectGatt(this, true, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    bluetoothAdapter.getProfileProxy(main, profileListener, BluetoothProfile.HID_DEVICE);
                    if (bluetoothHidDevice != null) {
                        bluetoothHidDevice.connect(device);
                    }
                }
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


//    public void setDeviceAddress(final String deviceAddress) {
//        this.deviceAddress = deviceAddress;
//    }

    public boolean getConnected() {
        return connected;
    }

    public String getDeviceAddress() {
        return myoDevice.getAddress();
    }

    private static IntentFilter makeMyoReceiverFilter() {
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
        if (main.checkForBTPermissions()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    if (deviceName.contains(defaultDeviceName)) {
                        close();    // prevent multiple Bluetooth connections
                        if (connect(device.getAddress())) {
                            // Successful pairing
                            myoDevice = device;
//                            deviceAddress = device.getAddress();
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
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
        if (service == null) {
            Log.e(LOG_TAG, "Custom BLE service not found");
            return;
        }

        if (main.checkForBTPermissions()) {
            // Get the read characteristic from the service
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(TX_CHARACTERISTIC_UUID));

            bluetoothGatt.setCharacteristicNotification(characteristic, true);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CONFIG_UUID));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }


    }

    @SuppressLint("MissingPermission")
    public void write(String value) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        // Check if service is available on device
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
        if (service == null) {
            Log.w(LOG_TAG, "ESP32 service not found");
            return;
        }

        if (main.checkForBTPermissions()) {
            // Get read characteristic
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(RX_CHARACTERISTIC_UUID));
            String data = value + "\n";
            characteristic.setValue(data.getBytes());

            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void pairDevice() {

        // Create device filter based on UUID
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(GAMEPAD_SERVICE_UUID))
                .build();

        BluetoothLeDeviceFilter deviceFilter = new BluetoothLeDeviceFilter.Builder()
                .setNamePattern(Pattern.compile(defaultDeviceName))
                .build();
//                .setScanFilter(scanFilter).build();


        // Set a DeviceFilter to an AssociationRequest so the device manager can determine what type of device to seek.
        AssociationRequest pairingRequest = new AssociationRequest.Builder()
                // Find only devices that match this filter
                .addDeviceFilter(deviceFilter)
                .setSingleDevice(false).build();


        CompanionDeviceManager deviceManager = (CompanionDeviceManager)
                main.getSystemService(Context.COMPANION_DEVICE_SERVICE);

        deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
            @Override
            public void onDeviceFound(IntentSender chooserLauncher) {
                try {
                    main.startIntentSenderForResult(
                            chooserLauncher, MainActivity.SELECT_DEVICE_REQUEST_CODE, null, 0,
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
        if (main.checkForBTPermissions()) {
            bluetoothGatt.close();
            // https://developer.android.com/guide/topics/connectivity/bluetooth/profiles
//            bluetoothAdapter.closeProfileProxy(0, bluetoothHidDevice);
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