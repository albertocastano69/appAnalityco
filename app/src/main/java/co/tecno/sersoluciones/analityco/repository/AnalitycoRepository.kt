package co.tecno.sersoluciones.analityco.repository

import androidx.work.WorkManager
import co.tecno.sersoluciones.analityco.models.Reporte
import co.tecno.sersoluciones.analityco.models.SurveySymptom
import co.tecno.sersoluciones.analityco.retrofit.AnalitycoApiService
import co.tecno.sersoluciones.analityco.room.AnalitycoDao
import com.google.gson.JsonObject
import com.google.gson.JsonParser.parseString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

enum class ApiStatus {
    ERROR, DONE
}

@Singleton
class AnalitycoRepository @Inject constructor(
    private val analitycoDao: AnalitycoDao,
    private val workManager: WorkManager,
    private val apiService: AnalitycoApiService
) {

    suspend fun insertReporte(reporte: Reporte) {
        return withContext(Dispatchers.IO) {
            analitycoDao.insertReporte(reporte)
        }
    }
    suspend fun insertSurveySimptom(surveys: SurveySymptom){
        return withContext(Dispatchers.IO){
            analitycoDao.insertSurveyOffline(surveys)
        }
    }

    suspend fun deleteReporte(id: Long) {
        withContext(Dispatchers.IO) {
            analitycoDao.deleteReporte(id)
        }
    }
    suspend fun deleteReportSurvey(id: Long){
        withContext(Dispatchers.IO){
            analitycoDao.deleteReporteSurvey(id)
        }
    }

    suspend fun getAllReportes(): List<Reporte> {
        return withContext(Dispatchers.IO) {
            analitycoDao.getAllReportes()
        }
    }

    suspend fun getReportSurvey(): List<SurveySymptom>{
        return  withContext(Dispatchers.IO){
            analitycoDao.getReportSurvey()
        }
    }

    suspend fun sendSingleReport(data: String) {
        withContext(Dispatchers.IO) {
            val jsonObject: JsonObject = parseString(data).asJsonObject
            val body: RequestBody = jsonObject.toString().toRequestBody()
            apiService.sendReporteAsync(body).await()
        }
    }

    suspend fun sendReporte(reporte: Reporte): ApiStatus {
        if (reporte.data.isEmpty()) return ApiStatus.DONE
        return withContext(Dispatchers.IO) {
            val jsonObject: JsonObject = parseString(reporte.data).asJsonObject
            val body: RequestBody = jsonObject.toString().toRequestBody()
            try {
                apiService.sendReporteAsync(body).await()
                ApiStatus.DONE
            } catch (e: Exception) {
                ApiStatus.ERROR
            }
        }
    }
    suspend fun sendSurverServer(reporteSurvey : SurveySymptom): ApiStatus{
        if(reporteSurvey.data.isEmpty())  return  ApiStatus.DONE
        return  withContext(Dispatchers.IO){
            val jsonObject : JsonObject = parseString(reporteSurvey.data).asJsonObject
            val body : RequestBody = jsonObject.toString().toRequestBody()
            try {
                apiService.sendReportSurvey(body).await()
                ApiStatus.DONE
            }catch (e: Exception){
                ApiStatus.ERROR
            }
        }
    }


}