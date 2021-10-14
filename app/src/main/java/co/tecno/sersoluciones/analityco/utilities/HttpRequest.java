package co.tecno.sersoluciones.analityco.utilities;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import co.tecno.sersoluciones.analityco.ApplicationContext;
import co.tecno.sersoluciones.analityco.receivers.LoginResultReceiver;

import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.log;
import static co.com.sersoluciones.facedetectorser.utilities.DebugLog.logW;


/**
 * Created by Ser SOluciones SAS on 10/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 **/
public class HttpRequest {

    public static String makeStringParamsLogin(String username, String password) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        ContentValues values = new ContentValues();
        values.put(Constantes.KEY_GRAN_TYPE, Constantes.KEY_PASS);
        values.put(Constantes.KEY_USERNAME, username);
        values.put(Constantes.KEY_PASS, password);
        values.put(Constantes.KEY_SCOPE, "openid roles email profile");
        values.put(Constantes.KEY_CLIENT_ID, Constantes.KEY_CLIENT_ID_VALUE);
        values.put(Constantes.KEY_CLIENT_SECRET, Constantes.KEY_CLIENT_SECRET_VALUE);


//        if (!(username.equals("analityco@mail.com") || username.equals("zquirozca@gmail.com") || username.equals("superuser@mail.com") || username.equals("fabio.zaraza@gmail.com") || username.equals("tavomorales88@gmail.com")
//                || username.equals("gus_adolfo88@hotmail.com") || username.equals("anderson.rodri26@gmail.com") || username.equals("fabio.zaraza@pm-asociados.com"))) {
//            values.put(Constantes.KEY_APP, "true");
//            values.put(Constantes.KEY_IMEI, imei);
//        }
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            String key = entry.getKey(); // name
            String value = entry.getValue().toString(); // value
            if (first)
                first = false;
            else
                result.append("&");
            try {
                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public static String makeParamsInUrl(ContentValues contentValues) {

        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : contentValues.valueSet()) {
            String key = entry.getKey(); // name
            log(key);
            String value = entry.getValue().toString(); // value
            if (first) {
                result.append("?");
                first = false;
            } else
                result.append("&");
            try {
                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value, "UTF-8"));
                //result.append(value);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    public static void refreshToken(String url, final String params,
                                    final LoginResultReceiver mReceiver, final String username) {

        JsonRequest<JSONObject> request = new JsonRequest<JSONObject>(Request.Method.POST, url,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            logW(response.toString());
                            MyPreferences myPreferences = new MyPreferences(ApplicationContext.applicationContext);
                            myPreferences.setJWT(response);
                            myPreferences.setUserJWT(username, response);
                            myPreferences.setExpiresInToken(response.getString("expires_in"));
                            myPreferences.isAccessTokenExpired();
                            mReceiver.send(Constantes.LOGIN_SUCCESS, Bundle.EMPTY);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            final Bundle bundle = new Bundle();

            @Override
            public void onErrorResponse(VolleyError error) {
                String responseError = "";
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {
                    switch (response.statusCode) {
                        case 400:
                            responseError = new String(response.data);
                            logW(responseError);
                            responseError = trimMessage(responseError);
                            //if(responseError != null) logE(responseError);
                            break;
                    }
                }

                bundle.putString(Intent.EXTRA_TEXT, responseError);
                mReceiver.send(Constantes.LOGIN_ERROR, bundle);
            }
        }
        ) {
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
            public String getBodyContentType() {
                // TODO Auto-generated method stub
                return "application/x-www-form-urlencoded";
            }
        };

        int socketTimeout = 10000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        ApplicationContext.getInstance().addToRequestQueue(request);
    }

    private static String trimMessage(String json) {
        String trimmedString = null;

        try {
            JSONObject obj = new JSONObject(json);
            trimmedString = obj.getString("error_description");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return trimmedString;
    }
}
