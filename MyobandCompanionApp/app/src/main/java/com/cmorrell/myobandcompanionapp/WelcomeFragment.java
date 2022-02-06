package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;


public class WelcomeFragment extends Fragment {

    public Button startBtn;
    private MainActivity main;

    public WelcomeFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) requireActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        startBtn = view.findViewById(R.id.start_btn);
        startBtn.setOnClickListener(view1 -> {
            if (main.getBluetoothLeService().checkForPairedDevices()) {
                // Go to ConnectionFragment
                NavDirections action = WelcomeFragmentDirections.actionWelcomeFragmentToConnectionFragment();
                Navigation.findNavController(view1).navigate(action);
            } else {
                // Go to BondingFragment
                NavDirections action = WelcomeFragmentDirections.actionWelcomeFragmentToBondingFragment();
                Navigation.findNavController(view1).navigate(action);
            }

        });
        return view;
    }

}