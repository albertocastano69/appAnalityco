package co.tecno.sersoluciones.analityco.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;

/**
 * Created by Ser Soluciones SAS on 05/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class ChangePasswordDialogFragment extends DialogFragment {

    @BindView(R.id.edit_current_pass)
    EditText editCurrentPass;
    @BindView(R.id.edit_pass)
    EditText editPass;
    @BindView(R.id.edit_confirm_pass)
    EditText editConfirmPass;
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;
    private Unbinder unbinder;


    public DialogFragment newInstance() {

        //Bundle args = new Bundle();
        return new ChangePasswordDialogFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        void onDialogPositiveClick(JSONObject jsonObject);
    }

    // Use this instance of the interface to deliver action events
    private NoticeDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Build the dialog and set up the button click handlers
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.change_password_dialog, null);
        unbinder = ButterKnife.bind(this, dialogView);
        builder.setView(dialogView);
        builder.setMessage("Cambiar contraseña");
        //builder.setTitle("Cambiar contraseña");
        builder.setCancelable(false);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validationFields()) {

                    String currentPass = editCurrentPass.getText().toString();
                    String newPass = editPass.getText().toString();
                    String confirmPass = editConfirmPass.getText().toString();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("OldPassword", currentPass);
                        jsonObject.put("NewPassword", newPass);
                        jsonObject.put("ConfirmPassword", confirmPass);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mListener.onDialogPositiveClick(jsonObject);

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


    private boolean validationFields() {
        View focusView = null;
        boolean cancel = false;

        editCurrentPass.setError(null);
        editPass.setError(null);
        editConfirmPass.setError(null);

        String currentPass = editCurrentPass.getText().toString();
        String newPass = editPass.getText().toString();
        String confirmPass = editConfirmPass.getText().toString();

        if (TextUtils.isEmpty(currentPass)) {
            editCurrentPass.setError(getString(R.string.error_field_required));
            focusView = editCurrentPass;
            cancel = true;
        } else if (TextUtils.isEmpty(newPass)) {
            editPass.setError(getString(R.string.error_field_required));
            focusView = editPass;
            cancel = true;
        } else if (!isPasswordValid(newPass)) {
            editPass.setError("La contraseña debe contener al menos 6 digitos, 1 mayuscula y un número");
            focusView = editPass;
            cancel = true;
        } else if (TextUtils.isEmpty(confirmPass)) {
            editConfirmPass.setError(getString(R.string.error_field_required));
            focusView = editConfirmPass;
            cancel = true;
        } else if (!confirmPass.equals(newPass)) {
            editConfirmPass.setError("La contraseña no coincide");
            focusView = editConfirmPass;
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isPasswordValid(String password) {
        final String PASSWORD_PATTERN = "^((?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])).{6,}$";

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        return pattern.matcher(password).matches();
    }

}

