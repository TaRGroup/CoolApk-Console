package com.targroup.coolapkconsole.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.targroup.coolapkconsole.R;
import com.targroup.coolapkconsole.activities.MainActivity;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by Administrator on 2017/2/4.
 */

public class CloudMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {
            String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
            String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setContentText(msg)
                    .setContentTitle(context.getString(R.string.app_name)).build();
            ((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(0, notification);
        }
    }
}
