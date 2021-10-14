package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.legacy.widget.Space;
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
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
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
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.BranchOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getBitmapFromUri;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getRealUriImage;
import static co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.processBarcode;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class AddCompanyProjectFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
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
    @BindView(R.id.spinner_role)
    EditText mRoleSpinner;
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
    @BindView(R.id.companyIdentification)
    LinearLayout companyIdentification;
    @BindView(R.id.spaceIdentification)
    Space spaceIdentification;
    @BindView(R.id.identificationEdit)
    TextView identificationEdit;
    @BindView(R.id.fab_scan)
    FloatingActionButton fabScan;
    @BindView(R.id.scanBtnLayout)
    LinearLayout scanBtnLayout;


    private static final int REQUEST_IMAGE_SELECTOR = 199;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

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
    private OnAddCompanyProject mListener;
    private Uri mImageUri;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private String[] docTypeArray;
    private boolean createNewCompany;
    private boolean addNewCompany;
    private String companyId;
    private String projectId;
    private String idNewCompany;
    private CompanyProject newComp;
    private int contextForm;
    private boolean logoUpload = false;
    private boolean scanccok;

    public static AddCompanyProjectFragment newInstanceActivity(String companId, String projectId) {
        int ContextFrom = 0;
        AddCompanyProjectFragment fragment = new AddCompanyProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, companId);
        args.putString(ARG_PARAM2, projectId);
        args.putInt(ARG_PARAM3, ContextFrom);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddCompanyProjectFragment newInstanceActivityEdit(String companId, String projectId, CompanyProject companyProject) {
        int ContextFrom = 1;
        AddCompanyProjectFragment fragment = new AddCompanyProjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, companId);
        args.putString(ARG_PARAM2, projectId);
        args.putInt(ARG_PARAM3, ContextFrom);
        args.putSerializable(ARG_PARAM4, companyProject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String jsonGeofence = "";
        if (getArguments() != null) {
            companyId = getArguments().getString(ARG_PARAM1);
            projectId = getArguments().getString(ARG_PARAM2);
            contextForm = getArguments().getInt(ARG_PARAM3);
            if (contextForm == 1) {
                newComp = (CompanyProject) getArguments().getSerializable(ARG_PARAM4);
                if (newComp.CompanyInfoId == null) newComp.CompanyInfoId = companyId;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.step2_company_projet, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        createNewCompany = false;
        addNewCompany = false;
        docTypeArray = new String[]{"Tipo", "CC", "NIT"};

        if (contextForm == 1) {
            initEdit();
        } else {
            init();
        }
        MyPreferences preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        serachCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((mDocNumberView.getText().toString().length() >= 9 && !mNumberVerifyView.getText().toString().isEmpty()) || !isNit) {
                    ContentValues values = new ContentValues();
//                    values.put("companyInfoId", companyId);
                    if (isNit) {
                        values.put("name", mDocNumberView.getText().toString() + "-" + mNumberVerifyView.getText().toString());
                        values.put("companyInfoId", companyId);
                    } else {
                        values.put("name", mDocNumberView.getText().toString());
                        values.put("companyInfoId", companyId);
                    }

                    String paramsQuery = HttpRequest.makeParamsInUrl(values);
                    String url = Constantes.LIST_COMPANIES_BY_DOCNUM;
                    Log.e("url", url);
                    createNewCompany = false;
                    CrudIntentService.startRequestCRUD(instance, url,
                            Request.Method.GET, "", paramsQuery, false);
                }
            }
        });
        sendCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean send = false;
                if (contextForm == 0) {
                    send = sumbitRequest();
                } else {
                    send = sumbitRequestEdit();
                }
                if (send) {
                    try {
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        fromDateStr = format.format(fromDate);
                        toDateStr = format.format(toDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                Log.e("dates:",newComp.Name+newComp.getId()+newComp.Rol);
                    if (contextForm == 0) {
                        newComp = new CompanyProject(fromDateStr, toDateStr, docTypeArray[mDocTypeSpinner.getSelectedItemPosition()],
                                mDocNumberView.getText().toString() + "-" + mNumberVerifyView.getText().toString(), mNameView.getText().toString(),
                                mRoleSpinner.getText().toString()
                        );
                        //newComp.CompanyInfoId=user.CompanyId;
                        if (newComp.CompanyInfoId == null && newComp.CompanyId != null)
                            newComp.CompanyInfoId = newComp.CompanyId;
                    } else {
                      /*  CompanyProject updateCmpany = new CompanyProject(fromDateStr, toDateStr, newComp.DocumentType, newComp.DocumentNumber,
                                mNameView.getText().toString(), mRoleSpinner.getText().toString()
                        );
                        idNewCompany = newComp.CompanyId;
                        if(updateCmpany.CompanyInfoId==null && updateCmpany.CompanyId!=null)updateCmpany.CompanyInfoId=idNewCompany;
                        newComp = updateCmpany;*/
                        idNewCompany = newComp.CompanyId;
                        newComp.StartDate = fromDateStr;
                        newComp.FinishDate = toDateStr;
                        newComp.Name = mNameView.getText().toString();
                        newComp.Rol = mRoleSpinner.getText().toString();
                    }


                    if (createNewCompany) {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("Name", newComp.Name);
                            json.put("DocumentType", newComp.DocumentType);
                            json.put("DocumentNumber", newComp.DocumentNumber);
                            json.put("Rol", "Empresa Asociada");
                            json.put("CompanyInfoParentId", companyId);
                            json.put("CompanyInfoId", newComp.CompanyInfoId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                   /* ContentValues values = new ContentValues();
                    values.put(Constantes.KEY_ADMIN, true);
                    String paramsQuery = HttpRequest.makeParamsInUrl(values);*/

                        CrudIntentService.startRequestCRUD(instance,
                                Constantes.CREATE_EMPLOYER_URL, Request.Method.POST, json.toString(), "", false);
                    }
                    if (!createNewCompany) {
                        applyForm(newComp);
                    }
                }

            }
        });
        cancelCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelForm();
            }
        });
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
            }
        });
        return view;
    }

    private void cancelForm() {
        if (mListener != null) {
            mListener.onCancelActionCompany();
        }
    }

    private void applyForm(CompanyProject newComp) {
        addNewCompany = true;
        if (idNewCompany != null)
            newComp.setId(idNewCompany);
        if (mImageUri != null && !mImageUri.toString().isEmpty())
            newComp.Photo = mImageUri.toString();
        // Gson gson = new Gson();
        //String json = gson.toJson(newComp);
        JSONObject json = new JSONObject();
        try {
            json.put("StartDate", newComp.StartDate);
            json.put("FinishDate", newComp.FinishDate);
            json.put("Rol", newComp.Rol);
            json.put("CompanyInfoId", newComp.CompanyInfoId);
            json.put("ProjectId ", projectId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CrudIntentService.startRequestCRUD(instance, Constantes.LIST_PROJECTS_URL + projectId + "/JoinCompanies/",
                Request.Method.POST, json.toString(), "", false);
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

        mNumberVerifyView.addTextChangedListener(new TextWatcherAdapter(mNumberVerifyView, this));
        mDocNumberView.addTextChangedListener(new TextWatcherAdapter(mDocNumberView, this));
        mDocNumberView.setEnabled(false);

        ArrayAdapter<String> adapterDomain = new ArrayAdapter<>(instance, R.layout.simple_spinner_item, new ArrayList<String>());

        char[] splitChar = {',', ';', ' '};

        @SuppressLint("Recycle")
        Cursor cursor = instance.getContentResolver().query(Constantes.CONTENT_ECONOMIC_URI, null, null, null,
                DBHelper.ECONOMIC_TABLE_COLUMN_NAME);
        cursor.moveToFirst();

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

    private void initEdit() {
        BranchOfficesRecyclerView.OnBranchOfficeInteractionListener mListener = this;
        instance = getActivity();

        mImageUri = null;
        fromDateBtn.setEnabled(false);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);

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

        mRoleSpinner.setText(newComp.Rol);

        mNameView.setEnabled(true);
        // mDocNumberView.setEnabled(true);
        mNameView.setText(newComp.Name);
        //mDocNumberView.setText(newComp.DocumentNumber);
        companyIdentification.setVisibility(View.GONE);
        identificationEdit.setVisibility(View.VISIBLE);
        identificationEdit.setText(newComp.DocumentType + ": " + newComp.DocumentNumber);
        //jsonGeofence;
        try {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            fromDate = format.parse(newComp.StartDate);
            toDate = format.parse(newComp.FinishDate);
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            fromDateStr = dateFormat.format(fromDate);
            toDateStr = dateFormat.format(toDate);
            fromDateBtn.setText(fromDateStr);
            toDateBtn.setText(toDateStr);
            toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        updateDatePicker(fromDate, toDate);
        mNameView.setEnabled(false);
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

    public interface OnAddCompanyProject {
        void onCancelActionCompany();

        void onApplyActionCompany();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddCompanyProject) {
            mListener = (OnAddCompanyProject) context;
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
                    scanBtnLayout.setVisibility(View.VISIBLE);
                    if (text.equals("CC")) {
                        setEditTextMaxLength(mDocNumberView, 10);
                        mNumberVerifyView.setVisibility(View.GONE);
                        layoutVerification.setVisibility(View.GONE);
                        //viewSeparation.setVisibility(View.GONE);
                        numberMaxNit = 5;
                        isNit = false;
                        mDocNumberView.setEnabled(false);
                        mNameView.setEnabled(false);
                        if (!scanccok) {
                            startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
                        }
                    } else {
                        scanccok = false;
                        setEditTextMaxLength(mDocNumberView, 9);
                        layoutVerification.setVisibility(View.VISIBLE);
                        mNumberVerifyView.setVisibility(View.VISIBLE);
                        //viewSeparation.setVisibility(View.VISIBLE);
                        scanBtnLayout.setVisibility(View.GONE);
                        numberMaxNit = 9;
                        isNit = true;
                        mDocNumberView.setEnabled(true);
                        mNameView.setEnabled(false);
                    }
                    mNumberVerifyView.setText("");
                    mDocNumberView.setText("");
                    mNameView.setText("");
                    // mDocNumberView.setEnabled(true);
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
//                        HashMap<String, String> params = new HashMap<>();
//                        params.put("txtNIT", mDocNumberView.getText().toString() + mNumberVerifyView.getText().toString());
//                        String url = Constantes.RUES_URL;
//                        CrudIntentService.startActionFormData(instance, url,
//                                Request.Method.POST, params);
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
        Log.e("onActivityResult", "requestCode:" + requestCode + "resultCode" + resultCode);
        if (resultCode == BarcodeDecodeSerActivity.SUCCESS) {
            if (data.hasExtra("barcode")) {
                String barcodeRes = data.getStringExtra("barcode");
                int barcodeType = data.getIntExtra("barcode-type", 0);
                switch (barcodeType) {
                    case Barcode.QR_CODE:

                        break;
                    case Barcode.PDF417:
                        processBarcodePDF417(barcodeRes);
                        break;
                }

            }
        } else {
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
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
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
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        Log.e("option", option + " " + jsonObjStr);
        switch (option) {
            case Constantes.SEND_REQUEST:

                break;
            case Constantes.TIME_OUT_REQUEST:
                MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                if (logoUpload) {
                    if (mListener != null) {
                        mListener.onApplyActionCompany();
                    }
                } else {
                    try {
                        String name = new JSONObject(jsonObjStr).getJSONArray("rows").getJSONObject(0).getString("razon_social");
                        mNameView.setText(name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constantes.SUCCESS_REQUEST:

                try {
                    JSONObject dates = new JSONObject(jsonObjStr);

                    if (createNewCompany) {
                        idNewCompany = dates.getString("Id");
                        //newComp.setId(idNewCompany);
                        Log.e("id_comp", idNewCompany);
                        createNewCompany = false;
                        applyForm(newComp);
                        //updateImage();

                    } else if (addNewCompany) {
                        updateImage();
                           /* if (mListener != null) {
                                mListener.onApplyActionCompany();
                            }*/
                    } else {
                        idNewCompany = dates.getString("Id");
                        mNameView.setText(dates.getString("Name"));
                        mRoleSpinner.setText(dates.getString("Rol"));
                        dates.getString("Logo");
                        String urlImage = Constantes.URL_IMAGES + dates.getString("Logo");
                        Picasso.get().load(urlImage)
                                .resize(0, 250)
                                .placeholder(R.drawable.image_not_available)
                                .error(R.drawable.image_not_available)
                                .into(iconLogoView);
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
                        .setPositiveButton("Si", (dialogInterface, i) -> {
                            createNewCompany = true;
                            HashMap<String, String> params = new HashMap<>();
                            params.put("txtNIT", mDocNumberView.getText().toString() + mNumberVerifyView.getText().toString());
                            CrudIntentService.startActionFormData(instance, Constantes.RUES_URL,
                                    Request.Method.POST, params);
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> {
                            if (mListener != null) {
                                mListener.onCancelActionCompany();
                            }
                        })
                        .setMessage("Desea crear la empresa con NIT:" + mDocNumberView.getText().toString());
                builder.create().show();
                break;
            case Constantes.SERVER_ERROR:
            case Constantes.BAD_REQUEST:
                if (url.equals(Constantes.RUES_URL)) enabledFields();
                break;
        }
    }

    private void updateImage() {

        if (mImageUri != null && !mImageUri.toString().isEmpty()) {
            HashMap<String, String> params = new HashMap<>();
            params.put("file", mImageUri.toString());
            logoUpload = true;
            String url = Constantes.UPDATE_LOGO_COMPANY_URL + idNewCompany;
            CrudIntentService.startActionFormData(getActivity(), url,
                    Request.Method.PUT, params);
            mImageUri = null;
        } else {
            if (mListener != null) {
                mListener.onApplyActionCompany();
            }
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
        } else if (TextUtils.isEmpty(mRoleSpinner.getText().toString())) {
            mRoleSpinner.setError(getString(R.string.error_field_required));
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
            return true;
        }
    }

    private boolean sumbitRequestEdit() {
        // Reset errors.

        byte geofenceLimit = 0;

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mRoleSpinner.getText().toString())) {
            mRoleSpinner.setError(getString(R.string.error_field_required));
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
            if (!mImageUri.toString().isEmpty())
                newComp.Photo = mImageUri.toString();
            return true;
        }
    }

    private void processBarcodePDF417(String barcodeRes) {
        String processCode = processBarcode(barcodeRes);
        logE(barcodeRes);
        if (!processCode.isEmpty()) {
            scanccok = true;
            DecodeBarcode.InfoUser infoUser = new Gson().fromJson(processCode, DecodeBarcode.InfoUser.class);
            logW(new Gson().toJson(infoUser));
            mDocNumberView.setText(infoUser.dni + "");
            mNameView.setText(infoUser.name + " " + infoUser.lastname);
            ContentValues values = new ContentValues();
            values.put("companyInfoId", companyId);
            values.put("docNum", mDocNumberView.getText().toString());
            String paramsQuery = HttpRequest.makeParamsInUrl(values);
            String url = Constantes.LIST_COMPANIES_BY_DOCNUM;
            Log.e("url", url);
            createNewCompany = false;
            CrudIntentService.startRequestCRUD(instance, url,
                    Request.Method.GET, "", paramsQuery, false);
        } else {
            MetodosPublicos.alertDialog(getActivity(), "No se pudo procesar el codigo de barras, intente de nuevo.");
        }
    }

    private void enabledFields() {
        mNameView.setEnabled(true);
        Toast.makeText(instance, getString(R.string.rues_not_response), Toast.LENGTH_SHORT).show();
    }
}
