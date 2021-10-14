package co.tecno.sersoluciones.analityco.fragments.personal.wizard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;

import net.steamcrafted.materialiconlib.MaterialIconView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.utilities.DecodeBarcode;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.tecno.sersoluciones.analityco.utilities.DecodeBarcode.processBarcode;


public class ScanPersonalFragmnet extends Fragment {

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
    private boolean enableQR;

    private OnScanListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableQR = false;
        if (getArguments() != null) {
            enableQR = getArguments().getBoolean(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_person, container, false);
        unbinder = ButterKnife.bind(this, view);
        //msg.setVisibility(View.GONE);

        writeDocument.setVisibility(View.GONE);
        writeLayout.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnScanListener) {
            mListener = (OnScanListener) context;
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

    @OnClick(R.id.read_barcode)
    public void scanDNI() {
        startActivityForResult(new Intent(getActivity(), BarcodeDecodeSerActivity.class), 0);
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
                            //processBarcodeQR(barcodeRes);
                            if (enableQR) {
                                if (mListener != null) {
                                    mListener.onApplyScanDocQR();
                                }
                            } else
                                Toast.makeText(getActivity(), "Codigo PDF417 no detectado, por favor escanee el codigo de " +
                                        "barras que se encuentra al respaldo de la cedula", Toast.LENGTH_LONG).show();
                            break;
                        case Barcode.PDF417:
                            log("CODE PDF417 DETECTED");
                            processBarcodePDF417(barcodeRes);
                            break;
                    }

                }
                break;
        }
    }

    private void processBarcodePDF417(String barcodeRes) {
        String processCode = processBarcode(barcodeRes);
        logW("processCode: " + processCode);
        if (!processCode.isEmpty()) {
            tvScanButtonError.setError(null);
            //readBarcode.setError(null);
            DecodeBarcode.InfoUser infoUser = new Gson().fromJson(processCode, DecodeBarcode.InfoUser.class);
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

    public interface OnScanListener {
        void onApplyScan(DecodeBarcode.InfoUser infoUser);

        void onApplyScanDoc(String doc);

        void onApplyScanDocQR();
    }

}

