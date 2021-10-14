package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.ContractsListActivity;
import co.tecno.sersoluciones.analityco.DetailsContractsActivityTabs;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.contracts.ContractRequerimensAdapter;
import co.tecno.sersoluciones.analityco.models.RequirementDetails;
import co.tecno.sersoluciones.analityco.models.Requirements;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;


public class RequirementListProjectFragment extends Fragment {

    private static final String ARG_IS_RENDER = "isRender";
    private static final String ARG_PROJECTID = "projectId";
    private static final int UPDATE = 1;

    @BindView(R.id.addCompanyProject)
    FloatingActionButton newuserProject;
    @BindView(R.id.recycler_companies)
    RecyclerView recyclerViewRequirement;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.daysWeekContract)
    CardView daysWeekContract;
    @BindView(R.id.daysWeek)
    TextView daysWeek;
    @BindView(R.id.hoursContract)
    CardView hoursContract;
    @BindView(R.id.hoursWork)
    TextView hoursWork;
    @BindView(R.id.ageContract)
    CardView ageContract;
    @BindView(R.id.ageRange)
    TextView ageRange;

    private View view;
    private User user;

    public RequirementListProjectFragment() {
    }

    public static RequirementListProjectFragment newInstance(String contractId, boolean isRender) {
        // Log.e("User2",data);
        RequirementListProjectFragment fragment = new RequirementListProjectFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_RENDER, isRender);
        args.putString(ARG_PROJECTID, contractId);
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
        title.setText("Requisitos de acceso");

        if (getArguments() != null) {

            boolean isRender = getArguments().getBoolean(ARG_IS_RENDER);
            MyPreferences preferences = new MyPreferences(getActivity());
            String profile = preferences.getProfile();
            user = new Gson().fromJson(profile, User.class);
            if (isRender) fillListRequirement();
        }
        newuserProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), ContractsListActivity.class);
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

    @SuppressLint("SetTextI18n")
    private void fillListRequirement() {
        ArrayList<RequirementDetails> requirementDetails = ((DetailsContractsActivityTabs) Objects.requireNonNull(getActivity())).dataFragment.getRequirementDetails();
        if (requirementDetails.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put("companyId", user.CompanyId);
            String paramsQuery = HttpRequest.makeParamsInUrl(values);
            return;
        }
        Collections.sort(requirementDetails.get(0).RequirementContracts, new Comparator<Requirements>() {
            @Override
            public int compare(Requirements t2, Requirements t1) {
                return t2.OrderNum - t1.OrderNum;
            }
        });
        if (requirementDetails.get(0).MinHour != null && requirementDetails.get(0).MaxHour != null) {
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
                hoursContract.setVisibility(View.VISIBLE);
                hoursWork.setText("Horario: " + localDateFormat.format(localDateFormat.parse(requirementDetails.get(0).MinHour)) + " - "
                        + localDateFormat.format(localDateFormat.parse(requirementDetails.get(0).MaxHour)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        ageContract.setVisibility(View.VISIBLE);
        ageRange.setText(requirementDetails.get(0).AgeMin + " ≤ " + "Edad" + " < " + requirementDetails.get(0).AgeMax);

        if (requirementDetails.get(0).WeekDays != null) {
            daysWeekContract.setVisibility(View.VISIBLE);
            daysWeek.setText(getDaysWeek(requirementDetails.get(0).WeekDays));
        }

        //mLayoutManager.setAutoMeasureEnabled(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewRequirement.setLayoutManager(mLayoutManager);
//        RequirementRecyclerAdapter adapter = new RequirementRecyclerAdapter(getActivity(), userAdmins.get(0).RequirementContracts);
        ContractRequerimensAdapter adapterRequeriments = new ContractRequerimensAdapter(getActivity(), requirementDetails.get(0).RequirementContracts);
        recyclerViewRequirement.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewRequirement.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewRequirement.addItemDecoration(mDividerItemDecoration);
        recyclerViewRequirement.setAdapter(adapterRequeriments);
    }


    private String getDaysWeek(int[] items) {
        String daysWeek = "";
        int size = items.length;
        for (int i = 0; i < size; i++) {
            daysWeek = daysWeek + items[i] + " ";
        }
        daysWeek = dltRepWords(daysWeek);
        daysWeek = daysWeek.replace("10", "Dom");
        daysWeek = daysWeek.replace("1", "Lun");
        daysWeek = daysWeek.replace("2", "Mar");
        daysWeek = daysWeek.replace("3", "Mie");
        daysWeek = daysWeek.replace("4", "jue");
        daysWeek = daysWeek.replace("5", "Vie");
        daysWeek = daysWeek.replace("6", "Sab");
        return daysWeek;
    }


    private String dltRepWords(String s) {
        String newString = new LinkedHashSet<>(Arrays.asList(s.split(" "))).toString().replaceAll("(^\\[|]$)", "").replace(", ", ",");
        return sortString(newString);
    }

    private String sortString(String s) {

        String[] stringArr = s.split(",");
        Arrays.sort(stringArr);
        ArrayList<String> listArr = new ArrayList<>(Arrays.asList(stringArr));
        if (listArr.contains("0")) {
            listArr.remove("0");
            listArr.add("10");
            String[] changeArray = new String[listArr.size()];
            changeArray = listArr.toArray(changeArray);
            stringArr = changeArray;
        }
        StringBuilder buffer = new StringBuilder();
        for (String each : stringArr)
            buffer.append(",").append(each);
        return buffer.deleteCharAt(0).toString();
    }

    private void permission() {
        //permisos visualizarcion
        if (user.claims.contains("contractsrequirements.view"))
            (view.findViewById(R.id.recycler_companies)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);

    }


}
