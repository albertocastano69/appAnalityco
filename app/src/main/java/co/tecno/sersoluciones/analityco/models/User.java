package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ser Soluciones SAS on 24/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class User implements Serializable{

    public String Email;
    public String Name;
    public String LastName;
    public String Password;
    public String ConfirmPassword;
    public String PhoneNumber;
    public String Sex;
    public String DocumentType;
    public String DocumentNumber;
    public String IMEI;
    public String BirthDate;
    public String Expiry;
    public String RoleName;
    public String rh;
    public String Id;
    public String imagePath;
    public int imageRotate;
    public String CityCode;
    public String ResponseReCaptcha;
    public String CompanyName;
    public boolean email_verified;
    public String avatar;
    public String FirebaseToken;
    public String CompanyInfoId;
    public String CompanyId;
    public String PersonalId;
    public String Photo;
    public String DocumentRaw;
    public ArrayList<String> roles;
    public ArrayList<String> claims;
    public ArrayList<CompanyList> Companies;
    public boolean IsAdmin;
    public boolean IsSuperUser;

    public User(){}

    public User(String email, String name, String lastName, String password, String confirmPassword,
                String phoneNumber, String sex, String documentType, String documentNumber, String IMEI,
                String cityCode, String rh, String ResponseReCaptcha) {
        Email = email;
        Name = name;
        LastName = lastName;
        Password = password;
        ConfirmPassword = confirmPassword;
        PhoneNumber = phoneNumber;
        Sex = sex;
        DocumentType = documentType;
        DocumentNumber = documentNumber;
        CityCode = cityCode;
        this.IMEI = IMEI;
        this.rh = rh;
        this.ResponseReCaptcha = ResponseReCaptcha;
    }
}
