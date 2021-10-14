package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;

/**
 * Created by Ser Soluciones SAS on 22/01/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class Requirements implements Serializable{

    public int Id;
    public String Name;
    public int ItemId;
    public boolean IsValided;
    public boolean ByAge;
    public boolean ByCertification;
    public boolean ByDate;
    public boolean ByValidity;
    public boolean BySecuritySocial;
    public boolean ByBiometric;
    public String Date;
    public int Days;
    public boolean RequiredFile;
    public boolean RequiredDate;
    public String AgeMin;
    public String AgeMax;
    public int ItemRequirementId;
    public int HeightMin;
    public int HeightMax;
    public String Description;
    public String Desc;
    public String Type;
    public String File;
    public String Attr;
    public String ContractId;
    public String ContractNumber;
    public int PersonalInfoId;
    public boolean IsEntry;
    public String WeekDays;
    public String MaxHour;
    public String MinHour;

    public int OrderNum;
    public String Abrv;
    public boolean ByEntry;
    public String ContractReview;
    public String DocsJSON;
    public boolean EnableInputEntry;

    public Requirements() {
    }
}
