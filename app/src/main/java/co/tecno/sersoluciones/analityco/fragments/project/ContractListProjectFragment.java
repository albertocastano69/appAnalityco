package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.ContractsListActivity;
import co.tecno.sersoluciones.analityco.DetailsContractsActivityTabs;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.EditInfoProjectActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.ProjectStageContract;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;

public class ContractListProjectFragment extends Fragment {

    private static final String ARG_PROJECTID = "projectId";
    private static final String ARG_IS_RENDER = "isRender";
    private static final int UPDATE = 1;
    private ClaimsBasicUser Claims;
    private String projectId;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newuserProject;

    private View view;
    private User user;

    public ContractListProjectFragment() {
    }

    public static ContractListProjectFragment newInstance(String projectId, boolean isRender) {
        // Log.e("User2",data);
        ContractListProjectFragment fragment = new ContractListProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, projectId);
        args.putBoolean(ARG_IS_RENDER, isRender);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.contract_list_project_fragmet, container, false);

        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {
            Claims = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getClaims();
            projectId = getArguments().getString(ARG_PROJECTID);
            boolean isRender = getArguments().getBoolean(ARG_IS_RENDER);

//            ArrayList<ProjectStages> stagesLists = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getProjectStages();
//            if (stagesLists != null) {
//                for (ProjectStages stage : stagesLists) {
//                    stageIds.add(stage.Id);
//                }
//            }

            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);

            permission();

            //Log.w("infoContract", fillForm);
            ArrayList<ObjectList> userProjec = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getObjectList();
            if (userProjec != null && isRender) fillListContracts(userProjec);
        }
        newuserProject.setOnClickListener(view -> {
            Intent project = new Intent(getActivity(), ContractsListActivity.class);
            startActivityForResult(project, UPDATE);
        });
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            logW("RESULT_OK");
        }
    }

    private void fillListContracts(ArrayList<ObjectList> userProjec) {

        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_companies);
        if (userProjec != null) {
            if (userProjec.isEmpty()) {
                recyclerViewProjects.setVisibility(View.GONE);
                return;
            }
        }
        Collections.sort(userProjec, new Comparator<ObjectList>() {
            @Override
            public int compare(ObjectList projectList, ObjectList t1) {
                return t1.FinishDate.compareTo(projectList.FinishDate);
            }
        });


        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        ContractRecyclerAdapter adapterProjects = new ContractRecyclerAdapter(getActivity(), userProjec);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterProjects);
    }

    private class ContractRecyclerAdapter extends CustomListInfoRecyclerViewUsers<ObjectList> {

        ContractRecyclerAdapter(Context context, ArrayList<ObjectList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ObjectList mItem = mItems.get(pos);
            holder.textName.setText(mItem.ContractReview);
            holder.textName.setAllCaps(false);
            holder.textSubName.setText(mItem.ContractorName);
            holder.textValidity2.setText("No. " + mItem.ContractNumber);

            //if(mItem.ProjectStageContracts!=null){
            for (ProjectStageContract psc : mItem.ProjectStageContracts) {
                // if(psc.ProjectId.equals(projectId)){
                if (psc.AllStages)
                    holder.profile.setText("Todas las etapas");
                else if (psc.Stages != null) {
                    String stage = "";
                    for (int i = 0; i < psc.Stages.size(); i++) {
                        stage = stage + psc.Stages.get(i).Name;
                        if (i < psc.Stages.size() - 1)
                            stage = stage + ",";
                    }
                    holder.profile.setText(stage);
                }
                // }
            }
            // }

            boolean isActive = true;
            Date now = new Date();
            String mDate = mItem.FinishDate;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
                    mDate = dateFormat.format(date);
                    if (date.before(now)) {
                        isActive = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.user_icon.setImageResource(R.drawable.ic_etapa1);
            holder.phone.setVisibility(View.GONE);
            holder.textValidity.setText(mDate);

            if (mItem.IsActive) {
                if (mItem.Expiry) {
                    holder.stateIcon.setVisibility(View.VISIBLE);
                    holder.textValidity.setTextColor(getResources().getColor(R.color.expiry));
                    holder.calendar.setColorResource(R.color.expiry);
                }
            } else {
                holder.stateIcon.setVisibility(View.VISIBLE);
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
                holder.textValidity.setTextColor(Color.RED);
                holder.calendar.setColorResource(R.color.bar_undecoded);
            }

            holder.logo.setImageResource(R.drawable.image_not_available);

            if (mItem.FormImageLogo != null) {
                String[] format = mItem.FormImageLogo.split(Pattern.quote("."));
                // Log.e("image", "ok" + mItem.FormImageLogo);
                String url = Constantes.URL_IMAGES + mItem.FormImageLogo;
                if (format[format.length - 1].equals("svg")) {
//                    Uri uri = Uri.parse(url);
                    Glide.with(getContext())
                            .load(url)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.loading_animation)
                                    .error(R.drawable.image_not_available))
                            .into(holder.logo);
                } else {
                    Picasso.get().load(url)
                            .resize(0, 150)
                            .placeholder(R.drawable.image_not_available)
                            .error(R.drawable.image_not_available)
                            .into(holder.logo);
                }
            }
            holder.btnEdit.setVisibility(View.GONE);
            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Gson gson = new Gson();
                    Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
                    project.putExtra("process", 8);
                    project.putExtra("project", projectId);
                    project.putExtra("company", user.CompanyId);
                    project.putExtra("userInfo", gson.toJson(mItem));
                    startActivityForResult(project, UPDATE);
                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onContractInteraction(mItem, holder.logo);
                }
            });
        }
    }

    private void onContractInteraction(ObjectList item, ImageView imageView) {
        ViewCompat.setTransitionName(imageView, item.Id);

        Intent i = new Intent(getActivity(), DetailsContractsActivityTabs.class);
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
        if (user.claims.contains("projectscontracts.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("projectscontracts.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("projectscontracts.add"))
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);
        }
    }
}
