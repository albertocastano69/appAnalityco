package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;

/**
 * Created by Ser Soluciones SAS on 02/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class Employer implements Serializable {

    public String Id;
    public boolean IsActive;
    public String DocumentType;
    public String DocumentNumber;
    public String Name;
    public String Address;
    public String CityCode;
    public String Website;
    public String Phone;
    public String Email;
    public String Logo;
    public String Rol;
    public String CityName;
    public String StateName;
    public String CreateDate;
    public String StartDate;
    public String FinishDate;
    public boolean Expiry;
    public boolean checkList;
    public int DaysToExpire;
    public String CompanyInfoParentId;
    public String CompanyId;
    public int IsManaged;
}
