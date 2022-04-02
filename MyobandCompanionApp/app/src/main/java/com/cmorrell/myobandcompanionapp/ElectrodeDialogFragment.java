package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ElectrodeDialogFragment extends DialogFragment {

    public static final String TAG = "ElectrodeDialog";
    public static final int MODE_OPEN = 1;
    public static final int MODE_CLOSE = 2;
    public static final int MODE_BOTH = 3;
    private MainActivity main;


    public ElectrodeDialogFragment() {
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
        View view = inflater.inflate(R.layout.fragment_electrode_dialog, container, false);

        Button openBtn = view.findViewById(R.id.open_btn);
        Button closeBtn = view.findViewById(R.id.close_btn);
        Button bothBtn = view.findViewById(R.id.both_btn);

        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.setElectrodeMode(MODE_OPEN);
                dismiss();
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.setElectrodeMode(MODE_CLOSE);
                dismiss();
            }
        });

        bothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.setElectrodeMode(MODE_BOTH);
                dismiss();
            }
        });



        return view;
    }
}