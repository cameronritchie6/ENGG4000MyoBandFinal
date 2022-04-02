package com.cmorrell.myobandcompanionapp;

public class FragmentContexts {
    private static CalibrationFragment calibrationFragment;
    private BondingFragment bondingFragment;

    public void setCalibrationFragment(CalibrationFragment calibrationFragment) {
        FragmentContexts.calibrationFragment = calibrationFragment;
    }

    public CalibrationFragment getCalibrationFragment() {
        return calibrationFragment;
    }

    public void setBondingFragment(BondingFragment bondingFragment) {
        this.bondingFragment = bondingFragment;
    }

    public BondingFragment getBondingFragment() {
        return bondingFragment;
    }
}
