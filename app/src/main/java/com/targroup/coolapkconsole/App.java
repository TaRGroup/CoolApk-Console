package com.targroup.coolapkconsole;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2017/1/30.
 */

public class App extends Application {
    private static SharedPreferences prefs;
    public static SharedPreferences getPrefs () {
        return prefs;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
