package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


public class CalibrationFragment extends Fragment {

    public ProgressBar bar1;
    public ProgressBar bar2;
    public TextView tv1;
    public TextView tv2;

    public MainActivity main;
    public static final int PB_MAX = 255;
    public static final int PB_MIN = 0;
    private final Handler handler = new Handler();

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

        Button menuBtn = view.findViewById(R.id.quit_calibration_btn);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_global_menuFragment);
            }
        });

        bar1 = view.findViewById(R.id.bar_1);
        bar2 = view.findViewById(R.id.bar_2);


        tv1 = view.findViewById(R.id.tv_proof_1);
        tv2 = view.findViewById(R.id.tv_proof_2);

        // Initialize ProgressBars to show resolution of 8 bits
        initProgressBar(bar1);
        initProgressBar(bar2);
        return view;
    }

    public void setBar1Value(int value) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                bar1.setProgress(value);
                tv1.setText(String.valueOf(value));
            }
        });

    }

    public void setBar2Value(int value) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                bar2.setProgress(value);
                tv2.setText(String.valueOf(value));
            }
        });
    }

    private void initProgressBar(ProgressBar bar) {
        // Set min and max to have resolution of 8 bits
        bar.setMin(PB_MIN);
        bar.setMax(PB_MAX);
    }
}