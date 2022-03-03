package com.cmorrell.myobandcompanionapp;

import android.content.Intent;
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
        String address = main.getBluetoothLeService().getDeviceAddress();
        if (!main.getBluetoothLeService().connect(address)) {
            Toast.makeText(main, "Failed to connect to Myoband device", Toast.LENGTH_SHORT).show();
            // Maybe go to connection screen, handle disconnect SOMEHOW
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

//        view.requestFocus();
//        main.unityPlayer.clearFocus();

        // Assign UI elements
        calibrationBtn = view.findViewById(R.id.calibration_btn);
        gamesBtn = view.findViewById(R.id.games_btn);
        settingsBtn = view.findViewById(R.id.settings_btn);
//        Button btn = view.findViewById(R.id.ble_button);
//
//        btn.setOnClickListener(v -> {
//            NavDirections action = MenuFragmentDirections.actionMenuFragmentToBLETestFragment();
//            Navigation.findNavController(view).navigate(action);
//        });



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
            // Go to training games screen
//                Intent intent = new Intent(requireActivity(), UnityPlayerActivity.class);
//                startActivity(intent);
            NavDirections action = MenuFragmentDirections.actionMenuFragmentToUnityFragment();
            Navigation.findNavController(view12).navigate(action);

        });

        settingsBtn.setOnClickListener(v -> {
            NavDirections action = MenuFragmentDirections.actionGlobalSettingsFragment();
            Navigation.findNavController(v).navigate(action);
        });

        return view;
    }

}