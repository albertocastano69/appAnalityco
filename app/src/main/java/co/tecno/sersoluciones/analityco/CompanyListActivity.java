package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import android.text.TextUtils;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.io.ByteArrayOutputStream;

import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.fragments.company.CompanyListFragment;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleEditText;

/**
 * Created by Ser Soluciones SAS on 17/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
public class CompanyListActivity extends BaseActivity implements CompanyListFragment.OnListFragmentInteractionListener,
        TextWatcherAdapter.TextWatcherListener {

    private LinearLayout searchToolbar;
    private ClearebleEditText editTextSearch;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;
    private int cy;
    private int cx;

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_company_list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mSectionsPagerAdapter.addFrag(CompanyListFragment.newInstance(1));
        mSectionsPagerAdapter.addFrag(CompanyListFragment.newInstance(2));
        mSectionsPagerAdapter.addFrag(CompanyListFragment.newInstance(3));
        mSectionsPagerAdapter.addFrag(CompanyListFragment.newInstance(4));

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                if (fragment instanceof CompanyListFragment) {
                    ((CompanyListFragment) fragment).updateList();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        View headerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_tab2, null, false);
        View mBottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheet.setVisibility(View.GONE);
        View topview = findViewById(R.id.top);
        topview.setVisibility(View.GONE);
        LinearLayout linearLayoutOne = headerView.findViewById(R.id.ll);
        RelativeLayout layoutActive = headerView.findViewById(R.id.ll2);
        RelativeLayout layoutNotActive = headerView.findViewById(R.id.ll3);
        RelativeLayout layoutExpire = headerView.findViewById(R.id.ll4);
        tabLayout.getTabAt(0).setCustomView(linearLayoutOne);
        tabLayout.getTabAt(1).setCustomView(layoutActive);
        tabLayout.getTabAt(2).setCustomView(layoutNotActive);
        tabLayout.getTabAt(3).setCustomView(layoutExpire);

        setQuantities();

        searchToolbar = findViewById(R.id.search_container);
        editTextSearch = findViewById(R.id.search_edit_text);
        editTextSearch.addTextChangedListener(new TextWatcherAdapter(editTextSearch, this));


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(CompanyListActivity.this, CompanyActivity.class),
                            ActivityOptions.makeSceneTransitionAnimation(CompanyListActivity.this).toBundle());
                } else {
                    startActivity(new Intent(CompanyListActivity.this, CompanyActivity.class));
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //CompanyListFragment fragment = CompanyListFragment.newInstance(1);
        //replaceFragment(fragment);
    }

    public void setQuantities() {
        String selection = "(" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ?)";
        Cursor cursorActive = getContentResolver().query(Constantes.CONTENT_COMPANY_URI, null, selection, new String[]{"1"}, null);
        Cursor cursorNotActive = getContentResolver().query(Constantes.CONTENT_COMPANY_URI, null, selection, new String[]{"0"}, null);
        selection = "(" + DBHelper.COMPANY_TABLE_COLUMN_EXPIRY + " = ?) and (" + DBHelper.COMPANY_TABLE_COLUMN_ACTIVE + " = ?)";
        Cursor cursorExpire = getContentResolver().query(Constantes.CONTENT_COMPANY_URI, null, selection, new String[]{"1", "1"}, null);

        ((TextView) (tabLayout.getTabAt(1).getCustomView()).findViewById(R.id.quantity_active)).setText(String.valueOf(cursorActive.getCount()));
        ((TextView) (tabLayout.getTabAt(2).getCustomView()).findViewById(R.id.quantity_not_active)).setText(String.valueOf(cursorNotActive.getCount()));
        ((TextView) (tabLayout.getTabAt(3).getCustomView()).findViewById(R.id.quantity_expire)).setText(String.valueOf(cursorExpire.getCount()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof CompanyListFragment) {
            ((CompanyListFragment) fragment).updateList();
        }
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

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
            case R.id.action_sort:
                Intent intent = new Intent(CompanyListActivity.this, SortCompanyActivity.class);
                intent.putExtra("posX", cx);
                intent.putExtra("posY", cy);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent, 0,
                            ActivityOptions.makeSceneTransitionAnimation(CompanyListActivity.this).toBundle());
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

    @SuppressWarnings("unused")
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
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
        if (fragment instanceof CompanyListFragment) {
            ((CompanyListFragment) fragment).syncData(!TextUtils.isEmpty(text) ? text : null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case SortCompanyActivity.ORDER_BY:
                Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                if (fragment instanceof CompanyListFragment) {
                    ((CompanyListFragment) fragment).updateList();
                }
                break;
        }
    }

    @Override
    public void onListFragmentInteraction(CompanyList item, ImageView imageView) {

        Bitmap bitmap;
        byte[] byteArray = new byte[0];
        try {
            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bStream);
            byteArray = bStream.toByteArray();
        } catch (Exception ignored) {
        }

        Intent i = new Intent(this, DetailsCompanyActivityTabs.class);
        i.putExtra("id", item.CompanyInfoId);
        i.putExtra("image", byteArray);
        i.putExtra("model", item);
        //startActivity(i);
        String transitionName = getString(R.string.img_transition);

        ActivityOptions transitionActivityOptions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            transitionActivityOptions = ActivityOptions
                    .makeSceneTransitionAnimation(this, imageView, transitionName);
            startActivity(i, transitionActivityOptions.toBundle());
        } else {
            startActivity(i);
        }
    }
}
