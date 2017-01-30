package com.targroup.coolapkconsole.model;

import android.content.SharedPreferences;

import com.targroup.coolapkconsole.App;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/30.
 * @author liangyuteng0927
 */

public class UserSave {
    private String mToken;
    private String mUserName;
    private String mUID;
    private String mSESSID;

    public void updateToSave () {
        SharedPreferences.Editor editor = App.getPrefs().edit();
        editor.putString("LOGIN-TOKEN", mToken);
        editor.putString("LOGIN-USERNAME", mUserName);
        editor.putString("LOGIN-UID", mUID);
        editor.putString("LOGIN-SESSID", mSESSID);
        editor.apply();
    }

    public UserSave () {
        SharedPreferences preferences = App.getPrefs();
        mToken = preferences.getString("LOGIN-TOKEN", null);
        mUserName = preferences.getString("LOGIN-USERNAME", null);
        mUID = preferences.getString("LOGIN-UID", null);
        mSESSID = preferences.getString("LOGIN-SESSID", null);
    }

    public UserSave (String webCookieString) {
        String [] str = webCookieString.split("; ");
        for (String s : str) {
            String [] str1 = s.split("=");
            switch (str1[0]) {
                case "SESSID" :
                    mSESSID = str1[1];
                    break;
                case "uid" :
                    mUID = str1[1];
                    break;
                case "username" :
                    mUserName = str1[1];
                    break;
                case "token" :
                    mToken = str1[1];
                    break;
            }
        }
    }

    public boolean isLogin () {
        if (mUID == null || mUID.isEmpty())
            return false;
        if (mUserName == null || mUserName.isEmpty())
            return false;
        if (mToken == null || mToken.isEmpty())
            return false;
        return true;
    }

    public Map<String, String> buildWebRequestCookie () {
        Map<String, String> map = new HashMap<>();
        if (!isLogin())
            return map;
        map.put("SESSID", mSESSID);
        map.put("uid", mUID);
        map.put("username", mUserName);
        map.put("token", mToken);
        return map;
    }
}
