package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

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
import co.tecno.sersoluciones.analityco.JoinPersonalContractActivity;
import co.tecno.sersoluciones.analityco.PersonalAddContract;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.ContractList;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.Utils;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.utilities.Constantes.UPDATE;


public class ContractListPersonalFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_DATA = "data";
    private static final String ARG_PERSONALTID = "personalId";
    private static final String PERSONAL = "data_personal";
    private static final String ARG_CLAIMS = "claims";

    private int personalId;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newContractPersonal;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.title2)
    TextView title2;

    private View view;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private ArrayList<ObjectList> contractLists;
    private String newContract;
    private ClaimsBasicUser claims;
    private MyPreferences preferences;
    private User user;

    public ContractListPersonalFragment() {
    }

    public static ContractListPersonalFragment newInstance(String newContract, ArrayList<ObjectList> contracts, int persoanlId, String claims) {
        // Log.e("User2",data);
        ContractListPersonalFragment fragment = new ContractListPersonalFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATA, contracts);
        args.putInt(ARG_PERSONALTID, persoanlId);
        args.putString(PERSONAL, newContract);
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
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);

        if (getArguments() != null) {
            claims = new Gson().fromJson(getArguments().getString(ARG_CLAIMS),
                    new TypeToken<ClaimsBasicUser>() {
                    }.getType());

            personalId = getArguments().getInt(ARG_PERSONALTID);
            ArrayList<ObjectList> contracts = (ArrayList<ObjectList>) getArguments().getSerializable(ARG_DATA);
            newContract = getArguments().getString(PERSONAL);

            title.setText("Vinculado a los contratos");
            title2.setText("Vincular a contrato");

            fillListContracts(contracts);
        }
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        newContractPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Contract = new Intent(getActivity(), PersonalAddContract.class);
                Contract.putExtra("infoPersonal", newContract);
                Contract.putExtra("PersonalId", personalId);

                ArrayList<String> arrayList = new ArrayList<>();
                for (ObjectList objectList : contractLists) {
                    arrayList.add(objectList.ContractNumber);
                }
//                arrayList.add(0, "1");
                String[] selectionArgs = arrayList.toArray(new String[arrayList.size()]);
                String selection = //"(" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ? ) and "
                        DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER + " not in (" + Utils.makePlaceholders(arrayList.size()) + ")";
//                for (String select : selectionArgs) {
//                    log("select: " + select);
//                }
                Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_CONTRACT_URI, null, selection, selectionArgs, null);
                JSONArray jsonArrayContracts = Utils.cursorToJArray(cursor);
                logW("lon cursor: " + cursor.getCount());
                ArrayList<ContractList> contractPrincipalList = new Gson().fromJson(jsonArrayContracts.toString(),
                        new TypeToken<ArrayList<ContractList>>() {
                        }.getType());
                cursor.close();
//                for (ObjectList obj: contractPrincipalList) logW("numero de contrato: " + obj.ContractNumber);
                Contract.putExtra("contractList", new Gson().toJson(contractPrincipalList));
                startActivity(Contract);
            }
        });
        permission();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_DELETE);
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(requestBroadcastReceiver);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
        }
    }

    private void fillListContracts(ArrayList<ObjectList> contracts) {

        contractLists = contracts;

        RecyclerView recyclerViewContracts = view.findViewById(R.id.recycler_companies);
        if (contractLists == null) {
            recyclerViewContracts.setVisibility(View.GONE);
            return;
        }
        if (contractLists.isEmpty()) {
            recyclerViewContracts.setVisibility(View.GONE);
            return;
        }

        Collections.sort(contractLists, new Comparator<ObjectList>() {
            @Override
            public int compare(ObjectList projectList, ObjectList t1) {
                return t1.FinishDate.compareTo(projectList.FinishDate);
            }
        });


        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewContracts.setLayoutManager(mLayoutManager);
        ContractRecyclerAdapter adapter = new ContractRecyclerAdapter(getActivity(), contractLists);
        recyclerViewContracts.setItemAnimator(new DefaultItemAnimator());
        recyclerViewContracts.setAdapter(adapter);
    }

    private class ContractRecyclerAdapter extends CustomListInfoRecyclerView<ObjectList> {

        ContractRecyclerAdapter(Context context, ArrayList<ObjectList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(@NonNull final CustomListInfoRecyclerView.ViewHolder holder, int position) {
            final int pos = holder.getAdapterPosition();
            final ObjectList mItem = mItems.get(pos);
            holder.textName.setAllCaps(false);
            holder.textName.setText(mItem.ContractReview);
            holder.textSubName.setText(mItem.EmployerName);
            holder.dateIcon.setVisibility(View.VISIBLE);
            holder.jobIcon.setVisibility(View.VISIBLE);
            holder.topLayout.setVisibility(View.VISIBLE);
//            holder.imageDots.setVisibility(View.GONE);
            holder.textJob.setText(mItem.Position);
            String mDate = mItem.FinishDate;
            String contractDate = mItem.FinishDateContract;
            try {
                if (mDate != null && !mDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(mDate);
                    Date date2 = format.parse(contractDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
                    mDate = dateFormat.format(date2);
                    contractDate = dateFormat.format(date);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.textValidity.setText(mDate);
            holder.textTopDate.setText(contractDate);

            holder.btnEdit.setVisibility(View.GONE);
            holder.iconIsActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);

            if (mItem.FormImageLogo != null && !mItem.FormImageLogo.equals("null")) {
                String url = Constantes.URL_IMAGES + mItem.FormImageLogo;
//
                chargeImage(url, holder);
            } else {
                holder.logo.setImageResource(R.drawable.image_not_available);
            }

            if (mItem.IsActive) {
                if (mItem.Expiry) {
                    holder.stateIcon.setVisibility(View.VISIBLE);
                    holder.textValidity.setTextColor(getResources().getColor(R.color.expiry));
                    holder.topIconDate.setColorResource(R.color.expiry);
                }
            } else {
                holder.stateIcon.setVisibility(View.VISIBLE);
                holder.stateIcon.setImageResource(R.drawable.state_icon_red);
                holder.topIconDate.setColor(getResources().getColor(R.color.bar_undecoded));
                holder.textTopDate.setTextColor(getResources().getColor(R.color.bar_undecoded));
            }

            holder.mView.setOnClickListener(view -> {
                ViewCompat.setTransitionName(holder.logo, mItem.Id);
                onContractInteraction(mItem, holder.logo);
            });

            holder.imageDots.setOnClickListener(view -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(getActivity(), view);
                popup.inflate(R.menu.action_menu);
                popup.getMenu().getItem(0).setVisible(false);
                popup.getMenu().getItem(1).setEnabled(false);

                if (user.IsAdmin || user.IsSuperUser) {
                    if (user.claims.contains("personalscontracts.view") || user.IsSuperUser)
                        popup.getMenu().getItem(1).setEnabled(true);
                } else if (claims != null) {
                    if (claims.Claims.contains("personalscontracts.view"))
                        popup.getMenu().getItem(1).setEnabled(true);
                }

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.edit:
                            Gson gson = new Gson();
                            Intent intent = new Intent(getActivity(), JoinPersonalContractActivity.class);
                            intent.putExtra("contractId", mItem.Id);
                            intent.putExtra("companyInfoId", mItem.CompanyInfoParentId);
                            intent.putExtra("contractType", mItem.ValueContractType);
                            intent.putExtra("docNumber", mItem.DocumentNumber);
                            intent.putExtra("typePermission", "personal");
                            startActivityForResult(intent, UPDATE);
                            return true;
                        default:
                            return false;
                    }
                });
                popup.show();
            });
        }
    }

    @Override
    public void onStringResult(String action, int option, String res, String url) {

    }

    private void onContractInteraction(ObjectList item, ImageView imageView) {

        Intent i = new Intent(getActivity(), DetailsContractsActivityTabs.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(Constantes.ITEM_OBJ, item);
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
        if (user.claims.contains("personalscontracts.view") || user.IsSuperUser)
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("personalscontracts.add") || user.IsSuperUser)
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);

        }//permisos usuarios normales
        else if (claims != null) {
            if (claims.Claims.contains("personalscontracts.add"))
                (view.findViewById(R.id.CompanyLayout)).setVisibility(View.VISIBLE);
        }
    }
}
