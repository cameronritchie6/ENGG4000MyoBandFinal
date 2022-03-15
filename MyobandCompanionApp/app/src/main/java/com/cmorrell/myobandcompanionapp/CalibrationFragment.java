package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;


public class CalibrationFragment extends Fragment {

    ProgressBar bar1;
    ProgressBar bar2;
    MainActivity main;

    public CalibrationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) requireActivity();
        MainActivity.myoReceiver.setCalibrationFragment(CalibrationFragment.this);

        main.setCalibrationFragment(CalibrationFragment.this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);
        bar1 = view.findViewById(R.id.bar_1);
        bar2 = view.findViewById(R.id.bar_2);
        bar1.setProgress(45);
        bar2.setProgress(80);
        return view;
    }

    public void setBar1Value(int value) {
        bar1.setProgress(value);
    }

    public void setBar2Value(int value) {
        bar2.setProgress(value);
    }
}