package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.fragments.enrollment.EnrollmentFragmnet;
import co.tecno.sersoluciones.analityco.fragments.enrollment.EnrollmentNewPersona;
import co.tecno.sersoluciones.analityco.fragments.personal.PersonalContractFragmentEnroll;
import co.tecno.sersoluciones.analityco.fragments.personal.RequirementsPersonal;
import co.tecno.sersoluciones.analityco.fragments.personal.RequirementsPersonalOtherContract;
import co.tecno.sersoluciones.analityco.models.ContractList;
import co.tecno.sersoluciones.analityco.models.ContractType;
import co.tecno.sersoluciones.analityco.models.InfoUserScan;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 01/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class PersonalAddContract extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener,
        RequirementsPersonal.OnUserInfo,
        PersonalContractFragmentEnroll.OnAddPersonalContract {

    // UI references.
    @BindView(R.id.continue_enroller)
    Button continueEnroller;
    @BindView(R.id.previous_button)
    Button previousButton;
    @BindView(R.id.name_project)
    TextView nameProjet;
    @BindView(R.id.name_step)
    TextView nameStep;
    @BindView(R.id.layout_project)
    LinearLayout layoutProject;
    @BindView(R.id.layout_steps)
    LinearLayout layoutSteps;
    @BindView(R.id.text_name)
    public TextView text_name;
    @BindView(R.id.text_sub_name)
    public TextView text_sub_name;
    @BindView(R.id.card_view_detail)
    public CardView card_view_detail;
    @BindView(R.id.logo)
    public ImageView logo_imag;
    @BindView(R.id.text_validity)
    public TextView text_validity;
    @BindView(R.id.btn_edit)
    public ImageView edit;
    @BindView(R.id.controls)
    public LinearLayout Buttons;

    private int userfound = 0;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private InfoUserScan newUser;
    private FragmentTransaction fragmentTransaction;
    private boolean createdPersonal = true;//false
    private String idContract = "";
    private String projectId;
    private String stepId;
    private int editPersonal = 0;
    private int requirement = 0;

    private Personal personarReturn;

    private Personal personal;
    //    private ProgressDialog progress;
    private ArrayList<ContractList> contratos;
    private int personalId;
    private User userCompany;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(this);
        card_view_detail.setVisibility(View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("PERSONAL");
        ((TextView) findViewById(R.id.title_contract)).setText("Contratos");
//        progress = new ProgressDialog(PersonalAddContract.this);
//        progress.setTitle("Cargando Contratos");
//        progress.show();
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);

        MyPreferences preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        userCompany = new Gson().fromJson(profile, User.class);

        editPersonal = 1;
        Buttons.setVisibility(View.GONE);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        String personinfo = bd.getString("infoPersonal");
        Log.e("personalinfo2", personinfo);
        updateList(bd.getString("contractList"));
//        contratos = new Gson().fromJson(bd.getString("contractList"),
//                new TypeToken<ArrayList<ObjectList>>() {
//                }.getType());

        personal = new Gson().fromJson(personinfo,
                new TypeToken<Personal>() {
                }.getType());
        personalId = bd.getInt("PersonalId");
        if (personal == null) {
            personal = new Personal();
            personal.PersonalCompanyInfoId = personalId;
        }
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // steps.setVisibility(View.VISIBLE);
                // project.setVisibility(View.VISIBLE);
                layoutProject.setVisibility(View.GONE);
                layoutSteps.setVisibility(View.GONE);
                continueEnroller.setText("SIGUIENTE");
                previousButton.setText(" ");
                continueEnroller.setEnabled(true);
                previousButton.setEnabled(false);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(EnrollmentFragmnet.scanCC());
                findViewById(R.id.projectCardview).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbarCompany).setVisibility(View.VISIBLE);
                card_view_detail.setVisibility(View.VISIBLE);
                findViewById(R.id.container_main).setVisibility(View.GONE);
                requirement = 0;
                editPersonal = 0;
               /* findViewById(R.id.projectCardview).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbarstage).setVisibility(View.VISIBLE);
                findViewById(R.id.stepCardview).setVisibility(View.VISIBLE);
                findViewById(R.id.toolbarCompany).setVisibility(View.VISIBLE);
                findViewById(R.id.container_main).setVisibility(View.GONE);*/
            }
        });

        continueEnroller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (idContract.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PersonalAddContract.this)
                            .setMessage("Para continuar debe seleccionar un contrato");
                    builder.create().show();
                }
                if (continueEnroller.getText().toString().equals("FINALIZAR")) {
                    onBackPressed();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    continueEnroller.setText("");
                    previousButton.setText("Anterior");
                    continueEnroller.setEnabled(false);
                    previousButton.setEnabled(true);
                    requirement = 0;
                    editPersonal = 0;
                    findViewById(R.id.projectCardview).setVisibility(View.GONE);
                    findViewById(R.id.toolbarCompany).setVisibility(View.GONE);
                    findViewById(R.id.container_main).setVisibility(View.VISIBLE);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.container_main, EnrollmentFragmnet.scanCC());
                    fragmentTransaction.commit();
                }
            }
        });

    }

    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_personal);
    }

    private void updateList(String jsonObjStr) {
        logW("test " + jsonObjStr);
        ArrayList<ContractList> contractList = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<ContractList>>() {
                }.getType());
        ArrayList<ContractList> activeContractList = new ArrayList<>();
        for (ContractList objectList : contractList)
            if (objectList.IsActive) activeContractList.add(objectList);
        contratos = activeContractList;
        if (activeContractList.size() > 0) {
            RecyclerView recyclerViewProjects = findViewById(R.id.contracts);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerViewProjects.setLayoutManager(mLayoutManager);
            ContractRecyclerAdapter adapterProjects = new ContractRecyclerAdapter(this, activeContractList);
            recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                    mLayoutManager.getOrientation());
            recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
            recyclerViewProjects.setAdapter(adapterProjects);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e("back", "" + item.getItemId());
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
//        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this)
//                .setTitle("Alerta")
//                .setMessage("Esta seguro de volver atrás?")
//                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        PersonalAddContract.super.onBackPressed();
//                    }
//                })
//                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//        android.app.AlertDialog alertDialog = builder.create();
//        alertDialog.show();
        super.onBackPressed();
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        //progressDialog.dismiss();
        Log.e("option", "" + option + " url: " + url);
//        progress.dismiss();
        boolean stringDocument = false;
        // private String projectId;
        // private int stepId;
        int step = 0;
        RequirementsPersonal usersFragment;
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                Log.e("user", jsonObjStr);
                if (url.equals(Constantes.LIST_CONTRACTS_URL + idContract + "/IsContract/" + personal.PersonalCompanyInfoId)) {

                }
                if (step == 6) {
                    Buttons.setVisibility(View.VISIBLE);
                    continueEnroller.setText("FINALIZAR");
                    continueEnroller.setEnabled(true);
                    previousButton.setText("");
                    previousButton.setEnabled(false);

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    RequirementsPersonalOtherContract requirementOtherContract = RequirementsPersonalOtherContract.infoUser(new Gson().toJson(personal), jsonObjStr);
                    fragmentTransaction.replace(R.id.container_main, requirementOtherContract);
                    fragmentTransaction.commit();
                } else if (url.equals("api/Contract/" + idContract + "/PersonalInfo/") && requirement == 0) {
                    requirement = 1;
                    Personal personal = new Gson().fromJson(jsonObjStr,
                            new TypeToken<Personal>() {
                            }.getType());
                    Buttons.setVisibility(View.VISIBLE);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    usersFragment = RequirementsPersonal.infoUser(personal, "" + projectId, stepId);
                    fragmentTransaction.replace(R.id.container_main, usersFragment);
                    fragmentTransaction.commit();
                } else if (createdPersonal) {
                    createdPersonal = false;
                    previousButton.setText("");
                    previousButton.setEnabled(false);
                    continueEnroller.setText("FINALIZAR");
                    continueEnroller.setEnabled(true);
                    Personal user = new Gson().fromJson(jsonObjStr,
                            new TypeToken<Personal>() {
                            }.getType());
                    if (user.DocumentNumber == null) {
                        int docNewPersonal = 1;
                        user.DocumentNumber = "" + docNewPersonal;
                    }
                    personarReturn = user;
                    if (editPersonal == 1 && requirement == 0) {
                        requirement = 1;
                        personarReturn = user;
                        Buttons.setVisibility(View.VISIBLE);
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        usersFragment = RequirementsPersonal.infoUser(user, "" + projectId, stepId);
                        fragmentTransaction.replace(R.id.container_main, usersFragment);
                        fragmentTransaction.commit();
                    }

                    //}
                }

                break;
            case Constantes.SEND_REQUEST:
                logW(jsonObjStr);
                if (userfound == 0 && !url.equals(Constantes.LIST_AUTOPOSITION_URL + userCompany.CompanyId)) {
                    updateList(jsonObjStr);
                }
                break;
            case Constantes.UPDATE_ADMIN_USERS:
                break;
            case Constantes.REQUEST_NOT_FOUND:
                userfound = 1;
                /*nameProjet.setText(newUser.name);
                nameStep.setText(newUser.dni);*/
                if (stringDocument) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PersonalAddContract.this)
                            .setTitle("Personal no encontrado")
                            .setMessage("Para crear personal es necesario escanear documento");
                    builder.create().show();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container_main, EnrollmentFragmnet.scanCC());
                    fragmentTransaction.commit();
                } else {
                    Buttons.setVisibility(View.GONE);
                    editPersonal = 1;
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    EnrollmentNewPersona newUsersFragment = EnrollmentNewPersona.infoUser(newUser, idContract);
                    fragmentTransaction.replace(R.id.container_main, newUsersFragment);
                    fragmentTransaction.commit();
                }
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
                if (personarReturn != null) {
                    Buttons.setVisibility(View.VISIBLE);
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    usersFragment = RequirementsPersonal.infoUser(personarReturn, "" + projectId, stepId);
                    fragmentTransaction.replace(R.id.container_main, usersFragment);
                    fragmentTransaction.commit();
                }

            case Constantes.TIME_OUT_REQUEST:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }


    @Override
    public void onCancelActionAddPersonal() {
        onBackPressed();
        finish();
    }

    @Override
    public void onApplyActionAddPersonal(Personal personal) {

        if (requirement == 0) {
            requirement = 1;
            Buttons.setVisibility(View.VISIBLE);
            continueEnroller.setText("FINALIZAR");
            continueEnroller.setEnabled(true);
            previousButton.setText("");
            previousButton.setEnabled(false);
            onBackPressed();
            finish();
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            usersFragment = RequirementsPersonal.infoUser(personal, "" + projectId, stepId);
//            fragmentTransaction.replace(R.id.container_main, usersFragment);
//            fragmentTransaction.commit();
        }
    }

    private class ContractRecyclerAdapter extends CustomListInfoRecyclerView<ContractList> {

        ContractRecyclerAdapter(Context context, ArrayList<ContractList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ContractList mItem = mItems.get(pos);
            holder.textName.setText(mItem.ContractReview);
            holder.textSubName.setText(mItem.ContractorName);
            holder.textValidity.setText(String.format(mItem.ContractNumber));
            holder.iconIsActive.setVisibility(View.GONE);
            holder.logo.setImageResource(R.drawable.image_not_available);
            holder.btnEdit.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            if (mItem.FormImageLogo != null) {
                String url = Constantes.URL_IMAGES + mItem.FormImageLogo;
                String[] format = url.split(Pattern.quote("."));
                if (format[format.length - 1].equals("svg")) {
//                    Uri uri = Uri.parse(url);
                    Glide.with(mContext)
                            .load(url)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.image_not_available))
                            .into(holder.logo);
                } else {
                    Picasso.get().load(url)
                            .resize(0, 250)
                            .placeholder(R.drawable.image_not_available)
                            .error(R.drawable.image_not_available)
                            .into(holder.logo);
                }
            }

            holder.cardViewDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItem.ProjectIds != null) {
                        fillDates(mItem, mContext);
                    } else {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(PersonalAddContract.this)
                                .setTitle("Alerta")
                                .setMessage("No hay etapas de proyecto asociadas")
                                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        PersonalAddContract.super.onBackPressed();
                                    }
                                });
                        android.app.AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void fillDates(ContractList mItem, Context context) {
        card_view_detail.setVisibility(View.VISIBLE);
        text_name.setText(mItem.ContractNumber);
        text_sub_name.setText(mItem.ContractReview);
        idContract = mItem.Id;
        projectId = mItem.ProjectId;
//        stepId = mItem.ProjectStages[0];
        Log.e("seleccion", "se selecciono item: " + mItem.Id);
//        card_view_detail.setBackgroundResource(R.color.accent);
        if (mItem.FormImageLogo != null) {
            String url = Constantes.URL_IMAGES + mItem.FormImageLogo;
            String[] format = url.split(Pattern.quote("."));
            if (format[format.length - 1].equals("svg")) {
//                Uri uri = Uri.parse(url);
                Glide.with(context)
                        .load(url)
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.loading_animation)
                                .error(R.drawable.image_not_available))
                        .into(logo_imag);
            } else {
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo_imag);
            }
        }
        continueEnroller.setText("");
        previousButton.setText("Anterior");
        continueEnroller.setEnabled(false);
        previousButton.setEnabled(true);
        findViewById(R.id.projectCardview).setVisibility(View.GONE);
        findViewById(R.id.toolbarCompany).setVisibility(View.GONE);
        if (contratos != null) {
            for (ContractList c : contratos) {
                if (c.Id == mItem.Id) {
                    personal.Position = c.Position;
                    personal.FinishDate = c.FinishDate;
                    personal.StartDate = c.StartDate;
                    personal.PersonalCompanyInfoId = personalId;
                    break;
                }
            }
        }

        card_view_detail.setVisibility(View.GONE);
        ContractType contractType = new ContractType();
        contractType.Value = "";
        findViewById(R.id.container_main).setVisibility(View.VISIBLE);
        PersonalContractFragmentEnroll addPersonalFragment = PersonalContractFragmentEnroll.newInstanceEdit(personal, idContract, userCompany.CompanyId, mItem.StartDate, mItem.FinishDate, contractType, mItem);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container_main, addPersonalFragment);
        fragmentTransaction.commit();
    }
}


