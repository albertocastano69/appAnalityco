package co.tecno.sersoluciones.analityco.steps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import co.tecno.sersoluciones.analityco.ProjectActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.fragments.project.NewCompanyProjectFragment;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.Project;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;

/**
 * Created by Ser Soluciones SAS on 28/09/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ProjectStep2 extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final int REQUEST_CODE = 1000;

    private Context instance;
    private FrameLayout fragmnetNewCompany;
    private ArrayList<CompanyProject> companyProjects;
    private String idCompany;

    private ProjectRecyclerAdapter customRecyclerAdapter;
    private boolean logoUpload = false;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private NewCompanyProjectFragment newCompanyProjectFragment;

    private boolean sendToServer;
    private Project project;
    private RecyclerView recyclerview;
    private RelativeLayout layout_stages;
    private LinearLayout legendLayout;
    private String companyInfoId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        companyProjects = new ArrayList<>();
    }

    //Set your layout here
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.project_step2, container, false);
        MyPreferences preferences = new MyPreferences(v.getContext());
        instance = getActivity();
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        recyclerview = v.findViewById(R.id.recyclerCompany);
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        layout_stages = v.findViewById(R.id.layout_stages);
        if (companyProjects.size() < 1) {
            if (user.Companies.size() == 1) {
                CompanyList companyList = user.Companies.get(0);
                CompanyProject newcompany = new CompanyProject(companyList.StartDate, companyList.FinishDate, companyList.DocumentType,
                        companyList.DocumentNumber, companyList.Name, "Empresa administradora ", companyList.Logo);
                companyProjects.add(newcompany);
                logW("LOGO: " + companyList.Logo);
            } else {
                for (CompanyList companyList : user.Companies) {
                    if (project.CompanyInfoId.equals(companyList.Id)) {
                        CompanyProject newcompany = new CompanyProject(companyList.StartDate, companyList.FinishDate, companyList.DocumentType,
                                companyList.DocumentNumber, companyList.Name, "Empresa administradora ", companyList.Logo);
                        companyProjects.add(newcompany);
                    }
                }

            }


        }

        customRecyclerAdapter = new ProjectRecyclerAdapter(getActivity(), companyProjects);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerview.setAdapter(customRecyclerAdapter);

        FloatingActionButton newCompany = v.findViewById(R.id.addCompanyProject);
        fragmnetNewCompany = v.findViewById(R.id.container_join);
        legendLayout = v.findViewById(R.id.layout_legend);
        newCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                legendLayout.setVisibility(View.GONE);
                AddFragmentJoinCompany();
            }
        });
        return v;
    }

    private boolean isSendToServer() {
        return sendToServer;
    }

    private void AddFragmentJoinCompany() {
        fragmnetNewCompany.setVisibility(View.VISIBLE);
        newCompanyProjectFragment = NewCompanyProjectFragment.newInstance(idCompany, companyInfoId);
        newCompanyProjectFragment.setTargetFragment(ProjectStep2.this, REQUEST_CODE);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, newCompanyProjectFragment);
        fragmentTransaction.commit();
        layout_stages.setVisibility(View.GONE);
        recyclerview.setVisibility(View.GONE);
    }

    private void RemoveFragmentJoinCompany() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(newCompanyProjectFragment);
        fragmentTransaction.commit();
        fragmnetNewCompany.setVisibility(View.GONE);
        layout_stages.setVisibility(View.VISIBLE);
        recyclerview.setVisibility(View.VISIBLE);
        legendLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    CompanyProject company = (CompanyProject) data.getSerializableExtra("newComp");
                    String id = data.getStringExtra("id");
                    Log.d("id_comp", id);
                    company.setId(id);
                    newItem(company);
                    RemoveFragmentJoinCompany();
                    break;
                case Activity.RESULT_CANCELED:
                    RemoveFragmentJoinCompany();
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sendToServer = false;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
        LocalBroadcastManager.getInstance(instance).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(requestBroadcastReceiver);
    }

    private void newItem(CompanyProject company) {
        Log.e("addcompany", "nueva empresa en recycler");
        companyProjects.add(company);
        customRecyclerAdapter.notifyItemInserted(companyProjects.size() - 1);
        customRecyclerAdapter.notifyDataSetChanged();
    }

    public void updateProject() {
        project = ((ProjectActivity) getActivity()).getProject();
        idCompany = project.CompanyId;
        companyInfoId = project.CompanyInfoId;
    }

    private class ProjectRecyclerAdapter extends CustomListInfoRecyclerView<CompanyProject> {

        ProjectRecyclerAdapter(Context context, ArrayList<CompanyProject> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final CompanyProject mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);

            holder.labelValidity.setVisibility(View.GONE);
            holder.textName.setText(mItem.Name + " " + mItem.Name);
            boolean isActive = true;
            holder.textActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.logo.setImageResource(R.drawable.image_not_available);
            if (position == 0) {
                Drawable drawableIsActive = MaterialDrawableBuilder.with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.BANK)
                        .setColor(Color.GRAY)
                        .build();
                holder.iconIsActive.setImageDrawable(drawableIsActive);
                holder.btnEdit.setVisibility(View.GONE);
                String nit = "NIT: " + mItem.DocumentNumber;
                holder.textSubName.setText(nit);
                holder.labelValidity.setVisibility(View.GONE);
                holder.textValidity.setText("Empresa Adminstradora ");
            } else {
                Drawable drawableIsActive = MaterialDrawableBuilder.with(getActivity())
                        .setIcon(mItem.IsActive ? MaterialDrawableBuilder.IconValue.THUMB_UP : MaterialDrawableBuilder.IconValue.THUMB_DOWN)
                        .setColor(mItem.IsActive ? Color.GREEN : Color.RED)
                        .setSizeDp(35)
                        .setIcon(MaterialDrawableBuilder.IconValue.DOMAIN)
                        .setColor(Color.GRAY)
                        .build();
                holder.iconIsActive.setImageDrawable(drawableIsActive);
                holder.textSubName.setText("NIT: " + mItem.DocumentNumber);
                holder.labelValidity.setVisibility(View.GONE);
                holder.textValidity.setText("Empresa Constructora");
            }
            holder.btnEdit.setVisibility(View.GONE);
            holder.logo.setImageResource(R.drawable.image_not_available);
            if (mItem.Logo != null) {
                String url = Constantes.URL_IMAGES + mItem.Logo;
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            }
            holder.btnEdit.setBackgroundResource(R.drawable.ic_close_black_24dp);
            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    companyProjects.remove(mItem);
                    customRecyclerAdapter.notifyItemRemoved(pos);
                    customRecyclerAdapter.notifyDataSetChanged();
                }
            });
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }

    }

    public void submitRequest() {
        companyProjects.remove(0);
        project = ((ProjectActivity) getActivity()).getProject();
        project.JoinCompanies = companyProjects;
        project.JoinCompaniesList = companyProjects;
        project.CompanyInfoId = ((ProjectActivity) getActivity()).idCompany;
        ((ProjectActivity) getActivity()).setProject(project);
        //((ProjectActivity) getActivity()).nextPage(2);
        final String json = new Gson().toJson(project);
        logW("Json to send"+json);
        sendFormToServer(json);
    }

    private void sendFormToServer(final String json) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Aviso")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Esta seguro de crear este proyecto?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendToServer = true;
                        ((ProjectActivity) getActivity()).showProgress(true);
                        CrudIntentService.startRequestCRUD(getActivity(),
                                Constantes.CREATE_PROJECT_URL, Request.Method.POST, json, "", false);

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

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        if (isSendToServer()) {
            ((ProjectActivity) getActivity()).showProgress(false);
            switch (option) {
                case Constantes.SUCCESS_REQUEST:
                    //clearForm();
                    logE("Proyecto creado satisfact.: " + jsonObjStr);
                    if (project.Logo != null && !project.Logo.isEmpty()) {
                        try {
                            JSONObject jsonObject = new JSONObject(jsonObjStr);
                            String _id = jsonObject.getString("Id");
                            Log.e("id_project", " " + _id);
                            logoUpload = true;
                            HashMap<String, String> params = new HashMap<>();
                            params.put("file", project.Logo);
                            ((ProjectActivity) getActivity()).showProgress(true);
                            String urlProject = Constantes.UPDATE_LOGO_PROJECT_URL + _id;
                            CrudIntentService.startActionFormData(getActivity(), urlProject,
                                    Request.Method.PUT, params);
                            log("VALUE_JSON_BROADCAST: " + jsonObjStr);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        msgSuccess();
                    }

                    break;
                case Constantes.SUCCESS_FILE_UPLOAD:
                    if (logoUpload) {
                        msgSuccess();
                    }
                    break;
                case Constantes.BAD_REQUEST:
                case Constantes.TIME_OUT_REQUEST:
                    sendToServer = false;
                    MetodosPublicos.alertDialog(getContext(), "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                    break;
            }
        }
    }

    private void msgSuccess() {
        sendToServer = false;
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getActivity());
        alertDialog.setCancelable(false);
        alertDialog.setTitle("Mensaje");
        alertDialog.setMessage("Proyecto creado satisfactoriamente");
        alertDialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // getActivity().finish();
                dialogInterface.dismiss();
                ((ProjectActivity) getActivity()).submitCreate();
            }
        });
        alertDialog.create().show();
    }

}