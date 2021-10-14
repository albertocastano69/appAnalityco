package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.adapters.BranchOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.adapters.CustomAutocompleteAdapter;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.contracts.SpinnerAdapterImage;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.Contract;
import co.tecno.sersoluciones.analityco.models.ContractType;
import co.tecno.sersoluciones.analityco.models.Image;
import co.tecno.sersoluciones.analityco.models.PersonalType;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getBitmapFromUri;
import static co.com.sersoluciones.facedetectorser.utilities.FileUtilsKt.getRealUriImage;

public class ContractActivity extends BaseActivity implements
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener,
        RequestBroadcastReceiver.BroadcastListener, BranchOfficesRecyclerView.OnBranchOfficeInteractionListener {

    private Unbinder unbinder;
    private RequestBroadcastReceiver getJSONBroadcastReceiver;

    @BindView(R.id.spinner_employee_type)
    Spinner contractType;
    @BindView(R.id.spinner_image)
    Spinner spinnerImages;


    private ClearebleAutoCompleteTextView cityAutoCompleteTextView;
    private EditText mReviewView;
    private EditText mNumContractView;
    @BindView(R.id.layout_spinner)
    LinearLayout layouySpinner;
    @BindView(R.id.employerSelected)
    RelativeLayout employerSelected;
    @BindView(R.id.file_contract)
    LinearLayout fileContract;
    @BindView(R.id.name_contract)
    TextView nameContract;
    @BindView(R.id.wizard_next_button)
    Button finish;
    @BindView(R.id.add_scan_contract)
    Button addScanContract;
    //Wire the layout to the step

    private String ContractNumber;
    private String ContractorId = "";

    @BindView(R.id.list_users_recycler)
    RecyclerView recyclerViewProjects;
    @BindView(R.id.list_employer_recycler)
    RecyclerView recyclerViewEmployer;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.content_main)
    LinearLayout contentMain;

    private ArrayList<Image> imagesContract;
    @BindView(R.id.btn_edit)
    public ImageView btnEdit;
    @BindView(R.id.label_validity)
    public TextView labelValidity;
    @BindView(R.id.text_active)
    public TextView text_active;
    @BindView(R.id.text_name)
    public TextView text_name;
    @BindView(R.id.text_sub_name)
    public TextView text_sub_name;
    @BindView(R.id.card_view_detail)
    public CardView card_view_detail;
    @BindView(R.id.logo)
    public ImageView logo_imag;
    @BindView(R.id.text_validity)
    public TextView text_validity;

    private String companyIdContract = "";
    @BindView(R.id.btn_edit_contract)
    public Button btnEdit_contract;
    @BindView(R.id.text_name_contract)
    public TextView text_name_contract;
    @BindView(R.id.text_sub_name_contract)
    public TextView text_sub_name_contract;
    @BindView(R.id.logo_contract)
    public ImageView logo_imag_contract;
    @BindView(R.id.spinner_companies)
    Spinner spinner_companies;

    @BindView(R.id.companies_spinner)
    Spinner companiesSpinner;
    @BindView(R.id.employers_spinner)
    Spinner employersSpinner;
    @BindView(R.id.companies_spinner_view)
    TextInputLayout companiesSpinnerView;
    @BindView(R.id.employers_spinner_view)
    TextInputLayout employersSpinnerView;


    private static final int REQUEST_IMAGE_SELECTOR = 199;

    private Context instance;

    private User user;
    private ArrayList<ContractType> contracts;
    private ArrayList<PersonalType> personalT;
    private String idCompany;
    private String refContract;


    private View mFormView;
    private View mProgressView;
    private ArrayList<CompanyList> companyProjec;

    private Uri imageUri = null;
    private SpinnerAdapterImage imagesAdapter;
    private String[] tagList;
    private String[] imageList;
    private int personalType = 0;
    private boolean blockCompanies = false;
    private ArrayList<CompanyList> employerCompanies;
    private String employerData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unbinder = ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("NUEVO CONTRATO");
        mFormView = findViewById(R.id.form);
        mProgressView = findViewById(R.id.progress);
        populateCityAutoComplete();

        card_view_detail.setVisibility(View.GONE);
        mReviewView = findViewById(R.id.edtt_review);
        mNumContractView = findViewById(R.id.edtt_numContract);
        cityAutoCompleteTextView = findViewById(R.id.edit_city);
        instance = this;
        getJSONBroadcastReceiver = new RequestBroadcastReceiver(this);


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        final MyPreferences preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);
        employersSpinner.setEnabled(false);
        if (user.Companies != null) {
            String[] companiesName = new String[user.Companies.size()];
            for (int i = 0; i < (user.Companies.size()); i++) {
                companiesName[i] = user.Companies.get(i).Name;
                logW("companiesName[i]: " + companiesName[i]);
            }
            spinner_companies.setAdapter(new ArrayAdapter<>(this, R.layout.simple_spinner_item_project, companiesName));
            if (user.Companies.size() == 1) {
                spinner_companies.setSelection(0);
                idCompany = user.Companies.get(0).Id;
            }

        }

        spinner_companies.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                idCompany = user.Companies.get(spinner_companies.getSelectedItemPosition()).Id;
                progress.setVisibility(View.VISIBLE);

                // tipos de contratos
                CrudIntentService.startRequestCRUD(instance, Constantes.CREATE_CONTRACTTYPE,
                        Request.Method.GET, "", "", true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        CrudIntentService.startRequestCRUD(this, Constantes.CONTRAC_REQUEST_NUM + idCompany,
                Request.Method.GET, "", "", false);
        logW("URL REFERENCIA " + Constantes.CONTRAC_REQUEST_NUM + idCompany);

        contractType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!blockCompanies) {
//                    recyclerViewProjects.setVisibility(View.VISIBLE);
                    companiesSpinnerView.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.VISIBLE);
                    card_view_detail.setVisibility(View.GONE);
                }
                if (contractType.getSelectedItemPosition() > 0) {
                    if (contracts.get(contractType.getSelectedItemPosition() - 1).Value.equals("AD") || contracts.get(contractType.getSelectedItemPosition() - 1).Value.equals("AS")) {
                        findViewById(R.id.contentContratista).setVisibility(View.GONE);
                        if (contracts.get(contractType.getSelectedItemPosition() - 1).Value.equals("AD")) {
                            for (CompanyList n : companyProjec) {
                                //logW(new Gson().toJson(n));
                                if (n.Id.equals(user.CompanyId)) {
                                    card_view_detail.setVisibility(View.VISIBLE);
//                                    recyclerViewProjects.setVisibility(View.GONE);
                                    companiesSpinnerView.setVisibility(View.GONE);
                                    labelValidity.setVisibility(View.GONE);
                                    btnEdit.setVisibility(View.GONE);
                                    text_validity.setText(n.Address);
                                    text_active.setVisibility(View.GONE);
                                    companyIdContract = n.Id;
                                    logW(new Gson().toJson(n));
                                    logW(new Gson().toJson(user));
                                    text_name.setText(n.Name);
                                    text_sub_name.setText(n.DocumentType + " " + n.DocumentNumber);
                                    if (n.Logo != null) {
                                        String url = Constantes.URL_IMAGES + n.Logo;
                                        Picasso.get().load(url)
                                                .resize(0, 250)
                                                .placeholder(R.drawable.image_not_available)
                                                .error(R.drawable.image_not_available)
                                                .into(logo_imag);
                                    } else {
                                        logo_imag.setImageResource(R.drawable.image_not_available);
                                    }
                                }
                            }
                        } else {
//                            recyclerViewProjects.setVisibility(View.VISIBLE);
                        }
                    } else {
                        findViewById(R.id.contentContratista).setVisibility(View.VISIBLE);
//                        recyclerViewProjects.setVisibility(View.VISIBLE);
                    }
                    ContractType selectedContractType = new ContractType();
                    Date currentTime = Calendar.getInstance().getTime();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
                    for (ContractType contractTypeItem : contracts) {
                        if (contractTypeItem.Description.equals(contractType.getSelectedItem().toString())) {
                            selectedContractType = contractTypeItem;
                        }
                    }
                    mNumContractView.setText(selectedContractType.Value + "-" + dateFormat.format(currentTime) + "-" + refContract);
                    mReviewView.setText("Contrato " + contractType.getSelectedItem().toString());
                    setContractTypeImage(contractType.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        finish.setOnClickListener(v -> requestContract());
        addScanContract.setOnClickListener(v -> photo());

        companiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    selectCompanyItem((CompanyList) companiesSpinner.getSelectedItem());
                    removeEmployerCompanyItem((CompanyList) companiesSpinner.getSelectedItem());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        employersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) selectEmployerItem((CompanyList) employersSpinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_contract);
    }

    private void populateCityAutoComplete() {
        SimpleCursorAdapter mAdapterCust = new SimpleCursorAdapter(this, R.layout.simple_spinner_item_2, null,
                new String[]{DBHelper.DANE_CITY_TABLE_COLUMN_NAME, DBHelper.DANE_CITY_TABLE_COLUMN_STATE},
                new int[]{android.R.id.text1, android.R.id.text2},
                0);


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
    }

    private Cursor getCursorCity(CharSequence str) {
        String select = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_NAME + " LIKE ? )";
        String[] selectArgs = {"%" + str + "%"};
        return getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI, null, select, selectArgs,
                DBHelper.DANE_CITY_TABLE_COLUMN_NAME);
    }

    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edit_city:
             /*  if (cityAutoCompleteTextView.isFocused()) {
                    //if (!text.isEmpty())
                    cityAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchCity = false;
                }*/
                break;
        }
    }

    @OnClick(R.id.send_contract)
    public void sendContractButton() {
        dispatchPhotoSelectionIntent();
    }

    //A system-based view to select photos.
    private void dispatchPhotoSelectionIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECTOR);
    }

    @OnClick(R.id.removeContract)
    public void removeContractButton() {
        fileContract.setVisibility(View.GONE);
//        mPhotoPath = "";
        nameContract.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        progress.setVisibility(View.GONE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_FORM_DATA);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(instance).registerReceiver(getJSONBroadcastReceiver,
                intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
//        progress.setVisibility(View.GONE);
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(getJSONBroadcastReceiver);
    }

    @Override
    public void didClearText(View view) {
        cityAutoCompleteTextView.setClearIconVisible(false);
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
                        ContractActivity.super.onBackPressed();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("entro", requestCode + "");

        switch (requestCode) {
            case PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = Uri.parse(data.getStringExtra(URI_IMAGE_KEY));

                    String readOnlyMode = "r";
                    try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, readOnlyMode)) {
                        assert pfd != null;
                        FileDescriptor fileDescriptor = Objects.requireNonNull(pfd).getFileDescriptor();
                        Bitmap myBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        BitmapDrawable bdrawable = new BitmapDrawable(getResources(), myBitmap);
                        addScanContract.setText("");
                        addScanContract.setBackground(bdrawable);
                        progress.setVisibility(View.GONE);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                break;
            case REQUEST_IMAGE_SELECTOR:
                if (resultCode != Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    if (imageUri == null) return;
                    Bitmap bitmap = getBitmapFromUri(getApplicationContext(), imageUri);
                    imageUri = getRealUriImage(getApplicationContext(), bitmap, true);
                    fileContract.setVisibility(View.VISIBLE);
//                    nameContract.setText(mPhotoPath);
                }
                break;
            default:
                break;
        }
    }


    private void requestContract() {
        ContractNumber = mNumContractView.getText().toString();
        if (TextUtils.isEmpty(ContractNumber)) {
            submitRequest();
        } else {
            CrudIntentService.startRequestCRUD(this,
                    Constantes.LIST_CONTRACTS_EXIST + companyIdContract + "/" + ContractNumber, Request.Method.GET, "", "", false);
        }
    }

    private void submitRequest() {
        // Reset errors.
        // mProjectNumberView.setError(null);
        progress.setVisibility(View.VISIBLE);
        mReviewView.setError(null);

        String contractReview = mReviewView.getText().toString();
        ContractNumber = mNumContractView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(contractReview)) {
            mReviewView.setError(getString(R.string.error_field_required));
            focusView = mReviewView;
            cancel = true;
        } else if (contractType.getSelectedItemPosition() == 0) {
            focusView = contractType;
            cancel = true;
        } else if (companyIdContract.equals("")) {
            focusView = companiesSpinner;
            cancel = true;
        } else if (ContractorId.equals("") && !(contracts.get(contractType.getSelectedItemPosition() - 1).Value.equals("AD") || contracts.get(contractType.getSelectedItemPosition() - 1).Value.equals("AS"))) {
            focusView = employersSpinner;
            cancel = true;
        }
        logW("validate " + cancel + " " + personalType);
        if (cancel) {
            focusView.requestFocus();
            progress.setVisibility(View.GONE);
            //notifyIncomplete();
            return;
        }
        int selectImage = spinnerImages.getSelectedItemPosition();
        if (contracts.get(contractType.getSelectedItemPosition() - 1).Value.equals("AD") || contracts.get(contractType.getSelectedItemPosition() - 1).Value.equals("AS")) {
            ContractorId = companyIdContract;
            String selected = contracts.get(contractType.getSelectedItemPosition() - 1).Value;
            for (PersonalType person : personalT) {
                if (person.Value.equals(selected)) {
                    personalType = person.Id;
                    break;
                }
            }
        }
//        else {
//            personalType = personalT.get(mRoleSpinner.getSelectedItemPosition() - 1).Id;
//        }

        Contract nwContract = new Contract(ContractNumber, contractReview, imagesContract.get(selectImage).Id,
                contracts.get(contractType.getSelectedItemPosition() - 1).Id, personalType,
                companyIdContract, ContractorId, imagesContract.get(selectImage).Logo);
        Gson gson = new Gson();
        String json = gson.toJson(nwContract);
        logW(json);
        CrudIntentService.startRequestCRUD(this,
                Constantes.LIST_CONTRACTS_URL, Request.Method.POST, json, "", false);
        //onExit(WizardStep.EXIT_NEXT);
    }

    private void updateList(String jsonObjStr) {
        Log.e("updateList", jsonObjStr);
        contracts = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<ContractType>>() {
                }.getType());

        String[] contract = new String[contracts.size() + 1];
        contract[0] = "TIPO DE CONTRATO";
        for (int i = 1; i <= contracts.size(); i++) {
            contract[i] = contracts.get(i - 1).Description;
            //logE(contract[i]);
        }

        contractType.setAdapter(new CustomArrayAdapter(instance, contract));
        CrudIntentService.startRequestCRUD(instance, Constantes.CREATE_COMPANY_URL + idCompany + "/JoinCompanies/",
                Request.Method.GET, "", "", true);
    }

    private void fillListCompanies(String companies) {
        logW("list " + companies);
        companyProjec = new Gson().fromJson(companies,
                new TypeToken<ArrayList<CompanyList>>() {
                }.getType());
        if (companyProjec.isEmpty()) {
            recyclerViewProjects.setVisibility(View.GONE);
            return;
        } else if (companyProjec.size() == 1) {
            blockCompanies = true;
            companiesSpinnerView.setVisibility(View.GONE);
//            btnEdit.setBackgroundResource(R.drawable.ic_list_remove);
            btnEdit.setVisibility(View.GONE);
            card_view_detail.setVisibility(View.VISIBLE);
            recyclerViewProjects.setVisibility(View.GONE);
            labelValidity.setVisibility(View.GONE);
            text_validity.setText(companyProjec.get(0).Address);
            text_active.setVisibility(View.GONE);
            companyIdContract = companyProjec.get(0).Id;
            text_name.setText(companyProjec.get(0).Name);
            text_sub_name.setText(companyProjec.get(0).DocumentType + " " + companyProjec.get(0).DocumentNumber);
            if (companyProjec.get(0).Logo != null) {
                String url = Constantes.URL_IMAGES + companyProjec.get(0).Logo;
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo_imag);
            }
        } else blockCompanies = false;

        Collections.sort(companyProjec, (projectList, t1) -> t1.Name.compareTo(projectList.Name));

        CustomAutocompleteAdapter adapter = new CustomAutocompleteAdapter(this, R.layout.simple_spinner_item_2, companyProjec);
        companiesSpinner.setAdapter(adapter);
        CrudIntentService.startRequestCRUD(instance, Constantes.IMAGES_CONTRACT_URL,
                Request.Method.GET, "", "", true);
    }

    private void fillListImages(String imagesList) {
        imagesContract = new Gson().fromJson(imagesList,
                new TypeToken<ArrayList<Image>>() {
                }.getType());
        imageList = new String[imagesContract.size()];
        tagList = new String[imagesContract.size()];
        for (int i = 0; i < imagesContract.size(); i++) {
            imageList[i] = imagesContract.get(i).Logo;
            tagList[i] = imagesContract.get(i).Tags[0];
        }
        imagesAdapter = new SpinnerAdapterImage(this,
                android.R.layout.simple_spinner_item,
                imageList);
        spinnerImages.setAdapter(imagesAdapter); // Set the custom adapter to the spinner
        // You can create an anonymous listener to handle the event when is selected an spinner item
        spinnerImages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                // Here you get the current item (a User object) that is selected by its position
                String imageSelect = imagesAdapter.getItem(position);
                // Here you can do the action you want to...
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
        //mLayoutManager.setAutoMeasureEnabled(true);
        ContentValues values = new ContentValues();
        values.put("type", "PersonalType");
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(instance, Constantes.CREATE_COMMONOPTIONS,
                Request.Method.GET, "", paramsQuery, true);
    }

    private void fillListPersonalType(String jsonObjStr) {
        personalT = new Gson().fromJson(jsonObjStr,
                new TypeToken<ArrayList<PersonalType>>() {
                }.getType());

        String[] contract = new String[personalT.size() + 1];
        contract[0] = "TIPO ";
        for (int i = 1; i <= personalT.size(); i++) {
            contract[i] = personalT.get(i - 1).Description;
        }
        ContentValues values = new ContentValues();
        values.put("companyinfoid", idCompany);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
//        mRoleSpinner.setAdapter(new CustomArrayAdapter(instance, contract));
        CrudIntentService.startRequestCRUD(instance, Constantes.LIST_EMPLOYERS_URL,
                Request.Method.GET, "", paramsQuery, true);
    }

    private void fillListEmployer(String employer) {
        employerData = employer;
        employerCompanies = new Gson().fromJson(employer,
                new TypeToken<ArrayList<CompanyList>>() {
                }.getType());
        if (employerCompanies.isEmpty()) {
            recyclerViewEmployer.setVisibility(View.GONE);
            return;
        }

        Collections.sort(employerCompanies, (projectList, t1) -> projectList.Name.compareTo(t1.Name));
        CustomAutocompleteAdapter adapter = new CustomAutocompleteAdapter(this, R.layout.simple_spinner_item_2, employerCompanies);
        employersSpinner.setAdapter(adapter);
        if (companyProjec.size() == 1) removeEmployerCompanyItem(companyProjec.get(0));

    }

    private void removeEmployerCompanyItem(CompanyList item) {
        CompanyList deleteItem = null;
        employerCompanies = new Gson().fromJson(employerData,
                new TypeToken<ArrayList<CompanyList>>() {
                }.getType());
        logW("employerCompanies by delete " + employerData);
        if (employerCompanies != null) {
            for (int i = 0; i < employerCompanies.size(); i++) {
                if (employerCompanies.get(i).Id.equals(item.Id))
                    deleteItem = employerCompanies.get(i);
            }
            if (deleteItem != null) employerCompanies.remove(deleteItem);
            Collections.sort(employerCompanies, (projectList, t1) -> projectList.Name.compareTo(t1.Name));
            CustomAutocompleteAdapter adapter = new CustomAutocompleteAdapter(this, R.layout.simple_spinner_item_2, employerCompanies);
            employersSpinner.setAdapter(adapter);
            employersSpinner.setEnabled(true);
            removeEmployer();
        }
    }

    private void fillRequestNum(String reference) {

        Reference ref = new Gson().fromJson(reference, Reference.class);
        if (ref.Count < 10) {
            refContract = "0" + ref.Count;
        } else {
            refContract = "" + ref.Count;
        }
    }

    @Override
    public void onListOrderInteraction(int position, boolean remove) {

    }

    @OnClick(R.id.btn_edit)
    public void removeCompany() {
        card_view_detail.setVisibility(View.GONE);
        recyclerViewProjects.setVisibility(View.VISIBLE);
        companiesSpinnerView.setVisibility(View.VISIBLE);
        companyIdContract = "";
        companiesSpinner.setSelection(0);
        employersSpinner.setEnabled(false);
    }

    @OnClick(R.id.btn_edit_contract)
    public void removeEmployer() {
        employerSelected.setVisibility(View.GONE);
        recyclerViewEmployer.setVisibility(View.VISIBLE);
        layouySpinner.setVisibility(View.GONE);
        employersSpinnerView.setVisibility(View.VISIBLE);
        ContractorId = "";
        employersSpinner.setSelection(0);
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {

        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (url.equals(Constantes.CONTRAC_REQUEST_NUM + idCompany)) {
                    fillRequestNum(jsonObjStr);
                    //logW("success " + jsonObjStr);

                } else if (url.equals(Constantes.LIST_CONTRACTS_URL) && imageUri != null && !imageUri.toString().isEmpty()) {
                    try {
                        JSONObject newcontract = new JSONObject(jsonObjStr);
                        String id = newcontract.getString("Id");
                        HashMap<String, String> params = new HashMap<>();
                        params.put("file", imageUri.toString());
                        String urlContract = Constantes.CONTRACT_FILE_URL + id;
                        CrudIntentService.startActionFormData(this, urlContract,
                                Request.Method.PUT, params);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (url.equals(Constantes.LIST_CONTRACTS_EXIST + companyIdContract + "/" + ContractNumber)) {
                    try {
                        JSONObject object = new JSONObject(jsonObjStr);
                        if (object.getBoolean("IsContract")) {
                            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ContractActivity.this)
                                    .setTitle("Contrato existente")
                                    .setPositiveButton("OK", null)
                                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            onBackPressed();
                                        }
                                    })
                                    .setMessage("Ya existe un contrato con ese número, por favor modifiquelo o deje el campo vacío ");
                            builder.create().show();
                        } else {
                            submitRequest();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (url.equals(Constantes.LIST_CONTRACTS_URL) && imageUri == null) {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                progress.setVisibility(View.GONE);
                setResult(RESULT_OK);
                finish();
                break;
            case Constantes.REQUEST_NOT_FOUND:

                break;
            case Constantes.SEND_REQUEST:
                //msgSuccess();
                logW("url: " + url);
                if (url.equals(Constantes.CREATE_CONTRACTTYPE)) {
                    updateList(jsonObjStr);
                } else if (url.equals(Constantes.CREATE_COMPANY_URL + idCompany + "/JoinCompanies/")) {
                    fillListCompanies(jsonObjStr);
                } else if (url.equals(Constantes.IMAGES_CONTRACT_URL)) {
                    // logW(jsonObjStr);
                    fillListImages(jsonObjStr);
                } else if (url.equals(Constantes.CREATE_COMMONOPTIONS)) {
                    fillListPersonalType(jsonObjStr);
                    // logW(jsonObjStr);
                } else if (url.equals(Constantes.LIST_EMPLOYERS_URL)) {
                    fillListEmployer(jsonObjStr);
                    progress.setVisibility(View.GONE);
                    contentMain.setVisibility(View.VISIBLE);
                    // logW(jsonObjStr);
                }
                break;
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
                //MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
        }
    }

    class Reference implements Serializable {
        int Count;
    }

    private void photo() {
        new PhotoSer.ActivityBuilder()
                //Detect Face
                .setDetectFace(false)
                //Save in Galery or Cache
                .setSaveGalery(true)
                .setFixAspectRatio(true)
                //Enable crop image
                .setCrop(false)
                //Quality of image
                .setQuality(50)
                .start(this);
    }

    private void setContractTypeImage(String contractTypeSelected) {
        logW("spinner " + contractTypeSelected);
        int pos;
        switch (contractTypeSelected) {
            case "Para Empleados Contratista":
                pos = new ArrayList<>(Arrays.asList(tagList)).indexOf("AA Contrato para Empleados Contratista");
                spinnerImages.setSelection(imagesAdapter.getPosition(imageList[pos]));
                personalType = getPersonalTypeId("CO");
                break;
            case "Para Empleados Proveedores":
                pos = new ArrayList<>(Arrays.asList(tagList)).indexOf("AA Contrato para empleados Proveedores");
                spinnerImages.setSelection(imagesAdapter.getPosition(imageList[pos]));
                personalType = getPersonalTypeId("PR");
                break;
            case "Para un Empleado Administrativo":
                pos = new ArrayList<>(Arrays.asList(tagList)).indexOf("AA Contrato para un Empleado Administrativo");
                spinnerImages.setSelection(imagesAdapter.getPosition(imageList[pos]));
                personalType = getPersonalTypeId("AD");
                break;
            case "Para un Empleado Asociado":
                pos = new ArrayList<>(Arrays.asList(tagList)).indexOf("AA Registro para Empleados Asociados");
                spinnerImages.setSelection(imagesAdapter.getPosition(imageList[pos]));
                personalType = getPersonalTypeId("AS");
                break;
            case "Para Empleados Funcionarios":
                pos = new ArrayList<>(Arrays.asList(tagList)).indexOf("AA Contrato para empleados Funcionarios");
                spinnerImages.setSelection(imagesAdapter.getPosition(imageList[pos]));
                personalType = getPersonalTypeId("FU");
                break;
            default:
                break;
        }
    }

    private int getPersonalTypeId(String s) {
        int id = 0;
        for (PersonalType personalType : personalT) {
            if (personalType.Value.equals(s)) id = personalType.Id;
        }
        return id;
    }

    @SuppressLint("SetTextI18n")
    public void selectCompanyItem(CompanyList mItem) {
        companiesSpinnerView.setVisibility(View.GONE);
        btnEdit.setBackgroundResource(R.drawable.ic_list_remove);
        btnEdit.setVisibility(View.VISIBLE);
        card_view_detail.setVisibility(View.VISIBLE);
        recyclerViewProjects.setVisibility(View.GONE);
        labelValidity.setVisibility(View.GONE);
        text_validity.setText(mItem.Address);
        text_active.setVisibility(View.GONE);
        companyIdContract = mItem.Id;
        text_name.setText(mItem.Name);
        text_sub_name.setText(mItem.DocumentType + " " + mItem.DocumentNumber);
        logo_imag.setImageDrawable(getDrawable(R.drawable.image_not_available));
        if (mItem.Logo != null) {
            String url = Constantes.URL_IMAGES + mItem.Logo;
            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(logo_imag);
        }

    }

    @SuppressLint("SetTextI18n")
    public void selectEmployerItem(CompanyList mItem) {
        employersSpinnerView.setVisibility(View.GONE);
        btnEdit_contract.setBackgroundResource(R.drawable.ic_list_remove);
        recyclerViewEmployer.setVisibility(View.GONE);
        ContractorId = mItem.Id;
        employerSelected.setVisibility(View.VISIBLE);
        text_name_contract.setText(mItem.Name);
        text_sub_name_contract.setText(mItem.DocumentType + " " + mItem.DocumentNumber);
        if (mItem.Logo != null) {
            String url = Constantes.URL_IMAGES + mItem.Logo;
            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(logo_imag_contract);
        }
    }
}
