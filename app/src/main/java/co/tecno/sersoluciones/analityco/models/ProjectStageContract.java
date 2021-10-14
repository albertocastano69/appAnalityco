package co.tecno.sersoluciones.analityco.models;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ser SOluciones SAS on 18/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class ProjectStageContract implements Serializable {

    public final String Name;
    public String Id;
    public String ProjectId;
    private final String ProjectNumber;
    private final String GeoFence;
    private final String StartDate;
    private final String FinishDate;
    private final String Review;
    private final String Address;
    private final String CityCode;
    public String Logo;
    private final int CompanyId;
    public String GeoFenceJson;
    private final ArrayList<CompanyProject> JoinCompanies;
    private final ArrayList<CompanyProject> JoinCompaniesList;
    private String ProjectStageArray;
    public ArrayList<ProjectStages> ProjectStageList;
    public ArrayList<ProjectStages> Stages;
    public boolean AllStages;

    public ProjectStageContract(String name, String projectNumber, String geoFence, String startDate, String endDate,
                   String review, String address, String cityCode, ArrayList<CompanyProject> joinCompanies,int companyId) {
        Name = name;
        ProjectNumber = projectNumber;
        GeoFence = geoFence;
        StartDate = startDate;
        FinishDate = endDate;
        Review = review;
        Address = address;
        CityCode = cityCode;
        JoinCompanies = joinCompanies;
        JoinCompaniesList = joinCompanies;
        CompanyId=companyId;
    }

    public void StringToArray(){
        if(ProjectStageArray!=null){
            this.ProjectStageList = new Gson().fromJson(ProjectStageArray,
                    new TypeToken<ArrayList<ProjectStages>>() {
                    }.getType());
        }
    }

}
