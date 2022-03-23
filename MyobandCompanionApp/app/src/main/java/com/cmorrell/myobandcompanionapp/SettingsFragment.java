package com.cmorrell.myobandcompanionapp;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class SettingsFragment extends Fragment {

    public Button developerBtn;
    public Button electrodeBtn;


    public SettingsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        developerBtn = view.findViewById(R.id.developer_btn);
        electrodeBtn = view.findViewById(R.id.electrode_btn);
        developerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show pin DialogFragment
                new DeveloperDialogFragment().show(
                        getChildFragmentManager(), DeveloperDialogFragment.TAG);
            }
        });

        electrodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                UsbManager manager = (UsbManager) requireActivity().getSystemService(Context.USB_SERVICE);
//                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
//                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//                while(deviceIterator.hasNext()){
//                    UsbDevice device = deviceIterator.next();
//                    //your code
//                }

                UsbManager manager = (UsbManager) requireActivity().getSystemService(Context.USB_SERVICE);
                UsbAccessory[] accessoryList = manager.getAccessoryList();



            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        boolean correctPassCode = SettingsFragmentArgs.fromBundle(getArguments()).getCorrectPassCode();
        if (correctPassCode) {
            developerBtn.setVisibility(View.INVISIBLE);
            TextView tv = view.findViewById(R.id.developer_tv);
            Button timeBtn = view.findViewById(R.id.time_btn);
            tv.setVisibility(View.VISIBLE);
            timeBtn.setVisibility(View.VISIBLE);
        }
    }
}