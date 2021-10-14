package co.tecno.sersoluciones.analityco.individualContract;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.BaseActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.models.InfoIndividualContract;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.RequerimentIndividualContract;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log;

public class DetailsSingleContractActivityTabs extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener, RequirementIndividualContractFragment.OnListFragmentInteractionListener{
    private ViewPager mViewPager;
    private String photo,PersonalId, PersonalEmployerInfoId,fillForm,contactInfo,EmployerId,IdContract,Name,LastName,Position,LogoEmployer,NameEmployer,RolEmployer,DocumentTypeEmployer,DocumentNumberEmployer,ProjectName,CityName,StateName;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private boolean[] options;
    private ArrayList<RequerimentIndividualContract>requerimentsUpload;
    private  String ListToRequeriments;
    InfoIndividualContract item = new InfoIndividualContract();
    TextView Contract,DescriptionContract,CreateDateContract,StateContract;
    private View mProgressView;
    private boolean DocumentsCompleted = false;
    int Nationality;
    TabLayout tabLayout;

    @SuppressLint({"SimpleDateFormat", "CutPasteId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Contract = findViewById(R.id.LabelContract);
        DescriptionContract = findViewById(R.id.toolbarCreateDate);
        CreateDateContract = findViewById(R.id.toolbarCreateDate);
        StateContract = findViewById(R.id.toolbarState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        mViewPager = findViewById(R.id.containerContract);
        mProgressView = findViewById(R.id.load);
        Bundle extras = getIntent().getExtras();
        requerimentsUpload = new ArrayList<>();
        options = new boolean[3];
        if (extras == null) finish();
        photo = extras.getString(Constantes.ITEM_PHOTO);
        IdContract = extras.getString(Constantes.ITEM_CONTRACT_ID);
        item = extras.getParcelable(Constantes.ITEM_OBJ);


        PersonalId = item.PersonalId;
        PersonalEmployerInfoId = item.PersonalEmployerInfo;
        EmployerId = item.EmployerId;
        IdContract = item.Id;
        Name = item.Name;
        LastName = item.LastName;
        LogoEmployer = item.LogoEmployer;
        NameEmployer = item.NameEmployer;
        RolEmployer = item.RolEmployer;
        DocumentTypeEmployer = item.DocumentTypeEmployer;
        DocumentNumberEmployer = item.DocumentNumberEmployer;

        fillForm = "";
        contactInfo= "";
        Position = "";
        ProjectName = "";
        ListToRequeriments = "";
        CityName = "";
        StateName = "";
        SelectColorActionBar(item.Descripction);
        StateContract.setText(item.Descripction);
        String Create = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = simpleDateFormat.parse(item.CreateDate);
            Create= format.format(date);
            System.out.println("Numero contrato"+ item.ContractNumber);
            if((!item.ContractNumber.equals("null")) ){
                Contract.setText(String.format("%s %s", "Contrato", item.ContractNumber));
                DescriptionContract.setText(item.Descripction);
            }else{
                Contract.setText("Contrato");
                DescriptionContract.setText("");
            }
            CreateDateContract.setText(Create);

        }catch (ParseException e){
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CrudIntentService.startRequestCRUD(this, Constantes.PERSON_URL+PersonalId,
                Request.Method.GET, "", "", false);
        CrudIntentService.startRequestCRUD(this, Constantes.PERSONAL_EMPLOYER_INFO_URL+PersonalEmployerInfoId,
                Request.Method.GET, "", "", false);
        CrudIntentService.startRequestCRUD(this, Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL+IdContract,
                Request.Method.GET, "", "", true, false);
        if(item.PositionId != 0){
            ContentValues cv = new ContentValues();
            cv.put("Id",item.PositionId);
            String paramsQuery = HttpRequest.makeParamsInUrl(cv);
            CrudIntentService.startRequestCRUD(this, Constantes.POSITION_URL,
                    Request.Method.GET, "", paramsQuery, false);
            CrudIntentService.startRequestCRUD(this, "api/Project/"+item.ProjectId,
                    Request.Method.GET, "", "", false);
        }
        if(!item.ContractCityId.equals("0")){
            CrudIntentService.startRequestCRUD(this, "api/DaneCity/"+item.ContractCityId,
                    Request.Method.GET, "", "", false);
        }
        if(fillForm != null){
            try {
                JSONObject jsonInstance = new JSONObject(fillForm);
                Personal newPersonal = new Gson().fromJson(fillForm,
                        new TypeToken<Personal>() {
                        }.getType());
                log("Name: " + jsonInstance.getString("Name") + " " + jsonInstance.getString("LastName"));
                Nationality = newPersonal.Nationality;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        showProgress(true);

    }

    private void SelectColorActionBar(String descripction) {
        switch (descripction){
            case "INICIADO":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.gray)));
                break;
            case  "SOLICITADO":
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.accent)));
                    break;
            case "APROBADO":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                break;
            case "ANULADO":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.anulado)));
                break;
            case "LIQUIDADO":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bar_undecoded)));
                break;
            case "RECHAZADO PARA CORREGIR":
            case "RECHAZADO":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.expiry)));
                break;
            case "CONTRATADO":
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.purple)));
        }
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_details_singlecontract_tabs);

    }
    @SuppressLint("ClickableViewAccessibility")
    private void enableSectionsAdapter() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(personalGeneralInfoFragmnetSingleContract.newInstance(photo,fillForm,contactInfo,Name,LastName));
        mSectionsPagerAdapter.addFrag(EmployerIndivudialContractListFragment.newInstance(EmployerId,LogoEmployer,NameEmployer,RolEmployer,DocumentTypeEmployer,DocumentNumberEmployer));
        mSectionsPagerAdapter.addFrag(RequirementIndividualContractFragment.newInstance(ListToRequeriments,IdContract,PersonalEmployerInfoId,Nationality,item.Descripction));
        mSectionsPagerAdapter.addFrag(DataForContractFragment.newinstance(item,Position,ProjectName,CityName,StateName));
        // Set up the ViewPager with the sections adapter.
        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        configTabs();
    }
    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("ConstantConditions")
    private void configTabs() {

        tabLayout = findViewById(R.id.tabscontract);
        tabLayout.setupWithViewPager(mViewPager);
        Drawable drawable1 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(0).setIcon(drawable1);

       Drawable drawable2 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.DOMAIN) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(1).setIcon(drawable2);

        Drawable drawable3 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.PLAYLIST_CHECK) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(2).setIcon(drawable3);

        tabLayout.getTabAt(3).setIcon(R.drawable.ic_baseline_text_snippet_24);

        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        if(!DocumentsCompleted){
            tabStrip.getChildAt(3).setVisibility(View.GONE);
            tabStrip.getChildAt(3).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(mViewPager.getCurrentItem() == 2){
                        mViewPager.setCurrentItem(2-1,false);
                        return true;
                    }
                    return false;
                }
            });
        }else {
            tabStrip.getChildAt(3).setVisibility(View.VISIBLE);
            tabStrip.getChildAt(3).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mViewPager.getCurrentItem();
                    return false;
                }
            });
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    protected void onResume() {
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
    public void onStringResult(@Nullable String action, int option, @Nullable String response, @Nullable String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (url.equals(Constantes.PERSON_URL + PersonalId)) {
                    fillForm = response;
                    options[0] = true;
                }
                if (url.equals(Constantes.PERSONAL_EMPLOYER_INFO_URL + PersonalEmployerInfoId)) {
                    contactInfo = response;
                    options[1] = true;
                }
                if (url.equals(Constantes.POSITION_URL)) {
                    try {
                        JSONObject Job = new JSONObject(response);
                        Position = Job.getString("Name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (url.equals("api/Project/" + item.ProjectId)) {
                    try {
                        JSONObject Project = new JSONObject(response);
                        ProjectName = Project.getString("Name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (url.equals("api/DaneCity/" + item.ContractCityId)) {
                    try {
                        JSONObject City = new JSONObject(response);
                        CityName = City.getString("CityName");
                        StateName = City.getString("StateName");
                        System.out.println("City"+CityName);
                        System.out.println("CityCode"+item.ContractCityId);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constantes.SEND_REQUEST:
                if(url.equals(Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL+IdContract)){
                    requerimentsUpload = RequirimentContract(response);
                    ListToRequeriments = response;

                    options[2] = true;
                }
                confirmDownloadData();
                break;
        }
    }
    private void confirmDownloadData() {
        boolean flag = true;
        for (boolean option : options) {
            flag = option;
            if (!option)
                break;
        }
        if (flag) {
            showProgress(false);
            enableSectionsAdapter();
        }
    }
    private ArrayList<RequerimentIndividualContract> RequirimentContract(String response){
        ArrayList<RequerimentIndividualContract> requiere = new ArrayList<>();
        if (response == null || response.equals("{}")) return requiere;
        JSONArray jsonArrayRequeriments;
        JSONObject jsonObjectDocument;
        String jsonFile;
        String DocumentContents;
        RequerimentIndividualContract requerimentIndividualContract;
        try {
            jsonArrayRequeriments = new JSONArray(response);
            for(int i = 0; i < jsonArrayRequeriments.length(); i++){
                jsonObjectDocument = jsonArrayRequeriments.getJSONObject(i).getJSONObject("Document");
                jsonFile = jsonArrayRequeriments.getJSONObject(i).getString("File");
                DocumentContents = jsonObjectDocument.getString("DocumentContents");
                if(DocumentContents.equals("DocumentsForm")){
                    requerimentIndividualContract = new Gson().fromJson((jsonObjectDocument.toString()),RequerimentIndividualContract.class);
                    requerimentIndividualContract.File = jsonFile;
                    requerimentIndividualContract.Name = jsonObjectDocument.getString("Name");
                    requerimentIndividualContract.Abrv = jsonObjectDocument.getString("Abrv");
                    requerimentIndividualContract.Description = jsonObjectDocument.getString("Description");
                    requiere.add(requerimentIndividualContract);
                }
            }
            DocumentsCompleted = requiere.size() >= 6;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requiere;
    }


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
    protected void onStop() {
        super.onStop();
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onListFragmentInteraction(boolean item) {
        LinearLayout tabStrip = ((LinearLayout)tabLayout.getChildAt(0));
        if(item){
            tabStrip.getChildAt(3).setVisibility(View.VISIBLE);
            tabStrip.getChildAt(3).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mViewPager.getCurrentItem();
                    return false;
                }
            });
        }else {
            tabStrip.getChildAt(3).setVisibility(View.GONE);
            tabStrip.getChildAt(3).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
            mViewPager.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(mViewPager.getCurrentItem() == 2){
                        mViewPager.setCurrentItem(2-1,false);
                        return true;
                    }
                    return false;
                }
            });
        }
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
        super.onBackPressed();
    }
}
