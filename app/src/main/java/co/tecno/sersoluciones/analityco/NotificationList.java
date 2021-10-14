package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Parcelable;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.Notification;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

public class NotificationList extends BaseActivity {

    private int item;
    private Notification notification;
    private RecyclerView projectsRecycler;
    private RecyclerView contractsRecycler;
    private RecyclerView employersRecycler;
    private RecyclerView personalRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Notificaciones");

        preferences = new MyPreferences(this);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        Gson gson = new Gson();
        notification = gson.fromJson((String) bd.get("Notifiation"), new TypeToken<Notification>() {
        }.getType());
        item = (int) bd.get("item");


        final Button projects = findViewById(R.id.project);
        final Button contracts = findViewById(R.id.contract);
        final Button employers = findViewById(R.id.employer);
        final Button personal = findViewById(R.id.personal);
        projectsRecycler = findViewById(R.id.frameProject);
        contractsRecycler = findViewById(R.id.frameContract);
        employersRecycler = findViewById(R.id.frameEmployer);
        personalRecycler = findViewById(R.id.framePersonal);

        filldatesRecycler();

        MyPreferences preferences = new MyPreferences(this);

        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);

        if (user.claims.contains("projects.view") || user.roles.contains("Innovodata")) {
            projects.setVisibility(View.VISIBLE);
        }
        if (user.claims.contains("employers.view") || user.roles.contains("Innovodata")) {
            employers.setVisibility(View.VISIBLE);
        }
        if (user.claims.contains("personals.view") || user.roles.contains("Innovodata")) {
            personal.setVisibility(View.VISIBLE);
        }
        if (user.claims.contains("contracts.view") || user.roles.contains("Innovodata")) {
            contracts.setVisibility(View.VISIBLE);
        }

        if (item == 1) {
            personalRecycler.setVisibility(View.VISIBLE);
            personal.setBackgroundResource(R.drawable.spinner_background_up);
        } else if (item == 2) {
            projectsRecycler.setVisibility(View.VISIBLE);
            projects.setBackgroundResource(R.drawable.spinner_background_up);
        } else if (item == 3) {
            employersRecycler.setVisibility(View.VISIBLE);
            employers.setBackgroundResource(R.drawable.spinner_background_up);
        } else if (item == 4) {
            contractsRecycler.setVisibility(View.VISIBLE);
            contracts.setBackgroundResource(R.drawable.spinner_background_up);
        }

        projects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (projectsRecycler.getVisibility() == View.GONE) {
                    projectsRecycler.setVisibility(View.VISIBLE);
                    projects.setBackgroundResource(R.drawable.spinner_background_up);
                } else {
                    projectsRecycler.setVisibility(View.GONE);
                    projects.setBackgroundResource(R.drawable.spinner_background);
                }
            }
        });
        contracts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contractsRecycler.getVisibility() == View.GONE) {
                    contractsRecycler.setVisibility(View.VISIBLE);
                    contracts.setBackgroundResource(R.drawable.spinner_background_up);
                } else {
                    contractsRecycler.setVisibility(View.GONE);
                    contracts.setBackgroundResource(R.drawable.spinner_background);
                }
            }
        });
        employers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (employersRecycler.getVisibility() == View.GONE) {
                    employersRecycler.setVisibility(View.VISIBLE);
                    employers.setBackgroundResource(R.drawable.spinner_background_up);
                } else {
                    employersRecycler.setVisibility(View.GONE);
                    employers.setBackgroundResource(R.drawable.spinner_background);
                }
            }
        });
        personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (personalRecycler.getVisibility() == View.GONE) {
                    personalRecycler.setVisibility(View.VISIBLE);
                    personal.setBackgroundResource(R.drawable.spinner_background_up);
                } else {
                    personalRecycler.setVisibility(View.GONE);
                    personal.setBackgroundResource(R.drawable.spinner_background);
                }
            }
        });
    }

    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_notification_list);
    }

    private void filldatesRecycler() {


        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        projectsRecycler.setLayoutManager(mLayoutManager);
        ProjectRecyclerAdapter adapterProjects = new ProjectRecyclerAdapter(this, notification.Projects);
        projectsRecycler.setItemAnimator(new DefaultItemAnimator());
        projectsRecycler.setAdapter(adapterProjects);

        LinearLayoutManager mLayoutManager2 = new LinearLayoutManager(this);
        contractsRecycler.setLayoutManager(mLayoutManager2);
        ContractRecyclerAdapter adapterContract = new ContractRecyclerAdapter(this, notification.Contracts);
        contractsRecycler.setItemAnimator(new DefaultItemAnimator());
        contractsRecycler.setAdapter(adapterContract);

        LinearLayoutManager mLayoutManager3 = new LinearLayoutManager(this);
        employersRecycler.setLayoutManager(mLayoutManager3);
        EmployerRecyclerAdapter adapterEmployer = new EmployerRecyclerAdapter(this, notification.Employers);
        employersRecycler.setItemAnimator(new DefaultItemAnimator());
        employersRecycler.setAdapter(adapterEmployer);

        LinearLayoutManager mLayoutManager4 = new LinearLayoutManager(this);
        personalRecycler.setLayoutManager(mLayoutManager4);
        PersonalRecyclerAdapter adapterPersonal = new PersonalRecyclerAdapter(this, notification.Personal);
        personalRecycler.setItemAnimator(new DefaultItemAnimator());
        personalRecycler.setAdapter(adapterPersonal);
    }

    private class ContractRecyclerAdapter extends CustomListInfoRecyclerViewUsers<ObjectList> {

        ContractRecyclerAdapter(Context context, ArrayList<ObjectList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ObjectList mItem = mItems.get(pos);
            holder.textName.setText(mItem.ContractorName);
            holder.textSubName.setText("No. " + mItem.ContractNumber);
            holder.textValidity2.setText("Expira en " + mItem.DaysToExpire + " dias");
            holder.vigence.setVisibility(View.GONE);
            holder.user_icon.setImageResource(R.drawable.ic_etapa1);
            holder.phone.setVisibility(View.GONE);
            holder.logo.setImageResource(R.drawable.image_not_available);

            if (mItem.FormImageLogo != null) {
                String[] format = mItem.FormImageLogo.split(Pattern.quote("."));
                Log.e("image", "ok" + mItem.FormImageLogo);
                String url = Constantes.URL_IMAGES + mItem.FormImageLogo;
//                if (format[format.length - 1].equals("svg")) {
//                    Uri uri = Uri.parse(url);
//                    holder.requestBuilder
//                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                            .load(uri)
//                            .into(holder.logo);
//                } else {
                    Picasso.get().load(url)
                            .resize(0, 250)
                            .placeholder(R.drawable.image_not_available)
                            .error(R.drawable.image_not_available)
                            .into(holder.logo);
//                }
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parcelablePersonalInteraction(holder.logo, mItem, DetailsContractsActivityTabs.class);
                }
            });
            holder.btnEdit.setVisibility(View.GONE);

        }
    }

    private class EmployerRecyclerAdapter extends CustomListInfoRecyclerView<ObjectList> {

        EmployerRecyclerAdapter(Context context, ArrayList<ObjectList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ObjectList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textSubName.setText(mItem.Address);
            //holder.btnEdit.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);

            holder.textValidity.setText("Expira en " + mItem.DaysToExpire + " dias");

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

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parcelablePersonalInteraction(holder.logo, mItem, DetailsEmployerActivity.class);
                }
            });
            holder.btnEdit.setVisibility(View.GONE);
        }
    }

    private class ProjectRecyclerAdapter extends CustomListInfoRecyclerView<ProjectList> {

        ProjectRecyclerAdapter(Context context, ArrayList<ProjectList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ProjectList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textSubName.setText(mItem.ProjectNumber);
            holder.textActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.textValidity.setText("Expira en " + mItem.DaysToExpire + " dias");

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
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parcelablePersonalInteraction(holder.logo, mItem, DetailsProjectActivityTabs.class);
                }
            });

            holder.stateIcon.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);

        }
    }

    private class PersonalRecyclerAdapter extends CustomListInfoRecyclerViewUsers<PersonalList> {

        PersonalRecyclerAdapter(Context context, ArrayList<PersonalList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final PersonalList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.LastName);
            holder.textSubName.setText("CC " + mItem.DocumentNumber);
            holder.phone.setVisibility(View.GONE);
            holder.textValidity2.setText("Expira en " + mItem.DaysToExpire + " dias");
            holder.logo.setImageResource(R.drawable.image_not_available);
            if (mItem.Photo != null) {
                String url = Constantes.URL_IMAGES + mItem.Photo;

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            }

            holder.btnEdit.setVisibility(View.GONE);
            holder.vigence.setVisibility(View.GONE);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parcelablePersonalInteraction(holder.logo, mItem, DetailsPersonalActivityTabs.class);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void parcelablePersonalInteraction(ImageView imageView, Object object, final Class<? extends Activity> ActivityToOpen) {

        Intent i = new Intent(this, ActivityToOpen);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.ITEM_OBJ, (Parcelable) object);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));
        startActivity(i);
    }

}
