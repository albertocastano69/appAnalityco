package co.tecno.sersoluciones.analityco.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.facedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.facedetectorser.utilities.DebugLog.logE
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import com.google.android.gms.location.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat

/**
 * Created by Ser Soluciones SAS on 21/10/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
const val UMB_ACCURACY = 150
const val SER_LOCATION_ACTION = "co.sersoluciones.locationser.services.SER_LOCATION_ACTION"

class LocationUpdateService : Service() {

    private val mBinder: IBinder = MyBinder()
    private var canGetLocation = false
    private var lastLocationJson: JSONObject? = null
    private var preferences: MyPreferences? = null
    private var mCurrentLocation: Location? = null
    private var isLocationManagerUpdatingLocation = false
    private var locationCallback: LocationCallback? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null

    private var runStartTimeInMillis: Long = 0

    inner class MyBinder : Binder() {
        val service: LocationUpdateService
            get() = this@LocationUpdateService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    fun restartLocationUpdate() {
        if (canGetLocation) {
            stopUpdatingLocation()
            startUpdatingLocation()
        }
    }
    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        log("Servicio Track iniciado")
        canGetLocation = false
        isGPSNetEnabled
        preferences = MyPreferences(this)

        createFuseLocation()
        fusedLocationClient?.lastLocation
            ?.addOnSuccessListener { location: Location? ->
                location?.let {
                    mCurrentLocation = it
                    updateLocation()
                }
            }
        createLocationCallback()
    }

    fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
//                    Log.d(
//                        TAG, "( accuracy: " + location.accuracy + " " + location.latitude + "," + location.longitude + ", time: " + dateFormat.format(
//                            location.time
//                        ) + ")"
//                    )
                    mCurrentLocation = location
                    updateLocation()
                }
            }
        }
    }

    private fun createFuseLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
    }

    private val locationRequest = LocationRequest.create()?.apply {
        interval = 5000
        fastestInterval = 3000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun stopUpdatingLocation() {
        if (isLocationManagerUpdatingLocation) {
            Log.w(TAG, "actualizacion de ubicacion apagado")
            isLocationManagerUpdatingLocation = false
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun startUpdatingLocation() {
        if (!isLocationManagerUpdatingLocation) {
            isLocationManagerUpdatingLocation = true
            runStartTimeInMillis = (SystemClock.elapsedRealtimeNanos() / 1000000)
            Log.w(TAG, "actualizacion de ubicacion encendido")
            if (fusedLocationClient == null) createFuseLocation()
            if (locationCallback == null) createLocationCallback()
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    override fun onDestroy() {
        DebugLog.logE("Servicio Track detenido")
        stopUpdatingLocation()
        isLocationManagerUpdatingLocation = false
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            stopService(Intent(this, LocationUpdateService::class.java))
            return START_NOT_STICKY
        }
//        launchService()
        if (canGetLocation) {
            stopUpdatingLocation()
            startUpdatingLocation()
        }

        return START_NOT_STICKY
    }

    private fun launchService() {
        var channelId: String? = "LocationUpdateService"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel()
        }
        val builder = NotificationCompat.Builder(this, channelId!!)
        val notification = builder.setOngoing(true) //.setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(104, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String? {
        val chan = NotificationChannel(
            "LocationUpdateService",
            "LocationUpdate Service", NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return "LocationUpdateService"
    }

    private val isGPSNetEnabled: JSONObject
        get() {
            canGetLocation = false
            var isGPSEnabled = false
            var networkEnabled = false
            try {
                val locationManager =
                    this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                if (isGPSEnabled) canGetLocation = true
            } catch (e: Exception) {
                logE("No network found" + e.message)
            }
            val jsonObject = JSONObject()
            try {
                jsonObject.put("GPS", isGPSEnabled)
                jsonObject.put("NETWORK", networkEnabled)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return jsonObject
        }

    fun getLastLocationJson(): JSONObject? {
        return if (lastLocationJson != null) lastLocationJson else JSONObject()
    }

    val lastLocation: Location?
        get() = mCurrentLocation

    private fun updateLocation() {

        val batteryIntent =
            applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!
        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = level / scale.toFloat()
        lastLocationJson = JSONObject()

        mCurrentLocation?.let { mCurrentLocation ->
            //Informacion del celular
            lastLocationJson!!.put(Constantes.IMEI_KEY, preferences!!.deviceId)
            lastLocationJson!!.put(
                Constantes.BATERIA_KEY,
                Integer.valueOf((batteryPct * 100).toInt())
            )
            lastLocationJson!!.put(Constantes.SELLER_ID, preferences!!.username)
            lastLocationJson!!.put(Constantes.VELOCIDAD_KEY, mCurrentLocation.speed.toInt())
            lastLocationJson!!.put(Constantes.LATITUD_KEY, mCurrentLocation.latitude)
            lastLocationJson!!.put(Constantes.LONGITUD_KEY, mCurrentLocation.longitude)
            lastLocationJson!!.put(Constantes.PROVIDER_KEY, mCurrentLocation.provider)
            lastLocationJson!!.put(Constantes.PRECISION_KEY, mCurrentLocation.accuracy.toInt())
            val jsonObjectGPS = isGPSNetEnabled
            jsonObjectGPS.put(Constantes.LATITUD_KEY, mCurrentLocation.latitude)
            jsonObjectGPS.put(Constantes.LONGITUD_KEY, mCurrentLocation.longitude)
            jsonObjectGPS.put(Constantes.ALTIUD_KEY, mCurrentLocation.altitude)
            lastLocationJson!!.put(Constantes.IS_GPS_KEY, jsonObjectGPS.toString())
            lastLocationJson!!.put(Constantes.BEARING_KEY, mCurrentLocation.bearing.toInt())
            lastLocationJson!!.put(Constantes.TIME_KEY, System.currentTimeMillis())

            val horizontalAccuracy: Float = mCurrentLocation.accuracy
//            logW("mCurrentLocation.accuracy ${mCurrentLocation.accuracy}")
//            if (horizontalAccuracy < UMB_ACCURACY) //100 meter filter
            sendLocationBroadcast(mCurrentLocation)
        }

    }

    /**
     * Envio de location por medio de broadcast
     */
    private fun sendLocationBroadcast(location: Location) {
        val intent = Intent(SER_LOCATION_ACTION)
        intent.putExtra("location", location)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        val dateFormat: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        private val TAG = LocationUpdateService::class.java.simpleName
    }
}