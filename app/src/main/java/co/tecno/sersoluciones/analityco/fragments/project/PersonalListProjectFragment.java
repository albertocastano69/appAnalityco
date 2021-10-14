package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsPersonalActivityTabs;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.PersonalListActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;


public class PersonalListProjectFragment extends Fragment {

    private static final String ARG_PROJECTID = "projectId";
    private static final String ARG_IS_RENDER = "isRender";
    private static final int UPDATE = 1;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newuserProject;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.title2)
    TextView title2;

    private View view;
    private User user;

    public PersonalListProjectFragment() {
    }

    public static PersonalListProjectFragment newInstance(String projectId, boolean isRender) {

        PersonalListProjectFragment fragment = new PersonalListProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, projectId);
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
            boolean isRender = getArguments().getBoolean(ARG_IS_RENDER);
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            title.setText(R.string.personal_vinculado);
            title2.setText(R.string.agregar_personal);
            ArrayList<PersonalList> personalProject = ((DetailsProjectActivityTabs) getActivity()).dataFragment.getPersonalList();

            if (personalProject != null && isRender)
                fillListPersonal(personalProject);
        }
        newuserProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), PersonalListActivity.class);
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

    private void fillListPersonal(ArrayList<PersonalList> userProjec) {
        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_companies);
        if (userProjec != null) {
//            if (userProjec.isEmpty()) {
//                recyclerViewProjects.setVisibility(View.GONE);
//                return;
//            }

            Collections.sort(userProjec, new Comparator<PersonalList>() {
                @Override
                public int compare(PersonalList projectList, PersonalList t1) {
                    return t1.FinishDate.compareTo(projectList.FinishDate);
                }
            });
        }

        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        PersonalRecyclerAdapter adapterProjects = new PersonalRecyclerAdapter(getActivity(), userProjec);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterProjects);
    }

    private class PersonalRecyclerAdapter extends CustomListInfoRecyclerViewUsers<PersonalList> {

        PersonalRecyclerAdapter(Context context, ArrayList<PersonalList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final PersonalList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.LastName);
            holder.textSubName.setText(mItem.JobName);
            holder.textValidity2.setText("CC " + mItem.DocumentNumber);
            holder.profile.setText("Vigencia en el proyecto");
            holder.user_icon.setVisibility(View.GONE);
            boolean isActive = true;
            Date now = new Date();
            String mDate = mItem.FinishDate;
            if (mItem.PhoneNumber == null) {
                holder.phone.setVisibility(View.GONE);
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
                    if (!isActive) {
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.stateIcon.setImageResource(R.drawable.state_icon_red);
                        holder.textValidity.setTextColor(getResources().getColor(R.color.bar_undecoded));
                        holder.calendar.setColorResource(R.color.bar_undecoded);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textValidity.setText(mDate);
            holder.logo.setImageResource(R.drawable.image_not_available);
            if (mItem.Photo != null) {
                String url = Constantes.URL_IMAGES + mItem.Photo;
                Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            }

            holder.btnEdit.setVisibility(View.GONE);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onListFragmentInteraction(mItem, holder.logo);
                }
            });
        }
    }

    private void onListFragmentInteraction(PersonalList personalList, ImageView imageView) {

        ViewCompat.setTransitionName(imageView, String.valueOf(personalList.Id));

        Intent i = new Intent(getActivity(), DetailsPersonalActivityTabs.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.ITEM_OBJ, personalList);
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
        if (user.claims.contains("projectspersonals.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);
    }
}
