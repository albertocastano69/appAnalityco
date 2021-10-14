package co.tecno.sersoluciones.analityco.individualContract;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.ApplicationContext;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.repositories.PositionIndividualContract;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.DaneCity;
import co.tecno.sersoluciones.analityco.models.IndividualContractsTypeModel;
import co.tecno.sersoluciones.analityco.models.InfoIndividualContract;
import co.tecno.sersoluciones.analityco.models.PayPeriod;
import co.tecno.sersoluciones.analityco.models.PersonalInividualContractRequest;
import co.tecno.sersoluciones.analityco.models.PlaceToJob;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

@SuppressLint("NonConstantResourceId")
public class DataForContractFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener, DatePickerDialog.OnDateSetListener{

    @Inject PersonalViewModel viewModel;
    @Inject CreatePersonalViewModel CreateviewModel;

    View view;
    Date dateInit;
    DatePickerDialog dateInitPickerDialog;
    private static final String ARG_POSTION = "position";
    private static final String ARG_PROJECT_NAME = "project";
    private static final String ARG_CITY_NAME = "city";
    private static final String ARG_STATE_NAME = "state";
    @BindView(R.id.typeContractCompleteTextView)
    AutoCompleteTextView TypeContract;
    @BindView(R.id.type_contract)
    TextInputLayout TypeContractInputLayout;
    @BindView(R.id.typePositionCompleteTextView)
    AutoCompleteTextView TypePosition;
    @BindView(R.id.type_position)
    TextInputLayout TypePositionInputLayout;
    @BindView(R.id.salary_edText)
    EditText Salary;
    @BindView(R.id.salary)
    TextInputLayout SalaryInputLayout;
    @BindView(R.id.edt_init_date)
    EditText startDate;
    @BindView(R.id.payPeriodCompleteTextView)
    AutoCompleteTextView Period;
    @BindView(R.id.pay_period)
    TextInputLayout PayPeriodInputLayout;
    @BindView(R.id.countryContractcCompleteTextView)
    AutoCompleteTextView Country;
    @BindView(R.id.CountryContract)
    TextInputLayout CountryInputLayout;
    @BindView(R.id.cityContractCompleteTextView)
    AutoCompleteTextView CityTv;
    @BindView(R.id.CityContract)
    TextInputLayout CityInputLayout;
    @BindView(R.id.placeJobCompleteTextView)
    AutoCompleteTextView PlaceJob;
    @BindView(R.id.PlaceJob)
    TextInputLayout PlaceJobInputLayout;
    @BindView(R.id.control_buttons)
    View controlButtons;
    @BindView(R.id.next_button)
    Button next_Button;
    @BindView(R.id.back_button)
    Button back_Button;
    @BindView(R.id.ordenRequest)
    LinearLayout ordenRequest;
    @BindView(R.id.ordenAproved)
    LinearLayout  ordenAproved;
    @BindView(R.id.ordenReject)
    LinearLayout  ordenReject;
    @BindView(R.id.mProgressView)
    LinearLayout mProgressView;
    @BindView(R.id.label_comment_reject)
    TextView label_comment_reject;
    @BindView(R.id.comment_reject)
    TextView comment_reject;
    @BindView(R.id.descriptionapproved)
    TextView descriptionapproved;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.iconFile)
    LinearLayout iconFile;
    int ContractType, PositionId, Payperiod;
    String ProjectId, PersonalEmployerInfoId, IdContract,Comment;
    User user;
    InfoIndividualContract item;
    PersonalInividualContractRequest itemSubmitRequest;
    Boolean IsAproved = false;
    boolean IsRejected = false;

    private RequestBroadcastReceiver requestBroadcastReceiver;

    public DataForContractFragment() {
    }
    public static  DataForContractFragment newinstance(InfoIndividualContract item, String Position, String ProjectName, String NameCity, String StateName){
        DataForContractFragment fragment = new DataForContractFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constantes.ITEM_OBJ,item);
        args.putString(ARG_POSTION,Position);
        args.putString(ARG_PROJECT_NAME,ProjectName);
        args.putString(ARG_CITY_NAME,NameCity);
        args.putString(ARG_STATE_NAME,StateName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_request_order, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if(getArguments() != null){
            item = getArguments().getParcelable(Constantes.ITEM_OBJ);
            Locale currentLocale = Locale.getDefault();
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
            otherSymbols.setDecimalSeparator(',');
            otherSymbols.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("#,###,###", otherSymbols);
            String salary = formatter.format(Long.parseLong(String.valueOf(item.Salary)));
            Salary.setText(salary);


            TypePosition.setText(getArguments().getString(ARG_POSTION));

            requestBroadcastReceiver = new RequestBroadcastReceiver(this);

            if(getArguments().getString(ARG_CITY_NAME) != null){
                CityTv.setText(String.format("%s %s", getArguments().getString(ARG_CITY_NAME), getArguments().getString(ARG_STATE_NAME)));
            }
            PlaceJob.setText(getArguments().getString(ARG_PROJECT_NAME));

            if(!item.StartDate.equals("0001-01-01T00:00:00")){
                String Start = "";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Date date = simpleDateFormat.parse(item.StartDate);
                    Start= format.format(date);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                startDate.setText(Start);
            }else {
                startDate.setText("");
            }
            Country.setText("Colombia");
            ContractType = item.IndividualContractTypeId;
            PositionId = item.PositionId;
            Payperiod = item.PayPeriodId;
            ProjectId = item.ProjectId;
            PersonalEmployerInfoId = item.PersonalEmployerInfo;
            IdContract = item.Id;
            Comment = item.Comment;

            viewModel.getPosition(item.EmployerId);
            CreateviewModel.getPlaceToJobWizard(item.EmployerId);



            Gson gson = new Gson();
            MyPreferences myPreferences = new MyPreferences(requireContext());
            user = gson.fromJson(myPreferences.getProfile(),User.class);
            itemSubmitRequest = new PersonalInividualContractRequest();

            switch (item.Descripction) {
                case "SOLICITADO":
                    CRUDService.startRequest(
                            getActivity(), Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL + IdContract,
                            Request.Method.GET, "", false
                    );
                    //RequestoOrderRequest();
                    //ExecuteMethodToAprroved();
                    IsAproved = false;
                    break;
                case "INICIADO":
                    IsAproved = false;
                    next_Button.setText("SOLICITAR");
                    next_Button.setEnabled(ValidatePermission());
                    next_Button.setOnClickListener(v -> {
                        if (Submit()) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
                            LayoutInflater inflater1 = getLayoutInflater();
                            View dialogView = inflater1.inflate(R.layout.custom_alert_dialog, null);
                            Button Btn_aceptar = dialogView.findViewById(R.id.Aceptar);
                            Button Btn_cancelar = dialogView.findViewById(R.id.Cancel);
                            dialogBuilder.setView(dialogView);
                            AlertDialog alertDialog = dialogBuilder.create();
                            alertDialog.show();
                            Btn_aceptar.setOnClickListener(v1 -> {
                                RequestOrder();
                                alertDialog.dismiss();
                            });
                            Btn_cancelar.setOnClickListener(v12 -> alertDialog.dismiss());
                        }
                    });
                    break;
                case "RECHAZADO":
                    next_Button.setText("SOLICITAR");
                    next_Button.setEnabled(ValidatePermission());
                    next_Button.setOnClickListener(v -> {
                        if (Submit()) {
                            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
                            LayoutInflater inflater1 = getLayoutInflater();
                            View dialogView = inflater1.inflate(R.layout.custom_alert_dialog, null);
                            Button Btn_aceptar = dialogView.findViewById(R.id.Aceptar);
                            Button Btn_cancelar = dialogView.findViewById(R.id.Cancel);
                            dialogBuilder.setView(dialogView);
                            AlertDialog alertDialog = dialogBuilder.create();
                            alertDialog.show();
                            Btn_aceptar.setOnClickListener(v1 -> {
                                RequestOrder();
                                alertDialog.dismiss();
                            });
                            Btn_cancelar.setOnClickListener(v12 -> alertDialog.dismiss());
                        }
                    });
                    label_comment_reject.setVisibility(View.VISIBLE);
                    comment_reject.setVisibility(View.VISIBLE);
                    comment_reject.setText(Comment);
                    break;
                case "APROBADO":
                    CRUDService.startRequest(
                            getActivity(), Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL + IdContract,
                            Request.Method.GET, "", false
                    );
                    IsAproved = true;
                    DisableComponents(true);
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                    controlButtons.setVisibility(View.GONE);
                    break;
                case "CONTRATADO":
                case "LIQUIDADO":
                    DisableComponents(true);
                    controlButtons.setVisibility(View.GONE);
                    break;
            }

            CreateviewModel.getPlaceJob().observe(getViewLifecycleOwner(), placeToJobs -> {
                ArrayAdapter  adapter = new ArrayAdapter(requireContext(),android.R.layout.simple_list_item_1,placeToJobs);
                PlaceJob.setAdapter(adapter);
            });

            PlaceJob.setOnItemClickListener((parent, view, position, id) -> {

                PlaceToJob item = (PlaceToJob) parent.getAdapter().getItem(position);
                itemSubmitRequest.setProjectId(item.getProjectid());
                itemSubmitRequest.setContractId(item.getContractId());
            });


            viewModel.getDaneCities().observe(getViewLifecycleOwner(), daneCities -> {

                ArrayAdapter item = new ArrayAdapter(requireContext(),R.layout.list_item,daneCities);
                CityTv.setAdapter(item);
            });

            CityTv.setOnItemClickListener((parent, view, position, id) -> {
                DaneCity item = (DaneCity) parent.getAdapter().getItem(position);
                itemSubmitRequest.setContractCityId(Integer.valueOf(item.CityCode));
            });

            viewModel.getPosition().observe(getViewLifecycleOwner(), positionIndividualContracts -> {
                ArrayAdapter item = new ArrayAdapter(requireContext(),R.layout.list_item,positionIndividualContracts);
                TypePosition.setAdapter(item);
            });
            TypePosition.setOnItemClickListener((parent, view, position, id) -> {
                PositionIndividualContract item = (PositionIndividualContract) parent.getAdapter().getItem(position);
                itemSubmitRequest.setPositionId(item.getId());
            });
            viewModel.getContractType().observe(getViewLifecycleOwner(), individualContractsTypeModels -> {
                ArrayAdapter itemAdapter = new ArrayAdapter(requireContext(),R.layout.list_item,individualContractsTypeModels);
                TypeContract.setAdapter(itemAdapter);
                if(individualContractsTypeModels.size() == 1 || item.IndividualContractTypeId != 0 ){
                    for (IndividualContractsTypeModel item : individualContractsTypeModels){
                        TypeContract.setText(item.Description);
                        itemSubmitRequest.setIndividualContractTypeId(Integer.valueOf(item.Id));
                    }
                }
            });

            TypeContract.setOnItemClickListener((parent, view, position, id) -> {
                IndividualContractsTypeModel item = (IndividualContractsTypeModel) parent.getAdapter().getItem(position);
                itemSubmitRequest.setIndividualContractTypeId(Integer.valueOf(item.Id));
            });

            viewModel.getPayPeriod().observe(getViewLifecycleOwner(), payPeriods -> {
                ArrayAdapter itemAdapter = new ArrayAdapter(requireContext(),R.layout.list_item,payPeriods);
                Period.setAdapter(itemAdapter);
                for(PayPeriod item : payPeriods){
                    Period.setText(item.Description);
                    itemSubmitRequest.setPayPeriodId(item.Id);
                }
            });
            Period.setOnItemClickListener((parent, view, position, id) -> {
                PayPeriod item = (PayPeriod) parent.getAdapter().getItem(position);
                itemSubmitRequest.setPayPeriodId(item.Id);
            });

            SearchDataBD(ContractType,Payperiod);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        dateInit = calendar.getTime();

        configDatePicker(dateInit);

        startDate.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                if(startDate.getText().toString().isEmpty()){
                    showDatePicker();
                }else {
                    String format = "dd/MMM/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(format, new Locale("es", "ES"));
                    try {
                        dateInit = sdf.parse(startDate.toString());
                    }catch (ParseException e){
                        e.printStackTrace();
                    }


                }
            }
        });

        return view;
    }



    private void configDatePicker(Date dateInit) {
        Date currentDate = MetodosPublicos.getCurrentDate();
        Calendar cF = Calendar.getInstance();
        cF.setTime(dateInit);
        dateInitPickerDialog = new DatePickerDialog(requireActivity(),this,cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        dateInitPickerDialog.getDatePicker().setMinDate(currentDate.getTime());
    }
    private  void showDatePicker(){
        dateInitPickerDialog.getDatePicker().setTag(R.id.btn_from_date);
        dateInitPickerDialog.show();
    }
    @SuppressLint("SimpleDateFormat")
    private void RequestOrder() {
        String salary = Salary.getText().toString();
        itemSubmitRequest.setSalary(Integer.valueOf(salary));
        SimpleDateFormat formatStart  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String DateStart = formatStart.format(dateInit);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("IndividualContractTypeId", itemSubmitRequest.getIndividualContractTypeId());
            jsonObject.put("PersonalEmployerInfoId", PersonalEmployerInfoId);
            jsonObject.put("PositionId", itemSubmitRequest.getPositionId());
            jsonObject.put("Salary", itemSubmitRequest.getSalary());
            jsonObject.put("PositionId", itemSubmitRequest.getPositionId());
            jsonObject.put("PayPeriodId", itemSubmitRequest.getPayPeriodId());
            jsonObject.put("StartDate", DateStart);
            jsonObject.put("ProjectId", itemSubmitRequest.getProjectId());
            jsonObject.put("ContractId", itemSubmitRequest.getContractId());
            jsonObject.put("ContractCityId", itemSubmitRequest.getContractCityId());
        }catch (JSONException e){
            e.printStackTrace();
        }
        JSONObject jsonObjectAddStage = new JSONObject();
        try{
            jsonObjectAddStage.put("IndividualContractId", IdContract);
            jsonObjectAddStage.put("StageTypeId", requestStageTypeId("SOLICITADO"));
        }catch (JSONException e){
            e.printStackTrace();
        }
        CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_INDIVIDUALCONTRACTS_URL + IdContract,
                Request.Method.PUT, jsonObject.toString(), "", false);
        IsRejected = false;
        CrudIntentService.startRequestCRUD(
                getActivity(),
                Constantes.REQUEST_INDIVIDUAL_CONTRACT_ADDSTAGE_URL, Request.Method.POST, jsonObjectAddStage.toString(), "", false
        );
        showProgress(true);
    }
    @SuppressLint("Recycle")
    private void SearchDataBD(int contractType, int payperiod) {
        String selection = "(" + DBHelper.COPTIONS_COLUMN_SERVER_ID + " = ?)";
         Cursor Contract = requireActivity().getContentResolver().query(Constantes.CONTENT_COMMON_OPTIONS_URI,null,selection,new String[]{String.valueOf(contractType)},null);
        if(Contract.moveToFirst()){
            TypeContract.setText(Contract.getString(Contract.getColumnIndex(DBHelper.COPTIONS_COLUMN_DESC)));
        }
        Cursor PayPeriod = requireActivity().getContentResolver().query(Constantes.CONTENT_COMMON_OPTIONS_URI,null,selection,new String[]{String.valueOf(payperiod)},null);
        if(PayPeriod.moveToFirst()){
            Period.setText(PayPeriod.getString(PayPeriod.getColumnIndex(DBHelper.COPTIONS_COLUMN_DESC)));
        }
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        ApplicationContext.analitycoComponent.inject(this);
    }

    private boolean ValidatePermission(){
        boolean permission =  false;
        if(user.claims.contains("contractingdata.requestdelete") || user.IsSuperUser){
            permission  = true;
        }
        return permission;
    }
    private boolean ValidatePermissionAprroved(){
        boolean permission =  false;
        if(user.claims.contains("contractingdata.approvereject") || user.IsSuperUser){
            permission  = true;
        }
        return permission;
    }
    @Override
    public void onStringResult(@org.jetbrains.annotations.Nullable String action, int option, @org.jetbrains.annotations.Nullable String response, @org.jetbrains.annotations.Nullable String url) {
        if (option == Constantes.SUCCESS_REQUEST) {
            if (url.equals(Constantes.LIST_INDIVIDUALCONTRACTS_URL + IdContract)) {
                if(IsRejected){
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject Stage = jsonObject.getJSONObject("IndividualContractStage");
                        Comment = Stage.getString("Comment");
                        label_comment_reject.setVisibility(View.VISIBLE);
                        comment_reject.setVisibility(View.VISIBLE);
                        comment_reject.setText(Comment);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    CrudIntentService.startRequestCRUD(
                            requireActivity(),
                            Constantes.REQUEST_HIRING_DOCUMENT_URL + IdContract, Request.Method.POST, "", "", false
                    );
                    ordenReject.setVisibility(View.GONE);
                }
            }
            if (url.equals(Constantes.REQUEST_HIRING_DOCUMENT_URL + IdContract)) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String urlOrdenRequest = jsonObject.getString("url");
                    ExecuteOrderRequest(urlOrdenRequest);
                    comment_reject.setVisibility(View.GONE);
                    label_comment_reject.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (url.equals(Constantes.REQUEST_APROVED_HIRING_DOCUMENT_URL + IdContract)) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String urlOrdenAproved = jsonObject.getString("url");
                    ExecuteOrderAproved(urlOrdenAproved);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (url.equals(Constantes.REQUEST_REJECT_HIRING_DOCUMENT_URL + IdContract)) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String urlOrdenReject = jsonObject.getString("url");
                    ExecuteOrderReject(urlOrdenReject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(url.equals(Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL + IdContract)){
                ShowDocuments(response);

            }
        }
    }

    private void ShowDocuments(String response) {
        try {
            JSONArray jsonArrayRequeriments = new JSONArray(response);
            String DocumentContents;
            for (int i = 0; i < jsonArrayRequeriments.length(); i++) {
                JSONObject object = jsonArrayRequeriments.getJSONObject(i);
                JSONObject Document = object.getJSONObject("Document");
                DocumentContents = Document.getString("DocumentContents");
                if(DocumentContents.equals("SOLICITUD_DE_INGRESO_APROBADA")){
                    String UrlFile = object.getString("File");
                    descriptionapproved.setText(Document.getString("Name"));
                    ordenAproved.setVisibility(View.VISIBLE);
                    iconFile.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.ic_blank_paper));
                    description.setTextColor(ContextCompat.getColor(getActivity(),R.color.black_alpha));
                    ordenAproved.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(UrlFile));
                            startActivity(intent);
                        }
                    });
                }
                if(DocumentContents.equals("SOLICITUD_DE_INGRESO")){
                    String UrlFile = object.getString("File");
                    object = jsonArrayRequeriments.getJSONObject(i);
                    Document = object.getJSONObject("Document");
                    description.setText(Document.getString("Name"));
                    ordenRequest.setVisibility(View.VISIBLE);
                    ordenRequest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(UrlFile));
                            startActivity(intent);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    private void ExecuteOrderReject(String urlOrdenReject) {
        IsRejected = true;
        CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_INDIVIDUALCONTRACTS_URL + IdContract,
                Request.Method.GET, "", "", false);
        if(urlOrdenReject != null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.expiry)));
            showProgress(false);
            next_Button.setText("SOLICITAR ORDEN");
            back_Button.setText("ANTERIOR");
            next_Button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.bar_decoded)));
            next_Button.setOnClickListener(v -> {
                if(Submit()){
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.custom_alert_dialog,null);
                    Button Btn_aceptar = dialogView.findViewById(R.id.Aceptar);
                    Button Btn_cancelar = dialogView.findViewById(R.id.Cancel);
                    dialogBuilder.setView(dialogView);
                    AlertDialog alertDialog = dialogBuilder.create();
                    alertDialog.show();
                    Btn_aceptar.setOnClickListener(v1 -> {
                        RequestOrder();
                        alertDialog.dismiss();
                    });
                    Btn_cancelar.setOnClickListener(v12 -> alertDialog.dismiss());
                }
            });
            ordenReject.setVisibility(View.VISIBLE);
            ordenAproved.setVisibility(View.GONE);
            iconFile.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.ic_blank_paper));
            View focusView = ordenReject;
            focusView.requestFocus();
            ordenReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlOrdenReject));
                    startActivity(intent);
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void ExecuteOrderAproved(String urlOrdenAproved) {
        showProgress(false);
        back_Button.setText("RECHAZAR ORDEN");
        back_Button.setEnabled(false);
        back_Button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
        next_Button.setEnabled(false);
        next_Button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
        DisableComponents(true);
        if(urlOrdenAproved != null){
            ((AppCompatActivity)getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            ordenAproved.setVisibility(View.VISIBLE);
            ordenRequest.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.ic_blank_paper));
            View focuesView = ordenAproved;
            focuesView.requestFocus();
            ordenAproved.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlOrdenAproved));
                startActivity(intent);
            });
        }
    }


    @SuppressLint("SetTextI18n")
    private void ExecuteOrderRequest(String urlOrdenRequest) {
        showProgress(false);
        if(urlOrdenRequest != null) {
            if(!IsAproved){
                ((AppCompatActivity)getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.accent)));
            }
            ordenRequest.setVisibility(View.VISIBLE);
            View focusView = ordenRequest;
            focusView.requestFocus();
            ordenRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(urlOrdenRequest));
                    startActivity(intent);
                }
            });
            if (!ValidatePermissionAprroved()) {
                DisableComponents(true);
                return;
            }
            back_Button.setText("RECHAZAR ORDEN");
            back_Button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
            back_Button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
            next_Button.setText("APROBAR ORDEN");
            next_Button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
            next_Button.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));
            processToAproved();

        }
    }
    @SuppressLint("SetTextI18n")
    private  void processToAproved(){
        next_Button.setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);
            Button Btn_aceptar = dialogView.findViewById(R.id.Aceptar);
            Button Btn_cancelar = dialogView.findViewById(R.id.Cancel);
            TextView Title = dialogView.findViewById(R.id.labelConfirmar);
            TextView SubTitle = dialogView.findViewById(R.id.labelsubtitle);
            TextView Email = dialogView.findViewById(R.id.labelEmail);
            Title.setText("CONFIRMAR APROBACIÓN");
            SubTitle.setVisibility(View.GONE);
            Email.setVisibility(View.GONE);
            dialogBuilder.setView(dialogView);
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();
            Btn_cancelar.setOnClickListener(v14 -> alertDialog.dismiss());
            Btn_aceptar.setOnClickListener(v13 -> {
                OrdenAprovedRequest();
                alertDialog.dismiss();
                showProgress(true);
            });
        });

        back_Button.setOnClickListener(v -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_rejected, null);
            EditText comment = dialogView.findViewById(R.id.commentRejected);
            Button Btn_aceptar = dialogView.findViewById(R.id.Aceptar);
            Button Btn_cancelar = dialogView.findViewById(R.id.Cancel);
            dialogBuilder.setView(dialogView);
            AlertDialog alertDialog = dialogBuilder.create();
            alertDialog.show();

            Btn_aceptar.setOnClickListener(v12 -> {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("IndividualContractId", IdContract);
                    jsonObject.put("StageTypeId", requestStageTypeId("RECHAZADO"));
                    if(!comment.getText().toString().isEmpty()){
                        jsonObject.put("Comment", comment.getText().toString());
                    }else{
                        jsonObject.put("Comment", "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                CrudIntentService.startRequestCRUD(
                        requireActivity(),
                        Constantes.REQUEST_INDIVIDUAL_CONTRACT_ADDSTAGE_URL, Request.Method.POST, jsonObject.toString(), "", false
                );
                CrudIntentService.startRequestCRUD(
                        requireActivity(),
                        Constantes.REQUEST_REJECT_HIRING_DOCUMENT_URL + IdContract, Request.Method.POST, "", "", false
                );
                alertDialog.dismiss();
                showProgress(true);
            });

            Btn_cancelar.setOnClickListener(v1 -> alertDialog.dismiss());
        });
    }

    private void OrdenAprovedRequest(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("IndividualContractId", IdContract);
            jsonObject.put("StageTypeId", requestStageTypeId("APROBADO"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CrudIntentService.startRequestCRUD(
                requireActivity(),
                Constantes.REQUEST_INDIVIDUAL_CONTRACT_ADDSTAGE_URL, Request.Method.POST, jsonObject.toString(), "", false
        );
        CrudIntentService.startRequestCRUD(
                requireActivity(),
                Constantes.REQUEST_APROVED_HIRING_DOCUMENT_URL + IdContract, Request.Method.POST, "", "", false
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE);
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT);
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        String myFormat = "dd/MMM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
        String dateInitStr = sdf.format(calendar.getTime());
        startDate.setText(dateInitStr);
        dateInit = calendar.getTime();
    }
    private void DisableComponents(boolean Disable){
        if(Disable){
            TypeContract.setEnabled(false);
            TypeContractInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            TypePosition.setEnabled(false);
            TypePositionInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            Salary.setEnabled(false);
            SalaryInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            Period.setEnabled(false);
            PayPeriodInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            Country.setEnabled(false);
            CountryInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            CityTv.setEnabled(false);
            CityInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            PlaceJob.setEnabled(false);
            PlaceJobInputLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
            startDate.setEnabled(false);
        }else {
            TypeContract.setEnabled(true);
            TypeContractInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            TypePosition.setEnabled(true);
            TypePositionInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            Salary.setEnabled(true);
            SalaryInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            Period.setEnabled(true);
            PayPeriodInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            Country.setEnabled(true);
            CountryInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            CityTv.setEnabled(true);
            CityInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            PlaceJob.setEnabled(true);
            PlaceJobInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
            startDate.setEnabled(true);
        }
    }
    private boolean Submit(){
        boolean send = true;
        View focusView = null;
        if(TextUtils.isEmpty(TypeContract.getText().toString())){
            TypePosition.setError(getString(R.string.error_field_required));
            send = false;
            focusView = TypeContract;
        }else if (TextUtils.isEmpty(TypePosition.getText().toString())){
            TypePosition.setError(getString(R.string.error_field_required));
            send = false;
            focusView = TypePosition;
        }else if(TextUtils.isEmpty(Salary.getText().toString())){
            send = false;
            focusView = Salary;
        }else if(TextUtils.isEmpty(Period.getText().toString())){
            Period.setError(getString(R.string.error_field_required));
            send = false;
            focusView = Period;
        }else if(TextUtils.isEmpty(Country.getText().toString())){
            Country.setError(getString(R.string.error_field_required));
            send = false;
            focusView = Country;
        }else if (TextUtils.isEmpty(CityTv.getText().toString())){
            CityTv.setError(getString(R.string.error_field_required));
            send = false;
            focusView = CityTv;
        }else if(TextUtils.isEmpty(PlaceJob.getText().toString())){
            PlaceJob.setError(getString(R.string.error_field_required));
            send = false;
            focusView = PlaceJob;
        }
        if (!send) {
            focusView.requestFocus();
        }
        return  send;
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private int requestStageTypeId(String args){
        String selection = ("(" + DBHelper.COPTIONS_COLUMN_DESC + " = ?)");
        String [] selectionArgs = new  String [] {args};
        Cursor cursor = requireActivity().getApplicationContext().getContentResolver().query(Constantes.CONTENT_COMMON_OPTIONS_URI,null,selection,selectionArgs,null);
        cursor.moveToFirst();
        int stageId = cursor.getInt(cursor.getColumnIndex(DBHelper.COPTIONS_COLUMN_SERVER_ID));
        return stageId;
    }
}