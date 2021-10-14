package co.tecno.sersoluciones.analityco.geofences

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.appcompat.widget.PopupMenu
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.BaseActivity
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.SettingsGeofenceActivity
import co.tecno.sersoluciones.analityco.fragments.ARG_POLYGON_STR
import co.tecno.sersoluciones.analityco.models.Geofence
import co.tecno.sersoluciones.analityco.utilities.MetodosPublicos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

import kotlinx.android.synthetic.main.activity_geofence_google.*
import net.steamcrafted.materialiconlib.MaterialMenuInflater
import java.util.*
import kotlin.math.roundToInt

class GeofenceGoogleActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener {

    private val BOGOTA = LatLng(4.72, -74.04)
    private val RADIUS_OF_EARTH_METERS = 6371009

    companion object {
        private const val MY_LOCATION_REQUEST_CODE = 329
        private const val NEW_REMINDER_REQUEST_CODE = 330
        private val PLACE_AUTOCOMPLETE_REQUEST_DESTINATION = 10
        const val EXTRA_LIMIT_FENCE = "MaxgeoFence"
        const val GEOFENCE = "GeoFence"

        @JvmStatic
        fun newIntent(context: Context, limitGeofence: Long): Intent {
            val intent = Intent(context, GeofenceGoogleActivity::class.java)
            intent.putExtra(EXTRA_LIMIT_FENCE, limitGeofence)
            return intent
        }

        @JvmStatic
        fun newIntent(context: Context, limitGeofence: Long, jsonGeo: String): Intent {
            val intent = Intent(context, GeofenceGoogleActivity::class.java)
            intent.putExtra(EXTRA_LIMIT_FENCE, limitGeofence)
            intent.putExtra(ARG_POLYGON_STR, jsonGeo)
            return intent
        }
    }

    private var drawPolygon = true
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationManager: LocationManager? = null
    private var mMap: GoogleMap? = null

    private val mCircles = ArrayList<DraggableCircle>(1)
    private var polyline: Polyline? = null
    private var mMarker: Marker? = null
    private val points = ArrayList<LatLng>()
    private var geofenceLimit: Long = 0
    private var isSave = false
    private var appPreferences: SharedPreferences? = null
    private var jsonPolygonStr = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = "Gecocerca"
        }
        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geofenceLimit = intent.getLongExtra(EXTRA_LIMIT_FENCE, 100)
        appPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (intent.extras != null && intent!!.extras!!.containsKey(ARG_POLYGON_STR))
            jsonPolygonStr = intent.getStringExtra(ARG_POLYGON_STR)!!

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_LOCATION_REQUEST_CODE
            )
        }

        fabCheck.setOnClickListener { createGeoFence() }
        fabSave.setOnClickListener { submit() }

        fabGoogle.setOnClickListener {
            val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields
            )
                .setCountry("CO")
                .build(this@GeofenceGoogleActivity)

//            val autocompleteFilter = AutocompleteFilter.Builder()
//                    .build()
//            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).setFilter(autocompleteFilter)
//                    .build(this@MainActivity)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_DESTINATION)
        }

    }

    override fun attachLayoutResource() {
        super.setChildLayout(R.layout.activity_geofence_google)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NEW_REMINDER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Snackbar.make(main, "", Snackbar.LENGTH_LONG).show()
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_DESTINATION) {
            if (resultCode == AutocompleteActivity.RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                onPlaceSelected(place)
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                val status = Autocomplete.getStatusFromIntent(data!!)
                logW(status.statusMessage)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private fun onPlaceSelected(place: Place?) {
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(place!!.latLng, 15.0f))
        if (place.attributions != null) {
            val attributions = place.attributions as List<String>
            attributions.forEach {
                logW(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            onMapAndPermissionReady()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.geo_fence, menu)
        MaterialMenuInflater
            .with(this) // Provide the activity context
            // Set the fall-back color for all the icons. Colors set inside the XML will always have higher priority
            .setDefaultColor(Color.WHITE)
            // Inflate the menu
            .inflate(R.menu.geo_fence, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                //NavUtils.navigateUpFromSameTask(this)
                onBackPressed()
                return true
            }
            R.id.action_clear -> clearMap()
            R.id.action_draw -> enableDraw()
            R.id.action_current_location -> currentLocation()
            R.id.action_settings -> dialogRadius() // startActivity(Intent(this, SettingsGeofenceActivity::class.java))
            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left)
    }

    private fun enableDraw() {
        val menuItemView = findViewById<View>(R.id.action_draw)
        val popupMenu = PopupMenu(this, menuItemView!!)
        popupMenu.inflate(R.menu.menu_popup_geofence)
        popupMenu.menu.getItem(0).isChecked = drawPolygon
        popupMenu.menu.getItem(1).isChecked = !drawPolygon
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_polyline -> {
                    if (!item.isChecked) {
                        popupMenu.menu.getItem(1).isChecked = false
                        drawPolygon = !item.isChecked
                        item.isChecked = !item.isChecked
                        clearMap()
                        Snackbar.make(menuItemView, "Ubique diferentes puntos para dibujar la geocerca.", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
                R.id.action_circle -> {
                    if (!item.isChecked) {
                        popupMenu.menu.getItem(0).isChecked = false
                        drawPolygon = item.isChecked
                        item.isChecked = !item.isChecked
                        clearMap()
                        Snackbar.make(
                            menuItemView,
                            "Presione sostenidamente para generar geocerca circular, puede jalar y soltar.",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
                }
                else -> {
                }
            }
            true
        }
        popupMenu.show()
    }

    private fun clearMap() {

        points.clear()
        if (polyline != null)
            polyline!!.remove()
        mMap?.clear()

        polyline = null
        mMarker = null
        isSave = false
        fabCheck.visibility = View.GONE
        fabSave.visibility = View.GONE
    }


    private fun createGeoFence() {
        if (!points.isEmpty()) {
            mMap?.clear()

            mMap?.addPolygon(
                PolygonOptions().addAll(points)
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(50, 0, 0, 0))
            )
            getPolygonCenterPoint(points)

            mMarker = null
            isSave = true
            fabCheck.visibility = View.GONE
            animateFabBtn(fabSave)
        }
    }

    private fun getPolygonCenterPoint(polygonPointsList: ArrayList<LatLng>): LatLng? {
        var centerLatLng: LatLng? = null
        val builder = LatLngBounds.Builder()
        for (i in 0 until polygonPointsList.size) {
            builder.include(polygonPointsList[i])
        }
        val bounds = builder.build()
        centerLatLng = bounds.center

//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

        return centerLatLng
    }

    private fun submit() {
        if (isSave) {

            if (!drawPolygon) {

                if (mCircles.size > 0) {
                    val circle = mCircles.get(0).getCircle()
                    points.clear()
                    points.addAll(getLatLngfromCircle(circle.center, circle.radius))
                }
            }

            val latLngsGoogle = ArrayList<LatLng>()
            val coordinates = ArrayList<DoubleArray>()
            val coordinatesDraw = ArrayList<DoubleArray>()

            for (latLngs in points) {
                logW("latLngs: " + latLngs.latitude + ", " + latLngs.longitude)
                //double[] polygonsArray = {latLngs.getLatitude(), latLngs.getLongitude()}
                val polygonsArray = doubleArrayOf(latLngs.latitude, latLngs.longitude)
                coordinates.add(polygonsArray)
                coordinatesDraw.add(doubleArrayOf(latLngs.longitude, latLngs.latitude))
                latLngsGoogle.add(LatLng(latLngs.latitude, latLngs.longitude))
            }
            coordinates.add(doubleArrayOf(points.get(0).latitude, points.get(0).longitude))
            coordinatesDraw.add(doubleArrayOf(points.get(0).longitude, points.get(0).latitude))

            var area = SphericalUtil.computeArea(latLngsGoogle)
            area = area / 1000000
            area = Math.round(area * 100).toDouble() / 100
            log("area polygon: $area km2")
            //Toast.makeText(this, "Area Geofence: " + area + " km2", Toast.LENGTH_LONG).show()

            if (area > geofenceLimit) {
                clearMap()
                MetodosPublicos.alertDialog(this, "Area geocerca mayor a la permitida")
                return
            }

            val geofence = co.tecno.sersoluciones.analityco.models.Geofence(
                Arrays.copyOf<DoubleArray, Any>(
                    coordinatesDraw.toTypedArray(),
                    coordinatesDraw.toTypedArray().size, Array<DoubleArray>::class.java
                )
            )
            val gson = Gson()
            val json = gson.toJson(geofence)
            logW(json)

            val intent = Intent()
            intent.putExtra(GEOFENCE, json)
            setResult(0, intent)
            onBackPressed()
        } else {
            MetodosPublicos.alertDialog(this, "Debe crear una geocerca para poder guardar")
        }
    }

    private fun getLatLngfromCircle(centre: LatLng, radius: Double): ArrayList<LatLng> {
        val points = ArrayList<LatLng>()
        // Convert to radians.
        val lat = centre.latitude * Math.PI / 180.0
        val lon = centre.longitude * Math.PI / 180.0
        var t = 0.0
        while (t <= Math.PI * 2) {
            // y
            val latPoint = lat + radius / RADIUS_OF_EARTH_METERS * Math.sin(t)
            // x
            val lonPoint = lon + radius / RADIUS_OF_EARTH_METERS * Math.cos(t) / Math.cos(lat)

            // saving the location on circle as a LatLng point
            val point = LatLng(latPoint * 180.0 / Math.PI, lonPoint * 180.0 / Math.PI)

            // here mMap is my GoogleMap object
            mMap?.addMarker(MarkerOptions().position(point))

            // now here note that same point(lat/lng) is used for marker as well as saved in the ArrayList
            points.add(point)
            t += 0.3
        }
        return points
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.run {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isMapToolbarEnabled = false
            setOnMarkerClickListener(this@GeofenceGoogleActivity)
        }
        mMap!!.setOnMapClickListener(this)
        mMap!!.setOnMarkerDragListener(this)
        mMap!!.setOnMapLongClickListener(this)

        mMap!!.setOnCircleClickListener(object : GoogleMap.OnCircleClickListener {
            override fun onCircleClick(circle: Circle) {
                // Flip the red, green and blue components of the circle's stroke color.
                circle.strokeColor = Color.RED
            }
        })
        onMapAndPermissionReady()
        if (!jsonPolygonStr.isEmpty())
            drawPolygon(jsonPolygonStr)
    }

    private fun drawPolygon(jsonObjStr: String) {
        val coord = Gson().fromJson(jsonObjStr, Geofence::class.java) ?: return

        val builder = LatLngBounds.Builder()
        if (coord.coordinates != null) {
            for (j in 0 until coord.coordinates[0].size) {
                points.add(LatLng(coord.coordinates[0][j][1], coord.coordinates[0][j][0]))
                builder.include(LatLng(coord.coordinates[0][j][1], coord.coordinates[0][j][0]))
            }
        }
        mMap?.addPolygon(
            PolygonOptions()
                .addAll(points)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(50, 0, 0, 0))
        )

        isSave = true
        animateFabBtn(fabSave)

        val bounds = builder.build()
        mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))

    }


    override fun onMarkerClick(p0: Marker?): Boolean {

        return true
    }

    private fun onMapAndPermissionReady() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true
            centerCamera()
        }
    }

    @SuppressLint("MissingPermission")
    private fun currentLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun centerCamera() {

        val bestProvider = locationManager?.getBestProvider(Criteria(), false)
        val location = locationManager?.getLastKnownLocation(bestProvider)
        if (location != null) {
            val latLng = LatLng(location.latitude, location.longitude)
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        } else
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(BOGOTA, 10.0f))

    }


    override fun onMarkerDragEnd(marker: Marker?) {
        onMarkerMoved(marker!!)
    }

    override fun onMarkerDragStart(marker: Marker?) {
        onMarkerMoved(marker!!)
    }

    override fun onMarkerDrag(marker: Marker?) {
        onMarkerMoved(marker!!)
    }

    private fun onMarkerMoved(marker: Marker) {
        for (draggableCircle in mCircles) {
            if (draggableCircle.onMarkerMoved(marker)) {
                break
            }
        }
    }

    override fun onMapClick(point: LatLng?) {

        if (drawPolygon) {

            if (isSave) {
                clearMap()
                isSave = false
            }

            points.add(point!!)

            // Get back the mutable Polyline

            if (polyline != null) polyline!!.remove()
            polyline = mMap?.addPolyline(
                PolylineOptions()
                    .color(Color.RED)
                    .addAll(points)
            )


            if (mMarker == null) {
                mMarker = mMap?.addMarker(
                    MarkerOptions()
                        .position(point)
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_RED
                            )
                        )
                )
            }
            animateMarker(mMarker!!, point)
            if (points.size == 3) {
                animateFabBtn(fabCheck)
            }
        }
    }

    fun animateFabBtn(fab: FloatingActionButton) {
        fab.visibility = View.VISIBLE
        fab.scaleX = 0f
        fab.scaleY = 0f

        val interpolador = AnimationUtils.loadInterpolator(
            baseContext,
            android.R.interpolator.fast_out_slow_in
        )

        fab.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(interpolador)
            .setDuration(600).startDelay = 500
    }

    override fun onMapLongClick(point: LatLng?) {
        if (drawPolygon) return


        if (mCircles.size > 0) {
            val circle = mCircles.get(0)
            circle.removeCircle()
        }

        // Create the circle.
        val circle = DraggableCircle(point!!, appPreferences?.getString("pref_key_radius", "100")!!.toInt())
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(point, getZoomLevel(circle.getCircle())))

        mCircles.add(0, circle)
        isSave = true
        animateFabBtn(fabSave)
    }

    fun animateMarker(marker: Marker, toPosition: LatLng) {
        val startPosition = marker.position
        val endPosition = LatLng(toPosition.latitude, toPosition.longitude)
        val latLngInterpolator = LatLngInterpolator.LinearFixed()
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.duration = 150 // duration 1 second
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener {
            val animation = it as ValueAnimator
            try {
                val v = animation.animatedFraction
                val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                marker.position = newPosition
            } catch (ex: Exception) {
                // I don't care atm..
            }
        }

        valueAnimator.start()
    }

    interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng

        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    fun getZoomLevel(circle: Circle?): Float {
        var zoomLevel = 11.0f
        if (circle != null) {
            val radius = circle.radius + circle.radius / 2
            val scale = radius / 500
            zoomLevel = (16 - Math.log(scale) / Math.log(2.0)).toFloat()
        }
        return zoomLevel
    }

    inner class DraggableCircle {
        private var mCenterMarker: Marker? = null
        private var mRadiusMarker: Marker? = null
        private var mCircle: Circle? = null
        private var mRadiusMeters: Double = 0.0

        constructor(
            center: LatLng, radiusMeters: Int
        ) {

            mRadiusMeters = radiusMeters.toDouble()
            mCenterMarker = mMap?.addMarker(
                MarkerOptions()
                    .position(center)
                    .draggable(true)
            )
            mRadiusMarker = mMap?.addMarker(
                MarkerOptions()
                    .position(toRadiusLatLng(center, radiusMeters.toDouble()))
                    .draggable(true)
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                        )
                    )
            )
            mCircle = mMap?.addCircle(
                CircleOptions()
                    .center(center)
                    .radius(radiusMeters.toDouble())
                    .strokeColor(Color.RED)
                    .fillColor(Color.argb(50, 0, 0, 0))
                    .clickable(true)
            )
        }

        fun removeCircle() {
            if (mCircle != null) {
                mCircle!!.remove()
                mRadiusMarker!!.remove()
                mCenterMarker!!.remove()
            }
        }

        fun onMarkerMoved(marker: Marker): Boolean {
            if (marker == mCenterMarker) {
                mCircle!!.center = marker.position
                mRadiusMarker!!.position = toRadiusLatLng(marker.position, mRadiusMeters)
                return true
            }
            if (marker == mRadiusMarker) {
                mRadiusMeters = toRadiusMeters(mCenterMarker!!.position, mRadiusMarker!!.position)
                mCircle!!.radius = mRadiusMeters.toDouble()
                return true
            }
            return false
        }

        fun setStrokeColor(color: Int) {
            mCircle!!.strokeColor = color
        }

        fun setStrokePattern(pattern: List<PatternItem>) {
            mCircle!!.strokePattern = pattern
        }

        fun getCircle(): Circle {
            return mCircle!!
        }

        fun setClickable(clickable: Boolean) {
            mCircle!!.isClickable = clickable
        }
    }

    /**
     * Generate LatLng of radius marker
     */
    private fun toRadiusLatLng(center: LatLng, radiusMeters: Double): LatLng {
        val radiusAngle = Math.toDegrees(radiusMeters / RADIUS_OF_EARTH_METERS) / Math.cos(Math.toRadians(center.latitude))
        return LatLng(center.latitude, center.longitude + radiusAngle)
    }

    private fun toRadiusMeters(center: LatLng, radius: LatLng): Double {
        val result = FloatArray(1)
        Location.distanceBetween(
            center.latitude, center.longitude,
            radius.latitude, radius.longitude, result
        )
        return result[0].toDouble()
    }

    fun dialogRadius() {

        val linearLayout = LinearLayout(this)
        val input = EditText(this@GeofenceGoogleActivity)
        val maxLength = 12
        input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.imeOptions = EditorInfo.IME_ACTION_DONE
        input.setEms(7)
        input.setText(appPreferences?.getString("pref_key_radius", "100"))
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.gravity = Gravity.CENTER
        linearLayout.layoutParams = lp
        linearLayout.addView(input)

        val builder = AlertDialog.Builder(this@GeofenceGoogleActivity)
            .setTitle("Geocerca circular")
            .setMessage("Radio de la geocerca circular en (m)")
            .setPositiveButton("Aceptar") { dialog, which ->

            }
            .setNegativeButton("Cancelar", null)
            .setView(linearLayout)

        val alertDialog = builder.create()
        alertDialog.show()


        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(View.OnClickListener {
            var wantToCloseDialog = false

            val value = input.text.toString()
            logW(value)
            if (!value.isEmpty()) {
                input.error = null
                appPreferences!!.edit().putString("pref_key_radius", value).apply()
                wantToCloseDialog = true
            } else {
                input.error = "Este campo es obligatorio"
                input.requestFocus()
            }
            if (wantToCloseDialog)
                alertDialog.dismiss()
        })
    }

}
