package co.tecno.sersoluciones.analityco;

import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.jetbrains.annotations.Nullable;

import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.createCar.CarGeneralInfoFragment;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;

public class DetailsCarActivityTabs extends BaseActivity implements RequestBroadcastReceiver.BroadcastListener {

    private RequestBroadcastReceiver requestBroadcastReceiver;
    private View mProgressView;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        mProgressView = findViewById(R.id.load);
        mViewPager = findViewById(R.id.container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        enableSectionsAdapter();

    }

    private void enableSectionsAdapter() {
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.addFrag(CarGeneralInfoFragment.newInstance(""));
        mSectionsPagerAdapter.addFrag(ProjectsCarListFragment.newInstance(""));
        mSectionsPagerAdapter.addFrag(CarAccessRequirementsFragment.newInstance(""));

        mViewPager.setPageMargin(0);
        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        configTabs();
    }

    private void configTabs() {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        Drawable drawable1 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CAR) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(0).setIcon(drawable1);

        Drawable drawable2 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CITY) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(1).setIcon(drawable2);

        Drawable drawable3 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.PLAYLIST_CHECK) // provide an icon
                .setColor(Color.WHITE) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        tabLayout.getTabAt(2).setIcon(drawable3);
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
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_details_personal_tabs);
    }

    @Override
    public void onStringResult(@Nullable String action, int option, @Nullable String response, @Nullable String url) {

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST);
        intentFilter.addAction(CrudIntentService.ACTION_REQUEST_POST);
        intentFilter.addAction(Constantes.BROADCAST_GET_JSON);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }
}
