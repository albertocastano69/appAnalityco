package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.OnClick;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.adapters.UserProfileAdapter;
import co.tecno.sersoluciones.analityco.dialogs.ChangePasswordDialogFragment;
import co.tecno.sersoluciones.analityco.dialogs.EditProfileDialogFragment;
import co.tecno.sersoluciones.analityco.models.Profile;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.MySingleton;
import co.tecno.sersoluciones.analityco.views.RevealBackgroundView;

import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;


/**
 * Created by Ser Soluciones SAS on 01/09/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class UserProfileActivity extends BaseActivity implements RevealBackgroundView.OnStateChangeListener,
        ChangePasswordDialogFragment.NoticeDialogListener, RequestBroadcastReceiver.BroadcastListener,
        EditProfileDialogFragment.EditProfileDialogListener {

    private static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

    @BindView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @BindView(R.id.rvUserProfile)
    RecyclerView rvUserProfile;
    @BindView(R.id.fab_settings)
    FloatingActionButton fabSettings;

    private UserProfileAdapter userPhotosAdapter;
    private MyPreferences preferences;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private User user;
    private View mLoginFormView;
    private View mProgressView;

    public static void startUserProfileFromLocation(int[] startingLocation, Activity startingActivity) {
        Intent intent = new Intent(startingActivity, UserProfileActivity.class);
        intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = new MyPreferences(this);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        /*RelativeLayout layout = new RelativeLayout(this);
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.addView(progressBar, params);
        setContentView(layout);*/

        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);
//        logW(profile);

        mLoginFormView = findViewById(R.id.form);
        mProgressView = findViewById(R.id.login_progress);
        setupUserProfileGrid();
        setupRevealBackground(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //NavUtils.navigateUpFromSameTask(this)
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("ON RESUME");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("ON PAUSE");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_user_profile);
    }

    private void setupRevealBackground(Bundle savedInstanceState) {
        vRevealBackground.setOnStateChangeListener(this);
        if (savedInstanceState == null) {
            final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
            vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                    vRevealBackground.startFromLocation(startingLocation);
                    return true;
                }
            });
        } else {
            //userPhotosAdapter.setLockedAnimations(true);
            vRevealBackground.setToFinishedFrame();
        }
    }

    @OnClick(R.id.fab_settings)
    public void popupProfile() {
        PopupMenu popup = new PopupMenu(this, fabSettings);
        popup.getMenuInflater()
                .inflate(R.menu.menu_user_settings, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_change_password:
                        alertDialogChangePass();
                        break;
                    case R.id.action_edit_profile:
                        /*DialogFragment dialog = new EditProfileDialogFragment().newInstance(user);
                        dialog.setCancelable(false);
                        dialog.show(getSupportFragmentManager(), "EditProfile");*/

                        /*new PhotoSer.ActivityBuilder()
                                .setDetectFace(true)
                                .start(UserProfileActivity.this);*/

                     /*   Intent intent = new Intent(UserProfileActivity.this, FaceTrackerActivity.class);
                        Bundle bundle = new Bundle();
                        PhotoSerOptions photoSerOptions = new PhotoSerOptions();
                        photoSerOptions.setDetectFace(true);
                        bundle.putParcelable(PHOTO_SER_EXTRA_OPTIONS, photoSerOptions);
                        intent.putExtra(PHOTO_SER_EXTRA_BUNDLE, bundle);
                        startActivityForResult(intent, PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE);*/
                        startFaceDectectorActivity();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(true)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    String pathImage = data.getStringExtra(URI_IMAGE_KEY);
                    HashMap<String, String> params = new HashMap<>();
                    params.put("file", pathImage);
                    showProgress(true);
                    CrudIntentService.startActionFormData(UserProfileActivity.this, Constantes.UPDATE_PHOTO_PROFILE_URL,
                            Request.Method.PUT, params);
                    break;
                default:
                    break;
            }
        }
    }

    private void alertDialogChangePass() {
        DialogFragment dialog = new ChangePasswordDialogFragment().newInstance();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "UserChangePass");
    }

    private void setupUserProfileGrid() {
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvUserProfile.setLayoutManager(layoutManager);
    }

    @Override
    public void onStateChange(int state) {
        log("onStateChange " + state);

        if (RevealBackgroundView.STATE_FINISHED == state) {
            MyPreferences preferences = new MyPreferences(this);
            rvUserProfile.setVisibility(View.VISIBLE);
            user.imagePath = preferences.getAvatarImgPath();
            userPhotosAdapter = new UserProfileAdapter(this, user);
            rvUserProfile.setAdapter(userPhotosAdapter);
            vRevealBackground.setVisibility(View.GONE);
        } else {
            rvUserProfile.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDialogPositiveClick(JSONObject jsonObject) {
        logW(jsonObject.toString());
        showProgress(true);
        CrudIntentService.startRequestCRUD(this, Constantes.USER_CHANGE_PASSWORD_URL,
                Request.Method.POST, jsonObject.toString(), "", false);
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
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        showProgress(false);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (action.equals(CrudIntentService.ACTION_REQUEST_POST))
                    msgSuccess();
                else {
                    showProgress(true);
                    preferences.setProfile(jsonObjStr);
                    requestImage(jsonObjStr);
                }
                break;
            case Constantes.SEND_REQUEST:
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:

                CrudIntentService.startRequestCRUD(this, Constantes.USER_INFO_URL,
                        Request.Method.GET, "", "", false);
                showProgress(true);
                //msgSuccess();
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:

                try {
                    if (jsonObjStr != null && jsonObjStr.equals("{\"Succeeded\":false,\"Errors\":[{\"Code\":\"PasswordMismatch\",\"Description\":\"PasswordMismatch\"}]}")) {
                        MetodosPublicos.alertDialog(this, "La contraseña actual ingresada es incorrecta");
                    }else{
                        MetodosPublicos.alertDialog(this, (new JSONObject(jsonObjStr).getJSONArray("Errors").getString(0)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void requestImage(String profile) {
        try {
            JSONObject jsonObjectProfile = new JSONObject(profile);
            String avatarUrl = jsonObjectProfile.getString("avatar");
            final String profileId = jsonObjectProfile.getString("sub");
            final String profileUsername = jsonObjectProfile.getString("username");
            String url = Constantes.URL_IMAGES + avatarUrl;
            log("avatarUrl: " + avatarUrl);
            if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
                // Retrieves an image specified by the URL, displays it in the UI.
                ImageRequest request = new ImageRequest(url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bitmap) {
                                log("Success bitmap profile");
                                try {
                                    saveAvatarImage(bitmap, profileId, profileUsername);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
                createProfile("", profileUsername);
                showProgress(false);
                finish();
                startActivity(getIntent());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveAvatarImage(Bitmap bitmap, String userId, String username) throws JSONException {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("profile", Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        JSONArray jsonArray = new JSONArray(preferences.getProfiles());
        String namePhoto;
        if (jsonArray.length() == 1)
            namePhoto = "thumbnail.png";
        else if (jsonArray.getJSONObject(0).getString("username").equals(username))
            namePhoto = "thumbnail.png";
        else
            namePhoto = String.format("%s.png", userId);

        File mypath = new File(directory, namePhoto);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(mypath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            showProgress(false);
            preferences.setAvatarImgPath(mypath.getPath());
            createProfile(mypath.getPath(), username);
            showProgress(false);
            finish();
            startActivity(getIntent());
        } catch (Exception e) {
            Log.e("SAVE_IMAGE", e.getMessage(), e);
        }
    }

    private void createProfile(String pathImage, String username) throws JSONException {
        String profileStr = preferences.getProfile();
        Profile profile = new Gson().fromJson(profileStr, Profile.class);
        String profileJson = new Gson().toJson(profile);

        JSONArray jsonArray = new JSONArray(preferences.getProfiles());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonProfile = jsonArray.getJSONObject(i);
            if (username.equals(jsonProfile.getString("username"))) {
                JSONObject jsonObject = new JSONObject(profileJson);
                jsonObject.put(Constantes.KEY_IMAGE_AVATAR_PATH, pathImage);
                jsonArray.put(i, jsonObject);
                break;
            }
        }

//        JSONObject jsonObject = new JSONObject(profileJson);
//        jsonObject.put(Constantes.KEY_IMAGE_AVATAR_PATH, pathImage);
//        jsonArray.put(jsonObject);
        preferences.setProfiles(jsonArray.toString());
    }

    private void msgSuccess() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Perfil actualizado exitosamente");
        alertDialog.create().show();
    }

    @Override
    public void onDialogEditProfileClick(JSONObject jsonObject) {
        logW(jsonObject.toString());
    }
}
