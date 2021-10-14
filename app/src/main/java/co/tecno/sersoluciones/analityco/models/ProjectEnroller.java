package co.tecno.sersoluciones.analityco.models;

import java.util.ArrayList;


@SuppressWarnings("unused")
public class ProjectEnroller {

    public String Name;
    public String Id;
    public String Projectnumber;
    public String Geofence;
    public String Createdate;
    public String StartDate;
    public String FinishDate;
    public String Review;
    public String Address;
    public String Citycode;
    public String Logo;
    public int Companyid;
    public boolean Ingeo;
    public double Distance_geo;
    public String Geofencejson;
    public String Companylogo;
    public String Companydocnum;
    public String Companyname;
    public ArrayList<CompanyProject> JoinCompanies;
    public ArrayList<CompanyProject> joincompanies;
    public ArrayList<ProjectStages> Projectstages;
    public ArrayList<ProjectStages> ProjectStageArray;

    public ProjectEnroller() {
    }
}
