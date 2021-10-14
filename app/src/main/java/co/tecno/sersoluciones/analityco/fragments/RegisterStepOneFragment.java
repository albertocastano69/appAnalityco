package co.tecno.sersoluciones.analityco.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.facedetectorser.serlibrary.PhotoSer;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.RegisterActivity;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static co.com.sersoluciones.facedetectorser.FaceTrackerActivity.URI_IMAGE_KEY;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class RegisterStepOneFragment extends Fragment {

    @BindView(R.id.icon_logo)
    CircleImageView iconLogo;
    @BindView(R.id.fab_remove)
    FloatingActionButton fabRemove;
    @BindView(R.id.fab_rotate)
    FloatingActionButton fabRotate;
    @BindView(R.id.user_id)
    EditText mEmailView;
    @BindView(R.id.password)
    EditText mPasswordView;
    @BindView(R.id.float_label_password)
    TextInputLayout floatLabelPassword;
    @BindView(R.id.user_phone)
    EditText mPhoneView;
    private Unbinder unbinder;

    private Uri mImageUri;
    private int rotateImage = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUri = null;
        rotateImage = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_step1, container, false);
        unbinder = ButterKnife.bind(this, view);

        iconLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        User user = ((RegisterActivity) getActivity()).getUser();
        if (user != null && user.imagePath != null && !user.imagePath.isEmpty()) {
            mImageUri = Uri.parse(user.imagePath);
            rotateImage = user.imageRotate;
            iconLogo.setImageURI(mImageUri);
            fabRemove.setVisibility(View.VISIBLE);
        }
        return view;
    }

    public void sumbitRequest() {

        // Reset errors.
        // Store values at the time of the login attempt.

        String username = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String phone = mPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(username)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("La contraseña debe contener al menos 6 digitos, 1 mayúscula, 1 minúscula  y un número");
            focusView = mPasswordView;
            cancel = true;
        } else if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isPhoneValid(phone)) {
            mPhoneView.setError("Digite un numero telefonico valido");
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {


            focusView.requestFocus();
        } else {
            User user = ((RegisterActivity) getActivity()).getUser();
            user.Email = username;
            user.Password = password;
            user.ConfirmPassword = password;
            user.PhoneNumber = phone;
            user.DocumentType = "CC";
            if (mImageUri != null)
                user.imagePath = mImageUri.toString();
            user.imageRotate = rotateImage;
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            ((RegisterActivity) getActivity()).nextPage(1);
        }
    }

    private boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        final String PASSWORD_PATTERN = "^((?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])).{6,}$";

        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        return pattern.matcher(password).matches();
    }

    private boolean isPhoneValid(String phone) {
        Pattern pattern = Patterns.PHONE;
        return pattern.matcher(phone).matches();
    }

    @OnClick(R.id.fab_rotate)
    public void rotateLogo() {
        rotateImage += 90;
        if (rotateImage == 360) rotateImage = 0;
        Bitmap bitmap = ((BitmapDrawable) iconLogo.getDrawable()).getBitmap();
        bitmap = MetodosPublicos.rotateImage(bitmap, 90);
        iconLogo.setImageBitmap(bitmap);
    }

    @OnClick(R.id.fab_remove)
    public void removeLogo() {
//        Drawable drawable = MaterialDrawableBuilder.with(getActivity()) // provide a context
//                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE) // provide an icon
//                .setColor(Color.GRAY)
//                .build();
        iconLogo.setImageResource(R.drawable.profile_dummy);
        fabRemove.setVisibility(View.GONE);
        fabRotate.setVisibility(View.GONE);
        mImageUri = null;
    }

    private void takePhoto() {
        startFaceDectectorActivity();
    }

    private void startFaceDectectorActivity() {
        new PhotoSer.ActivityBuilder()
                .setDetectFace(true)
                .start(this, getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        log("enta aca onActivityResult");
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == PhotoSer.SER_IMAGE_ACTIVITY_REQUEST_CODE) {
                    mImageUri = Uri.parse(data.getStringExtra(URI_IMAGE_KEY));
                    iconLogo.setImageURI(mImageUri);
                    fabRemove.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


}
