package com.regosen.dailyfuzzy.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import com.regosen.dailyfuzzy.receivers.BootReceiver;
import com.regosen.dailyfuzzy.receivers.NotificationReceiver;

import java.util.Calendar;

public class NotificationUtil {

    public static final String PREF_NOTIFICATION_ENABLED = "PREF_NOTIFICATION_ENABLED";
    public static final String PREF_NOTIFICATION_HOUR = "PREF_NOTIFICATION_HOUR";
    public static final String PREF_NOTIFICATION_MINUTE = "PREF_NOTIFICATION_MINUTE";

    public static int REQUEST_CODE_NOTIFICATION = 0;

    private static AlarmManager mAlarmMgr;
    private static PendingIntent mAlarmIntent;

    public static void setDailyNotifications(Context context, boolean notify, int hour, int minute, boolean savePrefs)
    {
        if (savePrefs)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(PREF_NOTIFICATION_ENABLED, notify);
            editor.putInt(PREF_NOTIFICATION_HOUR, hour);
            editor.putInt(PREF_NOTIFICATION_MINUTE, minute);
            editor.commit();
        }
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        // turn off existing alarm
        if (mAlarmMgr != null) {
            mAlarmMgr.cancel(mAlarmIntent);

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }

        // create new alarm (if enabled)
        if (notify)
        {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            boolean needsDayAdded = hour < c.get(Calendar.HOUR_OF_DAY);
            if (!needsDayAdded && (hour == c.get(Calendar.HOUR_OF_DAY))) {
                needsDayAdded = minute <= c.get(Calendar.MINUTE);
            }
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            if (needsDayAdded) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }

            mAlarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.setAction(NotificationReceiver.NOTIFICATION_ACTION);
            mAlarmIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_NOTIFICATION, intent, 0);

            mAlarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, mAlarmIntent);

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }
     
}