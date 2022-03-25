package com.zebrascannerdemo.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.zebrascannerdemo.application.ScanApplication;

public class Preferences {

    private static Preferences mInstance;
    private SharedPreferences mSharedPreferences;

    private Preferences() {
        mSharedPreferences = getSharedPreference();
    }

    public static Preferences getInstance() {
        if (mInstance == null) {
            mInstance = new Preferences();
        }
        return mInstance;
    }

    private Context getContext() {
        return ScanApplication.getContext();
    }

    private SharedPreferences getSharedPreference() {
        return getContext().getSharedPreferences(getContext().getPackageName(), Context.MODE_PRIVATE);
    }

    public void setString(String aKey, String aValue) {
        SharedPreferences.Editor lEditor = mSharedPreferences.edit();
        lEditor.putString(aKey, aValue);
        lEditor.apply();
    }

    public String getString(String aKey, String defaultValue) {
        return mSharedPreferences.getString(aKey, defaultValue);
    }

    public boolean getBoolean(String aKey, boolean aDefaultValue) {
        return mSharedPreferences.getBoolean(aKey, aDefaultValue);
    }

    public void setBoolean(String aKey, boolean aValue) {
        SharedPreferences.Editor lEditor = mSharedPreferences.edit();
        lEditor.putBoolean(aKey, aValue);
        lEditor.apply();
    }

    public void setInteger(String aKey, int aValue) {
        SharedPreferences.Editor lEditor = mSharedPreferences.edit();
        lEditor.putInt(aKey, aValue);
        lEditor.apply();
    }

    public int getInteger(String aKey, int defaultValue) {
        return mSharedPreferences.getInt(aKey, defaultValue);
    }
}
