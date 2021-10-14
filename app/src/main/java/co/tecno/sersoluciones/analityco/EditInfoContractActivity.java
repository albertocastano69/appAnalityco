package co.tecno.sersoluciones.analityco;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import co.tecno.sersoluciones.analityco.fragments.contract.AddProjectContract;
import co.tecno.sersoluciones.analityco.fragments.contract.EditContract;
import co.tecno.sersoluciones.analityco.fragments.contract.NewUserContractFragment;
import co.tecno.sersoluciones.analityco.fragments.contract.PersonalContractFragment;
import co.tecno.sersoluciones.analityco.fragments.project.AddUserProject;
import co.tecno.sersoluciones.analityco.models.Contract;
import co.tecno.sersoluciones.analityco.models.Personal;
import co.tecno.sersoluciones.analityco.models.ProjectList;
import co.tecno.sersoluciones.analityco.models.UserProject;

public class EditInfoContractActivity extends BaseActivity implements AddUserProject.OnAddUserProject,
        EditContract.OnEditContract, AddProjectContract.OnAddProjectContract, NewUserContractFragment.OnAddUserContract,
        PersonalContractFragment.OnAddPersonalContract {

    private String contractId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            int process = (Integer) bd.get("process");
            contractId = (String) bd.get("contract");
            if (process == 1) {
                getSupportActionBar().setTitle("Editar Contrato");
                Contract infoContract = new Gson().fromJson(bd.getString("json"),
                        new TypeToken<Contract>() {
                        }.getType());
                addMainFragment(infoContract);
            } else if (process == 2) {
                getSupportActionBar().setTitle("Asociar proyceto");
                AddFragmentProjects();
            } else if (process == 3) {
                getSupportActionBar().setTitle("Editar proyecto del contrato");
                ProjectList infoContract = new Gson().fromJson(bd.getString("json"),
                        new TypeToken<ProjectList>() {
                        }.getType());
                AddFragmentProjectsEdit(infoContract);
            } else if (process == 4) {
                getSupportActionBar().setTitle("Asociar Usuario");
                AddUserContract((String) bd.get("company"), bd.getParcelable("userprojectistInfo"), (String) bd.get("finishDate"));
            } else if (process == 5) {
                getSupportActionBar().setTitle(" Editar Usuario");
                UserProject userProject = new Gson().fromJson(bd.getString("userInfo"),
                        new TypeToken<UserProject>() {
                        }.getType());
                EditUserContract(userProject, (String) bd.get("company"), (String) bd.get("finishDate"));
            } else if (process == 6) {
                getSupportActionBar().setTitle(" Vincular Personal");
                addPersonal(bd.getString("companyInfoId"), (String) bd.get("startDate"), (String) bd.get("finishDate"), (String) bd.get("contractType"));
            } else if (process == 7) {
                getSupportActionBar().setTitle(" Editar Personal");
                Personal personal = new Gson().fromJson(bd.getString("personal"),
                        new TypeToken<Personal>() {
                        }.getType());
                editPersonal(bd.getString("companyInfoId"), personal, (String) bd.get("startDate"), (String) bd.get("finishDate"), (String) bd.get("contractType"));
            }
        }

    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_new_user_admin);
    }

    private void editPersonal(String companyInfoId, Personal personal, String startdate, String finishdate, String contractType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PersonalContractFragment mainFragment = PersonalContractFragment.newInstanceEdit(companyInfoId, personal, contractId, startdate, finishdate, contractType);
        fragmentTransaction.add(R.id.container_join, mainFragment);
        fragmentTransaction.commit();
    }

    private void addPersonal(String companyInfoId, String startdate, String finishdate, String contractType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PersonalContractFragment mainFragment = PersonalContractFragment.newInstance(companyInfoId, contractId, startdate, finishdate, contractType);
        fragmentTransaction.add(R.id.container_join, mainFragment);
        fragmentTransaction.commit();
    }

    private void addMainFragment(Contract contractEdit) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        EditContract mainFragment = EditContract.newInstance(contractEdit);
        fragmentTransaction.add(R.id.container_join, mainFragment);
        fragmentTransaction.commit();
    }

    private void AddFragmentProjects() {
        AddProjectContract addUserProjectFragment = AddProjectContract.newInstance(contractId);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
        fragmentTransaction.commit();
    }

    private void AddFragmentProjectsEdit(ProjectList project) {
        AddProjectContract addUserProjectFragment = AddProjectContract.newInstanceEdit(project, contractId);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
        fragmentTransaction.commit();
    }

    private void AddUserContract(String company, ArrayList<UserProject> userContractList, String finishdate) {
        NewUserContractFragment addUserProjectFragment = NewUserContractFragment.newInstanceActivityUser(company, contractId, userContractList, finishdate);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
        fragmentTransaction.commit();
    }

    private void EditUserContract(UserProject userProject, String company, String finishdate) {
        NewUserContractFragment addUserProjectFragment = NewUserContractFragment.newInstanceActivityUserEdit(company, contractId, userProject, finishdate);
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
        fragmentTransaction.commit();
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

    @Override
    public void onCancelActionEdit() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyActionEdit() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCancelActionAddProject() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyActionAddProject() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCancelActionAddPersonal() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyActionAddPersonal() {
        Intent i = new Intent();
        i.putExtra("newUser", true);
        setResult(RESULT_OK, i);
        finish();
    }
}

