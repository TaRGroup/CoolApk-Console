package com.targroup.coolapkconsole.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.activities.SplashActivity;
import com.targroup.coolapkconsole.utils.Util;

/**
 * Created by Administrator on 2017/2/4.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
