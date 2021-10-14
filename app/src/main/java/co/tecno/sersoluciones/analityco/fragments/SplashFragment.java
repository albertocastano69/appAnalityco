package co.tecno.sersoluciones.analityco.fragments;


import android.animation.Animator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.SplashActivity;
import co.tecno.sersoluciones.analityco.views.AnimatedSvgView;

/**
 * Created by gustavo morales on 4/07/2016.
 * tavomorales88@gmail.com
 **/
public class SplashFragment extends Fragment implements AnimatedSvgView.OnStateChangeListener {

    private static final String D = "SplashFragment";
    private View rootView;
    private AnimatedSvgView svgView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_splash, container, false);
        svgView = (AnimatedSvgView) rootView.findViewById(R.id.animated_svg_view);
        svgView.setOnStateChangeListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, rootView);
        if (getActivity() != null && !getActivity().isFinishing())
            reset();
    }


    @Override
    public void onStateChange(int state) {
        Log.d(D, "state: " + state);
        if (getActivity() != null && !getActivity().isFinishing())
            ((SplashActivity) Objects.requireNonNull(getActivity())).onStateChange(state);
    }

    private void reset() {
        new Handler().postDelayed(() -> {
            try {
                svgView.start();
                int cx = (rootView.getLeft() + rootView.getRight()) / 2;
                int cy = rootView.getBottom();
                int finalRadius = Math.max(rootView.getWidth(), rootView.getHeight());

                Animator anim = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    anim = ViewAnimationUtils.createCircularReveal(rootView, cx, cy, 0, finalRadius);
                    anim.setDuration(500);
                    anim.start();
                }
                int color = Color.WHITE;
                rootView.setBackgroundColor(color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 250);
    }


}
