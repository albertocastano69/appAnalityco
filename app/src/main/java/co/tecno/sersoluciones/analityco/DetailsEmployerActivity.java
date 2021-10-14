package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.OnClick;
import co.tecno.sersoluciones.analityco.fragments.employer.EditEmployerFormFragment;
import co.tecno.sersoluciones.analityco.models.ClaimsBasicUser;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 01/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class DetailsEmployerActivity extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener,
        EditEmployerFormFragment.OnEditEmployerListener {

    @BindView(R.id.header_img)
    ImageView headerImg;

    @BindView(R.id.text_web)
    TextView textWeb;
    @BindView(R.id.icon_address)
    MaterialIconView iconAddress;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.icon_mail)
    MaterialIconView iconMail;
    @BindView(R.id.text_mail)
    TextView textMail;
    @BindView(R.id.icon_phone)
    MaterialIconView iconPhone;
    @BindView(R.id.text_phone)
    TextView textPhone;
    @BindView(R.id.text_rol)
    TextView textRol;
    @BindView(R.id.text_nit)
    TextView textNit;
    @BindView(R.id.text_descripcion)
    TextView textDescription;
    @BindView(R.id.text_arl)
    TextView textArl;
    @BindView(R.id.icon_edit_main_form)
    MaterialIconView iconEdit;
    @BindView(R.id.companyManage)
    ImageView companyManage;

    private View mProgressView;
    private View mViewForm;

    private RequestBroadcastReceiver requestBroadcastReceiver;

    private String webSite;
    //private ProgressDialog progressDialog;
    private String employerId;
    private JSONObject jsonInstance;
    private EditEmployerFormFragment mainFragment;
    private ClaimsBasicUser claims;
    private int isManaged;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("EMPLEADOR");

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();
        ObjectList mItem = extras.getParcelable(Constantes.ITEM_OBJ);
        isManaged = extras.getInt("IsManaged");
        employerId = mItem.Id;

        String imageUrl = Constantes.URL_IMAGES + mItem.Logo;
        String imageTransitionName = extras.getString(Constantes.ITEM_TRANSITION_NAME);
        headerImg.setTransitionName(imageTransitionName);


        if(isManaged == 1){
            companyManage.setVisibility(View.VISIBLE);
        }else {
            companyManage.setVisibility(View.GONE);
        }
        Picasso.get()
                .load(imageUrl)
                .noFade()
                .resize(0, 150)
                .placeholder(R.drawable.image_not_available)
                .error(R.drawable.image_not_available)
                .into(headerImg);
        String documentType = mItem.DocumentType;
        //labelNit.setText(documentType);
        String docNumber = mItem.DocumentNumber;
        if (documentType.equalsIgnoreCase("NIT")) {
            docNumber = docNumber.substring(0, docNumber.length() - 1) + docNumber.substring(docNumber.length() - 1);
        }
        //textNit.setText(docNumber);
        //textName.setText(employer.Name);
        getSupportActionBar().setTitle(mItem.Name);
        getSupportActionBar().setSubtitle(documentType + " " + docNumber);


        mViewForm = findViewById(R.id.card_main);
        mProgressView = findViewById(R.id.progress);
        showProgress(true);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_EMPLOYERS_URL + employerId,
                Request.Method.GET, "", "", false);

        CrudIntentService.startRequestCRUD(this, Constantes.EMPLOYER_PERMISSION + employerId,
                Request.Method.GET, "", "", false);

       /* ContentValues values = new ContentValues();
        values.put("company", companyId);
        values.put(Constantes.KEY_SELECT, true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_PROJECTS_BY_COMPANY_URL,
                Request.Method.GET, "", paramsQuery, true);

        values = new ContentValues();
        values.put("id", companyId);
        paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(this, Constantes.LIST_USERS_BY_COMPANY_URL,
                Request.Method.GET, "", paramsQuery, true);*/
        validatePermissions();
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_details_employer);
    }

    private void addMainFragment(String json) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mainFragment = EditEmployerFormFragment.newInstance(json);
        fragmentTransaction.add(R.id.container_main, mainFragment);
        fragmentTransaction.commit();
    }


    private void removeMainFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(mainFragment);
        fragmentTransaction.commit();
    }

    @SuppressLint("SetTextI18n")
    private void fillForm(JSONObject jsonObject) throws JSONException {

        jsonInstance = jsonObject;
        String role = jsonObject.getString("Rol");
        textRol.setText(role);

        if (!jsonObject.getString("Address").isEmpty()) {
            textAddress.setText(String.format("%s, %s, %s",
                    jsonObject.getString("Address"), jsonObject.getString("CityName"), jsonObject.getString("StateName")));
        }
        webSite = jsonObject.isNull("Website") ? "" : jsonObject.getString("Website");
        textWeb.setPaintFlags(textWeb.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textWeb.setText(webSite);
        textWeb.setTextColor(Color.BLUE);

        String email = jsonObject.getString("Email");
        textMail.setPaintFlags(textMail.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textMail.setText(email);
        textMail.setTextColor(Color.BLUE);

        String phone = jsonObject.getString("Phone");
        textPhone.setPaintFlags(textPhone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textPhone.setText(phone);
        textPhone.setTextColor(Color.BLUE);

        String nit = jsonObject.getString("DocumentNumber");
        textNit.setText(nit);

        String arl = jsonObject.getString("NameARL");
        textArl.setText(arl);

        String descripcion = jsonObject.getString("ServicesProvided");
        textDescription.setText(descripcion);

    }

    @OnClick(R.id.text_web)
    public void clickWeb() {
        PopupMenu popup = new PopupMenu(this, textWeb);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Navegar");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(webSite));
                startActivity(i);
                return true;
            }
        });
        popup.show();
    }

    @OnClick(R.id.text_mail)
    public void clickEmail() {
        PopupMenu popup = new PopupMenu(this, textMail);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Enviar Correo");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + textMail.getText().toString()));
                intent.putExtra(Intent.EXTRA_EMAIL, textMail.getText().toString());

                startActivity(Intent.createChooser(intent, "Send Email"));
                return true;
            }
        });
        popup.show();
    }


    @OnClick(R.id.text_phone)
    public void clickPhone() {
        PopupMenu popup = new PopupMenu(this, textPhone);
        popup.getMenuInflater()
                .inflate(R.menu.menu_call, popup.getMenu());
        popup.getMenu().getItem(0).setTitle("Marcar");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Intent i = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + textPhone.getText().toString()));
                startActivity(i);
                return true;
            }
        });
        popup.show();
    }

    @OnClick(R.id.icon_address)
    public void openGoogleMaps() {
        String map = "http://maps.google.co.in/maps?q=" + textAddress.getText().toString();
        Intent i = new Intent(Intent.ACTION_VIEW,
                Uri.parse(map));
        startActivity(i);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        //progressDialog.dismiss();
        showProgress(false);
        if (action.equals(CRUDService.ACTION_REQUEST_SAVE) && url.equals(Constantes.LIST_EMPLOYERS_URL)) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        } else {
            switch (option) {
                case Constantes.SUCCESS_REQUEST:
                    //log("jsonObjStr: " + res);
                    if (url.equals(Constantes.EMPLOYER_PERMISSION + employerId)) {
                        claims = new Gson().fromJson(jsonObjStr,
                                new TypeToken<ClaimsBasicUser>() {
                                }.getType());
                        validatePermissions();
                    } else {
                        try {
                            fillForm(new JSONObject(jsonObjStr));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Constantes.SEND_REQUEST:
                    logW(jsonObjStr);
                    //fillListProjects(jsonObjStr);
                    break;
                case Constantes.UPDATE_ADMIN_USERS:
                    logW(jsonObjStr);
                    //fillListUsers(jsonObjStr);
                    break;
                case Constantes.NOT_INTERNET:
                case Constantes.BAD_REQUEST:
                case Constantes.TIME_OUT_REQUEST:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Alerta")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            })
                            .setMessage("Sin conexion con el servidor");
                    builder.create().show();
                    break;
            }
        }
    }

    //MODULO DE EDITAR

    public void updateVisivilityMain(View view) {
        findViewById(R.id.container_main).setVisibility(View.VISIBLE);
        findViewById(R.id.card_main).setVisibility(View.GONE);
        addMainFragment(jsonInstance.toString());
    }

    @Override
    public void onCancelAction() {
        findViewById(R.id.container_main).setVisibility(View.GONE);
        findViewById(R.id.card_main).setVisibility(View.VISIBLE);
        removeMainFragment();
    }

    public void onApplyAction() {
        findViewById(R.id.container_main).setVisibility(View.GONE);
        findViewById(R.id.card_main).setVisibility(View.VISIBLE);
        removeMainFragment();

        ContentValues values = new ContentValues();
        values.put(Constantes.KEY_SELECT, true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CRUDService.startRequest(this, Constantes.LIST_EMPLOYERS_URL,
                Request.Method.GET, paramsQuery, true);
        showProgress(true);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mViewForm.setVisibility(show ? View.GONE : View.VISIBLE);
        mViewForm.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mViewForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void validatePermissions() {
        preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        if (user.IsAdmin && !user.IsSuperUser) {
            if(!user.claims.contains("employers.update")){
                iconEdit.setColor(ContextCompat.getColor(this,R.color.gray));
                iconEdit.setEnabled(false);
            }
            if(!user.claims.contains("employersmanagedcompanyies.update") && user.claims.contains("employers.update")){
                if(isManaged == 1){
                    iconEdit.setVisibility(View.GONE);
                }else {
                    iconEdit.setVisibility(View.VISIBLE);
                }
            }
        } else if (claims != null && !user.IsSuperUser) {
            if(!user.claims.contains("employers.update")){
                iconEdit.setColor(ContextCompat.getColor(this,R.color.gray));
                iconEdit.setEnabled(false);
            }
            if(!user.claims.contains("employersmanagedcompanyies.update") && user.claims.contains("employers.update")){
                if(isManaged == 1){
                    iconEdit.setVisibility(View.GONE);
                }else {
                    iconEdit.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
