package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class SettingsFragment extends Fragment {

    public Button developerBtn;
    public Button numElectrodesBtn;
    public Button saveBtn;
    private MainActivity main;


    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) requireActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        developerBtn = view.findViewById(R.id.developer_btn);
        numElectrodesBtn = view.findViewById(R.id.num_electrodes_btn);
        saveBtn = view.findViewById(R.id.save_btn);
        Button menuBtn = view.findViewById(R.id.quit_settings_btn);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_global_menuFragment);
            }
        });

        developerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show pin DialogFragment
                new DeveloperDialogFragment().show(
                        getChildFragmentManager(), DeveloperDialogFragment.TAG);
            }
        });

        numElectrodesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ElectrodeDialogFragment().show(
                        getChildFragmentManager(), ElectrodeDialogFragment.TAG);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.toggleSaveAnalogData();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        assert getArguments() != null;
        boolean correctPassCode = getArguments().getBoolean("passCode", false);
//        SettingsFragmentArgs.fromBundle(getArguments().getBundle("passCode"))
//        boolean correctPassCode = SettingsFragmentArgs.fromBundle(getArguments()).getCorrectPassCode();
//        SettingsFragmentArgs
        if (correctPassCode) {
            developerBtn.setVisibility(View.INVISIBLE);
            TextView tv = view.findViewById(R.id.developer_tv);
            tv.setVisibility(View.VISIBLE);
            saveBtn.setVisibility(View.VISIBLE);
        }
    }
}