package co.tecno.sersoluciones.analityco.fragments.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

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


public class NewProjectStagesFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

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
    private DatePickerDialog fromDatePickerDialog, toDatePickerDialog;
    private String fromDateStr;
    private String toDateStr;
    private Date toDate;
    private Date fromDate;
    private Context instance;
    private View view;
    private ProjectStages projectStagesEdit;


    public static NewProjectStagesFragment newInstance() {
        return new NewProjectStagesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        updateDatePicker(fromDate, toDate);

        if (getArguments() != null) {
            projectStagesEdit = getArguments().getParcelable("mItem");
            title.setText(projectStagesEdit.Name);
            review.setText(projectStagesEdit.Review);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            SimpleDateFormat format2 = new SimpleDateFormat(myFormat, new Locale("es", "ES"));
            try {
                toDateBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                Date date1 = format.parse(projectStagesEdit.StartDate);
                Date date2 = format.parse(projectStagesEdit.FinishDate);
                String startDate = format2.format(date1);
                String finishDate = format2.format(date2);
                fromDateBtn.setText(startDate);
                fromDate = format2.parse(startDate);
                toDateBtn.setText(finishDate);
                toDate = format2.parse(finishDate);
                toDateStr = finishDate;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return view;
    }

    private void cancelForm() {

        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_CANCELED, null
        );
    }

    private void applyForm(ProjectStages newComp) {
        boolean isEditItem = false;
        ProjectStages projectStagesRemove = new ProjectStages();
        if (getArguments() != null) {
            isEditItem = true;
            projectStagesRemove = projectStagesEdit;
        }
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                new Intent().putExtra("newStage", newComp)
                        .putExtra("isEdit", isEditItem)
                        .putExtra("removeItem", projectStagesRemove)
        );
    }

    private void updateDatePicker(Date fromDate, Date toDate) {
        Calendar cF = Calendar.getInstance();
        cF.setTime(fromDate);

        Calendar cT = Calendar.getInstance();
        cT.setTime(toDate);
        fromDatePickerDialog = new DatePickerDialog(instance, this, cF.get(Calendar.YEAR),
                cF.get(Calendar.MONTH), cF.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());


        toDatePickerDialog = new DatePickerDialog(instance, this, cT.get(Calendar.YEAR),
                cT.get(Calendar.MONTH), cT.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

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
        /* unbinder.unbind(); */
    }

    @OnClick(R.id.positive_button)
    public void create() {
        if (sumbitRequest()) {
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            fromDateStr = format.format(fromDate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(toDate);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);

            toDateStr = format.format(calendar.getTime());
//            toDateStr = format.format(toDate);
            ProjectStages stageProject = new ProjectStages(fromDateStr, toDateStr, title.getText().toString(), review.getText().toString());
            applyForm(stageProject);
        }
    }

    @OnClick(R.id.negative_button)
    public void cancel() {
        cancelForm();
    }

    private boolean sumbitRequest() {
        // Reset errors.
        title.setError(null);
        review.setError(null);

        String titleStage = title.getText().toString();
        String reviewStage = review.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(titleStage)) {
            title.setError("Requerido");
            title.requestFocus();
            focusView = title;
            cancel = true;
        } else if (TextUtils.isEmpty(reviewStage)) {
            review.setError(getString(R.string.error_field_required));
            focusView = review;
            cancel = true;
        } else if (fromDateStr.isEmpty()) {
            fromDateBtn.setError(getString(R.string.error_field_required));
            fromDateBtn.requestFocus();

            tvFromDateError.setVisibility(View.VISIBLE);
            tvFromDateError.setError(getString(R.string.error_field_required));
            focusView = tvFromDateError;
            cancel = true;
        } else if (toDateStr == null) {
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
        } else {
            return true;
        }
    }

}
