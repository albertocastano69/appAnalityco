package co.tecno.sersoluciones.analityco;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Explode;
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
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import net.steamcrafted.materialiconlib.MaterialIconView;
import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import co.tecno.sersoluciones.analityco.callback.OnListEmployerInteractionListener;
import co.tecno.sersoluciones.analityco.fragments.employer.EmployerListFragment;
import co.tecno.sersoluciones.analityco.models.ObjectList;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.TextWatcherAdapter;
import co.tecno.sersoluciones.analityco.views.ClearebleEditText;

public class EmployersListActivity extends BaseActivity implements OnListEmployerInteractionListener,
        TextWatcherAdapter.TextWatcherListener {

    private LinearLayout searchToolbar;
    private ClearebleEditText editTextSearch;
    private EmployerListFragment fragment;

    private int cy;
    private int cx;
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

        fragment = EmployerListFragment.newInstance(1);
        replaceFragment(fragment);

        searchToolbar = findViewById(R.id.search_container);

        editTextSearch = findViewById(R.id.search_edit_text);
        editTextSearch.addTextChangedListener(new TextWatcherAdapter(editTextSearch, this));
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);

        if (user.Companies.size() == 1) multiAdmin = false;
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if (user.IsSuperUser || user.IsAdmin || !multiAdmin) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(new Intent(EmployersListActivity.this, EmployerActivity.class), Constantes.CREATE,
                            ActivityOptions.makeSceneTransitionAnimation(EmployersListActivity.this).toBundle());
                } else {
                    startActivityForResult(new Intent(EmployersListActivity.this, EmployerActivity.class), Constantes.CREATE);
                }
            } else {
                Intent intent = new Intent(EmployersListActivity.this, SelectCompanyPermissions.class);
                intent.putExtra("permission", "employers.add");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivityForResult(intent, Constantes.CREATE,
                            ActivityOptions.makeSceneTransitionAnimation(EmployersListActivity.this).toBundle());
                } else {
                    startActivityForResult(intent, Constantes.CREATE);
                }
            }

        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView title = findViewById(R.id.title);
        title.setText("EMPLEADORES CREADOS");
        title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        validatePermissions();
    }

    private void replaceFragment(EmployerListFragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void attachLayoutResource() {
        super.setChildLayout(R.layout.activity_employer_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (fragment != null) {
            fragment.updateList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MaterialMenuInflater
                .with(this)
                .setDefaultColor(Color.WHITE)
                .inflate(R.menu.menu_company_list, menu);
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
                Intent intent = new Intent(this, SortEmployerActivity.class);
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
        //Fragment fragment = mSectionsPagerAdapter.getPrimaryFragment();
        if (fragment != null) {
            fragment.syncData(!TextUtils.isEmpty(text) ? text : null);
        }
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
                    if (fragment != null) {
                        fragment.updateList();
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
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onListFragmentInteraction(ObjectList item, ImageView imageView) {
//        Bitmap bitmap;
//        byte[] byteArray = new byte[0];
//        try {
//            bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bStream);
//            byteArray = bStream.toByteArray();
//        } catch (Exception ignored) {
//        }
        Intent i = new Intent(this, DetailsEmployerActivity.class);
        i.putExtra(Constantes.ITEM_OBJ, item);
        i.putExtra("IsManaged",item.IsManaged);
        i.putExtra(Constantes.ITEM_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptionsCompat transitionActivityOptions =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this,
                        imageView,
                        ViewCompat.getTransitionName(imageView));
        startActivity(i, transitionActivityOptions.toBundle());

    }

    private void validatePermissions() {
        preferences = new MyPreferences(this);
        String profile = preferences.getProfile();
        User user = new Gson().fromJson(profile, User.class);
        if (user.IsAdmin && !user.IsSuperUser) {
            if (!user.claims.contains("employers.add")) fab.setVisibility(View.GONE);
        } else if (!multiAdmin && !user.IsSuperUser) {
            if (!user.Companies.get(0).Permissions.contains("employers.add"))
                fab.setVisibility(View.GONE);
        }
    }
}
