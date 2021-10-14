package co.tecno.sersoluciones.analityco;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ProjectsCarListFragment extends Fragment {
    private static final String ARG_DATA = "data";
    private View view;

    public ProjectsCarListFragment() {
    }
    public static ProjectsCarListFragment newInstance(String data){
        ProjectsCarListFragment fragment = new ProjectsCarListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.user_list_project_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        return view;
    }

}
