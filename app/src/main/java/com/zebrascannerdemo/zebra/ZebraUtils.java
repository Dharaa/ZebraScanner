package com.zebrascannerdemo.zebra;


import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebrascannerdemo.Utils.Preferences;

public class ZebraUtils {
    public static DCSScannerInfo scannerInfo;
    //---------------------------------------------------------------------------
    //Zebra Scanner Configuration settings

    public static int getBeeperVolume() {
        return Preferences.getInstance().getInteger(Constant.BeeperVolume, 0);
    }

    public static void setBeeperVolume(int beeperVolume) {
        Preferences.getInstance().setInteger(Constant.BeeperVolume, beeperVolume);
    }

    public static int getBeeperSequence() {
        return Preferences.getInstance().getInteger(Constant.BeeperSequence, 0);
    }

    public static void setBeeperSequence(int beeperSequence) {
        Preferences.getInstance().setInteger(Constant.BeeperSequence, beeperSequence);
    }

    public static boolean getGreenLEDControl() {
        return Preferences.getInstance().getBoolean(Constant.GreenLEDControl, false);
    }

    public static void setGreenLEDControl(boolean greenLEDControl) {
        Preferences.getInstance().setBoolean(Constant.GreenLEDControl, greenLEDControl);
    }

    public static boolean getBlueLEDControl() {
        return Preferences.getInstance().getBoolean(Constant.BlueLEDControl, false);
    }

    public static void setBlueLEDControl(boolean blueLEDControl) {
        Preferences.getInstance().setBoolean(Constant.BlueLEDControl, blueLEDControl);
    }

    public static boolean getRedLEDControl() {
        return Preferences.getInstance().getBoolean(Constant.RedLEDControl, false);
    }

    public static void setRedLEDControl(boolean redLEDControl) {
        Preferences.getInstance().setBoolean(Constant.RedLEDControl, redLEDControl);
    }

    public static boolean getVibrationEnable() {
        return Preferences.getInstance().getBoolean(Constant.VibrationEnable, false);
    }

    public static void setVibrationEnable(boolean vibrationEnable) {
        Preferences.getInstance().setBoolean(Constant.VibrationEnable, vibrationEnable);
    }

    public static int getVibrationDuration() {
        return Preferences.getInstance().getInteger(Constant.VibrationDuration, 0);
    }

    public static void setVibrationDuration(int vibrationDuration) {
        Preferences.getInstance().setInteger(Constant.VibrationDuration, vibrationDuration);
    }

    public static boolean getTriggerSetting() {
        return Preferences.getInstance().getBoolean(Constant.TriggerSetting, false);
    }

    public static void setTriggerSetting(boolean triggerSetting) {
        Preferences.getInstance().setBoolean(Constant.TriggerSetting, triggerSetting);
    }

    public static int getScannerID() {
        return Preferences.getInstance().getInteger(Constant.ScannerID, -1);
    }

    public static void setScannerID(int scannerID) {
        Preferences.getInstance().setInteger(Constant.ScannerID, scannerID);
    }

    public static String getScannerName() {
        return Preferences.getInstance().getString(Constant.ScannerName, "");
    }

    public static void setScannerName(String scannerName) {
        Preferences.getInstance().setString(Constant.ScannerName, scannerName);
    }

    public static int getPickListMode() {
        return Preferences.getInstance().getInteger(Constant.PickListMode, 0);
    }

    public static void setPickListMode(int pickerMode) {
        Preferences.getInstance().setInteger(Constant.PickListMode, pickerMode);
    }

    public static boolean getAimMode() {
        return Preferences.getInstance().getBoolean(Constant.AimMode, false);
    }

    public static void setAimMode(boolean aimMode) {
        Preferences.getInstance().setBoolean(Constant.AimMode, aimMode);
    }

    //---------------------------------------------------------------------------
}
