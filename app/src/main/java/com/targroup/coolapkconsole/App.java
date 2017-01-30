package com.targroup.coolapkconsole;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

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
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(this)
        .diskCache(new UnlimitedDiskCache(getCacheDir())).build());
    }
}
