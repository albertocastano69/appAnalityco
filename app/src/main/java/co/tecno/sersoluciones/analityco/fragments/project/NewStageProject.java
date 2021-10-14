package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.ProjectStages;
import co.tecno.sersoluciones.analityco.models.Validity;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class NewStageProject extends Fragment implements RequestBroadcastReceiver.BroadcastListener, DatePickerDialog.OnDateSetListener {

    @BindView(R.id.tvFromDateError)
    TextView tvFromDateError;
    @BindView(R.id.tvToDateError)
    TextView tvToDateError;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;
    @BindView(R.id.edtt_name)
    EditText title;
    @BindView(R.id.edit_economic_activity)
    EditText review;
    @BindView(R.id.btn_from_date)
    Button fromDateBtn;
    @BindView(R.id.btn_to_date)
    Button toDateBtn;
   /* @BindView(R.id.progress)
    ProgressBar mProgressView;
    @BindView(R.id.main_form)
    CardView mFormView;*/

    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private String fromDateStr;
    private String toDateStr;
    private Date toDate;
    private Date fromDate;
    private Context instance;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private OnAddStageProject mListener;
    private String idProject;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private View view;
    private int contexForm;
    private ProjectStages projectStages;

    public static NewStageProject newInstance(String id_project) {
        int ContextFrom = 0;
        NewStageProject fragment = new NewStageProject();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, id_project);
        args.putInt(ARG_PARAM2, ContextFrom);
        fragment.setArguments(args);
        return fragment;
    }

    public static NewStageProject newInstanceEdit(String id_project, ProjectStages ProjectStages) {
        int ContextFrom = 1;
        NewStageProject fragment = new NewStageProject();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, id_project);
        args.putInt(ARG_PARAM2, ContextFrom);
        args.putParcelable(ARG_PARAM3, ProjectStages);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        if (getArguments() != null) {
           /* mTitle = getArguments().getString(ARG_TITLE);
            mURL = getArguments().getString(ARG_URL);
            mModel = (Validity) getArguments().getSerializable(ARG_MODEL);*/
            idProject = getArguments().getString(ARG_PARAM1);
            contexForm = getArguments().getInt(ARG_PARAM2);
            if (contexForm == 1) {
                projectStages = getArguments().getParcelable(ARG_PARAM3);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_new_stage_project, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
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

        if (contexForm == 1) {
            title.setText(projectStages.Name);
            review.setText(projectStages.Review);

            try {
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date date1 = format.parse(projectStages.StartDate);
                Date date2 = format.parse(projectStages.FinishDate);
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                fromDateBtn.setText(dateFormat.format(date1));
                toDateBtn.setText(dateFormat.format(date2));
                toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
            fromDateBtn.setEnabled(false);
        }
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

    @OnClick(R.id.positive_button)
    public void create() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        fromDateStr = format.format(fromDate);
        toDateStr = format.format(toDate);
        ProjectStages stageProject = new ProjectStages(fromDateStr, toDateStr, idProject, title.getText().toString(), true, review.getText().toString());
        Gson gson = new Gson();

        if (contexForm == 1) {
            stageProject.setId(projectStages.getId());
            String json = gson.toJson(stageProject);
            CrudIntentService.startRequestCRUD(getActivity(),
                    Constantes.NEW_STAGE_URL + stageProject.getId(), Request.Method.PUT, json, "", false);
        } else {
            String json = gson.toJson(stageProject);
            CrudIntentService.startRequestCRUD(getActivity(),
                    Constantes.NEW_STAGE_URL, Request.Method.POST, json, "", false);
        }


    }

    @OnClick(R.id.negative_button)
    public void cancel() {
        if (mListener != null) {
            mListener.onCancelActionStage();
        }
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

                break;
            case R.id.btn_to_date:
                toDateStr = sdf.format(calendar.getTime());
                toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                toDateBtn.setText(toDateStr);
                toDate = calendar.getTime();
                toDateBtn.setError(null);
                break;
        }
    }


    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:
                if (mListener != null) {
                    mListener.onApplyActionStage();
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

                break;
        }
    }

    public interface OnAddStageProject {
        void onCancelActionStage();

        void onApplyActionStage();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddStageProject) {
            mListener = (OnAddStageProject) context;
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
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_PUT);
        LocalBroadcastManager.getInstance(instance).registerReceiver(requestBroadcastReceiver,
                intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(instance).unregisterReceiver(requestBroadcastReceiver);
    }
}
