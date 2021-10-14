package co.tecno.sersoluciones.analityco.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Ser SOluciones SAS on 18/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/

public class Project implements Serializable {

    private final String Name;
    private String Id;
    public String CompanyName;
    public boolean IsActive;
    public boolean Expiry;
    private String ProjectNumber;
    private final String GeoFence;
    public String StartDate;
    public String FinishDate;
    public Date toDate;
    private String Review;
    private final String Address;
    private final String CityCode;
    public String Logo;
    public String CompanyId;
    public String CompanyInfoId;
    private String GeoFenceJson;
    public ArrayList<CompanyProject> JoinCompanies;
    public ArrayList<CompanyProject> JoinCompaniesList;
    public ArrayList<ProjectStages> ProjectStageArray;
    public ArrayList<ProjectStages> ProjectStageList;
    public int DaysToExpire;

    public Project(String name, String projectNumber, String geoFence, String startDate, String endDate,
                   String review, String address, String cityCode, ArrayList<CompanyProject> joinCompanies, String companyId) {
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
        CompanyInfoId = companyId;
    }

    public Project(String name, String geoFence, String startDate, String endDate,
                   String review, String address, String cityCode, String companyId) {
        Name = name;
        GeoFence = geoFence;
        StartDate = startDate;
        FinishDate = endDate;
        Review = review;
        Address = address;
        CityCode = cityCode;
        CompanyInfoId = companyId;
    }

    public Project(String name, String id, String geoFence, String startDate,
                   String finishDate, String address, String cityCode, String logo, String geoFenceJson) {
        Name = name;
        Id = id;
        GeoFence = geoFence;
        StartDate = startDate;
        FinishDate = finishDate;
        Address = address;
        CityCode = cityCode;
        Logo = logo;
        GeoFenceJson = geoFenceJson;
    }

    public Project(String name, String projectNumber, String geoFence, String startDate, String endDate,
                   String review, String address, String cityCode, ArrayList<CompanyProject> joinCompanies, String companyId, ArrayList<ProjectStages> stages) {
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
        CompanyId = companyId;
        ProjectStageList = stages;

    }

}
