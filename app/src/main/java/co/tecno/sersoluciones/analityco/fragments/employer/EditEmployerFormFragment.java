package co.tecno.sersoluciones.analityco.fragments.employer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.DetailsEmployerActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.EmployerInstance;
import co.tecno.sersoluciones.analityco.models.RequestPatch;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class EditEmployerFormFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        AdapterView.OnItemSelectedListener,
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
    @BindView(R.id.user_doc_number)
    EditText mDocNumberView;
    @BindView(R.id.number_verification)
    EditText mNumberVerifyView;
    @BindView(R.id.layout_verification)
    TextInputLayout layoutVerification;

    @BindView(R.id.view_separation)
    View viewSeparation;
    @BindView(R.id.edtt_name)
    EditText mNameView;
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
    @BindView(R.id.edit_rol)
    EditText editRol;

    @BindView(R.id.frame_progress)
    FrameLayout mProgressView;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;

    @BindView(R.id.edit_afp)
    ClearebleAutoCompleteTextView afpAutoCompleteTextView;

    private Context instance;
    private boolean isNit;
    private int numberMaxNit;
    private Uri mImageUri;

    private String city;
    private boolean searchCity;
    private String cityCode;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    private OnEditEmployerListener mListener;
    private EmployerInstance employer;
    private boolean logoUpload;

    private boolean searchAfp;
    private int afpId;

    public static EditEmployerFormFragment newInstance(String json) {
        EditEmployerFormFragment fragment = new EditEmployerFormFragment();
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
            employer = new Gson().fromJson(mJson, EmployerInstance.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_form_employer, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        fillForm(employer);
        return view;
    }

    private void fillForm(EmployerInstance employer) {
        if (employer != null) {
            if (employer.Logo != null && !employer.Logo.isEmpty()) {
                String url = Constantes.URL_IMAGES + employer.Logo;
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(iconLogoView);
                fabRemove.setVisibility(View.VISIBLE);
            }
            mDocNumberView.setEnabled(true);
            if (employer.DocumentType.equals("NIT")) {
                String[] docNumber = employer.DocumentNumber.split("-");
                mDocNumberView.setText(docNumber[0]);
                mNumberVerifyView.setText(docNumber[1]);
                mNumberVerifyView.setEnabled(true);
                mDocTypeSpinner.setSelection(2);
            } else {
                mDocTypeSpinner.setSelection(1);
            }
            mNameView.setText(employer.Name);
            editRol.setText(employer.Rol);
            mWebView.setText(employer.Website);

            /*if (employer.CityCode != null)*/
            selectPosCitySpinner(employer);
            mAddressView.setText(employer.Address);
            cityAutoCompleteTextView.setText(employer.CityName);
            mPhoneView.setText(employer.Phone);
            mEmailView.setText(employer.Email);
            if (employer.ArlId != 0) {
                afpId = employer.ArlId;
            }
        }
    }

    private void selectPosCitySpinner(EmployerInstance employer) {
        logW("NameCity " + employer.CityCode + " StateName " + employer.StateName);
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_CODE + " = ? )";
        String[] selectArgs = {employer.CityCode == null ? "0" : employer.CityCode};
        //noinspection ConstantConditions
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            city = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
            cityCode = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
            cityAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
            searchCity = true;
        }
    }

    private void init() {
        instance = getActivity();
        logoUpload = false;
        mImageUri = null;
        searchAfp = false;
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        cityAutoCompleteTextView.setText("");
        searchCity = false;
        populateCityAutoComplete();

        fabRemove.setVisibility(View.GONE);
        iconLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPhotoSelectionIntent();
            }
        });

        String[] docTypeArray = new String[]{"Tipo", "CC", "NIT"};
        mDocTypeSpinner.setAdapter(new CustomArrayAdapter(instance, docTypeArray));
        mDocTypeSpinner.setOnItemSelectedListener(this);

        mNumberVerifyView.addTextChangedListener(new TextWatcherAdapter(mNumberVerifyView, this));
        mEmailView.addTextChangedListener(new TextWatcherAdapter(mEmailView, this));
        mDocNumberView.addTextChangedListener(new TextWatcherAdapter(mDocNumberView, this));
        mDocNumberView.setEnabled(false);
        numberMaxNit = 5;
        isNit = false;
        populateSecRefAutoComplete(afpAutoCompleteTextView);
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


    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
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
            case R.id.edit_afp:
                if (afpAutoCompleteTextView.isFocused()) {
                    //if (!text.isEmpty())
                    afpAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchAfp = false;
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
        switch (view.getId()) {
            case R.id.edit_city:
                searchCity = false;
                break;
            case R.id.edit_afp:
                searchAfp = false;
                break;
        }
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

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(false)
                .start(this, getActivity());
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
                    Uri imageUri = Uri.parse(data.getStringExtra(URI_IMAGE_KEY));
                    iconLogoView.setImageURI(imageUri);
                    fabRemove.setVisibility(View.VISIBLE);
                    mImageUri = imageUri;
                    break;
                default:
                    break;
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
        employer.Logo = "";
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

        String name = mNameView.getText().toString();
        String docNumber = mDocNumberView.getText().toString();
        String web = mWebView.getText().toString();
        String numVerify = mNumberVerifyView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String address = mAddressView.getText().toString();
        String email = mEmailView.getText().toString();
        String rol = editRol.getText().toString();
/*        TextView mDocTypeView = mDocTypeSpinner.getSelectedView().findViewById(android.R.id.text1);
        mDocTypeView.setError(null);
        String docType = tvDocTypeError.getText().toString();*/

        boolean cancel = false;
        View focusView = null;

       /* if (TextUtils.isEmpty(docType)) {
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
        } else*/
        if (TextUtils.isEmpty(rol)) {
            editRol.setError(getString(R.string.error_field_required));
            focusView = editRol;
            cancel = true;
        }

        if (!TextUtils.isEmpty(address) && !searchCity) {
            cityAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = cityAutoCompleteTextView;
            cancel = true;
        } else if (!TextUtils.isEmpty(address) && TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!TextUtils.isEmpty(address) && !isPhoneValid(phone)) {
            mPhoneView.setError("Digite un numero telefonico valido");
            focusView = mPhoneView;
            cancel = true;
        } else if (!TextUtils.isEmpty(address) && TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!TextUtils.isEmpty(address) && !isEmailValid(email)) {
            mEmailView.setError("Digite un email valido");
            focusView = mEmailView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(web) && !isWebValid(web)) {
            mWebView.setError("Digite la pagina web correctamente");
            focusView = mWebView;
            cancel = true;
        } else if (!searchAfp) {
            selectPosSecRefSpinner(afpAutoCompleteTextView);
        } else if (afpId == 0) {
            cancel = true;
            focusView = afpAutoCompleteTextView;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            if (isNit)
                docNumber = String.format("%s-%s", docNumber, numVerify);

            EmployerInstance employer2 = new EmployerInstance(employer.DocumentType, employer.DocumentNumber, name, address, cityCode, web, phone, email, rol);
            employer2.ArlId = afpId;
            Gson gson = new Gson();
            final String json = gson.toJson(employer2);
            logW("JSON " + address + " " + json);

            sendFormToServer(json);
        }
    }

    private void sendFormToServer(final String json) {
        showProgress(true);
        CrudIntentService.startRequestCRUD(getActivity(),
                Constantes.CREATE_EMPLOYER_URL + employer.Id, Request.Method.PUT, json, "", false);
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

    public interface OnEditEmployerListener {
        void onCancelAction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEditEmployerListener) {
            mListener = (OnEditEmployerListener) context;
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
                    ((DetailsEmployerActivity) Objects.requireNonNull(getContext())).onApplyAction();
//                    if (mListener != null) {
//                        mListener.onApplyAction();
//                    }
                } else {
                    if (mImageUri != null && !mImageUri.toString().isEmpty()) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("file", mImageUri.toString());
                        showProgress(true);
                        logoUpload = true;
                        String urlCompany = Constantes.UPDATE_LOGO_EMPLOYER_URL + employer.Id;
                        CrudIntentService.startActionFormData(getActivity(), urlCompany,
                                Request.Method.PUT, params);
                        log("VALUE_JSON_BROADCAST: " + jsonObjStr);
                        mImageUri = null;
                    } else if (employer.Logo != null) {
                        if (employer.Logo.isEmpty()) {
                            ArrayList<RequestPatch> requestPatches = new ArrayList<>();
                            requestPatches.add(new RequestPatch("Logo", ""));
                            final String json = new Gson().toJson(requestPatches);
                            logW(json);
                            showProgress(true);
                            CrudIntentService.startRequestPatch(getActivity(), Constantes.CREATE_EMPLOYER_URL + employer.Id, json);
                            logoUpload = true;
                        }
                        ((DetailsEmployerActivity) Objects.requireNonNull(getContext())).onApplyAction();
                    } else {
                        ((DetailsEmployerActivity) Objects.requireNonNull(getContext())).onApplyAction();
//                        if (mListener != null) {
//                            mListener.onApplyAction();
//                        }
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
                logW("entra");
                if (logoUpload) {
                    ((DetailsEmployerActivity) Objects.requireNonNull(getContext())).onApplyAction();
//                    if (mListener != null) {
//                        mListener.onApplyAction();
//                    }
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

    private void populateSecRefAutoComplete(final ClearebleAutoCompleteTextView autoCompleteTextView) {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(getActivity(), R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.SECURITY_REFERENCE_COLUMN_NAME, DBHelper.SECURITY_REFERENCE_COLUMN_DESCRIPTION},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        autoCompleteTextView.setAdapter(mAdapterCust);

        mAdapterCust.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence str) {
                return getCursorSecRef(str, 1);
            }
        });

        mAdapterCust.setCursorToStringConverter(cur -> {
            int index = cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
            return cur.getString(index);
        });
        autoCompleteTextView.setListener(this);

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            if (autoCompleteTextView.isFocused()) {
                Cursor cur = (Cursor) parent.getItemAtPosition(position);
                String name = cur.getString(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME));
                int serverId = cur.getInt(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID));
                autoCompleteTextView.setText(name);
                autoCompleteTextView.setClearIconVisible(true);
                searchAfp = true;
                afpId = serverId;
                logW("afpid " + id);
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(autoCompleteTextView, this));
        selectPosSecRefSpinner(autoCompleteTextView);
    }

    private Cursor getCursorSecRef(CharSequence str, int option) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_NAME + " LIKE ?  OR "
                + DBHelper.SECURITY_REFERENCE_COLUMN_DESCRIPTION + " LIKE ?) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {"%" + str + "%", "%" + str + "%", String.valueOf(option)};
        return getActivity().getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
    }

    @SuppressLint("SetTextI18n")
    private void selectPosSecRefSpinner(final ClearebleAutoCompleteTextView autoCompleteTextView) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_CODE + " = ? ) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {String.format("%s", employer.ArlId), String.valueOf(2)};
        autoCompleteTextView.setText(employer.NameARL);
        @SuppressLint("Recycle")
        Cursor cursor = getContext().getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME));
            int id = cursor.getInt(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID));
            autoCompleteTextView.setText(name);
            autoCompleteTextView.setClearIconVisible(!name.isEmpty());
            afpId = id;
            cursor.close();
        }
    }

}
