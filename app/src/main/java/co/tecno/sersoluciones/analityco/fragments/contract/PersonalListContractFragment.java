package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.DetailsContractsActivityTabs;
import co.tecno.sersoluciones.analityco.DetailsPersonalActivityTabs;
import co.tecno.sersoluciones.analityco.JoinPersonalContractActivity;
import co.tecno.sersoluciones.analityco.JoinPersonalWizardActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListPersonalRecyclerViewUsers;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;


public class PersonalListContractFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_COMPANYINFO_ID = "companyInfoId";
    private static final String ARG_PROJECTID = "projectId";
    private static final String ARG_START = "startDate";
    private static final String ARG_FINISH = "finishDate";
    private static final String ARG_CONTRACTTYPE = "contractType";
    private static final String ARG_IS_RENDER = "isRender";
    private static final int UPDATE = 1;
    private ClaimsBasicUser Claims;
    private String contractId;
    private int positionDelete = 0;
    private String deleteUrl = "";


    @BindView(R.id.addCompanyProject)
    FloatingActionButton newPersonalContract;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.title2)
    TextView title2;

    private View view;
    private User user;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private ArrayList<PersonalList> personalList;
    private PersonalRecyclerAdapter adapterProjects;
    private String contractType;
    private String companyInfoId;
    ProgressBar progressBar;

    public PersonalListContractFragment() {
    }

    public static PersonalListContractFragment newInstance(String companyInfoId, String contractId, String startDate, String finishDate, String contractType, boolean isRender) {

        PersonalListContractFragment fragment = new PersonalListContractFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMPANYINFO_ID, companyInfoId);
        args.putString(ARG_PROJECTID, contractId);
        args.putString(ARG_START, startDate);
        args.putString(ARG_FINISH, finishDate);
        args.putString(ARG_CONTRACTTYPE, contractType);
        args.putBoolean(ARG_IS_RENDER, isRender);
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
            Claims = ((DetailsContractsActivityTabs) requireActivity()).dataFragment.getClaims();
            companyInfoId = getArguments().getString(ARG_COMPANYINFO_ID);
            contractId = getArguments().getString(ARG_PROJECTID);
            boolean isRender = getArguments().getBoolean(ARG_IS_RENDER);
//            startDate = getArguments().getString(ARG_START);
//            finishDate = getArguments().getString(ARG_FINISH);
            contractType = getArguments().getString(ARG_CONTRACTTYPE);
            log("contractType " + contractType);
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            title.setText("Personal Vinculado");
            title2.setText("Vincular personal");
            if (isRender) fillListPersonal();
            permission();
        }

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
//        (view.findViewById(R.id.card_sub_main)).setVisibility(View.VISIBLE);
//        (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        newPersonalContract.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), JoinPersonalWizardActivity.class);
            intent.putExtra("contractId", contractId);
            intent.putExtra("companyInfoId", companyInfoId);
            intent.putExtra("contractType", contractType);
            intent.putExtra("typePermission", "contract");
            startActivityForResult(intent, UPDATE);
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
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(requestBroadcastReceiver);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == RESULT_OK) {
//            reloadData();
        }
    }

    private void fillListPersonal() {
        personalList = ((DetailsContractsActivityTabs) requireActivity()).dataFragment.getPersonalList();
        RecyclerView recyclerViewProjects = view.findViewById(R.id.recycler_companies);
        progressBar = view.findViewById(R.id.progress);
        if (personalList != null && personalList.isEmpty()) {
            recyclerViewProjects.setVisibility(View.GONE);
            return;
        } else {
            Collections.sort(personalList, (projectList, t1) -> t1.FinishDate.compareTo(projectList.FinishDate));

            Collections.sort(personalList, (projectList, t1) -> Boolean.compare(projectList.Expiry, t1.Expiry));
        }
        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewProjects.setLayoutManager(mLayoutManager);
        adapterProjects = new PersonalRecyclerAdapter(getActivity(), personalList);
        recyclerViewProjects.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewProjects.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewProjects.addItemDecoration(mDividerItemDecoration);
        recyclerViewProjects.setAdapter(adapterProjects);
        recyclerViewProjects.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
                @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItemPosition= mLayoutManager.findFirstVisibleItemPosition();
                    Log.w("visible Item", String.valueOf(visibleItemCount));
                    Log.w("Total Item", String.valueOf(totalItemCount));
                    Log.w("firstVisible Item", String.valueOf(firstVisibleItemPosition));

                // Load more if we have reach the end to the recyclerView
                    if ( (visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                            Log.w("LLego al final","por fin");
                            progressBar.setVisibility(View.VISIBLE);
                             fectdata();
                    }
            }
        });
    }

    private void fectdata() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapterProjects.limit += 10;
                adapterProjects.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        },5000);
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (url.equals(deleteUrl)) {
                    personalList.remove(positionDelete);
                    adapterProjects.notifyDataSetChanged();
                    positionDelete = 0;
                    ((DetailsContractsActivityTabs) getActivity()).reloadData();
                }
                break;
        }
    }

    private class PersonalRecyclerAdapter extends CustomListPersonalRecyclerViewUsers<PersonalList> {

        PersonalRecyclerAdapter(Context context, ArrayList<PersonalList> mItems) {
            super(context, mItems);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull final CustomListPersonalRecyclerViewUsers.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final PersonalList mItem = mItems.get(pos);
            holder.textName.setText(mItem.Name + " " + mItem.LastName);
            holder.textSubName.setText(mItem.JobName);
            holder.profile.setText(mItem.Position);
            holder.btnEdit.setImageResource(R.drawable.ic_dots_vertical);
            holder.user_icon.setImageResource(R.drawable.ic_job);
            boolean isActive = true;
            Date now = new Date();
            String mDate = mItem.FinishDate;
            if (mItem.PhoneNumber != null) {
                holder.phone.setVisibility(View.VISIBLE);
            }
            holder.phone.setOnClickListener(view -> {
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
                    } else if (mItem.Expiry) {
                        holder.stateIcon.setVisibility(View.VISIBLE);
                        holder.textValidity.setTextColor(getResources().getColor(R.color.expiry));
                        holder.calendar.setColorResource(R.color.expiry);
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

            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.widget.PopupMenu popup = new android.widget.PopupMenu(getActivity(), v);
                    popup.inflate(R.menu.action_menu);
                    popup.getMenu().getItem(0).setEnabled(false);
                    popup.getMenu().getItem(1).setEnabled(false);
                    if (user.IsAdmin || user.IsSuperUser) {
                        if ((user.claims.contains("contractspersonals.delete") || user.IsSuperUser) && !mItem.HaveRegister)
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (user.claims.contains("contractspersonals.update") || user.IsSuperUser)
                            popup.getMenu().getItem(1).setEnabled(true);
                    } else if (Claims != null) {
                        if ((Claims.Claims.contains("contractspersonals.delete")) && !mItem.HaveRegister)
                            popup.getMenu().getItem(0).setEnabled(true);
                        if (Claims.Claims.contains("contractspersonals.update"))
                            popup.getMenu().getItem(1).setEnabled(true);
                    }
                    popup.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                        .setTitle("Alerta")
                                        .setCancelable(false)
                                        .setPositiveButton("Si", (dialogInterface, i) -> {
                                            //delete=true;
                                            positionDelete = pos;
                                            deleteUrl = Constantes.LIST_CONTRACTS_URL + contractId + "/PersonalInfo/";
                                            CrudIntentService.startRequestCRUD(getActivity(),
                                                    deleteUrl + mItem.PersonalCompanyInfoId, Request.Method.DELETE, "", "", false);
                                        })
                                        .setNegativeButton("No", null)
                                        .setMessage("Desea desvincular este usuario");
                                builder.create().show();
                                return false;
                            case R.id.edit:
                                Intent intent = new Intent(getActivity(), JoinPersonalContractActivity.class);
                                intent.putExtra("contractId", contractId);
                                intent.putExtra("companyInfoId", companyInfoId);
                                intent.putExtra("contractType", contractType);
                                intent.putExtra("docNumber", mItem.DocumentNumber);
                                intent.putExtra("typePermission", "contract");
                                startActivityForResult(intent, UPDATE);
                                return true;
                            default:
                                return false;
                        }
                    });
                    popup.show();
                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItem.Id = mItem.PersonalCompanyInfoId;
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

        //permisos visualizarcion
        if (user.claims.contains("contractspersonals.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);
        (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);
        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("contractspersonals.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (Claims != null) {
            if (Claims.Claims.contains("contractspersonals.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }
    }
}
