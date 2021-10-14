package co.tecno.sersoluciones.analityco.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import co.tecno.sersoluciones.analityco.BuildConfig;
import co.tecno.sersoluciones.analityco.tasks.AsyncUpdateDBTask;
import co.tecno.sersoluciones.analityco.ui.createPersonal.PersonalFragment;
import co.tecno.sersoluciones.analityco.utilities.ConnectionDetector;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.MySingleton;
import co.tecno.sersoluciones.analityco.utilities.VolleyMultipartRequest;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CRUDService extends IntentService {
    private static final String ACTION_CU = "com.sersoluciones.apps.services.action.CU";
    private static final String ACTION_READ = "com.sersoluciones.apps.services.action.READ";

    private static final String EXTRA_URL = "com.sersoluciones.apps.services.extra.URL";
    private static final String EXTRA_METHOD = "com.sersoluciones.apps.services.extra.METHOD";
    private static final String EXTRA_JSON_OBJECT = "com.sersoluciones.apps.services.extra.JSON_OBJECT";
    private static final String EXTRA_SAVE = "com.sersoluciones.apps.services.extra.SAVE";
    private static final String EXTRA_PARAM_QUERY = "com.sersoluciones.apps.services.extra.PARAM_QUERY";
    private static final String EXTRA_DATA = "com.sersoluciones.apps.services.extra.DATA";
    private static final String EXTRA_IDS = "com.sersoluciones.apps.services.extra.IDS";
    private static final String EXTRA_JSON_GET = "com.sersoluciones.apps.services.extra.JSON_GET";
    private static final String EXTRA_EMPLOYERIDS = "com.sersoluciones.apps.services.extra.EMPLOYERIDS";

    public static final String ACTION_REQUEST_FORM_DATA = "com.sersoluciones.apps.services.action.REQUEST_FORM_DATA";
    public static final String ACTION_REQUEST_POST = "com.sersoluciones.apps.services.action.REQUEST_POST";
    public static final String ACTION_REQUEST_PUT = "com.sersoluciones.apps.services.action.REQUEST_PUT";
    public static final String ACTION_REQUEST_DELETE = "com.sersoluciones.apps.services.action.REQUEST_DELETE";
    public static final String ACTION_REQUEST_GET = "com.sersoluciones.apps.services.action.REQUEST_GET";
    public static final String ACTION_REQUEST_SAVE = "com.sersoluciones.apps.services.action.REQUEST_GET_SAVE";

    private static final Map<Integer, String> TYPE_ACTION_REQUEST;
    private int ChechSum;
    static {
        @SuppressLint("UseSparseArrays")
        Map<Integer, String> tmp = new HashMap<>();
        tmp.put(Request.Method.GET, ACTION_REQUEST_GET);
        tmp.put(Request.Method.POST, ACTION_REQUEST_POST);
        tmp.put(Request.Method.PUT, ACTION_REQUEST_PUT);
        tmp.put(Request.Method.DELETE, ACTION_REQUEST_DELETE);
        TYPE_ACTION_REQUEST = Collections.unmodifiableMap(tmp);
    }

    private MyPreferences preferences;
    public CRUDService() {
        super("CRUDIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startRequest(Context context, String url, int method, String jsonObject) {
        Intent intent = new Intent(context, CRUDService.class);
        intent.setAction(ACTION_CU);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_JSON_OBJECT, jsonObject);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
        context.startService(intent);
//        }
    }

    public static void startRequest(Context context, String url, int method) {
        Intent intent = new Intent(context, CRUDService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
        context.startService(intent);
//        }
    }

    public static void startRequest(Context context, String url, int method, String paramQuery, boolean save) {
        Intent intent = new Intent(context, CRUDService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_PARAM_QUERY, paramQuery);
        intent.putExtra(EXTRA_SAVE, save);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
        context.startService(intent);
//        }
    }
    public static void startRequest(Context context, String url, int method, String paramQuery, boolean save,String requestbyIds) {
        Intent intent = new Intent(context, CRUDService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_PARAM_QUERY, paramQuery);
        intent.putExtra(EXTRA_SAVE, save);
        intent.putExtra(EXTRA_IDS,requestbyIds);
        context.startService(intent);
    }
    public static void startRequest(Context context, String url, int method,boolean save,String requestbyIds,boolean json) {
        Intent intent = new Intent(context, CRUDService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_JSON_OBJECT, requestbyIds);
        intent.putExtra(EXTRA_SAVE, save);
        intent.putExtra(EXTRA_IDS,requestbyIds);
        intent.putExtra(EXTRA_JSON_GET,json);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        launchService();
        preferences = new MyPreferences(this);
    }

    private void launchService() {
        String channelId = "CRUDService";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(103, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel("CRUDService",
                "CRUD Service", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return "CRUDService";
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            ConnectionDetector connectionDetector = new ConnectionDetector(this);
            boolean conexionServer = connectionDetector.isConnectedToServer();
            final String action = intent.getAction();
            if (ACTION_CU.equals(action)) {
                try {
                    final String url = intent.getStringExtra(EXTRA_URL);
                    final int method = intent.getIntExtra(EXTRA_METHOD, 0);
                    final JSONObject jsonObject = new JSONObject((intent.getStringExtra(EXTRA_JSON_OBJECT)));

                    if (!conexionServer) {
                        processFinish(Constantes.NOT_INTERNET, null, TYPE_ACTION_REQUEST.get(method), url);
                        return;
                    }
                    makeRequest(url, method, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (ACTION_READ.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final int method = intent.getIntExtra(EXTRA_METHOD, 0);
                final boolean save = intent.getBooleanExtra(EXTRA_SAVE, false);
                final String paramQuery = intent.getStringExtra(EXTRA_PARAM_QUERY);
                final String requestbyids=intent.getStringExtra(EXTRA_IDS);
                final boolean jsonRquest = intent.getBooleanExtra(EXTRA_JSON_GET,false);
                if (!conexionServer) {
                    logE("error no internet");
                    processFinish(Constantes.NOT_INTERNET, null, save ? ACTION_REQUEST_SAVE : TYPE_ACTION_REQUEST.get(method), url);
                    return;
                }
                if(requestbyids!=null){
                    if(jsonRquest){
                        System.out.println("Ids"+intent.getStringExtra(EXTRA_JSON_OBJECT));
                        String idsPersonal = intent.getStringExtra(EXTRA_JSON_OBJECT);
                        String[] vectorIds = idsPersonal.split(",");
                        JSONObject object = new JSONObject();
                        try {
                            JSONArray json = new JSONArray(vectorIds);
                            object.put("ids",json);
                            makeRequest(url,method,save,object,requestbyids);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        makeRequest(url,method,save,paramQuery,requestbyids);
                    }
                }else if(requestbyids == null){
                    makeRequest(url, method, save, paramQuery);
                }
            } else if (ACTION_REQUEST_FORM_DATA.equals(action)) {
                String url = intent.getStringExtra(EXTRA_URL);
                int method = intent.getIntExtra(EXTRA_METHOD, 0);
                HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_DATA);
                String paramQuery = intent.getStringExtra(EXTRA_PARAM_QUERY);
                jsonMultiPartRequest(url, method, hashMap, paramQuery);
            }
        }
    }
    private void makeRequest(final String url, final int method, final boolean save, final JSONObject jsonRequest,final  String Requestbyids) {
        String uri = preferences.getUrlServer() + url;
        final boolean mSave = save;
        log("URLJSonSend: " + uri + ", save: " + mSave+ "objent: "+ jsonRequest);
        StringRequest stringRequest = new StringRequest(method, uri,
                response -> {
                    if (mSave) {
                        new AsyncUpdateDBTask(CRUDService.this).execute(url, response,Requestbyids);
                    } else {
                        processFinish(Constantes.SUCCESS_REQUEST, response, TYPE_ACTION_REQUEST.get(method), url);
                    }
                }, error -> validateErrors(error, url, method)) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + preferences.getToken());
                map.put("VersionApplication", BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE);
                return map;
            }
            @Override
            public String getBodyContentType() {
                return "application/json";
            }
            @Override
            public byte[] getBody() {
                return jsonRequest.toString().getBytes();
            }
        };
        int socketTimeout = 30000;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }
    private void makeRequest(final String url, final int method, final boolean save, final String paramsQuery,final  String Requestbyids) {
        String uri = preferences.getUrlServer() + url;
        final boolean mSave = save;
        if (paramsQuery != null && !paramsQuery.isEmpty()) {
            uri += paramsQuery;
        }
        log("URL: " + uri + ", save: " + mSave);
        StringRequest stringRequest = new StringRequest(method, uri,
                response -> {
                    if (mSave) {
                        new AsyncUpdateDBTask(CRUDService.this).execute(url, response,Requestbyids);
                    } else {
                        processFinish(Constantes.SUCCESS_REQUEST, response, TYPE_ACTION_REQUEST.get(method), url);
                    }
                }, error -> validateErrors(error, url, method)) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + preferences.getToken());
                map.put("VersionApplication", BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE);
                return map;
            }
        };
        int socketTimeout = 30000;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    private void makeRequest(final String url, final int method, final boolean save, final String paramsQuery) {
        String uri = preferences.getUrlServer() + url;
        final boolean mSave = save;
        if (paramsQuery != null && !paramsQuery.isEmpty()) {
            uri += paramsQuery;
        }

        log("URL: " + uri + ", save: " + mSave);
        StringRequest stringRequest = new StringRequest(method, uri,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (mSave) {
                            new AsyncUpdateDBTask(CRUDService.this).execute(url, response,null);
                        } else {
                            processFinish(Constantes.SUCCESS_REQUEST, response, TYPE_ACTION_REQUEST.get(method), url);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                validateErrors(error, url, method);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + preferences.getToken());
                map.put("VersionApplication", BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE);
                return map;
            }
        };
        int socketTimeout = 30000;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }

    private void makeRequest(final String url, final int method, JSONObject jsonObject) {
        String uri = preferences.getUrlServer() + url;
        log("URL: " + uri);
        JsonObjectRequest stringRequest = new JsonObjectRequest(method, uri, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        processFinish(Constantes.SUCCESS_REQUEST, response.toString(), TYPE_ACTION_REQUEST.get(method), url);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                validateErrors(error, url, method);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + preferences.getToken());
                map.put("VersionApplication", BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE);
                return map;
            }
        };
        int socketTimeout = 30000;
        DefaultRetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void jsonMultiPartRequest(final String uri, final int method, final Map<String, String> params, String paramsQuery) {
        String url = preferences.getUrlServer() + uri;
        if (paramsQuery != null && !paramsQuery.isEmpty()) {
            url += paramsQuery;
        }
        logW("url: " + url);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(method, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        processFinish(Constantes.SUCCESS_FILE_UPLOAD, resultResponse, ACTION_REQUEST_FORM_DATA, uri);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                validateErrors(error, uri, method);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + preferences.getToken());
                map.put("VersionApplication", BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, VolleyMultipartRequest.DataPart> paramsImage = new HashMap<>();
                for (String key : params.keySet()) {
                    switch (key) {
                        case "file":
                        case "picture":
                        case "image":
                        case "document_support":
                            Uri imageUri = Uri.parse(params.get(key));
                            String readOnlyMode = "r";
                            try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, readOnlyMode)) {
                                assert pfd != null;
                                FileDescriptor fileDescriptor = Objects.requireNonNull(pfd).getFileDescriptor();
                                Bitmap compressedImageBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                                String nameImage = String.valueOf(System.currentTimeMillis() / 1000);
                                paramsImage.put(key, new DataPart(nameImage + "_image.jpg",
                                        MetodosPublicos.getBitmapAsByteArray(compressedImageBitmap)));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                    }
                }
                return paramsImage;
            }
        };

        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        multipartRequest.setRetryPolicy(policy);
        MySingleton.getInstance(this).addToRequestQueue(multipartRequest);
    }


    private void validateErrors(VolleyError error, String url, int method) {

        error.printStackTrace();
        logE("Error: " + error.getLocalizedMessage()+"url"+ url);
        if (error instanceof com.android.volley.TimeoutError) {
            processFinish(Constantes.TIME_OUT_REQUEST, null, TYPE_ACTION_REQUEST.get(method), url);
            return;
        }
        NetworkResponse response = error.networkResponse;
        if (response != null) {
            switch (response.statusCode) {
                case 400://bad request
                    String errorStr = new String(response.data, StandardCharsets.UTF_8);
                    logE("Error 400: " + errorStr);
                    errorStr = trimMessage(errorStr);
                    processFinish(Constantes.BAD_REQUEST, errorStr, TYPE_ACTION_REQUEST.get(method), url);
                    break;
                case 401://un authorizad
                    logE("Error 401: unauthorizad");
                    preferences.setNotification(false);
                    preferences.setUserLogin(false);
                    processFinish(Constantes.UNAUTHORIZED, null, TYPE_ACTION_REQUEST.get(method), url);
                    break;
                case 403://forbbiden
                    logE("Error 403: forbbiden");
                    processFinish(Constantes.FORBIDDEN, null, TYPE_ACTION_REQUEST.get(method), url);
                    break;
                case 404://not found
                    processFinish(Constantes.REQUEST_NOT_FOUND, null, TYPE_ACTION_REQUEST.get(method), url);
                    break;
                case 412:
                    errorStr = new String(response.data, StandardCharsets.UTF_8);
                    processFinish(Constantes.PRECONDITION_FAILED,errorStr,TYPE_ACTION_REQUEST.get(method), url);
                    break;
                case 415:
                    errorStr = new String(response.data, StandardCharsets.UTF_8);
                    logE("Error 415: " + errorStr);
                    break;
                case 502:
                case 500://server dead
                    logE("Error 500: Servidor Muerto");
                    errorStr = new String(response.data, StandardCharsets.UTF_8);
                    logE("Error %500: " + errorStr);
                    if(url.equals(Constantes.LIST_CONTRACT_PER_OFFLINE_URL)){
                        preferences.setSynContractOffline(false);
                    }
                    processFinish(Constantes.NOT_CONNECTION, null, TYPE_ACTION_REQUEST.get(method), url);
                    break;
            }
        }

    }

    @SuppressLint("DefaultLocale")
    private void processFinish(int option, String jsonObject, String action, String url) {
        Intent localIntent = new Intent();
        localIntent.setAction(action);
        localIntent.putExtra(Constantes.OPTION_JSON_BROADCAST, option);
        localIntent.putExtra(Constantes.VALUE_JSON_BROADCAST, jsonObject);
        localIntent.putExtra(Constantes.URL_REQUEST_BROADCAST, url);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
    }

    public static String trimMessage(String json) {
        String trimmedString = "";
        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getJSONArray("Error").getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return trimmedString;
    }


}
