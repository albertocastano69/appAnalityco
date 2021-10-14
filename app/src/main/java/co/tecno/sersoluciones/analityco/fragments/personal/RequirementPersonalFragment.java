package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.models.Contract;
import co.tecno.sersoluciones.analityco.models.ContractList;
import co.tecno.sersoluciones.analityco.models.ContractReq;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.Requirements;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class RequirementPersonalFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_LIST_ITEMS = "array-items";
    private static final String ARG_CONTRACTS = "contracts";
    private static final String ARG_BIRTHDAY = "birthday";
    private static final String ARG_CLAIMS = "claims";
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private ArrayList<Requirements> items;
    private Spinner spinnerContract;
    private ArrayList<Requirements> requirements;
    private RecyclerView recyclerView;

    private CardView daysWeekReq;
    private TextView daysWeek;
    private TextView hoursWork;
    private CardView ageContract;
    private TextView ageRange;
    private ImageView checkAgeRangeImage;
    private CardView hoursContract;
    private String birthDate;
    private View view;
    private ArrayList<ContractReq> contractObjectList;

    public static RequirementPersonalFragment newInstance(int columnCount) {
        RequirementPersonalFragment fragment = new RequirementPersonalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public static RequirementPersonalFragment newInstance(ArrayList<Requirements> items, ArrayList<ContractReq> contracts, String birthDate, String claims) {
        RequirementPersonalFragment fragment = new RequirementPersonalFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTRACTS, contracts);
        args.putSerializable(ARG_LIST_ITEMS, items);
        args.putString(ARG_BIRTHDAY, birthDate);
        args.putString(ARG_CLAIMS, claims);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_COLUMN_COUNT))
                mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            if (getArguments().containsKey(ARG_LIST_ITEMS)) {
                if (getArguments().getSerializable(ARG_LIST_ITEMS) != null)
                    items = (ArrayList<Requirements>) getArguments().getSerializable(ARG_LIST_ITEMS);
                if (!getArguments().getString(ARG_BIRTHDAY).equals("UNAUTHORIZED"))
                    birthDate = getArguments().getString(ARG_BIRTHDAY);

                contractObjectList = (ArrayList<ContractReq>) getArguments().getSerializable(ARG_CONTRACTS);

            }
            Gson gson = new Gson();
            logW("reqs fragment" + gson.toJson(items));
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.requirement_personal_list, container, false);
        recyclerView = view.findViewById(R.id.list);
        spinnerContract = view.findViewById(R.id.spinner_contracts);
        requirements = new ArrayList<>();
        // Set the adapter
        requirements = new ArrayList<>();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), mColumnCount));
        }

        recyclerView.setAdapter(new RequirementPersonalRecyclerViewAdapter(getContext(), items, mListener));
        hoursContract = view.findViewById(R.id.hoursContract);
        hoursWork = view.findViewById(R.id.hoursWork);
        daysWeekReq = view.findViewById(R.id.daysWeekContract);
        daysWeek = view.findViewById(R.id.daysWeek);
        ageContract = view.findViewById(R.id.ageContract);
        ageRange = view.findViewById(R.id.ageRange);
        checkAgeRangeImage = view.findViewById(R.id.checkAgeRangeImage);
        permission();

        ArrayAdapter<ContractReq> adapter = new ArrayAdapter<ContractReq>(getContext(), R.layout.simple_spinner_item, contractObjectList);
        spinnerContract.setAdapter(adapter);
        spinnerContract.setSelection(0);
        spinnerContract.setOnItemSelectedListener(this);

        return view;
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
        void onListFragmentInteraction(Requirements item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_contracts:
                selectReqContract(position);
                break;
        }
    }

    private void selectReqContract(int position) {
        requirementsObjectOffline(position);
        addRequirementFragment();
    }

    private void addRequirementFragment() {
        recyclerView.setAdapter(new RequirementPersonalRecyclerViewAdapter(getContext(), requirements, mListener));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @SuppressLint("SetTextI18n")
    private void requirementsObjectOffline(int position) {

        logW("position spinner: " + position + " contractId: " + contractObjectList.get(position) + " id " + contractObjectList.get(position).Id);
        requirements.clear();
        if (items.size() > 0) {
            for (Requirements req : items) {
                if (req.ContractId.equals(contractObjectList.get(position).Id)) {
                    logW("Requisitos Desc segun contrato Id: " + req.ContractId + ", desc " + req.Desc);
                    requirements.add(req);
                }
            }
            for (ContractReq contract : contractObjectList) {
                if (contract.Id.equals(contractObjectList.get(position).Id) && !requirements.isEmpty()) {
                    requirements.get(0).MinHour = contract.MinHour;
                    requirements.get(0).MaxHour = contract.MaxHour;
                    requirements.get(0).WeekDays = contract.WeekDays;
                    requirements.get(0).MinHour = contract.MinHour;
                    requirements.get(0).MaxHour = contract.MaxHour;
                } else if (contract.Id.equals(contractObjectList.get(position).Id)) {
                    Requirements req = new Requirements();
                    req.MinHour = contract.MinHour;
                    req.MaxHour = contract.MaxHour;
                    req.WeekDays = contract.WeekDays;
                    req.MinHour = contract.MinHour;
                    req.MaxHour = contract.MaxHour;
                    requirements.add(req);
                }
            }
        } else
            for (ContractReq contract : contractObjectList) {
                Requirements req = new Requirements();
                req.MinHour = contract.MinHour;
                req.MaxHour = contract.MaxHour;
                req.WeekDays = contract.WeekDays;
                req.MinHour = contract.MinHour;
                req.MaxHour = contract.MaxHour;
                requirements.add(req);
            }

        if (requirements.get(0).MinHour != null && requirements.get(0).MaxHour != null) {
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
                hoursContract.setVisibility(View.VISIBLE);
                hoursWork.setText("Horario: " + localDateFormat.format(Objects.requireNonNull(localDateFormat.parse(requirements.get(0).MinHour))) + " - "
                        + localDateFormat.format(Objects.requireNonNull(localDateFormat.parse(requirements.get(0).MaxHour))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (requirements.get(0).WeekDays != null && !requirements.get(0).WeekDays.isEmpty()) {
            daysWeekReq.setVisibility(View.VISIBLE);
            daysWeek.setText(changeDaysWeek(requirements.get(0).WeekDays));
        }
        //Valores por defecto si la edad llena null
        if (requirements.get(0).AgeMin == null) requirements.get(0).AgeMin = "18";
        if (requirements.get(0).AgeMax == null) requirements.get(0).AgeMax = "88";

        if (requirements.get(0).AgeMin != null && requirements.get(0).AgeMax != null) {
            ageContract.setVisibility(View.VISIBLE);
            ageRange.setText(requirements.get(0).AgeMin + " ≤ " + "Edad" + " < " + requirements.get(0).AgeMax);
            if (birthDate != null && !birthDate.isEmpty()) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");
                try {
                    Date date = format.parse(birthDate);
                    checkAgeRange(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private String changeDaysWeek(String daysWeek) {

        daysWeek = daysWeek.replace("{", "");
        daysWeek = daysWeek.replace("}", "");
        daysWeek = daysWeek.replace("1", "Lun");
        daysWeek = daysWeek.replace("2", "Mar");
        daysWeek = daysWeek.replace("3", "Mie");
        daysWeek = daysWeek.replace("4", "jue");
        daysWeek = daysWeek.replace("5", "Vie");
        daysWeek = daysWeek.replace("6", "Sab");
        daysWeek = daysWeek.replace("0", "Dom");
        return daysWeek;
    }

    private void checkAgeRange(Date date) {
        Calendar a = Calendar.getInstance();
        Calendar b = Calendar.getInstance();
        a.setTime(date);
        b.setTime(Calendar.getInstance().getTime());
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }

        if (diff > Integer.parseInt(requirements.get(0).AgeMin) || diff < Integer.parseInt(requirements.get(0).AgeMax)) {
            checkAgeRangeImage.setImageResource(R.drawable.ic_checkmark);
        } else {
            checkAgeRangeImage.setImageResource(R.drawable.ic_close_arl);
        }
    }

    private void permission() {
        MyPreferences preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);

        //permisos visualizarcion
        if (user.claims.contains("personalsrequirements.view") || user.IsSuperUser)
            (view.findViewById(R.id.contentRequeriments)).setVisibility(View.VISIBLE);
        else (view.findViewById(R.id.alertPermissions)).setVisibility(View.VISIBLE);
    }

}
