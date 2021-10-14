package co.tecno.sersoluciones.analityco.steps;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.google.gson.Gson;


import net.steamcrafted.materialiconlib.MaterialIconView;

import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.ProjectActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.fragments.PolygonMapFragment;
import co.tecno.sersoluciones.analityco.geofences.GeofenceGoogleActivity;
import co.tecno.sersoluciones.analityco.models.Project;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getBitmapFromUri;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getRealUriImage;

/**
 * Created by Ser Soluciones SAS on 28/09/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ProjectStep1 extends Fragment implements TextWatcherAdapter.TextWatcherListener,
        ClearebleAutoCompleteTextView.Listener {
    private static final int REQUEST_IMAGE_SELECTOR = 199;

    private Unbinder unbinder;
    private EditText mNameView;
    private MaterialIconView iconLogoView;

    private View view;

    private boolean searchCity;
    private String cityId;
    private TextView tvGeofenceError;
    private FloatingActionButton fabRemove;
    private ClearebleAutoCompleteTextView cityAutoCompleteTextView;

    private Uri mImageUri;
    private EditText mAddressView;
    private String cityCode;
    private String jsonGeofence;
    private FloatingActionButton fab;
    private long maxgeoFence = 0;

    //Set your layout here
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.project_step1, container, false);
        unbinder = ButterKnife.bind(this, v);
        view = v;
        iconLogoView = v.findViewById(R.id.icon_logo);
        fabRemove = v.findViewById(R.id.fab_remove);
        mNameView = v.findViewById(R.id.edtt_name);
        mAddressView = v.findViewById(R.id.edtt_address);
        cityAutoCompleteTextView = v.findViewById(R.id.edit_city);
        fab = v.findViewById(R.id.fab);

        tvGeofenceError = v.findViewById(R.id.tvGeofenceError);
        maxgeoFence = ((ProjectActivity) getActivity()).maxgeoFence;
        fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (maxgeoFence > 0) {
                Intent geoFence = GeofenceGoogleActivity.newIntent(getContext(), maxgeoFence);
                startActivityForResult(geoFence, 0);
                getActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Debe seleccionar una compañia")
                        .setPositiveButton("ok", null);
                builder.create().show();
            }
        });

        fabRemove.setVisibility(View.GONE);
        iconLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchPhotoSelectionIntent();
            }
        });

        populateCityAutoComplete();

        return v;
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
                    String city = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
                    cityId = cur.getString(cur.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE));
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
        return getContext().getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
    }

    @Override
    public void didClearText(View view) {
        cityAutoCompleteTextView.setClearIconVisible(false);
        searchCity = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE_SELECTOR) {

            Uri imageUri = data.getData();
            if (imageUri == null) return;
            Bitmap bitmap = getBitmapFromUri(Objects.requireNonNull(getActivity()).getApplicationContext(), imageUri);
            mImageUri = getRealUriImage(getActivity().getApplicationContext(), bitmap, true);

            iconLogoView.setImageURI(mImageUri);
            fabRemove.setVisibility(View.VISIBLE);

            return;
        }
        if (resultCode == 0 && data != null) {
            jsonGeofence = data.getExtras().getString(GeofenceGoogleActivity.GEOFENCE);
            logW(jsonGeofence);
            Objects.requireNonNull(getActivity()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_map, PolygonMapFragment.newInstance(jsonGeofence))
                    .commit();
            view.findViewById(R.id.layout_image_screen).setVisibility(View.VISIBLE);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                //onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void submitRequest() {


        // Reset errors.
        // mProjectNumberView.setError(null);
        mNameView.setError(null);
        mAddressView.setError(null);
        //Wire the layout to the step
        String nameProject = mNameView.getText().toString();
        String address = mAddressView.getText().toString();
        // String projectNum = mProjectNumberView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nameProject)) {
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
        } else if (TextUtils.isEmpty(jsonGeofence) || jsonGeofence==null) {
            /*fab.requestFocus();
            tvGeofenceError.setVisibility(View.VISIBLE);
            tvGeofenceError.setError("De click para crear una geocerca");
            focusView = tvGeofenceError;
            cancel = true;*/
            jsonGeofence="";
        }

        if (cancel) {
            focusView.requestFocus();
            //notifyIncomplete();
            return;
        }
        //notifyCompleted();
        if (cityId != null) {
            cityCode = cityId;
        }
        String jsonGeo = jsonGeofence;
        log("cityId: " + cityCode + " jsonGeo: " + jsonGeo);

        Project project = new Project(nameProject, jsonGeofence, "", "", "", address,
                String.valueOf(cityId), ((ProjectActivity) getActivity()).idCompany);
        if (mImageUri != null)
            project.Logo = mImageUri.toString();

        project.CompanyId = ((ProjectActivity) getActivity()).idCompany;
        ((ProjectActivity) getActivity()).setProject(project);
        ((ProjectActivity) getActivity()).nextPage(1);
        final String json = new Gson().toJson(project);
        logW(json);

    }


    //A system-based view to select photos.
    private void dispatchPhotoSelectionIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECTOR);
    }


    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edit_city:
                if (cityAutoCompleteTextView.isFocused()) {
                    //if (!text.isEmpty())
                    Log.e("ontextchange", text);
                    cityAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchCity = false;
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}