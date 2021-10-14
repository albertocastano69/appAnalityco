package co.tecno.sersoluciones.analityco.fragments.project;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.AdminUsersOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.adapters.CustomArrayAdapter;
import co.tecno.sersoluciones.analityco.adapters.UsersArrayAdapter;
import co.tecno.sersoluciones.analityco.models.Roles;
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
public class AddUserProject extends Fragment implements
        AdminUsersOfficesRecyclerView.OnAdminUserInteractionListener, RequestBroadcastReceiver.BroadcastListener,
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
    @BindView(R.id.card_view_detail)
    CardView userdetails;
    @BindView(R.id.textError)
    TextView textError;
    @BindView(R.id.emialuser)
    LinearLayout emailView;

    private ArrayList<UserCustomer> userCustomers;
    private ArrayList<UserCustomer> userCustomerRemove;
    private Context instance;
    private static final String USERS_CUSTOMERS_KEY = "usersCustomersArray";
    private static final String KEY_USERS = "users_json";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private static final String ARG_PARAM6 = "param6";
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private Date fromDate;
    private Date toDate;
    private OnAddUserProject mListener;
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private String fromDateStr;
    private String toDateStr;
    private ArrayList<UserList> userLists;
    private UsersArrayAdapter usersArrayAdapter;
    private String idCompany = "";
    private String idProject;
    private String idUser = "";
    private int contexForm;
    private int processSendRequest = 0;
    private int processSuccesRequest = 0;
    private String[] rolList;
    private UserProject userProject;
    private ArrayList<Roles> rolInfoList;
    private ArrayList<UserProject> listUsers;
    private String finishDate;
    private Date finishDateContract;


    public static AddUserProject newInstanceActivityUser(String companId, String projectId, ArrayList<UserProject> userProjetList, String finishdate) {
        int ContextFrom = 0;
        AddUserProject fragment = new AddUserProject();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, companId);
        args.putString(ARG_PARAM2, projectId);
        args.putInt(ARG_PARAM3, ContextFrom);
        args.putParcelableArrayList(ARG_PARAM5, userProjetList);
        args.putString(ARG_PARAM6, finishdate);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddUserProject newInstanceActivityUserEdit(String companId, String projectId, UserProject userPro, String finishdate) {
        int ContextFrom = 1;
        AddUserProject fragment = new AddUserProject();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, companId);
        args.putString(ARG_PARAM2, projectId);
        args.putInt(ARG_PARAM3, ContextFrom);
        args.putParcelable(ARG_PARAM4, userPro);
        args.putString(ARG_PARAM6, finishdate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        userLists = new ArrayList<>();
        userCustomers = new ArrayList<>();
        if (getArguments() != null) {
           /* mTitle = getArguments().getString(ARG_TITLE);
            mURL = getArguments().getString(ARG_URL);
            mModel = (Validity) getArguments().getSerializable(ARG_MODEL);*/
            idCompany = getArguments().getString(ARG_PARAM1);
            idProject = getArguments().getString(ARG_PARAM2);
            contexForm = getArguments().getInt(ARG_PARAM3);
            finishDate = getArguments().getString(ARG_PARAM6);
            if (contexForm == 1) {
                userProject = getArguments().getParcelable(ARG_PARAM4);
                idUser = userProject.UserId;
                processSuccesRequest = 1;
            } else {
                listUsers = getArguments().getParcelableArrayList(ARG_PARAM5);

            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_user_project, container, false);
        unbinder = ButterKnife.bind(this, view);
        userdetails.setVisibility(View.GONE);
        if (contexForm == 1) emailView.setVisibility(View.GONE);
            init();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            //  startDateContract=format.parse(startDate);
            finishDateContract = format.parse(finishDate);
            // fromDate=startDateContract;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (contexForm == 1) {
            //userdetails.setVisibility(View.VISIBLE);
            if (userProject.StartDate != null && userProject.FinishDate != null) {
                try {
                    @SuppressLint("SimpleDateFormat")
                    Date date1 = format.parse(userProject.StartDate);
                    Date date2 = format.parse(userProject.FinishDate);
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                    fromDateBtn.setText(dateFormat.format(date1));
                    toDateBtn.setText(dateFormat.format(date2));
                    toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            userAutoCompleteTextView.setText(userProject.Email);
            userAutoCompleteTextView.setEnabled(false);
        }
        if (!idProject.equals("")) {
            String url = Constantes.LIST_PROJECTS_URL + idProject + "/Roles/";
            CrudIntentService.startRequestCRUD(instance, url,
                    Request.Method.GET, "", "", true);
        }
        serachUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processSuccesRequest = 0;
                String emailUser = userAutoCompleteTextView.getText().toString();
                if (!TextUtils.isEmpty(emailUser)) {
                    ContentValues values = new ContentValues();
                    values.put("project", true);
                    String paramsQuery = HttpRequest.makeParamsInUrl(values);

                    String url = Constantes.USER_BY_EMAIL_URL + emailUser;
                    CrudIntentService.startRequestCRUD(instance, url,
                            Request.Method.GET, "", "", false);
                }
            }
        });
        updateDatePicker(fromDate, toDate);

        return view;
    }

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
        if (finishDateContract != null)
            toDatePickerDialog.getDatePicker().setMaxDate(finishDateContract.getTime());
    }

    private void init() {
        instance = getActivity();

        userCustomerRemove = new ArrayList<>();
        String jsonArrayUsers = "";
        if (getArguments().containsKey(KEY_USERS))
            jsonArrayUsers = getArguments().getString(KEY_USERS);
        userLists = new Gson().fromJson(jsonArrayUsers, new TypeToken<ArrayList<UserList>>() {
        }.getType());

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

        if (roles != null) {
//            TextView rolView = roles.getSelectedView().findViewById(android.R.id.text1);
//            rolView.setError(null);
//            String role = rolView.getText().toString();
            textError.setVisibility(View.GONE);
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
//            else if (rolInfoList.isEmpty()) {
//                cancel = true;
//                rolView.setError("Campo obligatorio");
//            }
            else if (roles.getSelectedItemPosition() <= 0) {
                cancel = true;
                textError.setVisibility(View.VISIBLE);
                textError.setText("El Perfil es obligatorio");
            }

            if (!cancel) {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                fromDateStr = format.format(fromDate);
                toDateStr = format.format(toDate);
                UserProject user = new UserProject(fromDateStr, toDateStr, idUser, idCompany, rolInfoList.get(roles.getSelectedItemPosition() - 1).Id);
                Gson gson = new Gson();
                String json = gson.toJson(user);
                CrudIntentService.startRequestCRUD(getActivity(),
                        Constantes.LIST_PROJECTS_URL + idProject + "/Users/", Request.Method.POST, json, "", false);
            }
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

    private void Roles(String rolesResult) {
        Log.e("roles", rolesResult);
        rolInfoList = new Gson().fromJson(rolesResult,
                new TypeToken<ArrayList<Roles>>() {
                }.getType());

        rolList = new String[rolInfoList.size() + 1];
        rolList[0] = "PERFIL";
        for (int i = 0; i < rolInfoList.size(); i++) {
            Log.e("roles2", rolInfoList.get(i).Name);
            String[] nameRol = rolInfoList.get(i).Name.split(Pattern.quote("]"));
            rolList[i + 1] = nameRol[nameRol.length - 1];
        }
        roles.setAdapter(new CustomArrayAdapter(instance, rolList));
        if (contexForm == 1) {
            for (int i = 0; i < rolInfoList.size(); i++) {
                if (userProject.RoleId.equals(rolInfoList.get(i).Id)) {
                    roles.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    @Override
    public void onListAdminUserInteraction(int position, boolean remove) {

        UserCustomer userCustomer = userCustomers.get(position);
        if (remove)
            userCustomerRemove.add(userCustomer);
        else
            userCustomerRemove.remove(userCustomer);

    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        Log.e("option", " " + option + " " + jsonObjStr);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (processSuccesRequest == 0) {
                    processSuccesRequest = 1;
                    try {
                        JSONObject dates = new JSONObject(jsonObjStr);
                        idUser = dates.getString("Id");
                        MetodosPublicos.alertDialog(instance, "Usuario: " + dates.getString("Name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (mListener != null) {
                        mListener.onApplyActionUser();
                    }
                }
                break;
            case Constantes.SEND_REQUEST:
                if (processSendRequest == 0) {
                    Roles(jsonObjStr);
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
                        .setMessage("El usuario con correo" + userAutoCompleteTextView.getText().toString() +
                                " no existe, desea intentar con un correo diferente");
                builder.create().show();
                //MetodosPublicos.alertDialog(instance, "Comañia no existe, al guardar la información se creara");
                break;
        }
    }


    public interface OnAddUserProject {
        void onCancelActionUser();

        void onApplyActionUser();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddUserProject) {
            mListener = (OnAddUserProject) context;
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
