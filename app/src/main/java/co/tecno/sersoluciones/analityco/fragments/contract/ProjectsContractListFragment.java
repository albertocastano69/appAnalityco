package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsContractsActivityTabs;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.EditInfoContractActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

public class ProjectsContractListFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_PROJECTID = "contractId";
    private static final String ARG_ISREGISTER = "isRegister";
    private static final String ARG_IS_RENDER = "isRender";

    private static final int UPDATE = 1;
    private ClaimsBasicUser Claims;
    private int positionDelete = 0;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newCompanyProject;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.title2)
    TextView title2;
    @BindView(R.id.recycler_companies)
    RecyclerView recyclerViewProjects;

    private View view;
    private User user;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private ArrayList<ProjectList> projectsLists;
    private String contratId = "";
    private boolean isRegister;
    private ProjectRecyclerAdapter adapterProjects;

    public ProjectsContractListFragment() {
    }

    public static ProjectsContractListFragment newInstance(String contractId, boolean isRegister, boolean isRender) {

        ProjectsContractListFragment fragment = new ProjectsContractListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, contractId);
        args.putBoolean(ARG_ISREGISTER, isRegister);
        args.putBoolean(ARG_IS_RENDER, isRender);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {
            Claims = ((DetailsContractsActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getClaims();

            MyPreferences preferences = new MyPreferences(getContext());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);

            contratId = getArguments().getString(ARG_PROJECTID);
            isRegister = getArguments().getBoolean(ARG_ISREGISTER);
            boolean isRender = getArguments().getBoolean(ARG_IS_RENDER);
            permission();
            title.setText("Proyectos Vinculados");
            title2.setText("VINCULAR PROYECTO");
            if (isRender) fillListProject();
        }

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        newCompanyProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                project.putExtra("process", 2);
                project.putExtra("contract", contratId);
                startActivityForResult(project, UPDATE);
            }
        });
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

    private void fillListProject() {
        projectsLists = ((DetailsContractsActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getProjectList();
        if (projectsLists.isEmpty()) {

            recyclerViewProjects.setVisibility(View.GONE);
            return;
        } else {
            Collections.sort(projectsLists, (projectList, t1) -> Boolean.compare(t1.IsActive, projectList.IsActive));
            Collections.sort(projectsLists, (projectList, t1) -> Boolean.compare(projectList.Expiry, t1.Expiry));
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        if (recyclerViewProjects != null) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerViewProjects.setLayoutManager(mLayoutManager);
            adapterProjects = new ProjectRecyclerAdapter(getActivity(), projectsLists);
            recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                    mLayoutManager.getOrientation());
            recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
            recyclerViewProjects.setAdapter(adapterProjects);
        }
    }

    private class ProjectRecyclerAdapter extends CustomListInfoRecyclerViewUsers<ProjectList> {

        ProjectRecyclerAdapter(Context context, ArrayList<ProjectList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ProjectList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textSubName.setText(mItem.Address + " " + mItem.CityName);
            holder.user_icon.setVisibility(View.VISIBLE);
            holder.iconDate.setVisibility(View.VISIBLE);
            holder.phone.setVisibility(View.GONE);
            holder.user_icon.setImageResource(R.drawable.ic_etapa1);
            holder.btnEdit.setVisibility(View.GONE);
            holder.imageDots.setVisibility(View.VISIBLE);
            if (mItem.AllStages)
                holder.profile.setText("Todas las etapas");
            else if (mItem.Stages != null) {
                String stage = "";
                for (int i = 0; i < mItem.Stages.size(); i++) {
                    stage = stage + mItem.Stages.get(i).Name;
                    if (i < mItem.Stages.size() - 1)
                        stage = stage + ",";
                }
                holder.profile.setText(stage);
            }
            //holder.btnEdit.setVisibility(View.GONE);
            //holder.textActive.setVisibility(View.GONE);

            String mDate = mItem.FinishDate;
            String mFinishDate = mItem.ProjectFinishDate;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    Date now = new Date();
                    boolean isActive = true;
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    Date startDate = format.parse(mFinishDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
                    mDate = dateFormat.format(date);
                    mFinishDate = dateFormat.format(startDate);
                    if (date.before(now)) {
                        isActive = false;
                    }
                    if (!isActive) {
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.stateIcon.setImageResource(R.drawable.state_icon_red);
                        holder.textValidity.setTextColor(getResources().getColor(R.color.bar_undecoded));
                        holder.calendar.setColorResource(R.color.bar_undecoded);
                    } else if (mItem.Expiry) {
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.textValidity.setTextColor(getResources().getColor(R.color.expiry));
                        holder.calendar.setColorResource(R.color.expiry);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textValidity.setText(mDate);
            holder.textValidity2.setText(mFinishDate);

            if (!mItem.ProjectIsActive) {
                holder.textValidity2.setTextColor(getResources().getColor(R.color.bar_undecoded));
                holder.iconDate.setColorResource(R.color.bar_undecoded);
            } else if (mItem.ProjectExpiry) {
                holder.textValidity2.setTextColor(getResources().getColor(R.color.expiry));
                holder.iconDate.setColorResource(R.color.expiry);
            }


            if (mItem.Logo != null && !mItem.Logo.equals("null")) {
                String url = Constantes.URL_IMAGES + mItem.Logo;

                //holder.mNetworkImageView.setImageUrl(url, holder.mImageLoader);
                Picasso.get().load(url)
                        .resize(150, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            } else {
                holder.logo.setImageResource(R.drawable.image_not_available);
            }

            if (isRegister)
                holder.btnEdit.setVisibility(View.GONE);

            holder.imageDots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    android.widget.PopupMenu popup = new android.widget.PopupMenu(getActivity(), view);
                    popup.inflate(R.menu.action_menu);
                    popup.getMenu().getItem(0).setEnabled(false);
                    popup.getMenu().getItem(1).setEnabled(false);
                    if (user.IsAdmin || user.IsSuperUser) {
                        if (user.claims.contains("contractsprojects.delete") || user.IsSuperUser)
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (user.claims.contains("contractsprojects.update") || user.IsSuperUser)
                            popup.getMenu().getItem(1).setEnabled(true);
                    } else if (Claims != null) {
                        if (Claims.Claims.contains("contractsprojects.delete"))
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (Claims.Claims.contains("contractsprojects.update"))
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
                                            CrudIntentService.startRequestCRUD(getActivity(),
                                                    Constantes.LIST_CONTRACTS_URL + contratId + "/Project/" + mItem.ProjectId, Request.Method.DELETE, "", "", false);

                                        })
                                        .setNegativeButton("No", null)
                                        .setMessage("Desea desvincular este proyecto");
                                builder.create().show();
                                return false;
                            case R.id.edit:
                                Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                                project.putExtra("process", 3);
                                project.putExtra("contract", contratId);
                                project.putExtra("json", new Gson().toJson(mItem));
                                startActivityForResult(project, UPDATE);
                                return true;
                            default:
                                return false;
                        }
                    });
                    popup.show();
                }
            });

            holder.mView.setOnClickListener(view -> {
//                    mItem.Id = mItem.ProjectId;
                onProjectInteraction(mItem, holder.logo);
            });
        }
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                projectsLists.remove(positionDelete);
                adapterProjects.notifyDataSetChanged();
                positionDelete = 0;
//                ((DetailsContractsActivityTabs) getActivity()).reloadData();
                break;
        }
    }

    private void onProjectInteraction(ProjectList item, ImageView imageView) {
        ViewCompat.setTransitionName(imageView, item.Id);
        Intent i = new Intent(getActivity(), DetailsProjectActivityTabs.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.ITEM_OBJ, item);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptions transitionActivityOptions = ActivityOptions
                .makeSceneTransitionAnimation(getActivity(), imageView, ViewCompat.getTransitionName(imageView));
        startActivity(i, transitionActivityOptions.toBundle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void permission() {

        //permisos visualizacion
        if (user.claims.contains("contractsprojects.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("contractsprojects.add") || (user.IsSuperUser && !isRegister))
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("contractsprojects.add"))
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);
        }
    }
}
