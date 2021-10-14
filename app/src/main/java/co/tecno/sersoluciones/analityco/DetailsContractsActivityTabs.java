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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Calendar;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.fragments.DataFragment;
import co.tecno.sersoluciones.analityco.fragments.contract.ContractGeneralInfoFragmnet;
import co.tecno.sersoluciones.analityco.fragments.contract.PersonalListContractFragment;
import co.tecno.sersoluciones.analityco.fragments.contract.ProjectsContractListFragment;
import co.tecno.sersoluciones.analityco.fragments.contract.RequirementListProjectFragment;
import co.tecno.sersoluciones.analityco.fragments.contract.SubContractCompanyFragment;
import co.tecno.sersoluciones.analityco.fragments.contract.UsersListContractFragment;
import co.tecno.sersoluciones.analityco.models.Contract;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE;


public class DetailsContractsActivityTabs extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener {

    private RequestBroadcastReceiver requestBroadcastReceiver;

    private String fillForm;
    private String fillListProject;
    private String fillListSubContract;
    private String fillListUsers;
    private String fillListRequirement;
    private String fillListPersonal;
    private String ClaimsContract;
    private String contractId;
    private View mProgressView;
    private ViewPager mViewPager;
    private String startDate;
    private String finishDate;
    private String contractType;
    private int isActive;
    private String fillCompanyAdmin;
    private boolean isRegister;
    private String fillImages;
    private boolean[] options;
    private String imageTransitionName;
    private boolean flagRestart = false;
    public DataFragment dataFragment;
    private String companyInfoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PROYECTO");

        isRegister = true;
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();

        if (extras.containsKey("resetNewPersonal"))
            flagRestart = extras.getBoolean("resetNewPersonal");

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);

        ObjectList mItem = extras.getParcelable(Constantes.ITEM_OBJ);
        contractId = mItem.Id;
        isRegister = mItem.IsRegister;
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

        fillForm = "";
        fillListProject = "";
        fillListUsers = "";
        fillListPersonal = "";
        fillListRequirement = "";
        ClaimsContract = "";
        startDate = "";
        finishDate = "";
        fillCompanyAdmin = "";
        contractType = "";
        fillListSubContract = "";
        options = new boolean[7];

        mViewPager = findViewById(R.id.container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        showProgress(true);

        CrudIntentService.startRequestCRUD(this, Constantes.CONTRACT_PERMISSION + contractId,
                Request.Method.GET, "", "", false);

    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_details_contract_tabs);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logE("ON DESTROY");
        if (dataFragment != null) {
            // store the data in the fragment
//            dataFragment.setData("");
//            dataFragment.setClaims("");
//            dataFragment.setCompanyProjectAdmin("");
//            dataFragment.setProjectList("");
//            dataFragment.setUserProjectList("");
//            dataFragment.setPersonalList("");
//            dataFragment.setRequirementDetails("");
//            FragmentManager fm = getSupportFragmentManager();
//            fm.beginTransaction().remove(dataFragment).commit();
        }
    }

    private void enableSectionsAdapter() {
        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        dataFragment = (DataFragment) fm.findFragmentByTag("detailsContract");

        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new DataFragment();
            fm.beginTransaction().add(dataFragment, "detailsContract").commit();
            // load the data from the web
            dataFragment.setData(fillForm);
            dataFragment.setClaims(ClaimsContract);
            dataFragment.setCompanyProjectAdmin(fillCompanyAdmin);
            dataFragment.setProjectList(fillListProject);
            dataFragment.setUserProjectList(fillListUsers);
            dataFragment.setPersonalList(fillListPersonal);
            dataFragment.setRequirementDetails(fillListRequirement);
        } else {
            dataFragment.setData(fillForm);
            dataFragment.setClaims(ClaimsContract);
            dataFragment.setCompanyProjectAdmin(fillCompanyAdmin);
            dataFragment.setProjectList(fillListProject);
            dataFragment.setUserProjectList(fillListUsers);
            dataFragment.setPersonalList(fillListPersonal);
            dataFragment.setRequirementDetails(fillListRequirement);
        }

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(ContractGeneralInfoFragmnet.newInstance(contractId, isActive, fillImages, imageTransitionName));
        mSectionsPagerAdapter.addFrag(ProjectsContractListFragment.newInstance(contractId, isRegister, !fillListProject.equals("UNAUTHORIZED")));
        mSectionsPagerAdapter.addFrag(UsersListContractFragment.newInstance(contractId, finishDate, !fillListUsers.equals("UNAUTHORIZED")));
        mSectionsPagerAdapter.addFrag(PersonalListContractFragment.newInstance(companyInfoId, contractId, startDate, finishDate, contractType, !fillListPersonal.equals("UNAUTHORIZED")));
        mSectionsPagerAdapter.addFrag(RequirementListProjectFragment.newInstance(contractId, !fillListRequirement.equals("UNAUTHORIZED")));
        mSectionsPagerAdapter.addFrag(SubContractCompanyFragment.newInstance(fillListSubContract, contractId, isRegister));

        // Set up the ViewPager with the sections adapter.
        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(6);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        if (flagRestart) {
            mViewPager.setCurrentItem(3);
            flagRestart = false;
        }
        configTabs();
    }

    @SuppressWarnings("ConstantConditions")
    private void configTabs() {

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        /*Drawable drawable1 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.LIBRARY_BOOKS) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(0).setIcon(drawable1);*/
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_icon_contract_tab);


        Drawable drawable2 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CITY) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(1).setIcon(drawable2);

        Drawable drawable3 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(2).setIcon(drawable3);

        Drawable drawable4 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(3).setIcon(drawable4);
        Drawable drawable5 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.PLAYLIST_CHECK) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(4).setIcon(drawable5);
        Drawable drawable6 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.DOMAIN) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(5).setIcon(drawable6);
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
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_DELETE);
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
        if (action.equals(CRUDService.ACTION_REQUEST_SAVE) && url.equals(Constantes.LIST_CONTRACTS_URL)) {
            Intent intent = getIntent();
            finish();
            if (flagRestart) intent.putExtra("resetNewPersonal", true);
            startActivity(intent);
        } else {
            switch (option) {
                case Constantes.SUCCESS_REQUEST:
                    if (url.equals(Constantes.CONTRACT_PERMISSION + contractId)) {
                        ClaimsContract = jsonObjStr;
                        permission();
                    } else if (url.equals(Constantes.LIST_CONTRACTS_URL + contractId)) {
                        fillForm = jsonObjStr;

                        Contract contract = new Gson().fromJson(jsonObjStr,
                                new TypeToken<Contract>() {
                                }.getType());

                        isRegister = contract.IsRegister;
                        startDate = contract.StarDate;
                        finishDate = contract.FinishDate;
                        contractType = contract.ValueContractType;
                        companyInfoId = contract.CompanyInfoParentId;
                        getSupportActionBar().setSubtitle(contract.ContractNumber);
                        getSupportActionBar().setTitle(contract.ContractReview);
                        options[0] = true;
                    }
                    break;
                case Constantes.SEND_REQUEST:
                    if (url.equals("api/Contract/" + contractId + "/Project/")) {
                        fillListProject = jsonObjStr;
                        options[1] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/Users/")) {
                        fillListUsers = jsonObjStr;
                        options[2] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/PersonalInfo/")) {
                        fillListPersonal = jsonObjStr;
                        options[3] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/Requirements/")) {
                        // fillListRequirement(jsonObjStr);
                        fillListRequirement = jsonObjStr;
                        options[4] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/SubContractor/")) {
                        fillListSubContract = jsonObjStr;
                        options[5] = true;
                    } else if (url.equals(Constantes.IMAGES_CONTRACT_URL)) {
                        fillImages = jsonObjStr;
                        options[6] = true;
                    }
                    confirmDownloadData();
                    break;
                case Constantes.UPDATE_ADMIN_USERS:
                    break;
                case Constantes.UNAUTHORIZED:
                    if (url.equals("api/Contract/" + contractId + "/Project/")) {
                        fillListProject = "UNAUTHORIZED";
                        options[1] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/Users/")) {
                        fillListUsers = "UNAUTHORIZED";
                        options[2] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/PersonalInfo/")) {
                        fillListPersonal = "UNAUTHORIZED";
                        options[3] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/Requirements/")) {
                        // fillListRequirement(jsonObjStr);
                        fillListRequirement = "UNAUTHORIZED";
                        options[4] = true;
                    } else if (url.equals("api/Contract/" + contractId + "/SubContractor/")) {
                        fillListSubContract = "UNAUTHORIZED";
                        options[5] = true;
                    } else if (url.equals(Constantes.IMAGES_CONTRACT_URL)) {
                        fillImages = jsonObjStr;
                        options[6] = true;
                    }
                    confirmDownloadData();
                    break;
                case Constantes.NOT_INTERNET:
                case Constantes.BAD_REQUEST:
                case Constantes.TIME_OUT_REQUEST:
                     /*   if (jsonObjStr != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonObjStr);
                            if (!url.equals(Constantes.LIST_AUTOPOSITION_URL)) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                        .setTitle("Alerta")
                                        .setCancelable(false)
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
//                                            finish();
                                            }
                                        })
                                        .setMessage("Sin conexion con el servidor");
                                if (action.equals(CrudIntentService.ACTION_REQUEST_DELETE))
                                    builder.setMessage(jsonObject.getString("Error"));
                                builder.create().show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }*/
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
        CrudIntentService.startRequestCRUD(this, Constantes.IMAGES_CONTRACT_URL,
                Request.Method.GET, "", "", true);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_CONTRACTS_URL + contractId,
                Request.Method.GET, "", "", false);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_CONTRACTS_URL + contractId + "/Project/",
                Request.Method.GET, "", "", true);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_CONTRACTS_URL + contractId + "/Users/",
                Request.Method.GET, "", "", true);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_CONTRACTS_URL + contractId + "/PersonalInfo/",
                Request.Method.GET, "", "", true);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_CONTRACTS_URL + contractId + "/Requirements/",
                Request.Method.GET, "", "", true);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_CONTRACTS_URL + contractId + "/SubContractor/",
                Request.Method.GET, "", "", true);
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
        // Check which request we're responding
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("ok", "llego de actividad con codigo" + requestCode);
        Log.e("ok", "llego de actividad ok");
        if (resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                flagRestart = data.getBooleanExtra("newUser", false);
            }
            reloadData();
        }
    }

    public void reloadData() {
        ContentValues values = new ContentValues();
        values.put(Constantes.KEY_SELECT, true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CRUDService.startRequest(this, Constantes.LIST_CONTRACTS_URL,
                Request.Method.GET, paramsQuery, true);
    }
}