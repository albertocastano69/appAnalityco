package co.tecno.sersoluciones.analityco.steps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.google.gson.Gson;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import co.tecno.sersoluciones.analityco.ProjectActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.fragments.project.NewProjectStagesFragment;
import co.tecno.sersoluciones.analityco.models.Project;
import co.tecno.sersoluciones.analityco.models.ProjectStages;

/**
 * Created by Ser Soluciones SAS on 08/02/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class ProjectStep3 extends Fragment {

    private static final int REQUEST_CODE = 1000;

    private String fromDateProject;
    private String toDateProject;
    private Date toDate;
    private Date fromDate;
    private ArrayList<ProjectStages> stagesProjects;

    private ProjectRecyclerAdapter customRecyclerAdapter;
    private NewProjectStagesFragment newProjectStageFragment;
    private FrameLayout fragmnetNewStage;

    private View layoutStages;
    private RecyclerView recyclerview;

    //Set your layout here
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.project_step3, container, false);
        recyclerview = v.findViewById(R.id.recyclerCompany);
        stagesProjects = new ArrayList<>();
        customRecyclerAdapter = new ProjectRecyclerAdapter(getActivity(), stagesProjects);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerview.setAdapter(customRecyclerAdapter);

        layoutStages = v.findViewById(R.id.layout_stages);
        FloatingActionButton newCompany = v.findViewById(R.id.addCompanyProject);
        fragmnetNewStage = v.findViewById(R.id.container_join_company);
        addFragmentStage();
        newCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFragmentStage();
            }
        });
        layoutStages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragmentStage();
            }
        });
        return v;
    }

    private void addFragmentStage() {
        fragmnetNewStage.setVisibility(View.VISIBLE);
        newProjectStageFragment = NewProjectStagesFragment.newInstance();
        newProjectStageFragment.setTargetFragment(ProjectStep3.this, REQUEST_CODE);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join_company, newProjectStageFragment);
        fragmentTransaction.commit();
        layoutStages.setVisibility(View.GONE);
        recyclerview.setVisibility(View.GONE);
    }

    private void removeFragmentStage() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(newProjectStageFragment);
        fragmentTransaction.commit();
        fragmnetNewStage.setVisibility(View.GONE);
        layoutStages.setVisibility(View.VISIBLE);
        recyclerview.setVisibility(View.VISIBLE);
    }

    public void submitRequest() {
        if (stagesProjects.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("Debe crear por lo menos una etapa para seguir")
                    .setPositiveButton("Aceptar", null);
            builder.create().show();
            return;
        }
        Project project = ((ProjectActivity) getActivity()).getProject();
        project.ProjectStageList = stagesProjects;
        project.StartDate = fromDateProject;
        project.FinishDate = toDateProject;
        project.toDate = toDate;
        project.CompanyInfoId = project.CompanyId;
        ((ProjectActivity) getActivity()).setProject(project);
        ((ProjectActivity) getActivity()).nextPage(2);
        final String json = new Gson().toJson(project);
    }

    private void newItem(ProjectStages stages) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if (stagesProjects.size() <= 0) {
            try {
                fromDate = format.parse(stages.StartDate);
                toDate = format.parse(stages.FinishDate);
                fromDateProject = stages.StartDate;
                toDateProject = stages.FinishDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Date date1 = format.parse(stages.StartDate);
                if (fromDate.after(date1)) {
                    fromDate = date1;
                    fromDateProject = stages.StartDate;
                }
                if (toDate.after(date1)) {
                    toDate = date1;
                    toDateProject = stages.StartDate;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        stagesProjects.add(stages);
        customRecyclerAdapter.notifyItemInserted(stagesProjects.size() - 1);
        customRecyclerAdapter.notifyDataSetChanged();
        layoutStages.setVisibility(View.VISIBLE);
    }

    private class ProjectRecyclerAdapter extends CustomListInfoRecyclerView<ProjectStages> {

        ProjectRecyclerAdapter(Context context, ArrayList<ProjectStages> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ProjectStages mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textSubName.setText(mItem.Review);
            //elementos ocultos
            holder.logo.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);
            holder.iconIsActive.setVisibility(View.GONE);
            holder.dateIcon.setVisibility(View.VISIBLE);
            holder.cardViewDetail.setContentPadding(7, 2, 2, 2);

            String finishDateStr = mItem.FinishDate;
            boolean isActive = false;
            Date finishDate;
            try {
                if (!finishDateStr.isEmpty() && !finishDateStr.equals(" ")) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

                    finishDate = format.parse(finishDateStr);
                    finishDateStr = dateFormat.format(finishDate);

                    if (finishDate.after(new Date()))
                        isActive = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.textValidity.setText(finishDateStr);
            if (!isActive) {
                holder.textValidity.setTextColor(getResources().getColor(R.color.red));
                holder.dateIcon.setColor(getResources().getColor(R.color.red));
            }
//            Drawable drawableIsActive = MaterialDrawableBuilder.with(getActivity())
//                    .setIcon(isActive ? MaterialDrawableBuilder.IconValue.THUMB_UP : MaterialDrawableBuilder.IconValue.THUMB_DOWN)
//                    .setColor(isActive ? Color.GREEN : Color.RED)
//                    .setSizeDp(35)
//                    .build();
//            holder.iconIsActive.setImageDrawable(drawableIsActive);
//            holder.logo.setImageResource(R.drawable.image_not_available);
            holder.btnEdit.setImageResource(R.drawable.ic_dots_vertical);
            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(getActivity(), view);
                    popup.inflate(R.menu.action_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    stagesProjects.remove(mItem);
                                    customRecyclerAdapter.notifyItemRemoved(pos);
                                    customRecyclerAdapter.notifyDataSetChanged();
                                    return true;
                                case R.id.edit:
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("mItem", mItem);
                                    newProjectStageFragment = NewProjectStagesFragment.newInstance();
                                    newProjectStageFragment.setArguments(bundle);
                                    fragmnetNewStage.setVisibility(View.VISIBLE);
                                    newProjectStageFragment.setTargetFragment(ProjectStep3.this, REQUEST_CODE);
                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.add(R.id.container_join_company, newProjectStageFragment);
                                    fragmentTransaction.commit();
                                    layoutStages.setVisibility(View.GONE);
                                    recyclerview.setVisibility(View.GONE);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    ProjectStages stage = data.getParcelableExtra("newStage");
                    boolean remove = (boolean) data.getSerializableExtra("isEdit");
                    if (remove) {
                        ProjectStages stageRemove = data.getParcelableExtra("removeItem");
                        stagesProjects.remove(stageRemove);
                        customRecyclerAdapter.notifyDataSetChanged();
                    }
                    newItem(stage);
                    removeFragmentStage();
                    break;
                case Activity.RESULT_CANCELED:
                    removeFragmentStage();
                    break;
            }
        }
    }

}