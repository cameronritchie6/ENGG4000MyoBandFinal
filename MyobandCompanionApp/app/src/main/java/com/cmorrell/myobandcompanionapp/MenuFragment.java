package com.cmorrell.myobandcompanionapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;


public class MenuFragment extends Fragment {

    public Button calibrationBtn;
    public Button gamesBtn;
    public Button settingsBtn;
    private MainActivity  main;

    public MenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        main = (MainActivity) requireActivity();

        // Connect to device
        BluetoothLeService bluetoothLeService = main.getBluetoothLeService();
        if (bluetoothLeService != null && !bluetoothLeService.getConnected()) {
            String address = main.getBluetoothLeService().getDeviceAddress();
            if (!main.getBluetoothLeService().connect(address)) {
                // Failed to connect, return to connection screen
                main.onDisconnect();
            }
        }


    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // Assign UI elements
        calibrationBtn = view.findViewById(R.id.calibration_btn);
        gamesBtn = view.findViewById(R.id.games_btn);
        settingsBtn = view.findViewById(R.id.settings_btn);



        // Set on click listeners
        calibrationBtn.setOnClickListener(view1 -> {
            // Go to calibration screen
            ArrayList<Integer> gameControllerDeviceIds = new ArrayList<Integer>();
            int[] deviceIds = InputDevice.getDeviceIds();
            for (int deviceId : deviceIds) {
                InputDevice dev = InputDevice.getDevice(deviceId);
                int sources = dev.getSources();

                // Verify that the device has gamepad buttons, control sticks, or both.
                if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                        || ((sources & InputDevice.SOURCE_JOYSTICK)
                        == InputDevice.SOURCE_JOYSTICK)) {
                    // This device is a game controller. Store its device ID.
                    if (!gameControllerDeviceIds.contains(deviceId)) {
                        gameControllerDeviceIds.add(deviceId);
                    }
                }
            }

            NavDirections action = MenuFragmentDirections.actionMenuFragmentToCalibrationFragment();
            Navigation.findNavController(view1).navigate(action);
        });

        gamesBtn.setOnClickListener(view12 -> {
            // Go to training games activity
            Intent intent = new Intent(main, MainUnityActivity.class);
            startActivity(intent);

        });

        settingsBtn.setOnClickListener(v -> {
            NavDirections action = MenuFragmentDirections.actionGlobalSettingsFragment();
            Navigation.findNavController(v).navigate(action);
        });

        return view;
    }

}