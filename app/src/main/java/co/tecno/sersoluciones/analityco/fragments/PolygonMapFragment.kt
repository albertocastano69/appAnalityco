package co.tecno.sersoluciones.analityco.fragments


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.models.Geofence
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import java.util.ArrayList

const val ARG_POLYGON_STR = "json_polygon_str"

/**
 * A simple [Fragment] subclass.
 * Use the [PolygonMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PolygonMapFragment : Fragment(), OnMapReadyCallback {

    private var jsonPolygonStr: String? = null
    private var mMap: GoogleMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            jsonPolygonStr = it.getString(ARG_POLYGON_STR)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.frg) as SupportMapFragment  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment
        mapFragment.getMapAsync(this)
        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        mMap = googleMap!!
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL

        mMap!!.clear() //clear old markers

//        mMap.addMarker(MarkerOptions()
//                .position(LatLng(37.4219999, -122.0862462))
//                .title("Spider Man")
//                .icon(bitmapDescriptorFromVector(activity!!, R.drawable.ic_02_administrativotrasparente2)))

        drawPolygon(jsonPolygonStr!!)

    }


    private fun drawPolygon(jsonObjStr: String) {
        val coord = Gson().fromJson(jsonObjStr, Geofence::class.java) ?: return

        val polygon = ArrayList<LatLng>()
        val builder = LatLngBounds.Builder()
        if (coord.coordinates != null) {
            for (j in 0 until coord.coordinates[0].size) {
                polygon.add(LatLng(coord.coordinates[0][j][1], coord.coordinates[0][j][0]))
                builder.include(LatLng(coord.coordinates[0][j][1], coord.coordinates[0][j][0]))
            }
        }
        mMap?.addPolygon(PolygonOptions()
                .addAll(polygon)
                .strokeColor(Color.rgb(59, 178, 208))
                //.fillColor(Color.argb(50, 0, 0, 0)))
                .fillColor(Color.argb(50, 59, 178, 208)))

        val bounds = builder.build()
        try {
            mMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            e.printStackTrace()
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 16f))
        }


    }

    private fun areBoundsTooSmall(bounds: LatLngBounds, minDistanceInMeter: Int): Boolean {
        val result = FloatArray(1)
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude,
                bounds.northeast.latitude, bounds.northeast.longitude, result)
        return result[0] < minDistanceInMeter
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param jsonPolygon Parameter 1.
         * @return A new instance of fragment MapFragment.
         */
        @JvmStatic
        fun newInstance(jsonPolygon: String) =
                PolygonMapFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_POLYGON_STR, jsonPolygon)
                    }
                }
    }
}
