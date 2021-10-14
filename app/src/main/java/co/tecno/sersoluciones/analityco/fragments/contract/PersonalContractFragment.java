package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.JoinPersonalWizardActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.PersonalContract;
import co.tecno.sersoluciones.analityco.models.Position;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;
import co.tecno.sersoluciones.analityco.views.DelayAutoCompleteTextView;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class PersonalContractFragment extends Fragment implements DatePickerDialog.OnDateSetListener, TextWatcherAdapter.TextWatcherListener,
        ClearebleAutoCompleteTextView.Listener, RequestBroadcastReceiver.BroadcastListener {

    private Unbinder unbinder;
    private RequestBroadcastReceiver getJSONBroadcastReceiver;

    private OnAddPersonalContract mListener;
    private Date toDate;
    private Date fromDate;
    private String fromDateStr;
    private String toDateStr;
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
    @BindView(R.id.text_active)
    TextView textActive;
    @BindView(R.id.label_validity)
    TextView labelValidity;
    @BindView(R.id.layout_verification)
    TextInputLayout layoutVerification;
    @BindView(R.id.textError)
    public TextView textError;
    private String finishDate;
    private Date toDateContract;
    private Date fromDateContract;

    private static final String ARG_COMPANY_INFO_ID = "companyInfoId";
    private static final String ARG_CONTRACT_ID = "contractId";
    private static final String ARG_EDIT = "param2";
    private static final String ARG_PERSONAL_OBJ = "personal";
    private static final String ARG_START_DATE = "startdate";
    private static final String ARG_FINISH_DATE = "finishdate";
    private static final String ARG_CONTRACT_TYPE = "contractType";
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
    private Context instance;
    private String contractId;
    private Personal personal;
    private List<Position> positionList;
    private int sendPosition = 0;
    private String contractType;
    private String companyInfoId;

    public PersonalContractFragment() {

    }

    public static PersonalContractFragment newInstance(String companyInfoId, String contractId, String startdate, String finishdate, String contractType) {
        PersonalContractFragment fragment = new PersonalContractFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMPANY_INFO_ID, companyInfoId);
        args.putString(ARG_CONTRACT_ID, contractId);
        args.putBoolean(ARG_EDIT, false);
        args.putString(ARG_START_DATE, startdate);
        args.putString(ARG_FINISH_DATE, finishdate);
        args.putString(ARG_CONTRACT_TYPE, contractType);
        fragment.setArguments(args);
        return fragment;
    }

    public static PersonalContractFragment newInstanceEdit(String companyInfoId, Personal personal, String contractId, String startdate, String finishdate, String contractType) {
        PersonalContractFragment fragment = new PersonalContractFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COMPANY_INFO_ID, companyInfoId);
        args.putString(ARG_CONTRACT_ID, contractId);
        args.putBoolean(ARG_EDIT, true);
        args.putSerializable(ARG_PERSONAL_OBJ, personal);
        args.putString(ARG_START_DATE, startdate);
        args.putString(ARG_FINISH_DATE, finishdate);
        args.putString(ARG_CONTRACT_TYPE, contractType);
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
        positionList = new ArrayList<>();
        if (getArguments() != null) {
            editPersonal = getArguments().getBoolean(ARG_EDIT);
            companyInfoId = getArguments().getString(ARG_COMPANY_INFO_ID);
            contractId = getArguments().getString(ARG_CONTRACT_ID);
            String startDate = getArguments().getString(ARG_START_DATE);
            finishDate = getArguments().getString(ARG_FINISH_DATE);
            contractType = getArguments().getString(ARG_CONTRACT_TYPE);
            if (editPersonal) {
                personal = (Personal) getArguments().getSerializable(ARG_PERSONAL_OBJ);
            } else {
                card_view_detail.setVisibility(View.GONE);
            }
            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                if (startDate != null)
                    fromDateContract = format.parse(startDate);
                if (finishDate != null)
                    toDateContract = format.parse(finishDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        instance = getActivity();
        textActive.setVisibility(View.GONE);
        labelValidity.setVisibility(View.GONE);
        Calendar calendar = Calendar.getInstance();

        if (fromDateContract != null)
            fromDate = fromDateContract;
        else
            fromDate = calendar.getTime();

        if (toDateContract != null)
            toDate = toDateContract;
        else {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            toDate = calendar.getTime();
        }

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
            logW(new Gson().toJson(personal));
            layoutVerification.setVisibility(View.GONE);
            send.setVisibility(View.GONE);
            process = 1;
            fromDateStr = personal.StartDate;
            toDateStr = personal.FinishDate;
            positionString.setText(personal.Position);
            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                fromDate = format.parse(personal.StartDate);
                toDate = format.parse(personal.FinishDate);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                fromDateBtn.setText(dateFormat.format(fromDate));
                toDateBtn.setText(dateFormat.format(toDate));
                toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                identification.setText(personal.DocumentNumber);
                text_name.setText(personal.Name + " " + personal.LastName);
                text_sub_name.setText(personal.DocumentType + " " + personal.DocumentNumber);
                btnEdit.setVisibility(View.GONE);
                if (personal.Photo != null) {
                    String url = Constantes.URL_IMAGES + personal.Photo;

                    Picasso.get().load(url)
                            .resize(0, 250)
                            .placeholder(R.drawable.image_not_available)
                            .error(R.drawable.image_not_available)
                            .into(logo_imag);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (finishDate != null) {

            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                toDate = format.parse(finishDate);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                fromDateBtn.setText(dateFormat.format(new Date()));
                toDateBtn.setText(dateFormat.format(toDate));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        updateDatePicker(fromDate, toDate);
        getJSONBroadcastReceiver = new RequestBroadcastReceiver(this);
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
        if (toDateContract != null)
            toDatePickerDialog.getDatePicker().setMaxDate(toDateContract.getTime());
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
        } else*/
        if (fromDate.getTime() > toDate.getTime()) {
            toDateBtn.setError("La fecha debe ser mayor a la de inicio");
            toDateBtn.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError(getString(R.string.error_field_required));
            focusView = tvToDateError;
            cancel = true;
        }/* else if (spinnerRole.getSelectedItemPosition() <= 0) {
            cancel = true;
            focusView = spinnerRole;
        }*/ else if (process != 1) {
            cancel = true;
            identification.setError("Personal es obligatorio");
            focusView = identification;
        } else if ((contractType.equals("AD") || contractType.equals("AS") || contractType.equals("CO")) && TextUtils.isEmpty(positionString.getText().toString())) {
            cancel = true;
            positionString.setError("Cargo para este tipo de contrato es obligatorio");
            focusView = positionString;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            fromDateStr = format.format(fromDate);
            toDateStr = format.format(toDate);
            if (editPersonal) {
                PersonalContract newPersonal = new PersonalContract(fromDateStr, toDateStr, personal.PersonalCompanyInfoId, positionString.getText().toString());
                Gson gson = new Gson();
                String json = gson.toJson(newPersonal);
                logW(json);
                CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_CONTRACTS_URL + contractId + "/PersonalInfo/",
                        Request.Method.POST, json, "", false);
            } else {
                PersonalContract newPersonal = new PersonalContract(fromDateStr, toDateStr, personal.PersonalCompanyInfoId, positionString.getText().toString());
                Gson gson = new Gson();
                String json = gson.toJson(newPersonal);
                logW(json);
                CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_CONTRACTS_URL + contractId + "/PersonalInfo/",
                        Request.Method.POST, json, "", false);
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        int code = (Integer) view.getTag();

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

    @SuppressLint("SetTextI18n")
    private void fillFormPersonNew(String json) {
        personal = new Gson().fromJson(json,
                new TypeToken<Personal>() {
                }.getType());
        if (personal != null) {
            card_view_detail.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.GONE);
            text_name.setText(personal.Name + " " + personal.LastName);
            text_sub_name.setText(personal.JobName);
            text_sub_name.setTypeface(text_sub_name.getTypeface(), Typeface.BOLD);
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

        void onApplyActionAddPersonal();
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
                if (editPersonal) {
                    if (mListener != null) {
                        mListener.onApplyActionAddPersonal();
                    }
                } else if (process == 0) {
                    sendPosition = 0;
                    fillFormPersonNew(jsonObjStr);
                    process = 1;
                } else {
                    if (mListener != null) {
                        Log.e("Creo", "creo satisfactoriamet personal");
                        mListener.onApplyActionAddPersonal();
                    }
                }
                break;
            case Constantes.REQUEST_NOT_FOUND:
                if ((sendPosition == 0 || !editPersonal) && url.equals(Constantes.GETID_URL + identification.getText().toString() + "/" + contractId)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle("Persona no encontrada")
                            .setCancelable(false)
                            .setPositiveButton("Crear", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(getActivity(), JoinPersonalWizardActivity.class);
                                    intent.putExtra("contractId", contractId);
                                    intent.putExtra("companyInfoId", companyInfoId);
                                    intent.putExtra("contractType", contractType);
                                    intent.putExtra("docNumber", identification.getText().toString());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        startActivity(intent,
                                                ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                    } else {
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mListener != null) {
                                        mListener.onCancelActionAddPersonal();
                                    }
                                }
                            })
                            .setMessage("*Crear antes de vincular");
                    builder.create().show();
                } else {
                    sendPosition = 0;
                }
                break;
            case Constantes.SEND_REQUEST:
                //msgSuccess();
                if (editPersonal)
                    updateListEdit(jsonObjStr);
                else
                    updateList(jsonObjStr);
                break;
            case Constantes.BAD_REQUEST:
                if (url.equals(Constantes.LIST_CONTRACTS_URL + contractId + "/PersonalInfo/")) {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                            .setTitle("No se asocio Personal")
                            .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PersonalContract newPersonal = new PersonalContract(fromDateStr, toDateStr, personal.PersonalCompanyInfoId, positionString.getText().toString());
                                    Gson gson = new Gson();
                                    String json = gson.toJson(newPersonal);
                                    CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_CONTRACTS_URL + contractId + "/MovePersonalInfo/",
                                            Request.Method.POST, json, "", false);
                                    if (mListener != null) {
                                        Log.e("Creo", "creo satisfactoriamet personal");
                                        mListener.onApplyActionAddPersonal();
                                    }
                                }
                            })
                            .setNegativeButton("NO", null)
                            .setMessage("Personal ya existente en otro contrato de este mismo proyecto, ¿Desea mover de contrato al persoanl?.");
                    builder.create().show();
                } else if ((!editPersonal) && url.equals(Constantes.GETID_URL + identification.getText().toString() + "/" + contractId)) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity())
                            .setTitle("Persona no encontrada")
                            .setCancelable(false)
                            .setPositiveButton("Crear", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(getActivity(), JoinPersonalWizardActivity.class);
                                    intent.putExtra("contractId", contractId);
                                    intent.putExtra("companyInfoId", companyInfoId);
                                    intent.putExtra("contractType", contractType);
                                    intent.putExtra("docNumber", identification.getText().toString());
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        startActivity(intent,
                                                ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
                                    } else {
                                        startActivity(intent);
                                    }

                                }
                            })
                            .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (mListener != null) {
                                        mListener.onCancelActionAddPersonal();
                                    }
                                }
                            })
                            .setMessage("*Crear antes de vincular");
                    builder1.create().show();
                }
                break;
            case Constantes.TIME_OUT_REQUEST:
                //MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
        }
    }

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
            CrudIntentService.startRequestCRUD(getActivity(), Constantes.LIST_AUTOPOSITION_URL,
                    Request.Method.GET, "", paramsQuery, true);
        }
    }

}
