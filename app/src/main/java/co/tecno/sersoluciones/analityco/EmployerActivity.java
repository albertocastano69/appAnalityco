package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import com.ramotion.foldingcell.FoldingCell;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.EmployerInstance;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.processBarcode;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EmployerActivity extends BaseActivity implements AdapterView.OnItemSelectedListener,
        ClearebleAutoCompleteTextView.Listener, TextWatcherAdapter.TextWatcherListener, RequestBroadcastReceiver.BroadcastListener {

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
    @BindView(R.id.login_progress)
    ProgressBar mProgressView;
    @BindView(R.id.view_separation)
    View viewSeparation;
    @BindView(R.id.submit_button)
    Button submitButton;
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
    @BindView(R.id.folding_cell)
    FoldingCell foldingCell;
    @BindView(R.id.icon_unfold)
    MaterialIconView iconUnfold;
    @BindView(R.id.edit_rol)
    EditText editRol;
    @BindView(R.id.body)
    LinearLayout body;
    @BindView(R.id.controls)
    LinearLayout controls;
    @BindView(R.id.fab_scan)
    FloatingActionButton fabScan;
    @BindView(R.id.scanBtnLayout)
    LinearLayout scanBtnLayout;
    private User user;
    @BindView(R.id.edit_afp)
    ClearebleAutoCompleteTextView afpAutoCompleteTextView;

    private Context instance;
    private boolean scanccok;

    private View mFormView;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    private int numberMaxNit;
    private boolean isNit;
    private String city;
    private boolean searchCity;
    private boolean searchAfp;
    private String cityCode;
    private Uri mImageUri;
    private boolean logoUpload;
    private String employerId;
    private int afpId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }*/
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("EMPLEADOR");

        instance = this;
        mFormView = findViewById(R.id.form);

        final MyPreferences preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        init();
        fabScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(EmployerActivity.this, BarcodeDecodeSerActivity.class), 0);
            }
        });

        populateSecRefAutoComplete(2, afpAutoCompleteTextView);

    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_employer);
    }

    private void init() {
        String[] docTypeArray = new String[]{"Tipo", "CC", "NIT"};
        mDocTypeSpinner.setAdapter(new CustomArrayAdapter(instance, docTypeArray));
        mDocTypeSpinner.setOnItemSelectedListener(this);

        mNumberVerifyView.addTextChangedListener(new TextWatcherAdapter(mNumberVerifyView, this));
        mDocNumberView.addTextChangedListener(new TextWatcherAdapter(mDocNumberView, this));
        mDocNumberView.setEnabled(false);

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

        populateCityAutoComplete();
        numberMaxNit = 5;
        isNit = false;
        cityCode = "";
        searchCity = false;
        searchAfp = false;
        mImageUri = null;
        logoUpload = false;
        employerId = "";
        fabRemove.setVisibility(View.GONE);

        foldingCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!foldingCell.isUnfolded())
                    foldingCell.unfold(false);
            }
        });
    }

    @OnClick(R.id.icon_unfold)
    public void unFold() {
        foldingCell.fold(false);
    }


    @OnClick(R.id.icon_logo)
    public void dispatchPhotoSelectionIntent() {
       /* Intent intent = new Intent(instance, FaceTrackerActivity.class);
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
                .start(this);
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
                    //String state = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE));
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
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
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
                        viewSeparation.setVisibility(View.GONE);
                        numberMaxNit = 5;
                        isNit = false;
                        mDocNumberView.setEnabled(false);
                        mNameView.setEnabled(false);
                        if (!scanccok) {
                            startActivityForResult(new Intent(EmployerActivity.this, BarcodeDecodeSerActivity.class), 0);
                        }

                    } else {
                        scanccok = false;
                        mDocNumberView.setEnabled(true);
                        mNameView.setEnabled(false);
                        scanBtnLayout.setVisibility(View.GONE);
                        setEditTextMaxLength(mDocNumberView, 9);
                        layoutVerification.setVisibility(View.VISIBLE);
                        mNumberVerifyView.setVisibility(View.VISIBLE);
                        viewSeparation.setVisibility(View.VISIBLE);
                        numberMaxNit = 9;
                        isNit = true;
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
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void setEditTextMaxLength(EditText editText, int length) {
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        editText.setFilters(FilterArray);
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
            case R.id.edit_afp:
                if (afpAutoCompleteTextView.isFocused()) {
                    //if (!text.isEmpty())
                    afpAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchAfp = false;
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
                        } else {
                            mNumberVerifyView.setEnabled(false);
                        }
                    }
                }
                break;
            case R.id.number_verification:
                logW("mNumberVerifyView: " + text);
                if (mNumberVerifyView.isFocused()) {
                    if (!MetodosPublicos.validateDigitVerify(text, mDocNumberView)) {
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

    private boolean isDocNumberValid(String text) {
        return text.length() >= numberMaxNit;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("Esta seguro de volver atrás y no completar el registro?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EmployerActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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

        mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void removeLogo(View view) {
        Drawable drawable = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
                .setColor(Color.GRAY)
                .build();
        iconLogoView.setImageDrawable(drawable);
        fabRemove.setVisibility(View.GONE);
    }

    /**
     * Metodo que recibe la respuesta cuando la imagen es tomada
     *
     * @param requestCode param1
     * @param resultCode  param2
     * @param data        param3
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

    public void back(View view) {
        this.onBackPressed();
    }


    @OnClick(R.id.submit_button)
    public void submitRequest() {
        // Reset errors.
        mNameView.setError(null);
        mDocNumberView.setError(null);
        TextView mDocTypeView = mDocTypeSpinner.getSelectedView().findViewById(android.R.id.text1);
        mDocTypeView.setError(null);
        String docType = tvDocTypeError.getText().toString();

        String name = mNameView.getText().toString();
        String docNumber = mDocNumberView.getText().toString();
        String rol = editRol.getText().toString();

        String web = mWebView.getText().toString();
        String numVerify = mNumberVerifyView.getText().toString();
        String phone = mPhoneView.getText().toString();
        String address = mAddressView.getText().toString();
        String email = mEmailView.getText().toString();

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
        } else if (isNit && !MetodosPublicos.validateDigitVerify(numVerify, mDocNumberView)) {
            mNumberVerifyView.setError("Digito de verificación incorrecto");
            focusView = mNumberVerifyView;
            cancel = true;
        } else if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(rol)) {
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
        }
        if (!searchAfp) {
            selectPosSecRefSpinner(2, afpAutoCompleteTextView);
        }

        if (cancel) {


            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            if (isNit)
                docNumber = String.format("%s-%s", docNumber, numVerify);
            EmployerInstance employer = new EmployerInstance(docType, docNumber, name, address, cityCode, web, phone, email, rol);
            employer.CompanyInfoParentId = user.CompanyId;
            employer.ArlId = afpId;
            Gson gson = new Gson();
            final String json = gson.toJson(employer);
            sendFormToServer(json);
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

    private void sendFormToServer(final String json) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage("Esta seguro de crear este empleador?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress(true);
                        CrudIntentService.startRequestCRUD(EmployerActivity.this,
                                Constantes.CREATE_EMPLOYER_URL, Request.Method.POST, json, "", false);

                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        showProgress(false);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                try {
                    JSONObject jsonObject = new JSONObject(jsonObjStr);
                    employerId = jsonObject.getString("Id");
                    if (mImageUri != null && !mImageUri.toString().isEmpty()) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("file", mImageUri.toString());
                        showProgress(true);
                        logoUpload = true;
                        CrudIntentService.startActionFormData(EmployerActivity.this,
                                Constantes.UPDATE_LOGO_EMPLOYER_URL + employerId,
                                Request.Method.PUT, params);
                        log("VALUE_JSON_BROADCAST: " + jsonObjStr);
                        mImageUri = null;
                    } else {
                        msgSuccess();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Constantes.BAD_REQUEST:
                if (url.equals(Constantes.RUES_URL)) enabledFields();
                try {
                    if (jsonObjStr != null)
                        MetodosPublicos.alertDialog(EmployerActivity.this, new JSONObject(jsonObjStr).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case Constantes.TIME_OUT_REQUEST:
                MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                if (logoUpload)
                    msgSuccess();
                else {
                    try {
                        String name = new JSONObject(jsonObjStr).getJSONArray("rows").getJSONObject(0).getString("razon_social");
                        mNameView.setText(name);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constantes.SERVER_ERROR:
            case Constantes.REQUEST_NOT_FOUND:
                if (url.equals(Constantes.RUES_URL)) enabledFields();
                break;
        }
    }

    private void msgSuccess() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage("Empleador creado satisfactoriamente")
                .setCancelable(false)
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
        builder.create().show();
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
            MetodosPublicos.alertDialog(EmployerActivity.this, "No se pudo procesar el codigo de barras, intente de nuevo.");
        }
    }

    private void populateSecRefAutoComplete(final int option, final ClearebleAutoCompleteTextView autoCompleteTextView) {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(this, R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.SECURITY_REFERENCE_COLUMN_NAME, DBHelper.SECURITY_REFERENCE_COLUMN_DESCRIPTION},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);

        autoCompleteTextView.setAdapter(mAdapterCust);

        mAdapterCust.setFilterQueryProvider(str -> getCursorSecRef(str, option));

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
            }
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(autoCompleteTextView, this));
        selectPosSecRefSpinner(option, autoCompleteTextView);
    }

    private Cursor getCursorSecRef(CharSequence str, int option) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_NAME + " LIKE ?  OR "
                + DBHelper.SECURITY_REFERENCE_COLUMN_DESCRIPTION + " LIKE ?) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {"%" + str + "%", "%" + str + "%", String.valueOf(option)};
        return getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
    }

    @SuppressLint("SetTextI18n")
    private void selectPosSecRefSpinner(final int option, final ClearebleAutoCompleteTextView autoCompleteTextView) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_CODE + " = ? ) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {String.format("%s", option == 0 ? "EPS00000" : "ARL00000"), String.valueOf(2)};
        @SuppressLint("Recycle")
        Cursor cursor = getContentResolver().query(Constantes.CONTENT_SEC_REFS_URI, null, select, selectArgs,
                DBHelper.SECURITY_REFERENCE_COLUMN_NAME);
        if (cursor != null) {
            cursor.moveToFirst();
            String name = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME));
            int id = cursor.getInt(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID));
            autoCompleteTextView.setText(name);
            autoCompleteTextView.setClearIconVisible(!name.isEmpty());
            afpId = id;
        }
    }

    private void enabledFields() {
        mNameView.setEnabled(true);
        Toast.makeText(instance, getString(R.string.rues_not_response), Toast.LENGTH_SHORT).show();
    }
}
