package com.cmorrell.myobandcompanionapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class DeveloperDialogFragment extends DialogFragment {

    public static final String TAG = "DeveloperDialog";
    public static final String DEVELOPER_CODE = "4263";


    public DeveloperDialogFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_developer_dialog, container, false);
        Button btn = view.findViewById(R.id.enter_btn);
        EditText et = view.findViewById(R.id.pin_et);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = null;
                String code = et.getText().toString();
                if (code.equals(DEVELOPER_CODE)) {
                    dismiss();
                    NavGraphDirections.ActionGlobalSettingsFragment action = NavGraphDirections.actionGlobalSettingsFragment();
                    action.setCorrectPassCode(true);
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(action);
                    message = "Success";
                } else {
                    message = "Incorrect developer code";
                }
                Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                et.setText(""); // clear EditText
            }
        });
        return view;
    }

}