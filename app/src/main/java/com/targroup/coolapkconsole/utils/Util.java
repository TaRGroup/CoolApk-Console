package com.targroup.coolapkconsole.utils;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Created by Administrator on 2017/1/31.
 */

public class Util {
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
}
