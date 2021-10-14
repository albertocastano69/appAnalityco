package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.fragments.personal.ContractListPersonalFragment;
import co.tecno.sersoluciones.analityco.fragments.personal.EmployerPersonalListFragment;
import co.tecno.sersoluciones.analityco.fragments.personal.ProjectsPersonalListFragment;
import co.tecno.sersoluciones.analityco.fragments.personal.RequirementPersonalFragment;
import co.tecno.sersoluciones.analityco.fragments.personal.personalGeneralInfoFragmnet;
import co.tecno.sersoluciones.analityco.models.Contract;
import co.tecno.sersoluciones.analityco.models.ContractList;
import co.tecno.sersoluciones.analityco.models.ContractReq;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.Requirements;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 24/03/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class DetailsPersonalActivityTabs extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener,
        RequirementPersonalFragment.OnListFragmentInteractionListener {

    private RequestBroadcastReceiver requestBroadcastReceiver;
    private String fillForm;
    private String fillListProject;
    private String fillListCompany;
    private String fillListContract;
    private int personalId;
    private View mProgressView;
    private ViewPager mViewPager;
    private ArrayList<Requirements> Req;
    private boolean[] options;
    private String imageTransitionName;
    private String birthDate;

    private String claimsPersonal;
    private boolean updateList = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PERSONAL");
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();
        imageTransitionName = extras.getString(Constantes.ITEM_TRANSITION_NAME);
        PersonalList mItem = extras.getParcelable(Constantes.ITEM_OBJ);
        if (extras.containsKey("update_list") && extras.getBoolean("update_list"))
            updateList = true;
        if (mItem != null && mItem.Id != 0) personalId = mItem.Id;
        else personalId = mItem.PersonalCompanyInfoId;

        options = new boolean[3];

        String docNumber = mItem.DocumentNumber;
        String documentType = mItem.DocumentType;
        claimsPersonal = "";

        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator(',');
        otherSymbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###,###", otherSymbols);

        //DecimalFormat formatter = new DecimalFormat("#,###,###");
        String documentNumber = formatter.format(Long.parseLong(docNumber));

        getSupportActionBar().setTitle(mItem.Name + " " + mItem.LastName);
        getSupportActionBar().setTitle(String.format("%s: %s", documentType, documentNumber));
        getSupportActionBar().setSubtitle(mItem.Name + " " + mItem.LastName);

        mProgressView = findViewById(R.id.load);

        fillForm = "";
        fillListCompany = "";
        fillListContract = "";
        fillListProject = "";
        Req = new ArrayList<>();
        mViewPager = findViewById(R.id.container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CrudIntentService.startRequestCRUD(this, Constantes.PERSONAL_PERMISSION + personalId,
                Request.Method.GET, "", "", false);

        ContentValues values = new ContentValues();
        values.put("personalId", personalId);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(this, Constantes.DETAILS_PERSONAL_URL,
                Request.Method.GET, "", paramsQuery, false);

        values = new ContentValues();
        values.put("full", true);
        values.put("personalCompanyInfoId", personalId);
        paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_CONTRACT_PER_OFFLINE_URL,
                Request.Method.GET, "", paramsQuery, true);

//        values = new ContentValues();
//        values.put("personalId", personalId);
//        paramsQuery = HttpRequest.makeParamsInUrl(values);
//        CrudIntentService.startRequestCRUD(this,
//                String.format("api/Personal/%s/Contracts/", personalId),
//                Request.Method.GET, "", paramsQuery, true);
        showProgress(true);

    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_details_personal_tabs);
    }

    private void enableSectionsAdapter() {
        ArrayList<ContractReq> contractLists = new ArrayList<>();
        ArrayList<ObjectList> contractObjectLists = new ArrayList<>();
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(personalGeneralInfoFragmnet.newInstance(fillForm, personalId, claimsPersonal, imageTransitionName));
        mSectionsPagerAdapter.addFrag(ProjectsPersonalListFragment.newInstance(fillListProject, personalId, claimsPersonal));
        mSectionsPagerAdapter.addFrag(EmployerPersonalListFragment.newInstance(fillListCompany, personalId, claimsPersonal));
        if (!fillListContract.isEmpty() && !fillListContract.equals("UNAUTHORIZED")) {
            contractLists = new Gson().fromJson(fillListContract,
                    new TypeToken<ArrayList<ContractReq>>() {
                    }.getType());
            contractObjectLists = new Gson().fromJson(fillListContract,
                    new TypeToken<ArrayList<ObjectList>>() {
                    }.getType());
        }
        mSectionsPagerAdapter.addFrag(ContractListPersonalFragment.newInstance(fillForm, contractObjectLists, personalId, claimsPersonal));
        mSectionsPagerAdapter.addFrag(RequirementPersonalFragment.newInstance(Req, contractLists, birthDate, claimsPersonal));

        // Set up the ViewPager with the sections adapter.
        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        configTabs();
    }

    @SuppressWarnings("ConstantConditions")
    private void configTabs() {

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        Drawable drawable1 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(0).setIcon(drawable1);

        Drawable drawable2 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CITY) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(1).setIcon(drawable2);

        Drawable drawable3 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.DOMAIN) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(2).setIcon(drawable3);

        tabLayout.getTabAt(3).setIcon(R.drawable.ic_icon_contract_tab);
        Drawable drawable5 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.PLAYLIST_CHECK) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(4).setIcon(drawable5);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (updateList)
            setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
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

        mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
        mViewPager.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mViewPager.setVisibility(show ? View.GONE : View.VISIBLE);
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
        if (action.equals(CRUDService.ACTION_REQUEST_SAVE) && url.equals(Constantes.LIST_PERSONAL_URL)) {
            Intent intent = getIntent();
            intent.putExtra("update_list", true);
            finish();
            startActivity(intent);
        } else {
            switch (option) {
                case Constantes.SUCCESS_REQUEST:
                    if (url.equals(Constantes.PERSONAL_PERMISSION + personalId)) {
                        claimsPersonal = jsonObjStr;
                    } else if (url.equals(Constantes.LIST_CONTRACT_PER_OFFLINE_URL) || url.equals(Constantes.LIST_PERSONAL_URL)) {
                    } else {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonObjStr);
                            fillForm = jsonObjStr;
                            fillListCompany = "[]";
                            if (jsonObject.has("Employers"))
                                fillListCompany = (jsonObject.getString("Employers"));
                            fillListProject = "[]";
                            if (jsonObject.has("Projects"))
                                fillListProject = (jsonObject.getString("Projects"));

                            birthDate = (jsonObject.getString("BirthDate"));
                            options[0] = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Constantes.SEND_REQUEST:
                    if (url.equals(Constantes.LIST_CONTRACT_PER_OFFLINE_URL)) {
                        Req = requirementsObjectOffline(jsonObjStr);
                        fillListContract = jsonObjStr;
                        options[1] = true;
                        options[2] = true;
                    }
//                    else if (url.equals("api/Personal/" + personalId + "/Contracts/")) {
//                        fillListContract = jsonObjStr;
//                        options[1] = true;
//                    }
                    confirmDownloadData();
                    break;
                case Constantes.UNAUTHORIZED:
                    if (url.equals(Constantes.DETAILS_PERSONAL_URL)) {
                        fillForm = jsonObjStr;
                        birthDate = "UNAUTHORIZED";
                        fillListCompany = "UNAUTHORIZED";
                        fillListProject = "UNAUTHORIZED";
                        options[0] = true;
                    }
//                    else if (url.equals("api/Personal/" + personalId + "/Contracts/")) {
//                        fillListContract = "UNAUTHORIZED";
//                        options[1] = true;
//                    }
                    else if (url.equals(Constantes.LIST_CONTRACT_PER_OFFLINE_URL)) {
                        Req = null;
                        fillListContract = "UNAUTHORIZED";
                        options[1] = true;
                        options[2] = true;
                    }

                    break;
                case Constantes.NOT_INTERNET:
                case Constantes.BAD_REQUEST:
                case Constantes.TIME_OUT_REQUEST:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Alerta")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            })
                            .setMessage("Sin conexion con el servidor");
                    builder.create().show();
                    break;
            }
        }
    }

    @Override
    public void onListFragmentInteraction(Requirements item) {

        try {
            if (item.DocsJSON == null || item.DocsJSON.isEmpty() || new JSONObject(item.DocsJSON).length() == 0) {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (item.File != null && !item.File.isEmpty()) {
            String rute = item.File;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String[] extension = rute.split("\\.");
            int index = extension.length - 1;
            switch (extension[index]) {
                case "pdf":
                case "jpg":
                case "png":
                    intent.setDataAndType(Uri.parse(Constantes.URL_IMAGES + item.File), "text/html");
                    logW("URL: " + Constantes.URL_IMAGES + item.File);
                    startActivity(intent);
                    break;
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Alerta")
                    .setCancelable(false)
                    .setPositiveButton("OK", null)
                    .setMessage("El documento requerido no ha sido subido");
            builder.create().show();

        }
    }

    private ArrayList<Requirements> requirementsObjectOffline(String ReqJson) {
        ArrayList<Requirements> require = new ArrayList<>();

        if (ReqJson == null || ReqJson.equals("{}")) return require;
        JSONArray jsonArrayObject;
        JSONObject jsonObject;
        JSONArray jsonObjectSs;
        JSONArray jsonObjectReq;
        Requirements requirement;
        String ContractId;
        String ContractNumber;
        String weekDays;
        String maxHour;
        String minHour;
        String minAge;
        String maxAge;
        String contractReview;
        try {
            jsonArrayObject = new JSONArray(ReqJson);
            for (int k = 0; k < jsonArrayObject.length(); k++) {
                ContractId = jsonArrayObject.getJSONObject(k).getString("ContractId");
                ContractNumber = jsonArrayObject.getJSONObject(k).getString("ContractNumber");
                jsonObject = jsonArrayObject.getJSONObject(k).getJSONObject("Reqs");
                weekDays = jsonArrayObject.getJSONObject(k).getString("WeekDays");
                maxHour = jsonArrayObject.getJSONObject(k).getString("MaxHour");
                minHour = jsonArrayObject.getJSONObject(k).getString("MinHour");
                minAge = jsonArrayObject.getJSONObject(k).getString("AgeMin");
                maxAge = jsonArrayObject.getJSONObject(k).getString("AgeMax");
                contractReview = jsonArrayObject.getJSONObject(k).getString("ContractReview");
                try {
                    jsonObjectSs = jsonObject.getJSONArray("SocialSecurity");
                    for (int i = 0; i < jsonObjectSs.length(); i++) {
                        try {
                            requirement = new Gson().fromJson((jsonObjectSs.getJSONObject(i).toString()),
                                    new TypeToken<Requirements>() {
                                    }.getType());
                            requirement.ContractId = ContractId;
                            requirement.ContractNumber = ContractNumber;
                            requirement.IsEntry = false;
                            requirement.WeekDays = weekDays;
                            requirement.MaxHour = maxHour;
                            requirement.MinHour = minHour;
                            requirement.AgeMax = maxAge;
                            requirement.AgeMin = minAge;
                            requirement.ContractReview = contractReview;
                            require.add(requirement);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjectReq = jsonObject.getJSONArray("Certification");
                    for (int j = 0; j < jsonObjectReq.length(); j++) {
                        try {
                            requirement = new Gson().fromJson((jsonObjectReq.getJSONObject(j).toString()),
                                    new TypeToken<Requirements>() {
                                    }.getType());
                            requirement.ContractId = ContractId;
                            requirement.ContractNumber = ContractNumber;
                            requirement.IsEntry = false;
                            requirement.WeekDays = weekDays;
                            require.add(requirement);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjectReq = jsonObject.getJSONArray("Entry");
                    for (int j = 0; j < jsonObjectReq.length(); j++) {
                        try {
                            requirement = new Gson().fromJson((jsonObjectReq.getJSONObject(j).toString()),
                                    new TypeToken<Requirements>() {
                                    }.getType());
                            requirement.ContractId = ContractId;
                            requirement.ContractNumber = ContractNumber;
                            requirement.IsEntry = true;
                            requirement.WeekDays = weekDays;
                            require.add(requirement);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return require;
    }

    private void confirmDownloadData() {
        boolean flag = true;
        for (boolean option : options) {
            flag = option;
            if (!option)
                break;
        }
        if (flag) {
            preferences.setSyncDate(Calendar.getInstance().getTime().getTime());
            showProgress(false);
            enableSectionsAdapter();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.w("ok", "llego de actividad con codigo" + requestCode);
        Log.e("ok", "llego de actividad ok");
        if (resultCode == RESULT_OK) {
            ContentValues values = new ContentValues();
            values.put(Constantes.KEY_SELECT, true);
            String paramsQuery = HttpRequest.makeParamsInUrl(values);
            CRUDService.startRequest(this, Constantes.LIST_PERSONAL_URL,
                    Request.Method.GET, paramsQuery, true);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
