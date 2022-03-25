package com.zebrascannerdemo.zebra;

import android.util.Log;

public class Constant {
    public static final boolean DEBUG = true;

    public static final String ACTION_USB_PERMISSION = "com.android.hardware.USB_PERMISSION";
    public static final String ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    //Action strings for various RFID Events
    public static final String ACTION_SCANNER_CONNECTED = "com.zebra.scannercontrol.connected";
    public static final String ACTION_SCANNER_DISCONNECTED = "com.zebra.scannercontrol.disconnected";
    public static final String ACTION_SCANNER_AVAILABLE = "com.zebra.scannercontrol.available";
    public static final String ACTION_SCANNER_CONN_FAILED = "com.zebra.scannercontrol.conn.failed";
    public static final String ACTION_SCANNER_BARCODE_RECEIVED = "com.zebra.scannercontrol.barcode.received";
    public static final String ACTION_SCANNER_IMAGE_RECEIVED = "com.zebra.scannercontrol.image.received";
    public static final String ACTION_SCANNER_VIDEO_RECEIVED = "com.zebra.scannercontrol.video.received";
    public static final String PREF_OPMODE = "MOT_SETTING_OPMODE";
    public static final String PREF_SCANNER_DETECTION = "MOT_SETTING_SCANNER_DETECTION";
    public static final String PREF_EVENT_ACTIVE = "MOT_SETTING_EVENT_ACTIVE";
    public static final String PREF_EVENT_AVAILABLE = "MOT_SETTING_EVENT_AVAILABLE";
    public static final String PREF_EVENT_BARCODE = "MOT_SETTING_EVENT_BARCODE";
    public static final String PREF_EVENT_IMAGE = "MOT_SETTING_EVENT_IMAGE";
    public static final String PREF_EVENT_VIDEO = "MOT_SETTING_EVENT_VIDEO";
    public static final String PREF_EVENT_BINARY_DATA = "MOT_SETTING_EVENT_BINARY_DATA";
    //---------------------------------------------------------------------------
    public static final String PREF_NOTIFY_ACTIVE = "MOT_SETTING_NOTIFICATION_ACTIVE";
    public static final String PREF_NOTIFY_AVAILABLE = "MOT_SETTING_NOTIFICATION_AVAILABLE";
    public static final String PREF_NOTIFY_BARCODE = "MOT_SETTING_NOTIFICATION_BARCODE";
    public static final String PREF_NOTIFY_IMAGE = "MOT_SETTING_NOTIFICATION_IMAGE";
    public static final String PREF_NOTIFY_VIDEO = "MOT_SETTING_NOTIFICATION_VIDEO";
    public static final String PREF_NOTIFY_BINARY_DATA = "MOT_SETTING_NOTIFICATION_BINARY_DATA";
    //Type of data recieved
    public static final int BARCODE_RECEIVED = 30;
    public static final int SESSION_ESTABLISHED = 31;
    public static final int SESSION_TERMINATED = 32;
    public static final int SCANNER_APPEARED = 33;
    public static final int SCANNER_DISAPPEARED = 34;
    public static final int FW_UPDATE_EVENT = 35;
    public static final int AUX_SCANNER_CONNECTED = 36;
    public static final int IMAGE_RECEIVED = 37;
    public static final int VIDEO_RECEIVED = 38;
    //Data related to notifications
    public static final String NOTIFICATIONS_TYPE = "notifications_type";
    public static final String NOTIFICATIONS_TEXT = "notifications_text";
    public static final String NOTIFICATIONS_ID = "notifications_id";
    //---------------------------------------------------------------------------
    //Zebra Scanner Configuration related constant
    public static String ScannerID = "NoOfPastOrder";
    public static String ScannerName = "ScannerName";
    public static String PickListMode = "PickListMode";
    public static String bluetoothAddress = "40:a1:08:4f:7a:82";
    public static String BeeperVolume = "BeeperVolume";
    public static String BeeperSequence = "BeeperSequence";
    public static String GreenLEDControl = "GreenLEDControl";
    public static String BlueLEDControl = "BlueLEDControl";
    public static String RedLEDControl = "RedLEDControl";
    public static String VibrationEnable = "VibrationEnable";
    public static String VibrationDuration = "VibrationDuration";
    public static String TriggerSetting = "TriggerSetting";
    public static String AimMode = "AimMode";
    //Settings for notifications
    public static int MOT_SETTING_OPMODE;
    public static boolean MOT_SETTING_SCANNER_DETECTION;
    public static boolean MOT_SETTING_EVENT_ACTIVE;
    public static boolean MOT_SETTING_EVENT_AVAILABLE;
    public static boolean MOT_SETTING_EVENT_BARCODE;
    public static boolean MOT_SETTING_EVENT_IMAGE;
    public static boolean MOT_SETTING_EVENT_VIDEO;
    public static boolean MOT_SETTING_EVENT_BINARY_DATA;
    public static boolean MOT_SETTING_NOTIFICATION_ACTIVE;
    public static boolean MOT_SETTING_NOTIFICATION_AVAILABLE;
    public static boolean MOT_SETTING_NOTIFICATION_BARCODE;
    public static boolean MOT_SETTING_NOTIFICATION_IMAGE;
    public static boolean MOT_SETTING_NOTIFICATION_VIDEO;
    public static boolean MOT_SETTING_NOTIFICATION_BINARY_DATA;

    /**
     * Method to be used throughout the app for logging debug messages
     *
     * @param type    - One of TYPE_ERROR or TYPE_DEBUG
     * @param TAG     - Simple String indicating the origin of the message
     * @param message - Message to be logged
     */
    public static void logAsMessage(DEBUG_TYPE type, String TAG, String message) {
        if (DEBUG) {
            if (type == DEBUG_TYPE.TYPE_DEBUG)
                Log.d(TAG, message);
            else if (type == DEBUG_TYPE.TYPE_ERROR)
                Log.e(TAG, message);
        }
    }
    public enum DEBUG_TYPE {
        TYPE_DEBUG, TYPE_ERROR
    }

}
