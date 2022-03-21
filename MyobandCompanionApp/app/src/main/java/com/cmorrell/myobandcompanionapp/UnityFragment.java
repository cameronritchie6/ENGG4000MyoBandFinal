package com.cmorrell.myobandcompanionapp;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;


public class UnityFragment extends Fragment {

//    protected UnityPlayer unityPlayer;
    private MainActivity main;


    public UnityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = (MainActivity) requireActivity();
        MainActivity.myoReceiver.setUnityFragment(UnityFragment.this);
        // Lock screen orientation to landscape
        main.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // https://medium.com/xrpractices/embedding-unity-app-inside-native-android-application-c7b82245f8af
//        unityPlayer = new UnityPlayer(main);
//        View view = inflater.inflate(R.layout.fragment_unity, container, false);
        View view = main.unityPlayer.getView();
//        FrameLayout frameLayoutForUnity = (FrameLayout) view.findViewById(R.id.frameLayoutForUnity);
//        frameLayoutForUnity.addView(unityPlayer.getView(),
//                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        main.unityPlayer.requestFocus();
        main.unityPlayer.windowFocusChanged(true);

        return view;
    }



    @Override
    public void onDestroy() {
        main.unityPlayer.quit();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        main.unityPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        main.unityPlayer.resume();
    }
}