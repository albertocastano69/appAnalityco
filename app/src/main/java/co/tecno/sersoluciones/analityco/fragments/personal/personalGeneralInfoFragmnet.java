package co.tecno.sersoluciones.analityco.fragments.personal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.DocumentType;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.EditInfoPersonalActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log;

/**
 * Created by Ser Soluciones SAS on 01/03/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class personalGeneralInfoFragmnet extends Fragment {

    private static final String ARG_DATA = "data";
    private static final String ARG_PERSONALID = "personalId";
    private static final String ARG_CLAIMS = "claims";
    private MyPreferences preferences;
    private int personalId;
    private String fillForm;
    private ClaimsBasicUser claims;

    /* @BindView(R.id.label_nit)
    TextView labelNit;
    @BindView(R.id.text_nit)
    TextView textNit;*/

    @BindView(R.id.icon_phone)
    MaterialIconView iconPhone;
    @BindView(R.id.text_phone)
    TextView textPhone;

    @BindView(R.id.header_img)
    ImageView imageParalax;
    @BindView(R.id.text_home)
    TextView textHome;
    @BindView(R.id.text_contact_person)
    TextView textContactPerson;
    @BindView(R.id.text_contact_phone)
    TextView textContactPhone;
    @BindView(R.id.text_job)
    TextView textJob;
    @BindView(R.id.icon_edit_main_form)
    MaterialIconView editinfo;
    @BindView(R.id.text_eps)
    TextView textEps;
    @BindView(R.id.text_afp)
    TextView textAfp;

    @BindView(R.id.text_identification)
    TextView textIdentification;
    @BindView(R.id.text_rh)
    TextView textRH;
    @BindView(R.id.text_birth)
    TextView textBirth;
    @BindView(R.id.text_gender)
    TextView textGender;
    @BindView(R.id.nationality_flag)
    ImageView nationalityFlag;
    @BindView(R.id.TypeDocumentArea)
    TextView typeDocumentArea;
  /* @BindView(R.id.text_contact_parentesco)
    TextView textParentesco;*/

    private String imageTransitionName;

    public personalGeneralInfoFragmnet() {
    }

    public static personalGeneralInfoFragmnet newInstance(String data, int personalId, String claims, String transitionName) {

        personalGeneralInfoFragmnet fragment = new personalGeneralInfoFragmnet();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, data);
        args.putString(Constantes.ITEM_TRANSITION_NAME, transitionName);
        args.putInt(ARG_PERSONALID, personalId);
        args.putString(ARG_CLAIMS, claims);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new MyPreferences(getActivity());
        if (getArguments() != null) {
            personalId = getArguments().getInt(ARG_PERSONALID);
            fillForm = getArguments().getString(ARG_DATA);
            imageTransitionName = getArguments().getString(Constantes.ITEM_TRANSITION_NAME);
            claims = new Gson().fromJson(getArguments().getString(ARG_CLAIMS),
                    new TypeToken<ClaimsBasicUser>() {
                    }.getType());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personal_general_info_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        Bitmap bitmap;

        imageParalax.setTransitionName(imageTransitionName);
        if (!fillForm.isEmpty() && !fillForm.equals("UNAUTHORIZED")) {
            try {
                fillForm(fillForm);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent project = new Intent(getActivity(), EditInfoPersonalActivity.class);
                project.putExtra("process", 1);
                project.putExtra("personal", personalId);
                project.putExtra("json", fillForm);
                startActivityForResult(project, Constantes.UPDATE);
            }
        });
        permission();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void fillForm(String jsonObject) throws JSONException {

        JSONObject jsonInstance = new JSONObject(jsonObject);
        Personal newPersonal = new Gson().fromJson(jsonObject,
                new TypeToken<Personal>() {
                }.getType());
        if (newPersonal != null) {
            log("Name: " + jsonInstance.getString("Name") + " " + jsonInstance.getString("LastName"));
            log("New Person " + newPersonal);
            if (newPersonal.Photo != null) {
                String url = Constantes.URL_IMAGES + newPersonal.Photo;
                //
                Picasso.get()
                        .load(url)
                        .noFade()
                        .resize(0, 150)
                        .placeholder(R.drawable.image_not_available)
                        .error(R.drawable.image_not_available)
                        .into(imageParalax);
            }

            textJob.setText(newPersonal.JobName);
            String phone = newPersonal.PhoneNumber;
            textPhone.setPaintFlags(textPhone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            textPhone.setText(phone);
            textPhone.setTextColor(Color.BLUE);

            textEps.setText(newPersonal.NameEPS);
            textAfp.setText(newPersonal.NameAFP);

            if (newPersonal.Address == null) newPersonal.Address = "";
            if (newPersonal.CityName == null) newPersonal.CityName = "";
            textHome.setText(newPersonal.Address + " " + newPersonal.CityName);

            String contactPhone = newPersonal.EmergencyContactPhone;
            textContactPhone.setPaintFlags(textContactPhone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            textContactPhone.setText(contactPhone);
            textContactPhone.setTextColor(Color.BLUE);

            textContactPerson.setText(newPersonal.EmergencyContact/*+ "("+newPersonal.RelationshipWithContact+")"*/);

            Locale currentLocale = Locale.getDefault();
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
            otherSymbols.setDecimalSeparator(',');
            otherSymbols.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("#,###,###", otherSymbols);
            //DecimalFormat formatter = new DecimalFormat("#,###,###");
            String documentNumber = formatter.format(Long.parseLong(newPersonal.DocumentNumber));

            textIdentification.setText(documentNumber);
            textRH.setText(newPersonal.RH);
            textBirth.setText(newPersonal.CityOfBirthName + " " + newPersonal.StateOfBirthName);
            textGender.setText(newPersonal.Sex);
           //textParentesco.setText("("+newPersonal.RelationshipWithContact+")");

            if (newPersonal.Nationality == 0)
                nationalityFlag.setImageResource(R.drawable.ic_flag_of_colombia);
            else nationalityFlag.setImageResource(R.drawable.ic_flag_of_venezuela);

            typeDocumentArea.setText(newPersonal.DocumentType);


        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("ok", "llego de actividad con codigo" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.e("ok", "llego ");
        }
    }

    @OnClick(R.id.text_phone)
    public void clickPhone() {
        PopupMenu popup = new PopupMenu(getActivity(), textPhone);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Marcar");
        popup.setOnMenuItemClickListener(item -> {
            Intent i = new Intent(Intent.ACTION_DIAL,
                    Uri.parse("tel:" + textPhone.getText().toString()));
            startActivity(i);
            return true;
        });
        popup.show();
    }

    @OnClick(R.id.text_contact_phone)
    public void clickContactPhone() {
        PopupMenu popup = new PopupMenu(getActivity(), textContactPhone);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Marcar");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + textContactPhone.getText().toString()));
                startActivity(i);
                return true;
            }
        });
        popup.show();
    }

    @OnClick(R.id.icon_home)
    public void openGoogleMaps() {
        String map = "http://maps.google.co.in/maps?q=" + textHome.getText().toString();
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse(map));
        startActivity(i);
    }

    private void permission() {
        preferences = new MyPreferences(getActivity());
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        //permisos usuario administrativo
        if (user.IsAdmin || user.IsSuperUser) {
            if (user.claims.contains("personals.update") || user.IsSuperUser)
                editinfo.setVisibility(View.VISIBLE);
        }//permisos usuarios normales
        else if (claims != null) {
            if (claims.Claims.contains("personals.update")) editinfo.setVisibility(View.VISIBLE);
        }
    }

}

