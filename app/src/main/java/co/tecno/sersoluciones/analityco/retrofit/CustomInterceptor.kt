package co.tecno.sersoluciones.analityco.retrofit

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE
import co.tecno.sersoluciones.analityco.BuildConfig
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import okhttp3.*
import java.io.IOException

const val RETROFIT_TIME_OUT_EXCEPTION = "co.tecno.sersoluciones.analityco.retrofit.TIME_OUT_EXCEPTION"
fun sendLocalBroadcast(context: Context, action: String) {
    val localIntent = Intent()
    localIntent.action = action
    LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)
}

class CustomInterceptor constructor(val preferences: MyPreferences, val context: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        try {
            if (preferences.isUserLogin) {
                val newRequest: Request = getNewRequest(chain)
                return chain.proceed(newRequest)
            }
            return chain.proceed(chain.request())
        } catch (e: Throwable) {
            if (e is IOException) {
                if (e is java.net.SocketTimeoutException || e is java.net.ConnectException || e is java.net.UnknownHostException) {
                    sendLocalBroadcast(context, RETROFIT_TIME_OUT_EXCEPTION)
                    logE(e.message)
                }
                throw e
            } else {
                throw IOException(e)
            }
        }
    }


    private fun getNewRequest(chain: Interceptor.Chain) = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer ${preferences.token}")
        .addHeader("VersionApplication",  "${BuildConfig.VERSION_NAME} - ${BuildConfig.VERSION_CODE}")
        .build()

    companion object {
        @Throws(Exception::class)
        fun refreshToken(httpClient: OkHttpClient, preferences: MyPreferences): String {
            val formBody: RequestBody = FormBody.Builder()
//                    .add(Constantes.KEY_GRAN_TYPE, Constantes.GT_REFRESK_TOKEN)
//                    .add(Constantes.KEY_SCOPE, Constantes.VALUE_SCOPE)
//                    .add(Constantes.KEY_CLIENT_ID, Constantes.KEY_CLIENT_ID_VALUE)
//                    .add(Constantes.KEY_CLIENT_SECRET, Constantes.KEY_CLIENT_SECRET_VALUE)
//                    .add(Constantes.GT_REFRESK_TOKEN, preferences.refreshToken)
                .build()
            val baseUrl = preferences.urlServer
            val request =
                Request.Builder()
                    .url(baseUrl + Constantes.API_TOKEN_AUTH_SERVER).post(formBody)
                    .build()
            val response: Response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return ""//throw IOException("Unexpected code $response")

            return response.body!!.string()
        }
    }
}