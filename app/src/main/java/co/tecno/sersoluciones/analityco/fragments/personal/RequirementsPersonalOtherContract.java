package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.CustomListInfoRecyclerView;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.RequirementsList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class RequirementsPersonalOtherContract extends Fragment implements DatePickerDialog.OnDateSetListener,
        RequestBroadcastReceiver.BroadcastListener {

    @BindView(R.id.negative_button)
    Button cancel;
    @BindView(R.id.name_user_scan)
    TextView name;
    @BindView(R.id.edit_users)
    TextView id;
    @BindView(R.id.requirement)
    RecyclerView recyclerRequirements;
    @BindView(R.id.icon_logo)
    ImageView logo;
    @BindView(R.id.editRequirement)
    CardView editRequirement;
    @BindView(R.id.requiremnet)
    TextView requirementTitle;
    @BindView(R.id.requiremnetDes)
    TextView requiremnetDes;
    @BindView(R.id.icon_file)
    ImageView iconFile;
    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.btn_from_date)
    Button fromDateBtn;
    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    private String fromDateStr;
    private Date fromDate;
    private int idReq;
    private int idReqOk = -1;
    private String idContract;
    private Personal user;
    private int info = 0;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private Context instance;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private ArrayList<RequirementsList> requirements;

    private DatePickerDialog fromDatePickerDialog;
    private Uri mImageUri;
    private String JsonArray;
    private String urlModel = "";


    public static RequirementsPersonalOtherContract infoUser(String user, String requirement) {
        RequirementsPersonalOtherContract fragment = new RequirementsPersonalOtherContract();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, user);
        args.putString(ARG_PARAM2, requirement);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(false)
                .start(this, getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragmnet_info_personal_user, container, false);
        ButterKnife.bind(this, view);
        instance = getActivity();
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        fabRemove.setVisibility(View.GONE);

        iconFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent(getActivity(), FaceTrackerActivity.class);
                Bundle bundle = new Bundle();
                PhotoSerOptions photoSerOptions = new PhotoSerOptions();
                photoSerOptions.setDetectFace(false);
                bundle.putParcelable(PHOTO_SER_EXTRA_OPTIONS, photoSerOptions);
                intent.putExtra(PHOTO_SER_EXTRA_BUNDLE, bundle);
                startActivityForResult(intent, PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE);*/
                startFaceDectectorActivity();
            }
        });
        MyPreferences preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();

        if (getArguments() != null) {
            //user = (Personal) getArguments().getSerializable(ARG_PARAM1);
            user = new Gson().fromJson(getArguments().getString(ARG_PARAM1), new TypeToken<Personal>() {
            }.getType());
           /* if(user.PersonalId.equals("")){
                idUser=user.Id;
            }else{
                idUser = user.PersonalId;
            }*/
            id.setText(user.DocumentNumber + "");
            name.setText(user.Name + " " + user.LastName);
            name.setEnabled(false);
            id.setEnabled(false);
            if (user.Photo != null) {
                String url = Constantes.URL_IMAGES + user.Photo;
                Picasso.get().load(url)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo);
            }
            //getArguments().getSerializable(ARG_PARAM1);
            JsonArray = getArguments().getString(ARG_PARAM2);
            requirementsObject(JsonArray);
          /*  CrudIntentService.startRequestCRUD(instance,
                    Constantes.PERSONAL_URL + idUser + "/Requirements/" + idProject + "/" + idSteps, Request.Method.GET, "", "", false);*/
        }

        return view;
    }

    private void updateImage() {
        boolean cancel = true;
        if (fromDateStr != null) {
            cancel = false;
            tvFromDateError.setError("fecha obligatoria");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            fromDateStr = format.format(fromDate);
        }
        if (mImageUri != null && !mImageUri.toString().isEmpty() && !cancel) {
            HashMap<String, String> params = new HashMap<>();
            params.put("file", mImageUri.toString());
            // String url = Constantes.PERSONALREQ_URL + idReq + "/File";
            String url = "api/" + urlModel + "/UpdateFile/" + idReq;
            CrudIntentService.startActionFormData(getActivity(), url,
                    Request.Method.PUT, params);
            RequerimentValid requeriment = new RequerimentValid(fromDateStr);
            Gson gson = new Gson();
            String json = gson.toJson(requeriment);
            CrudIntentService.startRequestCRUD(getActivity(),
                    "api/" + urlModel + "/" + idReq, Request.Method.PUT, json, "", false);
            mImageUri = null;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("Es necesario seleccionar una imagen");
            builder.create().show();
        }
    }

    @OnClick(R.id.negative_button)
    public void cancelButton() {
        editRequirement.setVisibility(View.GONE);
    }

    private void updateDatePicker(Date fromDate) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);
        Calendar cT = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(instance, this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    @OnClick(R.id.positive_button)
    public void positiveButton() {
        updateImage();
    }

    @OnClick(R.id.fab_remove)
    public void removeLogo() {
        removeFile();
    }

    private void removeFile() {
        Drawable drawable = MaterialDrawableBuilder.with(instance) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
                .setColor(Color.GRAY)
                .build();
        iconFile.setImageDrawable(drawable);
        fabRemove.setVisibility(View.GONE);
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        //progressDialog.dismiss();
        switch (option) {
            case Constantes.SUCCESS_REQUEST:

                if (info == 0) {
                    info = 1;
                    requirements.get(idReqOk).IsValided = true;
                    editRequirement.setVisibility(View.GONE);
                    requirementsObjectOK();
                } else {
                    removeFile();
                    editRequirement.setVisibility(View.GONE);
                    info = 0;
                  /*  CrudIntentService.startRequestCRUD(instance,
                            Constantes.PERSONAL_URL + idUser + "/Requirements/" + idProject + "/" + idSteps, Request.Method.GET, "", "", false);
               */
                }
                break;
            case Constantes.SEND_REQUEST:
                break;
            case Constantes.UPDATE_ADMIN_USERS:
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
                break;
        }
    }

    private void requirementsObject(String json) {
        requirements = new ArrayList<RequirementsList>();
        JSONObject jsonObject = null;
        JSONArray jsonObjectSs = null;
        JSONArray jsonObjectReq = null;
        JSONObject Reqs = null;
        RequirementsList requirement = null;
        JSONArray jsonArray = null;

        try {
            jsonArray = new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int k = 0; k < jsonArray.length(); k++) {

            try {
                jsonObject = jsonArray.getJSONObject(k);
                idContract = jsonObject.getString("ContractId");
                String requirString = jsonObject.getString("Reqs");
                Reqs = new JSONObject(requirString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (Reqs != null) {
                try {
                    jsonObjectSs = Reqs.getJSONArray("SocialSecurity");
                    if (jsonObjectSs != null) {
                        for (int i = 0; i < jsonObjectSs.length(); i++) {
                            try {
                                requirement = new Gson().fromJson((jsonObjectSs.getJSONObject(i).toString()),
                                        new TypeToken<RequirementsList>() {
                                        }.getType());
                                requirement.withFile = true;

                                requirements.add(requirement);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjectReq = Reqs.getJSONArray("Certification");
                    if (jsonObjectReq != null) {
                        for (int j = 0; j < jsonObjectReq.length(); j++) {
                            try {
                                requirement = new Gson().fromJson((jsonObjectReq.getJSONObject(j).toString()),
                                        new TypeToken<RequirementsList>() {
                                        }.getType());
                                requirement.withFile = true;
                                requirements.add(requirement);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    jsonObjectReq = Reqs.getJSONArray("Entry");
                    if (jsonObjectReq != null) {
                        for (int j = 0; j < jsonObjectReq.length(); j++) {
                            try {
                                requirement = new Gson().fromJson((jsonObjectReq.getJSONObject(j).toString()),
                                        new TypeToken<RequirementsList>() {
                                        }.getType());
                                requirements.add(requirement);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerRequirements.setLayoutManager(mLayoutManager);
        RequirementRecyclerAdapter adapter = new RequirementRecyclerAdapter(getActivity(), requirements);
        recyclerRequirements.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerRequirements.getContext(),
                mLayoutManager.getOrientation());
        recyclerRequirements.addItemDecoration(mDividerItemDecoration);
        recyclerRequirements.setAdapter(adapter);
    }

    private void requirementsObjectOK() {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerRequirements.setLayoutManager(mLayoutManager);
        RequirementRecyclerAdapter adapter = new RequirementRecyclerAdapter(getActivity(), requirements);
        recyclerRequirements.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerRequirements.getContext(),
                mLayoutManager.getOrientation());
        recyclerRequirements.addItemDecoration(mDividerItemDecoration);
        recyclerRequirements.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT);
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
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        int code = (Integer) view.getTag();
        //logE("CODE: " + code);
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        String myFormat = "dd/MMM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
        switch (code) {
            case R.id.btn_from_date:
                fromDateStr = sdf.format(calendar.getTime());
                fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                fromDateBtn.setText(fromDateStr);
                fromDate = calendar.getTime();
                fromDateBtn.setError(null);

                tvFromDateError.setVisibility(View.GONE);
                tvFromDateError.setError(null);
                break;
        }
    }


    private interface OnUserInfo {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserInfo) {
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Uri imageUri = Uri.parse(data.getStringExtra(URI_IMAGE_KEY));
                    iconFile.setImageURI(imageUri);
                    mImageUri = imageUri;
                    break;
                default:
                    break;
            }
        }
    }

    private class RequirementRecyclerAdapter extends CustomListInfoRecyclerView<RequirementsList> {

        RequirementRecyclerAdapter(Context context, ArrayList<RequirementsList> mItems) {
            super(context, mItems);
        }

        @Override
        public void onBindViewHolder(CustomListInfoRecyclerView.ViewHolder holderView, final int position) {
            final int pos = holderView.getAdapterPosition();
            final RequirementsList mItem = mItems.get(pos);
            final CustomListInfoRecyclerView.ViewHolder holder = holderView;
            holder.textName.setText(mItem.Type);
            holder.cardViewDetail.setBackgroundResource(R.color.md_red_A700);
            holder.btnEdit.setVisibility(View.GONE);
            holder.textName.setTextColor(Color.WHITE);
            holder.textSubName.setTextColor(Color.WHITE);
            holder.iconIsActive.setVisibility(View.GONE);
            holder.labelValidity.setVisibility(View.GONE);
            holder.iconIsActive.setVisibility(View.GONE);
            holder.logo.setVisibility(View.GONE);
            holder.textActive.setVisibility(View.GONE);
            if (mItem.ValidityDate != null) {
                String mDate = mItem.ValidityDate;
                try {
                    if (mDate != null && !mDate.isEmpty()) {
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        Date date = format.parse(mDate);
                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        mDate = dateFormat.format(date);
                        holder.textValidity.setText(mDate);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mItem.IsValided) {
                holder.cardViewDetail.setBackgroundResource(R.color.accent);
                holder.textName.setTextColor(Color.BLACK);
                holder.textSubName.setTextColor(Color.BLACK);
            }
            if (mItem.withFile && !mItem.IsValided) {
                holder.btnEdit.setVisibility(View.VISIBLE);
                //holder.btnEdit.setBackgroundResource(R.drawable.ic_check_circle_white_24dp);
                //holder.btnEdit.setImageResource(R.drawable.ic_check_circle_white_24dp);
            }
            holder.textSubName.setText(mItem.Desc);
            // holder.textActive.setText("Contrato:"+mItem.contractId);
            requirements = mItems;
            //Log.e("logo",mItem.Logo);
            holder.btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    urlModel = mItem.Model;
                    idReq = mItem.Id;
                    idReqOk = position;
                    editRequirement.setVisibility(View.VISIBLE);
                    requirementTitle.setText(mItem.Type);
                    requiremnetDes.setText(mItem.Desc);
                    Calendar calendar = Calendar.getInstance();
                    fromDate = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    String myFormat = "dd/MMM/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
                    fromDateStr = sdf.format(fromDate);
                    fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    fromDateBtn.setText(fromDateStr);
                    updateDatePicker(fromDate);
                }
            });

        }
    }

    @OnClick(R.id.btn_from_date)
    public void fromDateDialog() {
        fromDatePickerDialog.getDatePicker().setTag(R.id.btn_from_date);
        fromDatePickerDialog.show();
    }

    class RequerimentValid {

        RequerimentValid(String date) {
        }
    }

}


