package co.tecno.sersoluciones.analityco.utilities

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.preference.PreferenceManager
import co.com.sersoluciones.facedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.databases.DBHelper
import org.json.JSONException
import org.json.JSONObject
import java.sql.Timestamp
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Ser SOluciones SAS on 10/05/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
@Singleton
class MyPreferences @SuppressLint("CommitPrefEdits") @Inject constructor(context: Context) {

    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor
    private val _context = context

    init {
        preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = preferences.edit()
    }

    fun cleanJWT() {
        editor.putString(Constantes.KEY_JWT, "")
        editor.putInt(Constantes.KEY_EXPIRES_IN, 0)
        editor.putLong(Constantes.KEY_TIME_EXPIRES_IN, 0)
        editor.apply()
    }

    var isUserLogin: Boolean
        get() = preferences.getBoolean(Constantes.IS_USER_LOGIN, false)
        set(value) {
            if (!value) editor.putBoolean(Constantes.NOTIFICATION, value)
            editor.putBoolean(Constantes.IS_USER_LOGIN, value)
            editor.apply()
        }

    var username: String
        get() = preferences.getString(Constantes.KEY_USERNAME, "")!!
        set(value) {
            editor.putString(Constantes.KEY_USERNAME, value)
            editor.apply()
        }

    var notification: Boolean
        get() = preferences.getBoolean(Constantes.NOTIFICATION, false)
        set(notification) {
            editor.putBoolean(Constantes.NOTIFICATION, notification)
            editor.apply()
        }

    fun setExpiresInToken(expiresIn: String?) {
        val expireDate = Integer.valueOf(expiresIn!!)
        val currentTime = System.currentTimeMillis()
        val timestamp = Timestamp(currentTime)
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp.time
        cal.add(Calendar.SECOND, expireDate)
        val timestampExpireDate = Timestamp(cal.time.time)
        val expireDateTimeMillis = timestampExpireDate.time
        editor.putLong(Constantes.KEY_TIME_EXPIRES_IN, expireDateTimeMillis)
        editor.putInt(Constantes.KEY_EXPIRES_IN, expireDate)
        editor.apply()
    }

    @Throws(JSONException::class)
    fun setJWT(response: JSONObject) {
        editor.putString(Constantes.KEY_JWT, response.getString("access_token"))
        editor.apply()
    }

    fun setJWT(jwt: String?) {
        editor.putString(Constantes.KEY_JWT, jwt)
        editor.apply()
    }

    private val timeExpiresInToken: Long
        get() = preferences.getLong(Constantes.KEY_TIME_EXPIRES_IN, 0)

    val isAccessTokenExpired: Long
        get() {
            val expiresIn = timeExpiresInToken
            if (expiresIn != 0L) {
                val currentTime = System.currentTimeMillis()
                return expiresIn - currentTime
            }
            return 0
        }

    val urlServer: String
        get() = "$iPServer/"

    //preferences.getString(Constantes.KEY_IP_SERVER, _context.getResources().getString(R.string.pref_value_ip_server));
    // "http://api-acceso-stg.analityco.com";
    private val iPServer: String
        get() {
            val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
            return if (!preferences.getBoolean("pref_key_bool_server", false)) "https://api-acceso.analityco.com" //"http://192.168.0.19:5006"
            //preferences.getString(Constantes.KEY_IP_SERVER, _context.getResources().getString(R.string.pref_value_ip_server));
            else "http://api-acceso-stg.analityco.com"
        }

    val token: String?
        get() = preferences.getString(Constantes.KEY_JWT, "")

    private fun setFromDate(fromDate: String) {
        editor.putString(Constantes.KEY_FROM_DATE, fromDate)
        editor.apply()
    }

    private fun setToDate(toDate: String) {
        editor.putString(Constantes.KEY_TO_DATE, toDate)
        editor.apply()
    }

    var profile: String?
        get() = preferences.getString(Constantes.KEY_PROFILE, "")
        set(profile) {
            editor.putString(Constantes.KEY_PROFILE, profile)
            editor.apply()
        }

    var avatarImgPath: String?
        get() = preferences.getString(Constantes.KEY_IMAGE_AVATAR_PATH, "")
        set(pathImage) {
            editor.putString(Constantes.KEY_IMAGE_AVATAR_PATH, pathImage)
            editor.apply()
        }

    fun setDeviceID() {
        @SuppressLint("HardwareIds") val android_id = Settings.Secure.getString(
            _context.applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        DebugLog.log("device id: $android_id")
        editor.putString(Constantes.DEVICE_ID, android_id)
        editor.apply()
        //String android_id = InstanceID.getInstance(_context).getId();

        /*if (ActivityCompat.checkSelfPermission(_context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String android_id;
        try {
            TelephonyManager info = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE);
            android_id = info.getDeviceId();
        } catch (Exception e) {
            WifiManager wimanager = (WifiManager) _context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            android_id = wimanager.getConnectionInfo().getMacAddress();
            if (android_id == null) {
                android_id = "";
            }
        }
        log("device id: " + android_id);
        editor.putString(Constantes.DEVICE_ID, android_id);
        editor.apply();*/
    }

    val deviceId: String?
        get() {
            val deviceId = preferences.getString(Constantes.DEVICE_ID, "")
            if (deviceId!!.isEmpty()) {
                DebugLog.logE("deviceId not found.")
                return ""
            }
            return deviceId
        }

    var selectedProjectId: String?
        get() = preferences.getString(Constantes.KEY_SELECTED_PROJECT_ID, "")
        set(value) {
            editor.putString(Constantes.KEY_SELECTED_PROJECT_ID, value)
            editor.apply()
        }

    var selectedPersonContractOffline : String?
        get() = preferences.getString(Constantes.PERSONALCOMPANY_URL, "")
        set(value) {
            editor.putString(Constantes.PERSONALCOMPANY_URL, value)
            editor.apply()
        }

    var orderCompanyBy: String?
        get() = preferences.getString(Constantes.COMPANY_ORDER_BY, String.format("%s %s", DBHelper.COMPANY_TABLE_COLUMN_NAME, "ASC"))
        set(orderBy) {
            editor.putString(Constantes.COMPANY_ORDER_BY, orderBy)
            editor.apply()
        }

    var orderPersonalBy: String?
        get() = preferences.getString(Constantes.PERSONAL_ORDER_BY, String.format("%s %s", DBHelper.PERSONAL_TABLE_COLUMN_SERVER_ID, "DESC"))
        set(orderBy) {
            editor.putString(Constantes.PERSONAL_ORDER_BY, orderBy)
            editor.apply()
        }

    var orderEmployerBy: String?
        get() = preferences.getString(Constantes.EMPLOYER_ORDER_BY, String.format("%s %s", DBHelper.EMPLOYER_TABLE_COLUMN_NAME, "ASC"))
        set(orderBy) {
            editor.putString(Constantes.EMPLOYER_ORDER_BY, orderBy)
            editor.apply()
        }

    var orderProjectBy: String?
        get() = preferences.getString(Constantes.PROJECT_ORDER_BY, String.format("%s %s", DBHelper.PROJECT_TABLE_COLUMN_NAME, "ASC"))
        set(orderBy) {
            editor.putString(Constantes.PROJECT_ORDER_BY, orderBy)
            editor.apply()
        }

    var orderContractBy: String?
        get() = preferences.getString(Constantes.CONTRACT_ORDER_BY, String.format("%s %s", DBHelper.CONTRACT_TABLE_COLUMN_CONTRACT_NUMBER, "ASC"))
        set(orderBy) {
            editor.putString(Constantes.CONTRACT_ORDER_BY, orderBy)
            editor.apply()
        }

    var profiles: String?
        get() = preferences.getString(Constantes.KEY_JSON_PROFILES, "[]")
        set(profile) {
            editor.putString(Constantes.KEY_JSON_PROFILES, profile)
            editor.apply()
        }

    @Throws(JSONException::class)
    fun setUserJWT(username: String?, response: JSONObject) {
        editor.putString(username, response.getString("access_token"))
        editor.apply()
    }

    fun getCurrentJWT(username: String?): String? {
        return preferences.getString(username, "")
    }

    fun removeCurrentJWT(username: String?) {
        editor.remove(username)
        editor.apply()
    }

    fun setCompanyDays(days: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
        val editor = preferences.edit()
        editor.putString(Constantes.KEY_COMPANY_DAYS, days.toString())
        editor.apply()
    }

    fun setProjectDays(days: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
        val editor = preferences.edit()
        editor.putString(Constantes.KEY_PROJECT_DAYS, days.toString())
        editor.apply()
    }

    fun setContractDays(days: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
        val editor = preferences.edit()
        editor.putString(Constantes.KEY_CONTRACT_DAYS, days.toString())
        editor.apply()
    }

    fun setEmployerDays(days: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
        val editor = preferences.edit()
        editor.putString(Constantes.KEY_EMPLOYER_DAYS, days.toString())
        editor.apply()
    }

    fun setPersonalDays(days: Int) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(_context)
        val editor = preferences.edit()
        editor.putString(Constantes.KEY_PERSONAL_DAYS, days.toString())
        editor.apply()
    }

    fun setVerifyAdnRegisterSettings(enabled: Boolean, option: Int) {
        if (option == 1) editor.putBoolean(
            Constantes.KEY_VIBRATION_ENABLED,
            enabled
        ) else if (option == 2) editor.putBoolean(
            Constantes.KEY_SPEAK_ENABLED,
            enabled
        ) else if (option == 3) editor.putBoolean(Constantes.KEY_DEFAULT_ACTIVITY_START,
                enabled
        ) else if (option == 4) editor.putBoolean(Constantes.KEY_DEFAULT_REQUERIMENT_INCOMPLETE,
        enabled
        ) else if (option == 5) editor.putBoolean(Constantes.KEY_DEFAULT_ENTRY,
        enabled
        )
        editor.apply()
    }

    fun getVerifyAdnRegisterSettings(option: Int): Boolean {
        return if (option == 1) preferences.getBoolean(
            Constantes.KEY_VIBRATION_ENABLED,
            true
        ) else if (option == 2) preferences.getBoolean(
            Constantes.KEY_SPEAK_ENABLED,
            true
        ) else if (option == 3) preferences.getBoolean(Constantes.KEY_DEFAULT_ACTIVITY_START,
                false
        ) else if (option == 4) preferences.getBoolean(Constantes.KEY_DEFAULT_REQUERIMENT_INCOMPLETE,
        false
        ) else if (option == 5) preferences.getBoolean(Constantes.KEY_DEFAULT_ENTRY,
        false
        )
        else false
    }

    fun setVerifyAdnRegisterOptions(value: Boolean, option: Int) {
        if (option == 1) editor.putBoolean(Constantes.KEY_CHECK_REQ, value) else if (option == 2) editor.putBoolean(
            Constantes.KEY_INSPECT_REQ,
            value
        ) else if (option == 3) editor.putBoolean(
            Constantes.KEY_REGISTER_IN,
            value
        ) else if (option == 4) editor.putBoolean(Constantes.KEY_REGISTER_OUT, value)
        editor.apply()
    }

    fun getVerifyAdnRegisterOption(option: Int): Boolean {
        return if (option == 1) preferences.getBoolean(
            Constantes.KEY_CHECK_REQ,
            true
        ) else if (option == 2) preferences.getBoolean(
            Constantes.KEY_INSPECT_REQ,
            true
        ) else if (option == 3) preferences.getBoolean(
            Constantes.KEY_REGISTER_IN,
            true
        ) else if (option == 4) preferences.getBoolean(Constantes.KEY_REGISTER_OUT, true) else false
    }

    var enabledErollmentButtons: Boolean
        get() = preferences.getBoolean(Constantes.KEY_ENROLLMENT_ENBABLED_BUTTONS, true)
        set(value) {
            editor.putBoolean(Constantes.KEY_ENROLLMENT_ENBABLED_BUTTONS, value)
            editor.apply()
        }

    var syncDate: Long
        get() = preferences.getLong(Constantes.KEY_SYNC_DATE, 0)
        set(timeStamp) {
            editor.putLong(Constantes.KEY_SYNC_DATE, timeStamp)
            editor.apply()
        }
    var policyUrls: String?
        get() = preferences.getString(Constantes.KEY_POLICY_DATA, "")
        set(policyUrls) {
            editor.putString(Constantes.KEY_POLICY_DATA, policyUrls)
            editor.apply()
        }

    var editReqEnabled: Boolean
        get() = preferences.getBoolean(Constantes.KEY_EDIT_REQ_ENABLED, false)
        set(value) {
            editor.putBoolean(Constantes.KEY_EDIT_REQ_ENABLED, value)
            editor.apply()
        }
    var isonline: Boolean
        get() = preferences.getBoolean("Conexion", false)
        set(value) {
            editor.putBoolean("Conexion", value)
            editor.apply()
        }
    var synContract: Boolean
        get() = preferences.getBoolean("synContract", false)
        set(value) {
            editor.putBoolean("synContract", value)
            editor.apply()
        }
    var synEmployer: Boolean
        get() = preferences.getBoolean("synEmployer", false)
        set(value) {
            editor.putBoolean("synEmployer", value)
            editor.apply()
        }
    var synPersonal: Boolean
        get() = preferences.getBoolean("synPersonal", false)
        set(value) {
            editor.putBoolean("synPersonal", value)
            editor.apply()
        }
    var synContractOffline: Boolean
        get() = preferences.getBoolean("synContractOffline", false)
        set(value) {
            editor.putBoolean("synContractOffline", value)
            editor.apply()
        }
    companion object {
        private const val PREF_NAME = "PREFERENCES_PACKING_LIST"
        private const val PRIVATE_MODE = 0
        private const val KEY_USER_NAME = "pref_key_username"
        private const val KEY_USER_PASS = "pref_key_password"
    }
}