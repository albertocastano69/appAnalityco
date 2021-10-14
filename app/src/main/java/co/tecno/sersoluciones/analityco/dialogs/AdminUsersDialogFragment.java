package co.tecno.sersoluciones.analityco.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


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
import co.tecno.sersoluciones.analityco.adapters.UsersArrayAdapter;
import co.tecno.sersoluciones.analityco.models.UserCustomer;
import co.tecno.sersoluciones.analityco.models.UserList;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleAutoCompleteTextView;

/**
 * Created by Ser Soluciones SAS on 13/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class AdminUsersDialogFragment extends DialogFragment implements
        TextWatcherAdapter.TextWatcherListener, ClearebleAutoCompleteTextView.Listener, DatePickerDialog.OnDateSetListener {

    private static final String KEY_USERS = "users_json";
    private static final String KEY_MAX_DATE = "max_date";

    private String userId;
    private String userEmail;
    private boolean searchUser;

    private Date fromDate;
    private Date toDate;

    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private String fromDateStr;
    private String toDateStr;
    private ArrayList<UserList> userLists;
    private Date maxDate;

    @BindView(R.id.edit_users)
    ClearebleAutoCompleteTextView userAutoCompleteTextView;
    @BindView(R.id.btn_from_date)
    Button fromDateBtn;
    @BindView(R.id.tvToDateError)
    TextView tvToDateError;
    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    @BindView(R.id.btn_to_date)
    Button toDateBtn;
    private Unbinder unbinder;
    @BindView(R.id.switch_active)
    SwitchCompat switchActive;

    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;

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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AdminUserDialogListener {
        void onAdminUserDialogClick(UserCustomer userCustomer);
    }

    // Use this instance of the interface to deliver action events
    private AdminUserDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AdminUserDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Build the dialog and set up the button click handlers
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.admin_user_dialog, null);
        unbinder = ButterKnife.bind(this, dialogView);
        builder.setView(dialogView);
        long maxDateMillis = 0;
        builder.setTitle("USUARIO ADMINISTRADOR");
        String jsonArrayUsers = "";
        if (getArguments().containsKey(KEY_USERS))
            jsonArrayUsers = getArguments().getString(KEY_USERS);
        if (getArguments().containsKey(KEY_MAX_DATE))
            maxDateMillis = getArguments().getLong(KEY_MAX_DATE);

        maxDate = new Date(maxDateMillis);
        userLists = new Gson().fromJson(jsonArrayUsers, new TypeToken<ArrayList<UserList>>() {
        }.getType());

        searchUser = false;
        populateUserAutoComplete();
        userId = "";

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

        updateDatePicker(fromDate, toDate);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validationFields()) {

                    UserCustomer userCustomer = new UserCustomer();
                    userCustomer.UserEmail = userEmail;
                    userCustomer.UserId = userId;
                    userCustomer.StartDate = fromDate;
                    userCustomer.FinishDate = toDate;
                    userCustomer.IsActive = switchActive.isChecked();

                    mListener.onAdminUserDialogClick(userCustomer);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dismiss();
                        }
                    }, 10);
                }
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }, 10);
            }
        });

        return builder.create();
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

    private void updateDatePicker(Date fromDate, Date toDate) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);

        Calendar cT = Calendar.getInstance();
        cT.setTime(toDate);
        fromDatePickerDialog = new DatePickerDialog(getContext(), this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog = new DatePickerDialog(getContext(), this, cT.get(Calendar.YEAR),
                cT.get(Calendar.MONTH), cT.get(Calendar.DAY_OF_MONTH));

        Date currentDate = MetodosPublicos.getCurrentDate();
        fromDatePickerDialog.getDatePicker().setMinDate(currentDate.getTime());
        toDatePickerDialog.getDatePicker().setMinDate(currentDate.getTime());
        if (maxDate.getTime() > currentDate.getTime()) {
            toDatePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
            fromDatePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
        }
    }

    private void populateUserAutoComplete() {

        UsersArrayAdapter adapter = new UsersArrayAdapter(getActivity(), userLists);

        userAutoCompleteTextView.setThreshold(1);
        userAutoCompleteTextView.setAdapter(adapter);
        userAutoCompleteTextView.setListener(this);
        userAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (userAutoCompleteTextView.isFocused()) {
                    String text = parent.getItemAtPosition(position).toString();
                    UserList user = (UserList) parent.getItemAtPosition(position);
                    userId = user.Id;
                    userEmail = user.UserName;
                    userAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchUser = true;
                }
            }
        });
        userAutoCompleteTextView.addTextChangedListener(new TextWatcherAdapter(userAutoCompleteTextView, this));
    }

    @Override
    public void onTextChanged(EditText view, String text) {
        switch (view.getId()) {
            case R.id.edit_users:
                if (userAutoCompleteTextView.isFocused()) {
                    userAutoCompleteTextView.setClearIconVisible(!text.isEmpty());
                    searchUser = false;
                }
                break;
        }
    }

    @Override
    public void didClearText(View view) {
        searchUser = false;
    }


    private boolean validationFields() {
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
            return false;
        }
        return true;
    }

}

