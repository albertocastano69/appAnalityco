package co.tecno.sersoluciones.analityco.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.RegisterActivity;
import co.tecno.sersoluciones.analityco.adapters.UserProfileAdapter;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.views.RevealBackgroundView;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

public class ProfileFragment extends Fragment implements RevealBackgroundView.OnStateChangeListener {

    @BindView(R.id.vRevealBackground)
    RevealBackgroundView vRevealBackground;
    @BindView(R.id.rvUserProfile)
    RecyclerView rvUserProfile;
    @BindView(R.id.close_alert)
    ImageButton closeAlert;
    @BindView(R.id.layout_alert)
    CardView layoutAlert;
    @BindView(R.id.terminos_y_condiciones)
    TextView alertText;

    private Unbinder unbinder;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        rootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                setupRevealBackground();
            }
        });
        setupUserProfileGrid();
        configPolicyAlert();
        //setupRevealBackground();
        return rootView;
    }

    private void setupRevealBackground() {

        final int[] startingLocation = new int[2];
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int widthPixels = metrics.widthPixels;
        startingLocation[0] += widthPixels / 2;
        vRevealBackground.setOnStateChangeListener(this);
        vRevealBackground.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
                vRevealBackground.startFromLocation(startingLocation);
                return true;
            }
        });
    }

    private void setupUserProfileGrid() {
        final StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvUserProfile.setLayoutManager(layoutManager);
    }

    @Override
    public void onStateChange(int state) {
        log("onStateChange " + state);

        User user = ((RegisterActivity) getActivity()).getUser();
        logW(new Gson().toJson(user));
        if (RevealBackgroundView.STATE_FINISHED == state) {
            rvUserProfile.setVisibility(View.VISIBLE);
            UserProfileAdapter userPhotosAdapter = new UserProfileAdapter(getActivity(), user);
            rvUserProfile.setAdapter(userPhotosAdapter);
            vRevealBackground.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @SuppressLint("SetTextI18n")
    private void configPolicyAlert() {

        MyPreferences preferences = new MyPreferences(requireContext());

        closeAlert.setOnClickListener(v -> {
            layoutAlert.setVisibility(View.GONE);
        });
        String termsAndConditions = getResources().getString(R.string.terminos_y_condiciones);
        String privacyPolicy = getResources().getString(R.string.politicas_privacidad);
        String fullText = (
                getResources().getString(R.string.alerta_privacidad) +
                        termsAndConditions + "y " +
                        privacyPolicy +
                        getResources().getString(R.string.analytico_text));
        Spannable spannable = new SpannableString(fullText);

        try {
            JSONArray jsonArray = new JSONArray(preferences.getPolicyUrls());
            String urlTC = "http://www.google.com";
            String urlPP = "http://www.google.com";
            String url;

            for (int item = 0; item < (jsonArray.length()); item = item + 1) {
                JSONObject jsonObject = jsonArray.getJSONObject(item);
                url = jsonObject.getString("Description");

                if (jsonObject.getString("Value").equals("PP")) {
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        urlPP = "http://" + url;
                    else urlPP = jsonObject.getString("Description");

                } if (jsonObject.getString("Value").equals("TC")) {
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        urlTC = "http://" + url;
                    else urlTC = jsonObject.getString("Description");
                }
            }

            String finalUrlTC = urlTC;
            String finalUrlPP = urlPP;
            spannable.setSpan(
                    new ClickableSpan() {

                        @Override
                        public void onClick(@NonNull View widget) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrlTC));
                            startActivity(browserIntent);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            ds.setColor(getResources().getColor(R.color.primary));
                        }
                    },
                    fullText.indexOf(termsAndConditions),
                    (fullText.indexOf(termsAndConditions) + termsAndConditions.length()),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannable.setSpan(
                    new ClickableSpan() {

                        @Override
                        public void onClick(@NonNull View widget) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrlPP));
                            startActivity(browserIntent);
                        }

                        @Override
                        public void updateDrawState(@NonNull TextPaint ds) {
                            super.updateDrawState(ds);
                            ds.setUnderlineText(false);
                            ds.setColor(getResources().getColor(R.color.primary));
                        }
                    },
                    fullText.indexOf(privacyPolicy),
                    (fullText.indexOf(privacyPolicy) + privacyPolicy.length()),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        alertText.setMovementMethod(LinkMovementMethod.getInstance());
        alertText.setText(spannable, TextView.BufferType.SPANNABLE);
    }
}
