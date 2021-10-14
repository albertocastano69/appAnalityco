package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.ContractList;
import co.tecno.sersoluciones.analityco.models.ContractType;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.PersonalContract;
import co.tecno.sersoluciones.analityco.models.Position;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;
import co.tecno.sersoluciones.analityco.views.DelayAutoCompleteTextView;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class PersonalContractFragmentEnroll extends Fragment implements DatePickerDialog.OnDateSetListener, TextWatcherAdapter.TextWatcherListener,
        ClearebleAutoCompleteTextView.Listener, RequestBroadcastReceiver.BroadcastListener {

    private Unbinder unbinder;
    private RequestBroadcastReceiver getJSONBroadcastReceiver;
    @BindView(R.id.spinner_subContract)
    Spinner spinner_subContract;
    private OnAddPersonalContract mListener;
    private Date toDate;
    private Date fromDate;
    private String fromDateStr;
    private String toDateStr;
    @BindView(R.id.icon_is_active)
    ImageView isActive;
    @BindView(R.id.layout_verification)
    TextInputLayout layoutVerification;
    @BindView(R.id.tvFromDateError)
    public TextView tvFromDateError;
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    @BindView(R.id.edit_position)
    DelayAutoCompleteTextView positionString;
    @BindView(R.id.card_view_detail)
    public CardView card_view_detail;
    @BindView(R.id.btn_from_date)
    Button fromDateBtn;
    @BindView(R.id.btn_to_date)
    Button toDateBtn;
    @BindView(R.id.tvToDateError)
    TextView tvToDateError;
    @BindView(R.id.edit_users)
    EditText identification;
    @BindView(R.id.fab_search)
    FloatingActionButton send;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String ARG_PARAM7 = "param7";
    private static final String ARG_PARAM8 = "param8";
    private boolean editPersonal;
    private int process = 0;
    @BindView(R.id.btn_edit)
    public ImageView btnEdit;
    @BindView(R.id.text_name)
    public TextView text_name;
    @BindView(R.id.text_sub_name)
    public TextView text_sub_name;
    @BindView(R.id.logo)
    public ImageView logo_imag;
    @BindView(R.id.text_validity)
    public TextView text_validity;
    @BindView(R.id.label_validity)
    public TextView label_validity;
    @BindView(R.id.text_active)
    public TextView textActive;
    @BindView(R.id.textError)
    public TextView textError;
    @BindView(R.id.details_contract)
    public CardView detailsContract;

    @BindView(R.id.contract_logo)
    public ImageView contractLogo;
    @BindView(R.id.contract_name)
    public TextView contractName;
    @BindView(R.id.contract_sub_name)
    public TextView contractSubname;
    @BindView(R.id.contract_text_icon1)
    public TextView textViewIcon1;
    @BindView(R.id.contract_text_icon2)
    public TextView textViewIcon2;
    @BindView(R.id.contract_icon1)
    public ImageView icon1;


    private Context instance;
    private String contractId;
    private Personal personal;
    private List<Position> positionList;
    private int sendPosition = 0;
    private String companyId;
    private Date finishDateContract;
    private ContractType contractType;
    private boolean flagMove = false;

    public PersonalContractFragmentEnroll() {

    }

    public static PersonalContractFragmentEnroll newInstanceEdit(Personal personal, String contractId, String companyId, String startdate, String finishdate, ContractType contractType, ContractList contractList) {
        PersonalContractFragmentEnroll fragment = new PersonalContractFragmentEnroll();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM3, personal);
        args.putBoolean(ARG_PARAM2, true);
        args.putString(ARG_PARAM1, contractId);
        args.putString(ARG_PARAM4, companyId);
        args.putString(ARG_PARAM5, startdate);
        args.putString(ARG_PARAM6, finishdate);
        args.putSerializable(ARG_PARAM7, contractType);
        args.putSerializable(ARG_PARAM8, contractList);
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
    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_personal_contract, container, false);
        unbinder = ButterKnife.bind(this, v);
        positionList = new ArrayList<Position>();
        flagMove = false;
        if (getArguments() != null) {
            editPersonal = getArguments().getBoolean(ARG_PARAM2);
            contractId = getArguments().getString(ARG_PARAM1);
            companyId = getArguments().getString(ARG_PARAM4);
            //startDate=getArguments().getString(ARG_PARAM5);
            String finishDate = getArguments().getString(ARG_PARAM6);
            contractType = (ContractType) getArguments().getSerializable(ARG_PARAM7);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            textActive.setVisibility(View.GONE);
            isActive.setVisibility(View.GONE);
            try {
                //  startDateContract=format.parse(startDate);
                finishDateContract = format.parse(finishDate);
                // fromDate=startDateContract;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance();
            fromDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            toDate = calendar.getTime();
            String myFormat = "dd/MMM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
            if (editPersonal) {
                fromDate = calendar.getTime();
                personal = (Personal) getArguments().getSerializable(ARG_PARAM3);
                logW(new Gson().toJson(personal));
                logW("personal:" + personal.PersonalCompanyInfoId);
                if (personal.Position != null) {
                    positionString.setText(personal.Position);
                }
                if (personal.StartDate != null) {
                    fromDateStr = personal.StartDate;
                    fromDateStr = sdf.format(fromDate);
                    fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    fromDateBtn.setText(fromDateStr);
                }
                if (personal.FinishDate != null) {
                    toDateStr = personal.FinishDate;
                    toDateStr = sdf.format(fromDate);
                    toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    toDateBtn.setText(fromDateStr);
                }
            } else {
                card_view_detail.setVisibility(View.GONE);
            }

            if (getArguments().getSerializable(ARG_PARAM8) != null) {
                ContractList contract = (ContractList) getArguments().getSerializable(ARG_PARAM8);
                detailsContract.setVisibility(View.VISIBLE);
                contractName.setText(contract.ContractReview);
                contractSubname.setText(contract.CompanyName);
                textViewIcon1.setText(sdf.format(finishDateContract));
                textViewIcon2.setText(contract.ContractNumber);

                if (contract.FormImageLogo != null) {
                    String url = Constantes.URL_IMAGES + contract.FormImageLogo;
                    String[] cFormat = url.split(Pattern.quote("."));
                    if (cFormat[cFormat.length - 1].equals("svg")) {
//                        Uri uri = Uri.parse(url);
                        Glide.with(getContext())
                                .load(url)
                                .apply(new RequestOptions()
                                        .placeholder(R.drawable.loading_animation)
                                        .error(R.drawable.image_not_available))
                                .into(contractLogo);
                    } else {
                        Picasso.get().load(url)
                                .resize(0, 250)
                                .placeholder(R.drawable.image_not_available)
                                .error(R.drawable.image_not_available)
                                .into(contractLogo);
                    }
                }
                switch (contractType.Value) {
                    case "AD":
                        icon1.setImageResource(R.drawable.ic_02_administrativotrasparente2);
                        break;
                    case "FU":
                        icon1.setImageResource(R.drawable.ic_14_funcionariotrasparente);
                        break;
                    case "PR":
                        icon1.setImageResource(R.drawable.ic_10_proveedor_trasparente);
                        break;
                    case "AS":
                        icon1.setImageResource(R.drawable.ic_04_asociado_trasparente2);
                        break;
                    case "VI":
                        icon1.setImageResource(R.drawable.ic_16_visitante_trasparente);
                        break;
                    case "OT":
                        icon1.setImageResource(R.drawable.ic_18_otros_trasparente);
                        break;
                    case "CO":
                        icon1.setImageResource(R.drawable.ic_06_contratista_trasparente);
                        break;
                }
            }

        }

        instance = getActivity();
        //positionString.setThreshold(4);
        positionString.setAdapter(new PositionAutoCompleteAdapterNew(getActivity())); // 'this' is Activity instance
        positionString.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Position positionJob = (Position) adapterView.getItemAtPosition(position);
                positionString.setText(positionJob.Name);
            }
        });
        if (editPersonal) {
            send.setVisibility(View.GONE);
            process = 1;
            toDateStr = personal.FinishDate;
            fromDateStr = personal.StartDate;
            //toDateStr = personal.FinishDate;
            positionString.setText(personal.Position);
            identification.setText(personal.DocumentNumber);
            layoutVerification.setVisibility(View.GONE);
            if (personal.Name == null) card_view_detail.setVisibility(View.GONE);
            text_name.setText(personal.Name + " " + personal.LastName);
            text_sub_name.setText(personal.DocumentNumber);
            btnEdit.setVisibility(View.GONE);
            label_validity.setVisibility(View.GONE);
            text_validity.setText(personal.JobName);
            if (personal.Position != null)
                positionString.setText(personal.Position);
            if (personal.StartDate != null)
                tvFromDateError.setText(personal.StartDate);
            if (personal.FinishDate != null)
                tvToDateError.setText(personal.FinishDate);

            if (personal.Photo != null) {
                String url = Constantes.URL_IMAGES + personal.Photo;

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo_imag);
            } else {
                logo_imag.setImageResource(R.drawable.image_not_available);
            }
            Drawable drawableIsActive = MaterialDrawableBuilder.with(getContext())
                    .setIcon(MaterialDrawableBuilder.IconValue.THUMB_UP)
                    .setColor(Color.GREEN)
                    .setSizeDp(35)
                    .build();

            isActive.setImageDrawable(drawableIsActive);


        }
        //toDateStr = "";

        updateDatePicker(fromDate, toDate);
        getJSONBroadcastReceiver = new RequestBroadcastReceiver(this);

        /*CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_POSITION_URL,
                Request.Method.GET, "", "", true);*/
        //api/Contract/{id}/SubContractor/
        CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_CONTRACTS_URL + contractId + "/SubContractor",
                Request.Method.GET, "", "", true);
        //api/Contract/{id}/IsContract/{personalCompanyInfoId}
        CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_CONTRACTS_URL + contractId + "/IsContract/" + personal.PersonalCompanyInfoId,
                Request.Method.GET, "", "", false);
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
        if (finishDateContract != null)
            toDatePickerDialog.getDatePicker().setMaxDate(finishDateContract.getTime());
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
        positionList = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<Position>>() {
                }.getType());

      /*  String[] position_spinner = new String[position.size() + 1];
        position_spinner[0] = "Posición";
        for (int i = 1; i <= position.size(); i++) {
            position_spinner[i] = position.get(i - 1).Name;
        }*/
        //spinnerRole.setAdapter(new CustomArrayAdapter(getActivity(), position_spinner));
    }

    private void updateListSubContract() {
       /* positionList = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<Position>>() {
                }.getType());

        String[] position_spinner = new String[position.size() + 1];
        position_spinner[0] = "Posición";
        for (int i = 1; i <= position.size(); i++) {
            position_spinner[i] = position.get(i - 1).Name;
        }
        spinner_subContract.setAdapter(new CustomArrayAdapter(getActivity(), position_spinner));*/

    }

    private void updateListEdit(String jsonObjStr) {
        positionList = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<Position>>() {
                }.getType());

        /*String[] position_spinner = new String[position.size() + 1];
        position_spinner[0] = "Posición";
        int selected = 0;
        for (int i = 1; i <= position.size(); i++) {
            position_spinner[i] = position.get(i - 1).Name;
            if (personal.PositionId == position.get(i - 1).Id)
                selected = i;
        }¨*/
        /*spinnerRole.setAdapter(new CustomArrayAdapter(getActivity(), position_spinner));
        spinnerRole.setSelection(selected);*/
    }

    @OnClick(R.id.fab_search)
    public void sendIdentification() {
        if (TextUtils.isEmpty(identification.getText().toString()))
            identification.setError("Ingrese cc");
        else {
            String url = Constantes.GETID_URL + identification.getText().toString() + "/" + contractId;
            CrudIntentService.startRequestCRUD(getActivity(), url,
                    Request.Method.GET, "", "", false);
        }

    }

    @OnClick(R.id.negative_button)
    public void cancel() {
        if (mListener != null) {
            mListener.onCancelActionAddPersonal();
        }
    }

    @OnClick(R.id.positive_button)
    public void send() {
        View focusView = null;
        boolean cancel = false;
        textError.setText("");
       /* if (fromDateStr.isEmpty()) {
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
        }*/
        if (fromDate.getTime() > toDate.getTime()) {
            toDateBtn.setError("La fecha debe ser mayor a la de inicio");
            toDateBtn.requestFocus();
            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError("La fecha debe ser mayor a la de inicio");
            focusView = tvToDateError;
            cancel = true;
        }/*else if (fromDate.getTime() < startDateContract.getTime()) {
            fromDateBtn.setError("La fecha no puede ser antes del inicio del contrato");
            fromDateBtn.requestFocus();

            tvFromDateError.setVisibility(View.VISIBLE);
            tvFromDateError.setError(getString(R.string.error_field_required));
            focusView = tvFromDateError;
            cancel = true;
        }*/ else if (toDate.getTime() > finishDateContract.getTime()) {
            toDateBtn.setError("La fecha no puede ser mayor al fin del contrato");
            toDateBtn.requestFocus();
            String myFormat = "dd/MMM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
            textError.setText("La fecha no puede ser mayor a " + sdf.format(finishDateContract.getTime()));
            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError(getString(R.string.error_field_required));
            focusView = tvFromDateError;
            cancel = true;
        }
        /* else if (spinnerRole.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinnerRole;
        }*/
        else if (process != 1) {
            cancel = true;
            identification.setError("Personal es obligatorio");
            focusView = identification;
        } else if ((contractType.Value.equals("AD") || contractType.Value.equals("AS") || contractType.Value.equals("CO")) && TextUtils.isEmpty(positionString.getText().toString())) {
            cancel = true;
            positionString.setError("Cargo para este tipo de contrato es obligatorio");
            focusView = positionString;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            fromDateStr = format.format(fromDate);
            toDateStr = format.format(toDate);
            PersonalContract newPersonal = new PersonalContract(fromDateStr, toDateStr, personal.PersonalCompanyInfoId, positionString.getText().toString());
            Gson gson = new Gson();
            String json = gson.toJson(newPersonal);
            logW(json);
            if (!flagMove) {
                CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_CONTRACTS_URL + contractId + "/PersonalInfo/",
                        Request.Method.POST, json, "", false);
            } else {
                CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_CONTRACTS_URL + contractId + "/MovePersonalInfo/",
                        Request.Method.POST, json, "", false);
            }
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

    private void fillFormPersonNew(String json) {
        personal = new Gson().fromJson(json,
                new TypeToken<Personal>() {
                }.getType());
        if (personal != null) {
            card_view_detail.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
            text_name.setText(personal.Name + " " + personal.LastName);
            if (personal.Photo != null) {
                String url = Constantes.URL_IMAGES + personal.Photo;

                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo_imag);
            } else {
                logo_imag.setImageResource(R.drawable.image_not_available);
            }
        }
    }

    public interface OnAddPersonalContract {
        void onCancelActionAddPersonal();

        void onApplyActionAddPersonal(Personal personal);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddPersonalContract) {
            mListener = (OnAddPersonalContract) context;
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
        Log.e("opción", "" + option);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (url.equals(Constantes.LIST_CONTRACTS_URL + contractId + "/IsContract/" + personal.PersonalCompanyInfoId)) {

                } else if (editPersonal) {
                    if (mListener != null) {
                        Personal personal = new Gson().fromJson(jsonObjStr,
                                new TypeToken<Personal>() {
                                }.getType());
                        mListener.onApplyActionAddPersonal(personal);
                    }
                } else if (process == 0) {
                    fillFormPersonNew(jsonObjStr);
                    process = 1;
                } else {
                    if (mListener != null) {
                        Log.e("Creo", "creo satisfactoriamet personal");
                        Personal personal = new Gson().fromJson(jsonObjStr,
                                new TypeToken<Personal>() {
                                }.getType());
                        mListener.onApplyActionAddPersonal(personal);
                    }
                }
                break;
            case Constantes.REQUEST_NOT_FOUND:
                if (url.equals(Constantes.LIST_CONTRACTS_URL + contractId + "/IsContract/" + personal.PersonalCompanyInfoId)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle("Contracto sin proyectos ")
                            .setCancelable(false)
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mListener != null) {
                                        mListener.onCancelActionAddPersonal();
                                    }
                                }
                            })
                            .setMessage("Este contrato no tiene proyectos asociados");
                    builder.create().show();
                } else if (url.equals(Constantes.LIST_CONTRACTS_URL + contractId + "/SubContractor")) {
                    spinner_subContract.setVisibility(View.GONE);
                } else if (sendPosition == 0 && !url.equals(Constantes.LIST_CONTRACTS_URL + "/" + contractId + "/IsContract/" + personal.PersonalCompanyInfoId)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle("Personal no encontrado")
                            .setCancelable(false)
                            .setPositiveButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mListener != null) {
                                        mListener.onCancelActionAddPersonal();
                                    }
                                }
                            })
                            .setNegativeButton("Si", null)
                            .setMessage("Desea buscar con otro número de cedula");
                    builder.create().show();
                } else if (!url.equals(Constantes.LIST_CONTRACTS_URL + contractId + "/IsContract/" + personal.PersonalCompanyInfoId)) {
                    sendPosition = 0;
                }
                break;
            case Constantes.SEND_REQUEST:
                //msgSuccess();
                if (url.equals(Constantes.LIST_CONTRACTS_URL + contractId + "/SubContractor")) {
                    updateListSubContract();
                } else if (editPersonal)
                    updateListEdit(jsonObjStr);
                else
                    updateList(jsonObjStr);
                break;
            case Constantes.BAD_REQUEST:
                logW("jsonObjStr: " + jsonObjStr);
                if (url.equals(Constantes.LIST_CONTRACTS_URL + contractId + "/IsContract/" + personal.PersonalCompanyInfoId) && jsonObjStr != null) {
                    jsonObjStr = CRUDService.trimMessage(jsonObjStr);
                    try {
                        JSONObject jsonObject = new JSONObject(jsonObjStr);
                        androidx.appcompat.app.AlertDialog.Builder alertDialog = new androidx.appcompat.app.AlertDialog.Builder(getContext());
                        alertDialog.setCancelable(false);
                        alertDialog.setTitle("Mensaje");

                        alertDialog.setMessage("La persona esta enrolada y activa en el proyecto con el contrato "
                                + jsonObject.getString("ContractNumber") + "\n¿Desea trasladarlo?");
                        alertDialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                flagMove = true;
                                dialog.dismiss();
                            }
                        });
                        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mListener != null) {
                                    mListener.onCancelActionAddPersonal();
                                }
                                dialog.dismiss();
                            }
                        });
                        alertDialog.create().show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
//                                    if (mListener != null) {
//                                        mListener.onCancelActionAddPersonal();
//                                    }
//                                    if (mListener != null) {
//                                        mListener.onApplyPersonalOtherContract(personal);
//                                    }

                break;
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

    class PositionAutoCompleteAdapterNew extends BaseAdapter implements Filterable {

        private final Context mContext;
        private List<Position> resultList = new ArrayList<Position>();

        PositionAutoCompleteAdapterNew(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public Position getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.simple_spinner_item_2, parent, false);
            }
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(getItem(position).Name);
            return convertView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        findPosition(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = positionList;
                        filterResults.count = positionList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        resultList = (List<Position>) results.values;
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }

        /**
         * Returns a search result for the given book title.
         */
        private void findPosition(String positionTitle) {
            sendPosition = 1;
            ContentValues values = new ContentValues();
            values.put("param", positionTitle);
            String paramsQuery = HttpRequest.makeParamsInUrl(values);
            CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_AUTOPOSITION_URL + companyId,
                    Request.Method.GET, "", paramsQuery, true);
        }
    }

}
