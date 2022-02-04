package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ConnectionFragment extends Fragment {

    private MainActivity main;
    public ProgressBar progressBar;
    public TextView connectingTv;
    public Button connectedBtn;

    public ConnectionFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) requireActivity();
        MainActivity.myoReceiver.setConnectionFragment(ConnectionFragment.this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection, container, false);

        // Instantiate UI elements
        progressBar = view.findViewById(R.id.connecting_bar);
        connectingTv = view.findViewById(R.id.connecting_tv);
        connectedBtn = view.findViewById(R.id.connected_btn);

        // Set listeners
        connectedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to menu fragment
                NavDirections action = ConnectionFragmentDirections.actionConnectionFragmentToMenuFragment();
                Navigation.findNavController(v).navigate(action);
            }
        });

        updateUI();
//        main.getBluetoothLeService().connect(main.getBluetoothLeService().getDeviceAddress());
        return view;
    }

    public void updateUI() {
        if (main.getBluetoothLeService().getConnected()) {
            progressBar.setVisibility(View.INVISIBLE);
            connectingTv.setVisibility(View.INVISIBLE);
            connectedBtn.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            connectingTv.setVisibility(View.VISIBLE);
            connectedBtn.setVisibility(View.INVISIBLE);
        }
    }
}