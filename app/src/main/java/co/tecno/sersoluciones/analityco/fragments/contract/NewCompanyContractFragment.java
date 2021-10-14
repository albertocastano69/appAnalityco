package co.tecno.sersoluciones.analityco.fragments.contract;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.tokenautocomplete.FilteredArrayAdapter;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.BranchOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.EconomicActivity;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getBitmapFromUri;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getRealUriImage;

/**
 * Created by Ser Soluciones SAS on 28/09/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class NewCompanyContractFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        AdapterView.OnItemSelectedListener, BranchOfficesRecyclerView.OnBranchOfficeInteractionListener,
        DatePickerDialog.OnDateSetListener, TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener {

    @BindView(R.id.icon_logo)
    MaterialIconView iconLogoView;
    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.fab_search)
    FloatingActionButton serachCompany;
    @BindView(R.id.spinner_doc_type)
    Spinner mDocTypeSpinner;
    @BindView(R.id.spinner_contract_type)
    Spinner mRoleSpinner;
    @BindView(R.id.tvDocTypeError)
    TextView tvDocTypeError;

    @BindView(R.id.number_verification)
    EditText mNumberVerifyView;
    @BindView(R.id.layout_verification)
    TextInputLayout layoutVerification;
    @BindView(R.id.edtt_name)
    EditText mNameView;
    @BindView(R.id.user_doc_number)
    EditText mDocNumberView;
    @BindView(R.id.card_view_main)
    CardView cardViewMain;
    @BindView(R.id.btn_from_date)
    Button fromDateBtn;
    @BindView(R.id.tvToDateError)
    TextView tvToDateError;
    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    @BindView(R.id.btn_to_date)
    Button toDateBtn;
    @BindView(R.id.create_button)
    Button sendCompany;
    @BindView(R.id.cancel_button)
    Button cancelCompany;


    private static final int REQUEST_IMAGE_SELECTOR = 199;
    private static final String ARG_PARAM1 = "param1";

    private Context instance;
    private boolean isNit;

    private BranchOfficesRecyclerView adapter;
    private ArrayList<BranchOffice> branchOffices;

    private String fromDateStr;
    private String toDateStr;
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;


    private int numberMaxNit;
    private ArrayList<BranchOffice> posBrachRemove;
    private Date fromDate;
    private Date toDate;

    private Uri mImageUri;


    private RequestBroadcastReceiver requestBroadcastReceiver;
    private String[] docTypeArray;
    private String[] contractTypeArray;
    private boolean createNewCompany;
    private User user;
    private String companyId;
    private String idNewCompany;
    private CompanyProject newComp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_PARAM1);
            companyId = user.CompanyId;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.step2_company_contract, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        createNewCompany = false;
        docTypeArray = new String[]{"Tipo", "CC", "NIT"};
        contractTypeArray = new String[]{"Tipo de empleado", "Contratista", "Subcontratista"};
        init();
        serachCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDocNumberView.getText().toString().length() < 9 || mNumberVerifyView.getText().toString().isEmpty()) {

                } else {
                    ContentValues values = new ContentValues();
                    values.put(Constantes.KEY_ADMIN, true);
                    String paramsQuery = HttpRequest.makeParamsInUrl(values);

                    String url = Constantes.LIST_COMPANIES_URL + companyId + "/JoinCompanies/?docType=" + docTypeArray[mDocTypeSpinner.getSelectedItemPosition()] + "&nit=" + mDocNumberView.getText().toString() + "-" + mNumberVerifyView.getText().toString();
                    Log.e("url", url);
                    createNewCompany = false;
                    CrudIntentService.startRequestCRUD(instance, url,
                            Request.Method.GET, "", "", false);
                }
            }
        });
        sendCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sumbitRequest()) {
                    try {
                        if (fromDateStr != null && !fromDateStr.isEmpty() && toDateStr != null && !toDateStr.isEmpty()) {
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            fromDateStr = format.format(fromDate);
                            toDateStr = format.format(toDate);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    newComp = new CompanyProject(fromDateStr, toDateStr, docTypeArray[mDocTypeSpinner.getSelectedItemPosition()],
                            mDocNumberView.getText().toString() + "-" + mNumberVerifyView.getText().toString(), mNameView.getText().toString(),
                            contractTypeArray[mRoleSpinner.getSelectedItemPosition()]
                    );

                    /*if (createNewCompany) {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("Name", newComp.Name);
                            json.put("DocumentType", newComp.DocumentType);
                            json.put("DocumentNumber", newComp.DocumentNumber);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                      CrudIntentService.startRequestCRUD(instance, Constantes.LIST_COMPANIES_URL + companyId + "/JoinCompanies/",
                                Request.Method.POST, json.toString(), "", false);
                    }else {
                        applyForm(newComp);
                    }*/
                }
            }
        });
        cancelCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelForm();
            }
        });
        return view;
    }

    private void cancelForm() {

        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_CANCELED, null
        );
    }

    private void applyForm(CompanyProject newComp) {
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                new Intent().putExtra("newComp", newComp).putExtra("id", idNewCompany)
        );
    }

    private void init() {
        BranchOfficesRecyclerView.OnBranchOfficeInteractionListener mListener = this;
        instance = getActivity();

        mImageUri = null;

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        populateCityAutoComplete();

        fabRemove.setVisibility(View.GONE);
        iconLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPhotoSelectionIntent();
            }
        });

        branchOffices = new ArrayList<>();
        branchOffices.add(new BranchOffice());
        posBrachRemove = new ArrayList<>();

        adapter = new BranchOfficesRecyclerView(instance, mListener);


        mDocTypeSpinner.setAdapter(new CustomArrayAdapter(instance, docTypeArray));
        mDocTypeSpinner.setOnItemSelectedListener(this);
        mRoleSpinner.setAdapter(new CustomArrayAdapter(instance, contractTypeArray));

        mNumberVerifyView.addTextChangedListener(new TextWatcherAdapter(mNumberVerifyView, this));
        mDocNumberView.addTextChangedListener(new TextWatcherAdapter(mDocNumberView, this));
        mDocNumberView.setEnabled(false);

        ArrayAdapter<String> adapterDomain = new ArrayAdapter<>(instance, R.layout.simple_spinner_item, new ArrayList<String>());

        char[] splitChar = {',', ';', ' '};

        ArrayList<EconomicActivity> economicActivities = new ArrayList<>();
        @SuppressLint("Recycle")
        Cursor cursor = instance.getContentResolver().query(Constantes.CONTENT_ECONOMIC_URI, null, null, null,
                DBHelper.ECONOMIC_TABLE_COLUMN_NAME);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            economicActivities.add(new EconomicActivity(cursor.getString(cursor.getColumnIndex(DBHelper.ECONOMIC_TABLE_COLUMN_CODE)),
                    cursor.getString(cursor.getColumnIndex(DBHelper.ECONOMIC_TABLE_COLUMN_NAME))));
            cursor.moveToNext();
        }

        log("economicActivities size : " + economicActivities.size());
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
                TextView textView2 = convertView.findViewById(android.R.id.text2);
                textView2.setText(economicActivity.Name);
                return convertView;
            }
        };

        Calendar calendar = Calendar.getInstance();
        fromDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        toDate = calendar.getTime();

        String myFormat = "dd/MMM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
        fromDateStr = sdf.format(fromDate);
        fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        fromDateBtn.setText(fromDateStr);
        toDateStr = "";

        numberMaxNit = 5;
        isNit = false;

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mDocTypeSpinner.getSelectedItemPosition() == 0) {
                    //&& fragmnetNewCompany.getVisibility() == View.VISIBLE) {
                    tvDocTypeError.setError("");
                    tvDocTypeError.setError("Requerido");
                    tvDocTypeError.requestFocus();
                }
            }
        }, 500);

        updateDatePicker(fromDate, toDate);
    }


    private void populateCityAutoComplete() {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(instance, R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.DANE_CITY_TABLE_COLUMN_NAME, DBHelper.DANE_CITY_TABLE_COLUMN_STATE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        mAdapterCust.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorCity(str);
            }
        });

        mAdapterCust.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
                return cur.getString(index);
            }
        });
    }

    private Cursor getCursorCity(CharSequence str) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_NAME + " LIKE ? )";
        String[] selectArgs = {"%" + str + "%"};
        return instance.getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
    }

    private void updateDatePicker(Date fromDate, Date toDate) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);

        Calendar cT = Calendar.getInstance();
        cT.setTime(toDate);
        fromDatePickerDialog = new DatePickerDialog(instance, this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        toDatePickerDialog = new DatePickerDialog(instance, this, cT.get(Calendar.YEAR),
                cT.get(Calendar.MONTH), cT.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            switch (parent.getId()) {
                case R.id.spinner_doc_type:
                    String text = parent.getItemAtPosition(position).toString();
                    tvDocTypeError.setText(text);
                    tvDocTypeError.setVisibility(View.GONE);
                    tvDocTypeError.setError(null);
                    if (text.equals("CC")) {
                        setEditTextMaxLength(mDocNumberView, 10);
                        mNumberVerifyView.setVisibility(View.GONE);
                        layoutVerification.setVisibility(View.GONE);
                        //viewSeparation.setVisibility(View.GONE);
                        numberMaxNit = 5;
                        isNit = false;
                    } else {
                        setEditTextMaxLength(mDocNumberView, 9);
                        layoutVerification.setVisibility(View.VISIBLE);
                        mNumberVerifyView.setVisibility(View.VISIBLE);
                        //viewSeparation.setVisibility(View.VISIBLE);
                        numberMaxNit = 9;
                        isNit = true;
                    }
                    mNumberVerifyView.setText("");
                    mDocNumberView.setText("");
                    mNameView.setText("");
                    mDocNumberView.setEnabled(true);
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onListOrderInteraction(final int position, boolean remove) {

        BranchOffice branchOffice = branchOffices.get(position);
        if (remove)
            posBrachRemove.add(branchOffice);
        else
            posBrachRemove.remove(branchOffice);
    }

    @OnClick(R.id.btn_to_date)
    public void toDateDialog() {
        toDatePickerDialog.getDatePicker().setTag(R.id.btn_to_date);
        toDatePickerDialog.show();
    }

    @OnClick(R.id.btn_from_date)
    public void fromDateDialog() {
        fromDatePickerDialog.getDatePicker().setTag(R.id.btn_from_date);
        fromDatePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        int code = (Integer) view.getTag();
        //logE("CODE: " + code);
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
        switch (view.getId()) {
            case R.id.user_doc_number:
                if (mDocNumberView.isFocused()) {
                    if (!isDocNumberValid(text)) {
                        mDocNumberView.setError("El número de documento es muy corto");
                        mDocNumberView.requestFocus();
                    } else {
                        if (text.length() == 9) {
                            mNumberVerifyView.setEnabled(true);
                            mNumberVerifyView.setError(getString(R.string.error_field_required));
                            mNumberVerifyView.requestFocus();
                            mNumberVerifyView.setText("");
                            //validateDigitVerify();
                        } else {
                            mNumberVerifyView.setEnabled(false);
                        }
                    }
                }
                break;
            case R.id.number_verification:
                logW("mNumberVerifyView: " + text);
                if (mNumberVerifyView.isFocused()) {
                    if (!validateDigitVerify(text)) {
                        mNumberVerifyView.setError("Digito de verificación incorrecto");
                        mNumberVerifyView.requestFocus();
                    } else {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("txtNIT", mDocNumberView.getText().toString() + mNumberVerifyView.getText().toString());
                        String url = Constantes.RUES_URL;
                        CrudIntentService.startActionFormData(instance, url,
                                Request.Method.POST, params);
                    }
                }
                break;
        }
    }

    private boolean isDocNumberValid(String text) {
        return text.length() >= numberMaxNit;
    }

    private boolean validateDigitVerify(String digitVerify) {
        ArrayList<Integer> documentNumberTemp = new ArrayList<>();
        String nit = mDocNumberView.getText().toString();
        if (nit.isEmpty() || nit.length() != 9)
            return false;
        final int len = nit.length();
        for (int i = 0; i < len; i++) {
            documentNumberTemp.add(Character.getNumericValue(nit.charAt(i)));
        }
        if (len == 9) {
            int v = 41 * documentNumberTemp.get(0);
            v += 37 * documentNumberTemp.get(1);
            v += 29 * documentNumberTemp.get(2);
            v += 23 * documentNumberTemp.get(3);
            v += 19 * documentNumberTemp.get(4);
            v += 17 * documentNumberTemp.get(5);
            v += 13 * documentNumberTemp.get(6);
            v += 7 * documentNumberTemp.get(7);
            v += 3 * documentNumberTemp.get(8);
            v = v % 11;
            if (v >= 2) v = 11 - v;

            return digitVerify.equals(String.valueOf(v));
        }
        return false;
    }

    @Override
    public void didClearText(View view) {
    }


    //A system-based view to select photos.
    private void dispatchPhotoSelectionIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECTOR);
    }

    /**
     * Metodo que recibe la respuesta cuando la imagen es tomada
     *
     * @param requestCode param1
     * @param resultCode  param2
     * @param data        param3
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == REQUEST_IMAGE_SELECTOR) {
                    Uri imageUri = data.getData();
                    if (imageUri == null) return;
                    Bitmap bitmap = getBitmapFromUri(Objects.requireNonNull(getActivity()).getApplicationContext(), imageUri);
                    mImageUri = getRealUriImage(getActivity().getApplicationContext(), bitmap, true);

                    iconLogoView.setImageURI(mImageUri);
                    fabRemove.setVisibility(View.VISIBLE);

                }
                break;
        }
    }

    @OnClick(R.id.fab_remove)
    public void removeLogo() {
        Drawable drawable = MaterialDrawableBuilder.with(instance) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
                .setColor(Color.GRAY)
                .build();
        iconLogoView.setImageDrawable(drawable);
        fabRemove.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        LocalBroadcastManager.getInstance(instance).registerReceiver(requestBroadcastReceiver,
                intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(requestBroadcastReceiver);
    }

    private void setEditTextMaxLength(EditText editText, int length) {
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        editText.setFilters(FilterArray);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unbinder.unbind();
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        Log.e("option", option + " " + jsonObjStr);
        switch (option) {
            case Constantes.SEND_REQUEST:

                break;
            case Constantes.BAD_REQUEST:
                //MetodosPublicos.alertDialog(instance, "Fallo al actualizar la base de datos");
                break;
            case Constantes.TIME_OUT_REQUEST:
                MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                break;
            case Constantes.SUCCESS_REQUEST:

                try {
                    JSONObject dates = new JSONObject(jsonObjStr);

                    if (createNewCompany) {
                        idNewCompany = dates.getString("Id");
                        //newComp.setId(idNewCompany);
                        createNewCompany = false;
                        applyForm(newComp);

                    } else {
                        mNameView.setText(dates.getString("Name"));
                        idNewCompany = dates.getString("Id");
                        //newComp.setId(dates.getString("Id"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
            case Constantes.REQUEST_NOT_FOUND:
                mNameView.setEnabled(true);
                createNewCompany = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Compañia no existe")
                        .setCancelable(false)
                        .setPositiveButton("Si", null)
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                cancelForm();
                            }
                        })
                        .setMessage("Desea crear la empresa con NIT:" + mDocNumberView.getText().toString());
                builder.create().show();
                //MetodosPublicos.alertDialog(instance, "Comañia no existe, al guardar la información se creara");
                break;
        }
    }

    private boolean sumbitRequest() {
        // Reset errors.
        mNameView.setError(null);
        mDocNumberView.setError(null);

        byte geofenceLimit = 0;

        String name = mNameView.getText().toString();
        String docNumber = mDocNumberView.getText().toString();
        String numVerify = mNumberVerifyView.getText().toString();

        TextView mDocTypeView = mDocTypeSpinner.getSelectedView().findViewById(android.R.id.text1);
        mDocTypeView.setError(null);
        String docType = tvDocTypeError.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(docType)) {
            mDocTypeView.setError("Requerido");
            mDocTypeView.requestFocus();

            tvDocTypeError.setVisibility(View.VISIBLE);
            tvDocTypeError.setError("Requerido");
            focusView = tvDocTypeError;
            cancel = true;
        } else if (TextUtils.isEmpty(docNumber)) {
            mDocNumberView.setError(getString(R.string.error_field_required));
            focusView = mDocNumberView;
            cancel = true;
        } else if (TextUtils.isEmpty(numVerify) && isNit) {
            mNumberVerifyView.setError(getString(R.string.error_field_required));
            focusView = mNumberVerifyView;
            cancel = true;
        } else if (isNit && !validateDigitVerify(numVerify)) {
            mNumberVerifyView.setError("Digito de verificación incorrecto");
            focusView = mNumberVerifyView;
            cancel = true;
        } else if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (mRoleSpinner.getSelectedItemPosition() == 0) {
            focusView = mRoleSpinner;
            cancel = true;
        } else if (fromDateStr.isEmpty()) {
            fromDateBtn.setError(getString(R.string.error_field_required));
            fromDateBtn.requestFocus();

            tvFromDateError.setVisibility(View.VISIBLE);
            tvFromDateError.setError(getString(R.string.error_field_required));
            focusView = tvFromDateError;
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
            tvToDateError.setError("La fecha debe ser mayor a la de inicio");
            focusView = tvToDateError;
            cancel = true;
        }

        adapter.update();
        adapter.swap(branchOffices);

        if (cancel) {


            focusView.requestFocus();
            return false;
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (isNit)
                docNumber = docNumber + numVerify;

            Gson gson = new Gson();

            return true;
        }
    }

}