package co.tecno.sersoluciones.analityco.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Created by Ser Soluciones SAS on 19/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
const val REQUEST_TIMEOUT = 15L

class ConnectionDetector(private val context: Context) {

    private var mConnectivityManager: ConnectivityManager? = null
    private var isNetwork = false

    val isConnectingToInternet: Boolean
        get() {
            mConnectivityManager = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
//            val builder = NetworkRequest.Builder()
//            mConnectivityManager!!.registerNetworkCallback(builder.build(), networkCallback)

//            val commands: MutableList<String> = ArrayList()
//            commands.add("ping")
//            commands.add("-c")
//            commands.add("5")
//            commands.add("18.215.226.185")
//            doCommand(commands)

            logW("isConnectedToServer $isConnectedToServer")
            val activeNetworkInfo = mConnectivityManager!!.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    private fun isConnectingToInternet2(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private var networkCallback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {

            isNetwork = true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) mConnectivityManager!!.bindProcessToNetwork(network) else ConnectivityManager.setProcessDefaultNetwork(network)
            val isMetered = mConnectivityManager!!.isActiveNetworkMetered
            logW("connection network available isMetered: $isMetered")
        }

        override fun onLost(network: Network) {
            logE("connection network lost")
            isNetwork = false
        }
    }

    val isConnectedToServer: Boolean
        get() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return (activeNetworkInfo != null && activeNetworkInfo.isConnected)
//                    && isInternetAccessible)
        }

    //make a URL to a known source
    private val isInternetAccessible: Boolean
        get() {

            val preferences = MyPreferences(context)
            val urlStr = preferences.urlServer + "api/Server/gettime/"
            //make a URL to a known source
            val url = URL(urlStr)
            val httpClient = OkHttpClient
                    .Builder()
                    .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                    .addNetworkInterceptor { chain ->

                        val httpUrl = chain.request().url
                        println("url: $httpUrl is the same ${url.sameFile(httpUrl.toUrl())}")
                        //                            logW("url ${conn.url.sameFile(url)} responseCode: ${conn.responseCode} body: ${br.readLine()}")
                        val response = chain.proceed(chain.request())

                        if (url.sameFile(httpUrl.toUrl()))
                            response
                        else
                            response.newBuilder().code(400).build()
                    }
                    .build()
            val request =
                    Request.Builder()
                            .url(urlStr)
                            .get()
                            .build()

            try {
                val response: Response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) return false//throw IOException("Unexpected code $response")


                val res = response.body?.string()
                println("response public url $res response.isRedirect ${response.isRedirect}")
                return true
            } catch (e: Exception) {
                if (e is SocketTimeoutException) logE("SocketTimeoutException")
                return false
            }
        }

    @Throws(IOException::class)
    fun doCommand(command: List<String?>?) {
        var s: String? = null
        val pb = ProcessBuilder(command)
        val process = pb.start()
        val stdInput = BufferedReader(InputStreamReader(process.inputStream))
        val stdError = BufferedReader(InputStreamReader(process.errorStream))
        // read the output from the command
        println("Here is the standard output of the command:\n")
        while (stdInput.readLine().also({ s = it }) != null) {
            println(s)
        }
        // read any errors from the attempted command
        println("Here is the standard error of the command (if any):\n")
        while (stdError.readLine().also({ s = it }) != null) {
            println(s)
        }
    }

}