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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.User;

/**
 * Created by Ser Soluciones SAS on 05/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class EditProfileDialogFragment extends DialogFragment {

    private static final String KEY_PROFILE = "profile";
    @BindView(R.id.negative_button)
    Button negativeButton;
    @BindView(R.id.positive_button)
    Button positiveButton;

    @BindView(R.id.edit_name)
    EditText editName;
    @BindView(R.id.edit_lastname)
    EditText editLastname;
    @BindView(R.id.edit_phone)
    EditText editPhone;
    private Unbinder unbinder;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface EditProfileDialogListener {
        void onDialogEditProfileClick(JSONObject jsonObject);
    }

    // Use this instance of the interface to deliver action events
    private EditProfileDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (EditProfileDialogListener) activity;
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
        View dialogView = inflater.inflate(R.layout.edit_profile_dialog, null);
        unbinder = ButterKnife.bind(this, dialogView);
        builder.setView(dialogView);
        builder.setMessage("Editar Perfil");
        builder.setTitle("Formulario");
        builder.setCancelable(false);
        User user = null;
        if (getArguments().containsKey(KEY_PROFILE))
            user = (User) getArguments().getSerializable(KEY_PROFILE);
        editName.setText(user.Name);
        editLastname.setText(user.LastName);
        editPhone.setText(user.PhoneNumber);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validationFields()) {

                    String name = editName.getText().toString();
                    String lastname = editLastname.getText().toString();
                    String phone = editPhone.getText().toString();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("name", name);
                        jsonObject.put("lastname", lastname);
                        jsonObject.put("phone", phone);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mListener.onDialogEditProfileClick(jsonObject);

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

        editName.setError(null);
        editLastname.setError(null);
        editPhone.setError(null);

        String name = editName.getText().toString();
        String lastname = editLastname.getText().toString();
        String phone = editPhone.getText().toString();

        if (TextUtils.isEmpty(name)) {
            editName.setError(getString(R.string.error_field_required));
            focusView = editName;
            cancel = true;
        } else if (TextUtils.isEmpty(lastname)) {
            editLastname.setError(getString(R.string.error_field_required));
            focusView = editLastname;
            cancel = true;
        } else if (TextUtils.isEmpty(phone)) {
            editPhone.setError(getString(R.string.error_field_required));
            focusView = editPhone;
            cancel = true;
        }


        if (cancel) {
            focusView.requestFocus();
            return false;
        }

        return true;
    }


}

