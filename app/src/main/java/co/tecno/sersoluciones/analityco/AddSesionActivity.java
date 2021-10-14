package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.gson.Gson;


import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Pattern;

import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.Profile;
import co.tecno.sersoluciones.analityco.receivers.LoginResultReceiver;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.MySingleton;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;

/**
 * A login screen that offers login via email/password.
 * Created by Ser Soluciones SAS on 14/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class AddSesionActivity extends AppCompatActivity implements LoginResultReceiver.Receiver,
        RequestBroadcastReceiver.BroadcastListener {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private LoginResultReceiver mReceiver;
    private MyPreferences preferences;
    private EditText mUsenameView;
    private String username;
    private String password;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Drawable upArrow = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ARROW_LEFT) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        // Set up the login form.
        mUsenameView = findViewById(R.id.user_id);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        preferences = new MyPreferences(this);
        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mReceiver = new LoginResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString(Constantes.KEY_USERNAME);
            password = extras.getString(Constantes.KEY_PASS);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute(username, password);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("ON PAUSE");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }


    /**
     * Metodo para ir a la actividad principal
     */
    private void goMainActivity() {
        preferences.setUserLogin(true);

        Intent intent = new Intent();
        intent.putExtra("username", username);
        setResult(7, intent);
        finish();
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsenameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        username = mUsenameView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsenameView.setError(getString(R.string.error_field_required));
            focusView = mUsenameView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mUsenameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsenameView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {


            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute(username, password);
        }
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
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
    public void onReceiveResult(int resultCode, Bundle resultData) {
        mAuthTask = null;
        showProgress(false);
        switch (resultCode) {
            case Constantes.LOGIN_SUCCESS:

                DBHelper dbHelper = new DBHelper(AddSesionActivity.this);
                dbHelper.deleteAllContractPerson();
                dbHelper.deleteAllContract();
                dbHelper.deleteAllCompany();
                dbHelper.deleteAllProject();
                dbHelper.deleteAllEmployer();
                dbHelper.deleteAllPersonal();

                preferences.setUsername(username);
                CrudIntentService.startRequestCRUD(this, Constantes.USER_INFO_URL,
                        Request.Method.GET, "", "", false);
                showProgress(true);
                break;
            case Constantes.LOGIN_ERROR:
                String error = resultData.getString(Intent.EXTRA_TEXT);
                if (error != null) {
                    if (!error.isEmpty()) {
                        logE(error);
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        mPasswordView.requestFocus();
                        MetodosPublicos.alertDialog(AddSesionActivity.this, error);
                    } else {
                        MetodosPublicos.alertDialog(AddSesionActivity.this, "Sin conexion con el servidor");
                    }
                } else
                    MetodosPublicos.alertDialog(AddSesionActivity.this, "Sin conexion con el servidor");
                break;
        }
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                log("jsonObjStr: " + jsonObjStr);
                preferences.setProfile(jsonObjStr);
                requestImage(jsonObjStr);
                break;
            case Constantes.SEND_REQUEST:
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
                break;
        }
    }

    private void requestImage(String profile) {
        try {
            JSONObject jsonObjectProfile = new JSONObject(profile);
            String avatarUrl = jsonObjectProfile.getString("avatar");
            final String profileName = jsonObjectProfile.getString("Name");
            String url = Constantes.URL_IMAGES + avatarUrl;
            log("avatarUrl: " + avatarUrl);
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
                // Retrieves an image specified by the URL, displays it in the UI.
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                log("Success bitmap profile");
                                saveAvatarImage(bitmap, profileName);
                            }
                        }, 80, 80, ImageView.ScaleType.FIT_CENTER, null,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                MySingleton.getInstance(this).addToRequestQueue(request);
            } else {
                showProgress(false);
                preferences.setAvatarImgPath("");
                createProfile("");
                goMainActivity();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createProfile(String pathImage) throws JSONException {
        String profileStr = preferences.getProfile();
        Profile profile = new Gson().fromJson(profileStr, Profile.class);
        String profileJson = new Gson().toJson(profile);

        JSONArray jsonArray = new JSONArray(preferences.getProfiles());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonProfile = jsonArray.getJSONObject(i);
            if (username.equals(jsonProfile.getString("username"))) {
                onBackPressed();
                return;
            }
        }

        JSONObject jsonObject = new JSONObject(profileJson);
        jsonObject.put(Constantes.KEY_IMAGE_AVATAR_PATH, pathImage);
        jsonArray.put(jsonObject);
        preferences.setProfiles(jsonArray.toString());
    }

    private void saveAvatarImage(Bitmap bitmap, String profileName) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File mypath = new File(directory, String.format("%s.png", profileName));

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            showProgress(false);
            preferences.setAvatarImgPath(mypath.getPath());
            createProfile(mypath.getPath());
            goMainActivity();
        } catch (Exception e) {
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    private class UserLoginTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String url = preferences.getUrlServer() + Constantes.API_TOKEN_AUTH_SERVER;
            String imei = preferences.getDeviceId();
            log("imei: " + imei);
            HttpRequest.refreshToken(url,
                    HttpRequest.makeStringParamsLogin(params[0], params[1]),
                    mReceiver, params[0]
            );
            return false;
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}

