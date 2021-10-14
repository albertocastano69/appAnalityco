package co.tecno.sersoluciones.analityco.models;

import com.google.gson.Gson;

import java.util.List;


/**
 * Created by Ser SOluciones SAS on 18/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class Company {

    private boolean IsActive;
    public String StartDate;
    public String FinishDate;
    private final String DocumentType;
    private final String DocumentNumber;
    private final String Name;
    private String[] Domains;
    private String Website;
    public byte GeofenceLimit;
    private String[] EconomicActivities;
    private String BranchOfficeJson;
    public String AdminUserJson;
    public String CompanyLogoPath;
    public String CompanyInfoId;

    public Company(boolean isActive, String startDate, String finishDate, String documentType, String documentNumber,
                   String name, String[] domains, String website, byte geofenceLimit, String[] economicActivities,
                   List<BranchOffice> branchOfficeJson) {
        IsActive = isActive;
        StartDate = startDate;
        FinishDate = finishDate;
        DocumentType = documentType;
        DocumentNumber = documentNumber;
        Name = name;
        Domains = domains;
        Website = website;
        GeofenceLimit = geofenceLimit;
        EconomicActivities = economicActivities;
        BranchOfficeJson = new Gson().toJson(branchOfficeJson);
        //AdminUserJson = new Gson().toJson(adminUserJson);
    }

    public Company(String name, String documentType,
                   String documentNumber) {
        Name = name;
        DocumentType = documentType;
        DocumentNumber = documentNumber;
    }
}
