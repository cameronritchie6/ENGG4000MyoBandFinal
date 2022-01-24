package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class BondingFragment extends Fragment {

    public Button bondingBtn;
    private MainActivity main;


    public BondingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) requireActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_bonding, container, false);
        bondingBtn = view.findViewById(R.id.bonding_btn);
        bondingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Bond with device
//main.getBluetoothLeService().connect(MainActivity.address);
                main.getBluetoothLeService().pairDevice();
//                main.getBluetoothLeService().pairDevice();
//                main.getBluetoothLeService().connect(main.getBluetoothLeService().getDeviceAddress());
            }
        });
        return view;
    }
}