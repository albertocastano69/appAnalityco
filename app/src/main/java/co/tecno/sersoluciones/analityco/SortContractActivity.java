package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.IdRes;
import android.transition.Explode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;



import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.BindView;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

/**
 * Created by Ser SOluciones SAS on 14/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class SortContractActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {


    @BindView(R.id.option_name)
    RadioButton optionName;
    @BindView(R.id.option_nit)
    RadioButton optionNit;
    @BindView(R.id.option_date)
    RadioButton optionDate;

    @BindView(R.id.option_address)
    RadioButton optionAddress;
    @BindView(R.id.option_active)
    RadioButton optionActive;
    @BindView(R.id.rg_sort_asc_desc)
    RadioGroup optionsOrderBy;
    @BindView(R.id.option_asc)
    RadioButton optionAsc;
    @BindView(R.id.option_desc)
    RadioButton optionDesc;


    private int posX, posY;
    private String orderByValue;
    private MyPreferences preferences;
    private boolean option;

    private static final int ORDER_BY = 0;
    private static final int NULL = 6;
    private String orderType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // set an exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getSupportActionBar().setTitle("");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);

        Drawable upArrow = MaterialDrawableBuilder.with(this)
                .setIcon(MaterialDrawableBuilder.IconValue.ARROW_LEFT)
                .setColor(Color.WHITE)
                .setToActionbarSize()
                .build();
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        preferences = new MyPreferences(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            posX = extras.getInt("posX");
            posY = extras.getInt("posY");
        }

        optionName.setOnClickListener(this);
        optionNit.setOnClickListener(this);
        optionDate.setOnClickListener(this);
        optionAddress.setOnClickListener(this);
        optionActive.setOnClickListener(this);

        optionNit.setText("Por Numero");

        optionAddress.setVisibility(View.GONE);

        optionsOrderBy.setOnCheckedChangeListener(this);
        optionAsc.setChecked(false);
        optionDesc.setChecked(false);
        orderByValue = DBHelper.CONTRACT_TABLE_COLUMN_NAME;
        orderType = "ASC";
        option = false;
        cleanRB();

        String[] orderBy = preferences.getOrderContractBy().split(" ");
        orderByValue = orderBy[0];
        orderType = orderBy[1];
        switch (orderByValue) {
            case DBHelper.CONTRACT_TABLE_COLUMN_NAME:
                optionName.setChecked(true);
                break;
            case DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER:
                optionNit.setChecked(true);
                break;
            case DBHelper.CONTRACT_TABLE_COLUMN_FINISH_DATE:
                optionDate.setChecked(true);
                break;
            case DBHelper.COMPANY_TABLE_COLUMN_ADDRESS:
                optionAddress.setChecked(true);
                break;
            case DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE:
                optionActive.setChecked(true);
                break;
        }

        ((RadioButton) optionsOrderBy.getChildAt((Constantes.DICT_ASC_ORDER_BY.get(orderType) * 2) - 1))
                .setChecked(true);
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_filter);
    }

    private void cleanRB() {
        optionName.setChecked(false);
        optionNit.setChecked(false);
        optionDate.setChecked(false);
        optionAddress.setChecked(false);
        optionActive.setChecked(false);
        //((RadioButton) optionsOrderBy.getChildAt(2)).setChecked(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    private void init() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                View rootView = findViewById(android.R.id.content);
                //int cx = (rootView.getLeft() + rootView.getRight()) / 2;
                //int cy = rootView.getBottom();
                int finalRadius = Math.max(rootView.getWidth(), rootView.getHeight());

                Animator anim;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    anim = ViewAnimationUtils.createCircularReveal(rootView, posX, posY, 0, finalRadius);
                    anim.setDuration(300);
                    anim.start();
                }
                int color = Color.rgb(240, 240, 240);
                rootView.setBackgroundColor(color);
            }
        }, 100);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
            case R.id.action_apply:
                option = true;
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        cleanRB();
        switch (view.getId()) {
            case R.id.option_name:
                optionName.setChecked(true);
                orderByValue = DBHelper.CONTRACT_TABLE_COLUMN_NAME;
                break;
            case R.id.option_nit:
                optionNit.setChecked(true);
                orderByValue = DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER;
                break;
            case R.id.option_date:
                optionDate.setChecked(true);
                orderByValue = DBHelper.CONTRACT_TABLE_COLUMN_FINISH_DATE;
                break;
            case R.id.option_address:
                optionAddress.setChecked(true);
                orderByValue = DBHelper.COMPANY_TABLE_COLUMN_ADDRESS;
                break;
            case R.id.option_active:
                optionActive.setChecked(true);
                orderByValue = DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE;
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.option_asc:
                log("entra option_asc");
                orderType = "ASC";
                break;
            case R.id.option_desc:
                log("entra option_desc");
                orderType = "DESC";
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        if (option) {
            if (orderByValue != null && !orderByValue.isEmpty()) {
                i.putExtra(Constantes.KEY_ORDER_BY, String.format("%s %s", orderByValue, orderType));
                preferences.setOrderContractBy(String.format("%s %s", orderByValue, orderType));
            } else preferences.setOrderContractBy("");
            setResult(ORDER_BY, i);
        } else {
            setResult(NULL, i);
        }
        finish();
        //overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_filter, menu);
        return true;
    }
}
