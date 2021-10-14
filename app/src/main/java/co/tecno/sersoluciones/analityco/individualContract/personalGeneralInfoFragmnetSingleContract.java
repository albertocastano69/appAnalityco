package co.tecno.sersoluciones.analityco.individualContract;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import kotlin.ULong;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log;

public class personalGeneralInfoFragmnetSingleContract extends Fragment {
    private static final String ARG_PHOTO = "photo";
    private static final String ARG_INFOPERSON = "fillform";
    private static final String ARG_EMPLOYERINFO = "contactInfo";
    private static final String ARG_NAME = "name";
    private static final String ARG_LASTNAME = "lastname";

    private String fillForm, contactInfo,Name,LastName;
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

    @BindView(R.id.icon_edit_main_form)
    MaterialIconView editinfo;

    @BindView(R.id.text_identification)
    TextView textIdentification;
    @BindView(R.id.text_rh)
    TextView textRH;
    @BindView(R.id.text_birth)
    TextView textBirth;
    @BindView(R.id.text_gender)
    TextView textGender;
    @BindView(R.id.text_email)
    TextView textEmail;
    @BindView(R.id.namePerson)
    TextView textName;
    @BindView(R.id.lastname)
    TextView TextLastName;
    @BindView(R.id.nationality_flag)
    ImageView nationalityFlag;


    private String imageTransitionName;

    public personalGeneralInfoFragmnetSingleContract() {
    }
    public static personalGeneralInfoFragmnetSingleContract newInstance(String photo, String fillform, String contactInfo, String Name, String LastName){
        personalGeneralInfoFragmnetSingleContract fragment = new personalGeneralInfoFragmnetSingleContract();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO,photo);
        args.putString(ARG_INFOPERSON,fillform);
        args.putString(ARG_EMPLOYERINFO,contactInfo);
        args.putString(ARG_NAME,Name);
        args.putString(ARG_LASTNAME,LastName);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageTransitionName = getArguments().getString(ARG_PHOTO);
            fillForm = getArguments().getString(ARG_INFOPERSON);
            contactInfo = getArguments().getString(ARG_EMPLOYERINFO);
            Name = getArguments().getString(ARG_NAME);
            LastName = getArguments().getString(ARG_LASTNAME);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.persona_generalinfo_singlecontract_fragment, container, false);
        Unbinder unbinder = ButterKnife.bind(this, view);
        imageParalax.setTransitionName(imageTransitionName);
        Picasso.get()
                .load(imageTransitionName)
                .noFade()
                .resize(0, 150)
                .placeholder(R.drawable.image_not_available)
                .error(R.drawable.image_not_available)
                .into(imageParalax);
        textName.setText(Name);
        TextLastName.setText(LastName);
        ContactInfo(contactInfo);
        try {
            fillForm(fillForm);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            Locale currentLocale = Locale.getDefault();
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
            otherSymbols.setDecimalSeparator(',');
            otherSymbols.setGroupingSeparator('.');
            DecimalFormat formatter = new DecimalFormat("#,###,###", otherSymbols);
            String documentNumber = formatter.format(Long.parseLong(newPersonal.DocumentNumber));
            textIdentification.setText(documentNumber);

            textRH.setText(newPersonal.RH);
            if(newPersonal.CityOfBirthCode != null){
            StringBuilder Code = new StringBuilder(newPersonal.CityOfBirthCode);
            Code.deleteCharAt(0);
            SelectCityBirth(Code,newPersonal.Nationality);
            }
            textGender.setText(newPersonal.Sex);

            if (newPersonal.Nationality == 0)
                nationalityFlag.setImageResource(R.drawable.ic_flag_of_colombia);
            else nationalityFlag.setImageResource(R.drawable.ic_flag_of_venezuela);
        }
    }

    private void ContactInfo(String response) {
        try {
            JSONObject jsonInstance = new JSONObject(response);
            Personal contactInfo = new Gson().fromJson(response,
                    new TypeToken<Personal>() {
                    }.getType());
            if (contactInfo != null) {
                log("Name: " + jsonInstance.getString("Email") + " " + jsonInstance.getString("PhoneNumber"));
                textEmail.setText(contactInfo.Email);
                textPhone.setText(contactInfo.PhoneNumber);
                textContactPerson.setText(contactInfo.EmergencyContact);
                textContactPhone.setText(contactInfo.EmergencyPhone);
                String cityAdrress =SelectCityOfAdrress(contactInfo.CityCode);
                textHome.setText(String.format("%s %s",contactInfo.Address,cityAdrress));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String SelectCityOfAdrress(String cityCode) {
        String Cityadress = "";
        String selection = "(" + DBHelper.DANE_CITY_TABLE_COLUMN_CODE + " = ?)";
        Cursor CityAdress = requireActivity().getContentResolver().query(Constantes.CONTENT_DANE_CITY_URI,null,selection,new String[]{cityCode},null);
        if(CityAdress.moveToFirst()){
         Cityadress = CityAdress.getString(CityAdress.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME));
        }
        return Cityadress;
    }

    private void SelectCityBirth(StringBuilder code, int nationality) {
        String selection = ("(" + DBHelper.CITY_TABLE_COLUMN_CODE + " = ?) and ("
                + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " lIKE ?)");
        Cursor CityBirth = requireActivity().getContentResolver().query(Constantes.CONTENT_CITY_URI,null,selection,new String[]{String.valueOf(code), String.valueOf(nationality)},null);
        if(CityBirth.moveToFirst()){
            String City = CityBirth.getString(CityBirth.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME));
            String Region = CityBirth.getString(CityBirth.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_STATE));
            textBirth.setText(String.format("%s %s",City,Region));
        }
    }
}
