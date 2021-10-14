package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.EditInfoProjectActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.fragments.PolygonMapFragment;
import co.tecno.sersoluciones.analityco.models.BasicProject;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.ProjectStages;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;


public class projectGeneralInfoFragmnet extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_PROJECTID = "projectId";
    private static final String ARG_ADDRESS = "address";
    private static final String ARG_ACTIVE = "isactive";
    private ClaimsBasicUser Claims;
    private String projectId;
    private String fillForm;

    @BindView(R.id.header_img)
    ImageView headerImg;
    @BindView(R.id.text_name)
    TextView textName;
    @BindView(R.id.text_start_date)
    TextView textStartDate;
    @BindView(R.id.text_end_date)
    TextView textEndDate;
    @BindView(R.id.state_icon)
    ImageView stateIcon;

    @BindView(R.id.icon_edit_main_form)
    MaterialIconView editProjectButton;
    @BindView(R.id.addStagesProject)
    FloatingActionButton newStageProject;
    private ArrayList<ProjectStages> stagesLists;
    private View view;
    private User user;
    private StagesRecyclerAdapter adapterProjects;
    private ProjectStages itemDelete = new ProjectStages();

    public projectGeneralInfoFragmnet() {
    }

    public static projectGeneralInfoFragmnet newInstance(String projectId, String address, int isactive, String transitionName) {

        projectGeneralInfoFragmnet fragment = new projectGeneralInfoFragmnet();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, projectId);
        args.putString(ARG_ADDRESS, address);
        args.putInt(ARG_ACTIVE, isactive);
        args.putString(Constantes.ITEM_TRANSITION_NAME, transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.project_general_info_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);

        if (getArguments() != null) {
            Claims = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getClaims();
            projectId = getArguments().getString(ARG_PROJECTID);
            fillForm = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getData();
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            int active = getArguments().getInt(ARG_ACTIVE);
            if (active == 1) {
                stateIcon.setVisibility(View.VISIBLE);
            } else if (active == 2) {
                stateIcon.setVisibility(View.VISIBLE);
                stateIcon.setImageResource(R.drawable.state_icon_red);
            }

            String imageTransitionName = getArguments().getString(Constantes.ITEM_TRANSITION_NAME);
            headerImg.setTransitionName(imageTransitionName);

            textName.setText(getArguments().getString(ARG_ADDRESS));
            setData(fillForm);
        }
        editProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
                project.putExtra("process", 1);
                project.putExtra("project", projectId);
//                project.putExtra("json", fillForm);
                if (saveJsonToFile(fillForm))
                    startActivityForResult(project, Constantes.UPDATE);
            }
        });
        newStageProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
                project.putExtra("process", 3);
                project.putExtra("project", projectId);
                startActivityForResult(project, Constantes.UPDATE);
            }
        });
        return view;
    }

    private boolean saveJsonToFile(String json) {
        String fileName = "file_Json";
        FileOutputStream outputStream;
        try {
            outputStream = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(json.getBytes());
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setData(String fillForm) {
        if (fillForm != null && !fillForm.isEmpty()) {
            final BasicProject project = new Gson().fromJson(fillForm,
                    new TypeToken<BasicProject>() {
                    }.getType());
            if (project.GeoFenceJson != null) {

                Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container_map, PolygonMapFragment.newInstance(project.GeoFenceJson))
                        .commit();
            }

            String logo = project.Logo;
            if (logo != null) {
                String imageUrl = Constantes.URL_IMAGES + logo;
                Picasso.get()
                        .load(imageUrl)
                        .noFade()
                        .resize(250, 250)
                        .placeholder(R.drawable.not_project_1)
                        .error(R.drawable.not_project_1)
                        .into(headerImg);
            } else {
                headerImg.setImageResource(R.drawable.not_project_1);
            }
            fillListStages(project.ProjectStageArray);
        }
    }

    private void fillListStages(ArrayList<ProjectStages> stages) {
        permission();
        this.stagesLists = stages;
        dates(stagesLists);
        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_projects);
        if (stagesLists.isEmpty()) {

            recyclerViewProjects.setVisibility(View.GONE);
            return;
        }

        Collections.sort(stagesLists, new Comparator<ProjectStages>() {
            @Override
            public int compare(ProjectStages projectList, ProjectStages t1) {
                return t1.FinishDate.compareTo(projectList.FinishDate);
            }
        });
        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        adapterProjects = new StagesRecyclerAdapter(getActivity(), stagesLists);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterProjects);
    }

    private void dates(ArrayList<ProjectStages> projectStages) {

        String startDateStr = "";
        String finishDateStr = "";
        Date startDate = null;
        Date finishDate = null;
        Date now = new Date();
        if (projectStages.size() > 0) {
            startDateStr = projectStages.get(0).StartDate;
            finishDateStr = projectStages.get(0).FinishDate;
        }
        boolean isActive = true;
        try {
            if (!finishDateStr.isEmpty()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                finishDate = format.parse(finishDateStr);
                startDate = format.parse(startDateStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < projectStages.size(); i++) {
            try {
                if (!startDateStr.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date finishDate2 = format.parse(projectStages.get(i).FinishDate);
                    Date startDate2 = format.parse(projectStages.get(i).StartDate);

                    if (finishDate != null && finishDate2 != null) {
                        if (finishDate.before(finishDate2)) {
                            finishDate = finishDate2;
                        }
                    }
                    if (startDate != null && startDate2 != null) {
                        if (startDate2.before(startDate)) {
                            startDate = startDate2;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            if (!finishDateStr.isEmpty()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
                startDateStr = dateFormat.format(startDate);
                finishDateStr = dateFormat.format(finishDate);
                textStartDate.setText(startDateStr);
                textEndDate.setText(finishDateStr);
                Log.e("fechas", "startdate: " + textStartDate + " enddate: " + textEndDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
        }
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                stagesLists.remove(itemDelete);
                adapterProjects.notifyDataSetChanged();
                ((DetailsProjectActivityTabs) getActivity()).reloadData();
                break;
        }
    }

    private class StagesRecyclerAdapter extends CustomListInfoRecyclerView<ProjectStages> {

        StagesRecyclerAdapter(Context context, ArrayList<ProjectStages> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ProjectStages mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textSubName.setText(mItem.Review);

            holder.logo.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);
            holder.iconIsActive.setVisibility(View.GONE);
            holder.dateIcon.setVisibility(View.VISIBLE);
//            holder.labelValidity.setVisibility(View.GONE);

            boolean isActive = true;
            Date now = new Date();
            String mDate = "" + mItem.FinishDate;
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
            holder.textValidity.setText(mDate);
            if (!mItem.IsActive) {
//                holder.stateIcon.setVisibility(View.VISIBLE);
//                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
                holder.textValidity.setTextColor(getResources().getColor(R.color.red));
                holder.dateIcon.setColor(getResources().getColor(R.color.red));
            } else if (mItem.Expiry) {
                holder.textValidity.setTextColor(getResources().getColor(R.color.expiry));
                holder.dateIcon.setColor(getResources().getColor(R.color.expiry));
            }


            holder.btnEdit.setImageResource(R.drawable.ic_dots_vertical);
            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(getActivity(), view);
                    popup.inflate(R.menu.action_menu);
                    popup.getMenu().getItem(0).setVisible(false);
                    popup.getMenu().getItem(1).setEnabled(false);

                    if (user.IsAdmin || user.IsSuperUser) {
//                        if ((user.claims.contains("projectsstages.delete") || user.IsSuperUser) && !mItem.HaveContract)
//                            popup.getMenu().getItem(0).setEnabled(true);
                        if (user.claims.contains("projectsstages.update") || user.IsSuperUser)
                            popup.getMenu().getItem(1).setEnabled(true);
                    } else if (Claims != null) {
//                        if (Claims.Claims.contains("projectsstages.delete"))
//                            popup.getMenu().getItem(0).setEnabled(true);
                        if (Claims.Claims.contains("projectsstages.update"))
                            popup.getMenu().getItem(1).setEnabled(true);
                    }
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
//                                case R.id.delete:
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
//                                            .setTitle("Alerta")
//                                            .setCancelable(false)
//                                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialogInterface, int i) {
//                                                    CrudIntentService.startRequestCRUD(getActivity(),
//                                                            Constantes.DELETE_STAGE_URL + mItem.getId(), Request.Method.DELETE, "", "", false);
//                                                    itemDelete = mItem;
//                                                }
//                                            })
//                                            .setNegativeButton("No", null)
//                                            .setMessage("Desea Eliminar esta etapa");
//                                    builder.create().show();
//                                    return false;
                                case R.id.edit:
                                    Gson gson = new Gson();
                                    Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
                                    project.putExtra("process", 4);
                                    project.putExtra("project", projectId);
                                    saveJsonToFile(gson.toJson(mItem));
                                    startActivityForResult(project, Constantes.UPDATE);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    popup.show();
                }
            });

        }
    }

    private void permission() {
        //permisos visualizacion
        if (user.claims.contains("projectsstages.view") || user.IsSuperUser) {
            view.findViewById(R.id.titleStages).setVisibility(View.VISIBLE);
            view.findViewById(R.id.recycler_projects).setVisibility(View.VISIBLE);
        }

        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("projects.update") || user.IsSuperUser) {
                editProjectButton.setVisibility(View.VISIBLE);
            }
            if (user.claims.contains("projectsstages.add") || user.IsSuperUser) {
                view.findViewById(R.id.titleStages).setVisibility(View.VISIBLE);
                view.findViewById(R.id.StagesLayout).setVisibility(View.VISIBLE);
            }

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("projects.update"))
                editProjectButton.setVisibility(View.VISIBLE);
            if (Claims.Claims.contains("projectsstages.add")) {
                view.findViewById(R.id.titleStages).setVisibility(View.VISIBLE);
                view.findViewById(R.id.StagesLayout).setVisibility(View.VISIBLE);
            }
        }
    }

}
