package co.tecno.sersoluciones.analityco.fragments.personal.wizard;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONObject;

import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.JoinPersonalWizardActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.PersonalNew;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.UpdateDBService;
import co.tecno.sersoluciones.analityco.utilities.CircleTransform;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.processBarcode;

/**
 * Created by Ser Soluciones SAS on 27/08/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class NewOtherPersonFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener, DatePickerDialog.OnDateSetListener,
        AdapterView.OnItemSelectedListener {

    @BindView(R.id.icon_logo)
    MaterialIconView iconLogoView;
    @BindView(R.id.edtt_name)
    EditText name;
    @BindView(R.id.edtt_last_name)
    EditText lastNameEditText;
    @BindView(R.id.edtt_identification)
    EditText id;

    @BindView(R.id.edit_city)
    ClearebleAutoCompleteTextView cityAutoCompleteTextView;
    @BindView(R.id.edit_city_birth)
    ClearebleAutoCompleteTextView cityBirthAutoCompleteTextView;
    @BindView(R.id.edtt_address)
    EditText address;
    @BindView(R.id.edtt_phone)
    EditText phone;
    @BindView(R.id.edtt_nameContact)
    EditText nameContact;
    @BindView(R.id.edtt_phoneContact)
    EditText phoneContact;
    @BindView(R.id.sexo)
    Spinner sexo;
    @BindView(R.id.rh)
    Spinner rh;
    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.nationalitySpinner)
    Spinner nationalitySpinner;

    @BindView(R.id.edit_Job)
    ClearebleAutoCompleteTextView jobAutoCompleteTextView;
    @BindView(R.id.edit_eps)
    ClearebleAutoCompleteTextView epsAutoCompleteTextView;
    @BindView(R.id.edit_afp)
    ClearebleAutoCompleteTextView afpAutoCompleteTextView;

    @BindView(R.id.controls_edit)
    LinearLayout controlsEdit;
    @BindView(R.id.cancel_button)
    Button cancelBtn;
    @BindView(R.id.submit_button)
    Button submitBtn;
    @BindView(R.id.border_bottom)
    View borderBottom;

    private String companyId;
    private String cityBirthCode;

    private OnCreateOtherUserListener mListener;
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private boolean searchCity;
    private boolean searchCityBirth;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private boolean searchJob;
    private Uri mImageUri;
    private Date dateBirth;
    private String cityCode;
    private Personal personal;
    private String[] listRh;
    private String[] listSexo;
    private String[] listNacionalidad;
    private String dateBirthStr;
    private int _id = 0;

    @BindView(R.id.tvFromDateError)
    public TextView tvDateBirthError;
    private DatePickerDialog dateBirthPickerDialog;
    @BindView(R.id.btn_from_date)
    Button dateBirthBtn;
    private String jobCode;
    private boolean searchEPS;
    private int epsId;
    private boolean searchAFP;
    private int afpId;
    private boolean createOtherPerson;
    private String documentRaw;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_POST);
        intentFilter.addAction(CRUDService.ACTION_REQUEST_FORM_DATA);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.new_other_personal_fragment, container, false);
        ButterKnife.bind(this, view);

        if (getArguments() != null) {
            companyId = getArguments().getString(ARG_PARAM2);
        }
        createOtherPerson = false;
        jobCode = "";
        mImageUri = null;
        dateBirthStr = "";
        documentRaw = "";
        searchCity = false;
        searchCityBirth = false;
        searchJob = false;
        searchEPS = false;
        searchAFP = false;
        controlsEdit.setVisibility(View.VISIBLE);
        borderBottom.setVisibility(View.VISIBLE);
        if (mListener != null)
            mListener.choiceEditUser(true);

        // populateCityBirthAutoComplete(0);
        populateCityAutoComplete();
        populateJobAutoComplete();
        populateSecRefAutoComplete(0, epsAutoCompleteTextView);
        populateSecRefAutoComplete(1, afpAutoCompleteTextView);
        selectDefaultCitySpinner(0);
        populateCityBirthAutoComplete(0);

        listRh = new String[]{"--", "O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        listSexo = new String[]{"M", "F"};
        listNacionalidad = new String[]{"COLOMBIANO", "VENEZOLANO"};
        rh.setAdapter(new ArrayAdapter(getContext(), R.layout.simple_spinner_item, listRh));
        sexo.setAdapter(new ArrayAdapter(getContext(), R.layout.simple_spinner_item, listSexo));
        nationalitySpinner.setAdapter(new ArrayAdapter(getContext(), R.layout.simple_spinner_item, listNacionalidad));
        nationalitySpinner.setOnItemSelectedListener(this);

        iconLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFaceDectectorActivity();
            }
        });
        Calendar calendar = Calendar.getInstance();
        dateBirth = calendar.getTime();

        fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLogo();
            }
        });
        updateDatePicker(dateBirth);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mListener != null)
            mListener.choiceEditUser(false);
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri uriImage = Uri.parse(data.getStringExtra(URI_IMAGE_KEY));

                    Picasso.get().load(uriImage)
                            .transform(new CircleTransform())
                            .placeholder(R.drawable.profile_dummy)
                            .error(R.drawable.profile_dummy)
                            .into(iconLogoView);
                    mImageUri = uriImage;
                    break;
                default:
                    break;
            }
        } else {
            switch (resultCode) {
                case BarcodeDecodeSerActivity.SUCCESS:
                    if (data.hasExtra("barcode")) {
                        String barcodeRes = data.getStringExtra("barcode");
                        int barcodeType = data.getIntExtra("barcode-type", 0);
                        switch (barcodeType) {
                            case Barcode.QR_CODE:

                                Toast.makeText(getActivity(), "Codigo PDF417 no detectado, por favor escanee el codigo de " +
                                        "barras que se encuentra al respaldo de la cedula", Toast.LENGTH_LONG).show();
                                break;
                            case Barcode.PDF417:
                                processBarcodePDF417(barcodeRes);
                                break;
                        }

                    }
                    break;
            }
        }
    }

    private void processBarcodePDF417(String barcodeRes) {
        String processCode = processBarcode(barcodeRes);
        if (!processCode.isEmpty()) {
            DecodeBarcode.InfoUser infoUser = new Gson().fromJson(processCode, DecodeBarcode.InfoUser.class);
            name.setText(infoUser.name);
            lastNameEditText.setText(infoUser.lastname);
            id.setText(String.valueOf(infoUser.dni));
            logW("rh: " + infoUser.rh);
            selectPosCitySpinner(infoUser);
            documentRaw = infoUser.DocumentRaw;
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            try {
                Date date = format.parse(String.valueOf(infoUser.birthDate));
                calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String myFormat = "dd/MMM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
            dateBirthStr = sdf.format(calendar.getTime());
            dateBirthBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            dateBirthBtn.setText(dateBirthStr);
            dateBirth = calendar.getTime();
            dateBirthBtn.setError(null);
            tvDateBirthError.setVisibility(View.GONE);
            tvDateBirthError.setError(null);

            for (int i = 0; i < listSexo.length; i++) {
                if (listSexo[i].equals(infoUser.sex)) {
                    sexo.setSelection(i);
                    break;
                }
            }

            for (int i = 0; i < listRh.length; i++) {
                if (listRh[i].equals(infoUser.rh)) {
                    rh.setSelection(i);
                    break;
                }
            }
            nationalitySpinner.setSelection(0);

        } else {
            MetodosPublicos.alertDialog(getActivity(), "No se pudo procesar el codigo de barras, intente de nuevo.");
        }
    }

    @OnClick(R.id.read_barcode)
    public void scanDNI() {
        startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
    }

    private void selectPosCitySpinner(DecodeBarcode.InfoUser infoUser) {
        String select = "(" + DBHelper.CITY_TABLE_COLUMN_CODE + " = ? ) and ( " + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? )";
        logE(String.format("%s %s", infoUser.cityCode, infoUser.stateCode));
        String[] selectArgs = {String.format("%s%s", infoUser.stateCode, infoUser.cityCode), String.valueOf(0)};
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_CITY_URI, null, select, selectArgs,
                DBHelper.CITY_TABLE_COLUMN_NAME);
        if (cursor != null) {
            cursor.moveToFirst();
            String city = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME));
            cityBirthCode = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE));
            cityBirthAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
            cityBirthAutoCompleteTextView.setText(city);
            searchCityBirth = true;
        }
    }

    private void selectDefaultCitySpinner(int nationality) {
        String select = "(" + DBHelper.CITY_TABLE_COLUMN_CODE + " = ? ) and ( " + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? )";
        logE(String.format("%s %s", "000", "00"));
        String[] selectArgs = {String.format("%s%s", "000", "00"), String.valueOf(nationality)};
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_CITY_URI, null, select, selectArgs,
                DBHelper.CITY_TABLE_COLUMN_NAME);
        if (cursor != null) {
            cursor.moveToFirst();
            String city = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME));
            cityBirthCode = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE));
            cityBirthAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
            cityBirthAutoCompleteTextView.setText(city);
            searchCityBirth = true;
        }
    }

    @OnClick(R.id.btn_from_date)
    public void fromDateDialog() {
        dateBirthPickerDialog.getDatePicker().setTag(R.id.btn_from_date);
        dateBirthPickerDialog.show();
    }

    @OnClick(R.id.cancel_button)
    public void cancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Atencion")
                .setMessage("¿Desea salir sin crear este empleado?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                })
                .setNegativeButton("Cancelar", null);
        builder.create().show();
//        controlsEdit.setVisibility(View.GONE);
//        borderBottom.setVisibility(View.GONE);
//        if (mListener != null)
//            mListener.choiceEditUser(true);
    }

    @OnClick(R.id.submit_button)
    public void saveData() {
        submit();
    }

    public void setData(DecodeBarcode.InfoUser infoUser, String companyId) {
        init();
        this.companyId = companyId;
        id.setText(String.valueOf(infoUser.dni));
        if (mListener != null)
            mListener.choiceEditUser(false);
    }

    private void init() {
        cleanForm();
        createOtherPerson = false;
        jobCode = "";
        mImageUri = null;
        documentRaw = "";
        dateBirthStr = "";
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        searchCity = false;
        searchCityBirth = false;
        searchJob = false;
        searchEPS = false;
        searchAFP = false;
        controlsEdit.setVisibility(View.VISIBLE);
        borderBottom.setVisibility(View.VISIBLE);
        if (mListener != null)
            mListener.choiceEditUser(false);

        // populateCityBirthAutoComplete(0);
        populateCityAutoComplete();
        populateJobAutoComplete();
        populateSecRefAutoComplete(0, epsAutoCompleteTextView);
        populateSecRefAutoComplete(1, afpAutoCompleteTextView);
        Calendar calendar = Calendar.getInstance();
        dateBirth = calendar.getTime();
        updateDatePicker(dateBirth);
        rh.setSelection(0);
        nationalitySpinner.setSelection(0);
        selectDefaultCitySpinner(0);
        populateCityBirthAutoComplete(0);
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(true)
                .setSaveGalery(true)
                .start(this, getActivity());
    }

    private void updateDatePicker(Date fromDate) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);

        dateBirthPickerDialog = new DatePickerDialog(getContext(), this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        dateBirthPickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    private void populateCityAutoComplete() {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(getContext(), R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.DANE_CITY_TABLE_COLUMN_NAME, DBHelper.DANE_CITY_TABLE_COLUMN_STATE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        cityAutoCompleteTextView.setAdapter(mAdapterCust);

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
        cityAutoCompleteTextView.setListener(this);
        cityAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (cityAutoCompleteTextView.isFocused()) {
                    Cursor cur = (Cursor) parent.getItemAtPosition(position);
                    cityCode = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
                    cityAutoCompleteTextView.setClearIconVisible(true);
                    searchCity = true;
                }
            }
        });
        cityAutoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(cityAutoCompleteTextView, this));
    }

    private void populateCityBirthAutoComplete(final int nacionality) {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(getContext(), R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.CITY_TABLE_COLUMN_NAME, DBHelper.CITY_TABLE_COLUMN_STATE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        cityBirthAutoCompleteTextView.setAdapter(mAdapterCust);

        mAdapterCust.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorCityBirth(str, nacionality);
            }
        });

        mAdapterCust.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME);
                return cur.getString(index);
            }
        });
        cityBirthAutoCompleteTextView.setListener(this);
        cityBirthAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (cityBirthAutoCompleteTextView.isFocused()) {
                    Cursor cur = (Cursor) parent.getItemAtPosition(position);
                    String city = cur.getString(cur.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME));
                    cityBirthCode = cur.getString(cur.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE));
                    cityBirthAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
                    searchCityBirth = true;
                }
            }
        });
        cityBirthAutoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(cityAutoCompleteTextView, this));
    }

    private void populateJobAutoComplete() {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(getContext(), R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.WORK_TABLE_COLUMN_NAME, DBHelper.WORK_TABLE_COLUMN_CODE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        jobAutoCompleteTextView.setAdapter(mAdapterCust);

        mAdapterCust.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorJob(str);
            }
        });

        mAdapterCust.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME);
                return "(" + jobCode + ") " + cur.getString(index);
            }
        });
        jobAutoCompleteTextView.setListener(this);
        jobAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (jobAutoCompleteTextView.isFocused()) {
                    Cursor cur = (Cursor) parent.getItemAtPosition(position);
                    //String name = cur.getString(cur.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME));
                    jobCode = cur.getString(cur.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_CODE));
                    //jobAutoCompleteTextView.setText("(" + jobCode + ") " + name);
                    jobAutoCompleteTextView.setClearIconVisible(true);
                    searchJob = true;
                }
            }
        });
        jobAutoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(jobAutoCompleteTextView, this));
        selectPosJobSpinner();
    }

    private void populateSecRefAutoComplete(final int option, final ClearebleAutoCompleteTextView autoCompleteTextView) {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(getContext(), R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.SECURITY_REFERENCE_COLUMN_NAME, DBHelper.SECURITY_REFERENCE_COLUMN_DESCRIPTION},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        autoCompleteTextView.setAdapter(mAdapterCust);

        mAdapterCust.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorSecRef(str, option);
            }
        });

        mAdapterCust.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            public CharSequence convertToString(Cursor cur) {
                int index = cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
                return cur.getString(index);
            }
        });
        autoCompleteTextView.setListener(this);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (autoCompleteTextView.isFocused()) {
                    Cursor cur = (Cursor) parent.getItemAtPosition(position);
                    String name = cur.getString(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME));
                    logW("NAME " + name);
                    int serverId = cur.getInt(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID));
                    autoCompleteTextView.setText(name);
                    autoCompleteTextView.setClearIconVisible(true);
                    if (option == 0) {
                        searchEPS = true;
                        epsId = serverId;
                    } else {
                        searchAFP = true;
                        afpId = serverId;
                    }
                }
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(autoCompleteTextView, this));
        selectPosSecRefSpinner(option, autoCompleteTextView);
    }

    @SuppressLint("SetTextI18n")
    private void selectPosJobSpinner() {
        String select = "(" + DBHelper.WORK_TABLE_COLUMN_SERVER_ID + " = ? )";
        String[] selectArgs = {String.format("%s", 1)};
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_WORK_URI, null, select, selectArgs,
                DBHelper.WORK_TABLE_COLUMN_NAME);
        if (cursor != null) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME));
            jobCode = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_CODE));
            // cityId = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
            jobAutoCompleteTextView.setText("(" + jobCode + ") " + name);
            jobAutoCompleteTextView.setClearIconVisible(!name.isEmpty());
            searchJob = true;
        }
    }

    @SuppressLint("SetTextI18n")
    private void selectPosSecRefSpinner(final int option, final ClearebleAutoCompleteTextView autoCompleteTextView) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_CODE + " = ? ) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {String.format("%s", option == 0 ? "EPS00000" : "AFP00000"), String.valueOf(option)};
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
        if (cursor != null) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME));
            int id = cursor.getInt(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID));
            autoCompleteTextView.setText(name);
            autoCompleteTextView.setClearIconVisible(!name.isEmpty());
            if (option == 0) {
                searchEPS = true;
                epsId = id;
            } else {
                searchAFP = true;
                afpId = id;
            }
        }
    }

    private void removeLogo() {
        Drawable drawable = MaterialDrawableBuilder.with(getContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
                .setColor(Color.GRAY)
                .build();
        iconLogoView.setImageDrawable(drawable);
        fabRemove.setVisibility(View.GONE);
    }

    private Cursor getCursorCityBirth(CharSequence str, int nationality) {
        String select = "(" + DBHelper.CITY_TABLE_COLUMN_NAME + " LIKE ? ) and ( " + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? )";
        String[] selectArgs = {"%" + str + "%", String.valueOf(nationality)};
        return getContext().getContentResolver().query(Constantes.CONTENT_CITY_URI, null, select, selectArgs,
                DBHelper.CITY_TABLE_COLUMN_NAME);
    }

    private Cursor getCursorCity(CharSequence str) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_NAME + " LIKE ? )";
        String[] selectArgs = {"%" + str + "%"};
        return getContext().getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
    }

    private Cursor getCursorJob(CharSequence str) {
        if (str != null && !str.toString().isEmpty()) {
            String asciiName = Normalizer.normalize(str, Normalizer.Form.NFD)
                    .replaceAll("[^\\p{ASCII}]", "");
            String select = "(" + DBHelper.WORK_TABLE_COLUMN_NAME + "  LIKE ? )";
            String[] selectArgs = {"%" + asciiName + "%"};
            return getContext().getContentResolver().query(Constantes.CONTENT_WORK_URI, null, select, selectArgs,
                    DBHelper.WORK_TABLE_COLUMN_NAME);
        }
        return null;
    }

    private Cursor getCursorSecRef(CharSequence str, int option) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_NAME + " LIKE ?  OR "
                + DBHelper.SECURITY_REFERENCE_COLUMN_DESCRIPTION + " LIKE ?) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {"%" + str + "%", "%" + str + "%", String.valueOf(option)};
        return getContext().getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
    }

    @Override
    public void didClearText(View view) {
        switch (view.getId()) {
            case R.id.edit_city:
                cityAutoCompleteTextView.setClearIconVisible(false);
                cityAutoCompleteTextView.requestFocus();
                searchCity = false;
                break;
            case R.id.edit_city_birth:
                cityBirthAutoCompleteTextView.setClearIconVisible(false);
                cityBirthAutoCompleteTextView.requestFocus();
                searchCityBirth = false;
                break;
            case R.id.edit_Job:
                jobAutoCompleteTextView.setClearIconVisible(false);
                jobAutoCompleteTextView.requestFocus();
                searchJob = false;
                break;
            case R.id.edit_eps:
                epsAutoCompleteTextView.setClearIconVisible(false);
                epsAutoCompleteTextView.requestFocus();
                searchEPS = false;
                break;
            case R.id.edit_afp:
                afpAutoCompleteTextView.setClearIconVisible(false);
                afpAutoCompleteTextView.requestFocus();
                searchAFP = false;
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edit_city:
                searchCity = false;
                cityCode = null;
                break;
            case R.id.edit_Job:
                searchJob = false;
                break;
            case R.id.edit_city_birth:
                searchCityBirth = false;
                cityBirthCode = null;
                break;
            case R.id.edit_eps:
                searchEPS = false;
                break;
            case R.id.edit_afp:
                searchAFP = false;
                break;
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
                dateBirthStr = sdf.format(calendar.getTime());
                dateBirthBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                dateBirthBtn.setText(dateBirthStr);
                dateBirth = calendar.getTime();
                dateBirthBtn.setError(null);

                tvDateBirthError.setVisibility(View.GONE);
                tvDateBirthError.setError(null);
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void submit() {
        boolean send = true;
        View view = null;
        if (mImageUri == null || mImageUri.toString().isEmpty()) {
            MetodosPublicos.alertDialog(getActivity(), "La foto es obligatoria");
            send = false;
            view = iconLogoView;
        } else if (TextUtils.isEmpty(name.getText().toString())) {
            name.setError(getString(R.string.error_field_required));
            send = false;
            view = name;
        } else if (TextUtils.isEmpty(lastNameEditText.getText().toString())) {
            lastNameEditText.setError(getString(R.string.error_field_required));
            send = false;
            view = lastNameEditText;
        } else if (TextUtils.isEmpty(id.getText().toString())) {
            id.setError(getString(R.string.error_field_required));
            send = false;
            view = id;
        }
//        else if (dateBirthStr.isEmpty()) {
//            dateBirthBtn.setError(getString(R.string.error_field_required));
//            dateBirthBtn.requestFocus();
//            send = false;
//            tvDateBirthError.setError(getString(R.string.error_field_required));
//        }
        else if (!searchCityBirth) {
            selectDefaultCitySpinner(0);
//            send = false;
//            cityBirthAutoCompleteTextView.setError(getString(R.string.error_field_required));
//            view = cityBirthAutoCompleteTextView;
        } else if (!searchJob) {
            selectPosJobSpinner();
//            send = false;
//            jobAutoCompleteTextView.setError(getString(R.string.error_field_required));
//            view = jobAutoCompleteTextView;
        }
//        else if (!searchCity) {
//            send = false;
//            cityAutoCompleteTextView.setError(getString(R.string.error_field_required));
//            view = cityAutoCompleteTextView;
//        }
        else if (!searchEPS) {
            selectPosSecRefSpinner(0, epsAutoCompleteTextView);
            /*send = false;
            epsAutoCompleteTextView.setError(getString(R.string.error_field_required));
            view = epsAutoCompleteTextView;*/
        } else if (!searchAFP) {
            selectPosSecRefSpinner(1, afpAutoCompleteTextView);
            /*send = false;
            afpAutoCompleteTextView.setError(getString(R.string.error_field_required));
            view = afpAutoCompleteTextView;*/
        }
//        } else if (TextUtils.isEmpty(address.getText().toString())) {
//            send = false;
//            address.setError(getString(R.string.error_field_required));
//            view = address;
//        } else if (TextUtils.isEmpty(phone.getText().toString())) {
//            send = false;
//            phone.setError(getString(R.string.error_field_required));
//            view = phone;
//        }

        if (!send) {
            if (view != null)
                view.requestFocus();
        } else {
            sendToServer();
        }

    }

    private void sendToServer() {
        if (!dateBirthStr.isEmpty()) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            dateBirthStr = format.format(dateBirth);
        } else {
            dateBirthStr = "";
        }

        if (getActivity() instanceof JoinPersonalWizardActivity)
            ((JoinPersonalWizardActivity) getActivity()).showProgress(true);

        PersonalNew newPersonal = new PersonalNew("", "", "CC", id.getText().toString(),
                name.getText().toString(), lastNameEditText.getText().toString(), phone.getText().toString(), cityBirthCode, "0",
                listRh[rh.getSelectedItemPosition()],
                listSexo[sexo.getSelectedItemPosition()], "", cityCode, address.getText().toString(),
                nameContact.getText().toString(), phoneContact.getText().toString(),
                "", dateBirthStr, companyId, jobCode, epsId, afpId, documentRaw, nationalitySpinner.getSelectedItemPosition());
        Gson gson = new Gson();
        String json = gson.toJson(newPersonal);

        CRUDService.startRequest(getContext(), Constantes.NEW_PERSONAL_COMPANY_URL, Request.Method.POST, json);
        createOtherPerson = true;
    }


    private void cleanForm() {
        cityCode = null;
        cityBirthCode = null;
        mImageUri = null;
        iconLogoView.setImageResource(R.drawable.profile_dummy);
        name.setText("");
        id.setText("");
        lastNameEditText.setText("");
        phone.setText("");
        address.setText("");
        nameContact.setText("");
        phoneContact.setText("");
        populateCityAutoComplete();
        populateJobAutoComplete();
        populateSecRefAutoComplete(0, epsAutoCompleteTextView);
        populateSecRefAutoComplete(1, afpAutoCompleteTextView);
        Calendar calendar = Calendar.getInstance();
        dateBirth = calendar.getTime();
        dateBirthBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0);
        dateBirthBtn.setText("");
        dateBirthBtn.setError(null);
        tvDateBirthError.setVisibility(View.GONE);
        tvDateBirthError.setError(null);
    }


    @Override
    public void onStringResult(String action, int option, String response, String url) {

        switch (action) {
            case CRUDService.ACTION_REQUEST_GET:
            case CRUDService.ACTION_REQUEST_PUT:
            case CRUDService.ACTION_REQUEST_DELETE:
                break;
            case CRUDService.ACTION_REQUEST_POST:
            case CRUDService.ACTION_REQUEST_FORM_DATA:
                if (createOtherPerson && (url.equals(Constantes.NEW_PERSONAL_COMPANY_URL) || url.equals(Constantes.UPDATE_PHOTO_NEW_PERSONAL_URL + _id)))
                    processRequestALL(option, response);
                break;
        }
    }

    private void processRequestALL(int option, String response) {

        if (getActivity() instanceof JoinPersonalWizardActivity)
            ((JoinPersonalWizardActivity) getActivity()).showProgress(false);

        switch (option) {
            case Constantes.SUCCESS_FILE_UPLOAD:
                if (mListener != null) {
                    // cancel();
                    if (getActivity() instanceof JoinPersonalWizardActivity)
                        ((JoinPersonalWizardActivity) getActivity()).showProgressLayout(false);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String photo = jsonObject.getString("Photo");
                        logW("photo: " + photo);
                        personal.Photo = photo;
                        mListener.onApplyNewUserOther(personal, mImageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constantes.SUCCESS_REQUEST:
                logW(response);
                personal = new Gson().fromJson(response,
                        new TypeToken<Personal>() {
                        }.getType());
//                if (mImagePath != null && !mImagePath.isEmpty()) {
//                    try {
//                        JSONObject jsonObject = new JSONObject(response);
//                        _id = jsonObject.getInt("PersonalCompanyInfoId");
//                        logW("PersonalCompanyInfoId: " + _id);
//                        HashMap<String, String> params = new HashMap<>();
//                        params.put("file", mImagePath);
//
//                        CRUDService.startRequest(getContext(),
//                                Constantes.UPDATE_PHOTO_NEW_PERSONAL_URL + _id, Request.Method.PUT, params);
//                        if (getActivity() instanceof PersonalWizardActivity) {
//                            ((PersonalWizardActivity) getActivity()).showProgress(false);
//                            ((PersonalWizardActivity) getActivity()).showProgressLayout(true);
//                        } else if (getActivity() instanceof JoinPersonalWizardActivity) {
//                            ((JoinPersonalWizardActivity) getActivity()).showProgress(false);
//                            ((JoinPersonalWizardActivity) getActivity()).showProgressLayout(true);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
                if (mListener != null) {
                    // cancel();
                    mListener.onApplyNewUserOther(personal, mImageUri);

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int _id = jsonObject.getInt("PersonalCompanyInfoId");
                        logW("PersonalCompanyInfoId: " + _id);
                        ContentValues cv = new ContentValues();
                        ArrayList<String> arrayListImages = new ArrayList<>();
                        arrayListImages.add(mImageUri.toString());
                        String images = TextUtils.join(",", arrayListImages);
                        cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_IMAGES, images);
                        logW("mImagePath to save later: " + mImageUri.toString());
                        cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_DATA, "");
                        cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_SERVER_ID, _id);
                        cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_URL, Constantes.UPDATE_PHOTO_NEW_PERSONAL_URL);
                        cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_METHOD, Request.Method.PUT);
                        getActivity().getContentResolver().insert(Constantes.CONTENT_PERSON_REPORT_URI, cv);
                        UpdateDBService.startRequest(getActivity(), false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    }
                }
                break;
            case Constantes.BAD_REQUEST:
                MetodosPublicos.alertDialog(getActivity(), response);
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.FORBIDDEN:
            case Constantes.REQUEST_NOT_FOUND:
            case Constantes.TIME_OUT_REQUEST:
                Toast.makeText(getActivity(), getActivity().getString(R.string.error_internet_toast), Toast.LENGTH_LONG).show();
                break;
        }
    }

    public interface OnCreateOtherUserListener {
        void onApplyNewUserOther(Personal personal, Uri mImageUri);

        void choiceEditUser(boolean option);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateOtherUserListener) {
            mListener = (OnCreateOtherUserListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCreateOtherUserListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        logW("position nacionality: " + position);
        selectDefaultCitySpinner(position);
        populateCityBirthAutoComplete(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
