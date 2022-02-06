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
        MainActivity.myoReceiver.setBondingFragment(BondingFragment.this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_bonding, container, false);
        bondingBtn = view.findViewById(R.id.bonding_btn);
        bondingBtn.setOnClickListener(view1 -> {
            // Bond with device
            main.getBluetoothLeService().pairDevice();
        });
        return view;
    }
}