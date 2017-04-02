package com.targroup.coolapkconsole.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.targroup.coolapkconsole.BuildConfig;

/**
 * Created by Administrator on 2017/1/31.
 */

public class Util {
    public static enum PublishState {
        PUBLISH_STATE_STABLE,
        PUBLISH_STATE_BETA,
        PUBLISH_STATE_ALPHA,
        PUBLISH_STATE_DEBUG
    }
    public static PublishState getPublishState () {
        if (BuildConfig.DEBUG)
            return PublishState.PUBLISH_STATE_DEBUG;
        switch (BuildConfig.BUILD_TYPE) {
            case "stable" :
                return PublishState.PUBLISH_STATE_STABLE ;
            case "beta" :
                return PublishState.PUBLISH_STATE_BETA ;
            case "alpha" :
                return PublishState.PUBLISH_STATE_ALPHA ;
            default:
                return PublishState.PUBLISH_STATE_STABLE;
        }
    }
    public static int[] buildMaterialColors () {
        return new int[]{
                Color.parseColor("#FFC93437")
                , Color.parseColor("#FF375BF1")
                , Color.parseColor("#FFF7D23E")
                , Color.parseColor("#FF34A350")
        };
    }
    /**
     * @see <a href="http://stackoverflow.com/questions/28998241/how-to-clear-cookies-and-cache-of-webview-on-android-when-not-in-webview" />
     * @param context
     */
    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            CookieSyncManager cookieSyncMngr= CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager= CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
    public static void restartApp (Context context, Class<? extends Activity> activity) {
        Intent mStartActivity = new Intent(context, activity);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
