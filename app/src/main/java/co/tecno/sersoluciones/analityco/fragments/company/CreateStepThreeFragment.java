package co.tecno.sersoluciones.analityco.fragments.company;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.adapters.AdminUsersOfficesRecyclerView;
import co.tecno.sersoluciones.analityco.models.Company;
import co.tecno.sersoluciones.analityco.models.UserAdmin;
import co.tecno.sersoluciones.analityco.models.UserCustomer;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class CreateStepThreeFragment extends Fragment implements RequestBroadcastReceiver.BroadcastListener,
        DatePickerDialog.OnDateSetListener {


    private Unbinder unbinder;
    @BindView(R.id.label_users)
    TextView labelUsers;
    @BindView(R.id.fab_user)
    FloatingActionButton fabUser;
    @BindView(R.id.view_user)
    View viewUser;
    @BindView(R.id.list_users_recycler)
    RecyclerView recyclerViewUsers;
    @BindView(R.id.users_view)
    CardView usersView;

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

    @BindView(R.id.layout_main_user)
    LinearLayout layoutMainUser;
    @BindView(R.id.layout_users)
    RelativeLayout layoutUsers;
    @BindView(R.id.layout_btns)
    LinearLayout layoutBtns;

    @BindView(R.id.logo)
    ImageView mLogoView;
    @BindView(R.id.text_name)
    TextView mNameUserView;
    @BindView(R.id.text_sub_name)
    TextView mSubNameUserView;
    @BindView(R.id.card_view_detail)
    View cardViewUser;
    @BindView(R.id.label_validity)
    TextView mTextValidityView;
    @BindView(R.id.text_active)
    TextView mTextActiveView;


    private AdminUsersOfficesRecyclerView adapterUser;
    private ArrayList<UserCustomer> userCustomers;
    private Context instance;

    private static final String USERS_CUSTOMERS_KEY = "usersCustomersArray";

    private Date fromDate;
    private Date toDate;

    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private String fromDateStr;
    private String toDateStr;
    private boolean searchUser;
    private UserCustomer userCustomerMain;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userCustomers = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_company_step_3, container, false);
        unbinder = ButterKnife.bind(this, view);
        init();
        return view;
    }

    private void init() {
        instance = getActivity();
        cardViewUser.setVisibility(View.GONE);
        mTextValidityView.setVisibility(View.GONE);
        mTextActiveView.setVisibility(View.GONE);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);

        fabUser.setOnClickListener(view -> {
            mNameUserView.setText("");
            mSubNameUserView.setText("");
            mLogoView.setImageResource(R.drawable.image_not_available);
            searchUser = false;
            cardViewUser.setVisibility(View.GONE);
            recyclerViewUsers.setVisibility(View.GONE);
            layoutMainUser.setVisibility(View.VISIBLE);
            layoutBtns.setVisibility(View.VISIBLE);
            layoutUsers.setVisibility(View.GONE);
        });

        LinearLayoutManager mLayoutManagerUsers = new LinearLayoutManager(instance);
        recyclerViewUsers.setLayoutManager(mLayoutManagerUsers);
        adapterUser = new AdminUsersOfficesRecyclerView(instance, (position, remove) -> {

        });
        recyclerViewUsers.setItemAnimator(new DefaultItemAnimator());

        recyclerViewUsers.setAdapter(adapterUser);
        recyclerViewUsers.setVisibility(View.GONE);

        searchUser = false;
        userCustomerMain = new UserCustomer();
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

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        LocalBroadcastManager.getInstance(instance).registerReceiver(requestBroadcastReceiver,
                intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(requestBroadcastReceiver);
    }

    public void configDatePickers(Company company) {
        updateDatePicker(fromDate, toDate, company);
    }

    private void updateDatePicker(Date fromDate, Date toDate, Company company) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);

        Calendar cT = Calendar.getInstance();
        cT.setTime(toDate);
        fromDatePickerDialog = new DatePickerDialog(getActivity(), this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        toDatePickerDialog = new DatePickerDialog(getActivity(), this, cT.get(Calendar.YEAR),
                cT.get(Calendar.MONTH), cT.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            toDatePickerDialog.getDatePicker().setMaxDate(dateFormat.parse(company.FinishDate).getTime());
            fromDatePickerDialog.getDatePicker().setMaxDate(dateFormat.parse(company.FinishDate).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
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


    @OnClick(R.id.fab_search)
    public void searchUser() {
        //processSuccesRequest = 0;
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
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void onAdminUserDialogClick(UserCustomer userCustomer) {
        for (UserCustomer user : userCustomers) {
            if (user.UserId.equals(userCustomer.UserId)) return;
        }
        recyclerViewUsers.setVisibility(View.VISIBLE);
        userCustomers.add(userCustomer);
        adapterUser.update();
        adapterUser.swap(userCustomers);
    }

    public ArrayList<UserCustomer> getUsers() {
        return userCustomers;
    }

    @OnClick(R.id.link_button)
    public void sumbitRequest() {
        View focusView = null;
        boolean cancel = false;

        if (!searchUser) {
            userAutoCompleteTextView.setError(getString(R.string.error_field_required));
            focusView = userAutoCompleteTextView;
            cancel = true;
        } else if (fromDateStr.isEmpty()) {
            fromDateBtn.setError(getString(R.string.error_field_required));
            fromDateBtn.requestFocus();

            tvFromDateError.setVisibility(View.VISIBLE);
            tvFromDateError.setError(getString(R.string.error_field_required));
            focusView = tvFromDateError;
            cancel = true;
        } else if (toDateStr.isEmpty()) {
            toDateBtn.setError(getString(R.string.error_field_required));
            toDateBtn.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError(getString(R.string.error_field_required));
            focusView = tvToDateError;
            cancel = true;
        } else if (fromDate.getTime() > toDate.getTime()) {
            toDateBtn.setError("La fecha debe ser mayor a la de inicio");
            toDateBtn.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError("La fecha debe ser mayor a la de inicio");
            focusView = tvToDateError;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
            return;
        }

        userCustomerMain.FinishDate = toDate;
        for (UserCustomer userCustomer : userCustomers) {
            if (userCustomer.UserId.equals(userCustomerMain.UserId)) {
                cleanForm();
                Toast.makeText(instance, "Usuario ya vinculado", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        userCustomers.add(userCustomerMain);
        recyclerViewUsers.setVisibility(View.VISIBLE);
        adapterUser.update();
        adapterUser.swap(userCustomers);

        cleanForm();
    }

    private void cleanForm() {
        layoutMainUser.setVisibility(View.GONE);
        layoutBtns.setVisibility(View.GONE);
        layoutUsers.setVisibility(View.VISIBLE);

        searchUser = false;
        toDateStr = "";
        toDateBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar, 0, 0, 0);
        toDateBtn.setText("");
        userAutoCompleteTextView.setText("");
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        //logE("option: " + option + " " + jsonObjStr);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                setUserInfo(jsonObjStr);
                break;
            case Constantes.BAD_REQUEST:
                break;
            case Constantes.TIME_OUT_REQUEST:
                break;
            case Constantes.REQUEST_NOT_FOUND:
                break;
        }
    }

    private void setUserInfo(String jsonObjStr) {
        UserAdmin user = new Gson().fromJson(jsonObjStr,
                new TypeToken<UserAdmin>() {
                }.getType());
        String userId = user.Id;
        mNameUserView.setText(String.format("%s %s", user.Name, user.LastName));
        mSubNameUserView.setText(user.UserName);
        cardViewUser.setVisibility(View.VISIBLE);
//        text_active.setVisibility(View.GONE);
//        text_active.setVisibility(View.GONE);
//        card_view_detail.setVisibility(View.VISIBLE);
        mLogoView.setImageResource(R.drawable.image_not_available);
        if (user.Photo != null) {
            String urlphoto = Constantes.URL_IMAGES + user.Photo;
            Picasso.get().load(urlphoto)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(mLogoView);
        }
        searchUser = true;
        userCustomerMain.UserId = userId;
        userCustomerMain.IsActive = true;
        userCustomerMain.Name = user.Name;
        userCustomerMain.LastName = user.LastName;
        userCustomerMain.PhotoUrl = user.Photo;
        userCustomerMain.UserEmail = user.UserName;
    }
}
