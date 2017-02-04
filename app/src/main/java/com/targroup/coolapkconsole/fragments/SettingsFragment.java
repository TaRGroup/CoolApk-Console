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
        ListPreference pushModePreference = (ListPreference)findPreference("PUSH_CHOICE");
        pushModePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Snackbar.make(getView(), R.string.toast_need_restart, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action_restart, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Util.restartApp(getActivity(), SplashActivity.class);
                            }
                        }).show();
                if ("gcm".equals(Util.PushUtil.getPushMode())) {
                    if (!Util.PushUtil.checkPlayServices(getActivity())) {
                        Snackbar.make(getView(), R.string.toast_google_play_missing, Snackbar.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }
}
