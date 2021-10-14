package co.tecno.sersoluciones.analityco.utilities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import android.widget.EditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import co.tecno.sersoluciones.analityco.BuildConfig;
import co.tecno.sersoluciones.analityco.services.ReportJobService;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser SOluciones SAS on 10/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class MetodosPublicos {

    public static Date getCurrentDate() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

    public static void alertDialog(Context context, String msg) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Mensaje");
            alertDialog.setMessage(msg);
            alertDialog.setPositiveButton("Aceptar", null);
            alertDialog.create().show();
        } catch (Exception e) {
            logW(e.toString());
        }
    }

    private static byte[] getBitmapAsByteArray(Bitmap bitmap, Bitmap.CompressFormat format) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        return getBitmapAsByteArray(bitmap, Bitmap.CompressFormat.JPEG);
    }

    private static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {

        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return bitmap;
    }

    public static boolean validateDigitVerify(String digitVerify, EditText view) {
        ArrayList<Integer> documentNumberTemp = new ArrayList<>();
        String nit = view.getText().toString();
        if (nit.isEmpty() || nit.length() != 9)
            return false;
        final int len = nit.length();
        for (int i = 0; i < len; i++) {
            documentNumberTemp.add(Character.getNumericValue(nit.charAt(i)));
        }

        if (len == 9) {
            int v = 41 * documentNumberTemp.get(0);
            v += 37 * documentNumberTemp.get(1);
            v += 29 * documentNumberTemp.get(2);
            v += 23 * documentNumberTemp.get(3);
            v += 19 * documentNumberTemp.get(4);
            v += 17 * documentNumberTemp.get(5);
            v += 13 * documentNumberTemp.get(6);
            v += 7 * documentNumberTemp.get(7);
            v += 3 * documentNumberTemp.get(8);
            v = v % 11;
            if (v >= 2) v = 11 - v;

            return digitVerify.equals(String.valueOf(v));
        }
        return false;
    }

    public static final String WORK_DURATION_KEY =
            BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY";
    private static final long MIN_PERIOD_MILLIS = 20 * 60 * 1000L; // 15 minutes

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        @SuppressLint("JobSchedulerService")
        ComponentName serviceComponent = new ComponentName(context, ReportJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setPersisted(true);
        //builder.setMinimumLatency(60 * 1000); // wait at least
        //builder.setOverrideDeadline(2 * DateUtils.MINUTE_IN_MILLIS); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        builder.setPeriodic(MIN_PERIOD_MILLIS);
        //builder.setBackoffCriteria(30000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL);
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            PersistableBundle extras = new PersistableBundle();
            extras.putLong(WORK_DURATION_KEY, 1000);
            builder.setExtras(extras);
            jobScheduler.schedule(builder.build());
        }
    }

}
