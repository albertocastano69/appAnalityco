package co.tecno.sersoluciones.analityco.fragments.company;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.CompanyActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.BranchOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.dialogs.OfficeDialogFragment;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.Company;
import co.tecno.sersoluciones.analityco.models.EconomicActivity;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;
import co.tecno.sersoluciones.analityco.views.DomainCompletionView;
import co.tecno.sersoluciones.analityco.views.EconomicCompletionView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.processBarcode;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class CreateStepOneFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        AdapterView.OnItemSelectedListener,
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener {

    private Unbinder unbinder;
    @BindView(R.id.icon_logo)
    MaterialIconView iconLogoView;
    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.spinner_doc_type)
    Spinner mDocTypeSpinner;
    @BindView(R.id.tvDocTypeError)
    TextView tvDocTypeError;

    @BindView(R.id.view_separation)
    View viewSeparation;
    @BindView(R.id.number_verification)
    EditText mNumberVerifyView;
    @BindView(R.id.layout_verification)
    TextInputLayout layoutVerification;
    @BindView(R.id.edtt_name)
    EditText mNameView;
    @BindView(R.id.user_doc_number)
    EditText mDocNumberView;

    @BindView(R.id.edit_economic_activity)
    EconomicCompletionView economicCompletionView;
    @BindView(R.id.email_login_form)
    LinearLayout emailLoginForm;
    @BindView(R.id.card_view_main)
    CardView cardViewMain;
    @BindView(R.id.edit_domain)
    DomainCompletionView domainCompletionView;
    @BindView(R.id.edit_web)
    EditText mWebView;

    @BindView(R.id.edtt_address)
    EditText mAddressView;
    @BindView(R.id.edit_city)
    ClearebleAutoCompleteTextView cityAutoCompleteTextView;
    @BindView(R.id.edtt_phone)
    EditText mPhoneView;
    @BindView(R.id.edtt_email)
    EditText mEmailView;

    @BindView(R.id.fab_branch)
    FloatingActionButton fabBranch;

    @BindView(R.id.list_offices_recycler)
    RecyclerView recyclerViewOffice;

    @BindView(R.id.label_add_branch)
    TextView labelAddBranch;

    @BindView(R.id.fab_scan)
    FloatingActionButton fabScan;
    @BindView(R.id.scanBtnLayout)
    LinearLayout scanBtnLayout;

    private boolean scanccok;

    private Context instance;
    private boolean isNit;

    private BranchOfficesRecyclerView adapter;
    private ArrayList<BranchOffice> branchOffices;

    private int numberMaxNit;

    private Uri mImageUri;


    private String city;
    private String state;
    private boolean searchCity;
    private int cityId;
    private RequestBroadcastReceiver requestBroadcastReceiver;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_company_step_1, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        fabScan.setOnClickListener(v -> startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0));
        return view;
    }

    private void init() {
        instance = getActivity();

        mImageUri = null;

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        cityAutoCompleteTextView.setText("");
        searchCity = false;
        populateCityAutoComplete();

        fabBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dialog = new OfficeDialogFragment().newInstance();
                dialog.show(getFragmentManager(), "FormOffice");
            }
        });

        fabRemove.setVisibility(View.GONE);
        iconLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPhotoSelectionIntent();
            }
        });

        branchOffices = new ArrayList<>();
        branchOffices.add(new BranchOffice());

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(instance);
        //mLayoutManager.setAutoMeasureEnabled(true);
        recyclerViewOffice.setLayoutManager(mLayoutManager);
        adapter = new BranchOfficesRecyclerView(instance, (position, remove) -> {

        });
        recyclerViewOffice.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerViewOffice.getContext(),
                mLayoutManager.getOrientation());
        recyclerViewOffice.addItemDecoration(mDividerItemDecoration);
        recyclerViewOffice.setAdapter(adapter);
        recyclerViewOffice.setVisibility(View.GONE);


        String[] docTypeArray = new String[]{"Tipo", "CC", "NIT"};
        mDocTypeSpinner.setAdapter(new CustomArrayAdapter(instance, docTypeArray));
        mDocTypeSpinner.setOnItemSelectedListener(this);

        mNumberVerifyView.addTextChangedListener(new TextWatcherAdapter(mNumberVerifyView, this));
        mEmailView.addTextChangedListener(new TextWatcherAdapter(mEmailView, this));
        mDocNumberView.addTextChangedListener(new TextWatcherAdapter(mDocNumberView, this));
        mDocNumberView.setEnabled(false);

        ArrayAdapter<String> adapterDomain = new ArrayAdapter<>(instance, R.layout.simple_spinner_item, new ArrayList<String>());
        domainCompletionView.setAdapter(adapterDomain);
        domainCompletionView.allowDuplicates(false);
        domainCompletionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        char[] splitChar = {',', ';', ' '};
        domainCompletionView.setSplitChar(splitChar);

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

        economicCompletionView.setAdapter(adapterEconomic);
        economicCompletionView.allowDuplicates(false);
        economicCompletionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select);
        economicCompletionView.setSplitChar(splitChar);


        numberMaxNit = 5;
        isNit = false;

        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mDocTypeSpinner.getSelectedItemPosition() == 0) {
                    TextView mDocTypeView = mDocTypeSpinner.getSelectedView().findViewById(android.R.id.text1);
                    mDocTypeView.setError("Requerido");
                    mDocTypeView.requestFocus();
                }
            }
        }, 500);


        tvDocTypeError.setVisibility(View.VISIBLE);
        tvDocTypeError.setError("Requerido");
        tvDocTypeError.requestFocus();
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(false)
                .start(this, getActivity());
    }

    private void populateCityAutoComplete() {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(instance, R.layout.simple_spinner_item_2, null,
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
                    city = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
                    state = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE));
                    cityId = cur.getInt(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
                    cityAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
                    searchCity = true;
                }
            }
        });
        cityAutoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(cityAutoCompleteTextView, this));
    }

    private Cursor getCursorCity(CharSequence str) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_NAME + " LIKE ? )";
        String[] selectArgs = {"%" + str + "%"};
        return instance.getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            switch (parent.getId()) {
                case R.id.spinner_doc_type:
                    String text = parent.getItemAtPosition(position).toString();
                    tvDocTypeError.setText(text);
                    tvDocTypeError.setVisibility(View.GONE);
                    scanBtnLayout.setVisibility(View.VISIBLE);
                    tvDocTypeError.setError(null);
                    if (text.equals("CC")) {
                        setEditTextMaxLength(mDocNumberView, 10);
                        mNumberVerifyView.setVisibility(View.GONE);
                        layoutVerification.setVisibility(View.GONE);
                        viewSeparation.setVisibility(View.GONE);
                        numberMaxNit = 5;
                        mDocNumberView.setEnabled(false);
                        mNameView.setEnabled(false);
                        isNit = false;
                        if (!scanccok) {
                            startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
                        }
                    } else {
                        scanccok = false;
                        setEditTextMaxLength(mDocNumberView, 9);
                        layoutVerification.setVisibility(View.VISIBLE);
                        mNumberVerifyView.setVisibility(View.VISIBLE);
                        viewSeparation.setVisibility(View.VISIBLE);
                        numberMaxNit = 9;
                        scanBtnLayout.setVisibility(View.GONE);
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

    public void onDialogPositiveClick(BranchOffice branchOffice) {
        recyclerViewOffice.setVisibility(View.VISIBLE);
        branchOffices.add(branchOffice);
        adapter.update();
        adapter.swap(branchOffices);
    }

    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edtt_email:
                if (isEmailValid(text)) {
                    new Handler().postDelayed(() -> {
                        String phone = mPhoneView.getText().toString();
                        String address = mAddressView.getText().toString();
                        String email = mEmailView.getText().toString();
                        branchOffices.set(0, new BranchOffice(cityId, "Principal", address, city, state, phone, email, true));
                        adapter.update();
                        adapter.swap(branchOffices);
                        recyclerViewOffice.setVisibility(View.VISIBLE);
                        //viewSeparationBranchs.setVisibility(View.VISIBLE);
                        fabBranch.setVisibility(View.VISIBLE);
                        labelAddBranch.setVisibility(View.VISIBLE);

                    }, 500);
                }
                break;
            case R.id.edit_city:
                if (cityAutoCompleteTextView.isFocused()) {
                    //if (!text.isEmpty())
                    cityAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchCity = false;
                }
                break;
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
        searchCity = false;
    }


    //A system-based view to select photos.
    private void dispatchPhotoSelectionIntent() {
        /*Intent intent = new Intent(instance, FaceTrackerActivity.class);
        Bundle bundle = new Bundle();
        PhotoSerOptions photoSerOptions = new PhotoSerOptions();
        photoSerOptions.setDetectFace(false);
        bundle.putParcelable(PHOTO_SER_EXTRA_OPTIONS, photoSerOptions);
        intent.putExtra(PHOTO_SER_EXTRA_BUNDLE, bundle);
        startActivityForResult(intent, PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE);*/
        startFaceDectectorActivity();
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
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri imageUri = android.net.Uri.parse(data.getStringExtra(URI_IMAGE_KEY));
                    iconLogoView.setImageURI(imageUri);
                    fabRemove.setVisibility(View.VISIBLE);
                    mImageUri = imageUri;
                    break;
                default:
                    break;
            }
        } else if (resultCode == BarcodeDecodeSerActivity.SUCCESS) {
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
        }
    }

    @OnClick(R.id.fab_remove)
    public void removeLogo() {
        Drawable drawable = MaterialDrawableBuilder.with(instance) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.BANK) // provide an icon
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
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

                try {
                    String name = new JSONObject(jsonObjStr).getJSONArray("rows").getJSONObject(0).getString("razon_social");
                    mNameView.setText(name);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }
    }

    public void submitRequest() {
        // Reset errors.
        mNameView.setError(null);
        mDocNumberView.setError(null);
        mWebView.setError(null);
        mPhoneView.setError(null);
        mAddressView.setError(null);
        mEmailView.setError(null);
        domainCompletionView.setError(null);
        economicCompletionView.setError(null);

        String name = mNameView.getText().toString();
        String docNumber = mDocNumberView.getText().toString();
        String web = mWebView.getText().toString();
        String numVerify = mNumberVerifyView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String address = mAddressView.getText().toString();
        String email = mEmailView.getText().toString();
        ArrayList<String> domains = (ArrayList<String>) domainCompletionView.getObjects();
        ArrayList<EconomicActivity> economicActivities = (ArrayList<EconomicActivity>) economicCompletionView.getObjects();

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
        } else if (economicActivities.isEmpty()) {
            economicCompletionView.setError(getString(R.string.error_field_required));
            focusView = economicCompletionView;
            cancel = true;
        }/* else if (domains.isEmpty()) {
            domainCompletionView.setError(getString(R.string.error_field_required));
            focusView = domainCompletionView;
            cancel = true;
        }*/ else if (TextUtils.isEmpty(address)) {
            mAddressView.setError(getString(R.string.error_field_required));
            focusView = mAddressView;
            cancel = true;
        } else if (!searchCity) {
            cityAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = cityAutoCompleteTextView;
            cancel = true;
        } else if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError("Digite un numero telefonico valido");
            focusView = mPhoneView;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError("Digite un email valido");
            focusView = mEmailView;
            cancel = true;
        } else if (address.isEmpty() || !searchCity || phone.isEmpty() || email.isEmpty()) {
            MetodosPublicos.alertDialog(instance, "Campos de Sucursal vacios o invalidos");
            focusView = mEmailView;
            cancel = true;
        } else if (branchOffices.isEmpty()) {
            MetodosPublicos.alertDialog(instance, "Debe crear por lo menos una sucursal");
            focusView = recyclerViewOffice;
            cancel = true;
        }

        if (!TextUtils.isEmpty(web) && !isWebValid(web)) {
            mWebView.setError("Digite la pagina web correctamente");
            focusView = mWebView;
            cancel = true;
        }

        branchOffices.set(0, new BranchOffice(cityId, "Principal", address, city, state, phone, email, true));
        adapter.update();
        adapter.swap(branchOffices);

        if (cancel) {


            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            byte geofenceLimit = 0;
            if (isNit)
                docNumber = String.format("%s-%s", docNumber, numVerify);

            String[] economicActivityArray = new String[economicActivities.size()];
            for (int i = 0; i < economicActivities.size(); i++) {
                economicActivityArray[i] = economicActivities.get(i).Code;
            }

            String[] domainsArray = domains.toArray(new String[domains.size()]);

            Gson gson = new Gson();
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Company company = new Company(true, dateFormat.format(new Date()), dateFormat.format(new Date()), docType, docNumber, name, domainsArray, web, geofenceLimit,
                    economicActivityArray, branchOffices);
            if (!mImageUri.toString().isEmpty())
                company.CompanyLogoPath = mImageUri.toString();
            ((CompanyActivity) getActivity()).setCompany(company);
            ((CompanyActivity) getActivity()).nextPage(1);

        }
    }

    private boolean isWebValid(String email) {
        Pattern pattern = Patterns.WEB_URL;
        return pattern.matcher(email).matches();
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private boolean isPhoneValid(String phone) {
        Pattern pattern = Patterns.PHONE;
        return pattern.matcher(phone).matches();
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
        } else {
            MetodosPublicos.alertDialog(getActivity(), "No se pudo procesar el codigo de barras, intente de nuevo.");
        }
    }
}
