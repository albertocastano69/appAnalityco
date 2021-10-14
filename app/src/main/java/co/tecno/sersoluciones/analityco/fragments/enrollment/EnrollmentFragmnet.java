package co.tecno.sersoluciones.analityco.fragments.enrollment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import net.steamcrafted.materialiconlib.MaterialIconView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.tecno.sersoluciones.analityco.R;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;

import static co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.processBarcode;


public class EnrollmentFragmnet extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    @BindView(R.id.read_barcode)
    MaterialIconView readBarcode;
    @BindView(R.id.write_cc)
    Button writeDocument;
    @BindView(R.id.tvScanButtonError)
    TextView tvScanButtonError;
    @BindView(R.id.write)
    LinearLayout writeLayout;
    @BindView(R.id.document)
    EditText document;
    private Unbinder unbinder;
    /* @BindView(R.id.image_dni)
     ImageView imageDni;*/
    @BindView(R.id.info_button)
    Button infoButton;
    @BindView(R.id.layout_info)
    CardView layoutInfo;

    private DecodeBarcode.InfoUser infoUser;
    private OnScanCC mListener;

    public EnrollmentFragmnet() {
    }

    public static EnrollmentFragmnet scanCC() {
        return new EnrollmentFragmnet();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoUser = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_enrollment, container, false);
        unbinder = ButterKnife.bind(this, view);
        //msg.setVisibility(View.GONE);
        layoutInfo.setVisibility(View.GONE);
        if (getArguments() != null) {
            boolean isFromCompany = getArguments().getBoolean(ARG_PARAM1);
            //startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
        }
        return view;
    }

    interface OnScanCC {

        void onApplyScan(DecodeBarcode.InfoUser infoUser);

        void onApplyScanDoc(String doc);

        void onApplyScanDocQR();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnScanCC) {
            mListener = (OnScanCC) context;
        } else {
           /* throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");*/
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick(R.id.read_barcode)
    public void scanDNI() {
        startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
    }

    @OnClick(R.id.info_button)
    public void hideInfo() {
        layoutInfo.setVisibility(View.GONE);
        readBarcode.setEnabled(true);
        // imageDni.setEnabled(true);
    }

    @OnClick(R.id.write_cc)
    public void write() {
        // readBarcode.setVisibility(View.GONE);
        // imageDni.setVisibility(View.GONE);
        writeDocument.setVisibility(View.GONE);
        writeLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.sendDocument)
    public void sendDocument() {
        if (document.getText().toString().isEmpty()) {
            document.setError("Ingrese documento");
        } else {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(document.getWindowToken(), 0);
            if (mListener != null) {
                mListener.onApplyScanDoc(document.getText().toString());
            }
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
        if (mListener != null) {
            mListener.onApplyScanDocQR();
        }
    }

    private void processBarcodePDF417(String barcodeRes) {
        String processCode = processBarcode(barcodeRes);
        if (!processCode.isEmpty()) {
            tvScanButtonError.setError(null);
            //readBarcode.setError(null);
            infoUser = new Gson().fromJson(processCode, DecodeBarcode.InfoUser.class);
            if (mListener != null) {
                mListener.onApplyScan(infoUser);
            }
        } else {
            MetodosPublicos.alertDialog(getActivity(), "No se pudo procesar el codigo de barras, intente de nuevo.");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

