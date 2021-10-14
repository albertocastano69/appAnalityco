package co.tecno.sersoluciones.analityco.fragments.project;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;
import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.fragments.PolygonMapFragment;
import co.tecno.sersoluciones.analityco.geofences.GeofenceGoogleActivity;
import co.tecno.sersoluciones.analityco.models.BasicProject;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.EconomicActivity;
import co.tecno.sersoluciones.analityco.models.Project;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getBitmapFromUri;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getRealUriImage;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EditBasicFomFragmnetP extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        ClearebleAutoCompleteTextView.Listener {

    public static final String GEOFENCE = "GeoFence";

    private Unbinder unbinder;
    @BindView(R.id.icon_logo)
    MaterialIconView iconLogoView;
    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.edtt_name)
    EditText mNameView;
    @BindView(R.id.edtt_address)
    EditText mAddressView;
    @BindView(R.id.edit_city)
    ClearebleAutoCompleteTextView cityAutoCompleteTextView;

    private static final int REQUEST_IMAGE_SELECTOR = 199;
    /* @BindView(R.id.label_add_branch)
     TextView labelAddBranch;*/
    @BindView(R.id.frame_progress)
    FrameLayout mProgressView;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;
    @BindView(R.id.layout_image_map)
    RelativeLayout mapLayout;

    private Context instance;

    private ArrayList<BranchOffice> branchOffices;

    private ArrayList<BranchOffice> posBrachRemove;

    private Uri mImageUri;


    private String city;
    private boolean searchCity;
    private String cityCode;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    private OnMainFragmentInteractionListener mListener;
    private BasicProject project;
    private boolean logoUpload;
    private String jsonGeofence;
    private View view;
    private TextView tvGeofenceError;
    private MyPreferences preferences;


    public static EditBasicFomFragmnetP newInstance() {
        return new EditBasicFomFragmnetP();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String mJson = restoreJsonFromFile();
        project = new Gson().fromJson(mJson, BasicProject.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_form_project, container, false);
        unbinder = ButterKnife.bind(this, view);
        tvGeofenceError = view.findViewById(R.id.tvGeofenceError);
        preferences = new MyPreferences(getContext());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long maxgeoFence = 100;
                String profile = preferences.getProfile();
                User user = new Gson().fromJson(profile, User.class);
                for (int i = 0; i < user.Companies.size(); i++) {
                    if (user.Companies.get(0).Id.equals(project.CompanyInfoId)) {
                        maxgeoFence = user.Companies.get(i).GeofenceLimit;
                        break;
                    }
                }
                Intent geoFence = GeofenceGoogleActivity.newIntent(getContext(), maxgeoFence, project.GeoFenceJson);
                startActivityForResult(geoFence, 0);
                getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });
        init();
        fillForm(project);
        return view;
    }

    private String restoreJsonFromFile() {
        String fileName = "file_Json";
        FileInputStream fis;
        try {
            fis = getActivity().openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void fillForm(final BasicProject project) {

        if (project.Logo != null && !project.Logo.isEmpty()) {
            String url = Constantes.URL_IMAGES + project.Logo;

            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.not_project_1)
                    .error(R.drawable.not_project_1)
                    .into(iconLogoView);
        } else {
            iconLogoView.setImageResource(R.drawable.not_project_1);
        }
        mNameView.setText(project.Name);
        mAddressView.setText(project.Address);
        //Cursor cur=getCursorCityName(Integer.parseInt(project.CityCode));
        //cityAutoCompleteTextView.setText(cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME)));
        mapLayout.setVisibility(View.VISIBLE);
        if (project.GeoFence != null) {
            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_map, PolygonMapFragment.newInstance(project.GeoFenceJson))
                    .commit();
        }
        selectPosCitySpinner(project);
        jsonGeofence = project.GeoFenceJson;
    }

    private void selectPosCitySpinner(BasicProject project) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_CODE + " = ? )";
        String[] selectArgs = {project.CityCode};

        try {
            @SuppressLint("Recycle")
            Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                    DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
            if (cursor != null) {
                cursor.moveToFirst();
                city = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
//                state = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE));
                cityCode = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
                cityAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
                cityAutoCompleteTextView.setText(city);
                searchCity = true;
            }
        } catch (Exception e) {

        }

    }

    private void init() {
        instance = getActivity();
        logoUpload = false;
        mImageUri = null;

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

        branchOffices = new ArrayList<>();
        branchOffices.add(new BranchOffice());
        posBrachRemove = new ArrayList<>();


        ArrayList<EconomicActivity> economicActivities = new ArrayList<>();
        @SuppressLint("Recycle")
        Cursor cursor = instance.getContentResolver().query(Constantes.CONTENT_ECONOMIC_URI, null, null, null,
                DBHelper.ECONOMIC_TABLE_COLUMN_NAME);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                economicActivities.add(new EconomicActivity(cursor.getString(cursor.getColumnIndex(DBHelper.ECONOMIC_TABLE_COLUMN_CODE)),
                        cursor.getString(cursor.getColumnIndex(DBHelper.ECONOMIC_TABLE_COLUMN_NAME))));
                cursor.moveToNext();
            }
            cursor.close();
        }

//        FilteredArrayAdapter<EconomicActivity> adapterEconomic = new FilteredArrayAdapter<EconomicActivity>(instance,
//                R.layout.simple_spinner_item, economicActivities) {
//            @Override
//            protected boolean keepObject(EconomicActivity obj, String mask) {
//                mask = mask.toLowerCase();
//                return obj.Code.toLowerCase().startsWith(mask) || obj.Name.toLowerCase().startsWith(mask);
//            }
//
//            @NonNull
//            @Override
//            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//                if (convertView == null) {
//                    LayoutInflater inflater = LayoutInflater.from(getContext());
//                    convertView = inflater.inflate(R.layout.simple_spinner_item_2, parent, false);
//                }
//                EconomicActivity economicActivity = getItem(position);
//                TextView textView = convertView.findViewById(android.R.id.text1);
//                textView.setText(economicActivity.Code);
//                TextView textView2 = convertView.findViewById(android.R.id.text2);
//                textView2.setText(economicActivity.Name);
//                return convertView;
//            }
//        };

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
//                    state = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE));
                    cityCode = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
                    cityAutoCompleteTextView.setClearIconVisible(!city.isEmpty());
                    searchCity = true;
                }
            }
        });
        cityAutoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(cityAutoCompleteTextView, new TextWatcherAdapter.TextWatcherListener() {
            @Override
            public void onTextChanged(EditText view, String text) {
                if (view.getId() == R.id.edit_city) {
                    if (cityAutoCompleteTextView.isFocused()) {
                        //if (!text.isEmpty())
                        cityAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                        searchCity = false;
                    }
                }
            }
        }));
    }

    private Cursor getCursorCity(CharSequence str) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_NAME + " LIKE ? )";
        String[] selectArgs = {"%" + str + "%"};
        return instance.getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
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
        logE("resultCode: " + resultCode);
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == REQUEST_IMAGE_SELECTOR) {
                    Uri imageUri = data.getData();
                    if (imageUri == null) return;
                    Bitmap bitmap = getBitmapFromUri(Objects.requireNonNull(getActivity()).getApplicationContext(), imageUri);
                    mImageUri = getRealUriImage(getActivity().getApplicationContext(), bitmap, true);
                    iconLogoView.setImageURI(mImageUri);
                    fabRemove.setVisibility(View.VISIBLE);
//                        File compressedImage = new Compressor(instance)
//                                .setMaxWidth(640)
//                                .setMaxHeight(480)
//                                .setQuality(75)
//                                .setCompressFormat(Bitmap.CompressFormat.WEBP)
//                                .compressToFile(mCurrentPhoto);
                    logW("path:" + mImageUri.toString());

                }
                break;
        }
        if (resultCode == 0 && data != null) {
            jsonGeofence = data.getExtras().getString(GEOFENCE);
            // logW(jsonGeofence);
            tvGeofenceError.setError(null);

            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_map, PolygonMapFragment.newInstance(jsonGeofence))
                    .commit();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        showProgress(false);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (mImageUri != null && !mImageUri.toString().isEmpty()) {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("file", mImageUri.toString());
                    showProgress(true);
                    logoUpload = true;
                    String urlCompany = Constantes.UPDATE_LOGO_PROJECT_URL + project.Id;
                    CrudIntentService.startActionFormData(getActivity(), urlCompany,
                            Request.Method.PUT, params);

                    mImageUri = null;
                } else {
                    if (mListener != null) {
                        mListener.onApplyAction();
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

    @OnClick(R.id.positive_button)
    public void sumbitRequest() {
        // Reset errors.
        mNameView.setError(null);
        mAddressView.setError(null);

        byte geofenceLimit = 0;

        String name = mNameView.getText().toString();
        String address = mAddressView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(address)) {
            mAddressView.setError(getString(R.string.error_field_required));
            focusView = mAddressView;
            cancel = true;
        } else if (!searchCity) {
            cityAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = cityAutoCompleteTextView;
            cancel = true;
        }


        if (cancel) {


            focusView.requestFocus();
        } else {
/*
            String fromDateStr = "";
            String toDateStr = "";
            ValidityDate startDate = null;
            ValidityDate finishDate = null;
            try {
                if (project.StartDate != null && !project.StartDate.isEmpty()) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    startDate = format.parse(project.StartDate);
                    finishDate = format.parse(project.FinishDate);
                    fromDateStr = format.format(startDate);
                    toDateStr = format.format(finishDate);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
*/
            Gson gson = new Gson();
            /*if(companies==null){
                companies=new ArrayList<CompanyProject>();
            }*/
            Project project2 = new Project(name, jsonGeofence, project.StartDate, project.FinishDate, "", address,
                    cityCode, project.CompanyInfoId);

            final String json = gson.toJson(project2);
            sendFormToServer(json);
        }
    }

    private void sendFormToServer(final String json) {
        showProgress(true);
        CrudIntentService.startRequestCRUD(getActivity(),
                Constantes.LIST_PROJECTS_URL + project.Id, Request.Method.PUT, json, "", false);
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
}

