package co.tecno.sersoluciones.analityco.fragments.company;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.AdminUsersOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.models.UserAdmin;
import co.tecno.sersoluciones.analityco.models.UserCustomer;
import co.tecno.sersoluciones.analityco.models.UserList;
import co.tecno.sersoluciones.analityco.models.UserProject;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class NewUserCompanyFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener, DatePickerDialog.OnDateSetListener {


    private Unbinder unbinder;
    @BindView(R.id.users_view)
    CardView usersView;
    @BindView(R.id.spinner_role)
    Spinner roles;
    @BindView(R.id.edit_users)
    EditText userAutoCompleteTextView;
    @BindView(R.id.btn_from_date)
    Button fromDateBtn;
    @BindView(R.id.tvToDateError)
    TextView tvToDateError;
    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    @BindView(R.id.btn_to_date)
    Button toDateBtn;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;

    @BindView(R.id.layout_main_user)
    LinearLayout layoutMainUser;
    @BindView(R.id.fab_search)
    FloatingActionButton serachUser;
    @BindView(R.id.text_name)
    public TextView text_name;
    @BindView(R.id.text_sub_name)
    public TextView text_sub_name;
    @BindView(R.id.card_view_detail)
    public CardView card_view_detail;
    @BindView(R.id.logo)
    public ImageView logo_imag;
    @BindView(R.id.btn_edit)
    public ImageView edit;
    @BindView(R.id.label_validity)
    public TextView text_validity;
    @BindView(R.id.text_active)
    public TextView text_active;
    @BindView(R.id.emialuser)
    public LinearLayout emialuser;

    private ArrayList<UserCustomer> userCustomers;
    private Context instance;
    private static final String USERS_CUSTOMERS_KEY = "usersCustomersArray";
    private static final String KEY_USERS = "users_json";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private static final String ARG_PARAM7 = "param7";
    private static final String ARG_PARAM8 = "param8";
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private Date fromDate;
    private Date toDate;
    private OnAddUserContract mListener;
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private String fromDateStr;
    private String toDateStr;
    private String idCompany = "";
    private String idUser = "";
    private int process = 1;
    private int processSendRequest = 0;
    private int processSuccesRequest = 0;
    private String email;
    private String name;
    private String photo;
    private String startDate;
    private String finishDate;
    private String userId;

    public static NewUserCompanyFragment newInstanceActivityUser(String companId) {
        int ContextFrom = 0;
        NewUserCompanyFragment fragment = new NewUserCompanyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, companId);
        args.putInt(ARG_PARAM2, 1);
        fragment.setArguments(args);
        return fragment;
    }

    public static NewUserCompanyFragment editInstanceActivityUser(String companId, String email, String name, String photo,
                                                                  String startDate, String finishDate, String userId) {
        int ContextFrom = 0;
        NewUserCompanyFragment fragment = new NewUserCompanyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, companId);
        args.putInt(ARG_PARAM2, 2);
        args.putString(ARG_PARAM3, email);
        args.putString(ARG_PARAM4, name);
        args.putString(ARG_PARAM5, photo);
        args.putString(ARG_PARAM6, startDate);
        args.putString(ARG_PARAM7, finishDate);
        args.putString(ARG_PARAM8, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        userCustomers = new ArrayList<>();

        if (getArguments() != null) {
           /* mTitle = getArguments().getString(ARG_TITLE);
            mURL = getArguments().getString(ARG_URL);
            mModel = (Validity) getArguments().getSerializable(ARG_MODEL);*/
            idCompany = getArguments().getString(ARG_PARAM1);
            process = getArguments().getInt(ARG_PARAM2);
            if (process == 2) {
                email = getArguments().getString(ARG_PARAM3);
                name = getArguments().getString(ARG_PARAM4);
                photo = getArguments().getString(ARG_PARAM5);
                startDate = getArguments().getString(ARG_PARAM6);
                finishDate = getArguments().getString(ARG_PARAM7);
                userId = getArguments().getString(ARG_PARAM8);
                idUser = userId + "";
                processSuccesRequest = 1;
                Log.e("user", idUser + " " + userId);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_user_project, container, false);
        unbinder = ButterKnife.bind(this, view);
        card_view_detail.setVisibility(View.GONE);
        roles.setVisibility(View.GONE);
        text_validity.setVisibility(View.GONE);
        init();

        serachUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processSuccesRequest = 0;
                String emailUser = userAutoCompleteTextView.getText().toString();
                if (!TextUtils.isEmpty(emailUser)) {
                    ContentValues values = new ContentValues();
                    values.put("admin", true);
                    String paramsQuery = HttpRequest.makeParamsInUrl(values);

                    String url = Constantes.USER_BY_EMAIL_URL + emailUser;
                    CrudIntentService.startRequestCRUD(instance, url,
                            Request.Method.GET, "", paramsQuery, false);
                }
            }
        });
        updateDatePicker(fromDate, toDate);

        return view;
    }

    //api/company/id/adminusers/id
    private void updateDatePicker(Date fromDate, Date toDate) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);

        Calendar cT = Calendar.getInstance();
        cT.setTime(toDate);
        fromDatePickerDialog = new DatePickerDialog(getActivity(), this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(fromDate.getTime());

        toDatePickerDialog = new DatePickerDialog(getActivity(), this, cT.get(Calendar.YEAR),
                cT.get(Calendar.MONTH), cT.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    private void init() {
        instance = getActivity();

        Calendar calendar = Calendar.getInstance();
        fromDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        toDate = calendar.getTime();

        String myFormat = "dd/MMM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
        fromDateStr = sdf.format(fromDate);
        fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        fromDateBtn.setText(fromDateStr);
        toDateStr = "";
        if (process == 2) {
                    processSendRequest = 1;
            Log.e("finishdate", finishDate);
            emialuser.setVisibility(View.GONE);
            card_view_detail.setVisibility(View.VISIBLE);
            text_name.setText(name);
            text_sub_name.setText(email);
            edit.setVisibility(View.GONE);
            logo_imag.setImageResource(R.drawable.image_not_available);
            if (photo != null) {
                String urlphoto = Constantes.URL_IMAGES + photo;
                Picasso.get().load(urlphoto)
                        .resize(0, 250)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(logo_imag);
            }
            try {
                if (finishDate != null) {
                    fromDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    toDate = format.parse(finishDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MMM/dd");
                    toDateStr = dateFormat.format(toDate);
                    toDateBtn.setText(toDateStr);
                    toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
                if (startDate != null) {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    fromDate = format.parse(startDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MMM/dd");
                    fromDateStr = dateFormat.format(fromDate);
                    fromDateBtn.setText(fromDateStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.negative_button)
    public void cancel() {
        if (mListener != null) {
            mListener.onCancelActionUser();
        }
    }

    @OnClick(R.id.positive_button)
    public void create() {
        boolean cancel = false;
        if (fromDate == null) {
            cancel = true;
            tvFromDateError.setError("campo obligatorio");
        } else if (TextUtils.isEmpty(idUser)) {
            cancel = true;
            userAutoCompleteTextView.setError("campo obligatorio");
        } else if (TextUtils.isEmpty(idUser)) {
            cancel = true;
            userAutoCompleteTextView.setError("Usuario no valido");
        }

        if (!cancel) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            fromDateStr = format.format(fromDate);
            toDateStr = format.format(toDate);
            UserProject user = new UserProject(fromDateStr, toDateStr, idUser, idCompany);
            Gson gson = new Gson();
            String json = gson.toJson(user);
            ///api/Company/{id}/AdminUsers
            CrudIntentService.startRequestCRUD(getActivity(),
                    "api/Company/" + idCompany + "/AdminUsers/", Request.Method.POST, json, "", false);
        }
    }

    @OnClick(R.id.btn_to_date)
    public void toDateDialog() {
        toDatePickerDialog.getDatePicker().setTag(R.id.btn_to_date);
        toDatePickerDialog.show();
    }

    @OnClick(R.id.btn_from_date)
    public void fromDateDialog() {
        fromDatePickerDialog.getDatePicker().setTag(R.id.btn_from_date);
        fromDatePickerDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        LocalBroadcastManager.getInstance(instance).registerReceiver(requestBroadcastReceiver,
                intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edit_users:
                if (userAutoCompleteTextView.isFocused()) {

                }
                break;
        }
    }

    @Override
    public void didClearText(View view) {
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
            case R.id.btn_to_date:
                toDateStr = sdf.format(calendar.getTime());
                toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                toDateBtn.setText(toDateStr);
                toDate = calendar.getTime();
                toDateBtn.setError(null);

                tvToDateError.setVisibility(View.GONE);
                tvToDateError.setError(null);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        /*if (jsonArrayUsers != null && !jsonArrayUsers.isEmpty())
            outState.putString(KEY_USERS_JSON, jsonArrayUsers);*/
        if (!userCustomers.isEmpty())
            outState.putString(USERS_CUSTOMERS_KEY, new Gson().toJson(userCustomers));
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        Log.e("option", " " + option + " " + jsonObjStr);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (processSuccesRequest == 0) {
                    processSuccesRequest = 1;
                    UserAdmin user = new Gson().fromJson(jsonObjStr,
                            new TypeToken<UserAdmin>() {
                            }.getType());
                    idUser = user.Id;
                    text_name.setText(user.Name + " " + user.LastName);
                    text_sub_name.setText(user.UserName);
                    edit.setVisibility(View.GONE);
                    text_active.setVisibility(View.GONE);
                    text_active.setVisibility(View.GONE);
                    card_view_detail.setVisibility(View.VISIBLE);
                    logo_imag.setImageResource(R.drawable.image_not_available);
                    if (user.Photo != null) {
                        String urlphoto = Constantes.URL_IMAGES + user.Photo;
                        Picasso.get().load(urlphoto)
                                .resize(0, 250)
                                .placeholder(R.drawable.image_not_available)
                                .error(R.drawable.image_not_available)
                                .into(logo_imag);
                    }
                } else {
                    if (mListener != null) {
                        mListener.onApplyActionUser();
                    }
                }
                break;
            case Constantes.SEND_REQUEST:
                if (processSendRequest == 0) {
                    ContentValues values = new ContentValues();
                    values.put("all", true);
                    String paramsQuery = HttpRequest.makeParamsInUrl(values);
                    processSendRequest = 1;
                  /* if(contexForm==0){
                       CrudIntentService.startRequestCRUD(instance, Constantes.LIST_USERS_URL,
                               Request.Method.GET, "", paramsQuery, true);
                   }*/

                } else {
                    //editListUsers(jsonObjStr);
                    //setUser(jsonObjStr);
                }
                break;
            case Constantes.BAD_REQUEST:
                //MetodosPublicos.alertDialog(instance, "Fallo al actualizar la base de datos");
                break;
            case Constantes.TIME_OUT_REQUEST:
                MetodosPublicos.alertDialog(instance, "Equipo sin conexion al Servidor, Intentelo mas tarde.");
                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                break;
            case Constantes.REQUEST_NOT_FOUND:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Usuario no existe")
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                userAutoCompleteTextView.setText("");
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mListener != null) {
                                    mListener.onCancelActionUser();
                                }
                            }
                        })
                        .setMessage("El usuario con correo:" + userAutoCompleteTextView.getText().toString() +
                                " no existe, desea intentar con un correo diferente");
                builder.create().show();
                //MetodosPublicos.alertDialog(instance, "Comañia no existe, al guardar la información se creara");
                break;
        }
    }


    public interface OnAddUserContract {
        void onCancelActionUser();

        void onApplyActionUser();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddUserContract) {
            mListener = (OnAddUserContract) context;
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


}
