package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Explode;
import android.util.DisplayMetrics;
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

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import net.steamcrafted.materialiconlib.MaterialIconView;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.util.Calendar;

import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.fragments.contract.ContractsListFragment;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.CrudIntentService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleEditText;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 25/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class ContractsListActivity extends BaseActivity
        implements ContractsListFragment.OnListContractInteractionListener,
        TextWatcherAdapter.TextWatcherListener {

    private LinearLayout searchToolbar;
    private ClearebleEditText editTextSearch;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout tabLayout;
    private int cx;
    private int cy;
    private FloatingActionButton fab;
    private User user;
    private boolean multiAdmin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);

        try (Cursor cursor = getContentResolver().query(Constantes.CONTENT_CONTRACT_URI, null, null, null, null)) {
            if (cursor != null && cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(Constantes.KEY_SELECT, true);
                String paramsQuery = HttpRequest.makeParamsInUrl(values);
                CrudIntentService.startRequestCRUD(
                        this, Constantes.LIST_CONTRACTS_URL,
                        Request.Method.GET, "", paramsQuery, true, true
                );
            }
        }

        toolbar.setTitle("CONTRATO");

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mSectionsPagerAdapter.addFrag(ContractsListFragment.newInstance(1));
        mSectionsPagerAdapter.addFrag(ContractsListFragment.newInstance(2));
        mSectionsPagerAdapter.addFrag(ContractsListFragment.newInstance(3));
        mSectionsPagerAdapter.addFrag(ContractsListFragment.newInstance(4));

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        @SuppressLint("InflateParams")
        View headerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_tab2, null, false);

        LinearLayout linearLayoutOne = headerView.findViewById(R.id.ll);
        RelativeLayout linearLayout2 = headerView.findViewById(R.id.ll2);
        RelativeLayout linearLayout3 = headerView.findViewById(R.id.ll3);
        RelativeLayout linearLayout4 = headerView.findViewById(R.id.ll4);

        tabLayout.getTabAt(0).setCustomView(linearLayoutOne);
        tabLayout.getTabAt(1).setCustomView(linearLayout2);
        tabLayout.getTabAt(2).setCustomView(linearLayout3);
        tabLayout.getTabAt(3).setCustomView(linearLayout4);
        setQuantities();
        searchToolbar = findViewById(R.id.search_container);
        editTextSearch = findViewById(R.id.search_edit_text);
        editTextSearch.addTextChangedListener(new TextWatcherAdapter(editTextSearch, this));
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);

        fab = findViewById(R.id.fab);

        if (user.Companies.size() == 1) multiAdmin = false;

        fab.setOnClickListener(view -> {
            if (user.IsSuperUser || user.IsAdmin || !multiAdmin) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(new Intent(ContractsListActivity.this, ContractActivity.class), Constantes.CREATE,
                            ActivityOptions.makeSceneTransitionAnimation(ContractsListActivity.this).toBundle());
                } else {
                    startActivityForResult(new Intent(ContractsListActivity.this, ContractActivity.class), Constantes.CREATE);
                }
            } else {
                Intent intent = new Intent(ContractsListActivity.this, SelectCompanyPermissions.class);
                intent.putExtra("permission", "contracts.add");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent, Constantes.CREATE,
                            ActivityOptions.makeSceneTransitionAnimation(ContractsListActivity.this).toBundle());
                } else {
                    startActivityForResult(intent, Constantes.CREATE);
                }
            }

        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("CONTRATOS");
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                if (fragment instanceof ContractsListFragment) {
                    ((ContractsListFragment) fragment).updateList();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TextView title = findViewById(R.id.title);
        title.setText("Contratos Creados");
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        validatePermissions();
    }

    public void setQuantities() {
        String selection = "(" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ?)";
        Cursor cursorActive = getContentResolver().query(Constantes.CONTENT_CONTRACT_URI, null, selection, new String[]{"1"}, null);
        Cursor cursorNotActive = getContentResolver().query(Constantes.CONTENT_CONTRACT_URI, null, selection, new String[]{"0"}, null);
        selection = "(" + DBHelper.CONTRACT_TABLE_COLUMN_EXPIRY + " = ?) and (" + DBHelper.CONTRACT_TABLE_COLUMN_ACTIVE + " = ?)";
        Cursor cursorExpire = getContentResolver().query(Constantes.CONTENT_CONTRACT_URI, null, selection, new String[]{"1", "1"}, null);

        ((TextView) (tabLayout.getTabAt(1).getCustomView()).findViewById(R.id.quantity_active)).setText(String.valueOf(cursorActive.getCount()));
        ((TextView) (tabLayout.getTabAt(2).getCustomView()).findViewById(R.id.quantity_not_active)).setText(String.valueOf(cursorNotActive.getCount()));
        ((TextView) (tabLayout.getTabAt(3).getCustomView()).findViewById(R.id.quantity_expire)).setText(String.valueOf(cursorExpire.getCount()));
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
    protected void onResume() {
        super.onResume();
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof ContractsListFragment) {
            ((ContractsListFragment) fragment).updateList();
        }
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
        if (fragment instanceof ContractsListFragment) {
            ((ContractsListFragment) fragment).syncData(!TextUtils.isEmpty(text) ? text : null);
        }
    }

    @Override
    public void onContractInteraction(ObjectList item, ImageView imageView) {
//        Bitmap bitmap;
//        byte[] byteArray = new byte[0];
//        try {
//            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bStream);
//            byteArray = bStream.toByteArray();
//        } catch (Exception ignored) {
//        }

        //Intent i = new Intent(this, DetailsContractsActivity.class);
        Intent i = new Intent(this, DetailsContractsActivityTabs.class);
        i.putExtra(Constantes.ITEM_OBJ, item);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptions transitionActivityOptions = ActivityOptions
                .makeSceneTransitionAnimation(this, imageView, ViewCompat.getTransitionName(imageView));
        startActivity(i, transitionActivityOptions.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constantes.CREATE:
            case Constantes.UPDATE:
                processResultCode(resultCode);
                break;
            default:
                if (resultCode == SortCompanyActivity.ORDER_BY) {
                    Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                    if (fragment instanceof ContractsListFragment) {
                        ((ContractsListFragment) fragment).updateList();
                    }
                }
                break;
        }
    }

    private void processResultCode(int resultCode) {
        switch (resultCode) {
            case RESULT_OK:
                restartActivity();
                break;
            case RESULT_CANCELED:
                break;
            default:
                break;
        }
    }

    private void restartActivity() {
        ContentValues values = new ContentValues();
        values.put(Constantes.KEY_SELECT, true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CrudIntentService.startRequestCRUD(
                this, Constantes.LIST_CONTRACTS_URL,
                Request.Method.GET, "", paramsQuery, true, true
        );
        recreate();
    }

    private void validatePermissions() {
        preferences = new MyPreferences(this);

        if (user.IsAdmin && !user.IsSuperUser) {
            if (!user.claims.contains("contracts.add")) fab.setVisibility(View.GONE);
        } else if (!multiAdmin && !user.IsSuperUser) {
            if (!user.Companies.get(0).Permissions.contains("contracts.add"))
                fab.setVisibility(View.GONE);
        }
    }
}
