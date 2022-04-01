package com.cmorrell.myobandcompanionapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Calendar;

public class MainUnityActivity extends UnityPlayerActivity {

    private UnityPlayer unityPlayer;
    private static final String LOG_TAG = "MainUnityActivity";

    private static final int ELECTRODE_1_CODE = 96;
    private static final int ELECTRODE_2_CODE = 97;
    private static final int CO_CONTRACTION_CODE = 99;
    private long previousTime = Calendar.getInstance().getTimeInMillis();
    private static final long COOLDOWN_IN_MILLIS = 300;    // Cooldown between inputs


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main_unity);
        unityPlayer = new UnityPlayer(this);
        setContentView(unityPlayer);
        Log.d(LOG_TAG, "WORKING");
//        if (unityPlayer.getParent() != null) {
//            // Avoid creating multiple Unity layouts
//            ((ViewGroup) unityPlayer.getParent()).removeAllViews();
//        }
//        View view = unityPlayer.getView();
//        FrameLayout frameLayoutForUnity = (FrameLayout) view.findViewById(R.id.frameLayoutForUnity);
//        frameLayoutForUnity.addView(unityPlayer.getView(),
//                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        unityPlayer.requestFocus();
        unityPlayer.windowFocusChanged(true);
    }

    @Override
    public void onUnityPlayerQuitted() {
        super.onUnityPlayerQuitted();
        // Handle quitting out
        showMainActivity();
    }

    public void quitUnity() {
        onUnityPlayerQuitted();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 96 = electrode 1
        // 23 = co-contraction
        // 4 = electrode 2
        // I found the xBox sends multiple codes for 1 button press (1 press sends 96 and 23)
        // xbox:
        // a = 96
        // b = 97
        // x = 99
        // y = 100

        Log.d(LOG_TAG, "BUTTON: " + keyCode);
        switch (keyCode) {
            case ELECTRODE_1_CODE:
                pressUpButton();
            case ELECTRODE_2_CODE:
                pressDownButton();
            case CO_CONTRACTION_CODE:
                spaceGameShoot();
        }
        if (checkCooldown()) {
            // Call method in Unity script
            quadrilateralJump();
            // Reset cooldown
            previousTime = Calendar.getInstance().getTimeInMillis();
        }
        // Don't return super() call to avoid calling back button pressed
        return true;
    }

    private boolean checkCooldown() {
        long time = Calendar.getInstance().getTimeInMillis();
        return (time - previousTime) >= COOLDOWN_IN_MILLIS;
    }

    private void pressUpButton() {
        UnityPlayer.UnitySendMessage("PlayerShip", "pressUpButton", "");
        UnityPlayer.UnitySendMessage("PlayerShipLite", "pressUpButton", "");
    }
    private void pressDownButton() {
        UnityPlayer.UnitySendMessage("PlayerShip", "pressDownButton", "");
        UnityPlayer.UnitySendMessage("PlayerShipLite", "pressDownButton", "");
    }

    private void releaseUpButton() {
        UnityPlayer.UnitySendMessage("PlayerShip", "releaseUpButton", "");
        UnityPlayer.UnitySendMessage("PlayerShipLite", "releaseUpButton", "");
    }

    private void releaseDownButton() {
        UnityPlayer.UnitySendMessage("PlayerShip", "releaseDownButton", "");
        UnityPlayer.UnitySendMessage("PlayerShipLite", "releaseDownButton", "");
    }



    private void quadrilateralJump() {
        UnityPlayer.UnitySendMessage("Player", "Jump", "");
    }

    private void spaceGameMove(Float amount) {
        UnityPlayer.UnitySendMessage("PlayerShip", "JavaThrust", amount.toString());
    }

    private void spaceGameShoot() {
        UnityPlayer.UnitySendMessage("PlayerShip", "fireSecondary", "");
    }

    private void spaceGameLiteMove(Float amount) {
        UnityPlayer.UnitySendMessage("PlayerShipLite", "JavaThrust", amount.toString());
    }

    protected void showMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        unityPlayer.quit();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        unityPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        unityPlayer.resume();
    }
}