package co.tecno.sersoluciones.analityco;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.tecno.sersoluciones.analityco.dialogs.OfficeDialogFragment;
import co.tecno.sersoluciones.analityco.fragments.ValidityFragment;
import co.tecno.sersoluciones.analityco.fragments.company.EditBasicFormFragment;
import co.tecno.sersoluciones.analityco.fragments.company.NewUserCompanyFragment;
import co.tecno.sersoluciones.analityco.models.BranchOffice;
import co.tecno.sersoluciones.analityco.models.Validity;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

public class NewUserAdminActivity extends BaseActivity implements EditBasicFormFragment.OnMainFragmentInteractionListener,
        NewUserCompanyFragment.OnAddUserContract,ValidityFragment.OnValidityFragmentInteractionListener,OfficeDialogFragment.NoticeDialogListener {

    private String company;
    private EditBasicFormFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            int process = (Integer) bd.get("process");
            company = (String) bd.get("company");
            if(process==1){
                addMainFragment(bd.getString("json"));
                try {
                    JSONObject json=new JSONObject(bd.getString("json"));
                    getSupportActionBar().setTitle(json.getString("Name"));
                    getSupportActionBar().setSubtitle(json.getString("DocumentType")+" "+json.getString("DocumentNumber"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if (process == 2) {
                getSupportActionBar().setTitle("Condiciones de Uso");
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

            }else if (process == 3) {
                getSupportActionBar().setTitle("Vincular Usuario Administrador");
                NewUserCompanyFragment addUserProjectFragment = NewUserCompanyFragment.newInstanceActivityUser(company);
                FragmentManager fragmentManager = this.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
                fragmentTransaction.commit();
            }else if (process == 4) {
                getSupportActionBar().setTitle("Editar Usuario Administrador");
                NewUserCompanyFragment addUserProjectFragment =
                        NewUserCompanyFragment.editInstanceActivityUser(company,bd.getString("email"),
                                bd.getString("name"),bd.getString("photo"),bd.getString("startDate"),bd.getString("finishDate")
                                ,bd.getString("idUser"));
                FragmentManager fragmentManager = this.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.container_join, addUserProjectFragment);
                fragmentTransaction.commit();
            }
        }

    }

    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_new_user_admin);
    }

    private void addValidityFragment(Validity mValidity) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.container_join, ValidityFragment.newInstance("EMPRESA",
                Constantes.CREATE_COMPANY_URL + company, mValidity));
        fragmentTransaction.commit();
    }

    private void addMainFragment(String json) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mainFragment = EditBasicFormFragment.newInstance(json);
        fragmentTransaction.add(R.id.container_join, mainFragment);
        fragmentTransaction.commit();
    }


    @Override
    public void onCancelActionUser() {
        //onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onApplyActionUser() {
        //onBackPressed();
        setResult(RESULT_OK);
        finish();
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
    public void onDialogPositiveClick(BranchOffice branchOffice) {
        mainFragment.onDialogPositiveClick(branchOffice);
    }
}
