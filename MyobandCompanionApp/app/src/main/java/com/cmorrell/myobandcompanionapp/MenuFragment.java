package com.cmorrell.myobandcompanionapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;


public class MenuFragment extends Fragment {

    Button calibrationBtn;
    Button gamesBtn;

    public MenuFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // Assign UI elements
        calibrationBtn = view.findViewById(R.id.calibration_btn);
        gamesBtn = view.findViewById(R.id.games_btn);


        // Set on click listeners
        calibrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to calibration screen
                NavDirections action = MenuFragmentDirections.actionMenuFragmentToCalibrationFragment();
                Navigation.findNavController(view).navigate(action);
            }
        });

        gamesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to training games screen
//                Intent intent = new Intent(requireActivity(), UnityPlayerActivity.class);
//                startActivity(intent);
                NavDirections action = MenuFragmentDirections.actionMenuFragmentToUnityFragment();
                Navigation.findNavController(view).navigate(action);

            }
        });


        return view;
    }



}