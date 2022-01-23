package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


public class CalibrationFragment extends Fragment {

    ProgressBar flexionBar;
    ProgressBar extensionBar;

    public CalibrationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);
        flexionBar = view.findViewById(R.id.flexion_bar);
        extensionBar = view.findViewById(R.id.extension_bar);
        flexionBar.setProgress(45);
        extensionBar.setProgress(80);
        return view;
    }
}