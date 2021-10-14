package co.tecno.sersoluciones.analityco.utilities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Point
import android.location.Location
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import android.widget.EditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Ser Soluciones SAS on 27/07/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */

fun isIngeo(geofenceJson: String, currentLocation: LatLng?): Boolean {
    val jsonObjectGeo = JSONObject(geofenceJson)
    val jsonArray = jsonObjectGeo.getJSONArray("coordinates")
    val latLngsGoogle: MutableList<LatLng> = ArrayList()
    for (i in 0 until jsonArray.length()) {
        for (j in 0 until jsonArray.getJSONArray(i)
            .length()) {
            val jsonArrayLatLng = jsonArray.getJSONArray(i).getJSONArray(j)
            //logW("lat: " + jsonArrayLatLng.getDouble(1) + ", lon: " + jsonArrayLatLng.getDouble(0));
            latLngsGoogle.add(
                LatLng(
                    jsonArrayLatLng.getDouble(1),
                    jsonArrayLatLng.getDouble(0)
                )
            )
        }
    }
    return if (currentLocation == null) false else PolyUtil.containsLocation(
        currentLocation,
        latLngsGoogle,
        false
    )
}

@SuppressLint("SimpleDateFormat")
fun validateFinishDate(finishDate: String): Boolean {
    var isActive = true
    val now = Date()
    @SuppressLint("SimpleDateFormat") val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val date = format.parse(finishDate)
    if (date!!.before(now)) {
        isActive = false
    }

    return isActive
}

object Utils {
    private var screenWidth = 0
    private const val screenHeight = 0

    @JvmStatic
    fun getScreenWidth(c: Context): Int {
        if (screenWidth == 0) {
            val wm = c.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            val size = Point()
            display.getSize(size)
            screenWidth = size.x
        }
        return screenWidth
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun reflectToContentValues(jsonArray: JSONArray): Array<ContentValues> {
        val contentValues = ArrayList<ContentValues>()
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            contentValues.add(reflectToContentValue(jsonObj))
        }
        return contentValues.toTypedArray()
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun reflectToContentValues(
        jsonArray: JSONArray,
        valuesExcept: Array<String>
    ): Array<ContentValues> {
        val contentValues = ArrayList<ContentValues>()
        for (i in 0 until jsonArray.length()) {
            val jsonObj = jsonArray.getJSONObject(i)
            contentValues.add(reflectToContentValue(jsonObj, valuesExcept))
        }
        return contentValues.toTypedArray()
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun reflectToContentValue(jsonObj: JSONObject): ContentValues {
        val cv = ContentValues()
        val keysIterator = jsonObj.keys()
        //        while (keysIterator.hasNext()) {
//            String key = keysIterator.next();
//            String value = jsonObj.getString(key);
//            //log("key: " + key + ", value: " + value);
//            cv.put(key, value);
//        }
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            val value = jsonObj[key]
            if (value is JSONObject) {
                cv.put(key, JSONObject(value.toString()).toString())
            } else if (value is JSONArray) {
                cv.put(key, JSONArray(value.toString()).toString())
            } else if (value is Boolean) {
                cv.put(key, if (value) 1 else 0)
            } else {
                cv.put(key, value.toString())
            }
        }
        return cv
    }

    @JvmStatic
    @Throws(JSONException::class)
    private fun reflectToContentValue(
        jsonObj: JSONObject,
        valuesExcept: Array<String>
    ): ContentValues {
        val cv = ContentValues()
        val keysIterator = jsonObj.keys()
        while (keysIterator.hasNext()) {
            val key = keysIterator.next()
            val value = jsonObj[key]
            if (Arrays.asList(*valuesExcept).contains(key)) {
                continue
            }
            if (value is JSONObject) {
                cv.put(key, JSONObject(value.toString()).toString())
            } else if (value is JSONArray) {
                cv.put(key, JSONArray(value.toString()).toString())
            } else if (value is Boolean) {
                cv.put(key, if (value) 1 else 0)
            } else {
                cv.put(key, value.toString())
            }
            //cv.put(key, value);
        }
        return cv
    }

    @JvmStatic
    fun cursorToJArray(cursor: Cursor): JSONArray {
        val jsonArray = JSONArray()
        while (cursor.moveToNext()) {
            jsonArray.put(cursorToJObject(cursor))
        }
        return jsonArray
    }

    @JvmStatic
    fun cursorToJObject(cursor: Cursor): JSONObject {
        val jsonObject = JSONObject()
        try {
            for (columnName in cursor.columnNames) {
                val typeColumn = cursor.getType(cursor.getColumnIndex(columnName))
                if (typeColumn == Cursor.FIELD_TYPE_STRING) {
                    val entityStr = cursor.getString(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityStr)
                } else if (typeColumn == Cursor.FIELD_TYPE_FLOAT) {
                    val entityFloat = cursor.getDouble(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityFloat)
                } else if (typeColumn == Cursor.FIELD_TYPE_INTEGER) {
                    val entityInt = cursor.getInt(cursor.getColumnIndex(columnName))
                    if (columnName == "IsActive" || columnName == "IsSelected" || columnName == "Expiry" || columnName == "IsRegister") if (entityInt == 1) jsonObject.put(
                        columnName,
                        true
                    ) else jsonObject.put(columnName, false) else jsonObject.put(
                        columnName,
                        entityInt
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

    @JvmStatic
    fun cursorToJObject(cursor: Cursor, valuesExcept: Array<String?>): JSONObject {
        val jsonObject = JSONObject()
        try {
            for (columnName in cursor.columnNames) {
                if (Arrays.asList(*valuesExcept).contains(columnName)) {
                    continue
                }
                val typeColumn = cursor.getType(cursor.getColumnIndex(columnName))
                if (typeColumn == Cursor.FIELD_TYPE_STRING) {
                    val entityStr = cursor.getString(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityStr)
                } else if (typeColumn == Cursor.FIELD_TYPE_FLOAT) {
                    val entityFloat = cursor.getDouble(cursor.getColumnIndex(columnName))
                    jsonObject.put(columnName, entityFloat)
                } else if (typeColumn == Cursor.FIELD_TYPE_INTEGER) {
                    val entityInt = cursor.getInt(cursor.getColumnIndex(columnName))
                    if (columnName == "IsActive") if (entityInt == 1) jsonObject.put(
                        columnName,
                        true
                    ) else jsonObject.put(columnName, false) else jsonObject.put(
                        columnName,
                        entityInt
                    )
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return jsonObject
    }

    @JvmStatic
    fun makePlaceholders(len: Int): String {
        return if (len < 1) {
            // It will lead to an invalid query anyway ..
            throw RuntimeException("No placeholders")
        } else {
            val sb = StringBuilder(len * 2 - 1)
            sb.append("?")
            for (i in 1 until len) {
                sb.append(",?")
            }
            sb.toString()
        }
    }

    @Throws(JSONException::class)
    fun merge(vararg jsonObjects: JSONObject): JSONObject {
        val jsonObject = JSONObject()
        for (temp in jsonObjects) {
            val keys = temp.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                jsonObject.put(key, temp[key])
            }
        }
        return jsonObject
    }

    @JvmStatic
    fun getLastTimeDay(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    @JvmStatic
    fun getFirstTimeDay(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }


    @JvmStatic
    fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }
}