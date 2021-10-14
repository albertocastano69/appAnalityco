package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;

/**
 * Created by Ser SOluciones SAS on 10/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class BranchOffice implements Serializable {

    public String CityCode;
    public String Name;
    public String Address;
    public String City;
    public String State;
    public String Phone;
    public String Email;
    public Boolean IsMain;

    public BranchOffice() {
    }

    public BranchOffice(int cityCode, String name, String address, String city, String state, String phone, String email, Boolean isMain) {
        CityCode = String.valueOf(cityCode);
        Name = name;
        Address = address;
        City = city;
        State = state;
        Phone = phone;
        Email = email;
        IsMain = isMain;
    }
}
