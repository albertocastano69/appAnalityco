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
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import co.tecno.sersoluciones.analityco.adapters.company.WizardCompanyPagerAdapter;
import co.tecno.sersoluciones.analityco.dialogs.AdminUsersDialogFragment;
import co.tecno.sersoluciones.analityco.dialogs.OfficeDialogFragment;
import co.tecno.sersoluciones.analityco.fragments.company.CreateStepOneFragment;
import co.tecno.sersoluciones.analityco.fragments.company.CreateStepThreeFragment;
import co.tecno.sersoluciones.analityco.fragments.company.CreateStepTwoFragment;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.Company;
import co.tecno.sersoluciones.analityco.models.UserCustomer;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.views.CustomViewPager;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

public class CompanyActivity extends BaseActivity implements OfficeDialogFragment.NoticeDialogListener,
        AdminUsersDialogFragment.AdminUserDialogListener {


    @BindView(R.id.progress)
    ProgressBar mProgressView;
    @BindView(R.id.previous_button)
    Button previousButton;
    @BindView(R.id.next_button)
    Button nextButton;
    @BindView(R.id.submit_button)
    Button submitButton;
    @BindView(R.id.register_view_pager)
    CustomViewPager mViewPager;

    private int posPage;
    private View mFormView;
    private GetJSONBroadcastReceiver getJSONBroadcastReceiver;
    private Context instance;
    private Company company;
    private WizardCompanyPagerAdapter mSectionsPagerAdapter;
    private boolean logoUpload;
    private String companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Datos de la Empresa");
        /*Drawable upArrow = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ARROW_LEFT_BOLD) // provide an icon
                .setColor(Color.BLACK) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        getSupportActionBar().setHomeAsUpIndicator(upArrow);*/
        mFormView = findViewById(R.id.form);

        getJSONBroadcastReceiver = new GetJSONBroadcastReceiver();
        instance = this;
        posPage = 0;
        company = null;
        logoUpload = false;
        //getUsers();

        mViewPager = findViewById(R.id.register_view_pager);
        mViewPager.setPagingEnabled(false);
        mSectionsPagerAdapter = new WizardCompanyPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                log("pos: " + position);
                Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                posPage = position;
                String title = Objects.requireNonNull(mSectionsPagerAdapter.getPageTitle(position)).toString();

                switch (position) {
                    case 0:
                        mViewPager.setPagingEnabled(false);
                        submitButton.setVisibility(View.GONE);
                        previousButton.setEnabled(false);
                        nextButton.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        mViewPager.setPagingEnabled(true);
                        submitButton.setVisibility(View.GONE);
                        previousButton.setEnabled(true);
                        nextButton.setVisibility(View.VISIBLE);
                        if (fragment instanceof CreateStepTwoFragment) {
                            ((CreateStepTwoFragment) fragment).updateCompany();
                        }
                        break;
                    case 2:
                        if (fragment instanceof CreateStepThreeFragment) {
                            //((CreateStepThreeFragment) fragment).setUserList(getJsonArrayUsers());
                            ((CreateStepThreeFragment) fragment).configDatePickers(company);
                        }
                        mViewPager.setPagingEnabled(false);
                        submitButton.setVisibility(View.VISIBLE);
                        previousButton.setEnabled(true);
                        nextButton.setVisibility(View.GONE);
                        break;
                }
                if (CompanyActivity.this.getSupportActionBar() == null) return;
                CompanyActivity.this.getSupportActionBar().setTitle(title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_company);
    }

    @OnClick(R.id.next_button)
    public void submit() {
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof CreateStepOneFragment) {
            ((CreateStepOneFragment) fragment).submitRequest();
        } else if (fragment instanceof CreateStepTwoFragment) {
            ((CreateStepTwoFragment) fragment).sumbitRequest();
        } else if (fragment instanceof CreateStepThreeFragment) {
            ((CreateStepThreeFragment) fragment).sumbitRequest();
        }
    }

    @OnClick(R.id.previous_button)
    public void previousPage() {
        posPage--;
        mViewPager.setCurrentItem(posPage);
    }

    @OnClick(R.id.submit_button)
    public void submitRequest() {
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof CreateStepThreeFragment) {
            ArrayList<UserCustomer> userCustomers = ((CreateStepThreeFragment) fragment).getUsers();
            company.AdminUserJson = new Gson().toJson(userCustomers);
        }

        final String json = new Gson().toJson(company);
        sendFormToServer(json);
    }

    public void nextPage(int pos) {
        mViewPager.setCurrentItem(pos);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @SuppressWarnings("unused")
    public void enableViewPager(boolean enable) {
        mViewPager.setPagingEnabled(enable);
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        LocalBroadcastManager.getInstance(instance).registerReceiver(getJSONBroadcastReceiver,
                intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(getJSONBroadcastReceiver);
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


    public void back(View view) {
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("Esta seguro de volver atrás y no completar el registro?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CompanyActivity.super.finish();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onDialogPositiveClick(BranchOffice branchOffice) {
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof CreateStepOneFragment) {
            ((CreateStepOneFragment) fragment).onDialogPositiveClick(branchOffice);
        }
    }

    @Override
    public void onAdminUserDialogClick(UserCustomer userCustomer) {
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof CreateStepThreeFragment) {
            ((CreateStepThreeFragment) fragment).onAdminUserDialogClick(userCustomer);
        }
    }


    /**
     * Clase especializada en recibir la respuesta de las peticiones enviadas al servidor
     */
    public class GetJSONBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int option = intent.getIntExtra(Constantes.OPTION_JSON_BROADCAST, 0);
            final String action = intent.getAction();
            log("Action: " + action);
            showProgress(false);
            if (CrudIntentService.ACTION_REQUEST_POST.equals(action)) {
                switch (option) {
                    case Constantes.SUCCESS_REQUEST:
                        try {
                            JSONObject jsonCompany = new JSONObject(intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST));
                            companyId = jsonCompany.getString("Id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (company.CompanyLogoPath != null && !company.CompanyLogoPath.isEmpty()) {
                            try {
                                if (intent.hasExtra(Constantes.VALUE_JSON_BROADCAST)) {
                                    String jsonObjStr = intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST);

                                    JSONObject jsonObject = new JSONObject(jsonObjStr);
                                    int _id = jsonObject.getInt("Id");

                                    HashMap<String, String> params = new HashMap<>();
                                    params.put("file", company.CompanyLogoPath);
                                    showProgress(true);
                                    logoUpload = true;
                                    String url = Constantes.UPDATE_LOGO_COMPANY_URL + _id;
                                    CrudIntentService.startActionFormData(CompanyActivity.this, url,
                                            Request.Method.PUT, params);
                                    log("VALUE_JSON_BROADCAST: " + jsonObjStr);
                                    company.CompanyLogoPath = "";
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

                    case Constantes.BAD_REQUEST:
                        String error = intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST);
                        try {
                            if (error != null)
                                MetodosPublicos.alertDialog(CompanyActivity.this, (new JSONObject(error).getJSONArray("Error").getString(0)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Constantes.TIME_OUT_REQUEST:
                        MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                        break;
                }
            } else {
                switch (option) {
                    case Constantes.SEND_REQUEST:
//                        String jsonJarrayStr = intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST);
                        break;
                    case Constantes.BAD_REQUEST:
                        //MetodosPublicos.alertDialog(instance, "Fallo al actualizar la base de datos");
                        break;
                    case Constantes.TIME_OUT_REQUEST:
                        MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                        break;
                    case Constantes.SUCCESS_FILE_UPLOAD:
                        if (logoUpload)
                            msgSuccess();
                        break;
                }
            }
        }
    }

    private void msgSuccess() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Empresa creado satisfactoriamente")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(instance, DetailsCompanyActivityTabs.class);
                        i.putExtra("id", companyId);
                        startActivity(i);
                        finish();
                    }
                });
        builder.create().show();
    }

    private void sendFormToServer(final String json) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Esta seguro de crear este Empresa?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress(true);
                        CrudIntentService.startRequestCRUD(CompanyActivity.this,
                                Constantes.CREATE_COMPANY_URL, Request.Method.POST, json, "", false);

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
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

        mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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


}
