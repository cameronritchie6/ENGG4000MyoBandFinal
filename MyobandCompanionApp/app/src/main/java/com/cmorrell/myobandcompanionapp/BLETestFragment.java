package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class BLETestFragment extends Fragment {

    public Button sendBtn;
    private MainActivity main;
    public EditText editText;
    public TextView textView;

    public BLETestFragment() {
        // Required empty public constructor
        main = (MainActivity) requireActivity();
        MainActivity.myoReceiver.setBleTestFragment(BLETestFragment.this);
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_b_l_e_test, container, false);

        sendBtn = view.findViewById(R.id.send_btn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.getBluetoothLeService().write(editText.getText().toString());
            }
        });

        return view;
    }
}