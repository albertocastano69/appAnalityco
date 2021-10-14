package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.ViewCompat;
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

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsCompanyActivityTabs;
import co.tecno.sersoluciones.analityco.DetailsEmployerActivity;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.EditInfoProjectActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

public class CompanyListProjectFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_PROJECTID = "projectId";
    private static final int UPDATE = 1;
    private ClaimsBasicUser Claims;
    private String projectId;


    @BindView(R.id.addCompanyProject)
    FloatingActionButton newCompanyProject;

    private View view;
    private User user;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private ArrayList<CompanyProject> companyProjec;
    private CompaniesRecyclerAdapter adapterProjects;
    private CompanyProject itemDelete = new CompanyProject();

    public CompanyListProjectFragment() {
    }

    public static CompanyListProjectFragment newInstance(String projectId) {

        CompanyListProjectFragment fragment = new CompanyListProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PROJECTID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.company_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {
            Claims = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getClaims();

            projectId = getArguments().getString(ARG_PROJECTID);
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            permission();
            fillListCompanies();
        }
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        newCompanyProject.setOnClickListener(view -> {
            Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
            project.putExtra("process", 5);
            project.putExtra("project", projectId);
            project.putExtra("company", user.CompanyId);
            startActivityForResult(project, UPDATE);
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

    private void fillListCompanies() {

        companyProjec = ((DetailsProjectActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getCompanyProjectList();

        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_companies);
        if (companyProjec.isEmpty()) {
            recyclerViewProjects.setVisibility(View.GONE);
            return;
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        adapterProjects = new CompaniesRecyclerAdapter(getActivity(), companyProjec);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterProjects);
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {
        logW("option" + option);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                companyProjec.remove(itemDelete);
                adapterProjects.notifyDataSetChanged();
                ((DetailsProjectActivityTabs) getActivity()).reloadData();
                break;
            case Constantes.BAD_REQUEST:
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    String message = jsonObject.getString("Error");
                    message = message.replace("[\"", "");
                    message = message.replace("\"]", "");
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setTitle("Error")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setMessage(message);
                    builder.create().show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private class CompaniesRecyclerAdapter extends CustomListInfoRecyclerView<CompanyProject> {

        CompaniesRecyclerAdapter(Context context, ArrayList<CompanyProject> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final CompanyProject mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.Name);
            boolean isActive = true;
            holder.textActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.topLayout.setVisibility(View.VISIBLE);

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

            holder.imageDots.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(getActivity(), view);
                    popup.inflate(R.menu.action_menu);
                    popup.getMenu().getItem(0).setEnabled(false);
                    popup.getMenu().getItem(1).setEnabled(false);
                    if (user.IsAdmin || user.IsSuperUser) {
                        if (user.claims.contains("projectscompanies.delete") || user.IsSuperUser)
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (user.claims.contains("projectscompanies.update") || user.IsSuperUser)
                            popup.getMenu().getItem(1).setEnabled(true);
                    } else if (Claims != null) {
                        if (Claims.Claims.contains("projectscompanies.delete"))
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (Claims.Claims.contains("projectscompanies.update"))
                            popup.getMenu().getItem(1).setEnabled(true);
                    }

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                            .setTitle("Alerta")
                                            .setCancelable(false)
                                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    CrudIntentService.startRequestCRUD(getActivity(),
                                                            Constantes.LIST_PROJECTS_BY_COMPANY_URL + projectId + "/JoinCompanies/" + mItem.getId(), Request.Method.DELETE, "", "", false);
                                                    itemDelete = mItem;
                                                }
                                            })
                                            .setNegativeButton("No", null)
                                            .setMessage("Desea desvincular esta compañia");
                                    builder.create().show();

                                    return false;
                                case R.id.edit:
                                    Gson gson = new Gson();
                                    Intent project = new Intent(getActivity(), EditInfoProjectActivity.class);
                                    project.putExtra("process", 6);
                                    project.putExtra("project", projectId);
                                    project.putExtra("company", user.CompanyId);
                                    project.putExtra("companyInfo", gson.toJson(mItem));
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

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!mItem.Rol.equals("Empresa Administradora")) {
                        openEmployer(mItem, holder.logo, DetailsEmployerActivity.class, false);
                    } else {
                        openEmployer(mItem, holder.logo, DetailsCompanyActivityTabs.class, true);
                    }
                }
            });
        }
    }

    public void openEmployer(CompanyProject item, ImageView imageView, Class<? extends Activity> ActivityToOpen, boolean isAdmin) {

        ObjectList objectList = new ObjectList(item.CompanyInfoId, item.Name, item.DocumentNumber, item.Logo, item.DocumentType);
        CompanyList companyList = new CompanyList(item.Name,item.DocumentType,item.DocumentNumber);
        Intent i = new Intent(getContext(), ActivityToOpen);
        if (isAdmin) {
            Bitmap bitmap;
            byte[] byteArray = new byte[0];
            try {
                bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bStream);
                byteArray = bStream.toByteArray();
            } catch (Exception ignored) {
                return;
            }
            i.putExtra("id", item.CompanyInfoId);
            i.putExtra("image", byteArray);
            i.putExtra("model", companyList);
        } else {
            i.putExtra(Constantes.ITEM_OBJ, objectList);
            i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));
        }
        startActivity(i);

    }

    private void permission() {

        //permisos visualizarcion
        if (user.claims.contains("projectscompanies.view") || user.IsSuperUser)
            view.findViewById(R.id.recycler_companies).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("projectscompanies.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("projectscompanies.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);
        }
    }

}
