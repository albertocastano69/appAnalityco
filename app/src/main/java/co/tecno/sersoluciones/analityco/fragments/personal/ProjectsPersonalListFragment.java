package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
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
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.EditInfoContractActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

/**
 * Created by Ser Soluciones SAS on 24/02/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ProjectsPersonalListFragment extends Fragment {

    private static final String ARG_CLAIMS = "claims";
    private static final String ARG_DATA = "data";
    private static final String ARG_PROJECTID = "contractId";
    private static final int UPDATE = 1;
    private MyPreferences preferences;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newCompanyProject;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.title2)
    TextView title2;
    @BindView(R.id.recycler_companies)
    RecyclerView recyclerViewProjects;

    private View view;
    private int persoanlId = 0;

    public ProjectsPersonalListFragment() {
    }

    public static ProjectsPersonalListFragment newInstance(String data, int persoanlId, String claims) {
        //Log.e("User2", data);
        ProjectsPersonalListFragment fragment = new ProjectsPersonalListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, data);
        args.putInt(ARG_PROJECTID, persoanlId);
        args.putString(ARG_CLAIMS, claims);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new MyPreferences(getActivity());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {
            (view.findViewById(R.id.card_sub_main)).setVisibility(View.VISIBLE);
            persoanlId = getArguments().getInt(ARG_PROJECTID);
            String fillForm = getArguments().getString(ARG_DATA);
//            logW(fillForm);
            title.setText("Vinculado a los Proyectos");
            title2.setText("Asociar proyecto");
            if (!fillForm.equals("UNAUTHORIZED")) fillListProject(fillForm);
        }
        newCompanyProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                project.putExtra("process", 2);
                project.putExtra("contract", persoanlId);
                startActivityForResult(project, UPDATE);
            }
        });
        permission();
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
        }
    }

    private void fillListProject(String projects) {
        ArrayList<ProjectList> projectsLists;
        projectsLists = new Gson().fromJson(projects,
                new TypeToken<ArrayList<ProjectList>>() {
                }.getType());
        //dates(stagesLists);
        if (projectsLists != null) {
            if (projectsLists.isEmpty()) {
                recyclerViewProjects.setVisibility(View.GONE);
                return;
            }
        }

        //mLayoutManager.setAutoMeasureEnabled(true);
        if (recyclerViewProjects != null) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerViewProjects.setLayoutManager(mLayoutManager);
            ProjectRecyclerAdapter adapterProjects = new ProjectRecyclerAdapter(getActivity(), projectsLists);
            recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                    mLayoutManager.getOrientation());
            recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
            recyclerViewProjects.setAdapter(adapterProjects);
        }
    }

    private class ProjectRecyclerAdapter extends CustomListInfoRecyclerView<ProjectList> {

        ProjectRecyclerAdapter(Context context, ArrayList<ProjectList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ProjectList mItem = mItems.get(pos);
            mItem.Id = mItem.ProjectId;
            holder.textName.setText(mItem.Name);
            holder.textName.setAllCaps(false);
            holder.textName.setTypeface(holder.textName.getTypeface(), Typeface.BOLD);
            holder.textSubName.setText(mItem.CompanyName);
            //holder.btnEdit.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.textValidity.setText(mItem.Address);
            ViewCompat.setTransitionName(holder.logo, mItem.Id);
            if (mItem.Logo != null && !mItem.Logo.equals("null")) {
                String url = Constantes.URL_IMAGES + mItem.Logo;
                //
                //holder.mNetworkImageView.setImageUrl(url, holder.mImageLoader);
                Picasso.get().load(url)
                        .resize(150, 150)
                        .centerCrop()
                        .placeholder(R.drawable.not_project_1)
                        .error(R.drawable.not_project_1)
                        .into(holder.logo);
            } else {
                holder.logo.setImageResource(R.drawable.not_project_1);
            }

           /* holder.stateIcon.setVisibility(View.VISIBLE);
            if (mItem.IsActive) {
                if (mItem.Expiry)
                    holder.stateIcon.setImageResource(R.drawable.state_icon);
                else
                    holder.stateIcon.setVisibility(View.GONE);
            } else {
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
            }*/

            String mDate = mItem.FinishDate;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    Date now = new Date();
                    boolean isActive = true;
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    mDate = dateFormat.format(date);
                    if (date.before(now)) {
                        isActive = false;
                    }
                    if (!isActive) {
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.stateIcon.setImageResource(R.drawable.state_icon_red);
                        // holder.textValidity.setTextColor(getResources().getColor(R.color.bar_undecoded));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textValidity.setText(mItem.Address + " " + mItem.CityName);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            holder.btnEdit.setVisibility(View.GONE);
           /* holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                    project.putExtra("process", 3);
                    project.putExtra("contract", persoanlId);
                    project.putExtra("json", new Gson().toJson(mItem));
                    startActivityForResult(project,UPDATE);
                }
            });*/

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    onProjectInteraction(mItem, holder.logo);
                }
            });
        }
    }

    private void onProjectInteraction(ProjectList item, ImageView imageView) {

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
        preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        //permisos de visualizacion
        if (user.claims.contains("personalsprojects.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);
    }

}
