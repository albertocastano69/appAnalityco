package co.tecno.sersoluciones.analityco.models;

/**
 * Created by Ser Soluciones SAS on 05/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EmployerInstance {

    public String Id;
    public final String DocumentType;
    public final String DocumentNumber;
    public final String Name;
    public final String Address;
    public final String CityCode;
    public final String Website;
    public final String Phone;
    public final String Email;
    public final String Rol;
    public String Logo;
    public String CityName;
    public String StateName;
    public String CompanyInfoParentId;
    public int ArlId;
    public String NameARL;

    public EmployerInstance(String documentType, String documentNumber, String name, String address,
                            String cityCode, String website, String phone, String email, String rol) {
        DocumentType = documentType;
        DocumentNumber = documentNumber;
        Name = name;
        Address = address;
        CityCode = cityCode;
        Website = website;
        Phone = phone;
        Email = email;
        Rol = rol;
    }
}
