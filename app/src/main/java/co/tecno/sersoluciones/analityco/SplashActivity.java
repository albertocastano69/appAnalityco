package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;

import java.util.LinkedHashMap;
import java.util.Map;

import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.utilities.AppPermissions;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.views.AnimatedSvgView.STATE_FINISHED;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity implements RequestBroadcastReceiver.BroadcastListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 30;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private MyPreferences preferences;
    private int state;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private boolean[] fillArray;
    private LinkedHashMap<Uri, String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        preferences = new MyPreferences(this);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
//        Push.setListener(new MyPushListener());
//        MobileCenter.start(getApplication(), "39bd9aee-8cf3-4d58-95f7-232e1a62bfe7",
//                Analytics.class, Crashes.class, Push.class);
//        Crashes.setEnabled(false);

        data = new LinkedHashMap<>();
        data.put(Constantes.CONTENT_CITY_URI, Constantes.LIST_CITIES_URL);
        data.put(Constantes.CONTENT_DANE_CITY_URI, Constantes.LIST_DANE_CITIES_URL);
        data.put(Constantes.CONTENT_ECONOMIC_URI, Constantes.LIST_ECONOMIC_URL);
        data.put(Constantes.CONTENT_WORK_URI, Constantes.LIST_JOB_URL);
        data.put(Constantes.CONTENT_SEC_REFS_URI, Constantes.LIST_SEC_REF_URL);

        fillArray = new boolean[data.size()];
        //MobileCenter.setLogLevel(Log.VERBOSE);

        mVisible = true;
        mContentView = findViewById(R.id.fullscreen_content);


        if (AppPermissions.checkAppPermissionsAndRequest(this)) {

            if (!verifyFillData()) {
                verifyInDb();
            }

            if (preferences.getDeviceId().isEmpty())
                preferences.setDeviceID();

        } else {
            findViewById(R.id.splashFragment).setVisibility(View.INVISIBLE);
            return;
        }
//        preferences.cleanJWT();
        verifyInDb();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (!extras.getBoolean("viewAnimation")) {
                if (verifyFillData())
                    onStateChange(STATE_FINISHED);
            }
        }

//            Fragment fragment = new SplashFragment();
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, fragment).commit();
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log("onNewIntent SplashActivity");
        //Push.checkLaunchedFromNotification(this, intent);
    }

    private void verifyInDb() {
        ContentValues values = new ContentValues();
        values.put("all", true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);

        int pos = 0;
        for (Object o : data.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            try (Cursor cursor = getContentResolver().query((Uri) pair.getKey(), null, null, null,
                    null)) {
                if (cursor != null) {
                    if (cursor.getCount() == 0) {
                        CRUDService.startRequest(this, (String) pair.getValue(),
                                Request.Method.GET, paramsQuery, true);
                    } else {
                        fillArray[pos] = true;
                    }
                    logW(pos + ". URL to Request: " + pair.getValue() + ": " + fillArray[pos]);

                    pos++;
                    //it.remove();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        logE("ON DESTROY SPLASH ACTIVITY");
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(10);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
        finish();
    }

    public void onStateChange(int state) {
        this.state = state;
        if (state == STATE_FINISHED) {
            verifyFillData();
        }
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AppPermissions.requestPermissionsResultHandler(requestCode, grantResults, this);
    }

    @Override
    public void onStringResult(String action, int option, String response, String url) {
        switch (action) {
            case CRUDService.ACTION_REQUEST_GET:
            case CRUDService.ACTION_REQUEST_SAVE:
                processRequestGet(option, url);
                break;
            case CRUDService.ACTION_REQUEST_POST:
            case CRUDService.ACTION_REQUEST_PUT:
            case CRUDService.ACTION_REQUEST_DELETE:

                break;
            case CRUDService.ACTION_REQUEST_FORM_DATA:
                break;
        }
    }

    private void processRequestGet(int option, String url) {
        log(url + " option: " + option);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                switch (url) {
                    case Constantes.LIST_CITIES_URL:
                        log("tabla llena LIST_CITIES_URL");
                        fillArray[0] = true;
                        break;
                    case Constantes.LIST_DANE_CITIES_URL:
                        log("tabla llena LIST_DANE_CITIES_URL");
                        fillArray[1] = true;
                        break;
                    case Constantes.LIST_ECONOMIC_URL:
                        log("tabla llena LIST_ECONOMIC_URL");
                        fillArray[2] = true;
                        break;
                    case Constantes.LIST_JOB_URL:
                        log("tabla llena LIST_JOB_URL");
                        fillArray[3] = true;
                        break;
                    case Constantes.LIST_SEC_REF_URL:
                        log("tabla llena LIST_SEC_REF_URL");
                        fillArray[4] = true;
                        break;
                }
                verifyFillData();
                break;
            case Constantes.UNAUTHORIZED:
            case Constantes.FORBIDDEN:
            case Constantes.REQUEST_NOT_FOUND:
            case Constantes.BAD_REQUEST:
            case Constantes.NOT_INTERNET:
                if (state == STATE_FINISHED) {
                    findViewById(R.id.splashFragment).setVisibility(View.GONE);
                }
                Toast.makeText(SplashActivity.this, "Modo Offline", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private boolean verifyFillData() {
        boolean flag = true;
        for (boolean item : fillArray) {
            if (!item) {
                flag = false;
                break;
            }
        }
        if (state == STATE_FINISHED && flag) {
            findViewById(R.id.splashFragment).setVisibility(View.GONE);
        }
        return flag;
    }
}
