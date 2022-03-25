package com.zebrascannerdemo.application;

import android.app.Application;
import android.content.Context;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.SDKHandler;
import com.zebrascannerdemo.zebra.Barcode;
import com.zebrascannerdemo.zebra.ScannerAppEngine;
import java.util.ArrayList;

public class ScanApplication extends Application {

    public static SDKHandler sdkHandler;
    public static ArrayList<ScannerAppEngine.IScannerAppEngineDevListDelegate> mDevListDelegates = new ArrayList<>();
    public static ArrayList<DCSScannerInfo> mScannerInfoList = new ArrayList<>();
    public static ArrayList<Barcode> barcodeData = new ArrayList<>();
    private static ScanApplication instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        sdkHandler = new SDKHandler(this);
    }
}