package co.tecno.sersoluciones.analityco.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog;
import co.tecno.sersoluciones.analityco.ApplicationContext;
import co.tecno.sersoluciones.analityco.BuildConfig;
import co.tecno.sersoluciones.analityco.utilities.ConnectionDetector;
import co.tecno.sersoluciones.analityco.utilities.Constantes;
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;
import co.tecno.sersoluciones.analityco.utilities.VolleyMultipartRequest;
import id.zelory.compressor.Compressor;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;
import static co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 * Created by Ser Soluciones SAS on 20/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class CrudIntentService extends IntentService {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_REQUEST = "co.sersoluciones.apps.services.action.REQUEST";
    public static final String ACTION_REQUEST_FORM_DATA = "co.sersoluciones.apps.services.action.REQUEST_FORM_DATA";
    public static final String ACTION_REQUEST_PATCH = "co.sersoluciones.apps.services.action.REQUEST_PATCH";
    public static final String ACTION_REQUEST_POST = "co.sersoluciones.apps.services.action.REQUEST_POST";
    public static final String ACTION_REQUEST_PUT = "co.sersoluciones.apps.services.action.REQUEST_PUT";
    public static final String ACTION_REQUEST_DELETE = "co.sersoluciones.apps.services.action.REQUEST_DELETE";

    private static final Map<Integer, String> TYPE_ACTION_REQUEST;

    static {
        @SuppressLint("UseSparseArrays")
        Map<Integer, String> tmp = new HashMap<>();
        tmp.put(1, ACTION_REQUEST_POST);
        tmp.put(2, ACTION_REQUEST_PUT);
        tmp.put(3, ACTION_REQUEST_DELETE);
        TYPE_ACTION_REQUEST = Collections.unmodifiableMap(tmp);
    }

    private static final String EXTRA_URL = "co.sersoluciones.apps.services.extra.URL";
    private static final String EXTRA_METHOD = "co.sersoluciones.apps.services.extra.METHOD";
    private static final String EXTRA_DATA = "co.sersoluciones.apps.services.extra.DATA";
    private static final String EXTRA_PARAMS_QUERY = "co.sersoluciones.apps.services.extra.PARAMS_QUERY";
    private static final String EXTRA_MANY = "co.sersoluciones.apps.services.extra.MANY";
    private static final String EXTRA_SAVE = "co.sersoluciones.apps.services.extra.SAVE";
    private MyPreferences preferences;
    private CrudIntentService instance;

    public CrudIntentService() {
        super("CrudIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startRequestCRUD(Context context, String url, int method, String jsonObject,
                                        String paramsQuery, boolean many, boolean save) {
        Intent intent = new Intent(context, CrudIntentService.class);
        intent.setAction(ACTION_REQUEST);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DATA, jsonObject);
        intent.putExtra(EXTRA_PARAMS_QUERY, paramsQuery);
        intent.putExtra(EXTRA_MANY, many);
        intent.putExtra(EXTRA_SAVE, save);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
        context.startService(intent);
//        }
    }


    public static void startRequestCRUD(Context context, String url, int method, String jsonObject,
                                        String paramsQuery, boolean many) {
        Intent intent = new Intent(context, CrudIntentService.class);
        intent.setAction(ACTION_REQUEST);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DATA, jsonObject);
        intent.putExtra(EXTRA_PARAMS_QUERY, paramsQuery);
        intent.putExtra(EXTRA_MANY, many);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
        context.startService(intent);
//        }
    }

    public static void startRequestPatch(Context context, String url, String jsonArray) {
        Intent intent = new Intent(context, CrudIntentService.class);
        intent.setAction(ACTION_REQUEST_PATCH);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_DATA, jsonArray);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
        context.startService(intent);
//        }
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    @SuppressWarnings("unused")
    public static void startActionFormData(Context context, String url, int method,
                                           final HashMap<String, String> params) {
        Intent intent = new Intent(context, CrudIntentService.class);
        intent.setAction(ACTION_REQUEST_FORM_DATA);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_METHOD, method);
        intent.putExtra(EXTRA_DATA, params);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(intent);
//        } else {
        context.startService(intent);
//        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        launchService();
        preferences = new MyPreferences(CrudIntentService.this);
        instance = this;
    }

    private void launchService() {
        String channelId = "CrudIntentService";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel();
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        Notification notification = builder.setOngoing(true)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(PRIORITY_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(104, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel("CrudIntentService",
                "CrudIntent Service", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return "CrudIntentService";
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ConnectionDetector connectionDetector = new ConnectionDetector(this);
        boolean conexionServer = connectionDetector.isConnectedToServer();
        DebugLog.log("conexion a internet: " + conexionServer);

        if (!conexionServer) {
            processFinish("", Constantes.NOT_INTERNET, null);
            return;
        }

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REQUEST.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final int method = intent.getIntExtra(EXTRA_METHOD, 0);
                final String data = intent.getStringExtra(EXTRA_DATA);
                final String paramsQuery = intent.getStringExtra(EXTRA_PARAMS_QUERY);
                final boolean many = intent.getBooleanExtra(EXTRA_MANY, false);
                final boolean save = intent.getBooleanExtra(EXTRA_SAVE, false);
                try {
                    if (data.isEmpty())
                        jsonRequest(url, method, paramsQuery, many, save);
                    else
                        jsonRequest(url, method, new JSONObject(data), paramsQuery, many, save);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (ACTION_REQUEST_FORM_DATA.equals(action)) {
                String url = intent.getStringExtra(EXTRA_URL);
                int method = intent.getIntExtra(EXTRA_METHOD, 0);
                HashMap<String, String> hashMap = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_DATA);
                final String paramsQuery = intent.getStringExtra(EXTRA_PARAMS_QUERY);
                jsonMultiPartRequest(url, method, hashMap, paramsQuery);
            } else if (ACTION_REQUEST_PATCH.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final String data = intent.getStringExtra(EXTRA_DATA);
                jsonPatchRequest(url, data);
            }
        }
    }


    private void jsonRequest(String url, int method, String paramsQuery, boolean many, boolean save) {
        jsonRequest(url, method, null, paramsQuery, many, save);
    }


    private void jsonRequest(final String uri, final int method, JSONObject jsonObject, String paramsQuery, boolean many, final boolean save) {

        final long initialTime = System.currentTimeMillis();
        String url = preferences.getUrlServer() + uri;
        if (paramsQuery != null && !paramsQuery.isEmpty()) {
            url += paramsQuery;
        }
        logW("url: " + url);
        if (jsonObject != null) {
            logW(jsonObject.toString());
        }
        JsonRequest request;
        if (many) {
            request = new JsonArrayRequest(url,
                    new Response.Listener<JSONArray>() {
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onResponse(JSONArray response) {
                            //logW(jarrayObject);
                            long seconds = (System.currentTimeMillis() - initialTime) / 1000;
                            DebugLog.logW(String.format("Total time request %02d:%02d", seconds / 60, seconds % 60));
                            switch (uri) {
                                case Constantes.LIST_USERS_URL:
                                    processFinish(uri, Constantes.SEND_REQUEST, response.toString());
                                    break;
                                /*case Constantes.LIST_PROJECTS_BY_COMPANY_URL:
                                    processFinish(Constantes.SEND_REQUEST, response.toString(), ACTION_REQUEST);
                                    break;*/
                                case Constantes.LIST_USERS_BY_COMPANY_URL:
                                    processFinish(uri, Constantes.UPDATE_ADMIN_USERS, response.toString());
                                    break;
                                default:
                                    if (save) {
                                        String jArrayStr = response.toString();
                                        UpdateDBAsyncTask updateDBAsyncTask = new UpdateDBAsyncTask(instance);
                                        updateDBAsyncTask.execute(jArrayStr, uri);
                                    } else {
                                        processFinish(uri, Constantes.SEND_REQUEST, response.toString());
                                    }
                                    break;

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            DebugLog.logE("Error: " + error.getMessage());
                            NetworkResponse response = error.networkResponse;
                            if (response != null) {
                                switch (response.statusCode) {
                                    case 404:
                                        DebugLog.logE("Error 404");
                                        processFinish(uri, Constantes.REQUEST_NOT_FOUND, null);
                                        break;
                                    case 401:
                                        DebugLog.logE("Error 401");
                                        preferences.setNotification(false);
                                        preferences.setUserLogin(false);
                                        processFinish(uri, Constantes.UNAUTHORIZED, null);
                                        break;
                                    case 403:
                                        DebugLog.logE("Error 403");
                                        processFinish(uri, Constantes.FORBIDDEN, null);
                                        break;
                                    case 400:
                                        DebugLog.logE("Error 400");
                                    default:
                                        processFinish(uri, Constantes.BAD_REQUEST, null);
                                        break;
                                }
                            } else {
                                processFinish(uri, Constantes.BAD_REQUEST, null);
                            }
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
        } else {

            request = new JsonObjectRequest(method, url, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String jsonObject = response.toString();
                            //logW(jsonObject);
                            if (method == Request.Method.GET)
                                processFinish(Constantes.SUCCESS_REQUEST, jsonObject, ACTION_REQUEST, uri);
                            else
                                processFinish(Constantes.SUCCESS_REQUEST, jsonObject, TYPE_ACTION_REQUEST.get(method), uri);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            DebugLog.logE("Error: " + error.getLocalizedMessage());
                            NetworkResponse response = error.networkResponse;
                            if (response != null) {
                                switch (response.statusCode) {
                                    case 404:
                                        DebugLog.logE("Error 404");
                                        if (method == Request.Method.GET)
                                            processFinish(Constantes.REQUEST_NOT_FOUND, null, ACTION_REQUEST, uri);
                                        else
                                            processFinish(Constantes.REQUEST_NOT_FOUND, null, TYPE_ACTION_REQUEST.get(method), uri);
                                        break;
                                    case 500:
                                        DebugLog.logE("Error 500");
                                        if (method == Request.Method.GET)
                                            processFinish(Constantes.SERVER_ERROR, null, ACTION_REQUEST, uri);
                                        else
                                            processFinish(Constantes.SERVER_ERROR, null, TYPE_ACTION_REQUEST.get(method), uri);
                                        break;
                                    case 401:
                                        DebugLog.logE("Error 401");
                                        preferences.setNotification(false);
                                        preferences.setUserLogin(false);
                                        processFinish(uri, Constantes.UNAUTHORIZED, null);
                                        break;
                                    case 403:
                                        DebugLog.logE("Error 403");
                                        processFinish(Constantes.FORBIDDEN, null, TYPE_ACTION_REQUEST.get(method), uri);
                                        break;
                                    case 400:
                                        try {
                                            DebugLog.logE("Error 400: " + new String(response.data, StandardCharsets.UTF_8));

                                            switch (uri) {
                                                case Constantes.CREATE_COMPANY_URL:
                                                case Constantes.CREATE_USER_URL:
                                                    if (method == Request.Method.GET)
                                                        processFinish(Constantes.BAD_REQUEST, new String(response.data, StandardCharsets.UTF_8), ACTION_REQUEST, uri);
                                                    else
                                                        processFinish(Constantes.BAD_REQUEST, new String(response.data, StandardCharsets.UTF_8), TYPE_ACTION_REQUEST.get(method), uri);
                                                    break;
                                                default:
                                                    String errorStr = null;
                                                    errorStr = new String(response.data, StandardCharsets.UTF_8);
                                                    logE("Error 400: " + errorStr);
                                                    if (method == Request.Method.GET)
                                                        processFinish(Constantes.BAD_REQUEST, errorStr, ACTION_REQUEST, uri);
                                                    else {
                                                        processFinish(Constantes.BAD_REQUEST, errorStr, TYPE_ACTION_REQUEST.get(method), uri);
                                                    }
                                                    //processFinish(Constantes.BAD_REQUEST, new String(response.data, "UTF-8"), TYPE_ACTION_REQUEST.get(method), uri);
                                                    break;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;
                                    default:
                                        if (method == Request.Method.GET)
                                            processFinish(Constantes.BAD_REQUEST, null, ACTION_REQUEST, uri);
                                        else
                                            processFinish(Constantes.BAD_REQUEST, null, TYPE_ACTION_REQUEST.get(method), uri);
                                        break;
                                }
                            } else {
                                if (method == Request.Method.GET)
                                    processFinish(Constantes.BAD_REQUEST, null, ACTION_REQUEST, uri);
                                else
                                    processFinish(Constantes.BAD_REQUEST, null, TYPE_ACTION_REQUEST.get(method), uri);
                            }
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
        }
        int socketTimeout = 60000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        Objects.requireNonNull(ApplicationContext.getInstance()).addToRequestQueue(request);
    }


    private void jsonPatchRequest(final String uri, String json) {
        String url = preferences.getUrlServer() + uri;
        DebugLog.logW("url: " + url);
        if (json != null) {
            logW(json);
        }
        JsonRequest<JSONObject> request =
                new JsonRequest<JSONObject>(Request.Method.PATCH, url, json, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String jsonObject = response.toString();
                        processFinish(Constantes.SUCCESS_REQUEST, jsonObject, ACTION_REQUEST_PATCH, uri);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        DebugLog.logE("Error: " + error.getLocalizedMessage());
                        NetworkResponse response = error.networkResponse;
                        if (response != null) {
                            DebugLog.logE("statusCode: " + response.statusCode);
                            DebugLog.logE("Error " + response.statusCode + ", data: " + new String(response.data, StandardCharsets.UTF_8));
                        }
                        processFinish(Constantes.BAD_REQUEST, null, ACTION_REQUEST_PATCH, uri);
                    }
                }) {
                    @Override
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        try {
                            String jsonString =
                                    new String(
                                            response.data,
                                            HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
                            return Response.success(
                                    new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                        } catch (UnsupportedEncodingException e) {
                            return Response.error(new ParseError(e));
                        } catch (JSONException je) {
                            return Response.error(new ParseError(je));
                        }
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + preferences.getToken());
                        map.put("VersionApplication", BuildConfig.VERSION_NAME + " - " + BuildConfig.VERSION_CODE);
                        return map;
                    }
                };
        int socketTimeout = 60000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        ApplicationContext.getInstance().addToRequestQueue(request);
    }

    private void jsonMultiPartRequest(final String uri, int method, final Map<String, String> params, String paramsQuery) {
        String url = preferences.getUrlServer() + uri;
        if (paramsQuery != null && !paramsQuery.isEmpty()) {
            url += paramsQuery;
        }
        if (uri.equals(Constantes.RUES_URL)) {
            url = uri;
        }
        DebugLog.logW("url: " + url);
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(method, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String resultResponse = new String(response.data);
                        processFinish(Constantes.SUCCESS_FILE_UPLOAD, resultResponse, ACTION_REQUEST_FORM_DATA, uri);
                        /*try {
                            JSONObject result = new JSONObject(resultResponse);
                            DebugLog.log(result.toString());
                            processFinish(Constantes.SUCCESS_REQUEST, resultResponse);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    }
                } else {
                    errorMessage = new String(networkResponse.data);
                }
                DebugLog.logE("Error: " + errorMessage);
                processFinish(Constantes.BAD_REQUEST, null, ACTION_REQUEST_FORM_DATA, uri);
                error.printStackTrace();
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
                        case "image":
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
//                                Bitmap compressedImageBitmap = new Compressor(instance).compressToBitmap(imageUri);

                            break;
                    }
                }
                return paramsImage;
            }
        };

        int socketTimeout = 120000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        multipartRequest.setRetryPolicy(policy);
        ApplicationContext.getInstance().addToRequestQueue(multipartRequest);
    }

    @SuppressLint("DefaultLocale")
    private void processFinish(String url, int option, String jsonObject) {
        Intent localIntent = new Intent();
        localIntent.setAction(Constantes.BROADCAST_GET_JSON);
        localIntent.putExtra(Constantes.OPTION_JSON_BROADCAST, option);
        localIntent.putExtra(Constantes.VALUE_JSON_BROADCAST, jsonObject);
        localIntent.putExtra(Constantes.URL_REQUEST_BROADCAST, url);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
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
}

