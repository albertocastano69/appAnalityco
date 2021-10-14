package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.EditInfoContractActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.models.EmployerList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

/**
 * Created by Ser Soluciones SAS on 26/02/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EmployerPersonalListFragment extends Fragment {

    private static final String ARG_DATA = "data";
    private static final String ARG_PROJECTID = "contractId";
    private static final String ARG_CLAIMS = "claims";
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

    public EmployerPersonalListFragment() {
    }

    public static EmployerPersonalListFragment newInstance(String data, int persoanlId, String claims) {

        EmployerPersonalListFragment fragment = new EmployerPersonalListFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {

            (view.findViewById(R.id.card_sub_main)).setVisibility(View.VISIBLE);
            persoanlId = getArguments().getInt(ARG_PROJECTID);
            String fillForm = getArguments().getString(ARG_DATA);

            title.setText("Vinculado a los Empleadores");
            title2.setText("Asociar proyecto");
            if (!fillForm.equals("UNAUTHORIZED")) fillListEmployers(fillForm);
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

    private void fillListEmployers(String employers) {

        ArrayList<EmployerList> employerList = new Gson().fromJson(employers,
                new TypeToken<ArrayList<EmployerList>>() {
                }.getType());
        RecyclerView recyclerViewEmployers = view.findViewById(R.id.recycler_companies);
        if (employerList != null) {
            if (employerList.isEmpty()) {
                recyclerViewEmployers.setVisibility(View.GONE);
                return;
            }
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewEmployers.setLayoutManager(mLayoutManager);
        EmployeRecyclerAdapter adapter = new EmployeRecyclerAdapter(getActivity(), employerList);
        recyclerViewEmployers.setItemAnimator(new DefaultItemAnimator());
        //DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewEmployers.getContext(),
        //        mLayoutManager.getOrientation());
        //recyclerViewEmployers.addItemDecoration(mDividerItemDecoration);
        recyclerViewEmployers.setAdapter(adapter);
    }

    private class EmployeRecyclerAdapter extends CustomListInfoRecyclerView<EmployerList> {

        EmployeRecyclerAdapter(Context context, ArrayList<EmployerList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            EmployerList mItem = mItems.get(pos);
            holder.textName.setText(mItem.NameCompany);
            holder.textSubName.setText(mItem.RolEmployer);
            holder.btnEdit.setVisibility(View.GONE);

            holder.textValidity.setText(String.format("%s.: %s", mItem.DocumentTypeEmployer, mItem.DocumentNumberEmployer));

            holder.iconIsActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);

            if (mItem.LogoEmployer != null && !mItem.LogoEmployer.equals("null")) {
                String url = Constantes.URL_IMAGES + mItem.LogoEmployer;
                //
                //holder.mNetworkImageView.setImageUrl(url, holder.mImageLoader);

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            } else {
                holder.logo.setImageResource(R.drawable.image_not_available);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }

    private void permission() {
        preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        //permisos de visualizacion
        if (user.claims.contains("personalsemployers.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

    }
}
