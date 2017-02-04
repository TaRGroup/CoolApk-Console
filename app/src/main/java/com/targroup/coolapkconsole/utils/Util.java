package com.targroup.coolapkconsole.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.targroup.coolapkconsole.App;
import com.targroup.coolapkconsole.BuildConfig;
import com.targroup.coolapkconsole.model.UserSave;
import com.targroup.coolapkconsole.services.RegistrationIntentService;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import io.yunba.android.manager.YunBaManager;

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
    public static class PushUtil {
        /**
         * Check the device to make sure it has the Google Play Services APK. If
         * it doesn't, display a dialog that allows users to download the APK from
         * the Google Play Store or enable it in the device's system settings.
         */
        public static boolean checkPlayServices(final Activity context) {
            final String TAG = "Push";
            final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
            if (resultCode != ConnectionResult.SUCCESS) {
                if (apiAvailability.isUserResolvableError(resultCode)) {
                    apiAvailability.getErrorDialog(context, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                            .show();
                } else {
                    Log.i(TAG, "This device is not supported.");
                }
                return false;
            }
            return true;
        }
        public static void startPush (Context c) {
            UserSave userSave = new UserSave();
            if (userSave.isLogin()) {
                switch (getPushMode()) {
                    case "yunba" :
                        YunBaManager.start(c.getApplicationContext());
                        YunBaManager.subscribe(c.getApplicationContext(), userSave.getUID(), new IMqttActionListener() {
                            private static final String TAG = "Push-YunbaInit";
                            @Override
                            public void onSuccess(IMqttToken arg0) {
                                Log.d(TAG, "Subscribe topic succeed");
                            }

                            @Override
                            public void onFailure(IMqttToken arg0, Throwable arg1) {
                                Log.d(TAG, "Subscribe topic failed");
                            }
                        });
                        break;
                    case "gcm" :
                        Intent intent = new Intent(c, RegistrationIntentService.class);
                        c.startService(intent);
                        break;
                }
            }
        }
        public static String getPushMode () {
            return  App.getPrefs().getString("PUSH_CHOICE", "gcm");
        }
        public static boolean isPushEnable () {
            return !"disable".equals(getPushMode());
        }
    }
}
