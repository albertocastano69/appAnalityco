package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import co.tecno.sersoluciones.analityco.fragments.ValidityFragment;
import co.tecno.sersoluciones.analityco.fragments.project.AddCompanyProjectFragment;
import co.tecno.sersoluciones.analityco.fragments.project.AddUserProject;
import co.tecno.sersoluciones.analityco.fragments.project.EditBasicFomFragmnetP;
import co.tecno.sersoluciones.analityco.fragments.project.NewStageProject;
import co.tecno.sersoluciones.analityco.models.CompanyProject;
import co.tecno.sersoluciones.analityco.models.ProjectStages;
import co.tecno.sersoluciones.analityco.models.UserProject;
import co.tecno.sersoluciones.analityco.models.Validity;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

public class EditInfoProjectActivity extends BaseActivity implements EditBasicFomFragmnetP.OnMainFragmentInteractionListener,
        ValidityFragment.OnValidityFragmentInteractionListener, NewStageProject.OnAddStageProject, AddCompanyProjectFragment.OnAddCompanyProject,
        AddUserProject.OnAddUserProject {

    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            int process = (Integer) bd.get("process");
            projectId = (String) bd.get("project");
            if (process == 1) {

                String jsonStr = restoreJsonFromFile();
                addMainFragment();
                try {
                    JSONObject json = new JSONObject(jsonStr);
                    getSupportActionBar().setTitle(json.getString("Name"));
                    getSupportActionBar().setSubtitle(json.getString("ProjectNumber"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (process == 2) {
                Validity mValidity = new Validity();
                mValidity.IsActive = bd.getBoolean("isActive");
                String StartDate = bd.getString("startDate");
                String FinishDate = bd.getString("finishDate");
                try {
                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date startDate = format.parse(StartDate);
                    Date finishDate = format.parse(FinishDate);
                    mValidity.StartDate = startDate;
                    mValidity.FinishDate = finishDate;
                    addValidityFragment(mValidity);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (process == 3) {
                getSupportActionBar().setTitle(" Nueva etapa de Proyecto");
                AddFragmentJoinStages();
            } else if (process == 4) {
                String jsonStr = restoreJsonFromFile();
                getSupportActionBar().setTitle(" Editar etapa");
                EditStagesProject(jsonStr);
            } else if (process == 5) {
                getSupportActionBar().setTitle(" Nueva compañia");
                AddFragmentJoinCompany(bd.getString("company"));
            } else if (process == 6) {
                getSupportActionBar().setTitle(" Editar compañia");
                CompanyProject companyProject = new Gson().fromJson(bd.getString("companyInfo"),
                        new TypeToken<CompanyProject>() {
                        }.getType());
                EditCompanyProject(companyProject, bd.getString("company"));
            } else if (process == 7) {
                getSupportActionBar().setTitle(" Vincular usuario");
                AddUserProject(bd.getString("company"), bd.<UserProject>getParcelableArrayList("userprojectistInfo"), bd.getString("finishdate"));
            } else if (process == 8) {
                getSupportActionBar().setTitle(" Editar Usuario");
                UserProject userProject = new Gson().fromJson(bd.getString("userInfo"),
                        new TypeToken<UserProject>() {
                        }.getType());
                EditUserProject(userProject, bd.getString("company"), bd.getString("finishdate"));
            }
        }
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_new_user_admin);
    }

    private String restoreJsonFromFile() {
        String fileName = "file_Json";
        FileInputStream fis;
        try {
            fis = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void EditUserProject(UserProject userProject, String company, String finishdate) {
        AddUserProject addUserProjectFragment = AddUserProject.newInstanceActivityUserEdit(company, projectId, userProject, finishdate);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
        fragmentTransaction.commit();
    }

    private void AddUserProject(String company, ArrayList<UserProject> userProjectList, String finishdate) {
        AddUserProject addUserProjectFragment = AddUserProject.newInstanceActivityUser(company, projectId, userProjectList, finishdate);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
        fragmentTransaction.commit();
    }

    private void addValidityFragment(Validity mValidity) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, ValidityFragment.newInstance("PROYECTO",
                Constantes.CREATE_PROJECT_URL + projectId, mValidity));
        fragmentTransaction.commit();
    }

    private void addMainFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        EditBasicFomFragmnetP mainFragment = EditBasicFomFragmnetP.newInstance();
        fragmentTransaction.add(R.id.container_join, mainFragment);
        fragmentTransaction.commit();
    }

    private void EditCompanyProject(CompanyProject companyProject, String company) {
        AddCompanyProjectFragment newCompanyProjectFragment = AddCompanyProjectFragment.newInstanceActivityEdit(company, projectId, companyProject);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, newCompanyProjectFragment);
        fragmentTransaction.commit();
    }

    private void AddFragmentJoinCompany(String company) {
        AddCompanyProjectFragment newCompanyProjectFragment = AddCompanyProjectFragment.newInstanceActivity(company, projectId);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, newCompanyProjectFragment);
        fragmentTransaction.commit();
    }

    private void EditStagesProject(String projectStagesString) {
        ProjectStages projectStages = new Gson().fromJson(projectStagesString,
                new TypeToken<ProjectStages>() {
                }.getType());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        NewStageProject newStageProjectFragment = NewStageProject.newInstanceEdit(projectId, projectStages);
        fragmentTransaction.add(R.id.container_join, newStageProjectFragment);
        fragmentTransaction.commit();
    }

    private void AddFragmentJoinStages() {
        NewStageProject newStageProjectFragment = NewStageProject.newInstance(projectId);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, newStageProjectFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onValidityFragmentInteraction() {
        //onBackPressed();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCancelValidityFragmentInteraction() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onCancelAction() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyAction() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCancelActionStage() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyActionStage() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCancelActionCompany() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyActionCompany() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCancelActionUser() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyActionUser() {
        setResult(RESULT_OK);
        finish();
    }
}

