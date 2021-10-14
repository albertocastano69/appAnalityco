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

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.fragments.company.CompanyDetailsFragment;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;

public class DetailsCompanyActivityTabs extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener,
        CompanyDetailsFragment.OnListFragmentInteractionListener {

    private RequestBroadcastReceiver requestBroadcastReceiver;

    private String fillForm;
    private String fillListProjects;
    private String fillListUsers;
    private String companyId;
    private byte[] byteArray;
    private View mProgressView;
    private ViewPager mViewPager;
    private boolean[] options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);

        //String profile = preferences.getProfile();
        //User user = new Gson().fromJson(profile, User.class);
        //Bitmap bitmap = null;
        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();
        companyId = extras.getString("id");
        if (extras.containsKey("image")) {
            byteArray = extras.getByteArray("image");
            //bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }

        if (extras.containsKey("model")) {
            CompanyList companyList = (CompanyList) extras.getSerializable("model");

            String documentType = companyList.DocumentType;
            //labelNit.setText(documentType);

            String docNumber = companyList.DocumentNumber;
            if (documentType.equalsIgnoreCase("NIT")) {
                docNumber = docNumber.substring(0, docNumber.length() - 1) + docNumber.substring(docNumber.length() - 1);
            }
            getSupportActionBar().setTitle(companyList.Name);
            getSupportActionBar().setSubtitle(documentType + " " + docNumber);
            //textNit.setText(docNumber);
            //textName.setText(companyList.Name);
        } else {
            getSupportActionBar().setTitle("EMPRESA ADMINISTRADORA");
        }
        mProgressView = findViewById(R.id.load);

        fillForm = "";
        fillListProjects = "";
        fillListUsers = "";
        options = new boolean[3];
        mViewPager = findViewById(R.id.container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showProgress(true);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_COMPANIES_URL + companyId,
                Request.Method.GET, "", "", false);

        ContentValues values = new ContentValues();
        values.put("company", companyId);
        values.put(Constantes.KEY_SELECT, true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_PROJECTS_BY_COMPANY_URL,
                Request.Method.GET, "", paramsQuery, true);

        values = new ContentValues();
        values.put("id", companyId);
        paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_USERS_BY_COMPANY_URL,
                Request.Method.GET, "", paramsQuery, true);
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_details_company_tabs);
    }

    private void enableSectionsAdapter() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(CompanyDetailsFragment.newInstance(1, fillForm, byteArray, companyId));
        mSectionsPagerAdapter.addFrag(CompanyDetailsFragment.newInstance(2, fillForm, byteArray, companyId));
        mSectionsPagerAdapter.addFrag(CompanyDetailsFragment.newInstance(3, fillListProjects, byteArray, companyId));
        mSectionsPagerAdapter.addFrag(CompanyDetailsFragment.newInstance(4, fillListUsers, byteArray, companyId));

        // Set up the ViewPager with the sections adapter.
        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        configTabs();
    }

    private void configTabs() {

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        Drawable drawable1 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.BANK) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(0).setIcon(drawable1);

        tabLayout.getTabAt(1).setIcon(R.drawable.ic_condiciones);

        Drawable drawable3 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CITY) // provide an icon
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
    public void onListFragmentInteraction() {

        finish();
        startActivity(getIntent());
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
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
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
        if (action.equals(CRUDService.ACTION_REQUEST_SAVE) && url.equals(Constantes.CREATE_COMPANY_URL)) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        } else {
            switch (option) {
                case Constantes.SUCCESS_REQUEST:
                    fillForm = jsonObjStr;
                    options[0] = true;
                    break;
                case Constantes.SEND_REQUEST:
                    fillListProjects = jsonObjStr;
                    options[1] = true;
                    break;
                case Constantes.UPDATE_ADMIN_USERS:
                    fillListUsers = jsonObjStr;
                    options[2] = true;
                    break;
                case Constantes.UNAUTHORIZED:
                    if (url.equals(Constantes.LIST_COMPANIES_URL + companyId)) {
                        fillForm = "UNAUTHORIZED";
                        options[0] = true;
                    } else if (url.equals(Constantes.LIST_PROJECTS_BY_COMPANY_URL)) {
                        fillListProjects = "UNAUTHORIZED";
                        options[1] = true;
                    } else if (url.equals(Constantes.LIST_USERS_BY_COMPANY_URL)) {
                        fillListUsers = "UNAUTHORIZED";
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
            confirmDownloadData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding
        super.onActivityResult(requestCode, resultCode, data);
        Log.w("ok", "llego de actividad con codigo" + requestCode);
        Log.e("ok", "llego de actividad ok");
        if (resultCode == RESULT_OK) {

            ContentValues values = new ContentValues();
            values.put(Constantes.KEY_SELECT, true);
            String paramsQuery = HttpRequest.makeParamsInUrl(values);
            CRUDService.startRequest(this, Constantes.CREATE_COMPANY_URL,
                    Request.Method.GET, paramsQuery, true);

        } else if (requestCode == RESULT_CANCELED) {

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
}
