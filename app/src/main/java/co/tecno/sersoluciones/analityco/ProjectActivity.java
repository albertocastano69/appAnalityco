package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.gson.Gson;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.tecno.sersoluciones.analityco.adapters.project.WizardProjectPagerAdapter;
import co.tecno.sersoluciones.analityco.models.Project;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.steps.ProjectStep1;
import co.tecno.sersoluciones.analityco.steps.ProjectStep2;
import co.tecno.sersoluciones.analityco.steps.ProjectStep3;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.views.CustomViewPager;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

public class ProjectActivity extends BaseActivity {

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
    @BindView(R.id.spinnerBase)
    Spinner spinnerCompanies;

    private int posPage;
    private View mFormView;

    private WizardProjectPagerAdapter mSectionsPagerAdapter;
    private Project project;
    public String idCompany;
    public long maxgeoFence = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Datos del Proyecto");

        mFormView = findViewById(R.id.form);

        mViewPager = findViewById(R.id.register_view_pager);
        mViewPager.setPagingEnabled(false);
        mSectionsPagerAdapter = new WizardProjectPagerAdapter(getSupportFragmentManager(), this);
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
                String title = mSectionsPagerAdapter.getPageTitle(position).toString();

                switch (position) {
                    case 0:
                        mViewPager.setPagingEnabled(false);
                        submitButton.setVisibility(View.GONE);
                        previousButton.setEnabled(false);
                        nextButton.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        mViewPager.setPagingEnabled(false);
                        submitButton.setVisibility(View.GONE);
                        previousButton.setEnabled(true);
                        nextButton.setVisibility(View.VISIBLE);

                        break;
                    case 2:
                        if (fragment instanceof ProjectStep2) {
                            ((ProjectStep2) fragment).updateProject();
                        }
                        mViewPager.setPagingEnabled(false);
                        submitButton.setVisibility(View.VISIBLE);
                        previousButton.setEnabled(true);
                        nextButton.setVisibility(View.GONE);
                        break;
                }
                if (ProjectActivity.this.getSupportActionBar() == null) return;
                ProjectActivity.this.getSupportActionBar().setTitle(title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        MyPreferences preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        fillSpinner(user);
    }

    private void fillSpinner(User user) {
        if (user.Companies != null) {
            for (int i = 0; i < user.Companies.size(); i++) {
                if (user.IsAdmin || user.IsSuperUser) {
                    idCompany = user.Companies.get(0).Id;
                    logW("idCompany: " + idCompany);
                    maxgeoFence = user.Companies.get(i).GeofenceLimit;
                    break;
                }
            }

            spinnerCompanies.setVisibility(View.VISIBLE);
            spinnerCompanies.setAdapter(new ArrayAdapter<>(this, R.layout.simple_spinner_item_project, user.Companies));
            spinnerCompanies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_project);
    }

    @OnClick(R.id.next_button)
    public void submit() {
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof ProjectStep1) {
            ((ProjectStep1) fragment).submitRequest();
        } else if (fragment instanceof ProjectStep3) {
            ((ProjectStep3) fragment).submitRequest();
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
        if (fragment instanceof ProjectStep2) {
            ((ProjectStep2) fragment).submitRequest();
        }
    }

    public void submitCreate() {
        setResult(RESULT_OK);
        finish();
    }

    public void nextPage(int pos) {
        mViewPager.setCurrentItem(pos);
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("Esta seguro de volver atrás y no completar el registro?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProjectActivity.super.finish();
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

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
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
