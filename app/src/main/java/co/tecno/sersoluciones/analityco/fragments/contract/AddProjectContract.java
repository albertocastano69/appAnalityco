package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.models.Contract;
import co.tecno.sersoluciones.analityco.models.EconomicActivity;
import co.tecno.sersoluciones.analityco.models.ProjectContract;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.ProjectStageContract;
import co.tecno.sersoluciones.analityco.models.ProjectStages;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;
import co.tecno.sersoluciones.analityco.views.EconomicCompletionView;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;

/**
 * Created by Ser Soluciones SAS on 01/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class AddProjectContract extends Fragment implements DatePickerDialog.OnDateSetListener, TextWatcherAdapter.TextWatcherListener,
        ClearebleAutoCompleteTextView.Listener, RequestBroadcastReceiver.BroadcastListener {

    private Unbinder unbinder;
    private RequestBroadcastReceiver getJSONBroadcastReceiver;
    @BindView(R.id.spinner_Project)
    Spinner spinnerProject;
    @BindView(R.id.spinner_Steps)
    Spinner spinnerStep;
    private OnAddProjectContract mListener;
    private Date toDate;
    private Date fromDate;
    private String fromDateStr;
    private String toDateStr;
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;

    @BindView(R.id.btn_from_date)
    Button fromDateBtn;
    @BindView(R.id.btn_to_date)
    Button toDateBtn;
    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    @BindView(R.id.tvToDateError)
    TextView tvToDateError;
    @BindView(R.id.edit_economic_activity)
    EconomicCompletionView economicCompletionView;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    private Context instance;
    private ArrayList<ProjectStageContract> projects;
    private String[] project_spinner;
    private String[] steps_spinner;
   // private Contract contract;
    private boolean editProject = false;
    private ProjectList projectList;
    private ArrayList<ProjectStages> stages;
    private String contractId;

    public AddProjectContract() {

    }

    public static AddProjectContract newInstanceEdit(ProjectList project, String contractId) {
        AddProjectContract fragment = new AddProjectContract();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, project);
        args.putBoolean(ARG_PARAM2, true);
        args.putString(ARG_PARAM3, contractId);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddProjectContract newInstance(String contractId) {
        AddProjectContract fragment = new AddProjectContract();
        Bundle args = new Bundle();
        args.putBoolean(ARG_PARAM2, false);
        args.putString(ARG_PARAM3, contractId);
        fragment.setArguments(args);
        return fragment;
    }

    @OnClick(R.id.btn_from_date)
    public void fromDateDialog() {
        fromDatePickerDialog.getDatePicker().setTag(R.id.btn_from_date);
        fromDatePickerDialog.show();
    }

    @OnClick(R.id.btn_to_date)
    public void toDateDialog() {
        toDatePickerDialog.getDatePicker().setTag(R.id.btn_to_date);
        toDatePickerDialog.show();
    }

    //Set your layout here
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_project_contract, container, false);
        MyPreferences preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        if (getArguments() != null) {
            editProject = getArguments().getBoolean(ARG_PARAM2);
            if (editProject) {
                projectList = getArguments().getParcelable(ARG_PARAM1);
                contractId = getArguments().getString(ARG_PARAM3);
            } else {
                //contract = (Contract) getArguments().getSerializable(ARG_PARAM1);
                //contractId=contract.Id;
                contractId = getArguments().getString(ARG_PARAM3);
                toDateStr="";
                ContentValues valuesex = new ContentValues();
                valuesex.put("company", user.CompanyId);
                valuesex.put("select", true);
                String paramsQueryex = HttpRequest.makeParamsInUrl(valuesex);
                CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_PROJECTS_URL,
                        Request.Method.GET, "", paramsQueryex, true, false);
            }

        }
        unbinder = ButterKnife.bind(this, v);
        instance = getActivity();
        economicCompletionView.allowDuplicates(false);
        Calendar calendar = Calendar.getInstance();
        fromDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        toDate = calendar.getTime();

        if (editProject) {
            fromDateStr = projectList.StartDate;
            toDateStr = projectList.FinishDate;
            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                fromDate = format.parse(projectList.StartDate);
                toDate = format.parse(projectList.FinishDate);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                fromDateBtn.setText(dateFormat.format(fromDate));
                toDateBtn.setText(dateFormat.format(toDate));
                toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            String myFormat = "dd/MMM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
            fromDateStr = sdf.format(fromDate);
            fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            fromDateBtn.setText(fromDateStr);
        }
        //toDateStr = "";

        updateDatePicker(fromDate, toDate);
        getJSONBroadcastReceiver = new RequestBroadcastReceiver(this);

        if (editProject) {
            project_spinner = new String[1];
            project_spinner[0] = projectList.Name;
            spinnerProject.setAdapter(new CustomArrayAdapter(getActivity(), project_spinner));
            CrudIntentService.startRequestCRUD(getActivity(), Constantes.STAGEBYPROJECT_URL + projectList.ProjectId,
                    Request.Method.GET, "", "", true);
        }
        spinnerProject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!editProject)
                     economicCompletionView.clear();

                if (spinnerProject.getSelectedItemPosition() == 0) {
                    steps_spinner = new String[1];
                    steps_spinner[0] = "ETAPAS";
                    spinnerStep.setAdapter(new CustomArrayAdapter(getActivity(), steps_spinner));
                } else {
                    if (!editProject)
                        fillListStages(projects.get((spinnerProject.getSelectedItemPosition()) - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spinnerStep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                EconomicActivity economicActivity;
                economicCompletionView.allowDuplicates(false);
                if (spinnerStep.getSelectedItemPosition() > 0) {
                    Log.e("selectStep", i + "");
                    if (editProject)
                        economicActivity = new EconomicActivity(stages.get(spinnerStep.getSelectedItemPosition() - 1).Name, stages.get(spinnerStep.getSelectedItemPosition() - 1).Id + "");
                    else
                        economicActivity = new EconomicActivity(projects.get(spinnerProject.getSelectedItemPosition() - 1).ProjectStageList.get(spinnerStep.getSelectedItemPosition() - 1).Name, projects.get(spinnerProject.getSelectedItemPosition() - 1).ProjectStageList.get(spinnerStep.getSelectedItemPosition() - 1).Id + "");
                    if(!duplicate(economicActivity)){
                        economicCompletionView.addObject(economicActivity);
                        Log.e("economic",economicActivity.Name+economicActivity.Code);
                    }

                }
                spinnerStep.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return v;
    }

    private void updateDatePicker(Date fromDate, Date toDate) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);

        Calendar cT = Calendar.getInstance();
        cT.setTime(toDate);
        fromDatePickerDialog = new DatePickerDialog(getContext(), this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        toDatePickerDialog = new DatePickerDialog(getContext(), this, cT.get(Calendar.YEAR),
                cT.get(Calendar.MONTH), cT.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(instance).registerReceiver(getJSONBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(getJSONBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                //onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateList(String jsonObjStr) {
        projects = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<ProjectStageContract>>() {
                }.getType());

        project_spinner = new String[projects.size() + 1];
        project_spinner[0] = "PROYECTOS";
        for (int i = 1; i <= projects.size(); i++) {
            project_spinner[i] = projects.get(i - 1).Name;
            projects.get(i-1).StringToArray();
        }
        spinnerProject.setAdapter(new CustomArrayAdapter(getActivity(), project_spinner));
        if (project_spinner.length > 1)
            spinnerProject.setSelection(1);
    }

    private void fillListStages(ProjectStageContract projet) {
        steps_spinner = new String[projet.ProjectStageList.size() + 1];
        steps_spinner[0] = "ETAPAS";
        ArrayList<EconomicActivity> economicActivities = new ArrayList<>();
        for (int i = 1; i < projet.ProjectStageList.size(); i++) {
            steps_spinner[i] = projet.ProjectStageList.get(i - 1).Name;
            economicActivities.add(new EconomicActivity(projet.ProjectStageList.get(i - 1).Name, "" + projet.ProjectStageList.get(i - 1).Id));
        }
        FilteredArrayAdapter<EconomicActivity> adapterEconomic = new FilteredArrayAdapter<EconomicActivity>(instance,
                R.layout.simple_spinner_item, economicActivities) {
            @Override
            protected boolean keepObject(EconomicActivity obj, String mask) {
                mask = mask.toLowerCase();
                return obj.Code.toLowerCase().startsWith(mask) || obj.Name.toLowerCase().startsWith(mask);

            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.simple_spinner_item_2, parent, false);
                }
                EconomicActivity economicActivity = getItem(position);
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(economicActivity.Code);
                /*TextView textView2 = convertView.findViewById(android.R.id.text2);
                textView2.setText(economicActivity.Name);*/
                return convertView;
            }
        };
        char[] splitChar = {',', ';', ' '};
        economicCompletionView.setAdapter(adapterEconomic);
        economicCompletionView.allowDuplicates(false);
        economicCompletionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        economicCompletionView.setSplitChar(splitChar);
        for (int i = 1; i < steps_spinner.length; i++) {
            steps_spinner[i] = projet.ProjectStageList.get(i - 1).Name;
        }
        spinnerStep.setAdapter(new CustomArrayAdapter(getActivity(), steps_spinner));
    }

    private void fillListStagesEdit(String jsonObjStr) {
        stages = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<ProjectStages>>() {
                }.getType());
        steps_spinner = new String[stages.size() + 1];
        steps_spinner[0] = "ETAPAS";
        ArrayList<EconomicActivity> economicActivities = new ArrayList<>();
        for (int i = 1; i < stages.size(); i++) {
            steps_spinner[i] = stages.get(i - 1).Name;
            economicActivities.add(new EconomicActivity(stages.get(i - 1).Name, "" + stages.get(i - 1).Id));
        }
        FilteredArrayAdapter<EconomicActivity> adapterEconomic = new FilteredArrayAdapter<EconomicActivity>(instance,
                R.layout.simple_spinner_item, economicActivities) {
            @Override
            protected boolean keepObject(EconomicActivity obj, String mask) {
                mask = mask.toLowerCase();
                return obj.Code.toLowerCase().startsWith(mask) || obj.Name.toLowerCase().startsWith(mask);

            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.simple_spinner_item_2, parent, false);
                }
                char[] splitChar = {',', ';', ' '};
                EconomicActivity economicActivity = getItem(position);
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(economicActivity.Code);
                /*TextView textView2 = convertView.findViewById(android.R.id.text2);
                textView2.setText(economicActivity.Name);*/
                return convertView;
            }
        };
        char[] splitChar = {',', ';', ' '};
        economicCompletionView.setAdapter(adapterEconomic);
        economicCompletionView.allowDuplicates(false);
        economicCompletionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        economicCompletionView.setSplitChar(splitChar);
        for (int i = 1; i < steps_spinner.length; i++) {
            steps_spinner[i] = stages.get(i - 1).Name;
            if (projectList.ProjectStages != null) {
                for (int j = 0; j < projectList.ProjectStages.length; j++) {
                    if (projectList.ProjectStages[j].equals(stages.get(i - 1).Id+"")) {
                        EconomicActivity economicActivity = new EconomicActivity(stages.get(i - 1).Name, stages.get(i - 1).Id + "");
                        economicCompletionView.addObject(economicActivity);
                        break;
                    }
                }
            }
        }
        spinnerStep.setAdapter(new CustomArrayAdapter(getActivity(), steps_spinner));
    }

    @OnClick(R.id.cancel_button)
    public void cancel() {
        if (mListener != null) {
            mListener.onCancelActionAddProject();
        }
    }

    private boolean duplicate(EconomicActivity economicActivity){
        ArrayList<EconomicActivity> economicActivities = (ArrayList<EconomicActivity>) economicCompletionView.getObjects();
        ArrayList<Integer> stages = new ArrayList<>();
        for (int i = 0; i < economicActivities.size(); i++) {
            if(economicActivities.get(i).Code==economicActivity.Code){
               return true;
            }
        }
        return false;
    }

    @OnClick(R.id.create_button)
    public void send() {
        View focusView = null;
        ArrayList<EconomicActivity> economicActivities = (ArrayList<EconomicActivity>) economicCompletionView.getObjects();

        boolean cancel = false;
        if (fromDateStr.isEmpty()) {
            fromDateBtn.setError(getString(R.string.error_field_required));
            fromDateBtn.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError(getString(R.string.error_field_required));
            focusView = tvToDateError;
            cancel = true;
        } else if (toDateStr.isEmpty()) {
            toDateBtn.setError(getString(R.string.error_field_required));
            toDateBtn.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError(getString(R.string.error_field_required));
            focusView = tvToDateError;
            cancel = true;
        } else if (fromDate.getTime() > toDate.getTime()) {
            toDateBtn.setError("La fecha debe ser mayor a la de inicio");
            toDateBtn.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError(getString(R.string.error_field_required));
            focusView = tvToDateError;
            cancel = true;
        }else if(economicActivities.size()<=0){
            cancel=true;
            focusView=economicCompletionView;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("Se debe seleccionar al menos una etapa");
            builder.create().show();
        }

        if (cancel) {
            focusView.requestFocus();

        } else {
             ArrayList<Integer> stages = new ArrayList<>();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            fromDateStr = format.format(fromDate);
            toDateStr = format.format(toDate);

            int[] economicActivityArray = new int[economicActivities.size()];
            for (int i = 0; i < economicActivities.size(); i++) {
                Log.e("etapas",economicActivities.get(i).Name);
                economicActivityArray[i] = Integer.parseInt(economicActivities.get(i).Name);
            }
            ProjectContract newProject;
            if (!editProject) {
                ProjectStageContract project = projects.get((spinnerProject.getSelectedItemPosition()) - 1);
                newProject = new ProjectContract(fromDateStr, toDateStr, economicActivityArray, project.Id);
            } else {
                newProject = new ProjectContract(fromDateStr, toDateStr, economicActivityArray, "" + projectList.ProjectId);
            }
            Gson gson = new Gson();
            String json = gson.toJson(newProject);
            CrudIntentService.startRequestCRUD(getActivity(),
                    Constantes.LIST_CONTRACTS_URL + contractId+ "/Project/", Request.Method.POST, json, "", false);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        int code = (Integer) view.getTag();
        logE("CODE: " + code);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        String myFormat = "dd/MMM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
        switch (code) {
            case R.id.btn_from_date:
                fromDateStr = sdf.format(calendar.getTime());
                fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                fromDateBtn.setText(fromDateStr);
                fromDate = calendar.getTime();
                fromDateBtn.setError(null);

                tvFromDateError.setVisibility(View.GONE);
                tvFromDateError.setError(null);
                break;
            case R.id.btn_to_date:
                toDateStr = sdf.format(calendar.getTime());
                toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                toDateBtn.setText(toDateStr);
                toDate = calendar.getTime();
                toDateBtn.setError(null);

                tvToDateError.setVisibility(View.GONE);
                tvToDateError.setError(null);
                break;
        }
    }


    @Override
    public void onTextChanged(EditText view, String text) {

    }

    @Override
    public void didClearText(View view) {

    }

    public interface OnAddProjectContract {
        void onCancelActionAddProject();

        void onApplyActionAddProject();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddProjectContract) {
            mListener = (OnAddProjectContract) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {

        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (mListener != null) {
                    mListener.onApplyActionAddProject();
                }
                break;
            case Constantes.REQUEST_NOT_FOUND:

                break;
            case Constantes.SEND_REQUEST:
                //msgSuccess();
               // logW(jsonObjStr);
                if (!editProject)
                    updateList(jsonObjStr);
                else
                    fillListStagesEdit(jsonObjStr);
                break;
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
                //MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
        }
    }
    /*private void msgSuccess() {
        MetodosPublicos.alertDialog(getContext(), "Proyecto creado satisfactoriamente");
    }*/

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
