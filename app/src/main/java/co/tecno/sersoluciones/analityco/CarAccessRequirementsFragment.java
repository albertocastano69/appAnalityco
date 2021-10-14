package co.tecno.sersoluciones.analityco;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.models.ContractReq;

public class CarAccessRequirementsFragment extends Fragment {
    private static final String ARG_CONTRACTS = "contracts";
    private View view;

    public CarAccessRequirementsFragment() {
    }

    public static CarAccessRequirementsFragment newInstance(String contracts){
        CarAccessRequirementsFragment fragment = new CarAccessRequirementsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONTRACTS, contracts);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.requirement_personal_list, container, false);
        return view;
    }
}
