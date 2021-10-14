package co.tecno.sersoluciones.analityco.fragments.personal;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import java.text.Normalizer;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.PersonalInstance;
import co.tecno.sersoluciones.analityco.models.UpdatePersonalInfo;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Created by Ser SOluciones SAS on 24/01/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EditPersonalFormFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        AdapterView.OnItemSelectedListener, TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener {


    private static final String ARG_JSON = "json_company";

    private Unbinder unbinder;
    @BindView(R.id.icon_logo)
    MaterialIconView iconLogoView;
    @BindView(R.id.fab_change)
    FloatingActionButton fabChange;

    @BindView(R.id.edtt_address)
    EditText mAddressView;
    @BindView(R.id.edit_city)
    ClearebleAutoCompleteTextView cityAutoCompleteTextView;
    @BindView(R.id.edtt_phone)
    EditText mPhoneView;
    @BindView(R.id.edit_Job)
    ClearebleAutoCompleteTextView jobAutoCompleteTextView;
    @BindView(R.id.frame_progress)
    FrameLayout mProgressView;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;
    @BindView(R.id.label_nit)
    TextView labelNit;
    @BindView(R.id.text_nit)
    TextView textNit;
    @BindView(R.id.edit_emergency_contact)
    EditText editEmergencyContact;
    @BindView(R.id.edit_emergency_phone)
    EditText editEmergencyPhone;
    private String jobCode, jobName;
    @BindView(R.id.edit_eps)
    ClearebleAutoCompleteTextView epsAutoCompleteTextView;
    @BindView(R.id.edit_afp)
    ClearebleAutoCompleteTextView afpAutoCompleteTextView;
   /* @BindView(R.id.spinner_job)
    Spinner spinnerJob;*/

    private Context instance;
    private Uri mImageUri;

    private String city;
    private boolean searchCity, searchJob;
    private String cityCode;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    private OnEditPersonalListener mListener;
    private boolean logoUpload;
    private PersonalInstance personal;
    private int epsId;
    private int afpId;

    public static EditPersonalFormFragment newInstance(String json) {
        EditPersonalFormFragment fragment = new EditPersonalFormFragment();
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
            personal = new Gson().fromJson(mJson, PersonalInstance.class);
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_form_personal, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        fillForm(personal);
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void fillForm(PersonalInstance personal) {

        labelNit.setText(String.format("C.C.: %-12s RH: %-10s", personal.DocumentNumber,
                personal.RH));

        textNit.setText(String.format("Sexo: %s", personal.Sex));

        if (personal.Photo != null && !personal.Photo.isEmpty()) {
            String url = Constantes.URL_IMAGES + personal.Photo;
            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(iconLogoView);
            fabChange.setVisibility(View.VISIBLE);
        }

        selectPosCitySpinner(personal);
        selectPosJobSpinner(personal);
        selectPosSecRefSpinner(personal, 0, epsAutoCompleteTextView);
        selectPosSecRefSpinner(personal, 1, afpAutoCompleteTextView);
        mAddressView.setText(personal.Address);
        cityAutoCompleteTextView.setText(personal.CityName);
        jobAutoCompleteTextView.setText("(" + personal.JobCode + ") " + personal.JobName);
        mPhoneView.setText(personal.PhoneNumber);
        editEmergencyContact.setText(personal.EmergencyContact);
        editEmergencyPhone.setText(personal.EmergencyContactPhone);
        epsId = personal.EpsId;
        afpId = personal.AfpId;
        searchJob = true;
        /* Job job = new Job();
        for (Job job1 : jobArrayList) {
            if (job1.getName().equals(personal.JobName)) {
                job = job1;
                //jobId = job1.getId();
                jobId = job1.Code;
                break;
            }
        }*/
        //spinnerJob.setSelection(((CustomArrayObjAdapter) spinnerJob.getAdapter()).getPosition(job));
        //spinnerJob.setOnItemSelectedListener(this);
    }

    private void selectPosCitySpinner(PersonalInstance personal) {
        if (personal.CityCode == null || personal.CityCode.isEmpty()) return;
        logW("NameCity " + personal.CityName + " StateName " + personal.StateName);
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_CODE + " = ? )";
        String[] selectArgs = {personal.CityCode};
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

    private void selectPosJobSpinner(PersonalInstance personal) {
        logW("JobCode " + personal.JobCode);
        String select = "(" + DBHelper.WORK_TABLE_COLUMN_CODE + " = ? )";
        String[] selectArgs = {personal.JobCode};
        //noinspection ConstantConditions
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_WORK_URI, null, select, selectArgs,
                DBHelper.WORK_TABLE_COLUMN_NAME);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            jobName = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME));
            jobCode = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_CODE));
            jobAutoCompleteTextView.setClearIconVisible(!jobName.isEmpty());
            searchJob = true;
        }
    }

    private void init() {
        instance = getActivity();
        logoUpload = false;
        mImageUri = null;
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        cityAutoCompleteTextView.setText("");
        searchCity = false;
        searchJob = false;
        populateCityAutoComplete();
        populateJobAutoComplete();
        populateSecRefAutoComplete(0, epsAutoCompleteTextView);
        populateSecRefAutoComplete(1, afpAutoCompleteTextView);
        fabChange.setVisibility(View.GONE);
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
                    int serverId = cur.getInt(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID));
                    if (option == 0) {
                        epsId = serverId;
                    } else {
                        afpId = serverId;
                    }
                    autoCompleteTextView.setClearIconVisible(true);
                    if (option == 0) {
                    }
                }
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(autoCompleteTextView, this));
    }

    private Cursor getCursorSecRef(CharSequence str, int option) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_NAME + " LIKE ?  OR "
                + DBHelper.SECURITY_REFERENCE_COLUMN_DESCRIPTION + " LIKE ?) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {"%" + str + "%", "%" + str + "%", String.valueOf(option)};
        return getContext().getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
    }

    @SuppressLint("SetTextI18n")
    private void selectPosSecRefSpinner(PersonalInstance personal, final int option, final ClearebleAutoCompleteTextView autoCompleteTextView) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_CODE + " = ? ) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {String.format("%s", option == 0 ? personal.EpsCode : personal.AfpCode), String.valueOf(option)};
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
        logW("epsid validate " + (cursor != null));
        if (cursor != null) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME));
            int id = cursor.getInt(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID));
            autoCompleteTextView.setText(name);
            autoCompleteTextView.setClearIconVisible(!name.isEmpty());
            if (option == 0) {
                epsId = id;
            } else {
                afpId = id;
            }
        }
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
        cityAutoCompleteTextView.setListener(new ClearebleAutoCompleteTextView.Listener() {
            @Override
            public void didClearText(View view) {
                if (searchCity) {
                    cityAutoCompleteTextView.setClearIconVisible(false);
                    searchCity = false;
                }
            }
        });
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
                return cur.getString(index);
            }
        });
        jobAutoCompleteTextView.setListener(new ClearebleAutoCompleteTextView.Listener() {
            @Override
            public void didClearText(View view) {
                if (searchJob) {
                    jobAutoCompleteTextView.setClearIconVisible(false);
                    searchJob = false;
                }
            }
        });
        jobAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (jobAutoCompleteTextView.isFocused()) {
                    Cursor cur = (Cursor) parent.getItemAtPosition(position);
                    jobName = cur.getString(cur.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME));
                    jobCode = cur.getString(cur.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_CODE));
                    jobAutoCompleteTextView.setClearIconVisible(!jobName.isEmpty());
                    searchJob = true;
                    jobAutoCompleteTextView.setText("(" + jobCode + ") " + jobName);
                }
            }
        });
        jobAutoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(jobAutoCompleteTextView, this));
    }

    private Cursor getCursorCity(CharSequence str) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_NAME + " LIKE ? )";
        String[] selectArgs = {"%" + str + "%"};
        return instance.getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
    }

    private Cursor getCursorJob(CharSequence str) {
        String asciiName = Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        String select = "(" + DBHelper.WORK_TABLE_COLUMN_NAME + "  LIKE ? )";
        String[] selectArgs = {"%" + asciiName + "%"};
        return getContext().getContentResolver().query(Constantes.CONTENT_WORK_URI, null, select, selectArgs,
                DBHelper.WORK_TABLE_COLUMN_NAME);
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
            case R.id.edit_Job:
                if (jobAutoCompleteTextView.isFocused()) {
                    //if (!text.isEmpty())
                    jobAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchJob = false;
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
                    fabChange.setVisibility(View.VISIBLE);
                    mImageUri = imageUri;
                    break;
                default:
                    break;
            }
        }
    }

    @OnClick({R.id.fab_change, R.id.icon_logo})
    public void changeLogo() {
       /* Intent intent = new Intent(getActivity(), FaceTrackerActivity.class);
        Bundle bundle = new Bundle();
        PhotoSerOptions photoSerOptions = new PhotoSerOptions();
        photoSerOptions.setDetectFace(true);
        bundle.putParcelable(PHOTO_SER_EXTRA_OPTIONS, photoSerOptions);
        intent.putExtra(PHOTO_SER_EXTRA_BUNDLE, bundle);
        startActivityForResult(intent, PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE);*/
        startFaceDectectorActivity();
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(true)
                .start(this, getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(instance).registerReceiver(requestBroadcastReceiver,
                intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.positive_button)
    public void sumbitRequest() {
        // Reset errors.
        mPhoneView.setError(null);
        mAddressView.setError(null);

        String phone = mPhoneView.getText().toString();
        String address = mAddressView.getText().toString();

        boolean cancel = false;
        View focusView = null;
        /*if (TextUtils.isEmpty(address)) {
            mAddressView.setError(getString(R.string.error_field_required));
            focusView = mAddressView;
            cancel = true;
        } else if (!searchCity) {
            cityAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = cityAutoCompleteTextView;
            cancel = true;
        } else if (!searchJob) {
            jobAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = jobAutoCompleteTextView;
            cancel = true;
        } else if (!searchEPS) {
            cancel = true;
            epsAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = epsAutoCompleteTextView;
        } else if (!searchAFP) {
            cancel = true;
            afpAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = afpAutoCompleteTextView;
        } else if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError("Digite un numero telefonico valido");
            focusView = mPhoneView;
            cancel = true;
        }*/

        if (cancel) {


            focusView.requestFocus();
        } else {

            String emergencyPhone = editEmergencyPhone.getText().toString();
            String emergencyContact = editEmergencyContact.getText().toString();
            UpdatePersonalInfo personal = new UpdatePersonalInfo(phone, /*jobId,*/ cityCode, address, emergencyContact, emergencyPhone, jobCode,
                    epsId, afpId);
            Gson gson = new Gson();
            final String json = gson.toJson(personal);
            logW(json);
            sendFormToServer(json);
        }
    }

    private void sendFormToServer(final String json) {
        showProgress(true);
        CrudIntentService.startRequestCRUD(getActivity(),
                Constantes.UPDATE_PERSONAL_INFO_URL + personal.PersonalCompanyInfoId, Request.Method.PUT, json, "", false);
    }

    @Override
    public void didClearText(View view) {
        switch (view.getId()) {
            case R.id.edit_eps:
                epsAutoCompleteTextView.setClearIconVisible(false);
                break;
            case R.id.edit_afp:
                afpAutoCompleteTextView.setClearIconVisible(false);
                break;
        }
    }

    public interface OnEditPersonalListener {
        void onCancelAction();

        void onApplyAction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEditPersonalListener) {
            mListener = (OnEditPersonalListener) context;
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
                        String urlCompany = Constantes.UPDATE_PHOTO_NEW_PERSONAL_URL + personal.PersonalCompanyInfoId;
                        CrudIntentService.startActionFormData(getActivity(), urlCompany,
                                Request.Method.PUT, params);

                        mImageUri = null;
                    } else {
                        if (mListener != null) {
                            mListener.onApplyAction();
                        }
                    }
                }
                break;
            case Constantes.SEND_REQUEST:
                logW(jsonObjStr);
                //fillSpinnerJob(jsonObjStr);
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
                }
                break;
        }
    }
}
