package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class BondingFragment extends Fragment {

    public Button bondingBtn;
    private MainActivity main;
    public TextView bondingTv;
    public ProgressBar progressBar;


    public BondingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) requireActivity();
        main.setBondingFragment(BondingFragment.this);
        MainActivity.myoReceiver.setBondingFragment(BondingFragment.this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_bonding, container, false);
        bondingTv = view.findViewById(R.id.bonding_tv);
        progressBar = view.findViewById(R.id.bonding_progress_bar);
        bondingBtn = view.findViewById(R.id.bonding_btn);
        bondingBtn.setOnClickListener(view1 -> {
            // Bond with device
            main.getBluetoothLeService().pairDevice();
            setUI(true);
        });
        return view;
    }

    public void setUI(boolean scanning) {
        if (scanning) {
            progressBar.setVisibility(View.VISIBLE);
            bondingBtn.setVisibility(View.INVISIBLE);
            bondingTv.setText("Searching for MyoBand devices!");
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            bondingBtn.setVisibility(View.VISIBLE);
            bondingTv.setText(R.string.bonding_tv_txt);
        }

    }
}