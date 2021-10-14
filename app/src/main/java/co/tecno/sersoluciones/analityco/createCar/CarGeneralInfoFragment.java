package co.tecno.sersoluciones.analityco.createCar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.fragments.personal.personalGeneralInfoFragmnet;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

public class CarGeneralInfoFragment extends Fragment {

    private static final String ARG_DATA = "data";
    private MyPreferences preferences;
    private String fillForm;

    public static personalGeneralInfoFragmnet newInstance(String data) {

        personalGeneralInfoFragmnet fragment = new personalGeneralInfoFragmnet();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new MyPreferences(getActivity());
        if (getArguments() != null) {
            fillForm = getArguments().getString(ARG_DATA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.car_general_info_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);

        return view;
    }
}
