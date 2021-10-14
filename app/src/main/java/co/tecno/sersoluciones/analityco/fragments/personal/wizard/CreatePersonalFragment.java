package co.tecno.sersoluciones.analityco.fragments.personal.wizard;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.com.sersoluciones.facedetectorser.utilities.DebugLog;
import co.tecno.sersoluciones.analityco.JoinPersonalWizardActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.Job;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.PersonalNew;
import co.tecno.sersoluciones.analityco.models.UpdatePersonalInfo;
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
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class CreatePersonalFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener {

    @BindView(R.id.icon_logo)
    MaterialIconView iconLogoView;
    @BindView(R.id.icon_edit_main_form)
    MaterialIconView editIconView;

    @BindView(R.id.edit_city)
    ClearebleAutoCompleteTextView cityAutoCompleteTextView;
    @BindView(R.id.edit_Job)
    ClearebleAutoCompleteTextView jobAutoCompleteTextView;
    @BindView(R.id.edit_eps)
    ClearebleAutoCompleteTextView epsAutoCompleteTextView;
    @BindView(R.id.edit_afp)
    ClearebleAutoCompleteTextView afpAutoCompleteTextView;
    @BindView(R.id.edtt_address)
    EditText address;

    @BindView(R.id.edtt_phone)
    EditText phone;
    @BindView(R.id.edtt_nameContact)
    EditText nameContact;
    @BindView(R.id.edtt_phoneContact)
    EditText phoneContact;

    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.text_rh)
    TextView textViewRH;
    @BindView(R.id.text_sex)
    TextView textViewSex;
    @BindView(R.id.controls_edit)
    LinearLayout controlsEdit;
    @BindView(R.id.cancel_button)
    Button cancelBtn;
    @BindView(R.id.submit_button)
    Button submitBtn;
    @BindView(R.id.border_bottom)
    View borderBottom;


    private RequestBroadcastReceiver requestBroadcastReceiver;
    private String companyId;
    private String jobCode;
    private String cityId;
    private int process; //1 creacion 2 edicion 3 detalles

    private OnNewUserListener mListener;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private boolean searchCity;
    private boolean searchJob;

    private Uri mImageUri = null;
    private ArrayList<Job> jobs;

    private String cityCode;
    private String contractId;
    private DecodeBarcode.InfoUser infoUser;
    private Personal personal;
    private boolean searchEPS;
    private boolean searchAFP;
    private int epsId;
    private int afpId;
    private boolean flagPerson;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("ON CREATE CreatePersonalFragment");
        process = 3;
        flagPerson = false;
        searchCity = false;
        searchJob = false;
        searchEPS = false;
        searchAFP = false;
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        if (getArguments() != null) {
            infoUser = (DecodeBarcode.InfoUser) getArguments().getSerializable(ARG_PARAM1);
            this.process = getArguments().getInt(ARG_PARAM3);
            this.companyId = getArguments().getString(ARG_PARAM2);
            this.contractId = getArguments().getString(ARG_PARAM2);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_POST);
        intentFilter.addAction(CRUDService.ACTION_REQUEST_PUT);
        intentFilter.addAction(CRUDService.ACTION_REQUEST_FORM_DATA);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_personal, container, false);
        ButterKnife.bind(this, view);
        controlsEdit.setVisibility(View.GONE);
        borderBottom.setVisibility(View.GONE);
        populateCityAutoComplete();
        populateJobAutoComplete();
        populateSecRefAutoComplete(0, epsAutoCompleteTextView);
        populateSecRefAutoComplete(1, afpAutoCompleteTextView);

        iconLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFaceDectectorActivity();
            }
        });
        fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLogo();
            }
        });

        if (mListener != null)
            mListener.choiceEditUser(true);

        return view;
    }

    @OnClick(R.id.icon_edit_main_form)
    public void editForm() {
        enableFields(true);
        process = 2;
        editIconView.setVisibility(View.GONE);
        controlsEdit.setVisibility(View.VISIBLE);
        borderBottom.setVisibility(View.VISIBLE);
        if (mListener != null)
            mListener.choiceEditUser(false);
    }

    @OnClick(R.id.cancel_button)
    public void cancel() {
        if (process == 1) {
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
        } else {
            populateJobAutoComplete();
            populateSecRefAutoComplete(0, epsAutoCompleteTextView);
            populateSecRefAutoComplete(1, afpAutoCompleteTextView);
            enableFields(false);
            process = 3;
            editIconView.setVisibility(View.VISIBLE);
            controlsEdit.setVisibility(View.GONE);
            borderBottom.setVisibility(View.GONE);
            if (mListener != null)
                mListener.choiceEditUser(true);
        }
    }

    @OnClick(R.id.submit_button)
    public void saveData() {
        submit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri uriImage = Uri.parse(data.getStringExtra(URI_IMAGE_KEY));
                    logW("uriImage " + uriImage);

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
        }
    }

    public void setData(DecodeBarcode.InfoUser infoUser, String companyId) {
        cleanForm();
        enableFields(true);
        flagPerson = false;
        this.infoUser = infoUser;
        this.companyId = companyId;
        this.process = 1; //proceso de creacion
        textViewRH.setText(infoUser.rh);
        textViewSex.setText(infoUser.sex);

        editIconView.setVisibility(View.GONE);
        controlsEdit.setVisibility(View.VISIBLE);
        borderBottom.setVisibility(View.VISIBLE);

        if (mListener != null)
            mListener.choiceEditUser(false);
    }

    @SuppressLint("SetTextI18n")
    public void fillData(DecodeBarcode.InfoUser infoUser, Personal personal, String companyId) {
        cleanForm();

        editIconView.setVisibility(View.VISIBLE);
        controlsEdit.setVisibility(View.GONE);
        borderBottom.setVisibility(View.GONE);

        if (mListener != null)
            mListener.choiceEditUser(true);

        flagPerson = false;
        this.infoUser = infoUser;
        this.companyId = companyId;
        this.process = 3;
        this.personal = personal;
        //logW("rh: " + infoUser.rh + ", sex: " + infoUser.sex);
        address.setText(String.valueOf(personal.Address));
        phone.setText(String.valueOf(personal.PhoneNumber));
        nameContact.setText(String.valueOf(personal.EmergencyContact));
        phoneContact.setText(String.valueOf(personal.EmergencyContactPhone));
        textViewRH.setText(infoUser.rh);
        textViewSex.setText(infoUser.sex);
        if (infoUser.rh == null || infoUser.rh.isEmpty())
            textViewRH.setText(personal.RH);
        if (infoUser.sex == null || infoUser.sex.isEmpty())
            textViewSex.setText(personal.Sex);

        if (personal.Photo != null && !personal.Photo.isEmpty()) {
            String url = Constantes.URL_IMAGES + personal.Photo;

            Picasso.get().load(url)
                    .resize(0, 250)
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.profile_dummy)
                    .error(R.drawable.profile_dummy)
                    .into(iconLogoView);
        }
        if (personal.CityCode != null && !personal.CityCode.isEmpty()) {
            selectPosCitySpinner(personal);
            searchCity = false;
        }
        selectPosJobSpinner(personal);
        selectPosSecRefSpinner(personal, 0, epsAutoCompleteTextView);
        selectPosSecRefSpinner(personal, 1, afpAutoCompleteTextView);
        cityAutoCompleteTextView.setText(personal.CityName);
        jobAutoCompleteTextView.setText("(" + personal.JobCode + ") " + personal.JobName);
        enableFields(false);
        epsId = personal.EpsId;
        afpId = personal.AfpId;
        searchJob = true;
        searchEPS = true;
        searchAFP = true;
    }

    private void enableFields(boolean enable) {
        address.setEnabled(enable);
        phone.setEnabled(enable);
        nameContact.setEnabled(enable);
        phoneContact.setEnabled(enable);
        jobAutoCompleteTextView.setEnabled(enable);
        cityAutoCompleteTextView.setEnabled(enable);
        iconLogoView.setEnabled(enable);
        epsAutoCompleteTextView.setEnabled(enable);
        afpAutoCompleteTextView.setEnabled(enable);
    }

    private void selectPosCitySpinner(Personal personal) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_CODE + " = ? )";
        String[] selectArgs = {personal.CityCode};
        //noinspection ConstantConditions
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String city = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
            cityId = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
            cityAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
            searchCity = true;
        }
    }

    @SuppressLint("SetTextI18n")
    private void selectPosSecRefSpinner(Personal personal, final int option, final ClearebleAutoCompleteTextView autoCompleteTextView) {
        String select = "(" + DBHelper.SECURITY_REFERENCE_COLUMN_CODE + " = ? ) and "
                + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = ?";
        String[] selectArgs = {String.format("%s", option == 0 ? personal.EpsCode : personal.AfpCode), String.valueOf(option)};
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

    private void selectPosJobSpinner(Personal personal) {
        DebugLog.logW("JobCode " + personal.JobCode);
        String select = "(" + DBHelper.WORK_TABLE_COLUMN_CODE + " = ? )";
        String[] selectArgs = {personal.JobCode};
        //noinspection ConstantConditions
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_WORK_URI, null, select, selectArgs,
                DBHelper.WORK_TABLE_COLUMN_NAME);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String jobName = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME));
            jobCode = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_CODE));
            jobAutoCompleteTextView.setClearIconVisible(!jobName.isEmpty());
            searchJob = true;
            log("entra aca selectPosJobSpinner");
        }
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(true)
                .setSaveGalery(true)
                .start(this, getActivity());
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
                    //cityCode = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
                    cityId = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
                    cityAutoCompleteTextView.setClearIconVisible(true);
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
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(getContext(), R.layout.simple_spinner_item_3, null,
                new String[]{DBHelper.SECURITY_REFERENCE_COLUMN_CODE, DBHelper.SECURITY_REFERENCE_COLUMN_NAME},
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

    private void removeLogo() {
        Drawable drawable = MaterialDrawableBuilder.with(getContext()) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
                .setColor(Color.GRAY)
                .build();
        iconLogoView.setImageDrawable(drawable);
        fabRemove.setVisibility(View.GONE);
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
            logW("NAME " + name);
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


    @Override
    public void didClearText(View view) {
        switch (view.getId()) {
            case R.id.edit_city:
                cityAutoCompleteTextView.setClearIconVisible(false);
                cityAutoCompleteTextView.requestFocus();
                searchCity = false;
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
    public void onTextChanged(EditText view, String text) {
        ((ClearebleAutoCompleteTextView) view).setClearIconVisible(!text.isEmpty());

        switch (view.getId()) {
            case R.id.edit_city:
                searchCity = false;
                break;
            case R.id.edit_Job:
                searchJob = false;
                break;
            case R.id.edit_eps:
                searchEPS = false;
                break;
            case R.id.edit_afp:
                searchAFP = false;
                break;
        }
    }

    @SuppressWarnings("unused")
    public void updateListJobCursor() {
        Cursor cursorJobs = getActivity().getContentResolver().query(Constantes.CONTENT_WORK_URI, null, null, null, null);
        jobs = new ArrayList<>();
        for (cursorJobs.moveToFirst(); !cursorJobs.isAfterLast(); cursorJobs.moveToNext()) {
            int id = cursorJobs.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_SERVER_ID);
            int name = cursorJobs.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME);
            int code = cursorJobs.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_CODE);
            String nameJob = cursorJobs.getString(name);
            String codeJob = cursorJobs.getString(code);
            int idJob = cursorJobs.getInt(id);
            jobs.add(new Job(idJob, nameJob, codeJob));
        }
        updateListJob();
    }

    private void updateListJob() {
        String[] jobSpinner = new String[jobs.size() + 1];
        jobSpinner[0] = "TRABAJO";
        for (int i = 1; i <= jobs.size(); i++) {
            jobSpinner[i] = jobs.get(i - 1).Name;
        }
    }

    private void selectPosCitySpinner(DecodeBarcode.InfoUser infoUser) {
        cityCode = infoUser.stateCode + "" + infoUser.cityCode;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    public interface OnNewUserListener {
        void onApplyNewUser(Personal personal, Uri mImagePath, boolean create);

        void choiceEditUser(boolean option);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNewUserListener) {
            mListener = (OnNewUserListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNewUserListener");
        }
    }

    @SuppressLint("SetTextI18n")
    public void submit() {
        boolean send = true;
        View view = null;
        if (process == 1 && (mImageUri == null || mImageUri.toString().isEmpty())) {
            MetodosPublicos.alertDialog(getActivity(), "La foto es obligatoria");
            send = false;
            view = iconLogoView;
        } else if (!searchJob) {
            selectPosJobSpinner();
            /*send = false;
            jobAutoCompleteTextView.setError(getString(R.string.error_field_required));
            view = jobAutoCompleteTextView;*/
        }
//        } else if (!searchCity) {
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
        /*else if (TextUtils.isEmpty(address.getText().toString())) {
            send = false;
            address.setError(getString(R.string.error_field_required));
            view = address;
        } else if (TextUtils.isEmpty(phone.getText().toString())) {
            send = false;
            phone.setError(getString(R.string.error_field_required));
            view = phone;
        }*/
        if (!send) {
            view.requestFocus();
        } else {
            sendToServer();
        }

    }

    private void cleanForm() {
        mImageUri = null;
        flagPerson = false;
        searchCity = false;
        searchJob = false;
        searchEPS = false;
        searchAFP = false;
        iconLogoView.setImageResource(R.drawable.profile_dummy);
        cityId = null;
        phone.setText("");
        address.setText("");
        nameContact.setText("");
        phoneContact.setText("");
        populateCityAutoComplete();
        populateJobAutoComplete();
        populateSecRefAutoComplete(0, epsAutoCompleteTextView);
        populateSecRefAutoComplete(1, afpAutoCompleteTextView);
    }

    private void sendToServer() {

        if (process == 1) {
            flagPerson = true;
            if (getActivity() instanceof JoinPersonalWizardActivity)
                ((JoinPersonalWizardActivity) getActivity()).showProgress(true);
            cityCode = infoUser.stateCode + "" + infoUser.cityCode;
//            selectPosCitySpinner(infoUser);
            PersonalNew newPersonal = new PersonalNew("", "", "CC", String.valueOf(infoUser.dni),
                    infoUser.name, infoUser.lastname, phone.getText().toString(), cityCode, "0", infoUser.rh,
                    infoUser.sex, /*idJob,*/ "", cityId, address.getText().toString(),
                    nameContact.getText().toString(), phoneContact.getText().toString(),
                    contractId, String.valueOf(infoUser.birthDate), companyId, jobCode, epsId, afpId, infoUser.DocumentRaw, 0);

            String jsonPersonal = new Gson().toJson(newPersonal);
            CRUDService.startRequest(getContext(),
                    Constantes.NEW_PERSONAL_COMPANY_URL, Request.Method.POST, jsonPersonal);
        } else if (process == 2) {

            /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("Atencion")
                    .setMessage("¿Esta seguro de editar este empleado?")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {*/

            if (getActivity() instanceof JoinPersonalWizardActivity)
                ((JoinPersonalWizardActivity) getActivity()).showProgress(true);
            UpdatePersonalInfo updatePersonalInfo = new UpdatePersonalInfo(phone.getText().toString(), cityId, address.getText().toString(),
                    nameContact.getText().toString(), phoneContact.getText().toString(), jobCode, epsId, afpId);
            String jsonPersonal = new Gson().toJson(updatePersonalInfo);
            logW(jsonPersonal);
            flagPerson = true;
            CRUDService.startRequest(getContext(),
                    Constantes.UPDATE_PERSONAL_INFO_URL + personal.PersonalCompanyInfoId, Request.Method.PUT, jsonPersonal);
                      /*  }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //enableFields(false);
                            cancel();
                            //mListener.onApplyNewUser(personal);
                        }
                    });
            builder.create().show();*/
        } else if (process == 3) {
            if (mListener != null) {
                enableFields(false);

                mListener.onApplyNewUser(personal, null, false);
            }
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
    public void onStringResult(String action, int option, String response, String url) {

        switch (action) {
            case CRUDService.ACTION_REQUEST_GET:
                break;
            case CRUDService.ACTION_REQUEST_POST:
            case CRUDService.ACTION_REQUEST_PUT:

                if (flagPerson)
                    processRequestALL(option, response);
                break;
            case CRUDService.ACTION_REQUEST_DELETE:
            case CRUDService.ACTION_REQUEST_FORM_DATA:
                if (flagPerson)
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
                    process = 3;
                    cancel();
                    enableFields(false);
                    if (getActivity() instanceof JoinPersonalWizardActivity)
                        ((JoinPersonalWizardActivity) getActivity()).showProgressLayout(false);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String photo = jsonObject.getString("Photo");
                        logW("photo: " + photo);
                        personal.Photo = photo;
                        if (mListener != null)
                            mListener.onApplyNewUser(personal, mImageUri, true);
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
//                        int _id = jsonObject.getInt("PersonalCompanyInfoId");
//                        logW("PersonalCompanyInfoId: " + _id);
//                        HashMap<String, String> params = new HashMap<>();
//                        params.put("file", mImagePath);
//                        CRUDService.startRequest(getContext(),
//                                Constantes.UPDATE_PHOTO_NEW_PERSONAL_URL + _id, Request.Method.PUT, params);
//                        if (getActivity() instanceof PersonalWizardActivity) {
//                            ((PersonalWizardActivity) getActivity()).showProgress(false);
//                            ((PersonalWizardActivity) getActivity()).showProgressLayout(true);
//                        } else if (getActivity() instanceof JoinPersonalWizardActivity) {
//                            ((JoinPersonalWizardActivity) getActivity()).showProgress(false);
//                            ((JoinPersonalWizardActivity) getActivity()).showProgressLayout(true);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                } else {
                if (mListener != null) {
                    process = 3;
                    cancel();
                    enableFields(false);

                    mListener.onApplyNewUser(personal, mImageUri, true);

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
                if (getActivity() instanceof JoinPersonalWizardActivity)
                    ((JoinPersonalWizardActivity) getActivity()).showProgress(false);
                Toast.makeText(getActivity(), getActivity().getString(R.string.error_internet_toast), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
