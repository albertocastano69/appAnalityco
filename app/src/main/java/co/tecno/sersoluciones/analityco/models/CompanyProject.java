package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;

/**
 * Created by Ser Soluciones SAS on 28/09/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class CompanyProject implements Serializable {

    public String Id;
    public String StartDate;
    public String FinishDate;
    public String DocumentType;
    public String DocumentNumber;
    public String Name;
    public String Rol;
    public String CompanyId;
    public boolean IsActive;
    public String Photo;
    public String Logo;
    public String CompanyInfoId;
    public String CompanyInfoParentId;

    public CompanyProject() {
    }

    public CompanyProject(String startDate, String finishDate, String documentType, String documentNumber,
                          String name, String role) {

        StartDate = startDate;
        FinishDate = finishDate;
        DocumentType = documentType;
        DocumentNumber = documentNumber;
        Name = name;
        Rol = role;
        IsActive = true;
        //AdminUserJson = new Gson().toJson(adminUserJson);
    }

    public CompanyProject(String startDate, String finishDate, String documentType, String documentNumber,
                          String name, String role, String logo) {

        StartDate = startDate;
        FinishDate = finishDate;
        DocumentType = documentType;
        DocumentNumber = documentNumber;
        Name = name;
        Rol = role;
        IsActive = true;
        Logo = logo;
        //AdminUserJson = new Gson().toJson(adminUserJson);
    }

    public String getId() {
        return CompanyInfoId;
    }

    public void setId(String id) {
        this.CompanyInfoId = id;
    }
}

