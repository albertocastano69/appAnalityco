package co.tecno.sersoluciones.analityco;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import co.tecno.sersoluciones.analityco.fragments.personal.EditPersonalFormFragment;

/**
 * Created by Ser SOluciones SAS on 24/04/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class EditInfoPersonalActivity extends BaseActivity implements EditPersonalFormFragment.OnEditPersonalListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if (bd != null) {
            int process = (Integer) bd.get("process");
            int personalId = (Integer) bd.get("personal");
            if (process == 1) {
                addMainFragment(bd.getString("json"));
                try {
                    JSONObject json = new JSONObject(bd.getString("json"));
                    getSupportActionBar().setTitle(json.getString("Name"));
                    getSupportActionBar().setSubtitle("CC " + json.getString("DocumentNumber"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_new_user_admin);
    }

    private void addMainFragment(String json) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        EditPersonalFormFragment mainFragment = EditPersonalFormFragment.newInstance(json);
        fragmentTransaction.add(R.id.container_join, mainFragment);
        fragmentTransaction.commit();
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

}

