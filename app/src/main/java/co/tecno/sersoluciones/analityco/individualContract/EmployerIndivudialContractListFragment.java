package co.tecno.sersoluciones.analityco.individualContract;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

public class EmployerIndivudialContractListFragment extends Fragment {

    private static final String ARG_EMPLOYER_ID = "employer";
    private static final String ARG_EMPLOYER_LOGO = "employerlog";
    private static final String ARG_EMPLOYER_ROL = "employerrol";
    private static final String ARG_EMPLOYER_NAME = "employername";
    private static final String ARG_EMPLOYER_DOCUMENT_TYPE = "employerdocType";
    private static final String ARG_EMPLOYER_DOCUMENT_NUMBER = "employerdocNumber";
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.text_name)
    TextView NameEmployer;
    @BindView(R.id.text_sub_name)
    TextView RolEmployer;
    @BindView(R.id.text_validity)
    TextView NitEmployer;
    @BindView(R.id.logo)
    ImageView logoEmployer;
    String EmployerId,Logo,Name,Rol,DocType,DocNumber;

    private View view;

    public EmployerIndivudialContractListFragment() {
    }

    public static EmployerIndivudialContractListFragment newInstance(String employerId, String Logo, String Name,String Rol, String DocumentType,String DocumenNumber) {

        EmployerIndivudialContractListFragment fragment = new EmployerIndivudialContractListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMPLOYER_ID, employerId);
        args.putString(ARG_EMPLOYER_LOGO, Logo);
        args.putString(ARG_EMPLOYER_NAME, Name);
        args.putString(ARG_EMPLOYER_ROL, Rol);
        args.putString(ARG_EMPLOYER_DOCUMENT_TYPE, DocumentType);
        args.putString(ARG_EMPLOYER_DOCUMENT_NUMBER, DocumenNumber);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_employer_individual_contract, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        if (getArguments() != null) {
            (view.findViewById(R.id.card_sub_main)).setVisibility(View.VISIBLE);
            EmployerId = getArguments().getString(ARG_EMPLOYER_ID);
            Logo = getArguments().getString(ARG_EMPLOYER_LOGO);
            Name = getArguments().getString(ARG_EMPLOYER_NAME);
            Rol = getArguments().getString(ARG_EMPLOYER_ROL);
            DocType = getArguments().getString(ARG_EMPLOYER_DOCUMENT_TYPE);
            DocNumber = getArguments().getString(ARG_EMPLOYER_DOCUMENT_NUMBER);
            title.setText("Empleador");
            String url = Constantes.URL_IMAGES+Logo;
            NameEmployer.setText(Name);
            RolEmployer.setText(Rol);
            NitEmployer.setText(String.format("%s.: %s", DocType, DocNumber));
            Picasso.get().load(url)
                    .resize(0, 250)
                    .placeholder(R.drawable.image_not_available)
                    .error(R.drawable.image_not_available)
                    .into(logoEmployer);
            }
        return view;
        }
}
