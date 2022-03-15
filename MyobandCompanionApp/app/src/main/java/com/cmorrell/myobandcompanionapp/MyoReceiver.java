package com.cmorrell.myobandcompanionapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.nio.charset.StandardCharsets;

public class MyoReceiver extends BroadcastReceiver {

    private BondingFragment bondingFragment;
    private CalibrationFragment calibrationFragment;
    private BLETestFragment bleTestFragment;
    private ConnectionFragment connectionFragment;
    private UnityFragment unityFragment;


    private static final String LOG_TAG = "MyoReceiver";
    private MainActivity main;

    public void setMain(MainActivity main) {
        this.main = main;
    }

    public void setBondingFragment(BondingFragment bondingFragment) {
        this.bondingFragment = bondingFragment;
    }

    public void setCalibrationFragment(CalibrationFragment calibrationFragment) {
        this.calibrationFragment = calibrationFragment;
    }

    public void setBleTestFragment(BLETestFragment bleTestFragment) {
        this.bleTestFragment = bleTestFragment;
    }

    public void setConnectionFragment(ConnectionFragment connectionFragment) {
        this.connectionFragment = connectionFragment;
    }

    public void setUnityFragment(UnityFragment unityFragment) {
        this.unityFragment = unityFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        byte[] data;
        String dataString;

        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            Log.d(LOG_TAG, "Device has been connected.");
            if (isCurrentFragment(connectionFragment)) {
                connectionFragment.updateUI();
            }
            // Check to see if connected device is a game controller
            main.checkForMyoController();
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            Log.d(LOG_TAG, "Device has been disconnected.");
            if (!isCurrentFragment(connectionFragment)) {
                // Handle disconnect
                main.onDisconnect();
            }
        } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//            List<BluetoothGattService> services = main.getBluetoothLeService().getSupportedGattServices();
//            System.out.print(services);
            Log.d(LOG_TAG, "GATT services discovered.");
            main.getBluetoothLeService().setCharacteristicNotification();
        } else if (BluetoothLeService.ACTION_DATA_SENT.equals(action)) {
            // Sent data to Myoband
            Log.d(LOG_TAG, "SENT DATA");
            // Get data sent by application
            data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
            dataString = new String(data, StandardCharsets.UTF_8);

        } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            // Received data from Myoband
            Log.d(LOG_TAG, "RECEIVED DATA");
            // Get data received by application
            data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
            dataString = new String(data, StandardCharsets.UTF_8);
//            dataString = dataString.substring(0, dataString.length() - 1);  // remove \n
            if (isCurrentFragment(calibrationFragment)) {
                try {
                    // Set meter value for calibration fragment
                    int analogValue = Integer.parseInt(dataString);
                    calibrationFragment.setBar1Value(analogValue);
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Unable to parse integer.");
                }
            } else if (isCurrentFragment(bleTestFragment)) {
                bleTestFragment.setText(dataString);
            } else if (dataString.equals(BluetoothLeService.TIME)) {
                main.getBluetoothLeService().write("T");
            }
//            UnityPlayer.UnitySendMessage("Canvas", "ShowMessage", dataString);
        }
    }

    /**
     * Determines if the fragment is currently visible.
     *
     * @param fragment fragment that may be visible.
     * @return true if fragment is visible, otherwise false.
     */
    private boolean isCurrentFragment(Fragment fragment) {
        return fragment != null && fragment.isVisible();
    }
}