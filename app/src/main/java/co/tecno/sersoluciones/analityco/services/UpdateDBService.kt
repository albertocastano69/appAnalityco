package co.tecno.sersoluciones.analityco.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE
import co.tecno.sersoluciones.analityco.ApplicationContext
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.repository.AnalitycoRepository
import co.tecno.sersoluciones.analityco.repository.ApiStatus
import co.tecno.sersoluciones.analityco.services.UpdateDBService
import co.tecno.sersoluciones.analityco.utilities.*
import co.tecno.sersoluciones.analityco.worker.BackupWorker
import co.tecno.sersoluciones.analityco.worker.SendMsgWorker
import co.tecno.sersoluciones.analityco.worker.SendReportWorker
import com.android.volley.*
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.JsonRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Ser Soluciones SAS on 11/11/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
@SuppressLint("Registered")
class UpdateDBService : IntentService("UpdateDBService") {
    private var preferences: MyPreferences? = null
    private var instance: Context? = null

    @Inject
    lateinit var repository: AnalitycoRepository

    init {
        ApplicationContext.analitycoComponent.injectService(this)
    }

    override fun onCreate() {
        super.onCreate()
        preferences = MyPreferences(this@UpdateDBService)
        instance = this
        launchService()
    }

    private fun launchService() {
        var channelId = "UpdateDBService"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel()
        }
        val builder = NotificationCompat.Builder(this, channelId)
        val notification = builder.setOngoing(true) //.setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(9, notification)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val chan = NotificationChannel(
            "UpdateDBService",
            "UpdateDB Service", NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return "UpdateDBService"
    }

    override fun onHandleIntent(intent: Intent?) {
        val connectionDetector = ConnectionDetector(this)
        val conexionServer = connectionDetector.isConnectedToServer
        DebugLog.log("conexion a internet: $conexionServer")
        if (!conexionServer) return
        try {
            val updateDb = intent!!.getBooleanExtra(EXTRA_UPDATE_DB, false)
            sendReportToServer()
            sendReports()
            sendReportSurvey()
            sendImageToIndivudalContractToServer()
            if (updateDb) {
                var values = ContentValues()
                values.put(Constantes.KEY_SELECT, true)
                var paramsQuery = HttpRequest.makeParamsInUrl(values)
                jsonRequest(Constantes.LIST_PROJECTS_URL, paramsQuery)
                jsonRequest(Constantes.LIST_CONTRACTS_URL, paramsQuery)
                values = ContentValues()
                values.put("project", true)
                paramsQuery = HttpRequest.makeParamsInUrl(values)
                jsonRequest(Constantes.LIST_CONTRACT_PER_OFFLINE_URL, paramsQuery)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun sendReportSurvey(){
        GlobalScope.launch(Dispatchers.IO) {
            val reports = repository.getReportSurvey()
            loop@ for(data in reports){
                when(repository.sendSurverServer(data)){
                    ApiStatus.ERROR ->{
                        logE("---------- reporte no enviado y puesto en un worker para futuro envio -> $data.id")
//                        setReportWork()
//                        break@loop
                    }
                    ApiStatus.DONE ->{
                        DebugLog.log("reporte borrado exitosamente ${data.id}")
                        repository.deleteReportSurvey(data.id)
                    }
                }
            }
        }
    }
    private fun sendReports() {

        GlobalScope.launch(Dispatchers.IO) {
            val reports = repository.getAllReportes()
            loop@ for (report in reports) {

                when (repository.sendReporte(report)) {
                    ApiStatus.ERROR -> {
                        logE("---------- reporte no enviado y puesto en un worker para futuro envio -> $report.id")
//                        setReportWork()
//                        break@loop
                    }
                    ApiStatus.DONE -> {
                        DebugLog.log("reporte borrado exitosamente ${report.id}")
                        repository.deleteReporte(report.id)
                    }
                }

            }
        }
    }

    private fun setReportWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val sendReportRequest = OneTimeWorkRequestBuilder<SendReportWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(SendReportWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, sendReportRequest)
    }

    private fun jsonRequest(uri: String, paramsQuery: String?) {
        val initialTime = System.currentTimeMillis()
        if (preferences == null) preferences = MyPreferences(this)
        var url = preferences!!.urlServer + uri
        if (paramsQuery != null && !paramsQuery.isEmpty()) {
            url += paramsQuery
        }
        DebugLog.logW("url: $url")
        val request: JsonRequest<*> = object : JsonArrayRequest(url,
            Response.Listener { response -> //logW(jarrayObject);
                val seconds = (System.currentTimeMillis() - initialTime) / 1000
                DebugLog.logW(
                    String.format(
                        "Total time request %02d:%02d",
                        seconds / 60,
                        seconds % 60
                    )
                )
                val jArrayStr = response.toString()
                val updateDBAsyncTask = UpdateDBAsyncTask(instance, false)
                updateDBAsyncTask.execute(jArrayStr, uri)
                preferences!!.syncDate = Calendar.getInstance().time.time
            },
            Response.ErrorListener { error ->
                DebugLog.logE("Error: " + error.message)
                val response = error.networkResponse
                if (response != null) {
                    when (response.statusCode) {
                        404 -> DebugLog.logE("Error 404")
                        401 -> DebugLog.logE("Error 401")
                        400 -> DebugLog.logE("Error 400")
                        else -> {
                        }
                    }
                }
            }) {
            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer " + preferences!!.token
                return map
            }
        }
        val socketTimeout = 60000
        val policy: RetryPolicy =
            DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        request.retryPolicy = policy
        ApplicationContext.instance!!.addToRequestQueue(request)
    }

    @Throws(JSONException::class)
    private fun sendReportToServer() {
        @SuppressLint("Recycle") val cursor =
            contentResolver.query(Constantes.CONTENT_PERSON_REPORT_URI, null, null, null, null)
        if (cursor != null && cursor.count > 0) {
            DebugLog.log("Count Reports saved: " + cursor.count)
            val dataColumn = cursor.getColumnIndex(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_DATA)
            val idColumn = cursor.getColumnIndex(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_ID)
            val imagesColumn = cursor.getColumnIndex(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_IMAGES)
            val methodColumn = cursor.getColumnIndex(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_METHOD)
            val serverIdColumn = cursor.getColumnIndex(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_SERVER_ID)
            val urlColumn = cursor.getColumnIndex(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_URL)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val _id = cursor.getInt(idColumn)
                val data = cursor.getString(dataColumn)
                DebugLog.log("Report personal _id to send: $_id")
                val url = cursor.getString(urlColumn)
                val images = cursor.getString(imagesColumn)
                val method = cursor.getInt(methodColumn)
                val serverId = cursor.getInt(serverIdColumn)
                if (data.isNotEmpty()) {
                    val jsonObject = JSONObject(data)
                    DebugLog.logW("jsonObject: $jsonObject")
                    sendReportRequest(url, jsonObject, _id, method, images, serverId)
                } else sendReportRequest(url, JSONObject(), _id, method, images, serverId)
                cursor.moveToNext()
            }
        } else {
            DebugLog.log("Not reports to send")
        }
        cursor?.close()
    }
    @Throws(JSONException::class)
    private fun sendImageToIndivudalContractToServer() {
        @SuppressLint("Recycle") val cursor =
            contentResolver.query(Constantes.CONTENT_UPDATE_IMAGE_URI, null, null, null, null)
        if (cursor != null && cursor.count > 0) {
            DebugLog.log("Count Reports saved: " + cursor.count)
            val dataColumn = cursor.getColumnIndex(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_DATA)
            val idColumn = cursor.getColumnIndex(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_ID)
            val imagesColumn = cursor.getColumnIndex(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_IMAGES)
            val methodColumn = cursor.getColumnIndex(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_METHOD)
            val serverIdColumn = cursor.getColumnIndex(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_SERVER_ID)
            val urlColumn = cursor.getColumnIndex(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_URL)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val _id = cursor.getInt(idColumn)
                val data = cursor.getString(dataColumn)
                DebugLog.log("Report personal _id to send: $_id")
                val url = cursor.getString(urlColumn)
                val images = cursor.getString(imagesColumn)
                val method = cursor.getInt(methodColumn)
                val serverId = cursor.getString(serverIdColumn)
                sendImageToIndividualContractRequest(url, JSONObject(), _id, method, images, serverId)
                cursor.moveToNext()
            }
        } else {
            DebugLog.log("Not reports to send")
        }
        cursor?.close()
    }

    private fun sendReportRequest(
        uri: String,
        jsonObject: JSONObject,
        _id: Int,
        method: Int,
        images: String,
        serverId: Int
    ) {
        var uri = uri
        val initialTime = System.currentTimeMillis()
        if (preferences == null) preferences = MyPreferences(this)
        if (uri.isEmpty()) uri = Constantes.PERSONAL_REPORT_URL
        var url = preferences!!.urlServer + uri
        DebugLog.logW("url: $url")
        if (method == Request.Method.PUT && !images.isEmpty()) {
            url += serverId.toString()
            sendImages(images, url, _id)
            return
        }
        val finalUrl = url
        val request: JsonRequest<*> = object : JsonObjectRequest(method, url, jsonObject,
            Response.Listener { //logW(jarrayObject);
                val seconds = (System.currentTimeMillis() - initialTime) / 1000
                DebugLog.logW(
                    String.format(
                        "Total time request %02d:%02d",
                        seconds / 60,
                        seconds % 60
                    )
                )
                if (method == Method.POST && images.isEmpty()) {
                    val uri =
                        Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL_REPORT + "/" + _id)
                    val count = contentResolver.delete(uri, null, null)
                    DebugLog.logW(
                        """
                            Report personal _id deleted: $_id,
                            affected columns: $count
                            """.trimIndent()
                    )
                } else if (method == Method.PUT && !images.isEmpty()) {
                    sendImages(images, finalUrl, _id)
                }
            },
            Response.ErrorListener { error -> DebugLog.logE("Error: " + error.message) }) {
            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer " + preferences!!.token
                return map
            }
        }
        val socketTimeout = 60000
        val policy: RetryPolicy =
            DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        request.retryPolicy = policy
        ApplicationContext.instance!!.addToRequestQueue(request)
    }

    private fun sendImages(images: String, url: String, _localId: Int) {
//        String newUrl = url + "UploadImage/" + id;
        val imagesArray = images.split(",".toRegex()).toTypedArray()
        val imagesList = ArrayList(Arrays.asList(*imagesArray))
        for (imagePath in imagesList) {
            val params = HashMap<String, String>()
            params["file"] = imagePath
            jsonMultiPartRequest(url, params, _localId, imagePath)
        }
    }
    private fun sendImagesIndividualContract(images: String, url: String, _localId: Int) {
//        String newUrl = url + "UploadImage/" + id;
        val imagesArray = images.split(",".toRegex()).toTypedArray()
        val imagesList = ArrayList(Arrays.asList(*imagesArray))
        for (imagePath in imagesList) {
            val params = HashMap<String, String>()
            params["file"] = imagePath
            jsonMultiPartRequestImageIndividualContract(url, params, _localId, imagePath)
        }
    }
    private fun sendImageToIndividualContractRequest(
        uri: String,
        jsonObject: JSONObject,
        _id: Int,
        method: Int,
        images: String,
        serverId: String
    ) {
        var uri = uri
        val initialTime = System.currentTimeMillis()
        if (preferences == null) preferences = MyPreferences(this)
        var url = preferences!!.urlServer + uri
        DebugLog.logW("url: $url")
        if (method == Request.Method.PUT && !images.isEmpty()) {
            DebugLog.logW("MEtodo put0")
            url += serverId
            sendImagesToIndividualContract(images, url, _id)
            return
        }
        val finalUrl = url
        val request: JsonRequest<*> = object : JsonObjectRequest(method, url, jsonObject,
            Response.Listener { //logW(jarrayObject);
                val seconds = (System.currentTimeMillis() - initialTime) / 1000
                DebugLog.logW(
                    String.format(
                        "Total time request %02d:%02d",
                        seconds / 60,
                        seconds % 60
                    )
                )
                if (method == Method.POST && images.isEmpty()) {
                    val uri =
                        Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_UPDATE_IMAGE + "/" + _id)
                    val count = contentResolver.delete(uri, null, null)
                    DebugLog.logW(
                        """
                            Report personal _id deleted: $_id,
                            affected columns: $count
                            """.trimIndent()
                    )
                } else if (method == Method.PUT && !images.isEmpty()) {
                    DebugLog.logW("MEtodo put")
                    sendImagesIndividualContract(images, finalUrl, _id)
                }
            },
            Response.ErrorListener { error -> DebugLog.logE("Error: " + error.message) }) {
            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer " + preferences!!.token
                return map
            }
        }
        val socketTimeout = 60000
        val policy: RetryPolicy =
            DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        request.retryPolicy = policy
        ApplicationContext.instance!!.addToRequestQueue(request)
    }

    private fun sendImagesToIndividualContract(images: String, url: String, _localId: Int) {
//        String newUrl = url + "UploadImage/" + id;
        val imagesArray = images.split(",".toRegex()).toTypedArray()
        val imagesList = ArrayList(Arrays.asList(*imagesArray))
        for (imagePath in imagesList) {
            val params = HashMap<String, String>()
            params["file"] = imagePath
            jsonMultiPartRequestImageIndividualContract(url, params, _localId, imagePath)
        }
    }

    private fun jsonMultiPartRequest(
        url: String, params: Map<String, String>, _localId: Int,
        imagePath: String
    ) {

        // String url = preferences.getUrlServer() + uri;
        DebugLog.logW("url: $url")
        val multipartRequest: VolleyMultipartRequest = object :
            VolleyMultipartRequest(Method.PUT, url, Response.Listener { response ->
                val resultResponse = String(response.data)
                DebugLog.log(resultResponse)
                contentResolver.query(
                    Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL_REPORT + "/" + _localId),
                    null, null, null, null
                ).use { c ->
                    if (c != null && c.count > 0) {
                        c.moveToPosition(0)
                        val imagesColumn = c.getColumnIndex(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_IMAGES)
                        val images = c.getString(imagesColumn)
                        val imagesArray = images.split(",".toRegex()).toTypedArray()
                        val imagesList =
                            ArrayList(Arrays.asList(*imagesArray))
                        val it = imagesList.iterator()
                        while (it.hasNext()) {
                            if (it.next() == imagePath) {
                                it.remove()
                                break
                            }
                        }
                        if (imagesList.size > 0) {
                            val imagesStr = TextUtils.join(",", imagesList)
                            DebugLog.log("imagesStr: $imagesStr")
                            val uriUpdate =
                                Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL_REPORT + "/" + _localId)
                            val cv = ContentValues()
                            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_IMAGES, imagesStr)
                            val count = contentResolver.update(uriUpdate, cv, null, null)
                            DebugLog.logW(
                                """
                                Report _id updated: $_localId,
                                affected columns: $count
                                """.trimIndent()
                            )
                        } else {
                            val uriDelete =
                                Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_PERSONAL_REPORT + "/" + _localId)
                            val count = contentResolver.delete(uriDelete, null, null)
                            DebugLog.logW(
                                """
                                Report _id deleted: $_localId,
                                affected columns: $count
                                """.trimIndent()
                            )
                        }
                    }
                }
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                var errorMessage = "Unknown error"
                if (networkResponse == null) {
                    if (error.javaClass == TimeoutError::class.java) {
                        errorMessage = "Request timeout"
                    }
                } else {
                    errorMessage = String(networkResponse.data)
                }
                DebugLog.logE("Error: $errorMessage")
                error.printStackTrace()
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer " + preferences!!.token
                return map
            }

            override fun getParams(): Map<String, String> {
                return params
            }

            override fun getByteData(): Map<String, DataPart> {
                val paramsImage: MutableMap<String, DataPart> = HashMap()
                for (key in params.keys) {
                    when (key) {
                        "file", "image" -> {
                            val imageUri = Uri.parse(params[key])
                            val readOnlyMode = "r"
                            try {
                                contentResolver.openFileDescriptor(imageUri, readOnlyMode).use { pfd ->
                                    pfd?.let {
                                        val fileDescriptor =
                                            Objects.requireNonNull(pfd).fileDescriptor
                                        val compressedImageBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                                        val nameImage = (System.currentTimeMillis() / 1000).toString()
                                        paramsImage.put(
                                            key, DataPart(
                                                nameImage + "_image.jpg",
                                                MetodosPublicos.getBitmapAsByteArray(compressedImageBitmap)
                                            )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                return paramsImage
            }
        }
        val socketTimeout = 120000
        val policy: RetryPolicy =
            DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        multipartRequest.retryPolicy = policy
        ApplicationContext.instance!!.addToRequestQueue(multipartRequest)
    }
    private fun jsonMultiPartRequestImageIndividualContract(
            url: String, params: Map<String, String>, _localId: Int,
            imagePath: String
    ) {

        // String url = preferences.getUrlServer() + uri;
        DebugLog.logW("url: $url")
        val multipartRequest: VolleyMultipartRequest = object :
                VolleyMultipartRequest(Method.PUT, url, Response.Listener { response ->
                    val resultResponse = String(response.data)
                    DebugLog.log(resultResponse)
                    contentResolver.query(
                            Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_UPDATE_IMAGE + "/" + _localId),
                            null, null, null, null
                    ).use { c ->
                        if (c != null && c.count > 0) {
                            c.moveToPosition(0)
                            val imagesColumn = c.getColumnIndex(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_IMAGES)
                            val images = c.getString(imagesColumn)
                            val imagesArray = images.split(",".toRegex()).toTypedArray()
                            val imagesList =
                                    ArrayList(Arrays.asList(*imagesArray))
                            val it = imagesList.iterator()
                            while (it.hasNext()) {
                                if (it.next() == imagePath) {
                                    it.remove()
                                    break
                                }
                            }
                            if (imagesList.size > 0) {
                                val imagesStr = TextUtils.join(",", imagesList)
                                DebugLog.log("imagesStr: $imagesStr")
                                val uriUpdate =
                                        Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_UPDATE_IMAGE + "/" + _localId)
                                val cv = ContentValues()
                                cv.put(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_IMAGES, imagesStr)
                                val count = contentResolver.update(uriUpdate, cv, null, null)
                                DebugLog.logW(
                                        """
                                Report _id updated: $_localId,
                                affected columns: $count
                                """.trimIndent()
                                )
                            } else {
                                val uriDelete =
                                        Uri.parse("content://" + Constantes.AUTHORITY + "/" + DBHelper.TABLE_NAME_UPDATE_IMAGE + "/" + _localId)
                                val count = contentResolver.delete(uriDelete, null, null)
                                DebugLog.logW(
                                        """
                                Report _id deleted: $_localId,
                                affected columns: $count
                                """.trimIndent()
                                )
                            }
                        }
                    }
                }, Response.ErrorListener { error ->
                    val networkResponse = error.networkResponse
                    var errorMessage = "Unknown error"
                    if (networkResponse == null) {
                        if (error.javaClass == TimeoutError::class.java) {
                            errorMessage = "Request timeout"
                        }
                    } else {
                        errorMessage = String(networkResponse.data)
                    }
                    DebugLog.logE("Error: $errorMessage")
                    error.printStackTrace()
                }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val map = HashMap<String, String>()
                map["Authorization"] = "Bearer " + preferences!!.token
                return map
            }

            override fun getParams(): Map<String, String> {
                return params
            }

            override fun getByteData(): Map<String, DataPart> {
                val paramsImage: MutableMap<String, DataPart> = HashMap()
                for (key in params.keys) {
                    when (key) {
                        "file", "image" -> {
                            val imageUri = Uri.parse(params[key])
                            val readOnlyMode = "r"
                            try {
                                contentResolver.openFileDescriptor(imageUri, readOnlyMode).use { pfd ->
                                    pfd?.let {
                                        val fileDescriptor =
                                                Objects.requireNonNull(pfd).fileDescriptor
                                        val compressedImageBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                                        val nameImage = (System.currentTimeMillis() / 1000).toString()
                                        paramsImage.put(
                                                key, DataPart(
                                                nameImage + "_image.jpg",
                                                MetodosPublicos.getBitmapAsByteArray(compressedImageBitmap)
                                        )
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
                return paramsImage
            }
        }
        val socketTimeout = 120000
        val policy: RetryPolicy =
                DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        multipartRequest.retryPolicy = policy
        ApplicationContext.instance!!.addToRequestQueue(multipartRequest)
    }
    companion object {
        private const val EXTRA_UPDATE_DB = "co.sersoluciones.apps.services.extra.UPDATE_DB"
        @JvmStatic
        fun startRequest(context: Context, updateDb: Boolean) {
            val intent = Intent(context, UpdateDBService::class.java)
            intent.putExtra(EXTRA_UPDATE_DB, updateDb)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}