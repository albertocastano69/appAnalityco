package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ser SOluciones SAS on 18/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class BasicProject implements Serializable {

    public String Name;
    public String Id;
    public String CompanyName;
    public boolean IsActive;
    public boolean Expiry;
    public String ProjectNumber;
    public String GeoFence;
    public String StartDate;
    public String FinishDate;
    public Date toDate;
    public String Review;
    public String Address;
    public String CityCode;
    public String Logo;
    public String CompanyId;
    public String CompanyInfoId;
    public String GeoFenceJson;
    public ArrayList<ProjectStages> ProjectStageArray;
}