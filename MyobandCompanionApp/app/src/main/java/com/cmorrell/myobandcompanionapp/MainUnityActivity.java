package com.cmorrell.myobandcompanionapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.player.UnityPlayer;
import com.unity3d.player.UnityPlayerActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainUnityActivity extends UnityPlayerActivity {

    private UnityPlayer unityPlayer;
    private static final String LOG_TAG = "MainUnityActivity";


    private long previousTime = Calendar.getInstance().getTimeInMillis();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unityPlayer = new UnityPlayer(this);
        setContentView(unityPlayer);
        Log.d(LOG_TAG, "WORKING");


        unityPlayer.requestFocus();
        unityPlayer.windowFocusChanged(true);
    }

    /**
     * Generic method that is called whenever a MotionEvent is detected.
     * @param ev MotionEvent that was detected
     * @return true if source is an analog stick
     */
    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {

        // Check that the event came from a game controller
        if ((ev.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                ev.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = ev.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(ev, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(ev, -1);
            return true;
        }
        return super.dispatchGenericMotionEvent(ev);
    }

    /**
     *
     * @param event MotionEvent that has occurred
     * @param historyPos position in analog history buffer
     */
    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {

        float[] analogStickValues = GameControls.processJoystickInput(event, historyPos);

        float x = analogStickValues[0];
        float y = analogStickValues[1];

        Log.d(LOG_TAG, String.format("X: %f\tY: %f", x, y));


            if (GameControls.map(x) < GameControls.THRESHOLD_VOLTAGE) {
                releaseUpButton();
            }
            if (GameControls.map(y) < GameControls.THRESHOLD_VOLTAGE) {
                releaseDownButton();
            }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.d(LOG_TAG, "BUTTON: " + keyCode);
        switch (keyCode) {
            case GameControls.ELECTRODE_1_CODE:
                pressUpButton();
            case GameControls.ELECTRODE_2_CODE:
                pressDownButton();
            case GameControls.CO_CONTRACTION_CODE:
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
        return (time - previousTime) >= GameControls.COOLDOWN_IN_MILLIS;
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
        Log.d(LOG_TAG, "RUP");
        UnityPlayer.UnitySendMessage("PlayerShip", "releaseUpButton", "");
        UnityPlayer.UnitySendMessage("PlayerShipLite", "releaseUpButton", "");
    }

    private void releaseDownButton() {
        Log.d(LOG_TAG, "RDOWN");
        UnityPlayer.UnitySendMessage("PlayerShip", "releaseDownButton", "");
        UnityPlayer.UnitySendMessage("PlayerShipLite", "releaseDownButton", "");
    }


    private void quadrilateralJump() {
        UnityPlayer.UnitySendMessage("Player", "Jump", "");
    }


    private void spaceGameShoot() {
        UnityPlayer.UnitySendMessage("PlayerShip", "fireSecondary", "");
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