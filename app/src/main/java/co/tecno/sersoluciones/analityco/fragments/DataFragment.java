package co.tecno.sersoluciones.analityco.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.ProjectStages;
import co.tecno.sersoluciones.analityco.models.RequirementDetails;
import co.tecno.sersoluciones.analityco.models.UserProject;

public class DataFragment extends Fragment {

    // data object we want to retain
    private String data;
    private ArrayList<CompanyProject> companyProjectList;
    private CompanyProject companyProjectAdmin;
    private ArrayList<PersonalList> personalList;
    private ArrayList<ObjectList> objectList;
    private ClaimsBasicUser claims;
    private ArrayList<ProjectStages> projectStages;
    private ArrayList<UserProject> userProjectList;
    private ArrayList<ProjectList> projectList;
    private ArrayList<RequirementDetails> requirementDetails;
    private ArrayList<CompanyProject> linkedCompanyProjectList;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        companyProjectList = new ArrayList<>();
        personalList = new ArrayList<>();
        objectList = new ArrayList<>();
        projectStages = new ArrayList<>();
        userProjectList = new ArrayList<>();
        projectList = new ArrayList<>();
        requirementDetails = new ArrayList<>();
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public ArrayList<CompanyProject> getCompanyProjectList() {
        return companyProjectList;
    }

    public void setCompanyProjectList(String companyProjectStr) {
        if (!companyProjectStr.equals("UNAUTHORIZED"))
            this.companyProjectList = new Gson().fromJson(companyProjectStr,
                    new TypeToken<ArrayList<CompanyProject>>() {
                    }.getType());
    }

    public void setCompanyProjectAdmin(String companyProjectStr) {
        if (!companyProjectStr.equals("UNAUTHORIZED"))
            this.companyProjectAdmin = new Gson().fromJson(companyProjectStr,
                    new TypeToken<CompanyProject>() {
                    }.getType());
    }

    public ArrayList<PersonalList> getPersonalList() {
        return personalList;
    }

    public void setPersonalList(String personalListStr) {
        if (!personalListStr.equals("UNAUTHORIZED"))
            this.personalList = new Gson().fromJson(personalListStr,
                    new TypeToken<ArrayList<PersonalList>>() {
                    }.getType());
    }

    public ArrayList<ObjectList> getObjectList() {
        return objectList;
    }

    public void setObjectList(String objectListStr) {
        if (!objectListStr.equals("UNAUTHORIZED"))
            this.objectList = new Gson().fromJson(objectListStr,
                    new TypeToken<ArrayList<ObjectList>>() {
                    }.getType());
    }

    public ClaimsBasicUser getClaims() {
        return claims;
    }

    public void setClaims(String claims) {
        this.claims = new Gson().fromJson(claims,
                new TypeToken<ClaimsBasicUser>() {
                }.getType());
    }

    public ArrayList<ProjectStages> getProjectStages() {
        return projectStages;
    }

    public void setProjectStages(String projectStagesStr) {
        if (!projectStagesStr.equals("UNAUTHORIZED"))
            this.projectStages = new Gson().fromJson(projectStagesStr,
                    new TypeToken<ArrayList<ProjectStages>>() {
                    }.getType());
    }

    public ArrayList<UserProject> getUserProjectList() {
        return userProjectList;
    }

    public void setUserProjectList(String userProjectStr) {
        if (!userProjectStr.equals("UNAUTHORIZED"))
            this.userProjectList = new Gson().fromJson(userProjectStr,
                    new TypeToken<ArrayList<UserProject>>() {
                    }.getType());
    }

    public ArrayList<ProjectList> getProjectList() {
        return projectList;
    }

    public void setProjectList(String projectListStr) {
        if (!projectListStr.equals("UNAUTHORIZED"))
            this.projectList = new Gson().fromJson(projectListStr,
                    new TypeToken<ArrayList<ProjectList>>() {
                    }.getType());
    }

    public ArrayList<RequirementDetails> getRequirementDetails() {
        return requirementDetails;
    }

    public void setRequirementDetails(String requirementDetailsStr) {
        if (!requirementDetailsStr.equals("UNAUTHORIZED"))
            this.requirementDetails = new Gson().fromJson(requirementDetailsStr,
                    new TypeToken<ArrayList<RequirementDetails>>() {
                    }.getType());
    }

    public ArrayList<CompanyProject> getLinkedCompanyDetails() {
        return linkedCompanyProjectList;
    }

    public void setLinkedCompany(String linkedCompanydetails) {
        if (!linkedCompanydetails.equals("UNAUTHORIZED"))
            this.linkedCompanyProjectList = new Gson().fromJson(linkedCompanydetails,
                    new TypeToken<ArrayList<CompanyProject>>() {
                    }.getType());
    }
}