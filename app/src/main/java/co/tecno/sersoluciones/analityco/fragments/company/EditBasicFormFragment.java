package co.tecno.sersoluciones.analityco.fragments.company;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.BranchOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.dialogs.OfficeDialogFragment;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.Company;
import co.tecno.sersoluciones.analityco.models.CompanyInstance;
import co.tecno.sersoluciones.analityco.models.EconomicActivity;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;
import co.tecno.sersoluciones.analityco.views.DomainCompletionView;
import co.tecno.sersoluciones.analityco.views.EconomicCompletionView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getBitmapFromUri;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getRealUriImage;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Created by Ser Soluciones SAS on 01/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EditBasicFormFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        AdapterView.OnItemSelectedListener, BranchOfficesRecyclerView.OnBranchOfficeInteractionListener,
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener {


    private static final String ARG_JSON = "json_company";

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
    @BindView(R.id.edit_domain)
    DomainCompletionView domainCompletionView;
    @BindView(R.id.edit_web)
    EditText mWebView;
    @BindView(R.id.edit_max_geo)
    EditText geofenceLimitView;
    @BindView(R.id.edtt_address)
    EditText mAddressView;
    @BindView(R.id.edit_city)
    ClearebleAutoCompleteTextView cityAutoCompleteTextView;
    @BindView(R.id.edtt_phone)
    EditText mPhoneView;
    @BindView(R.id.edtt_email)
    EditText mEmailView;
    @BindView(R.id.view_separation_branchs)
    RelativeLayout viewSeparationBranchs;
    @BindView(R.id.fab_branch)
    FloatingActionButton fabBranch;
    @BindView(R.id.view)
    View view;
    @BindView(R.id.list_offices_recycler)
    RecyclerView recyclerViewOffice;

    private static final int REQUEST_IMAGE_SELECTOR = 199;
    @BindView(R.id.label_add_branch)
    TextView labelAddBranch;
    @BindView(R.id.frame_progress)
    FrameLayout mProgressView;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;

    private Context instance;
    private boolean isNit;

    private BranchOfficesRecyclerView adapter;
    private ArrayList<BranchOffice> branchOffices;

    private int numberMaxNit;
    private Uri mImageUri;
    private String docType;
    private String docNum;


    private String city;
    private String state;
    private boolean searchCity;
    private String cityCode;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    private OnMainFragmentInteractionListener mListener;
    private CompanyInstance company;
    private boolean logoUpload;

    public static EditBasicFormFragment newInstance(String json) {
        EditBasicFormFragment fragment = new EditBasicFormFragment();
        Bundle args = new Bundle();
        args.putString(ARG_JSON, json);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mJson = getArguments().getString(ARG_JSON);
            company = new Gson().fromJson(mJson, CompanyInstance.class);
            if (company.Logo == null) company.Logo = "";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_form_company, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        fillForm(company);
        return view;
    }

    private void fillForm(CompanyInstance company) {

        if (company.Logo != null) {
            if (!company.Logo.isEmpty()) {
                String url = Constantes.URL_IMAGES + company.Logo;
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(iconLogoView);
                fabRemove.setVisibility(View.VISIBLE);
            }
        }
        mDocNumberView.setEnabled(true);
        docType = company.DocumentType;
        docNum = company.DocumentNumber;
        if (company.DocumentType.equals("NIT")) {
            String[] docNumber = company.DocumentNumber.split("-");
            mDocNumberView.setText(docNumber[0]);
            mNumberVerifyView.setText(docNumber[1]);
            mNumberVerifyView.setEnabled(true);
            mDocTypeSpinner.setSelection(2);
        } else {
            mDocTypeSpinner.setSelection(1);
        }
        mNameView.setText(company.Name);
        if (company.EconomicActivities != null && company.EconomicActivities.length > 0) {
            for (int i = 0; i < company.EconomicActivities.length; i++) {
                EconomicActivity economicActivity = new EconomicActivity(company.EconomicActivities[i], "");
                economicCompletionView.addObject(economicActivity);
            }
        }
        if (company.Domains != null && company.Domains.length > 0) {
            for (int i = 0; i < company.Domains.length; i++) {
                log("Domain: " + company.Domains[i]);
                domainCompletionView.addObject(company.Domains[i]);
            }
        }

        mWebView.setText(company.Website);
        geofenceLimitView.setText(String.valueOf(company.GeofenceLimit));

        selectPosCitySpinner(company);
        mAddressView.setText(company.Address);
        cityAutoCompleteTextView.setText(company.NameCity);
        mPhoneView.setText(company.Phone);
        mEmailView.setText(company.Email);
    }

    private void selectPosCitySpinner(CompanyInstance company) {
        logW("NameCity " + company.NameCity + " StateName " + company.StateName);
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_NAME + " = ? ) and (" + DBHelper.DANE_CITY_TABLE_COLUMN_STATE + " = ? )";
        String[] selectArgs = {company.NameCity, company.StateName};
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
        if (cursor != null) {
            cursor.moveToFirst();
            city = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
            state = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE));
            cityCode = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
            cityAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
            searchCity = true;
        }
    }

    private void init() {
        BranchOfficesRecyclerView.OnBranchOfficeInteractionListener mListener = this;
        instance = getActivity();
        logoUpload = false;
        mImageUri = null;

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        cityAutoCompleteTextView.setText("");
        searchCity = false;
        populateCityAutoComplete();

        fabBranch.setOnClickListener(view -> {
            DialogFragment dialog = new OfficeDialogFragment().newInstance();
            dialog.show(getFragmentManager(), "FormOffice");
        });


        fabRemove.setVisibility(View.GONE);
        iconLogoView.setOnClickListener(view -> dispatchPhotoSelectionIntent());

        branchOffices = new ArrayList<>();
        branchOffices.add(new BranchOffice());
        for (int i = 1; i < company.BranchOffices.size(); i++) {
            branchOffices.add(company.BranchOffices.get(i));
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(instance);
        //mLayoutManager.setAutoMeasureEnabled(true);
        recyclerViewOffice.setLayoutManager(mLayoutManager);
        adapter = new BranchOfficesRecyclerView(instance, mListener);
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
        domainCompletionView.allowCollapse(false);

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

     /*   new Handler().postDelayed(new Runnable() {
            public void run() {
                if (mDocTypeSpinner.getSelectedItemPosition() == 0) {
                    TextView mDocTypeView = mDocTypeSpinner.getSelectedView().findViewById(android.R.id.text1);
                    mDocTypeView.setError("Requerido");
                    mDocTypeView.requestFocus();
                }
            }
        }, 500);*/
    }


    private void populateCityAutoComplete() {

        SimpleCursorAdapter cityAdapter = new SimpleCursorAdapter(instance, R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.DANE_CITY_TABLE_COLUMN_NAME, DBHelper.DANE_CITY_TABLE_COLUMN_STATE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        cityAutoCompleteTextView.setAdapter(cityAdapter);

        cityAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorCity(str);
            }
        });

        cityAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
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
                    cityCode = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
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
                        viewSeparation.setVisibility(View.GONE);
                        numberMaxNit = 5;
                        isNit = false;
                    } else {
                        setEditTextMaxLength(mDocNumberView, 9);
                        layoutVerification.setVisibility(View.VISIBLE);
                        mNumberVerifyView.setVisibility(View.VISIBLE);
                        viewSeparation.setVisibility(View.VISIBLE);
                        numberMaxNit = 9;
                        isNit = true;
                    }
                    /*mNumberVerifyView.setText("");
                    mDocNumberView.setText("");
                    mNameView.setText("");
                    mDocNumberView.setEnabled(true);*/
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onDialogPositiveClick(BranchOffice branchOffice) {
        //  logW(new Gson().toJson(branchOffice).toString());
        recyclerViewOffice.setVisibility(View.VISIBLE);
        branchOffices.add(branchOffice);
        adapter.update();
        adapter.swap(branchOffices);
        // logW(new Gson().toJson(branchOffices).toString());
    }

    @Override
    public void onListOrderInteraction(final int position, boolean remove) {

    }


    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edtt_email:
                if (isEmailValid(text)) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            String phone = mPhoneView.getText().toString();
                            String address = mAddressView.getText().toString();
                            String email = mEmailView.getText().toString();
                            branchOffices.set(0, new BranchOffice(Integer.valueOf(cityCode), "Principal", address, city, state, phone, email, true));
                            adapter.update();
                            adapter.swap(branchOffices);
                            recyclerViewOffice.setVisibility(View.VISIBLE);
                            viewSeparationBranchs.setVisibility(View.VISIBLE);
                            fabBranch.setVisibility(View.VISIBLE);
                            labelAddBranch.setVisibility(View.VISIBLE);

                        }
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
        company.Logo = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PATCH);
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
        unbinder.unbind();
    }

    @OnClick(R.id.positive_button)
    public void sumbitRequest() {
        // Reset errors.
        mNameView.setError(null);
        mDocNumberView.setError(null);
        mWebView.setError(null);
        mPhoneView.setError(null);
        mAddressView.setError(null);
        mEmailView.setError(null);
        domainCompletionView.setError(null);
        economicCompletionView.setError(null);

        byte geofenceLimit = 0;

        String geofenceLimitStr = geofenceLimitView.getText().toString();
        String name = mNameView.getText().toString();
        String docNumber = mDocNumberView.getText().toString();
        String web = mWebView.getText().toString();
        String numVerify = mNumberVerifyView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String address = mAddressView.getText().toString();
        String email = mEmailView.getText().toString();
        ArrayList<String> domains = (ArrayList<String>) domainCompletionView.getObjects();
        ArrayList<EconomicActivity> economicActivities = (ArrayList<EconomicActivity>) economicCompletionView.getObjects();
        //String docType = tvDocTypeError.getText().toString();
       /* TextView mDocTypeView = mDocTypeSpinner.getSelectedView().findViewById(android.R.id.text1);
        mDocTypeView.setError(null);
        String docType = tvDocTypeError.getText().toString();*/

        boolean cancel = false;
        View focusView = null;

      /*  if (TextUtils.isEmpty(docType)) {
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
        } else*/
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (economicActivities.isEmpty()) {
            economicCompletionView.setError(getString(R.string.error_field_required));
            focusView = economicCompletionView;
            cancel = true;
        } else if (domains.isEmpty()) {
            domainCompletionView.setError(getString(R.string.error_field_required));
            focusView = domainCompletionView;
            cancel = true;
        } else if (TextUtils.isEmpty(geofenceLimitStr)) {
            geofenceLimitView.setError(getString(R.string.error_field_required));
            focusView = geofenceLimitView;
            cancel = true;
        } else if (TextUtils.isEmpty(address)) {
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

        branchOffices.set(0, new BranchOffice(Integer.valueOf(cityCode), "Principal", address, city, state, phone, email, true));
        adapter.update();
        adapter.swap(branchOffices);

        if (cancel) {


            focusView.requestFocus();
        } else {

            if (!geofenceLimitStr.isEmpty())
                geofenceLimit = Byte.parseByte(geofenceLimitStr);
            if (isNit)
                docNumber = String.format("%s-%s", docNumber, numVerify);

            String[] economicActivityArray = new String[economicActivities.size()];
            for (int i = 0; i < economicActivities.size(); i++) {
                economicActivityArray[i] = economicActivities.get(i).Code;
            }

            String[] domainsArray = domains.toArray(new String[domains.size()]);

            Date startDate = null;
            Date finishDate = null;
            try {
                if (!company.StartDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    startDate = format.parse(company.StartDate);
                    finishDate = format.parse(company.FinishDate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Gson gson = new Gson();

            Company companySend = new Company(company.IsActive, company.StartDate, company.FinishDate,
                    docType, docNum, name, domainsArray, web, geofenceLimit,
                    economicActivityArray, branchOffices);
            //companySend.BranchOfficeJson=BranchOffices;
            final String json = gson.toJson(companySend);
            sendFormToServer(json);
        }
    }

    private void sendFormToServer(final String json) {
        showProgress(true);
        CrudIntentService.startRequestCRUD(getActivity(),
                Constantes.CREATE_COMPANY_URL + company.Id, Request.Method.PUT, json, "", false);
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

    public interface OnMainFragmentInteractionListener {
        void onCancelAction();

        void onApplyAction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMainFragmentInteractionListener) {
            mListener = (OnMainFragmentInteractionListener) context;
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

    @OnClick(R.id.negative_button)
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onCancelAction();
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mProgressView != null)
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        showProgress(false);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (logoUpload) {
                    if (mListener != null) {
                        mListener.onApplyAction();
                    }
                } else {
                    if (mImageUri != null && !mImageUri.toString().isEmpty()) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("file", mImageUri.toString());
                        showProgress(true);
                        logoUpload = true;
                        String urlCompany = Constantes.UPDATE_LOGO_COMPANY_URL + company.Id;
                        CrudIntentService.startActionFormData(getActivity(), urlCompany,
                                Request.Method.PUT, params);
                        log("VALUE_JSON_BROADCAST: " + jsonObjStr);
                        mImageUri = null;
                    } /*else if (company.Logo.isEmpty()) {
                        ArrayList<RequestPatch> requestPatches = new ArrayList<>();
                        requestPatches.add(new RequestPatch("Logo", ""));
                        final String json = new Gson().toJson(requestPatches);
                        logW(json);
                        showProgress(true);
                        CrudIntentService.startRequestPatch(getActivity(), Constantes.CREATE_COMPANY_URL + company.Id, json);
                        logoUpload = true;
                    } */ else {
                        if (mListener != null) {
                            mListener.onApplyAction();
                        }
                    }
                }
                break;
            case Constantes.SEND_REQUEST:
                break;
            case Constantes.BAD_REQUEST:
                //MetodosPublicos.alertDialog(instance, "Fallo al actualizar la base de datos");
                break;
            case Constantes.TIME_OUT_REQUEST:
                MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                if (logoUpload) {
                    if (mListener != null) {
                        mListener.onApplyAction();
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
        }
    }
}
