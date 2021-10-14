package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import co.tecno.sersoluciones.analityco.DetailsEmployerActivity;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

public class LinkedCompaniesListProjectFragment extends Fragment {

    private static final String ARG_PROJECTID = "projectId";

    @BindView(R.id.titleList)
    TextView titleList;

    private View view;
    private User user;

    public LinkedCompaniesListProjectFragment() {
    }

    public static LinkedCompaniesListProjectFragment newInstance(String projectId) {

        LinkedCompaniesListProjectFragment fragment = new LinkedCompaniesListProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.company_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        titleList.setText("Empresas Vinculadas");
        if (getArguments() != null) {
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            permission();
            fillListCompanies();
        }
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
        }
    }

    private void fillListCompanies() {

        ArrayList<CompanyProject> linkedCompanyProjec = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getLinkedCompanyDetails();
        ArrayList<CompanyProject> companyProjects = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getCompanyProjectList();

        ArrayList<CompanyProject> removeItems = new ArrayList<>();
        for (CompanyProject item : companyProjects) {
            for (CompanyProject item2 : linkedCompanyProjec)
                if (item.CompanyInfoId.equals(item2.Id))
                    removeItems.add(item2);
        }
        linkedCompanyProjec.removeAll(removeItems);

        Collections.sort(linkedCompanyProjec, new Comparator<CompanyProject>() {
            @Override
            public int compare(CompanyProject projectList, CompanyProject t1) {
                return Boolean.compare(projectList.IsActive, t1.IsActive);
            }
        });

        Collections.sort(linkedCompanyProjec, new Comparator<CompanyProject>() {
            @Override
            public int compare(CompanyProject projectList, CompanyProject t1) {
                return t1.FinishDate.compareTo(projectList.FinishDate);
            }
        });

        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_companies);
        if (linkedCompanyProjec.isEmpty()) {
            recyclerViewProjects.setVisibility(View.GONE);
            return;
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        LinkedCompaniesRecycler adapterLinkedCompanies = new LinkedCompaniesRecycler(getActivity(), linkedCompanyProjec);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterLinkedCompanies);
    }

    private class LinkedCompaniesRecycler extends CustomListInfoRecyclerView<CompanyProject> {

        LinkedCompaniesRecycler(Context context, ArrayList mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final CompanyProject mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.Name);
            boolean isActive = true;
            holder.textActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.topLayout.setVisibility(View.VISIBLE);
            holder.imageDots.setVisibility(View.GONE);
            holder.textJob.setText("Vigencia empresa vinculada");

            holder.logo.setImageResource(R.drawable.image_not_available);
            if (mItem.Rol.equals("Empresa Administradora")) {
                Drawable drawableIsActive = MaterialDrawableBuilder.with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.BANK)
                        .setColor(Color.GRAY)
                        .build();
                holder.iconIsActive.setImageDrawable(drawableIsActive);
                holder.topLayout.setVisibility(View.GONE);
            } else {
                Drawable drawableIsActive = MaterialDrawableBuilder.with(getActivity())
                        .setIcon(MaterialDrawableBuilder.IconValue.DOMAIN)
                        .setColor(Color.GRAY)
                        .build();
                holder.iconIsActive.setImageDrawable(drawableIsActive);
            }
            holder.textValidity.setText("NIT: " + mItem.DocumentNumber);
            holder.labelValidity.setVisibility(View.GONE);
            holder.textSubName.setText(mItem.Rol);
            //Log.e("logo",mItem.Logo);
            if (mItem.Logo != null) {
                String url = Constantes.URL_IMAGES + mItem.Logo;

                Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            }

            String mDate = mItem.FinishDate;
            Date now = new Date();
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
                        holder.textTopDate.setTextColor(getResources().getColor(R.color.bar_undecoded));
                        holder.topIconDate.setColorResource(R.color.bar_undecoded);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textTopDate.setText(mDate);
            holder.mView.setOnClickListener(view -> {

                if (!mItem.Rol.equals("Empresa Administradora")) {
                    openEmployer(mItem, holder.logo);
                }
            });
        }
    }

    public void openEmployer(CompanyProject item, ImageView imageView) {

        ObjectList mItem = new ObjectList(item.CompanyInfoId, item.Name, item.DocumentNumber, item.Logo, item.DocumentType);
        Intent i = new Intent(getContext(), DetailsEmployerActivity.class);
        i.putExtra(Constantes.ITEM_OBJ, mItem);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));
        startActivity(i);

    }

    private void permission() {

        //permisos visualizarcion
        if (user.IsSuperUser || user.claims.contains("projectsemployers.view"))
            view.findViewById(R.id.recycler_companies).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);
    }

}
