package co.tecno.sersoluciones.analityco.individualContract;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.com.sersoluciones.facedetectorser.FaceTrackerActivity;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.ApplicationContext;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.AdapterDocumentsRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.RequerimentIndividualContractAdapter;
import co.tecno.sersoluciones.analityco.models.AfpList;
import co.tecno.sersoluciones.analityco.models.EpsList;
import co.tecno.sersoluciones.analityco.models.RequerimentIndividualContract;
import co.tecno.sersoluciones.analityco.nav.CreatePersonalViewModel;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.ui.createPersonal.viewmodels.PersonalViewModel;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

@SuppressLint("NonConstantResourceId")
public class RequirementIndividualContractFragment extends Fragment implements AdapterView.OnItemSelectedListener, RequerimentIndividualContractAdapter.OnDocumentIndivudualContractListenerEdit, RequestBroadcastReceiver.BroadcastListener, AdapterDocumentsRecyclerView.OnDocumentIndivudualContractListener, DatePickerDialog.OnDateSetListener {

    View view;
    @Inject
    PersonalViewModel viewModel;
    @Inject
    CreatePersonalViewModel CreateviewModel;

    @BindView(R.id.listObligatory)
    RecyclerView recyclerViewDocumentObligatory;
    @BindView(R.id.listUpload)
    RecyclerView recyclerView;
    @BindView(R.id.editRequirement)
    CardView editRequeriment;
    @BindView(R.id.requirementTitle)
    TextView requirementTitle;
    @BindView(R.id.DateToRequeriment)
    RelativeLayout DateToRequeriment;
    @BindView(R.id.SearchToRequeriment)
    RelativeLayout SearchToRequeriment;
    @BindView(R.id.fromDateBtn)
    Button fromDateBtn;
    @BindView(R.id.eps_input_layout)
    TextInputLayout epsinputLayout;
    @BindView(R.id.afp_input_layout)
    TextInputLayout afpinputLayout;
    @BindView(R.id.iconFile)
    ImageView iconFile;
    @BindView(R.id.fabRemove)
    FloatingActionButton fabRemove;
    @BindView(R.id.positiveButton)
    Button positiveButton;
    @BindView(R.id.negativeButton)
    Button negativeButton;
    @BindView(R.id.epsAutoCompleteTextView)
    AutoCompleteTextView epsAutoCompleteTextView;
    @BindView(R.id.afpAutoCompleteTextView)
    AutoCompleteTextView afpAutoCompleteTextView;
    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.mProgressView)
    LinearLayout mProgressView;

    private static final String ARG_LIST_ITEMS_UPLOAD = "items_upload";
    private static final String ARG_ID_CONTRACT = "id_contract";
    private static final String ARG_PERSONAL_EMPLOYER_INFO = "personal_employer_info_id";
    private static final String ARG_PERSONAL_NATIONALITY = "personal_nationality";
    private static final String ARG_CONTRACT_DESCRIPTION = "contract_description";

    private ArrayList<RequerimentIndividualContract> mValues, Requiered;
    private String ItemsUpload, IdRequeriment;
    private int Nationality = 0;
    private int epsId = 0;
    private int afpId = 0;
    private AdapterDocumentsRecyclerView Adapter;
    private RequerimentIndividualContractAdapter AdapterRequeried;
    private OnListFragmentInteractionListener mListener;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private int positionArray = 0;
    private Date fromDate;
    private  String fromDateStr, IdContract,PersonalEmployerInfoId;
    private  DatePickerDialog fromDatePickerDialog;
    private  Uri mImageUri;
    private boolean DocumentRequeriedDate, DocumentNoRequeried,DocumentIsEps,DocumentIsAfps, IsUpload;
    private boolean FirtTime = false;
    private boolean ShowPencilEdit = false;


    public static RequirementIndividualContractFragment newInstance(String ItemsUpload,String IdContract, String PersonalEmployerInfoId, int Nationality,String Description) {
        RequirementIndividualContractFragment fragment = new RequirementIndividualContractFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_ITEMS_UPLOAD, ItemsUpload);
        args.putString(ARG_ID_CONTRACT, IdContract);
        args.putString(ARG_PERSONAL_EMPLOYER_INFO, PersonalEmployerInfoId);
        args.putInt(ARG_PERSONAL_NATIONALITY,Nationality);
        args.putString(ARG_CONTRACT_DESCRIPTION,Description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            ItemsUpload = getArguments().getString(ARG_LIST_ITEMS_UPLOAD);
            IdContract = getArguments().getString(ARG_ID_CONTRACT);
            PersonalEmployerInfoId = getArguments().getString(ARG_PERSONAL_EMPLOYER_INFO);
            Nationality = getArguments().getInt(ARG_PERSONAL_NATIONALITY);
            FirtTime = true;
            switch (getArguments().getString(ARG_CONTRACT_DESCRIPTION)){
                case "CONTRATADO":
                case "APROBADO":
                case "LIQUIDADO":
                    ShowPencilEdit = false;
                    break;
                case "SOLICITADO":
                case "INICIADO":
                case "RECHAZADO PARA CORREGIR":
                case "RECHAZADO":
                    ShowPencilEdit = true;
                    break;
            }
        }
        mValues = new ArrayList<>();
        Requiered = new ArrayList<>();
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.requeriment_individual_contract, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        if (ItemsUpload != null) {
            LoadDocuments(ItemsUpload);
        }

        viewModel.getListEps().observe(getViewLifecycleOwner(), new Observer<List<EpsList>>() {
            @Override
            public void onChanged(List<EpsList> epsLists) {
                ArrayAdapter epsAdapter = new ArrayAdapter(getActivity(),R.layout.list_item,epsLists);
                epsAutoCompleteTextView.setAdapter(epsAdapter);

            }
        });
        epsAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EpsList item = (EpsList) parent.getAdapter().getItem(position);
                epsId = item.getId();
            }
        });
        viewModel.getListAfp().observe(getViewLifecycleOwner(), new Observer<List<AfpList>>() {
            @Override
            public void onChanged(List<AfpList> afpLists) {
                ArrayAdapter afpAdapter = new ArrayAdapter(getActivity(),R.layout.list_item,afpLists);
                afpAutoCompleteTextView.setAdapter(afpAdapter);
            }
        });
        afpAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AfpList item = (AfpList) parent.getAdapter().getItem(position);
                afpId = item.getId();
            }
        });

        fromDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePickerDialog.getDatePicker().setTag(R.id.fromDateBtn);
                fromDatePickerDialog.show();
            }
        });
        iconFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFaceDectectorActivity();
            }
        });
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateImage();
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerViewDocumentObligatory.setVisibility(View.VISIBLE);
                editRequeriment.setVisibility(View.GONE);
            }
        });
        fabRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImageUri = null;
                iconFile.setImageResource(R.drawable.ic_note_text);
            }
        });
        return view;
    }

    private void updateImage() {
        if(mImageUri != null && !mImageUri.toString().isEmpty()){
            if(DocumentIsEps){
                if(!epsAutoCompleteTextView.getText().toString().isEmpty()){
                    showProgress(true);
                    JSONObject jsonObject = new JSONObject();
                    JSONObject jsonObjectDocument = new JSONObject();
                    try {
                        jsonObject.put("EpsId",epsId);
                        jsonObjectDocument.put("IndividualContractId", IdContract);
                        jsonObjectDocument.put("IdDocument", IdRequeriment);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    UploadInfoDocument(jsonObject.toString(),jsonObjectDocument.toString());
                    UploadImageDocument(IdRequeriment);
                }else {
                    epsAutoCompleteTextView.setError(getString(R.string.error_field_required));
                }
            }else if (DocumentIsAfps){
                if(!afpAutoCompleteTextView.getText().toString().isEmpty()){
                    showProgress(true);
                    JSONObject jsonObject = new JSONObject();
                    JSONObject jsonObjectDocument = new JSONObject();
                    try {
                        jsonObject.put("AfpId", afpId);
                        jsonObjectDocument.put("IndividualContractId", IdContract);
                        jsonObjectDocument.put("IdDocument", IdRequeriment);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    UploadInfoDocument(jsonObject.toString(),jsonObjectDocument.toString());
                    UploadImageDocument(IdRequeriment);
                }else {
                    afpAutoCompleteTextView.setError(getString(R.string.error_field_required));
                }
            }else if (DocumentRequeriedDate){
                if (fromDateStr != null) {
                    tvFromDateError.setError("fecha obligatoria");
                    @SuppressLint("SimpleDateFormat")
                     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    fromDateStr = format.format(fromDate);
                }
                showProgress(true);
                JSONObject jsonObjectDocument = new JSONObject();
                try {
                    jsonObjectDocument.put("IndividualContractId", IdContract);
                    jsonObjectDocument.put("RequiredDate", fromDateStr);
                    jsonObjectDocument.put("IdDocument", IdRequeriment);
                    jsonObjectDocument.put("IsActive", true);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                CrudIntentService.startRequestCRUD(
                        requireActivity(),
                        "api/Document/IndividualContract", Request.Method.POST, jsonObjectDocument.toString(), "", false
                );
                UploadImageDocument(IdRequeriment);
            }else {
                showProgress(true);
                JSONObject jsonObjectDocument = new JSONObject();
                try {
                    jsonObjectDocument.put("IndividualContractId", IdContract);
                    jsonObjectDocument.put("IdDocument", IdRequeriment);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                CrudIntentService.startRequestCRUD(
                        requireActivity(),
                        "api/Document/IndividualContract", Request.Method.POST, jsonObjectDocument.toString(), "", false
                );
                UploadImageDocument(IdRequeriment);
            }
            DocumentIsEps = false;
            DocumentIsAfps = false;
            DocumentRequeriedDate = false;
        }else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
            dialogBuilder.setMessage("Es necesario seleccionar una imagen");
            dialogBuilder.show();
        }
    }
    private void UploadImageDocument(String IdDocument){
        HashMap <String, String> params = new HashMap();
        params.put("file",mImageUri.toString());
        String url = "api/Document/IndividualContract/Upload/"+IdDocument;
        CrudIntentService.startActionFormData(
                requireActivity(), url,
                Request.Method.PUT, params
        );
    }
    private void UploadInfoDocument(String jsonObject, String jsonObjectDocument){
        CrudIntentService.startRequestCRUD(
                requireActivity(),
                "api/PersonalEmployerInfo/" + PersonalEmployerInfoId, Request.Method.PUT, jsonObject, "", false
        );
        CrudIntentService.startRequestCRUD(
                requireActivity(),
                "api/Document/IndividualContract", Request.Method.POST, jsonObjectDocument, "", false
        );
    }
    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(false)
                .setQuality(50)
                .setSaveGalery(false)
                .setCrop(false)
                .start(this, getActivity());
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void ShowDocumetsObligatory(String itemsObligatory) {
        JSONArray jsonArrayRequeriments;
        String DocumentContents;
        boolean IsUpload;
        RequerimentIndividualContract requerimentIndividualContract;
        try {
            jsonArrayRequeriments = new JSONArray(itemsObligatory);
            for (int i = 0; i < jsonArrayRequeriments.length(); i++) {
                DocumentContents = jsonArrayRequeriments.getJSONObject(i).getString("DocumentContents");
                IsUpload = jsonArrayRequeriments.getJSONObject(i).getBoolean("IsUpload");
                if (DocumentContents.equals("DocumentsForm") && IsUpload) {
                    requerimentIndividualContract = new Gson().fromJson((jsonArrayRequeriments.getJSONObject(i).toString()), RequerimentIndividualContract.class);
                    Requiered.add(requerimentIndividualContract);
                    if(Nationality == 0){
                        Predicate<RequerimentIndividualContract> condition = new Predicate<RequerimentIndividualContract>() {
                            @Override
                            public boolean test(RequerimentIndividualContract requerimentIndividualContract) {
                                return requerimentIndividualContract.Name.equals("Permios Especial de Permanecia");
                            }
                        };
                        Requiered.removeIf(condition);
                    }
                    if(mValues.size() != 0 ){
                        for(RequerimentIndividualContract item : mValues){
                            Predicate<RequerimentIndividualContract> condition = new Predicate<RequerimentIndividualContract>() {
                                @Override
                                public boolean test(RequerimentIndividualContract requerimentIndividualContract) {
                                    return requerimentIndividualContract.Name.equals(item.Name);
                                }
                            };
                            Requiered.removeIf(condition);
                        }
                    }
                }
            }

            recyclerViewDocumentObligatory.setLayoutManager(new LinearLayoutManager(getActivity()));
            Adapter = new AdapterDocumentsRecyclerView(getActivity(), Requiered, this,ShowPencilEdit);
            recyclerViewDocumentObligatory.setAdapter(Adapter);
            Adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void LoadDocuments(String response){
        if (response == null || response.equals("[]")){
            CRUDService.startRequest(
                    getActivity(), Constantes.INDIVIDUAL_DOCUMENT_CONTRACT_URL,
                    Request.Method.GET, "", false
            );
            return;
        }
        JSONArray jsonArrayRequeriments;
        JSONObject jsonObjectDocument;
        String jsonFile;
        String DocumentContents;
        RequerimentIndividualContract requerimentIndividualContract;
        try {
            jsonArrayRequeriments = new JSONArray(response);
            for(int i = 0; i < jsonArrayRequeriments.length(); i++){
                jsonObjectDocument = jsonArrayRequeriments.getJSONObject(i).getJSONObject("Document");
                jsonFile = jsonArrayRequeriments.getJSONObject(i).getString("File");
                DocumentContents = jsonObjectDocument.getString("DocumentContents");
                if(DocumentContents.equals("DocumentsForm")){
                    requerimentIndividualContract = new Gson().fromJson((jsonObjectDocument.toString()),RequerimentIndividualContract.class);
                    requerimentIndividualContract.File = jsonFile;
                    requerimentIndividualContract.Name = jsonObjectDocument.getString("Name");
                    requerimentIndividualContract.Abrv = jsonObjectDocument.getString("Abrv");
                    requerimentIndividualContract.Description = jsonObjectDocument.getString("Description");
                    mValues.add(requerimentIndividualContract);
                }
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            AdapterRequeried = new RequerimentIndividualContractAdapter(getActivity(),mValues,this,ShowPencilEdit);
            recyclerView.setAdapter(AdapterRequeried);
            AdapterRequeried.notifyDataSetChanged();
            if (mValues.size() < 6 && FirtTime) {
                CRUDService.startRequest(
                        getActivity(), Constantes.INDIVIDUAL_DOCUMENT_CONTRACT_URL,
                        Request.Method.GET, "", false
                );
                FirtTime = false;
            }
            mListener.onListFragmentInteraction(mValues.size() >=6);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ApplicationContext.analitycoComponent.inject(this);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStringResult(@Nullable String action, int option, @Nullable String response, @Nullable String url) {
        if (option == Constantes.SUCCESS_REQUEST) {
            if(url.equals("api/Document/")){
                Requiered.clear();
                ShowDocumetsObligatory(response);
            }
            if (url.equals("api/Document/IndividualContract/"+IdContract)) {
                mValues.clear();
                LoadDocuments(response);
            }
            if(url.equals("api/Document/IndividualContract")){
                if (!IsUpload) {
                    DeleteElement(positionArray);
                } else {
                    IsUpload = false;
                }
                CRUDService.startRequest(
                        getActivity(), Constantes.INDIVIDUAL_REQUERIMENT_CONTRACT_URL + IdContract,
                        Request.Method.GET, "", false
                );
                mImageUri = null;
                iconFile.setImageResource(R.drawable.ic_note_text);
                recyclerViewDocumentObligatory.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
                title.setVisibility(View.VISIBLE);
                editRequeriment.setVisibility(View.GONE);
                showProgress(false);
            }
        }
        if (option == Constantes.NOT_INTERNET ){
            showProgress(false);
            requireActivity().finish();
        }
        if(option == Constantes.UNAUTHORIZED){
            showProgress(false);
            requireActivity().finish();
        }
        if(option == Constantes.FORBIDDEN){
            showProgress(false);
            requireActivity().finish();
        }
    }
    private void DeleteElement(int PositionArray){
        Requiered.remove(PositionArray);
        Adapter.notifyItemRemoved(positionArray);
        Adapter.notifyDataSetChanged();
    }
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE);
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        LocalBroadcastManager.getInstance(requireActivity())
                .registerReceiver(requestBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(requestBroadcastReceiver);
    }
    @Override
    public void editRequerimentItemUpload(@Nullable RequerimentIndividualContract mItem, int pos) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positionArray = pos;
                recyclerView.setVisibility(View.GONE);
                recyclerViewDocumentObligatory.setVisibility(View.GONE);
                editRequeriment.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                IsUpload = true;
                requirementTitle.setText(mItem.Name);
                IdRequeriment = mItem.Id;
                if(mItem.IsByDate){
                    DateToRequeriment.setVisibility(View.VISIBLE);
                    SearchToRequeriment.setVisibility(View.GONE);
                    Calendar calendar = Calendar.getInstance();
                    fromDate = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    String myFormat = "dd/MMM/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
                    fromDateStr = sdf.format(fromDate);
                    fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            0,
                            0
                    );
                    fromDateBtn.setText(fromDateStr);
                    DocumentRequeriedDate = true;
                    updateDatePicker(fromDate);
                }else {
                    SearchToRequeriment.setVisibility(View.VISIBLE);
                    DateToRequeriment.setVisibility(View.GONE);
                    if(mItem.Abrv.equals("EPS")){
                        afpinputLayout.setVisibility(View.GONE);
                        epsinputLayout.setVisibility(View.VISIBLE);
                        DocumentIsEps = true;
                    }else if (mItem.Abrv.equals("AFP")){
                        afpinputLayout.setVisibility(View.VISIBLE);
                        epsinputLayout.setVisibility(View.GONE);
                        DocumentIsAfps = true;
                    }else {
                        afpinputLayout.setVisibility(View.GONE);
                        epsinputLayout.setVisibility(View.GONE);
                        DocumentNoRequeried = true;
                    }
                }

            }
        }).setNegativeButton("No",null)
                .setMessage("Es necesario tener acceso a internet para subir archivos, desea continuar?");
        dialogBuilder.create();
        dialogBuilder.show();
        mValues.set(pos, mItem);
        AdapterRequeried.notifyDataSetChanged();
    }
    @Override
    public void editRequerimentItem(@Nullable RequerimentIndividualContract mItem, int pos) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        dialogBuilder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                positionArray = pos;
                recyclerView.setVisibility(View.GONE);
                recyclerViewDocumentObligatory.setVisibility(View.GONE);
                editRequeriment.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                IsUpload = false;
                requirementTitle.setText(mItem.Name);
                IdRequeriment = mItem.Id;
                if(mItem.IsByDate){
                    DateToRequeriment.setVisibility(View.VISIBLE);
                    SearchToRequeriment.setVisibility(View.GONE);
                    Calendar calendar = Calendar.getInstance();
                    fromDate = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    String myFormat = "dd/MMM/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
                    fromDateStr = sdf.format(fromDate);
                    fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            0,
                            0
                    );
                    fromDateBtn.setText(fromDateStr);
                    DocumentRequeriedDate = true;
                    updateDatePicker(fromDate);
                }else {
                    SearchToRequeriment.setVisibility(View.VISIBLE);
                    DateToRequeriment.setVisibility(View.GONE);
                    if(mItem.Abrv.equals("EPS")){
                        afpinputLayout.setVisibility(View.GONE);
                        epsinputLayout.setVisibility(View.VISIBLE);
                        DocumentIsEps = true;
                    }else if (mItem.Abrv.equals("AFP")){
                        afpinputLayout.setVisibility(View.VISIBLE);
                        epsinputLayout.setVisibility(View.GONE);
                        DocumentIsAfps = true;
                    }else {
                        afpinputLayout.setVisibility(View.GONE);
                        epsinputLayout.setVisibility(View.GONE);
                        DocumentNoRequeried = true;
                    }
                }

            }
        }).setNegativeButton("No",null)
        .setMessage("Es necesario tener acceso a internet para subir archivos, desea continuar?");
        dialogBuilder.create();
        dialogBuilder.show();
        Requiered.set(pos, mItem);
        Adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri =
                        Uri.parse(data.getStringExtra(FaceTrackerActivity.URI_IMAGE_KEY));
                iconFile.setImageURI(imageUri);
                mImageUri = imageUri;
                fabRemove.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateDatePicker(Date fromDate) {
        Calendar cf = Calendar.getInstance();
        cf.setTime(fromDate);
        fromDatePickerDialog = new DatePickerDialog(requireActivity(),this,cf.get(Calendar.YEAR),
                cf.get(Calendar.MONTH), cf.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        String myFormat = "dd/MMM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
        fromDateStr = sdf.format(calendar.getTime());
        fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                0,
                0
        );
        fromDate = calendar.getTime();
        fromDateBtn.setText(fromDateStr);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(boolean item);
    }
}
