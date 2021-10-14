package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;

import butterknife.BindView;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.nav.CreatePersonalActivity;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

public class SelectCompanyPermissions extends BaseActivity {

    private User user;
    private ArrayList<CompanyList> admins;
    private CompaniesRecyclerAdapter adapterCompanies;
    @BindView(R.id.message)
    TextView menssage;
    private String permission;
    private Class<? extends Activity> ActivityToOpen = null;
    private boolean isEnrollment = false;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Seleccion Administradora");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        menssage.setText("Por favor seleccione la empresa con la que va a trabajar");

        Intent intent = getIntent();
        permission = intent.getStringExtra("permission");
        selectActivity(permission);

        MyPreferences preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);
        admins = user.Companies;

        RecyclerView recyclerViewPCompanies = findViewById(R.id.recycler_companies);
        if (admins != null && !admins.isEmpty()) {
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerViewPCompanies.setLayoutManager(mLayoutManager);
            adapterCompanies = new CompaniesRecyclerAdapter(this, admins);
            recyclerViewPCompanies.setItemAnimator(new DefaultItemAnimator());
            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewPCompanies.getContext(),
                    mLayoutManager.getOrientation());
            recyclerViewPCompanies.addItemDecoration(mDividerItemDecoration);
            recyclerViewPCompanies.setAdapter(adapterCompanies);
        } else recyclerViewPCompanies.setVisibility(View.GONE);


    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_select_company_permissions);
    }

    private class CompaniesRecyclerAdapter extends CustomListInfoRecyclerView<CompanyList> {

        CompaniesRecyclerAdapter(Context context, ArrayList<CompanyList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final CompanyList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.Name);
            boolean isActive = true;
            holder.textActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);
            holder.topLayout.setVisibility(View.GONE);

            holder.logo.setImageResource(R.drawable.image_not_available);

            Drawable drawableIsActive = MaterialDrawableBuilder.with(mContext)
                    .setIcon(MaterialDrawableBuilder.IconValue.BANK)
                    .setColor(Color.GRAY)
                    .build();
            holder.iconIsActive.setImageDrawable(drawableIsActive);

            holder.textValidity.setText("NIT: " + mItem.DocumentNumber);
            holder.labelValidity.setVisibility(View.GONE);
            if (mItem.Roles != null) holder.textSubName.setText(mItem.Roles[0]);

            if (mItem.Logo != null) {
                String url = Constantes.URL_IMAGES + mItem.Logo;
                Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItem.Permissions.contains(permission) && permission.equals("personals.add")) {
                        Intent intent = new Intent(SelectCompanyPermissions.this, CreatePersonalActivity.class);

                        startActivityForResult(intent, Constantes.CREATE);
                    } else if (mItem.Permissions.contains(permission) && !isEnrollment) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startActivityForResult(new Intent(SelectCompanyPermissions.this, CreatePersonalActivity.class), Constantes.CREATE,
                                    ActivityOptions.makeSceneTransitionAnimation(SelectCompanyPermissions.this).toBundle());
                        } else {
                            startActivityForResult(new Intent(SelectCompanyPermissions.this, CreatePersonalActivity.class), Constantes.CREATE);
                        }


                    } else if (isEnrollment) {
                        Intent intent = new Intent(SelectCompanyPermissions.this, ActivityToOpen);
                        intent.putExtra("companyInfoId", mItem.Id);
                        intent.putExtra("permissions", mItem.Permissions);
                        startActivity(intent);

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                                .setTitle("Alerta")
                                .setCancelable(false)
                                .setPositiveButton("ok", null)
                                .setMessage("El usuario con esta empresa administradora no posee el permiso de creación, por favor contacte al administrador.");
                        builder.create().show();
                    }

                }
            });
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectActivity(String permission) {

        switch (permission) {

            case "personals.add":
                ActivityToOpen = CreatePersonalActivity.class;
                break;
            case "contracts.add":
                ActivityToOpen = ContractActivity.class;
                break;
            case "employers.add":
                ActivityToOpen = EmployerActivity.class;
                break;
            case "enrollment":
                isEnrollment = true;
                ActivityToOpen = EnrollmentActivity.class;
                break;


        }
    }
}
