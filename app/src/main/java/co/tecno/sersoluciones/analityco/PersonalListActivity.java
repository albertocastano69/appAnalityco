package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.steamcrafted.materialiconlib.MaterialIconView;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import java.util.Calendar;

import co.tecno.sersoluciones.analityco.adapters.SectionsPagerAdapter;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.fragments.personal.PersonalListFragment;
import co.tecno.sersoluciones.analityco.fragments.personal.PersonalNotificationFragments;
import co.tecno.sersoluciones.analityco.models.Notification;
import co.tecno.sersoluciones.analityco.models.PersonalList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.nav.CreatePersonalActivity;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleEditText;

import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;

public class PersonalListActivity extends BaseActivity implements PersonalListFragment.OnListPersonalInteractionListener,
        TextWatcherAdapter.TextWatcherListener {

    private LinearLayout searchToolbar;
    private ClearebleEditText editTextSearch;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Notification notification;
    private TabLayout tabLayout;
    private int cy;
    private int cx;
    private MaterialIconView materialIconView;
    private FloatingActionButton fab;
    private boolean multiAdmin = true;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        Gson gson = new Gson();
        notification = gson.fromJson((String) bd.get("Notifiation"), new TypeToken<Notification>() {
        }.getType());
        int countNotification = 0;

       // logW("Notification: " + notification);

            if (notification.CountPersonal > 0) countNotification++;

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mSectionsPagerAdapter.addFrag(PersonalListFragment.newInstance(1));
        mSectionsPagerAdapter.addFrag(PersonalListFragment.newInstance(2));
        mSectionsPagerAdapter.addFrag(PersonalListFragment.newInstance(3));
        mSectionsPagerAdapter.addFrag(PersonalNotificationFragments.newInstance((String)bd.get("Notifiation")));

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

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


       /* tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 3:
                        Intent intent = new Intent(PersonalListActivity.this, NotificationList.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("Notifiation", new Gson().toJson(notification));
                        intent.putExtra("item", 1);
                        startActivity(intent);
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/

        searchToolbar = findViewById(R.id.search_container);
        editTextSearch = findViewById(R.id.search_edit_text);
        editTextSearch.addTextChangedListener(new TextWatcherAdapter(editTextSearch, this));
        TextView title = findViewById(R.id.title);
        title.setText("PERSONAL CREADO");
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        fab = findViewById(R.id.fab);
        preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        ((TextView) (tabLayout.getTabAt(3).getCustomView()).findViewById(R.id.quantity_expire)).setText(String.valueOf(countNotification));
        user = new Gson().fromJson(profile, User.class);

        if (user.Companies.size() == 1) multiAdmin = false;

        fab.setOnClickListener(view -> {
            if (user.IsSuperUser || user.IsAdmin || !multiAdmin) {
//                Intent intent = new Intent(PersonalListActivity.this, PersonalWizardActivity.class);
                Intent intent5 = new Intent(PersonalListActivity.this, CreatePersonalActivity.class);
                intent5.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent5, Constantes.CREATE);
            } else {
                Intent intent6 = new Intent(PersonalListActivity.this, SelectCompanyPermissions.class);
                intent6.putExtra("permission", "personals.add");
                intent6.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent6, Constantes.CREATE,
                            ActivityOptions.makeSceneTransitionAnimation(PersonalListActivity.this).toBundle());
                } else {
                    startActivityForResult(intent6, Constantes.CREATE);
                }

            }
        });//90801060409021

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(final int position) {
                log("pos: " + position);
                Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                if (fragment instanceof PersonalListFragment) {
                    ((PersonalListFragment) fragment).updateList();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setQuantities();
        materialIconView = findViewById(R.id.icon);

        logW("ON CREATE PERSONAL LIST");
        validatePermissions();
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_company_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(() -> {
            Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
            if (fragment instanceof PersonalListFragment) {
                try (Cursor cursor = getContentResolver().query(Constantes.CONTENT_PERSONAL_URI, null,
                        null, null, null)) {
                    if (cursor != null && cursor.getCount() == 0) {
                        ((PersonalListFragment) fragment).syncPersonal();
                    }
                }
            }
        }, 500);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof PersonalListFragment) {
            ((PersonalListFragment) fragment).updateList();
        }
    }

    public void setQuantities() {
        String selection = "(" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ?)";
        Cursor cursorActive = getContentResolver().query(Constantes.CONTENT_PERSONAL_URI, null, selection, new String[]{"1"}, null);
        Cursor cursorNotActive = getContentResolver().query(Constantes.CONTENT_PERSONAL_URI, null, selection, new String[]{"0"}, null);
        selection = "(" + DBHelper.PERSONAL_TABLE_COLUMN_EXPIRY + " = ?) and (" + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ?)";
        Cursor cursorExpire = getContentResolver().query(Constantes.CONTENT_PERSONAL_URI, null, selection, new String[]{"1", "1"}, null);

        ((TextView) (tabLayout.getTabAt(1).getCustomView()).findViewById(R.id.quantity_active)).setText(String.valueOf(cursorActive.getCount()));
        ((TextView) (tabLayout.getTabAt(2).getCustomView()).findViewById(R.id.quantity_not_active)).setText(String.valueOf(cursorNotActive.getCount()));
        ((TextView) (tabLayout.getTabAt(3).getCustomView()).findViewById(R.id.quantity_expire)).setText(String.valueOf(cursorExpire.getCount()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MaterialMenuInflater
                .with(this)
                .setDefaultColor(Color.WHITE)
                .inflate(R.menu.menu_personal_list, menu);
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
                Intent intent = new Intent(this, SortPersonalActivity.class);
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

            case R.id.action_update:
                Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                if (fragment instanceof PersonalListFragment) {
                    ((PersonalListFragment) fragment).syncPersonal();
                }
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
        if (fragment instanceof PersonalListFragment) {
            ((PersonalListFragment) fragment).syncData(!TextUtils.isEmpty(text) ? text : null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logW("llega aca de nuevo requestCode: " + requestCode + ", resultCode: " + resultCode);
        switch (requestCode) {
            case Constantes.CREATE:
                if (resultCode == Activity.RESULT_OK)
                    restartActivity();
                break;
            case Constantes.UPDATE:
            case Constantes.DETAILS:
                processResultCode(resultCode);
                break;
            default:
                if (resultCode == SortCompanyActivity.ORDER_BY) {
                    Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
                    if (fragment instanceof PersonalListFragment) {
                        ((PersonalListFragment) fragment).updateList();
                    }
                }
                break;
        }
    }


    private void processResultCode(int resultCode) {
        switch (resultCode) {
            case RESULT_OK:
                logW("Reinicio de actividad personal list");
                restartActivity();
                break;
            case RESULT_CANCELED:
                break;
            default:
                break;
        }
    }

    private void restartActivity() {

        Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment instanceof PersonalListFragment) {
            ((PersonalListFragment) fragment).syncPersonal();
            ((PersonalListFragment) fragment).updateList(String.format("%s %s", DBHelper.PERSONAL_TABLE_COLUMN_SERVER_ID, "DESC"));
        }
        recreate();
    }

    @Override
    public void onListFragmentInteraction(PersonalList item, ImageView imageView) {

        Intent i = new Intent(this, DetailsPersonalActivityTabs.class);
        i.putExtra(Constantes.ITEM_OBJ, item);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptions transitionActivityOptions = ActivityOptions
                .makeSceneTransitionAnimation(this, imageView, ViewCompat.getTransitionName(imageView));
        startActivityForResult(i, Constantes.DETAILS, transitionActivityOptions.toBundle());
    }

    private void validatePermissions() {
        //permisos usuario administrativo
        if (user.IsAdmin && !user.IsSuperUser) {
            if (!user.claims.contains("personals.add")) fab.setVisibility(View.GONE);
        } else if (!multiAdmin && !user.IsSuperUser) {
            if (!user.Companies.get(0).Permissions.contains("personals.add"))
                fab.setVisibility(View.GONE);
        }
    }
}
