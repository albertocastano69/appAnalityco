package co.tecno.sersoluciones.analityco.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;


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
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.RequestPatch;
import co.tecno.sersoluciones.analityco.models.Validity;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ValidityFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ValidityFragment extends Fragment implements DatePickerDialog.OnDateSetListener, RequestBroadcastReceiver.BroadcastListener {

    private static final String ARG_TITLE = "name";
    private static final String ARG_URL = "url";
    private static final String ARG_MODEL = "model";

    @BindView(R.id.btn_from_date)
    Button btnFromDate;
    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    @BindView(R.id.btn_to_date)
    Button btnToDate;
    @BindView(R.id.tvToDateError)
    TextView tvToDateError;
    private Unbinder unbinder;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;
    @BindView(R.id.label_active)
    TextView labelActive;
    @BindView(R.id.progress)
    ProgressBar mProgressView;
    @BindView(R.id.main_form)
    CardView mFormView;

    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private String fromDateStr;
    private String toDateStr;
    private Date toDate;
    private Date fromDate;

    private String mTitle;
    private String mURL;

    private OnValidityFragmentInteractionListener mListener;
    private Validity mModel;
    private RequestBroadcastReceiver requestBroadcastReceiver;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param url    Parameter 2.
     * @return A new instance of fragment ValidityFragment.
     */
    public static ValidityFragment newInstance(String param1, String url, Validity model) {
        ValidityFragment fragment = new ValidityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, param1);
        args.putString(ARG_URL, url);
        args.putSerializable(ARG_MODEL, model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mURL = getArguments().getString(ARG_URL);
            mModel = (Validity) getArguments().getSerializable(ARG_MODEL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.base_validity_layout, container, false);
        unbinder = ButterKnife.bind(this, view);

      /*  ((TextView) view.findViewById(R.id.title_card_validity)).setText(
                String.format("VIGENCIA DEL %s", mTitle));*/
        labelActive.setText(String.format("%s ACTIVO:", mTitle));
        Calendar calendar = Calendar.getInstance();
        fromDate = calendar.getTime();
        fromDate = mModel.StartDate;
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        toDate = calendar.getTime();
        toDate = mModel.FinishDate;

        String myFormat = "dd/MMM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
        fromDateStr = sdf.format(fromDate);
        btnFromDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        btnFromDate.setText(fromDateStr);

        if(toDate==null){
            toDate=fromDate;
        }
        toDateStr = sdf.format(toDate);
        btnToDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        btnToDate.setText(toDateStr);

        //switchActive.setChecked(mModel.IsActive);
        updateDatePicker(fromDate, toDate);
        return view;
    }

    @OnClick(R.id.btn_from_date)
    public void fromDateDialog() {
        fromDatePickerDialog.getDatePicker().setTag(R.id.btn_from_date);
        fromDatePickerDialog.show();
    }

    @OnClick(R.id.btn_to_date)
    public void toDateDialog() {
        toDatePickerDialog.getDatePicker().setTag(R.id.btn_to_date);
        toDatePickerDialog.show();
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
                btnFromDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                btnFromDate.setText(fromDateStr);
                fromDate = calendar.getTime();
                btnFromDate.setError(null);

                tvFromDateError.setVisibility(View.GONE);
                tvFromDateError.setError(null);
                break;
            case R.id.btn_to_date:
                toDateStr = sdf.format(calendar.getTime());
                btnToDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                btnToDate.setText(toDateStr);
                toDate = calendar.getTime();
                btnToDate.setError(null);

                tvToDateError.setVisibility(View.GONE);
                tvToDateError.setError(null);
                break;
        }
    }

    @OnClick(R.id.negative_button)
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onCancelValidityFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnValidityFragmentInteractionListener) {
            mListener = (OnValidityFragmentInteractionListener) context;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PATCH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        log("ON PAUSE");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(requestBroadcastReceiver);
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

        mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onStringResult(String action, int option, String res,String url) {
        showProgress(false);
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (mListener != null) {
                    mListener.onValidityFragmentInteraction();
                }
                break;
            case Constantes.REQUEST_NOT_FOUND:

                break;
            case Constantes.SUCCESS_FILE_UPLOAD:
                break;
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
                break;
        }
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
    public interface OnValidityFragmentInteractionListener {
        void onValidityFragmentInteraction();
        void onCancelValidityFragmentInteraction();
    }

    @OnClick(R.id.positive_button)
    public void sumbitRequest() {
        boolean cancel = false;
        View focusView = null;

        if (fromDateStr.isEmpty()) {
            btnFromDate.setError(getString(R.string.error_field_required));
            btnFromDate.requestFocus();

            tvFromDateError.setVisibility(View.VISIBLE);
            tvFromDateError.setError(getString(R.string.error_field_required));
            focusView = tvFromDateError;
            cancel = true;
        } else if (toDateStr.isEmpty()) {
            btnToDate.setError(getString(R.string.error_field_required));
            btnToDate.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError(getString(R.string.error_field_required));
            focusView = tvToDateError;
            cancel = true;
        } else if (fromDate.getTime() > toDate.getTime()) {
            btnToDate.setError("La fecha debe ser mayor a la de inicio");
            btnToDate.requestFocus();

            tvToDateError.setVisibility(View.VISIBLE);
            tvToDateError.setError("La fecha debe ser mayor a la de inicio");
            focusView = tvToDateError;
            cancel = true;
        }

        //boolean isActive = switchActive.isChecked();
        if (cancel) {


            focusView.requestFocus();
        } else {
            ArrayList<RequestPatch> requestPatches = new ArrayList<>();
            //requestPatches.add(new RequestPatch(DBHelper.COMPANY_TABLE_COLUMN_ACTIVE, isActive));
            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                String StartDate = format.format(fromDate);
                String FinishDate = format.format(toDate);
                requestPatches.add(new RequestPatch(DBHelper.COMPANY_TABLE_COLUMN_START_DATE, StartDate));
                requestPatches.add(new RequestPatch(DBHelper.COMPANY_TABLE_COLUMN_FINISH_DATE, FinishDate));
            } catch (Exception e) {
                e.printStackTrace();
            }
            /*requestPatches.add(new RequestPatch(DBHelper.COMPANY_TABLE_COLUMN_START_DATE, fromDate));
            requestPatches.add(new RequestPatch(DBHelper.COMPANY_TABLE_COLUMN_FINISH_DATE, toDate));*/

            final String json = new Gson().toJson(requestPatches);
            sendFormToServer(json);
        }
    }

    private void sendFormToServer(String json) {
        showProgress(true);
        CrudIntentService.startRequestPatch(getActivity(), mURL, json);
    }
}
