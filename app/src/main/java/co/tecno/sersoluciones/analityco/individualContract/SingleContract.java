package co.tecno.sersoluciones.analityco.individualContract;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import net.steamcrafted.materialiconlib.MaterialIconView;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import co.tecno.sersoluciones.analityco.BaseActivity;
import co.tecno.sersoluciones.analityco.R;
import co.tecno.sersoluciones.analityco.SortContractActivity;
import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;

import co.tecno.sersoluciones.analityco.models.InfoIndividualContract;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleEditText;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;


public class SingleContract extends BaseActivity implements ContractSingleListFragment.OnListSingleContractInteractionListener,
        TextWatcherAdapter.TextWatcherListener {

    private LinearLayout searchToolbar;
    private ClearebleEditText editTextSearch;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;
    private int cy;
    private int cx;
    private MaterialIconView materialIconView;
    private FloatingActionButton fab;
    private View mProgressView;
    ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mSectionsPagerAdapter.addFrag(ContractSingleListFragment.newInstance(1));
        mSectionsPagerAdapter.addFrag(ContractSingleListFragment.newInstance(2));
        mSectionsPagerAdapter.addFrag(ContractSingleListFragment.newInstance(3));

        mProgressView = findViewById(R.id.load);
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = findViewById(R.id.tabs);
        fab = findViewById(R.id.fab);
        tabLayout.setupWithViewPager(mViewPager);

        View headerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_tab_singlecontract, null, false);

        LinearLayout linearLayoutOne = headerView.findViewById(R.id.ll);
        RelativeLayout linearLayout2 = headerView.findViewById(R.id.ll2);
        RelativeLayout linearLayout3 = headerView.findViewById(R.id.ll3);


        tabLayout.getTabAt(0).setCustomView(linearLayoutOne);
        tabLayout.getTabAt(1).setCustomView(linearLayout2);
        tabLayout.getTabAt(2).setCustomView(linearLayout3);

        searchToolbar = findViewById(R.id.search_container);
        editTextSearch = findViewById(R.id.search_edit_text);
        editTextSearch.addTextChangedListener(new TextWatcherAdapter(editTextSearch, this));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                log("pos: " + position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(SingleContract.this, CreateIndividualContractActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, Constantes.CREATE);
        });
    }
    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_company_list);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MaterialMenuInflater
                .with(this)
                .setDefaultColor(Color.WHITE)
                .inflate(R.menu.menu_company_list, menu);
        //getMenuInflater().inflate(R.menu.menu_company_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
            case R.id.action_sort:
                Intent intent = new Intent(this, SortContractActivity.class);
                intent.putExtra("posX", cx);
                intent.putExtra("posY", cy);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent, 0,
                            ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                } else {
                    startActivityForResult(intent, 0);
                }

                break;
            case R.id.action_search:
                toolbar.setVisibility(View.GONE);
                expand();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //View v = getCurrentFocus();
        //if (v instanceof EditText) {}
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            cx = (int) event.getX();
            cy = (int) event.getY();
            //log("cx: " + event.getX() + ",cy: " + event.getY());
        }
        return super.dispatchTouchEvent(event);
    }

    public void collapse(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circleReveal(false);
        } else {
            searchToolbar.setVisibility(View.GONE);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
            Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-searchToolbar.getHeight()));
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(alphaAnimation);
            animationSet.addAnimation(translateAnimation);
            animationSet.setDuration(220);
            searchToolbar.startAnimation(animationSet);
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();

        if(fragment instanceof ContractSingleListFragment){
            ((ContractSingleListFragment) fragment).ReloadList();
            ((ContractSingleListFragment) fragment).filtrar("");
            ((ContractSingleListFragment) fragment).filtrarlastName("");
            ((ContractSingleListFragment) fragment).filtrarDni("");
        }
    }
    private void expand() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            circleReveal(true);
        } else {
            TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-searchToolbar.getHeight()), 0.0f);
            translateAnimation.setDuration(220);
            searchToolbar.clearAnimation();
            searchToolbar.startAnimation(translateAnimation);
            searchToolbar.setVisibility(View.VISIBLE);
        }
        editTextSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextSearch, InputMethodManager.SHOW_IMPLICIT);
    }
    @SuppressLint("PrivateResource")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void circleReveal(final boolean isShow) {

        int width = toolbar.getWidth();
        width -= (getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) / 2);

        int cx = width;
        int cy = toolbar.getHeight() / 2;

        Animator anim;

        if (isShow)
            anim = ViewAnimationUtils.createCircularReveal(searchToolbar, cx, cy, 0, (float) width);
        else
            anim = ViewAnimationUtils.createCircularReveal(searchToolbar, cx, cy, (float) width, 0);

        anim.setDuration(250L);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    searchToolbar.setVisibility(View.GONE);
                    //searchToolbarDummy.setVisibility(View.GONE);
                }
            }
        });

        // make the view visible and start the animation
        if (isShow) {
            searchToolbar.setVisibility(View.VISIBLE);
        }

        // start the animation
        anim.start();
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow)
                    toolbar.setVisibility(View.VISIBLE);
            }
        });
    }
    @Override
    public void onTextChanged(EditText view, String text) {
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if(fragment instanceof ContractSingleListFragment){
            ((ContractSingleListFragment) fragment).filtrar(text);
            ((ContractSingleListFragment) fragment).ReloadList();
        }
    }
    @Override
    public void onListFragmentInteraction(String imageView, InfoIndividualContract item) {
        Intent i = new Intent(this, DetailsSingleContractActivityTabs.class);
        i.putExtra(Constantes.ITEM_PHOTO, imageView);
        i.putExtra(Constantes.ITEM_OBJ,item);
        startActivity(i);
    }
}
