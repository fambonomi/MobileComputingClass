package com.aware.plugin.mobicom.debug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;

import com.aware.Applications;
import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Light;
import com.aware.Screen;
import com.aware.providers.Applications_Provider;
import com.aware.providers.Light_Provider;
import com.aware.utils.Aware_Plugin;

import java.util.Calendar;

/**
 * Created by denzil on 19/2/15.
 */
public class Plugin extends Aware_Plugin {

    //Tracks likely time to sleep
    private static boolean is_sleeping_time = false;

    //Is the room bright
    private static boolean is_bright = false;

    //Did you set the alarm
    private static boolean is_alarm_set = false;

    //Keep track of sleep time
    private static long sleeping_timer = 0;

    private static long last_timestamp = 0;


    @Override
    public void onCreate() {
        super.onCreate();

        //Activate sensors
        Aware.setSetting(this, Aware_Preferences.STATUS_SCREEN, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_LIGHT, true);
        Aware.setSetting(this, Aware_Preferences.STATUS_APPLICATIONS, true);

        //Apply settings
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

        IntentFilter filter = new IntentFilter();
        filter.addAction(Screen.ACTION_AWARE_SCREEN_OFF);
        filter.addAction(Screen.ACTION_AWARE_SCREEN_ON);
        filter.addAction(Light.ACTION_AWARE_LIGHT);
        filter.addAction(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND);

        registerReceiver(dataReceiver, filter);
    }

    private SensorDataReceiver dataReceiver = new SensorDataReceiver();
    public static class SensorDataReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(System.currentTimeMillis());

            if ( date.get(Calendar.HOUR_OF_DAY) > 22 || date.get(Calendar.HOUR_OF_DAY) < 5 ) {
                is_sleeping_time = true;
            } else {
                is_sleeping_time = false;
            }

            if( intent.getAction().equals(Screen.ACTION_AWARE_SCREEN_OFF) ) {
                if( is_sleeping_time && ! is_bright && is_alarm_set ) {
                    if( last_timestamp == 0 ) last_timestamp = System.currentTimeMillis();
                    sleeping_timer += System.currentTimeMillis() - last_timestamp;

                    last_timestamp = System.currentTimeMillis();
                }
            }
            if( intent.getAction().equals(Screen.ACTION_AWARE_SCREEN_ON)) {
                if( is_sleeping_time && is_bright && is_alarm_set ) {

                    Log.d("SLEEPING", "Slept for : "+sleeping_timer);

                    sleeping_timer = 0;

                }
            }
            if( intent.getAction().equals(Light.ACTION_AWARE_LIGHT)) {
                Cursor light = context.getContentResolver().query(Light_Provider.Light_Data.CONTENT_URI, null, null, null, Light_Provider.Light_Data.TIMESTAMP + " DESC LIMIT 1");
                if( light != null && light.moveToFirst() ) {
                    double light_value = light.getDouble(light.getColumnIndex(Light_Provider.Light_Data.LIGHT_LUX));
                    if( light_value < 20 ) {
                        is_bright = false;
                    } else {
                        is_bright = true;
                    }
                }
            }
            if( intent.getAction().equals(Applications.ACTION_AWARE_APPLICATIONS_FOREGROUND)) {

                date.set(Calendar.HOUR_OF_DAY, 0);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);

                String where = Applications_Provider.Applications_Foreground.TIMESTAMP + ">" + date.getTimeInMillis() + Applications_Provider.Applications_Foreground.PACKAGE_NAME + " LIKE '%alarm%' OR "+ Applications_Provider.Applications_Foreground.APPLICATION_NAME + " LIKE '%alarm%'";
                Cursor alarm = context.getContentResolver().query(Applications_Provider.Applications_Foreground.CONTENT_URI, null, where, null, null, null );
                if( alarm != null && alarm.moveToFirst() ) {
                    is_alarm_set = true;
                }
                if( alarm != null && ! alarm.isClosed()) alarm.close();

                is_alarm_set = false;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }
}
