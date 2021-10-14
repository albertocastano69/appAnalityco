package co.tecno.sersoluciones.analityco.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * Created by Ser Soluciones SAS on 11/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class AppStartReceiver extends BroadcastReceiver {

    //    private final SyncDBBroadCast alarm = new SyncDBBroadCast();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "android.intent.action.BOOT_COMPLETED")) {
            log("se inicio en AppStartReceiver");
//            alarm.setAlarm(context);
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                MetodosPublicos.scheduleJob(context);
            }*/
        }
    }

}