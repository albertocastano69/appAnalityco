package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
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

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

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
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.models.UserProject;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;


public class UsersListProjectFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_PROJECTID = "projectId";
    private static final String ARG_PARAM6 = "param6";
    private static final String ARG_IS_RENDER = "isRender";
    private static final int UPDATE = 1;
    private ClaimsBasicUser Claims;
    private String projectId;
    private String finishdate;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newuserProject;

    private View view;
    private User user;
    private ArrayList<UserProject> userProjec;
    private UserProject itemDelete = new UserProject(null, null, null);
    private UsersRecyclerAdapter adapterProjects;

    public UsersListProjectFragment() {
    }

    public static UsersListProjectFragment newInstance(String projectId, String finishdate, boolean isRender) {

        UsersListProjectFragment fragment = new UsersListProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, projectId);
        args.putString(ARG_PROJECTID, projectId);
        args.putString(ARG_PARAM6, finishdate);
        args.putBoolean(ARG_IS_RENDER, isRender);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {
            Claims = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getClaims();
            projectId = getArguments().getString(ARG_PROJECTID);
            finishdate = getArguments().getString(ARG_PARAM6);
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            boolean isRender = getArguments().getBoolean(ARG_IS_RENDER);
            userProjec = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getUserProjectList();

            if (userProjec != null && isRender) fillListCompanies();
        }
        newuserProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
                project.putExtra("process", 7);
                project.putExtra("project", projectId);
                project.putExtra("company", user.CompanyId);
                project.putExtra("userprojectistInfo", userProjec);
                project.putExtra("finishdate", finishdate);
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

    private void fillListCompanies() {

        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_companies);
        if (userProjec != null) {
            if (userProjec.isEmpty()) {
                recyclerViewProjects.setVisibility(View.GONE);
                return;
            }

            Collections.sort(userProjec, new Comparator<UserProject>() {
                @Override
                public int compare(UserProject projectList, UserProject t1) {
                    return t1.FinishDate.compareTo(projectList.FinishDate);
                }
            });
        }


        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        adapterProjects = new UsersRecyclerAdapter(getActivity(), userProjec);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterProjects);
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                userProjec.remove(itemDelete);
                adapterProjects.notifyDataSetChanged();
                ((DetailsProjectActivityTabs) getActivity()).reloadData();
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
                String[] parts = mItem.RoleName.split(separador);
                if (parts.length >= 2) {
                    holder.profile.setText(parts[1]);
                } else if (parts.length >= 1) {
                    holder.profile.setText(parts[0]);
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
            Drawable drawableIsActive = MaterialDrawableBuilder.with(getActivity())
                    .setIcon(isActive ? MaterialDrawableBuilder.IconValue.THUMB_UP : MaterialDrawableBuilder.IconValue.THUMB_DOWN)
                    .setColor(isActive ? Color.GREEN : Color.RED)
                    .setSizeDp(35)
                    .build();
            holder.logo.setImageResource(R.drawable.profile_dummy);
            if (mItem.Photo != null) {
                String url = Constantes.URL_IMAGES + mItem.Photo;
                Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.profile_dummy)
                        .error(R.drawable.profile_dummy)
                        .into(holder.logo);
            }
            if (Claims != null && !Claims.Claims.contains("users.update") && !user.roles.contains("Innovodata")) {
                holder.btnEdit.setVisibility(View.GONE);
            }
            holder.imageDots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    android.widget.PopupMenu popup = new android.widget.PopupMenu(getActivity(), view);
                    popup.inflate(R.menu.action_menu);
                    popup.getMenu().getItem(0).setEnabled(false);
                    popup.getMenu().getItem(1).setEnabled(false);

                    if (user.IsAdmin || user.IsSuperUser) {
                        if (user.claims.contains("projectsusers.delete") || user.IsSuperUser)
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (user.claims.contains("projectsusers.update") || user.IsSuperUser)
                            popup.getMenu().getItem(1).setEnabled(true);
                    } else if (Claims != null) {
                        if (Claims.Claims.contains("projectsusers.delete"))
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (Claims.Claims.contains("projectsusers.update"))
                            popup.getMenu().getItem(1).setEnabled(true);
                    }

                    popup.setOnMenuItemClickListener(new android.widget.PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(final MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                            .setTitle("Alerta")
                                            .setCancelable(false)
                                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    CrudIntentService.startRequestCRUD(getActivity(),
                                                            Constantes.LIST_PROJECTS_URL + projectId + "/Users/" + mItem.UserId, Request.Method.DELETE, "", "", false);
                                                    itemDelete = mItem;
                                                }
                                            })
                                            .setNegativeButton("No", null)
                                            .setMessage("Desea desvincular este usuario");
                                    builder.create().show();
                                    return false;
                                case R.id.edit:
                                    Gson gson = new Gson();
                                    Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
                                    project.putExtra("process", 8);
                                    project.putExtra("project", projectId);
                                    project.putExtra("company", user.CompanyId);
                                    project.putExtra("userInfo", gson.toJson(mItem));
                                    project.putExtra("finishdate", finishdate);
                                    startActivityForResult(project, UPDATE);
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

        //permisos visualizarcion
        if (user.claims.contains("projectsusers.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("projectsusers.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("projectsusers.add"))
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);
        }
    }
}
