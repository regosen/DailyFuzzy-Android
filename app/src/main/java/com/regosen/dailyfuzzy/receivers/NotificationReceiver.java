package com.regosen.dailyfuzzy.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.activities.PictureActivity;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String NOTIFICATION_ACTION = "DAILYFUZZY_NOTIFICATION_ACTION";

    private static final String TAG = NotificationReceiver.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NOTIFICATION_ACTION)) {
            Intent resultIntent = new Intent(context, PictureActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(context.getString(R.string.notification_action_title))
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                    .setAutoCancel(true)
                    .build();
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification);
        }
    }
}