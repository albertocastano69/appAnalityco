package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.tecno.sersoluciones.analityco.adapters.RegisterPagerAdapter;
import co.tecno.sersoluciones.analityco.fragments.ProfileFragment;
import co.tecno.sersoluciones.analityco.fragments.RegisterStepOneFragment;
import co.tecno.sersoluciones.analityco.fragments.RegisterStepTwoFragment;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.views.CustomViewPager;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * A login screen that offers login via email/password.
 * Created by Ser Soluciones SAS on 14/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class RegisterActivity extends AppCompatActivity implements RequestBroadcastReceiver.BroadcastListener {


    @BindView(R.id.previous_button)
    Button previousButton;
    @BindView(R.id.next_button)
    Button nextButton;
    @BindView(R.id.submit_button)
    Button submitButton;
    // UI references.
    private View mProgressView;
    private View mLoginFormView;
    private MyPreferences preferences;

    private RegisterActivity instance;

    private GetJSONBroadcastReceiver getJSONBroadcastReceiver;
    private RegisterPagerAdapter mSectionsPagerAdapter;
    private CustomViewPager mViewPager;
    private int posPage;
    private User user;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private String firebaseToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Paso 1 Registrar contacto");
        posPage = 0;
        mViewPager = findViewById(R.id.register_view_pager);
        mViewPager.setPagingEnabled(false);
        mSectionsPagerAdapter = new RegisterPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mSectionsPagerAdapter);
//        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(3);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        getTokenFirebase();
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                log("pos: " + position);
                posPage = position;
                String title = "";
                switch (position) {
                    case 0:
                        mViewPager.setPagingEnabled(false);
                        submitButton.setVisibility(View.GONE);
                        previousButton.setEnabled(false);
                        nextButton.setVisibility(View.VISIBLE);
                        title = "Paso 1 Registrar contacto";
                        break;
                    case 1:
                        mViewPager.setPagingEnabled(false);
                        submitButton.setVisibility(View.GONE);
                        previousButton.setEnabled(true);
                        nextButton.setVisibility(View.VISIBLE);
                        title = "Paso 2 Escaneo cedula";
                        break;
                    case 2:
                        submitButton.setVisibility(View.VISIBLE);
                        previousButton.setEnabled(true);
                        nextButton.setVisibility(View.GONE);
                        title = "Paso 3 Verificacion datos";
                        break;
                }
                RegisterActivity.this.getSupportActionBar().setTitle(title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        preferences = new MyPreferences(this);
        mLoginFormView = findViewById(R.id.form);
        mProgressView = findViewById(R.id.login_progress);

        getJSONBroadcastReceiver = new GetJSONBroadcastReceiver();
        instance = this;

        user = new User();

        if (preferences.isUserLogin()) {
            goMainActivity();
        } else {
            preferences.cleanJWT();
        }

        CrudIntentService.startRequestCRUD(this, Constantes.URL_POLICY,
                Request.Method.GET, "", "", true);
    }

    private void getTokenFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            if (instanceIdResult != null) {
                firebaseToken = instanceIdResult.getToken();
                Log.w("firebaseToken", firebaseToken);
            }
        });
    }


    @OnClick(R.id.next_button)
    public void submit() {
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof RegisterStepOneFragment) {
            ((RegisterStepOneFragment) fragment).sumbitRequest();
        } else if (fragment instanceof RegisterStepTwoFragment) {
            //mSectionsPagerAdapter.getItemPosition(new ProfileFragment());
            mSectionsPagerAdapter.setProfileFragment();
            mSectionsPagerAdapter.notifyDataSetChanged();
            ((RegisterStepTwoFragment) fragment).sumbitRequest();
        }

    }

    @OnClick(R.id.previous_button)
    public void previousPage() {
        posPage--;
        mViewPager.setCurrentItem(posPage);
    }

    @OnClick(R.id.submit_button)
    public void submitRequest() {
        attemptLogin(false);
    }

    public void nextPage(int pos) {
        if (pos == 2) {
            mSectionsPagerAdapter.setProfileFragment();
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
        mViewPager.setCurrentItem(pos);
    }

    public void enableViewPager(boolean enable) {
        mViewPager.setPagingEnabled(enable);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(getJSONBroadcastReceiver,
                intentFilter);

        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(getJSONBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metodo para ir a la actividad principal
     */
    private void goMainActivity() {
        preferences.setUserLogin(true);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(boolean verifyCaptcha) {

        Gson gson = new Gson();
        user.FirebaseToken = firebaseToken;
        final String json = gson.toJson(user);
        logW("json to insert"+json);
        if (verifyCaptcha)
            sendFormToServer(json);
        else
            verifyWithRecaptcha();
    }

    public User getUser() {
        return user;
    }

    private void sendFormToServer(final String json) {
        showProgress(true);
        CrudIntentService.startRequestCRUD(RegisterActivity.this,
                Constantes.CREATE_USER_URL, Request.Method.POST, json, "", false);
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra("viewAnimation", false);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
        finish();
    }

    /**
     * Clase especializada en recibir la respuesta de las peticiones enviadas al servidor
     */
    public class GetJSONBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int option = intent.getIntExtra(Constantes.OPTION_JSON_BROADCAST, 0);
            showProgress(false);
            switch (option) {
                case Constantes.SUCCESS_REQUEST:
                    //clearForm();
                    if (user.imagePath != null && !user.imagePath.isEmpty()) {
                        try {
                            if (intent.hasExtra(Constantes.VALUE_JSON_BROADCAST)) {
                                String jsonObjStr = intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST);

                                JSONObject jsonObject = new JSONObject(jsonObjStr);
                                String _id = jsonObject.getString("Id");

                                HashMap<String, String> params = new HashMap<>();
                                params.put("file", user.imagePath);
                                showProgress(true);
                                String url = String.format("api/User/%s/UpdatePhoto/", _id);
                                CrudIntentService.startActionFormData(RegisterActivity.this, url,
                                        Request.Method.PUT, params);
                                log("VALUE_JSON_BROADCAST: " + jsonObjStr);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        msgSuccess();
                    }
                    break;
                case Constantes.REQUEST_NOT_FOUND:
                    break;
                case Constantes.SUCCESS_FILE_UPLOAD:
                    msgSuccess();
                    break;
                case Constantes.BAD_REQUEST:
                    String error = intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST);
                    try {
                        if (error != null)
                            MetodosPublicos.alertDialog(RegisterActivity.this, (new JSONObject(error).getJSONArray("Error").getString(0)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case Constantes.TIME_OUT_REQUEST:
                    MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                    break;

                case Constantes.NOT_INTERNET:
                case Constantes.SEND_REQUEST:
                    break;

            }
        }
    }

    private void msgSuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this)
                .setTitle("Felicidades")
                .setMessage("Su cuenta ha sido creada exitosamente, ahora verifica tu correo para habilitarla")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //onBackPressed();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra(Constantes.KEY_USERNAME, user.Email);
                        intent.putExtra(Constantes.KEY_PASS, user.Password);
                        startActivity(intent);
                        finish();
                    }
                })
                .setCancelable(false);
        builder.create().show();
    }

    public void back(View view) {
        this.onBackPressed();
    }


    private void verifyWithRecaptcha() {

        SafetyNet.getClient(this).verifyWithRecaptcha("6LcqmCwUAAAAADw9L4nK0EOhee3IX9ZGq-VGUda4")
                .addOnSuccessListener(this,
                        new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                            @Override
                            public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                                // Indicates communication with reCAPTCHA service was
                                // successful.
                                String userResponseToken = response.getTokenResult();
                                if (!userResponseToken.isEmpty()) {
                                    user.ResponseReCaptcha = userResponseToken;
                                    attemptLogin(true);
                                    log("userResponseToken: " + userResponseToken);
                                }
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            // An error occurred when communicating with the
                            // reCAPTCHA service. Refer to the status code to
                            // handle the error appropriately.
                            ApiException apiException = (ApiException) e;
                            int statusCode = apiException.getStatusCode();
                            log("Error: " + CommonStatusCodes
                                    .getStatusCodeString(statusCode));
                        } else {
                            // A different, unknown type of error occurred.
                            logE("Error: " + e.getMessage());
                        }
                    }
                });
    }

    @Override
    public void onStringResult(String action, int option, String response, String url) {
        switch (option) {
            case Constantes.SEND_REQUEST:
                logW("response " + response);
                preferences.setPolicyUrls(response);
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
        }

    }
}

