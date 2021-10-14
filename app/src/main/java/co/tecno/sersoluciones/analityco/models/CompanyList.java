package co.tecno.sersoluciones.analityco.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ser Soluciones SAS on 18/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class CompanyList implements Serializable {

    public String Id;
    public String CompanyInfoId;
    public boolean IsActive;
    public String Name;
    public String Phone;
    public String StartDate;
    public String FinishDate;
    public String DocumentType;
    public String DocumentNumber;
    public String Address;
    public String NameCity;
    public String StateName;
    public boolean Expiry;
    public String Logo;
    public boolean IsSelected;
    public String BranchOffices;
    public ArrayList<String> Permissions;
    public long GeofenceLimit;
    public String Rol;
    public String CompanyInfoParentId;
    public String[] Roles;

    public CompanyList() {
    }

    public CompanyList(String name, String documentType, String documentNumber) {
        Name = name;
        DocumentType = documentType;
        DocumentNumber = documentNumber;
    }

    @NonNull
    @Override
    public String toString() {
        return Name;
    }
}
