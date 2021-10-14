package co.tecno.sersoluciones.analityco.fragments.company;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.picasso.Picasso;
import com.tokenautocomplete.TokenCompleteTextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsProjectActivityTabs;
import co.tecno.sersoluciones.analityco.NewUserAdminActivity;
import co.tecno.sersoluciones.analityco.ProjectsListActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.BranchOfficesInfoRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.adapters.CustomListProyectRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.company.MyCompanyRecyclerViewAdapter;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.models.UserAdmin;
import co.tecno.sersoluciones.analityco.models.Validity;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.views.ShipInfoView;


public class CompanyDetailsFragment extends Fragment implements BranchOfficesInfoRecyclerView.OnBranchOfficeListener, RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_DATA = "data";
    private static final String ARG_BITMAP = "bitmap";
    private static final String ARG_COMPANY = "company";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private static final int UPDATE = 1;

    private Unbinder unbinder;
    @BindView(R.id.header_img)
    ImageView headerImg;
    @BindView(R.id.edit_economic_activity)
    ShipInfoView economicShipInfoView;
    @BindView(R.id.info_domains)
    ShipInfoView domainShipInfoView;
    @BindView(R.id.text_web)
    TextView textWeb;
    @BindView(R.id.text_area_geofence)
    TextView textAreaGeofence;
    @BindView(R.id.icon_address)
    MaterialIconView iconAddress;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.icon_mail)
    MaterialIconView iconMail;
    @BindView(R.id.text_mail)
    TextView textMail;
    @BindView(R.id.icon_phone)
    MaterialIconView iconPhone;
    @BindView(R.id.text_phone)
    TextView textPhone;
    @BindView(R.id.recycler_branch_offices)
    RecyclerView recyclerBranchOffices;
    @BindView(R.id.text_start_date)
    TextView textStartDate;
    @BindView(R.id.text_end_date)
    TextView textEndDate;
    @BindView(R.id.general)
    RelativeLayout general;
    @BindView(R.id.card_sub_main)
    CardView card_sub_main;
    @BindView(R.id.users)
    CardView card_users;
    @BindView(R.id.proyects)
    CardView card_proyects;
    @BindView(R.id.card_validity)
    CardView card_validity;
    @BindView(R.id.addProject)
    FloatingActionButton addProyect;
    @BindView(R.id.addUser)
    FloatingActionButton addUser;
    @BindView(R.id.icon_edit_validity)
    MaterialIconView validityInfo;
    @BindView(R.id.icon_edit_main_form)
    MaterialIconView editInfo;
    @BindView(R.id.document)
    MaterialIconView document;
    @BindView(R.id.state_icon)
    ImageView stateIcon;
    @BindView(R.id.text_more_info)
    TextView textMoreInfo;
    @BindView(R.id.NewProjectLayout)
    LinearLayout addProjectLayout;
    @BindView(R.id.buttonAddUser)
    LinearLayout addUserLayout;

    private RequestBroadcastReceiver requestBroadcastReceiver;

    private String webSite;
    //private ProgressDialog progressDialog;
    private String companyId;
    private String companyIdEdit;
    private JSONObject jsonInstance;
    private View view;
    private ArrayList<UserAdmin> userAdmins;
    private UsersRecyclerAdapter adapterUser;
    private Validity mValidity;
    private String startDateIntent;
    private String finishDateIntent;
    private MyPreferences preferences;
    private String fillForm;

    public static CompanyDetailsFragment newInstance(int columnCount, String data, byte[] byteArray, String companyId) {
        Log.e("User2", data);
        CompanyDetailsFragment fragment = new CompanyDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putString(ARG_DATA, data);
        args.putByteArray(ARG_BITMAP, byteArray);
        args.putString(ARG_COMPANY, companyId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new MyPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.content_details_company, container, false);
        unbinder = ButterKnife.bind(this, view);
        ArrayAdapter<String> adapterShipDefault = new ArrayAdapter<>(getActivity(), R.layout.simple_spinner_item, new ArrayList<String>());
        economicShipInfoView.setAdapter(adapterShipDefault);
        economicShipInfoView.allowDuplicates(false);
        economicShipInfoView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.None);
        char[] splitChar = {',', ';', ' '};
        economicShipInfoView.setSplitChar(splitChar);
        economicShipInfoView.allowCollapse(false);
        userAdmins = new ArrayList<>();
        domainShipInfoView = view.findViewById(R.id.info_domains);
        domainShipInfoView.setAdapter(adapterShipDefault);
        domainShipInfoView.allowDuplicates(false);
        domainShipInfoView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.None);
        domainShipInfoView.setSplitChar(splitChar);
        domainShipInfoView.allowCollapse(false);

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        Bitmap bitmap;
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            companyId = getArguments().getString(ARG_COMPANY);
            fillForm = getArguments().getString(ARG_DATA);
            byte[] byteArray = getArguments().getByteArray(ARG_BITMAP);
            if (byteArray != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
            } else {
                bitmap = null;
            }

            if (bitmap != null)
                headerImg.setImageBitmap(bitmap);
            else
                headerImg.setImageResource(R.drawable.image_not_available);
        }
        if (mColumnCount == 1) {
            card_users.setVisibility(View.GONE);
            card_proyects.setVisibility(View.GONE);
            card_validity.setVisibility(View.GONE);
            if (!fillForm.isEmpty() && !fillForm.equals("UNAUTHORIZED")) {
                try {
                    fillForm(new JSONObject(fillForm));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (mColumnCount == 2) {
            general.setVisibility(View.GONE);
            card_proyects.setVisibility(View.GONE);
            card_users.setVisibility(View.GONE);
            card_sub_main.setVisibility(View.GONE);
            if (!fillForm.isEmpty() && !fillForm.equals("UNAUTHORIZED")) {
                try {
                    fillForm(new JSONObject(fillForm));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (mColumnCount == 3) {
            general.setVisibility(View.GONE);
            card_users.setVisibility(View.GONE);
            card_validity.setVisibility(View.GONE);
            card_sub_main.setVisibility(View.GONE);
            if (!fillForm.isEmpty() && !fillForm.equals("UNAUTHORIZED"))
                fillListProjects(fillForm);
        } else if (mColumnCount == 4) {
            general.setVisibility(View.GONE);
            card_proyects.setVisibility(View.GONE);
            card_validity.setVisibility(View.GONE);
            card_sub_main.setVisibility(View.GONE);
            Log.e("data3", fillForm);
            if (!fillForm.isEmpty() && !fillForm.equals("UNAUTHORIZED"))
                fillListUsers(fillForm);
        }
        addProyect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), ProjectsListActivity.class);
                startActivity(project);
            }
        });
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), NewUserAdminActivity.class);
                project.putExtra("process", 3);
                project.putExtra("company", companyId);
                startActivityForResult(project, UPDATE);
            }
        });
        validityInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), NewUserAdminActivity.class);
                project.putExtra("process", 2);
                project.putExtra("company", companyIdEdit);
                project.putExtra("isActive", mValidity.IsActive);
                project.putExtra("startDate", startDateIntent);
                project.putExtra("finishDate", finishDateIntent);
                startActivityForResult(project, UPDATE);
            }
        });
        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (jsonInstance != null) {
                    Intent project = new Intent(getActivity(), NewUserAdminActivity.class);
                    project.putExtra("process", 1);
                    project.putExtra("company", companyId);
                    project.putExtra("json", jsonInstance.toString());
                    startActivityForResult(project, UPDATE);
                }
            }
        });
        document.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        permission();
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
            mListener.onListFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction();
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {
        Log.e("option", option + "");
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                // userAdmins.remove();
                mListener.onListFragmentInteraction();
                break;
        }
    }


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void fillForm(JSONObject jsonObject) throws JSONException {

        jsonInstance = jsonObject;
        Log.d("json", jsonObject.toString(10));
        companyIdEdit = jsonObject.getString("Id");


        String info = jsonObject.getString("ServicesProvided");
        textMoreInfo.setText(info);
        if (!jsonObject.isNull("Logo")) {

            String url = Constantes.URL_IMAGES + jsonObject.getString("Logo");
            Picasso.get().load(url)
                    .resize(0, 150)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(headerImg);
        }

        if (jsonObject.has("EconomicActivities") && !jsonObject.getString("EconomicActivities").equals("null")) {
            JSONArray economicActivitiesArray = jsonObject.getJSONArray("EconomicActivities");
            for (int i = 0; i < economicActivitiesArray.length(); i++) {
                economicShipInfoView.addObject(economicActivitiesArray.getString(i));
            }
        }
        if (jsonObject.has("Domains") && !jsonObject.getString("Domains").equals("null")) {
            JSONArray domainsArray = jsonObject.getJSONArray("Domains");
            for (int i = 0; i < domainsArray.length(); i++) {
                domainShipInfoView.addObject(domainsArray.getString(i));
            }
        }

        webSite = jsonObject.isNull("Website") ? "" : jsonObject.getString("Website");
        textWeb.setPaintFlags(textWeb.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textWeb.setText(webSite);
        textWeb.setTextColor(Color.BLUE);

        String email = jsonObject.getString("Email");
        textMail.setPaintFlags(textMail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textMail.setText(email);
        textMail.setTextColor(Color.BLUE);

        String phone = jsonObject.getString("Phone");
        textPhone.setPaintFlags(textPhone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textPhone.setText(phone);
        textPhone.setTextColor(Color.BLUE);

        textAreaGeofence.setText(String.format("%,d", Long.parseLong(jsonObject.getString("GeofenceLimit"))) + " km");
        textAddress.setText(jsonObject.getString("Address"));

        ArrayList<BranchOffice> branchOffices = new Gson().fromJson(jsonObject.getJSONArray("BranchOffices").toString(),
                new TypeToken<ArrayList<BranchOffice>>() {
                }.getType());

        for (Iterator<BranchOffice> it = branchOffices.iterator(); it.hasNext(); ) {
            BranchOffice branchOffice = it.next();
            if (branchOffice.IsMain)
                it.remove();
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        //mLayoutManager.setAutoMeasureEnabled(true);
        recyclerBranchOffices.setLayoutManager(mLayoutManager);
        BranchOfficesInfoRecyclerView adapter = new BranchOfficesInfoRecyclerView(getActivity(), this, branchOffices);
        recyclerBranchOffices.setItemAnimator(new DefaultItemAnimator());
        recyclerBranchOffices.setAdapter(adapter);
        if (branchOffices.size() <= 0) {
            view.findViewById(R.id.branch_office_titles).setVisibility(View.GONE);
            view.findViewById(R.id.branch_office_line).setVisibility(View.GONE);
        }
        boolean isActive = jsonObject.getBoolean("IsActive");
        boolean isExpiry = jsonObject.getBoolean("Expiry");
        Drawable drawableIsActive = MaterialDrawableBuilder.with(getActivity())
                .setIcon(isActive ? MaterialDrawableBuilder.IconValue.THUMB_UP : MaterialDrawableBuilder.IconValue.THUMB_DOWN)
                .setColor(isActive ? isExpiry ? Color.rgb(255, 128, 0) : Color.GREEN : Color.RED)
                .setSizeDp(35)
                .build();
        stateIcon.setVisibility(View.VISIBLE);
        if (isActive) {
            if (isExpiry)
                stateIcon.setImageResource(R.drawable.state_icon);
            else
                stateIcon.setVisibility(View.GONE);
        } else {
            stateIcon.setImageResource(R.drawable.state_icon_red);
        }
        // iconIsActive.setImageDrawable(drawableIsActive);


        String startDateStr = jsonObject.getString("StartDate");
        String finishDateStr = jsonObject.getString("FinishDate");
        startDateIntent = startDateStr;
        finishDateIntent = finishDateStr;
        Date startDate = null;
        Date finishDate = null;
        try {
            if (!startDateStr.isEmpty()) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                startDate = format.parse(startDateStr);

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
                startDateStr = dateFormat.format(startDate);

                finishDate = format.parse(finishDateStr);
                finishDateStr = dateFormat.format(finishDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        textStartDate.setText(startDateStr);
        textEndDate.setText(finishDateStr);

        mValidity = new Validity();
        mValidity.IsActive = isActive;
        mValidity.StartDate = startDate;
        mValidity.FinishDate = finishDate;
        //addValidityFragment(mValidity);

        //addMainFragment(jsonObject.toString());
    }

    private void fillListProjects(String projects) {

        ArrayList<ProjectList> projectLists = new Gson().fromJson(projects,
                new TypeToken<ArrayList<ProjectList>>() {
                }.getType());
        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_projects);
        if (projectLists.isEmpty()) {
            recyclerViewProjects.setVisibility(View.GONE);
            return;
        } else {
            Collections.sort(projectLists, new Comparator<ProjectList>() {
                @Override
                public int compare(ProjectList projectList, ProjectList t1) {
                    return t1.FinishDate.compareTo(projectList.FinishDate);
                }
            });
        }


        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        ProjectRecyclerAdapter adapterProjects = new ProjectRecyclerAdapter(getActivity(), projectLists);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterProjects);
    }

    private void fillListUsers(String users) {
        userAdmins = new Gson().fromJson(users,
                new TypeToken<ArrayList<UserAdmin>>() {
                }.getType());
        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_users);
        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        adapterUser = new UsersRecyclerAdapter(getActivity(), userAdmins);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterUser);
    }


    @OnClick(R.id.text_web)
    public void clickWeb() {
        PopupMenu popup = new PopupMenu(getActivity(), textWeb);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Navegar");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(webSite));
                startActivity(i);
                return true;
            }
        });
        popup.show();
    }

    @OnClick(R.id.text_mail)
    public void clickEmail() {
        PopupMenu popup = new PopupMenu(getActivity(), textMail);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Enviar Correo");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + textMail.getText().toString()));
                intent.putExtra(Intent.EXTRA_EMAIL, textMail.getText().toString());

                startActivity(Intent.createChooser(intent, "Send Email"));
                return true;
            }
        });
        popup.show();
    }


    @OnClick(R.id.text_phone)
    public void clickPhone() {
        PopupMenu popup = new PopupMenu(getActivity(), textPhone);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Marcar");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + textPhone.getText().toString()));
                startActivity(i);
                return true;
            }
        });
        popup.show();
    }

    @OnClick(R.id.icon_address)
    public void openGoogleMaps() {
        String map = "http://maps.google.co.in/maps?q=" + textAddress.getText().toString();
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse(map));
        startActivity(i);
    }

    @Override
    public void onClickBrach(BranchOffice mItem) {
        Spanned msg;
        String body = "<b>Dirección:</b> " + mItem.Address +
                "<br><b>Ciudad:</b> " + mItem.City + ", " + mItem.State +
                "<br><b>Correo Electrónico:</b> " + mItem.Email +
                "<br><b>Teléfono:</b> " + mItem.Phone;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            msg = Html.fromHtml(body, Html.FROM_HTML_MODE_LEGACY);
        } else {
            msg = Html.fromHtml(body);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(mItem.Name)
                .setMessage(msg);
        builder.create().show();
    }

    private class ProjectRecyclerAdapter extends CustomListProyectRecyclerView<ProjectList> {

        ProjectRecyclerAdapter(Context context, ArrayList<ProjectList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(final CustomListProyectRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ProjectList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name);
            holder.textName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            holder.textSubName.setText(mItem.Address + " " + mItem.CityName);

            String mDate = mItem.FinishDate;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
                    mDate = dateFormat.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textDate.setText(mDate);

            if (mItem.Logo != null && !mItem.Logo.equals("null")) {
                String url = Constantes.URL_IMAGES + mItem.Logo;
                //holder.mNetworkImageView.setImageUrl(url, holder.mImageLoader);
                Picasso.get().load(url)
                        .resize(80, 80)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(holder.logo);
            } else {
                holder.logo.setImageResource(R.drawable.image_not_available);
            }

            if (mItem.IsActive) {
                if (mItem.Expiry) {
                    holder.textDate.setTextColor(Color.parseColor("#c6ff8000"));
                    holder.iconDate.setColor(Color.parseColor("#c6ff8000"));
                } else
                    holder.stateIcon.setVisibility(View.GONE);
            } else {
                holder.textDate.setTextColor(Color.parseColor("#ffff4444"));
                holder.iconDate.setColor(Color.parseColor("#ffff4444"));
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onProjectInteraction(mItem, holder.logo);
                }
            });
        }
    }

    private class UsersRecyclerAdapter extends CustomListInfoRecyclerViewUsers<UserAdmin> {

        UsersRecyclerAdapter(Context context, ArrayList<UserAdmin> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(final CustomListInfoRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final UserAdmin mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.LastName);
            holder.textSubName.setText(mItem.UserName);
            holder.profile.setText("Administrador");
            String mDate = mItem.FinishDate;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy");
                    mDate = dateFormat.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textValidity.setText(mDate);

            holder.logo.setImageResource(R.drawable.image_not_available);
            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("user", mItem.UserId);
                    Intent project = new Intent(getActivity(), NewUserAdminActivity.class);
                    project.putExtra("process", 4);
                    project.putExtra("company", companyId);
                    project.putExtra("startDate", mItem.StartDate);
                    project.putExtra("finishDate", mItem.FinishDate);
                    project.putExtra("photo", mItem.Photo);
                    project.putExtra("name", mItem.Name + " " + mItem.LastName);
                    project.putExtra("email", mItem.UserName);
                    project.putExtra("idUser", mItem.UserId);
                    startActivityForResult(project, UPDATE);
                }
            });

            if (mItem.Photo != null && !mItem.Photo.equals("null")) {
                String url = Constantes.URL_IMAGES + mItem.Photo;
                //holder.mNetworkImageView.setImageUrl(url, holder.mImageLoader);
                Picasso.get().load(url)
                        .resize(0, 150)
                        .placeholder(R.drawable.profile_dummy)
                        .error(R.drawable.profile_dummy)
                        .into(holder.logo);
            } else {
                holder.logo.setImageResource(R.drawable.profile_dummy);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle("Alerta")
                            .setCancelable(false)
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ////api/company/id/adminusers/id
                                    CrudIntentService.startRequestCRUD(getActivity(),
                                            Constantes.LIST_COMPANIES_URL + companyId + "/adminusers/" + mItem.UserId, Request.Method.DELETE, "", "", false);

                                }
                            })
                            .setNegativeButton("No", null)
                            .setMessage("Desea desvincular este usuario administrador");
                    builder.create().show();
                    return false;
                }
            });

            holder.phone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mItem.PhoneNumber.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mItem.PhoneNumber));
                        startActivity(intent);
                    } else {
                        // 1. Instantiate an AlertDialog.Builder with its constructor
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.alert_null_phone)
                                .setTitle(R.string.alert_null_phone_title)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                }
            });

        }
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


    private void onProjectInteraction(ProjectList item, ImageView imageView) {
        ViewCompat.setTransitionName(imageView, item.Id);
        Intent i = new Intent(getActivity(), DetailsProjectActivityTabs.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.ITEM_OBJ, item);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptions transitionActivityOptions = ActivityOptions
                .makeSceneTransitionAnimation(getActivity(), imageView, ViewCompat.getTransitionName(imageView));
        startActivity(i, transitionActivityOptions.toBundle());
    }


    private void permission() {
        preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        //permisos usuario administrativo

        if (!user.IsSuperUser) {
            if (!user.claims.contains("administratorsserviceconditions.view")) {
//                card_validity.setVisibility(View.GONE);
                (view.findViewById(R.id.validity_info)).setVisibility(View.GONE);
                (view.findViewById(R.id.document)).setVisibility(View.GONE);
                (view.findViewById(R.id.alertPermissions1)).setVisibility(View.VISIBLE);
            }
            if (!user.claims.contains("branchoffices.view")) {
                (view.findViewById(R.id.branch_view)).setVisibility(View.GONE);
            }
            if (!user.claims.contains("administratorsproyects.add")) {
                addProjectLayout.setVisibility(View.GONE);
            }
            if (!user.claims.contains("administrators.update")) {
                editInfo.setVisibility(View.GONE);
            }
            if (!user.claims.contains("companiesadmins.add")) {
                addUserLayout.setVisibility(View.GONE);
            }
            if (!user.claims.contains("companiesadmins.view")) {
                (view.findViewById(R.id.recycler_users)).setVisibility(View.GONE);
                (view.findViewById(R.id.alertPermissions3)).setVisibility(View.VISIBLE);
            }
        }
    }
}
