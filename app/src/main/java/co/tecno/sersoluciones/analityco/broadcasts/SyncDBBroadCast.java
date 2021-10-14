package co.tecno.sersoluciones.analityco.broadcasts;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import co.tecno.sersoluciones.analityco.services.UpdateDBService;

import static android.content.Context.ALARM_SERVICE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 11/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class SyncDBBroadCast extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private static final long INTERVAL_HOUR = 60 * 60 * 1000;
    private static final long INTERVAL_TIME = 3 * INTERVAL_HOUR;

    @Override
    public void onReceive(Context context, Intent intent) {
        @SuppressLint("SimpleDateFormat")
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy, HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        log("Alarma iniciada, hora: " + date);
        UpdateDBService.startRequest(context, true);
    }

    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, SyncDBBroadCast.class);
        boolean alarmRunning = (PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmRunning) {
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            // Set the alarm's trigger time to 8:00 a.m.
            calendar.set(Calendar.HOUR_OF_DAY, 8);

            /*
             * If you don't have precise time requirements, use an inexact repeating alarm
             * the minimize the drain on the device battery.
             *
             * The call below specifies the alarm type, the trigger time, the interval at
             * which the alarm is fired, and the alarm's associated PendingIntent.
             * It uses the alarm type RTC_WAKEUP ("Real Time Clock" wake up), which wakes up
             * the device and triggers the alarm according to the time of the device's clock.
             *
             * Alternatively, you can use the alarm type ELAPSED_REALTIME_WAKEUP to trigger
             * an alarm based on how much time has elapsed since the device was booted. This
             * is the preferred choice if your alarm is based on elapsed time--for example, if
             * you simply want your alarm to fire every 60 minutes. You only need to use
             * RTC_WAKEUP if you want your alarm to fire at a particular date/time. Remember
             * that clock-based time may not translate well to other locales, and that your
             * app's behavior could be affected by the user changing the device's time setting.
             *
             * Here are some examples of ELAPSED_REALTIME_WAKEUP:
             *
             * // Wake up the device to fire a one-time alarm in one minute.
             * alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
             *         SystemClock.elapsedRealtime() +
             *         60*1000, alarmIntent);
             *
             * // Wake up the device to fire the alarm in 30 minutes, and every 30 minutes
             * // after that.
             * alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
             *         AlarmManager.INTERVAL_HALF_HOUR,
             *         AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);
             */

            // Set the alarm to fire at approximately 8:30 a.m., according to the device's
            // clock, and to repeat once a day.
            //alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
            //        calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

            // Hopefully your alarm will have a lower frequency than this!
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    INTERVAL_TIME, alarmIntent);

            /*PendingIntent.getService(context, 0, intent, 0);
            AlarmManager alarmManagerstop = (AlarmManager)context.getSystemService(ALARM_SERVICE);
            if (alarmManagerstop != null) {
                alarmManagerstop.cancel(alarmIntent);
            }*/
        } else {
            logW("Alarm is already running. Do nothing.");
        }

        // Enable to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, AppStartReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


    /**
     * Cancels the alarm.
     *
     * @param context as context
     */
    // BEGIN_INCLUDE(cancel_alarm)
    @SuppressWarnings("unused")
    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Disable {@code BootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, AppStartReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    // END_INCLUDE(cancel_alarm)

}
