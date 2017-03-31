package com.regosen.dailyfuzzy.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import com.regosen.dailyfuzzy.R;
import com.regosen.dailyfuzzy.utils.FuzzyHelper;
import com.regosen.dailyfuzzy.utils.NotificationUtil;

import java.util.Calendar;

import static com.regosen.dailyfuzzy.utils.NotificationUtil.PREF_NOTIFICATION_ENABLED;
import static com.regosen.dailyfuzzy.utils.NotificationUtil.PREF_NOTIFICATION_HOUR;
import static com.regosen.dailyfuzzy.utils.NotificationUtil.PREF_NOTIFICATION_MINUTE;

public class SettingsActivity extends AppCompatActivity {

    public static int REQUEST_CODE_NOTIFICATION = 0;

    private TimePicker mNotificationTime;
    private CheckBox mNotificationOn;
    private AlarmManager mAlarmMgr;
    private PendingIntent mAlarmIntent;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem itemPast = menu.findItem(R.id.action_settings);
        itemPast.setEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = FuzzyHelper.handleMenuNavigation(item.getItemId(), getApplicationContext());
        if (intent != null) {
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mNotificationTime = (TimePicker)findViewById(R.id.notification_time);
        mNotificationOn = (CheckBox) findViewById(R.id.chk_toggle_notification);

        setTitle(getString(R.string.action_settings));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mNotificationOn.setChecked(sharedPref.getBoolean(PREF_NOTIFICATION_ENABLED, true));
        mNotificationTime.setVisibility(mNotificationOn.isChecked() ? View.VISIBLE : View.INVISIBLE);

        Calendar c = Calendar.getInstance();
        int hour = sharedPref.getInt(PREF_NOTIFICATION_HOUR, c.get(Calendar.HOUR));
        int minute = sharedPref.getInt(PREF_NOTIFICATION_MINUTE, c.get(Calendar.MINUTE));
        mNotificationTime.setCurrentHour(hour);
        mNotificationTime.setCurrentMinute(minute);

        mNotificationOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mNotificationTime.setVisibility(mNotificationOn.isChecked() ? View.VISIBLE : View.INVISIBLE);
                updateNotification();
            }
        });

        mNotificationTime.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {

            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                updateNotification();
            }
        });
    }

    private void updateNotification()
    {
        NotificationUtil.setDailyNotifications(getApplicationContext(), mNotificationOn.isChecked(),
                mNotificationTime.getCurrentHour(), mNotificationTime.getCurrentMinute(), true);
    }
     
}