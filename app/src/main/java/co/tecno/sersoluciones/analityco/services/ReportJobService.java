package co.tecno.sersoluciones.analityco.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.RequiresApi;


import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.utilities.MetodosPublicos.WORK_DURATION_KEY;

/**
 * Created by Ser Soluciones SAS on 14/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
@RequiresApi(api = Build.VERSION_CODES.M)
public class ReportJobService extends JobService {

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        logW("Servicio iniciado ReportJobService");

        long duration = params.getExtras().getLong(WORK_DURATION_KEY);
        UpdateDBService.startRequest(getApplicationContext(), false);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                jobFinished(params, false);
                //MetodosPublicos.scheduleJob(getApplicationContext()); // reschedule the job
            }
        }, duration);


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}