package co.tecno.sersoluciones.analityco.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.RegisterActivity;
import co.tecno.sersoluciones.analityco.databases.DBHelper;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.processBarcode;

//import co.tecno.sersoluciones.analityco.BarcodeDecoderActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 */
public class RegisterStepTwoFragment extends Fragment {
    @BindView(R.id.read_barcode)
    Button readBarcode;
    @BindView(R.id.tvScanButtonError)
    TextView tvScanButtonError;

    private Unbinder unbinder;
    @BindView(R.id.image_dni)
    ImageView imageDni;
    @BindView(R.id.info_button)
    Button infoButton;
    @BindView(R.id.layout_info)
    CardView layoutInfo;

    private DecodeBarcode.InfoUser infoUser;
    private String cityCode;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoUser = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register_step_two, container, false);
        unbinder = ButterKnife.bind(this, view);

        return view;
    }

    @OnClick({R.id.read_barcode, R.id.image_dni})
    public void scanDNI() {
        startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
    }

    @OnClick(R.id.info_button)
    public void hideInfo() {
        layoutInfo.setVisibility(View.GONE);
        readBarcode.setEnabled(true);
        imageDni.setEnabled(true);
    }

    private void selectPosCitySpinner(DecodeBarcode.InfoUser infoUser) {
        logW("city: " + infoUser.cityCode + ", state: " + infoUser.stateCode);
        String select = "(" + DBHelper.CITY_TABLE_COLUMN_CODE + " = ? ) and ( " + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? )";
        logE(String.format("%s %s", infoUser.cityCode, infoUser.stateCode));
        String[] selectArgs = {String.format("%s%s", infoUser.stateCode, infoUser.cityCode), String.valueOf(0)};
        @SuppressLint("Recycle")
        Cursor cursor = getActivity().getContentResolver().query(Constantes.CONTENT_CITY_URI, null, select, selectArgs,
                DBHelper.CITY_TABLE_COLUMN_NAME);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            cityCode = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case BarcodeDecodeSerActivity.SUCCESS:
                if (data.hasExtra("barcode")) {
                    String barcodeRes = data.getStringExtra("barcode");
                    int barcodeType = data.getIntExtra("barcode-type", 0);
                    switch (barcodeType) {
                        case Barcode.QR_CODE:
                            processBarcodeQR(barcodeRes);
                            break;
                        case Barcode.PDF417:
                            processBarcodePDF417(barcodeRes);
                            break;
                    }
                }
                break;
        }
    }

    private void processBarcodeQR(String barcodeRes) {
        log("MSG FROM QR CODE: " + barcodeRes);
//        Intent intent = new Intent(getActivity(), BarcodeVisionActivity.class);
//        intent.putExtra(BarcodeVisionActivity.UseFlash, false);
//        startActivityForResult(intent, 0);
    }

    private void processBarcodePDF417(String barcodeRes) {
        //MetodosPublicos.alertDialog(getActivity(), barcodeRes);
        String processCode = processBarcode(barcodeRes);
        logW(processCode);
        if (!processCode.isEmpty()) {
            tvScanButtonError.setError(null);
            readBarcode.setError(null);
            infoUser = new Gson().fromJson(processCode, DecodeBarcode.InfoUser.class);
            selectPosCitySpinner(infoUser);
            sumbitRequest();
        } else {
            MetodosPublicos.alertDialog(getActivity(), "No se pudo procesar el codigo de barras, intente de nuevo.");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void sumbitRequest() {

        boolean cancel = false;
        View focusView = null;
        tvScanButtonError.setError(null);
        readBarcode.setError(null);
        if (infoUser == null) {
            readBarcode.setError(getString(R.string.error_field_required));
            readBarcode.requestFocus();

            tvScanButtonError.setVisibility(View.VISIBLE);
            tvScanButtonError.setError("Dale click para escanear el código de barras");
            focusView = tvScanButtonError;
            cancel = true;
        } else if (infoUser.name == null || TextUtils.isEmpty(infoUser.name)) {
            readBarcode.setError(getString(R.string.error_field_required));
            readBarcode.requestFocus();

            tvScanButtonError.setVisibility(View.VISIBLE);
            tvScanButtonError.setError("Dale click para escanear el código de barras");
            focusView = tvScanButtonError;
            cancel = true;
        }

        if (cancel) {


            focusView.requestFocus();
        } else {
            MyPreferences preferences = new MyPreferences(getActivity());
            String imei = preferences.getDeviceId();

            User user = ((RegisterActivity) getActivity()).getUser();
            user.Name = infoUser.name;
            user.LastName = infoUser.lastname;
            user.Sex = infoUser.sex;
            user.DocumentNumber = String.valueOf(infoUser.dni);
            user.CityCode = cityCode;
            user.IMEI = imei;
            user.BirthDate = String.valueOf(infoUser.birthDate);
            user.rh = infoUser.rh;
            user.DocumentRaw = infoUser.DocumentRaw;
            ((RegisterActivity) getActivity()).enableViewPager(true);
            ((RegisterActivity) getActivity()).nextPage(2);
        }
    }

}
