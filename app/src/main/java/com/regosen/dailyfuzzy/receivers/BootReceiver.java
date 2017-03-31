package com.regosen.dailyfuzzy.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.regosen.dailyfuzzy.utils.NotificationUtil;

import static com.regosen.dailyfuzzy.utils.NotificationUtil.PREF_NOTIFICATION_ENABLED;
import static com.regosen.dailyfuzzy.utils.NotificationUtil.PREF_NOTIFICATION_HOUR;
import static com.regosen.dailyfuzzy.utils.NotificationUtil.PREF_NOTIFICATION_MINUTE;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPref.getBoolean(PREF_NOTIFICATION_ENABLED, false)) {
                NotificationUtil.setDailyNotifications(context, true,
                        sharedPref.getInt(PREF_NOTIFICATION_HOUR, 0),
                        sharedPref.getInt(PREF_NOTIFICATION_MINUTE, 0),
                        false);
            }
        }
    }
}