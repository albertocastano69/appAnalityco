package co.tecno.sersoluciones.analityco

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.transition.Explode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import butterknife.BindView
import butterknife.ButterKnife
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.services.LocationUpdateService
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task


/**
 * Created by Ser Soluciones SAS on 25/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
@SuppressLint("Registered")
abstract class BaseActivity : AppCompatActivity() {
    // Bool to track whether the app is already resolving an error
    private var mResolvingError = false
    private var myHandler: Handler? = null
    private var delayTime: Long = 0
    private var expiresTime = false
    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    lateinit var preferences: MyPreferences

    @JvmField
    protected var alertDialogLocation: AlertDialog? = null
    private var connectivityManager: ConnectivityManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // inside your activity (if you did not enable transitions in your theme)
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.exitTransition = Explode()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_layout)
        attachLayoutResource()
        ButterKnife.bind(this)
        setupToolbar()
        mResolvingError = (savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false))
        preferences = MyPreferences(this)
        expiresTime = false
        if (!preferences.isUserLogin) expiresTime = true
        delayTime = preferences.isAccessTokenExpired
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        myHandler = Handler()
    }

    abstract fun attachLayoutResource()
    private fun setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            //toolbar.setNavigationIcon(R.drawable.ic_menu_white);
        }
    }

    override fun onResume() {
        super.onResume()
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
        onDetectInternet(isOnline(this))
        preferences.isonline=isOnline(this)
    }

    override fun onPause() {
        super.onPause()
        onDetectInternet(isOnline(this))
        preferences.isonline=isOnline(this)
    }

    override fun onStart() {
        super.onStart()
        registerConnectivityNetworkMonitor()
        if (expiresTime || !preferences.isUserLogin) finish() else timeToken()
    }

    override fun onStop() {
        super.onStop()
        myHandler!!.removeCallbacks(closeControls)
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    /**
     *
     */
    private fun registerConnectivityNetworkMonitor() {

        val builder = NetworkRequest.Builder()
        connectivityManager?.registerNetworkCallback(
            builder.build(), networkCallback
        )
    }

    protected fun setChildLayout(layout: Int) {
        val mInflater = LayoutInflater.from(this)
        val relativeLayout = findViewById<RelativeLayout>(R.id.main_layout)
        mInflater.inflate(layout, relativeLayout, true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError)
    }

    /**
     * Metodo para refrescar el token si existe interaccion con la aplicacion
     */
    override fun onUserInteraction() {
        super.onUserInteraction()
        if (expiresTime || !preferences.isUserLogin) {
            Toast.makeText(this, "Sesion finalizada por favor logueese de nuevo", Toast.LENGTH_SHORT).show()
            finish()
        } else timeToken()

    }

    /**
     * Metodo para refrescar la sesion y reiniciar contadores
     * realiza peticion tipo POST al servidor y obtiene de nuevo el Token
     */
    private fun timeToken() {
        delayTime = preferences.isAccessTokenExpired
        myHandler!!.removeCallbacks(closeControls)
        myHandler!!.postDelayed(closeControls, delayTime)
        //int expiresInToken = preferences.getExpiresInToken();
        val millisecondsRemain = delayTime
        //log("millisecondsRemain: " + millisecondsRemain + ", secondsRemain: "
        //        + millisecondsRemain/1000 +", expiresInToken: " + expiresInToken);
        if (millisecondsRemain <= 0 && !expiresTime) {
            expiresTime = true
            finish()
        }
    }

    /**
     * Variable que tiene en cuenta el tiempo de expiracion del token
     */
    private val closeControls = Runnable {
        logE("tiempo expirado")
        expiresTime = true
        finish()
    }

    /**
     * Check if network available or not
     *
     * @param context context of de activity
     */
    @Suppress("DEPRECATION")
    private fun isOnline(context: Context): Boolean {
        var isOnline = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = cm.activeNetworkInfo
            //should check null because in airplane mode it will be null
            isOnline = netInfo != null && netInfo.isConnected
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return isOnline
    }

    /**
     * @param noConnection boolean
     * @return intent
     */
    @Suppress("unused")
    private fun getConnectivityIntent(noConnection: Boolean): Intent {
        val intent = Intent()
        intent.action = "co.com.sersoluciones.CONNECTIVITY_CHANGE"
        intent.putExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, noConnection)
        return intent
    }

    private var isNetwork = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        /**
         * @param network
         */
        override fun onAvailable(network: Network) {
            if (!isNetwork) {
                logW("Connection network available")
                isNetwork = true
                onDetectInternet(true)
                preferences.isonline=true
            }
        }

        /**
         * @param network
         */
        override fun onLost(network: Network) {
            if (isNetwork) {
                logE("Connection network lost")
                isNetwork = false
                onDetectInternet(false)
                preferences.isonline=false
            }
        }
    }

    open fun onDetectInternet(online: Boolean) {
        logW("visibility textViewOnline ${!online} ")
//        findViewById<View>(R.id.textViewOnline).visibility = if (online) View.GONE else View.VISIBLE
        Handler(Looper.getMainLooper()).post {
            findViewById<View>(R.id.textViewOnline).visibility =
                if (online) View.GONE else View.VISIBLE
        }
    }

    @Suppress("unused")
    protected fun dialogLocation(msg: String? = "Por favor encienda el GPS para que Analityco opere correctamente") {
        val builder = AlertDialog.Builder(this)
            .setMessage(msg)
            .setPositiveButton(R.string.accept) { dialog: DialogInterface?, which: Int ->
                contentIntent
                alertDialogLocation = null
            }
            .setNegativeButton("Cancelar") { dialog: DialogInterface, which: Int ->
                dialog.dismiss()
                alertDialogLocation = null
                finish()
            }
            .setCancelable(false)
        alertDialogLocation = builder.create()
        alertDialogLocation!!.show()
    }

    private val contentIntent: Unit
        get() {
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1)
        }

    private val locationRequest = LocationRequest.create()?.apply {
        interval = 10 * 1000
        fastestInterval = 5 * 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    protected fun checkLocationSettings(function: (value: Boolean) -> Unit) {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { _ ->
            try {
                Intent(this.applicationContext, LocationUpdateService::class.java).also { intent ->
                    startService(intent)
                }
                function.invoke(true)
                Log.d(TAG, "servicio de ubicacion del sistema funcionando normal")
            } catch (e: Exception) {
            }
        }

        task.addOnFailureListener { exception ->

            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    logE("NO TIENE PERMISOS DE UBICACION LA APP")
                    function.invoke(false)
                    stopService(Intent(applicationContext, LocationUpdateService::class.java))
                    exception.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> {
                if (resultCode == Activity.RESULT_OK) {
                    Intent(
                        this.applicationContext,
                        LocationUpdateService::class.java
                    ).also { intent ->
                        startService(intent)
                    }
                    this.recreate()
                } else {
                    stopService(Intent(applicationContext, LocationUpdateService::class.java))
                    finish()
                }
            }
        }
    }

    companion object {
        const val REQUEST_CHECK_SETTINGS = 1001
        private const val STATE_RESOLVING_ERROR = "resolving_error"
        private val TAG = "BaseActivity"
    }
}