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
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.unity3d.player.UnityPlayer;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int SELECT_DEVICE_REQUEST_CODE = 1;    // request code for BLE bonding
    public static final int REQUEST_ENABLE_BT = 2;  // request code to enable Bluetooth


    private BluetoothLeService bluetoothLeService;
    public static MyoReceiver myoReceiver = new MyoReceiver();

    private static final String LOG_TAG = "MainActivity";
    public static final String ACTION_QUIT_UNITY = "com.cmorrell.myobandcompanionapp.ACTION_QUIT_UNITY";

    public UnityPlayer unityPlayer;

    private boolean myoControllerConnected = false;


    private CalibrationFragment calibrationFragment;
    // https://medium.com/android-news/5-steps-to-implement-room-persistence-library-in-android-47b10cd47b24




    /*
    Todo: Saving data in a database
     Todo: Fix the bottom navigation bar disappearing once returning to main menu from Unity
     Todo: Fix orientation change (turning screen sideways)
     Todo: Look at putting calibration screen progress bars on separate thread
     Todo: Let user select input device for game controller
     Todo: Theme setting
     Todo: Number of electrodes setting
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
//            ParcelUuid[] uuids;
//            if (device.fetchUuidsWithSdp()) {
//                uuids = device.getUuids();
//            }
//            device.getType();

//            device.fetchUuidsWithSdp()

            if (device != null && checkForBTPermissions()) {
                // Bond with device
                String address = device.getAddress();
                device.createBond();
//                bluetoothLeService.setDeviceAddress(address);
                bluetoothLeService.setMyoDevice(device);
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

        if (!bluetoothLeService.getConnected()) {
            // Device disconnected in background
            onDisconnect();
        }

    }

    /**
     * Generic method that is called whenever a MotionEvent is detected.
     * @param ev MotionEvent that was detected
     * @return true if source is an analog stick
     */
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {

        // Check that the event came from a game controller
        if ((ev.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                ev.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = ev.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(ev, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(ev, -1);
            return true;
        }
        return super.dispatchGenericMotionEvent(ev);
    }

    public void setCalibrationFragment(CalibrationFragment calibrationFragment) {
        this.calibrationFragment = calibrationFragment;
    }

    /**
     * Get the centered range for the analog range
     * @param event MotionEvent that has occurred
     * @param device InputDevice being used as game controller
     * @param axis axis of joystick
     * @param historyPos position in analog history buffer
     * @return coordinate value as a float
     */
    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    /**
     *
     * @param event MotionEvent that has occurred
     * @param historyPos position in analog history buffer
     */
    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_X, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x == 0) {
            x = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_Z, historyPos);
        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y == 0) {
            y = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_RZ, historyPos);
        }

        Log.d(LOG_TAG, String.format("X: %f\tY: %f", x, y));

        // UPDATE MOVEMENT IN GAME
        if (isCurrentFragment(calibrationFragment)) {
            calibrationFragment.setBar1Value(Math.round(x * 100));
            calibrationFragment.setBar2Value(Math.round(y) * 100);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(LOG_TAG, "BUTTON: " + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Determines if the fragment is currently visible.
     *
     * @param fragment fragment that may be visible.
     * @return true if fragment is visible, otherwise false.
     */
    public boolean isCurrentFragment(Fragment fragment) {
        return fragment != null && fragment.isVisible();
    }

    /**
     * Get required BLE permissions for this device
     * @return required BLE permissions as a String array
     */
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

    /**
     * Determine if device already has permission
     * @param permission permission to verify
     * @return true if device has permission, false otherwise
     */
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check for BLE permissions
     * @return true if device has all BLE permissions, false otherwise
     */
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

    /**
     * Check to see if a Myoband has been connected
     */
    public void checkForMyoController() {

        myoControllerConnected = false; // assume controller is not connected

        // Loop through device Ids to determine if one of them is a gamepad
        int[] deviceIds = InputDevice.getDeviceIds();

        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                if (dev.getName().equals(BluetoothLeService.DEFAULT_DEVICE_NAME)) {
                    // Input device is a Myoband
                    myoControllerConnected = true;
                    return;
                }
            }
        }

        // Tell user that no Myoband has been detected
        Toast.makeText(this, "No Myoband detected", Toast.LENGTH_SHORT).show();

    }

    /**
     * Handles BLE disconnect from Myoband
     */
    public void onDisconnect() {
        Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment)
                .navigate(R.id.action_global_connectionFragment);
        Toast.makeText(this, "Myoband disconnected", Toast.LENGTH_SHORT).show();
    }


    /**
     * Get BluetoothLeService
     * @return  current BluetoothLeService object
     */
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