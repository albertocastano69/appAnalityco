package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
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
import co.tecno.sersoluciones.analityco.EditInfoContractActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;


public class SubContractCompanyFragment extends Fragment {

    private static final String ARG_DATA = "data";
    private static final String ARG_PROJECTID = "contractId";
    private static final String ARG_ISREGISTER = "isRegister";
    private static final int UPDATE = 1;

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
    private String contratId = "";

    public SubContractCompanyFragment() {
    }

    public static SubContractCompanyFragment newInstance(String data, String contractId, boolean isRegister) {
        SubContractCompanyFragment fragment = new SubContractCompanyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, data);
        args.putString(ARG_PROJECTID, contractId);
        args.putBoolean(ARG_ISREGISTER, isRegister);
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
            contratId = getArguments().getString(ARG_PROJECTID);
            String fillForm = getArguments().getString(ARG_DATA);

            (view.findViewById(R.id.card_sub_main)).setVisibility(View.VISIBLE);

            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            title.setText("Subcontratistas del contrato");
            title2.setText("Asociar proyecto");
            if (!fillForm.equals("UNAUTHORIZED")) fillListCompany(fillForm);
        }

       /* requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        newCompanyProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                project.putExtra("process", 2);
                project.putExtra("contract", contratId);
                startActivityForResult(project, UPDATE);
            }
        });*/
        permission();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void fillListCompany(String projects) {
        ArrayList<CompanyList> companiesLists = new Gson().fromJson(projects,
                new TypeToken<ArrayList<CompanyList>>() {
                }.getType());
        //dates(stagesLists);

        if (companiesLists.isEmpty()) {

            recyclerViewProjects.setVisibility(View.GONE);
            return;
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        if (recyclerViewProjects != null) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerViewProjects.setLayoutManager(mLayoutManager);
            SubContractAdapter adapterProjects = new SubContractAdapter(getActivity(), companiesLists);
            recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                    mLayoutManager.getOrientation());
            recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
            recyclerViewProjects.setAdapter(adapterProjects);
        }
    }

    private class SubContractAdapter extends CustomListInfoRecyclerViewUsers<CompanyList> {

        SubContractAdapter(Context context, ArrayList<CompanyList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final CompanyList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textSubName.setText(mItem.Rol);
            holder.user_icon.setVisibility(View.GONE);
            holder.imgStage.setVisibility(View.VISIBLE);
            holder.textValidity2.setText(mItem.DocumentType + ": " + mItem.DocumentNumber);
            holder.vigence.setVisibility(View.GONE);

            //holder.btnEdit.setVisibility(View.GONE);
            //holder.textActive.setVisibility(View.GONE);

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
                        holder.textValidity.setTextColor(getResources().getColor(R.color.bar_undecoded));
                        holder.calendar.setColorResource(R.color.bar_undecoded);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textValidity.setText(mDate);

            if (mItem.Logo != null && !mItem.Logo.equals("null")) {
                String url = Constantes.URL_IMAGES + mItem.Logo;

                //holder.mNetworkImageView.setImageUrl(url, holder.mImageLoader);
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            } else {
                holder.logo.setImageResource(R.drawable.image_not_available);
            }
            holder.phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItem.Phone != null) {
                        PopupMenu popup = new PopupMenu(mContext, holder.phone);
                        popup.getMenuInflater()
                                .inflate(R.menu.menu_call, popup.getMenu());
                        popup.getMenu().getItem(0).setTitle("Marcar");
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent i = new Intent(Intent.ACTION_DIAL,
                                        Uri.parse("tel:" + mItem.Phone));
                                mContext.startActivity(i);
                                return true;
                            }
                        });
                        popup.show();
                    }
                }
            });
          /*  holder.stateIcon.setVisibility(View.VISIBLE);
            if (mItem.IsActive) {
                if (mItem.Expiry)
                    holder.stateIcon.setImageResource(R.drawable.state_icon);
                else
                    holder.stateIcon.setVisibility(View.GONE);
            } else {
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
            }
            if (!Claims.Claims.contains("projects.update") && !user.roles.contains("Innovodata")) {
                holder.btnEdit.setVisibility(View.GONE);
            }*/

            //if (isRegister)
            holder.btnEdit.setVisibility(View.GONE);

            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent project = new Intent(getActivity(), EditInfoContractActivity.class);
                    project.putExtra("process", 3);
                    project.putExtra("contract", contratId);
                    project.putExtra("json", new Gson().toJson(mItem));
                    startActivityForResult(project, UPDATE);
                }
            });
        }
    }

    private void permission() {

        //permisos visualizarcion
        if (user.claims.contains("contractssubemployers.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);
    }

}

