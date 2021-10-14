package co.tecno.sersoluciones.analityco.fragments.enrollment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.InfoUserScan;
import co.tecno.sersoluciones.analityco.models.Job;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.PersonalNew;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;
import co.tecno.sersoluciones.analityco.views.DelayAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EnrollmentNewPersona extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener {

    @BindView(R.id.icon_logo)
    MaterialIconView iconLogoView;
    @BindView(R.id.name_personal)
    TextView name_personal;
    @BindView(R.id.id_personal)
    TextView id_personal;
    @BindView(R.id.edit_city)
    ClearebleAutoCompleteTextView cityAutoCompleteTextView;
    @BindView(R.id.edit_Job)
    ClearebleAutoCompleteTextView jobAutoCompleteTextView;
    @BindView(R.id.edtt_address)
    EditText address;
    @BindView(R.id.edtt_email)
    EditText email;
    @BindView(R.id.edtt_phone)
    EditText phone;
    @BindView(R.id.edtt_nameContact)
    EditText nameContact;
    @BindView(R.id.edtt_phoneContact)
    EditText phoneContact;
    @BindView(R.id.spinner_contract)
    Spinner spinnerContract;
    private ProgressDialog progress;
    @BindView(R.id.create_button)
    Button sendPersonal;
    @BindView(R.id.cancel_button)
    Button cancelPersonal;
    @BindView(R.id.edit_position)
    DelayAutoCompleteTextView positionString;
    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.formNewUser)
    LinearLayout formNewUser;

    private boolean logoUpload = false;
    private int sendPosition = 0;
    private String idCompany;
    private String jobCode;
    private String cityId;
    private InfoUserScan user;

    private int process;

    private OnNewUser mListener;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private Context instance;
    private boolean searchCity;
    private boolean searchJob;
    private RequestBroadcastReceiver requestBroadcastReceiver;


    private ArrayList<Job> jobs;

    private String cityCode;
    private Uri mImageUri = null;
    private List<co.tecno.sersoluciones.analityco.models.Position> positionList;
    private String idContract = "";

    private String startDate;
    private String finishDate;
    private Personal personal;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri imageUri = Uri.parse(data.getStringExtra(URI_IMAGE_KEY));
                    //Log.d(TAG,"Entra aca jeje, pathImage: " + pathImage);
                    iconLogoView.setImageURI(imageUri);
                    mImageUri = imageUri;
                    break;
                default:
                    break;
            }
        }
    }

    public static EnrollmentNewPersona infoUser(InfoUserScan user, String idContract) {
        EnrollmentNewPersona fragment = new EnrollmentNewPersona();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, user);
        args.putString(ARG_PARAM2, idContract);
        args.putInt(ARG_PARAM3, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_personal_enrollment, container, false);
        ButterKnife.bind(this, view);
        instance = getActivity();
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        searchCity = false;
        searchJob = false;
        positionList = new ArrayList<co.tecno.sersoluciones.analityco.models.Position>();
        populateCityAutoComplete();
        populateJobAutoComplete();
        positionString.setAdapter(new PositionAutoCompleteAdapterNew(getActivity())); // 'this' is Activity instance
        positionString.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Position positionJob = (Position) adapterView.getItemAtPosition(position);
                positionString.setText(positionJob.Name);
            }
        });
        //updateListJobCursor();
        // CrudIntentService.startRequestCRUD(instance, "api/Job", Request.Method.GET, "", "", true);
        iconLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), FaceTrackerActivity.class);
                Bundle bundle = new Bundle();
                PhotoSerOptions photoSerOptions = new PhotoSerOptions();
                photoSerOptions.setDetectFace(true);
                bundle.putParcelable(PHOTO_SER_EXTRA_OPTIONS, photoSerOptions);
                intent.putExtra(PHOTO_SER_EXTRA_BUNDLE, bundle);
                startActivityForResult(intent, PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE);*/
                startFaceDectectorActivity();
            }
        });
        //card_view_detail.setVisibility(View.GONE);
        if (getArguments() != null) {
            user = (InfoUserScan) getArguments().getSerializable(ARG_PARAM1);
            process = getArguments().getInt(ARG_PARAM3);

            if (process == 0) {
                idContract = getArguments().getString(ARG_PARAM2);
            } else if (process == 1) {
                idCompany = getArguments().getString(ARG_PARAM2);
                view.findViewById(R.id.work).setVisibility(View.GONE);
            }
            id_personal.setText(user.dni + "");
            name_personal.setText(user.name + " " + user.lastname);
        }
        fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeLogo();
            }
        });

        return view;
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(true)
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
                    //String city = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
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

    @Override
    public void didClearText(View view) {
        switch (view.getId()) {
            case R.id.edit_city:
                cityAutoCompleteTextView.setClearIconVisible(false);
                searchCity = false;
                break;
            case R.id.edit_Job:
                jobAutoCompleteTextView.setClearIconVisible(false);
                searchJob = false;
                break;
        }
    }

    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edit_city:
                searchCity = false;
                break;
            case R.id.edit_Job:
                searchJob = false;
                break;
        }
    }

    @OnClick(R.id.cancel_button)
    public void cancelButton() {
        if (mListener != null) {
            mListener.onCancelNewUser();
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
        /*jobs = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<Job>>() {
                }.getType());*/

        String[] job_spinner = new String[jobs.size() + 1];
        job_spinner[0] = "TRABAJO";
        for (int i = 1; i <= jobs.size(); i++) {
            job_spinner[i] = jobs.get(i - 1).Name;
        }
        //spinnerJob.setAdapter(new CustomArrayAdapter(instance, job_spinner));
        /*CrudIntentService.startRequestCRUD(instance, "api/Position", Request.Method.GET, "", "", true);*/
    }

    private void updateListPosition(String jsonObjStr) {
        positionList = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<Position>>() {
                }.getType());
    }

    private void selectPosCitySpinner(InfoUserScan infoUser) {
        cityCode = infoUser.stateCode + "" + infoUser.cityCode;
    }

    @OnClick(R.id.create_button)
    public void positiveButton() {
        if (submit()) {

            selectPosCitySpinner(user);
            //idContract = contract.get(spinnerContract.getSelectedItemPosition() - 1).ContractId;
            //int idJob = jobs.get(spinnerJob.getSelectedItemPosition() - 1).Id;
            // String idJob = jobs.get(spinnerJob.getSelectedItemPosition() - 1).Code;
            //int idPosition = positions.get(spinnerPosition.getSelectedItemPosition() - 1).Id;

            PersonalNew newPersonal = new PersonalNew(startDate, finishDate, "CC", "" + user.dni,
                    user.name, user.lastname, phone.getText().toString(), cityCode, "0", user.rh,
                    user.sex, /*idJob,*/ positionString.getText().toString(), cityId, address.getText().toString(),
                    nameContact.getText().toString(), phoneContact.getText().toString(),
                    idContract, user.birthDate, idCompany, jobCode, 0, 0, user.DocumentRaw, 0);
            Gson gson = new Gson();
            String json = gson.toJson(newPersonal);
            formNewUser.setVisibility(View.GONE);
            progress = new ProgressDialog(getActivity());
            progress.setTitle("Creando personal");
            progress.show();
            if (process == 0) {
                CrudIntentService.startRequestCRUD(instance,
                        Constantes.NEW_PERSONAL_URL, Request.Method.POST, json, "", false);
            } else if (process == 1) {
                CrudIntentService.startRequestCRUD(instance,
                        Constantes.NEW_PERSONAL_COMPANY_URL, Request.Method.POST, json, "", false);
            }

        }
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        if (progress != null) {
            progress.dismiss();
        }
        Log.e("option", " new user " + option);
        //formNewUser.setVisibility(View.VISIBLE);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                String userInfo = new Gson().fromJson(jsonObjStr,
                        new TypeToken<InfoUserScan>() {
                        }.getType());
                personal = new Gson().fromJson(jsonObjStr,
                        new TypeToken<Personal>() {
                        }.getType());
                if (mImageUri != null && !mImageUri.toString().isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonObjStr);
                        int _id = jsonObject.getInt("PersonalCompanyInfoId");
                        Log.e("id_project", " " + _id);
                        logoUpload = true;
                        HashMap<String, String> params = new HashMap<>();
                        params.put("file", mImageUri.toString());
                        formNewUser.setVisibility(View.GONE);
                        String urlProject = Constantes.UPDATE_PHOTO_NEW_PERSONAL_URL + _id;
                        CrudIntentService.startActionFormData(getActivity(), urlProject,
                                Request.Method.PUT, params);
                        log("VALUE_JSON_BROADCAST: " + jsonObjStr);
                        mImageUri = null;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    if (mListener != null) {
                        mListener.onApplyNewUser(personal);
                    }
                }
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                if (logoUpload) {
                    if (mListener != null) {
                        mListener.onApplyNewUser(personal);
                    }
                }
                break;
            case Constantes.SEND_REQUEST:
                logW(jsonObjStr);
               /* if (sendPosition == 0) {
                    if (consult == 0) {
                        consult = 1;
                        updateListJob(jsonObjStr);
                    } else if (consult == 1)
                        updateListJob(jsonObjStr);
                } else*/
                if (sendPosition == 1) {
                    sendPosition = 0;
                    updateListPosition(jsonObjStr);
                }
                break;
            case Constantes.UPDATE_ADMIN_USERS:
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
                if (!url.equals("api/Contract/Positions")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle("No se asocio Personal")
                            .setMessage("Personal ya existente en otro contrato de este mismo proyecto, por favor seleccione otro personal.");
                    builder.create().show();
                }
                Log.e("badRiquest", "URL: " + url);

                break;
            case Constantes.TIME_OUT_REQUEST:
                break;
            case Constantes.REQUEST_NOT_FOUND:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
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

    interface OnNewUser {

        void onCancelNewUser();

        void onApplyNewUser(Personal infoUser);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EnrollmentNewPersona.OnNewUser) {
            mListener = (EnrollmentNewPersona.OnNewUser) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @SuppressLint("SetTextI18n")
    private boolean submit() {
        boolean send = true;
        View view = null;
       /* if (spinnerJob.getSelectedItemPosition() <= 0) {
            send = false;
            view = spinnerJob;
        } else*/
        if (!searchJob) {
            send = false;
            jobAutoCompleteTextView.setError(getString(R.string.error_field_required));
            view = jobAutoCompleteTextView;
        } else if (!searchCity) {
            send = false;
            cityAutoCompleteTextView.setError(getString(R.string.error_field_required));
            view = cityAutoCompleteTextView;
        } else if (TextUtils.isEmpty(address.getText().toString())) {
            send = false;
            address.setError(getString(R.string.error_field_required));
            view = address;
        } else if (TextUtils.isEmpty(email.getText().toString())) {
            send = false;
            email.setError(getString(R.string.error_field_required));
            view = email;
        } else if (TextUtils.isEmpty(phone.getText().toString())) {
            send = false;
            phone.setError(getString(R.string.error_field_required));
            view = phone;
        } else if (TextUtils.isEmpty(positionString.getText().toString()) && process == 0) {
            send = false;
            positionString.setError(getString(R.string.error_field_required));
            view = positionString;
        }
        /*else if (spinnerPosition.getSelectedItemPosition() <= 0) {
            send = false;
        }*/
        if (!send) {
            view.requestFocus();
            return false;
        }

        return send;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public class Position {
        public int Id;
        String Name;
        public int CompanyId;
        public String CompanyName;

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
            // GoogleBooksProtocol is a wrapper for the Google Books API
            //GoogleBooksProtocol protocol = new GoogleBooksProtocol(context, MAX_RESULTS);
            //return protocol.findBooks(bookTitle);
        }
    }

}
