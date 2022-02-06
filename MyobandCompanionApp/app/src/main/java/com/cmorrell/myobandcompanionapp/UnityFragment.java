package com.cmorrell.myobandcompanionapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;

import java.util.Objects;


public class UnityFragment extends Fragment {

    protected UnityPlayer mUnityPlayer;
    FrameLayout frameLayoutForUnity;


    public UnityFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // https://medium.com/xrpractices/embedding-unity-app-inside-native-android-application-c7b82245f8af
        mUnityPlayer = new UnityPlayer(requireActivity());
        View view = inflater.inflate(R.layout.fragment_unity, container, false);
        this.frameLayoutForUnity = (FrameLayout) view.findViewById(R.id.frameLayoutForUnity);
        this.frameLayoutForUnity.addView(mUnityPlayer.getView(),
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        mUnityPlayer.requestFocus();
        mUnityPlayer.windowFocusChanged(true);



        return view;
    }

    // TEST TO RECEIVE DATA FROM UNITY APP, HAVEN'T GOTTEN THIS TO WORK YET
    public void setDataFromUnity(String value) {
        Log.d("UNITY", "Just received value from Unity app: " + value);
    }

    @Override
    public void onDestroy() {
        mUnityPlayer.quit();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mUnityPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mUnityPlayer.resume();
    }
}