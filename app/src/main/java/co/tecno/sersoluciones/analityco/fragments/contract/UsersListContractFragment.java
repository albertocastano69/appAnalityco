package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsContractsActivityTabs;
import co.tecno.sersoluciones.analityco.EditInfoContractActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.models.UserProject;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

public class UsersListContractFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_PROJECTID = "projectId";
    private static final String ARG_IS_RENDER = "isRender";
    private static final String ARG_PARAM6 = "param6";
    private static final int UPDATE = 1;
    private ClaimsBasicUser Claims;
    private String contractId;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newUserContract;

    private View view;
    private User user;
    private ArrayList<UserProject> userContract;
    private String finishDate;
    private int positionDelete = 0;
    private String deleteUrl = "";
    private UsersRecyclerAdapter adapterUsers;
    private RequestBroadcastReceiver requestBroadcastReceiver;


    public UsersListContractFragment() {
    }

    public static UsersListContractFragment newInstance(String contractId, String finishdate, boolean isRender) {

        UsersListContractFragment fragment = new UsersListContractFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, contractId);
        args.putString(ARG_PARAM6, finishdate);
        args.putBoolean(ARG_IS_RENDER, isRender);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {
            Claims = ((DetailsContractsActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getClaims();
            finishDate = getArguments().getString(ARG_PARAM6);
            contractId = getArguments().getString(ARG_PROJECTID);
            boolean isRender = getArguments().getBoolean(ARG_IS_RENDER);
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            if (isRender) fillListCompanies();
        }
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        newUserContract.setOnClickListener(view -> {
            Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
            project.putExtra("process", 4);
            project.putExtra("contract", contractId);
            project.putExtra("company", user.CompanyId);
            project.putExtra("userprojectistInfo", userContract);
            project.putExtra("finishDate", finishDate);
            startActivityForResult(project, UPDATE);
        });
        permission();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_DELETE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(requestBroadcastReceiver);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
        }
    }

    private void fillListCompanies() {

        userContract = ((DetailsContractsActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getUserProjectList();
        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_companies);
        if (userContract != null) {
            if (userContract.isEmpty()) {
                recyclerViewProjects.setVisibility(View.GONE);
                return;
            }
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        adapterUsers = new UsersRecyclerAdapter(getActivity(), userContract);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterUsers);
    }

    @Override
    public void onStringResult(String action, int option, String response, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (url.equals(deleteUrl)) {
                    userContract.remove(positionDelete);
                    adapterUsers.notifyDataSetChanged();
                    positionDelete = 0;
                    ((DetailsContractsActivityTabs) getActivity()).reloadData();
                }
                break;
        }
    }

    private class UsersRecyclerAdapter extends CustomListInfoRecyclerViewUsers<UserProject> {

        UsersRecyclerAdapter(Context context, ArrayList<UserProject> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final UserProject mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.LastName);
            holder.textSubName.setText(mItem.UserName);
            holder.btnEdit.setVisibility(View.GONE);
            holder.imageDots.setVisibility(View.VISIBLE);
            holder.profile.setText(mItem.RolName);
            /*String separador = Pattern.quote("]");
            if (mItem.RoleName != null) {
                holder.profile.setText(mItem.RoleName);
                String[] parts = mItem.RoleName.split(separador);
                if (parts.length >= 1) {
                    holder.profile.setText(parts[1]);
                }
            }*/

            boolean isActive = true;
            Date now = new Date();
            String mDate = mItem.FinishDate;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    mDate = dateFormat.format(date);
                    if (date.before(now)) {
                        isActive = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textValidity.setText(mDate);
            if (!isActive) {
                holder.stateIcon.setVisibility(View.VISIBLE);
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
                holder.textValidity.setTextColor(getResources().getColor(R.color.bar_undecoded));
                holder.calendar.setColorResource(R.color.bar_undecoded);
            }
            holder.phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItem.PhoneNumber != null) {
                        PopupMenu popup = new PopupMenu(getActivity(), holder.phone);
                        popup.getMenuInflater()
                                .inflate(R.menu.menu_call, popup.getMenu());
                        popup.getMenu().getItem(0).setTitle("Marcar");
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent i = new Intent(Intent.ACTION_DIAL,
                                        Uri.parse("tel:" + mItem.PhoneNumber));
                                startActivity(i);
                                return true;
                            }
                        });
                        popup.show();
                    }
                }
            });
            holder.logo.setImageResource(R.drawable.profile_dummy);
            if (mItem.Photo != null) {
                String url = Constantes.URL_IMAGES + mItem.Photo;

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.profile_dummy)
                        .error(R.drawable.profile_dummy)
                        .into(holder.logo);
            }
            if (!Claims.Claims.contains("users.update") && !user.roles.contains("Innovodata")) {
                holder.btnEdit.setVisibility(View.GONE);
            }

            holder.imageDots.setOnClickListener(view -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(getActivity(), view);
                popup.inflate(R.menu.action_menu);
                popup.getMenu().getItem(0).setEnabled(false);
                popup.getMenu().getItem(1).setEnabled(false);
                if (user.IsAdmin || user.IsSuperUser) {
                    if (user.claims.contains("contractsusers.delete") || user.IsSuperUser)
                        popup.getMenu().getItem(0).setEnabled(true);
                    if (user.claims.contains("contractsusers.update") || user.IsSuperUser)
                        popup.getMenu().getItem(1).setEnabled(true);
                } else if (Claims != null && !user.IsSuperUser) {
                    if (Claims.Claims.contains("contractsusers.delete"))
                        popup.getMenu().getItem(0).setEnabled(true);
                    if (Claims.Claims.contains("contractsusers.update"))
                        popup.getMenu().getItem(1).setEnabled(true);
                }

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.delete:
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                    .setTitle("Alerta")
                                    .setCancelable(false)
                                    .setPositiveButton("Si", (dialogInterface, i) -> {
                                        positionDelete = pos;
                                        deleteUrl = Constantes.LIST_CONTRACTS_URL + contractId + "/Users/" + mItem.UserId;
                                        logW("position delete " + positionDelete);
                                        CrudIntentService.startRequestCRUD(getActivity(),
                                                deleteUrl, Request.Method.DELETE, "", "", false);
                                    })
                                    .setNegativeButton("No", null)
                                    .setMessage("Desea desvincular este usuario");
                            builder.create().show();
                            return false;
                        case R.id.edit:
                            Gson gson = new Gson();
                            Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                            project.putExtra("process", 5);
                            project.putExtra("contract", contractId);
                            project.putExtra("company", user.CompanyId);
                            project.putExtra("userInfo", gson.toJson(mItem));
                            project.putExtra("finishDate", finishDate);
                            startActivityForResult(project, UPDATE);
                            return true;
                        default:
                            return false;
                    }
                });
                popup.show();
            });

        }
    }

    private void permission() {
        //permisos visualizacion
        if (user.claims.contains("contractsusers.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("contractsusers.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("contractsusers.add"))
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);
        }
    }

}
