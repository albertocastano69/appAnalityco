package co.tecno.sersoluciones.analityco;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.com.sersoluciones.barcodedetectorser.BarcodeDecodeSerActivity;
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog;
import co.tecno.sersoluciones.analityco.createCar.CarsListActivity;
import co.tecno.sersoluciones.analityco.individualContract.SingleContract;
import co.tecno.sersoluciones.analityco.databases.DBHelper;
import co.tecno.sersoluciones.analityco.databases.InnovoDao;
import co.tecno.sersoluciones.analityco.fragments.DashBoardFragment;
import co.tecno.sersoluciones.analityco.models.CompanyList;
import co.tecno.sersoluciones.analityco.models.Expiry;
import co.tecno.sersoluciones.analityco.models.Notification;
import co.tecno.sersoluciones.analityco.models.User;
import co.tecno.sersoluciones.analityco.receivers.RequestBroadcastReceiver;
import co.tecno.sersoluciones.analityco.services.CRUDService;
import co.tecno.sersoluciones.analityco.services.UpdateDBService;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * Created by Ser Soluciones SAS on 01/07/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class MainActivity extends AppCompatActivity implements
        RequestBroadcastReceiver.BroadcastListener {

    private static final long PROFILE_SETTING = 10001;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.container)
    FrameLayout container;
    @BindView(R.id.progress)
    LinearLayout progress;
    private MyPreferences preferences;
    private Handler myHandler;
    private long delayTime;
    private boolean expiresTime = false;
    //private NavigationView navigationView;
    private RequestBroadcastReceiver requestBroadcastReceiver;
    private Drawer drawer;
    private String currentUsername;
    private Notification notificationModel;
    private int fragmentselect = 1;
    private LinkedHashMap<Uri, String> data;
    private boolean[] fillArray;
    private User user;
    private MenuItem syncButton;
    private ConnectivityManager connectivityManager;
    private boolean SynFirst = false;
    boolean IsLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        preferences = new MyPreferences(this);
        setSupportActionBar(toolbar);
        logW("estado de usuario " + preferences.isUserLogin());
        if (!preferences.isUserLogin()) {
            closeSession();
            return;
        }

        getTokenFirebase();
        String profile = preferences.getProfile();
        user = new Gson().fromJson(profile, User.class);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            IsLogin = extras.getBoolean("Login");
            if (extras.containsKey("username"))
                currentUsername = extras.getString("username");
        }
        try {
            configDrawer(toolbar);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        expiresTime = false;
        delayTime = preferences.isAccessTokenExpired();
        myHandler = new Handler();
        requestBroadcastReceiver = new RequestBroadcastReceiver(this);
        //checkForUpdates();
        requestNotification();

        data = new LinkedHashMap<>();
        fillArray = new boolean[4];

        if(IsLogin){
            VerificateInDb();
        }

        /*View header = navigationView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.textViewUsername)).setText(username);*/

        CRUDService.startRequest(MainActivity.this, Constantes.DASHBOARD_URL,
                Request.Method.GET);

//        new SyncDBBroadCast().setAlarm(this);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        registerConnectivityNetworkMonitor();
        AppCenter.start(getApplication(), "78371a9d-532b-473b-b87e-2e3de0b3d814",
                Analytics.class, Crashes.class);
//        Crashes.setEnabled(false);
        if (preferences.getVerifyAdnRegisterSettings(3)) {
            startActivity(new Intent(this, EnrollmentActivity.class));
        }

    }
    private void VerificateInDb(){
        DBHelper dbHelper = new DBHelper(MainActivity.this);
        SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(DBHelper.TABLE_NAME_CONTRACT,null,null,null,null,null,null);
        loadDataFromServer();
        /*if(cursor.getCount() == 0){
            log("Se sincroniza");
            SynFirst = true;

        }else{
            SynFirst = false;
        }*/
    }
    private void getTokenFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            if (instanceIdResult != null) {
                String newToken = instanceIdResult.getToken();
                Log.w("newToken", newToken);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        log("entra aca onNewIntent");
        try {
            handleIntent(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleIntent(Intent intent) throws JSONException {
        if (intent.hasExtra("activate_user")) {
            log("activate_user");
            boolean activateUser = intent.getBooleanExtra("activate_user", false);
            if (activateUser) {
                configDrawer(toolbar);
                finish();
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 7) {
            currentUsername = data.getStringExtra("username");
            Intent intent = getIntent();
            intent.putExtra("username", currentUsername);
            finish();
            startActivity(intent);
        }
    }
    private void configDrawer(Toolbar toolbar) throws JSONException {

        //logW(new Gson().toJson(user));

        String profilesStr = preferences.getProfiles();
        JSONArray jsonArrayProfile = new JSONArray(profilesStr);

        ArrayList<IProfile> iProfilesList = getProfiles(jsonArrayProfile);
//        final IProfile addAccount = new ProfileSettingDrawerItem().withName("Añadir Cuenta").withDescription("Añade una nueva cuenta Innovodata")
//                .withIcon(new IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add))
//                //GoogleMaterial.Icon.gmd_add)//.paddingDp(5)
//                //.colorRes(R.color.material_drawer_primary_text))
//                .withIdentifier(PROFILE_SETTING);


        final IProfile settingsAccount = new ProfileSettingDrawerItem().withName("Administrar Cuenta")
                .withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(100001);
        final IProfile logoutAccount = new ProfileSettingDrawerItem().withName("Cerrar Sesión")
                .withIcon(GoogleMaterial.Icon.gmd_exit_to_app).withIdentifier(100002);

//        iProfilesList.add(addAccount);
        iProfilesList.add(settingsAccount);
        iProfilesList.add(logoutAccount);
        IProfile[] iProfiles = iProfilesList.toArray(new IProfile[iProfilesList.size()]);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_image)
                .addProfiles(
                        iProfiles
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {

                        if (profile instanceof ProfileSettingDrawerItem) {
//                            if (profile.getIdentifier() == PROFILE_SETTING) {
//                                startActivityForResult(new Intent(MainActivity.this, AddSesionActivity.class), 9);
//                            } else
                            if (profile.getIdentifier() == 100002) {
                                confirmClose(currentUsername);
                            } else {
                                int[] startingLocation = new int[2];
                                DisplayMetrics metrics = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                int widthPixels = metrics.widthPixels;
                                //int heightPixels = metrics.heightPixels;
                                startingLocation[0] += widthPixels / 2;
                                UserProfileActivity.startUserProfileFromLocation(startingLocation, MainActivity.this);
                            }
                        } else {
                            currentUsername = profile.getEmail().getText().toString();
                            String jwt = preferences.getCurrentJWT(currentUsername);
                            preferences.setJWT(jwt);
                            CRUDService.startRequest(MainActivity.this, Constantes.USER_INFO_URL, Request.Method.GET);
                        }
                        return false;
                    }
                })
                .build();
        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem navMain = new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_home)
                .withIdentifier(1).withName(R.string.str_nav_main);

        PrimaryDrawerItem navScan = new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_camera)
                .withIdentifier(2).withName(R.string.scanear);
        Drawable drawableAdmin = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.BANK) // provide an icon
                .setColor(Color.GRAY) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        PrimaryDrawerItem navCustomer = new PrimaryDrawerItem().withIcon(drawableAdmin)
                .withIdentifier(3).withName(R.string.customer);


        Drawable drawable1 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.CITY) // provide an icon
                .setColor(Color.GRAY) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        PrimaryDrawerItem navProject = new PrimaryDrawerItem().withIcon(drawable1)
                .withIdentifier(4).withName(R.string.projects);
        PrimaryDrawerItem navEmployer = new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_business)
                .withIdentifier(5).withName(R.string.empleadores);
        PrimaryDrawerItem navContract = new PrimaryDrawerItem().withIcon(R.drawable.ic_icon_contract)
                .withIdentifier(6).withName(R.string.contratos);
        Drawable drawable2 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.BARCODE_SCAN) // provide an icon
                .setColor(Color.GRAY) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        PrimaryDrawerItem navEnrollment = new PrimaryDrawerItem().withIcon(drawable2)
                .withIdentifier(9).withName(R.string.scanearCheck);
        Drawable drawable3 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT) // provide an icon
                .setColor(Color.GRAY) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        PrimaryDrawerItem navPersonal = new PrimaryDrawerItem().withIcon(drawable3)
                .withIdentifier(12).withName(R.string.personal_menu);
        PrimaryDrawerItem navContractSingle = new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_folder_shared)
                .withIdentifier(17).withName(R.string.contratosindividual);
        PrimaryDrawerItem navCar = new PrimaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_directions_car)
                .withIdentifier(18).withName(R.string.vehiculos);
        PrimaryDrawerItem navAdmin = new PrimaryDrawerItem().withIcon(drawableAdmin)
                .withIdentifier(13).withName(R.string.admin_company);
        Drawable drawable4 = MaterialDrawableBuilder.with(this) // provide a context
                .setIcon(MaterialDrawableBuilder.IconValue.COMMENT) // provide an icon
                .setColor(Color.GRAY) // set the icon color
                .setToActionbarSize() // set the icon size
                .build();
        PrimaryDrawerItem navTest = new PrimaryDrawerItem().withIcon(drawable3)
                .withIdentifier(16).withName("DashWeb");
       /* PrimaryDrawerItem notification = new PrimaryDrawerItem().withIcon(drawable4)
                .withIdentifier(14).withName(R.string.notification);*/
        ExpandableDrawerItem expandableDrawerItemPersonal = null;
        logW("Prueba"+notificationModel);
        if (notificationModel != null) {
            if (notificationModel.Count > 0) {
               /* if(notificationModel.CountCompanies>0){
                    new SecondaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_people)
                            .withIdentifier(16).withName(R.string.personalCreate);
                }*/
                int countNotification = 0;
                if (notificationModel.CountProjects > 0) countNotification++;
                if (notificationModel.CountEmployers > 0) countNotification++;
                if (notificationModel.CountContracts > 0) countNotification++;
                if (notificationModel.CountPersonal > 0) countNotification++;

                IDrawerItem[] itemsIDrawerItem = new IDrawerItem[countNotification];
                countNotification = 0;

                if (notificationModel.CountProjects > 0) {
                    itemsIDrawerItem[countNotification] = new SecondaryDrawerItem().withIcon(drawable1)
                            .withIdentifier(11).withName(R.string.projects).withBadge(notificationModel.CountProjects + "").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary_dark));
                    countNotification++;
                }
                if (notificationModel.CountEmployers > 0) {
                    itemsIDrawerItem[countNotification] = new SecondaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_business)
                            .withIdentifier(14).withName(R.string.empleadores).withBadge(notificationModel.CountEmployers + "").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary_dark));
                    countNotification++;
                }
                if (notificationModel.CountContracts > 0) {
                    itemsIDrawerItem[countNotification] = new SecondaryDrawerItem().withIcon(R.drawable.ic_icon_contract)
                            .withIdentifier(15).withName(R.string.contratos).withBadge(notificationModel.CountContracts + "").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary_dark));
                    countNotification++;
                }
                if (notificationModel.CountPersonal > 0) {
                    itemsIDrawerItem[countNotification] = new SecondaryDrawerItem().withIcon(drawable3)
                            .withIdentifier(10).withName(R.string.personal_menu).withBadge(notificationModel.CountPersonal + "").withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.primary_dark));
                    countNotification++;
                }
                expandableDrawerItemPersonal =
                        new ExpandableDrawerItem().withName(R.string.notification).withIcon(drawable4).withIdentifier(19).withSelectable(false).withSubItems(itemsIDrawerItem).withDescription(notificationModel.Count + "");
            }
        }

        SecondaryDrawerItem navSetting = new SecondaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_settings)
                .withIdentifier(7).withName(R.string.str_settings);
        /*SecondaryDrawerItem navExit = new SecondaryDrawerItem().withIcon(GoogleMaterial.Icon.gmd_exit_to_app)
                .withIdentifier(8).withName(R.string.str_exit);*/

        ArrayList<IDrawerItem> iDrawerItems = new ArrayList<>();
        iDrawerItems.add(navMain);

        if (user.email_verified) {

            addNotification();
            boolean enrollmentsView = false;
            boolean employersView = false;
            boolean projectsView = false;
            boolean contractsView = false;
            boolean personalsView = false;
            boolean notificationView = false;
            boolean IndividualContractView = false;
            //boolean carView = false;

            if (!user.IsAdmin && !user.IsSuperUser) {
                for (CompanyList c : user.Companies) {
                    if (c.Permissions != null) {
                        if (c.Permissions.contains("enrollments.view")) enrollmentsView = true;
                        if (c.Permissions.contains("employers.view")) employersView = true;
                        if (c.Permissions.contains("projects.view")) projectsView = true;
                        if (c.Permissions.contains("contracts.view")) contractsView = true;
                        if (c.Permissions.contains("personals.view")) personalsView = true;
                        if (c.Permissions.contains("notifications.view")) notificationView = true;
                        if (c.Permissions.contains("contracting.view")) IndividualContractView = true;
                    }

                }
            }

            //iDrawerItems.add(navScan);
            iDrawerItems.add(navEnrollment);
            if (user.IsSuperUser) {
                iDrawerItems.add(navCustomer);
            }
            //iDrawerItems.add(navScan);
            if (user.claims.contains("enrollments.view") || user.IsSuperUser || (!user.IsAdmin && enrollmentsView)) {
                iDrawerItems.add(navEnrollment);
            }
            if (user.IsAdmin) {
                iDrawerItems.add(navAdmin);
            }
            if (user.IsSuperUser) {
                iDrawerItems.add(navCustomer);
            }
            if (user.claims.contains("employers.view") || user.IsSuperUser || (!user.IsAdmin && employersView)) {
                iDrawerItems.add(navEmployer);
            }
            if (user.claims.contains("projects.view") || user.IsSuperUser || (!user.IsAdmin && projectsView))
                iDrawerItems.add(navProject);


            if (user.claims.contains("contracts.view") || user.IsSuperUser || (!user.IsAdmin && contractsView)) {
                iDrawerItems.add(navContract);
            }
            if (user.claims.contains("personals.view") || user.IsSuperUser || (!user.IsAdmin && personalsView)){
                iDrawerItems.add(navPersonal);

            }

            if(user.claims.contains("contracting.view") || user.IsSuperUser || (!user.IsAdmin && IndividualContractView)){
                iDrawerItems.add(navContractSingle);
                iDrawerItems.add(navCar);
            }
            // iDrawerItems.add(notification);
            if (expandableDrawerItemPersonal != null && (user.claims.contains("notifications.view") || (!user.IsAdmin && notificationView)))
                iDrawerItems.add(expandableDrawerItemPersonal);

            // iDrawerItems.add(navTest);
        }
        if (!user.email_verified) {
            navScan.withEnabled(false);
            navCustomer.withEnabled(false);
            navProject.withEnabled(false);
            navEmployer.withEnabled(false);
            navContract.withEnabled(false);
            navEnrollment.withEnabled(false);
            navAdmin.withEnabled(false);
            navContractSingle.withEnabled(false);
            navCar.withEnabled(false);
        }
        iDrawerItems.add(new DividerDrawerItem());
        iDrawerItems.add(navSetting);
        //iDrawerItems.add(navExit);

        IDrawerItem[] drawerItems = iDrawerItems.toArray(new IDrawerItem[iDrawerItems.size()]);
        //create the drawer and remember the `Drawer` result object
        // do something with the clicked item :D
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .addDrawerItems(
                        drawerItems
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        navBarClick(drawerItem.getIdentifier());
                        return false;
                    }
                })
                .build();
//        drawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Powered By SerSoluciones"));
        //set the selection to the item with the identifier 1
        drawer.setSelection(1);

        if (currentUsername != null) {
            headerResult.setActiveProfile(getProfile(currentUsername));
        }

    }

    private void addNotification() {

        if (!preferences.getNotification()) {
            if (notificationModel != null) {
                if (notificationModel.Count > 0) {
                    preferences.setNotification(true);
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    if (notificationModel.CountProjects > 0)
                        inboxStyle.addLine("Projectos: " + notificationModel.CountProjects);
                    if (notificationModel.CountEmployers > 0)
                        inboxStyle.addLine("Empleadores: " + notificationModel.CountEmployers);
                    if (notificationModel.CountContracts > 0)
                        inboxStyle.addLine("Contratos: " + notificationModel.CountContracts);
                    if (notificationModel.CountPersonal > 0)
                        inboxStyle.addLine("Personal: " + notificationModel.CountPersonal);

                    long[] v = {500, 1000};
                    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    NotificationCompat.Builder builder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.mipmap.ic_launcher_round)
                                    .setContentTitle("Notificaciones")
                                    .setContentText("Total notificaciones: " + notificationModel.Count)
                                    .setVibrate(v)
                                    .setSound(uri)
                                    .setStyle(inboxStyle);

                    Intent notificationIntent = new Intent(this, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);
//                    builder.setContentIntent(contentIntent);

                    // Add as notification
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(0, builder.build());
                }
            }
        }
    }
    /*@Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString("username", currentUsername);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentUsername = savedInstanceState.getString("username");
    }*/

    private void navBarClick(long position) {
        switch ((int) position) {
            case 1:
                if (fragmentselect == 1) {
                    replaceFragment(DashBoardFragment.newInstance());
                    fragmentselect = 0;
                }
                break;
            case 2:
                fragmentselect = 1;
                //replaceFragment(ScanFragment.newInstance());
                break;
            case 3:
                openActivity(CompanyListActivity.class);
                break;
            case 4:
                openActivity(ProjectsListActivity.class);
                break;
            case 5:
                openActivity(EmployersListActivity.class);
                break;
            case 6:
                openActivity(ContractsListActivity.class);
                break;
            case 7:
                dialogConfirmCredentials();
                break;
            case 8:

                break;
            case 9:
                openActivity(EnrollmentActivity.class);
                break;
            case 10:
                Intent intent = new Intent(this, NotificationList.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("Notifiation", new Gson().toJson(notificationModel));
                intent.putExtra("item", 1);
                startActivity(intent);
                break;
            case 11:
                Intent intent1 = new Intent(this, NotificationList.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent1.putExtra("Notifiation", new Gson().toJson(notificationModel));
                intent1.putExtra("item", 2);
                startActivity(intent1);
                break;
            case 12:
                Intent intent4 = new Intent(this, PersonalListActivity.class);
                intent4.putExtra("Notifiation", new Gson().toJson(notificationModel));
                startActivity(intent4);

//                openActivity(PersonalListActivity.class);
                break;
            case 13:
                if (user.Companies != null) {
                    Intent i = new Intent(this, DetailsCompanyActivityTabs.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
                  /*  if (user.Companies.get(0).CompanyInfoId == null)
                        i.putExtra("id", user.CompanyId);
                    else
                        i.putExtra("id", user.Companies.get(0).CompanyInfoId);*/
                    i.putExtra("id", user.CompanyId);
                    startActivity(i);
                }
                break;
            case 14:
                Intent intent2 = new Intent(this, NotificationList.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent2.putExtra("Notifiation", new Gson().toJson(notificationModel));
                intent2.putExtra("item", 3);
                startActivity(intent2);
                break;
            case 15:
                Intent intent3 = new Intent(this, NotificationList.class);
                intent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP & Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent3.putExtra("Notifiation", new Gson().toJson(notificationModel));
                intent3.putExtra("item", 4);
                startActivity(intent3);
                break;
            case 16:
                openActivity(BarcodeDecodeSerActivity.class);
                break;
            case 17:
                openActivity(SingleContract.class);
                break;

            case 18:
                openActivity(CarsListActivity.class);
                break;
        }
    }

    private ArrayList<IProfile> getProfiles(JSONArray jsonArray) throws JSONException {
        int identifier = 100;
        ArrayList<IProfile> iProfiles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonProfile = jsonArray.getJSONObject(i);

            String imagePathAvatar = jsonProfile.getString(Constantes.KEY_IMAGE_AVATAR_PATH);
            Drawable iconProfileDrawer = ContextCompat.getDrawable(MainActivity.this, R.drawable.profile_dummy);
            if (!imagePathAvatar.isEmpty()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(imagePathAvatar, options);
                iconProfileDrawer = new BitmapDrawable(getResources(), bitmap);
            }

            String nameUser = jsonProfile.getString("Name") + " " + jsonProfile.getString("LastName");

            IProfile drawerItem = new ProfileDrawerItem().withName(nameUser)
                    .withEmail(jsonProfile.getString("username"))
                    .withIcon(iconProfileDrawer).withIdentifier(identifier + i);
            iProfiles.add(drawerItem);
        }
        return iProfiles;
    }

    private int getProfile(String username) throws JSONException {
        JSONArray jsonArray = new JSONArray(preferences.getProfiles());
        int identifier = 100;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonProfile = jsonArray.getJSONObject(i);
            if (username.equals(jsonProfile.getString("username"))) {
                logE("iguales " + username + ", identifier:" + (identifier + i));
                return identifier + i;
            }
        }
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!preferences.isUserLogin()) return;
        //navigationView.setCheckedItem(R.id.nav_home);
        drawer.setSelection(1);
        //checkForCrashes();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CRUDService.ACTION_REQUEST_GET);
        intentFilter.addAction(CRUDService.ACTION_REQUEST_SAVE);
        LocalBroadcastManager.getInstance(this).registerReceiver(requestBroadcastReceiver,
                intentFilter);
        onDetectInternet(isOnline(this));
        preferences.setIsonline(isOnline(this));

    }

    @Override
    protected void onPause() {
        super.onPause();
        log("ON PAUSE");
        onDetectInternet(isOnline(this));
        preferences.setIsonline(isOnline(this));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(requestBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } //else {
        //super.onBackPressed();
        //}*/

        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, DetailActivity.class));
//            replaceFragment(new InfoFragment());
            return true;
        } else if (id == R.id.Sync) {
            syncButton = item;
            item.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            UpdateDBService.startRequest(this, false);
            //loadDataFromServer();
            replaceFragment(DashBoardFragment.newInstance());
            item.setIcon(R.drawable.ic_baseline_sync_disabled_24);
            progress.setVisibility(View.GONE);
            if(!SynFirst)Toast.makeText(MainActivity.this, "Sincronización de datos exitosa", Toast.LENGTH_SHORT).show();
            myHandler.postDelayed(enabledSyncButton, 60000);
            myHandler.postDelayed(Buttonsyn, 60000);
        }
        return super.onOptionsItemSelected(item);
    }

private void loadDataFromServer() {
//        CRUDService.startRequest(this, Constantes.USER_INFO_URL, Request.Method.GET);

        data = new LinkedHashMap<>();
        data.put(Constantes.CONTENT_CONTRACT_URI, Constantes.LIST_CONTRACTS_URL);
        data.put(Constantes.CONTENT_PERSONAL_URI, Constantes.LIST_PERSONAL_URL);
        data.put(Constantes.CONTENT_PROJECT_URI, Constantes.LIST_PROJECTS_URL);
//        data.put(Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI, Constantes.LIST_CONTRACT_PER_OFFLINE_URL);
        data.put(Constantes.CONTENT_EMPLOYER_URI, Constantes.LIST_EMPLOYERS_URL);
        fillArray = new boolean[data.size()];
        launchRequests();
    }

    private void launchRequests() {
         int pos = 0;
         for (Object o : data.entrySet()) {
             Map.Entry pair = (Map.Entry) o;
             ContentValues values = new ContentValues();
             values.put(Constantes.KEY_SELECT, true);
             if (pair.getValue().equals(Constantes.LIST_CONTRACT_PER_OFFLINE_URL))
                 values.put("project", true);
             else
                 values.put(Constantes.KEY_SELECT, true);
             String paramsQuery = HttpRequest.makeParamsInUrl(values);
             CRUDService.startRequest(this, (String) pair.getValue(),
                     Request.Method.GET, paramsQuery, true);
             DebugLog.logW(pos + ". URL to Request: " + pair.getValue() + ": " + fillArray[pos]);
             pos++;
             //it.remove();
         }
     }
    private void requestNotification() {
        ContentValues values = new ContentValues();
        values.put("company", true);
        values.put("project", true);
        values.put("contract", true);
        values.put("employer", true);
        values.put("personal", true);
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        CRUDService.startRequest(MainActivity.this, Constantes.LIST_DASHBOARD_NOTIFICATION_URL,
                Request.Method.GET, paramsQuery, false);
    }

    private void NotificationMenu(String notification) {

        notificationModel = new Gson().fromJson(notification,
                new TypeToken<Notification>() {
                }.getType());
        try {
            configDrawer(toolbar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo para mostrar una ventana emergente de confirmacion y cerrar sesion
     */
    private void confirmClose(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage("¿Esta seguro que desea cerrar la sesión?")
                .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            JSONArray jsonArray = new JSONArray(preferences.getProfiles());
                            if (jsonArray.length() > 1) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonProfile = jsonArray.getJSONObject(i);
                                    if (username.equals(jsonProfile.getString("username"))) {
                                        jsonArray.remove(i);
                                        preferences.removeCurrentJWT(username);
                                        break;
                                    }
                                }
                                preferences.setProfiles(jsonArray.toString());
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                currentUsername = jsonObject.getString("username");
                                String jwt = preferences.getCurrentJWT(currentUsername);
                                preferences.setJWT(jwt);

                                CRUDService.startRequest(MainActivity.this, Constantes.USER_INFO_URL, Request.Method.GET);

                            } else {
                                preferences.cleanJWT();
                                Toast.makeText(MainActivity.this, "La sesion ha expirado.", Toast.LENGTH_LONG).show();
                                closeSession();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Metodo para cerrar la actividad y volver a la actividad de Login
     */
    private void closeSession() {
        InnovoDao innovoDao=new InnovoDao(MainActivity.this);
        if (expiresTime) return;
        expiresTime = true;
        logE("cierre de sesion por vencimiento de Token");
        preferences.setNotification(false);
        preferences.setUserLogin(false);
        preferences.cleanJWT();
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        innovoDao.DeleteTableProjectSyn();
//        intent.putExtra("viewAnimation", false);
        startActivity(intent);
        finish();

    }

    private void replaceFragment(final Fragment fragment) {
        new Handler().post(new Runnable() {
            public void run() {
                try {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.container, fragment);
                    fragmentTransaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Metodo para validar y cerrar la aplicacion
     */
    private void dialogConfirmCredentials() {

        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!preferences.isUserLogin()) closeSession();
    }

    /**
     * Metodo que se lanza cuando se sale de la actividad por tanto se paran los
     * contadores que tiene en cuenta el tiempo de expiracion del token
     */
    public void onStop() {
        super.onStop();
        myHandler.removeCallbacks(closeControls);
    }

    /**
     * Metodo que se lanza cuando la actividad se destruye por completo
     */
    @Override
    protected void onDestroy() {
        if (connectivityManager != null)
            connectivityManager.unregisterNetworkCallback(networkCallback);
        logE("ON DESTROY MAIN ACTIVITY");
        super.onDestroy();
        //unregisterManagers();
    }

    /**
     * Metodo para refrescar el token si existe interaccion con la aplicacion
     */
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (!expiresTime)
            timeToken();
    }

    /**
     * Metodo para refrescar la sesion y reiniciar contadores
     * realiza peticion tipo POST al servidor y obtiene de nuevo el Token
     */
    private void timeToken() {
        delayTime = preferences.isAccessTokenExpired();
        myHandler.removeCallbacks(closeControls);
        myHandler.postDelayed(closeControls, delayTime);
        //int expiresInToken = preferences.getExpiresInToken();

        long millisecondsRemain = delayTime;
        //log("millisecondsRemain: " + millisecondsRemain + ", secondsRemain: "
        //        + millisecondsRemain/1000 +", expiresInToken: " + expiresInToken);
        if (millisecondsRemain <= 0) {
            Toast.makeText(MainActivity.this, "La sesion ha expirado.", Toast.LENGTH_LONG).show();
            closeSession();
        }
    }

    /**
     * Variable que tiene en cuenta el tiempo de expiracion del token
     */
    private final Runnable closeControls = new Runnable() {
        public void run() {
            logE("tiempo expirado");
            closeSession();
        }
    };


    @Override
    public void onStringResult(String action, int option, String jsonObjStr, String url) {
        switch (option) {
            case Constantes.SUCCESS_REQUEST:

                switch (url) {
                    case Constantes.LIST_DASHBOARD_NOTIFICATION_URL:

                        NotificationMenu(jsonObjStr);
                        break;
                    case Constantes.USER_INFO_URL:
                        processProfile(jsonObjStr);
                        break;
                  case Constantes.LIST_CONTRACTS_URL:
                        DebugLog.log("tabla llena LIST_CONTRACTS_URL");
                        fillArray[0] = true;
                        break;
                    case Constantes.LIST_PERSONAL_URL:
                        DebugLog.log("tabla llena LIST_PERSONAL_URL");
                        fillArray[1] = true;
                        break;
                    case Constantes.LIST_PROJECTS_URL:
                        DebugLog.log("tabla llena LIST_PROJECTS_URL");
                        fillArray[2] = true;
                        break;
                /*    case Constantes.LIST_CONTRACT_PER_OFFLINE_URL:
                        DebugLog.log("tabla llena LIST_CONTRACT_PER_OFFLINE_URL");
//                        fillArray[3] = true;
                        break;*/
                    case Constantes.LIST_EMPLOYERS_URL:
                        DebugLog.log("tabla llena LIST_EMPLOYERS_URL");
                        fillArray[3] = true;
                        break;
                }
                if (!url.equals(Constantes.LIST_DASHBOARD_NOTIFICATION_URL) && !url.equals(Constantes.USER_INFO_URL))
                    verifyFillData();
                SynFirst = false;
                break;
            case Constantes.NOT_INTERNET:
            case Constantes.BAD_REQUEST:
            case Constantes.TIME_OUT_REQUEST:
            case Constantes.NOT_CONNECTION:
                break;
            case Constantes.UNAUTHORIZED:
                closeSession();
                break;
        }
    }

    private void processProfile(String jsonObjStr) {
        preferences.setProfile(jsonObjStr);

        try {
            JSONObject jsonObject = new JSONObject(jsonObjStr);
            jsonObject.getString("Expiry");
            Expiry expiry = new Gson().fromJson(jsonObject.getString("Expiry"), Expiry.class);
//            ArrayList<String> listPer
            preferences.setCompanyDays(expiry.Company);
            preferences.setProjectDays(expiry.Project);
            preferences.setEmployerDays(expiry.Employer);
            preferences.setContractDays(expiry.Contract);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        DBHelper dbHelper = new DBHelper(MainActivity.this);
//        dbHelper.deleteAllContractPerson();
//        dbHelper.deleteAllContract();
//        dbHelper.deleteAllCompany();
//        dbHelper.deleteAllProject();
//        dbHelper.deleteAllEmployer();
//        dbHelper.deleteAllPersonal();
//        preferences.setSelectedProjectId("");

        Intent intent = getIntent();
        intent.putExtra("username", currentUsername);
        finish();
        startActivity(intent);
    }

    private void verifyFillData() {
        logW("entra verifyFillData()");
        SynFirst = true;
        boolean flag = true;
        for (boolean item : fillArray) {
            if (!item) {
                flag = false;
                break;
            }
        }
        if (flag) {
            myHandler.removeCallbacks(enabledSyncButton);
            if (syncButton != null) syncButton.setEnabled(true);
//            preferences.setSyncDate(Calendar.getInstance().getTime().getTime());
            if(SynFirst){
                Toast.makeText(MainActivity.this, "Sincronización de datos exitosa", Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Check if network available or not
     *
     * @param context context of de activity
     */
    private boolean isOnline(Context context) {
        boolean isOnline = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            isOnline = netInfo != null && netInfo.isConnected();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return isOnline;
    }

    /**
     *
     */
    private void registerConnectivityNetworkMonitor() {

        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        connectivityManager.registerNetworkCallback(
                builder.build(), networkCallback);
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

        @Override
        public void onAvailable(@NotNull Network network) {
            logW("connection network available");
            onDetectInternet(true);
            preferences.setIsonline(true);
            replaceFragment(new DashBoardFragment());
        }
        @Override
        public void onLost(@NotNull Network network) {
            logE("connection network lost");
            preferences.setIsonline(false);
            onDetectInternet(false);
        }
    };

    public void onDetectInternet(final boolean online) {

        new Handler(Looper.getMainLooper()).post(() -> findViewById(R.id.textViewOnline).setVisibility(online ? View.GONE : View.VISIBLE));
    }

    private Runnable enabledSyncButton = () -> syncButton.setEnabled(true);
    private Runnable Buttonsyn = () -> syncButton.setIcon(R.drawable.ic_baseline_sync_24);

    private void openActivity(Class<? extends Activity> ActivityToOpen) {
        Intent intent = new Intent(MainActivity.this, ActivityToOpen);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
