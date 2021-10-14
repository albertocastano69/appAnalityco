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
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.google.gson.Gson;


import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.adapters.project.ProjectPagerAdapter;
import co.tecno.sersoluciones.analityco.fragments.DataFragment;
import co.tecno.sersoluciones.analityco.fragments.project.CompanyListProjectFragment;
import co.tecno.sersoluciones.analityco.fragments.project.ContractListProjectFragment;
import co.tecno.sersoluciones.analityco.fragments.project.LinkedCompaniesListProjectFragment;
import co.tecno.sersoluciones.analityco.fragments.project.PersonalListProjectFragment;
import co.tecno.sersoluciones.analityco.fragments.project.UsersListProjectFragment;
import co.tecno.sersoluciones.analityco.fragments.project.projectGeneralInfoFragmnet;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

public class DetailsProjectActivityTabs extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener {

    private RequestBroadcastReceiver requestBroadcastReceiver;

    private String fillForm;
    private String fillListCompany;
    //    private String fillListContract;
    private String fillListUsers;
    private String fillListStage;
    //    public String fillListPersonal;
    private String filllListLinkedCompanies;
    private String ClaimsProject;
    private String projectId;
    private View mProgressView;
    private ViewPager mViewPager;
    private int process;
    private String finishDate;
    private String address;
    private int isActive;
    private String fillCompanyAdmin;
    private boolean[] options;
    private String imageTransitionName;
    public DataFragment dataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PROYECTO");
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();
        ProjectList mItem = extras.getParcelable(Constantes.ITEM_OBJ);
        projectId = mItem.ProjectId;
        if (projectId == null) projectId = mItem.Id;
        imageTransitionName = extras.getString(Constantes.ITEM_TRANSITION_NAME);
        isActive = 0;
        if (mItem.IsActive) {
            if (mItem.Expiry)
                isActive = 1;
        } else {
            isActive = 2;
        }
        getSupportActionBar().setTitle(mItem.Name);
        getSupportActionBar().setSubtitle(mItem.ProjectNumber);
        mProgressView = findViewById(R.id.load);
        options = new boolean[2];

        fillForm = "";
        fillListCompany = "";
        fillListUsers = "";
        fillListStage = "";
        ClaimsProject = "";
        finishDate = "";
        fillCompanyAdmin = "";
        if (mItem.IsActive) {
            if (mItem.Expiry) {
            }
        } else {
        }
        address = mItem.Address;
        mViewPager = findViewById(R.id.container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showProgress(true);

        CrudIntentService.startRequestCRUD(this, Constantes.PROJECT_PERMISSION + projectId,
                Request.Method.GET, "", "", false);
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_details_project_tabs);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (dataFragment != null) {
            // store the data in the fragment
//            dataFragment.setData("");
//            dataFragment.setClaims("");
//            dataFragment.setCompanyProjectAdmin("");
//            dataFragment.setCompanyProjectList("");
//            dataFragment.setUserProjectList("");
//            dataFragment.setObjectList("");
//            dataFragment.setProjectStages("");
//            dataFragment.setPersonalList("");
//            dataFragment.setLinkedCompany("");
//            FragmentManager fm = getSupportFragmentManager();
//            fm.beginTransaction().remove(dataFragment).commit();
        }
    }

    private void enableSectionsAdapter() {

        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        dataFragment = (DataFragment) fm.findFragmentByTag("detailsProject");

        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment, "detailsProject").commit();
            // load the data from the web
            dataFragment.setData(fillForm);
            dataFragment.setClaims(ClaimsProject);
            dataFragment.setCompanyProjectAdmin(fillCompanyAdmin);
            dataFragment.setCompanyProjectList(fillListCompany);
            dataFragment.setUserProjectList(fillListUsers);
            dataFragment.setProjectStages(fillListStage);
            dataFragment.setLinkedCompany(filllListLinkedCompanies);
        } else {
            dataFragment.setData(fillForm);
            dataFragment.setClaims(ClaimsProject);
            dataFragment.setCompanyProjectAdmin(fillCompanyAdmin);
            dataFragment.setCompanyProjectList(fillListCompany);
            dataFragment.setUserProjectList(fillListUsers);
            dataFragment.setProjectStages(fillListStage);
            dataFragment.setLinkedCompany(filllListLinkedCompanies);
        }

        ProjectPagerAdapter mSectionsPagerAdapter = new ProjectPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(projectGeneralInfoFragmnet.newInstance(projectId, address, isActive, imageTransitionName));
        mSectionsPagerAdapter.addFrag(CompanyListProjectFragment.newInstance(projectId));
        mSectionsPagerAdapter.addFrag(LinkedCompaniesListProjectFragment.newInstance(projectId));
        mSectionsPagerAdapter.addFrag(UsersListProjectFragment.newInstance(projectId, finishDate, !fillListUsers.equals("UNAUTHORIZED")));
//        mSectionsPagerAdapter.addFrag(ContractListProjectFragment.newInstance(projectId, !fillListContract.equals("UNAUTHORIZED")));
//        mSectionsPagerAdapter.addFrag(PersonalListProjectFragment.newInstance(projectId, !fillListPersonal.equals("UNAUTHORIZED")));

        // Set up the ViewPager with the sections adapter.
        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        configTabs();
    }

    @SuppressWarnings("ConstantConditions")
    private void configTabs() {

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        Drawable drawable1 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CITY) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(0).setIcon(drawable1);

        Drawable drawable2 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.BANK) // provide an icon
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

        Drawable drawable4 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(3).setIcon(drawable4);

//        tabLayout.getTabAt(4).setIcon(R.drawable.ic_icon_contract_tab);
//
//        Drawable drawable6 = MaterialDrawableBuilder.with(this) // provide a context
//                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT) // provide an icon
//                .setColor(Color.WHITE) // set the icon color
//                .setToActionbarSize() // set the icon size
//                .build();
//        tabLayout.getTabAt(5).setIcon(drawable6);
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
        //progressDialog.dismiss();
        if (action.equals(CRUDService.ACTION_REQUEST_SAVE) && url.equals(Constantes.LIST_PROJECTS_URL)) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        } else {
            switch (option) {
                case Constantes.SUCCESS_REQUEST:

                    Log.e("onStringResult", url);
                    if (process == 0 && url.equals(Constantes.LIST_PROJECTS_URL + projectId)) {
                        try {
                            final JSONObject project = new JSONObject(jsonObjStr);
                            fillForm = jsonObjStr;
                            fillListCompany = project.getString("JoinCompanies");
//                            String contract = project.getString("ContractIds");
//                            String personal = project.getString("PersonalIds");
                            fillListStage = project.getString("ProjectStageArray");
                            finishDate = project.getString("FinishDate");
                            CompanyProject comp = new CompanyProject();
                            comp.DocumentNumber = project.getString("CompanyDocNum");
                            comp.Name = project.getString("CompanyName");
                            comp.Logo = project.getString("CompanyLogo");
                            filllListLinkedCompanies = project.getString("EmployerIds");

                            fillCompanyAdmin = new Gson().toJson(comp);

                            //String strOfInts = contract.replaceAll("\\[|\\]|\\s", ",");
//                            String strOfInts = contract.replaceAll("\\[|]|\\s", "");
//                            if (!strOfInts.isEmpty()) {
//                                ContentValues values = new ContentValues();
//                                values.put("ids", strOfInts);
//                                values.put("projectId", projectId);
//                                String paramsQuery = HttpRequest.makeParamsInUrl(values);
//
//                                CrudIntentService.startRequestCRUD(DetailsProjectActivityTabs.this, Constantes.CONTRACT_BY_ARRAYIDS,
//                                        Request.Method.GET, "", paramsQuery, true);
//                            } else
//                            options[0] = true;


                            //strOfInts = personal.replaceAll("\\[|\\]|\\s", ",");
//                            strOfInts = personal.replaceAll("\\[|]|\\s", "");
//                            if (!strOfInts.isEmpty()) {
//                                ContentValues valuespersonal = new ContentValues();
//                                valuespersonal.put("ids", strOfInts);
//                                String paramsQuery = HttpRequest.makeParamsInUrl(valuespersonal);
//                                CrudIntentService.startRequestCRUD(DetailsProjectActivityTabs.this, Constantes.PERSONAL_BY_ARRAYIDS,
//                                        Request.Method.GET, "", paramsQuery, true);
//                            } else
//                            options[1] = true;

                            String strOfInts = filllListLinkedCompanies.replaceAll("\\[|]|\\s", "");
                            if (!strOfInts.isEmpty()) {
                                ContentValues valuesEmployer = new ContentValues();
                                valuesEmployer.put("ids", strOfInts);
                                String paramsQuery = HttpRequest.makeParamsInUrl(valuesEmployer);
                                CrudIntentService.startRequestCRUD(DetailsProjectActivityTabs.this, Constantes.EMPLOYER_BY_ARRAYIDS,
                                        Request.Method.GET, "", paramsQuery, true);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        process = 1;

                    } else if (process == 0 && url.equals(Constantes.PROJECT_PERMISSION + projectId)) {

                        ClaimsProject = jsonObjStr;
                        permission();
                    }
//                    else if (url.equals(Constantes.CONTRACT_BY_ARRAYIDS)) {
//                        fillListContract = jsonObjStr;
//                    }

                    break;
                case Constantes.SEND_REQUEST:
                    switch (url) {
//                        case Constantes.CONTRACT_BY_ARRAYIDS: {
//                            fillListContract = jsonObjStr;
//                            options[0] = true;
//                            break;
//                        }
//                        case Constantes.PERSONAL_BY_ARRAYIDS: {
//                            fillListPersonal = jsonObjStr;
//                            options[1] = true;
//                            break;
//                        }
                        case Constantes.EMPLOYER_BY_ARRAYIDS: {
                            filllListLinkedCompanies = jsonObjStr;
                            options[0] = true;
                            break;
                        }
                        default: {
                            fillListUsers = jsonObjStr;
                            options[1] = true;
                            break;
                        }
                    }
                    confirmDownloadData();
                    break;
                case Constantes.UNAUTHORIZED:

//                    if (url.equals(Constantes.CONTRACT_BY_ARRAYIDS)) {
//                        fillListContract = "UNAUTHORIZED";
//                        options[0] = true;
//                    }
//                    if (url.equals(Constantes.PERSONAL_BY_ARRAYIDS)) {
//                        fillListPersonal = "UNAUTHORIZED";
//                        options[1] = true;
//                    }
                    if (url.equals(Constantes.EMPLOYER_BY_ARRAYIDS)) {
                        filllListLinkedCompanies = "UNAUTHORIZED";
                        options[0] = true;
                    }
                    if (url.equals(Constantes.LIST_PROJECTS_URL + projectId + "/Users/")) {
                        fillListUsers = "UNAUTHORIZED";
                        options[1] = true;
                    }
                    confirmDownloadData();
                    break;
                case Constantes.UPDATE_ADMIN_USERS:
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

    private void permission() {

        CrudIntentService.startRequestCRUD(this, Constantes.LIST_PROJECTS_URL + projectId,
                Request.Method.GET, "", "", false);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_PROJECTS_URL + projectId + "/Users/",
                Request.Method.GET, "", "", true);

    }

    private void confirmDownloadData() {
        boolean flag = true;
        logW("options 0 " + options[0]);
        logW("options 1 " + options[1]);

        for (boolean option : options) {
            flag = option;
            logW("test " + flag);
            if (!option)
                break;
        }
        if (flag) {
            showProgress(false);
            enableSectionsAdapter();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.w("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == RESULT_OK) {
            reloadData();
        }

    }

    public void reloadData() {
        ContentValues values = new ContentValues();
        values.put(Constantes.KEY_SELECT, true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CRUDService.startRequest(this, Constantes.LIST_PROJECTS_URL,
                Request.Method.GET, paramsQuery, true);
    }


}