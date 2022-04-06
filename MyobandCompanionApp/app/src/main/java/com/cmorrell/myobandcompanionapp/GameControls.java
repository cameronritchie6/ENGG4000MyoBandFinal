package com.cmorrell.myobandcompanionapp;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GameControls {
    public static final int ELECTRODE_1_CODE = 96;
    public static final int ELECTRODE_2_CODE = 97;
    public static final int CO_CONTRACTION_CODE = 98;
    public static final double THRESHOLD_VOLTAGE = 2/3.3 * 100;
    public static final long COOLDOWN_IN_MILLIS = 300;    // Cooldown between inputs

    /**
     * Get the centered range for the analog range
     * @param event MotionEvent that has occurred
     * @param device InputDevice being used as game controller
     * @param axis axis of joystick
     * @param historyPos position in analog history buffer
     * @return coordinate value as a float
     */
    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    /**
     *
     * @param event MotionEvent that has occurred
     * @param historyPos position in analog history buffer
     */
    public static float[] processJoystickInput(MotionEvent event,
                                      int historyPos) {

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float x = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_X, historyPos);
        if (x == 0) {
            x = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_X, historyPos);
        }
        if (x == 0) {
            x = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_Z, historyPos);
        }

        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float y = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);
        if (y == 0) {
            y = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_HAT_Y, historyPos);
        }
        if (y == 0) {
            y = getCenteredAxis(event, inputDevice,
                    MotionEvent.AXIS_RZ, historyPos);
        }

        if (MainActivity.getElectrodeMode() == ElectrodeDialogFragment.MODE_OPEN) {
            // Ignore electrode 2
            y = -1;
        } else if (MainActivity.getElectrodeMode() == ElectrodeDialogFragment.MODE_CLOSE) {
            // Ignore electrode 1
            x = -1;
        }

        return new float[]{x, y};

    }

    /**
     * Map from one range of values to another
     * @param amount value to map to other range
     * @return mapped value
     */
    public static float map(float amount) {
        long inMin = -1;
        long inMax = 1;
        long outMin = 0;
        long outMax = 100;
        return (amount - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }

}
