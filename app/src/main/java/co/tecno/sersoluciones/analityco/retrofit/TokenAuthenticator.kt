package co.tecno.sersoluciones.analityco.retrofit

import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.tecno.sersoluciones.analityco.models.Profile
import co.tecno.sersoluciones.analityco.utilities.JWTUtils
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject

class TokenAuthenticator constructor(val preferences: MyPreferences) : Authenticator {

    private val httpClient = OkHttpClient()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (preferences.isUserLogin) {
            DebugLog.logE("Error 401: unauthorized")
            preferences.notification = false
            preferences.isUserLogin = false
//            val token = refreshToken(httpClient, preferences)
//            processToken(token, preferences)
//
//            return response.request.newBuilder()
//                    .header("Authorization", "Bearer ${preferences.token}")
//                    .build()
        }
        return response.request
    }
}

fun processToken(
    response: String,
    preferences: MyPreferences
) {
    try {
        val jObj = JSONObject(response)
        // val token = response.getString("token")

        val jsonObjectResponse = JWTUtils.decoded(jObj.getString("access_token"))
        preferences.setExpiresInToken(jsonObjectResponse!!.getString("exp"))
        preferences.isAccessTokenExpired
        preferences.username = (jsonObjectResponse.getString("preferred_username"))

        val profile = Gson().fromJson(jsonObjectResponse.toString(), Profile::class.java)
        log(Gson().toJson(profile))
        preferences.profile = Gson().toJson(profile)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}