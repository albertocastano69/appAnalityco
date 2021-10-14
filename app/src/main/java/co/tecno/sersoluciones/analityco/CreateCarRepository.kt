package co.tecno.sersoluciones.analityco

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.tecno.sersoluciones.analityco.databases.InnovoDao
import co.tecno.sersoluciones.analityco.models.CompanyList
import co.tecno.sersoluciones.analityco.models.Position
import co.tecno.sersoluciones.analityco.models.ProjectList
import co.tecno.sersoluciones.analityco.retrofit.AnalitycoApiService
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import com.android.volley.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateCarRepository @Inject constructor(
        private val innovoDao: InnovoDao,
        private val service: AnalitycoApiService,
        private val mContext: Context
) {

    private var data: LinkedHashMap<Uri, String>? = null
    private var fillArray: BooleanArray = booleanArrayOf()

    suspend fun verifyInitialDataInDB(): Boolean {
        return withContext(Dispatchers.IO) {
            val launchRequest = BooleanArray(3)

            data = LinkedHashMap()
            data!![Constantes.CONTENT_CONTRACT_URI] = Constantes.LIST_CONTRACTS_URL
            data!![Constantes.CONTENT_EMPLOYER_URI] = Constantes.LIST_EMPLOYERS_URL
            data!![Constantes.CONTENT_PROJECT_URI] = Constantes.LIST_PROJECTS_URL
            fillArray = BooleanArray(data!!.size)

            val values = ContentValues()
            values.put(Constantes.KEY_SELECT, true)
            val paramsQuery = HttpRequest.makeParamsInUrl(values)
            var pos = 0
            for (o in data!!.entries) {
                val pair = o as Map.Entry<*, *>
                mContext.contentResolver.query(
                        (pair.key as Uri?)!!, null, null, null,
                        null
                ).use { cursor ->
                    if (cursor != null && cursor.count == 0) {
                        if (!launchRequest[pos])
                            CRUDService.startRequest(mContext, pair.value as String?, Request.Method.GET, paramsQuery, true)
                        launchRequest[pos] = true
                    } else {
                        fillArray[pos] = true
                    }
                    DebugLog.logW(pos.toString() + ". URL to Request: " + pair.value + ": " + fillArray[pos])
                    pos++
                }
            }

            var flag = true
            for (item in fillArray) {
                if (!item) {
                    flag = false
                    break
                }
            }

            flag
        }
    }

    suspend fun fetchPersonalPositions(companyInfoId: String): List<Position> {
        return withContext(Dispatchers.IO) {
            service.getPersonalPositionsAsync(companyInfoId).await()
        }
    }

    suspend fun getProjects(
            columns: Array<String>? = null, selection: String? = null, selectionArgs: Array<String>? = null, orderBy: String? = null
    ): List<ProjectList> {
        return withContext(Dispatchers.IO) {
            innovoDao.selectAllProjects(columns = columns, selection = selection, selectionArgs = selectionArgs, orderBy = orderBy)
        }
    }
}