package co.tecno.sersoluciones.analityco.adapters.repositories

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.work.*
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logW
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databases.InnovoDao
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.nav.getContentVPersonal
import co.tecno.sersoluciones.analityco.retrofit.AnalitycoApiService
import co.tecno.sersoluciones.analityco.room.AnalitycoDao
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.Utils
import co.tecno.sersoluciones.analityco.worker.SendMsgWorker
import co.tecno.sersoluciones.analityco.worker.SendReportWorker
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList


@Singleton
class EnrollmentRepository @Inject constructor(
        private val analitycoDao: AnalitycoDao,
        private val analitycoApiService: AnalitycoApiService,
        private val application: Application,
        private val workManager: WorkManager,
        private val innovoDao: InnovoDao
) {

    val reports = analitycoDao.liveReports()

    suspend fun fetchDailyPersonal() {
        withContext(Dispatchers.IO) {

            var listPersonalIsEmpty = false
            application.contentResolver
                .query(Constantes.CONTENT_PERSONAL_URI, null, null, null, null)
                .use { cursor ->
                    listPersonalIsEmpty = cursor == null || cursor.count == 0
                }
            log("listPersonalIsEmpty $listPersonalIsEmpty")
            if (listPersonalIsEmpty) return@withContext
            val arrayPersonalListDB = getPersonalListDb()
            val list = analitycoApiService.getPersonalRealTimeAsync().await()
            list.forEach { personalRealTime ->
                if (!arrayPersonalListDB.filter { x -> x.PersonalCompanyInfoId.toLong() == personalRealTime.personalId }.any()) {
                    log("personalRealTime to insert in personalList DB ${personalRealTime.personalId} ${personalRealTime.name} ")
                    val personalNetwork =
                        analitycoApiService.getPersonalInfoIdAsync(personalRealTime.documentNumber!!, personalRealTime.companyInfoId!!).await()
                    personalNetwork?.let {
                        log("personal to insert ${Gson().toJson(personalNetwork)}")
                        innovoDao.insertPersonal(getContentVPersonal(personalNetwork))

                        val listPersonalOffline = analitycoApiService.getPersonalContractOfflineAsync(
                                personalCompanyInfoId = personalRealTime.personalId,
                                project = true
                        ).await()
                        replacePersonalInfo(listPersonalOffline)
                    }

                }
            }
            analitycoDao.insertAllPersonalRealTime(*list.toTypedArray())
        }
    }

    suspend fun sendPersonalRealTime(personalRealTime: PersonalRealTime) {
        withContext(Dispatchers.IO) {
            analitycoApiService.createPersonalRealTimeAsync(personalRealTime).await()
        }
    }

    val personals: LiveData<List<PersonalRealTime>> = analitycoDao.getAllPersonalRealTimes()

    suspend fun getPersonalRealTimeDB(personalId: Long): PersonalRealTime? {
        return withContext(Dispatchers.IO) {
            analitycoDao.getPersonalRealTime(personalId)
        }
    }

    suspend fun insertPersonalRealTime(personalRealTime: PersonalRealTime) {
        withContext(Dispatchers.IO) {
            analitycoDao.insertPersonalRealTime(personalRealTime)
        }
    }

    private fun getPersonalListDb(): MutableList<PersonalList> {
        val arrayPersonal: MutableList<PersonalList> = ArrayList()
        val selection = ("( " + DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE + " = ?)")
        val selectionArgs = arrayOf("1")
        application.contentResolver
            .query(Constantes.CONTENT_PERSONAL_URI, null, selection, selectionArgs, null)
            .use { cursor ->
                if (cursor != null) {
                    val nameColumn = cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_NAME)
                    val lastNameColumn = cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_LASTNAME)
                    val photoColumn = cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_PHOTO)
                    val personalId = cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_SERVER_ID)
                    val companyInfoIdColumn = cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_COMPANY_INFO_ID)
                    val documentColumn = cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_DOC_NUM)
                    val isActive = cursor.getColumnIndex(DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE)

                    while (cursor.moveToNext()) {
                        arrayPersonal.add(
                                PersonalList(
                                        cursor.getInt(personalId),
                                        cursor.getString(nameColumn),
                                        cursor.getString(lastNameColumn),
                                        cursor.getString(documentColumn),
                                        cursor.getString(companyInfoIdColumn),
                                        cursor.getString(photoColumn),
                                        cursor.getInt(isActive) == 1
                                )
                        )

                    }
                }
            }
        return arrayPersonal
    }

    suspend fun getPersonalByProject(projectId: String): List<PersonalRealTime>? {

        return withContext(Dispatchers.IO) {

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.HOUR_OF_DAY, 0)

            val currentDate = calendar.time
            var updateDailyTable = true
            val listPersonalDb = analitycoDao.getPersonalRealTimeByProject(projectId)
            listPersonalDb?.let {
                if (listPersonalDb.isNotEmpty()) {
                    val personal = listPersonalDb.firstOrNull()
//                    log("currenDate ${currenDate.time} personal.timestamp ${personal?.timestamp} projectId ${projectId}")
                    if (personal != null && personal.timestamp >= currentDate.time)
                        updateDailyTable = false
                }
            }

            val arrayPersonal = getPersonalListDb()
            logW("updateDailyTable $updateDailyTable count ${arrayPersonal.size}")
            val rightNow = Calendar.getInstance()
            val arrayPersonalRealTime = ArrayList<PersonalRealTime>()
            val selection = ("( " + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ?)")
            val selectionArgs = arrayOf(projectId)
            application.contentResolver
                .query(
                        Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI,
                        null,
                        selection,
                        selectionArgs,
                        null
                )
                ?.use { cursor ->
                    val personalIdColumn =
                        cursor.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID)
                    while (cursor.moveToNext()) {
                        val personalId = cursor.getInt(personalIdColumn)
                        val personalList =
                            arrayPersonal.firstOrNull { x -> x.PersonalCompanyInfoId == personalId }
                        if (personalList != null) {

                            var startDateStr =
                                cursor.getString(cursor.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE))
                            var finishDateStr =
                                cursor.getString(cursor.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE))
                            val startDateContract =
                                cursor.getString(cursor.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_START_DATE_CONTRACT))
                            val finishDateContract =
                                cursor.getString(cursor.getColumnIndex(DBHelper.CONTRACT_PERSON_TABLE_COLUMN_FINISH_DATE_CONTRACT))
                            if (startDateStr.isNullOrEmpty() || startDateStr == "null")
                                startDateStr = startDateContract

                            if (finishDateStr.isNullOrEmpty() || finishDateStr == "null")
                                finishDateStr = finishDateContract

                            @SuppressLint("SimpleDateFormat")
                            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            val startDate = format.parse(startDateStr)
                            val finishDate = format.parse(finishDateStr)

                            if (rightNow.time.after(startDate))
                                if (rightNow.time.before(finishDate))
                                    arrayPersonalRealTime.add(
                                            PersonalRealTime(
                                                    personalId = personalId.toLong(),
                                                    name = personalList.Name,
                                                    lastName = personalList.LastName,
                                                    avatar = personalList.Photo ?: "",
                                                    projectId = projectId,
                                                    companyInfoId = personalList.CompanyInfoId,
                                                    documentNumber = personalList.DocumentNumber
                                            )
                                    )


                        }
                    }
                }

//            log("arrayPersonalRealTime count ${arrayPersonalRealTime.size} listPersonalDb count ${listPersonalDb?.size}")
            if (arrayPersonalRealTime.size != listPersonalDb!!.size) {
                arrayPersonalRealTime.forEach {
                    if (!listPersonalDb.filter { x -> x.personalId == it.personalId }.any()) {
                        analitycoDao.insertPersonalRealTime(it)
                    }
                }
            }

            //remove personal not exist
//            listPersonalDb.forEach {
//                if (!arrayPersonal.filter { x -> x.PersonalCompanyInfoId.toLong() == it.personalId }.any()) {
//                    log("remove personal db real time ${it.personalId} ${it.name} ")
//                    analitycoDao.removePersonalRealTime(it.personalId)
//                }
//            }

            if (updateDailyTable) {
                analitycoDao.insertAllPersonalRealTime(*arrayPersonalRealTime.toTypedArray())
            }

            orderListPersonal(projectId)
        }
    }

    suspend fun removeAllPersonalRealTime() {
        withContext(Dispatchers.IO) {
            analitycoDao.removeAllPersonalRealTime()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
     fun LoadProjectSync(id: String?, Name: String?, Date: String?){
            innovoDao.loadProjectSyn(id, Name, Date)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun LoadPersonalSync(personalCompanyInfoId: Int?){
        innovoDao.loadpersonaltSyn(personalCompanyInfoId.toString())
    }

    suspend fun getPersonalRealTimeByProject(projectId: String): List<PersonalRealTime>? {
        return withContext(Dispatchers.IO) {
            orderListPersonal(projectId)
        }
    }

    private fun orderListPersonal(projectId: String): List<PersonalRealTime>? {
        return analitycoDao.getPersonalRealTimeByProject(projectId)
            ?.sortedWith(compareByDescending<PersonalRealTime> { it.lastTime }
                    .thenByDescending { it.getTime })
    }

    suspend fun updateRegister(personalRealTime: PersonalRealTime) {
        withContext(Dispatchers.IO) {
            var findPersonal = true
            val selection = ("( " + DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID + " = ?)")
            val selectionArgs = arrayOf(personalRealTime.personalId.toString())
            application.contentResolver
                .query(Constantes.CONTENT_PERSONAL_URI, null, selection, selectionArgs, null)
                .use { cursor ->
                    findPersonal = cursor != null && cursor.count > 0
                }
            if (!findPersonal) {
                try {
                    val personalNetwork =
                        analitycoApiService.getPersonalInfoIdAsync(personalRealTime.documentNumber!!, personalRealTime.companyInfoId!!).await()
                    personalNetwork?.let {
                        log("personal to insert ${Gson().toJson(personalNetwork)}")
                        innovoDao.insertPersonal(getContentVPersonal(personalNetwork))

                        val list = analitycoApiService.getPersonalContractOfflineAsync(
                                personalCompanyInfoId = personalRealTime.personalId,
                                project = true
                        ).await()
                        replacePersonalInfo(list)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            analitycoDao.insertPersonalRealTime(personalRealTime)
        }
    }

    private fun replacePersonalInfo(personalContractOfflineList: List<PersonalContractOfflineNetwork>) {

        if (personalContractOfflineList.isNotEmpty()) {

            val personalCompanyInfo = personalContractOfflineList.first()
            val selection = (DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PERSONALCOMPANY_ID + " = ? ")

            val selectionArgs = arrayOf(personalCompanyInfo.PersonalCompanyInfoId.toString())
            var count = application.contentResolver.delete(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI, selection, selectionArgs
            )
            logW("Count delete $count")
            val jArray = JSONArray(Gson().toJson(personalContractOfflineList))
            logW("personalCompanyInfo.DocumentNumber ${personalCompanyInfo.DocumentNumber}")
            val contentValues = Utils.reflectToContentValues(jArray)
            count = application.contentResolver.bulkInsert(
                    Constantes.CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT,
                    contentValues
            )
            logW(String.format("CONTENT_CONTRACT_PER_OFFLINE_URI_BULK_INSERT count: %s", count))

        }
    }

    fun setReportWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val sendReportRequest = OneTimeWorkRequestBuilder<SendReportWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(SendReportWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, sendReportRequest)
    }

    fun createWorkRequest(stringObj: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val stringObj = Gson().toJson(personalRealTime)
        val stringArray: Array<String> = arrayOf(
                stringObj
        )

        val externalId = workDataOf(
                MSG_KEY to stringArray
        )

        val sendMsgRequest = OneTimeWorkRequestBuilder<SendMsgWorker>()
            .setConstraints(constraints)
            .setInputData(externalId)
            .build()

        workManager.enqueueUniqueWork(SendMsgWorker.TAG, ExistingWorkPolicy.APPEND, sendMsgRequest)

    }

    //surveys

    suspend fun fetchSurvey() {
        withContext(Dispatchers.IO) {
            val list = analitycoApiService.getSurveyListAsync().await()
            analitycoDao.insertAllSurveys(*list.map {
                Survey(
                        id = it.id,
                        isActive = it.isActive ?: false,
                        symptoms = it.symptoms?.joinToString(",") ?: "",
                        otherPersonHasSymptoms = it.otherPersonHasSymptoms ?: false,
                        otherPersonHasCOVID19 = it.otherPersonHasCOVID19 ?: false,
                        transport = it.transport?.joinToString(",") ?: "",
                        personalCompanyInfoId = it.personalCompanyInfoId ?: 0L,
                        createDate = it.createDate ?: "",
                        responseState = it.responseState!!.toInt() ?: 0
                )
            }.toTypedArray())
        }
    }
    suspend fun fetchSurveyOffline(data: ReportSurvey) {
        withContext(Dispatchers.IO) {
            println("CompanyId${data.PersonalCompanyInfoId}")
            analitycoDao.insertAllSurveys(Survey(
                    id = 0,
                    isActive = data.IsActive ?: false,
                    symptoms = data.Symptoms?.joinToString(",") ?: "",
                    otherPersonHasSymptoms = data.OtherPersonHasSymptoms ?: false,
                    otherPersonHasCOVID19 = data.OtherPersonHasCOVID19 ?: false,
                    transport = data.Transport?.joinToString(",") ?: "",
                    personalCompanyInfoId = data.PersonalCompanyInfoId ?: 0L,
                    createDate = data.CreateDate ?: "",
                    responseState = data.ResponseState ?: 0
            ))
        }
    }

    suspend fun getSurveyDB(surveyId: Long, CreateDate: String): Survey? {
        return withContext(Dispatchers.IO) {
            analitycoDao.getSurvey(surveyId)
        }
    }

    suspend fun getSurveyInfoDB(surveyId: Long, CreateDate: String):Survey? {
        return withContext(Dispatchers.IO) {
            analitycoDao.getSurveyInfo(surveyId)
        }
    }

    suspend fun getSurveyToken(document: String, pin: String): String {
        return withContext(Dispatchers.IO) {
            val request = analitycoApiService.getSurveyTokenAsync(
                    docNumber = document,
                    pin = pin,
                    clientId = "analityco_client"
            ).await()
            request.personalInfo?.token ?: ""
        }
    }

    suspend fun fetchPersonalContractOffline(personalCompanyInfoId: Long): List<PersonalContractOfflineNetwork> {
        return withContext(Dispatchers.IO) {
            val request =
                analitycoApiService.getPersonalContractOfflineAsync(
                        personalCompanyInfoId = personalCompanyInfoId,
                        project = true
                ).await()
            request
        }
    }

    suspend fun getAfIndicator(projectId: String): Long {
        return withContext(Dispatchers.IO) {
            val indicator = analitycoDao.getEntryCountDailyPersonal(projectId) -
                    analitycoDao.getExitCountDailyPersonal(projectId)
            indicator
        }
    }
    suspend fun personInorOut(document: String): Long{
        return withContext(Dispatchers.IO){
            val entry = analitycoDao.getRegisterEntry(document)
            entry
        }
    }

    suspend fun personOut(document: String): Long{
        return  withContext(Dispatchers.IO){
            val out = analitycoDao.getRegisterExit(document)
            out
        }
    }

    suspend fun inputStatus(document: String): Long{
        return  withContext(Dispatchers.IO){
            val value_input = analitycoDao.getInputStatus(document)
            value_input
        }
    }

    suspend fun removePersonalRealTime(projectId: String?) {
        withContext(Dispatchers.IO) {
            val selection = "(" + DBHelper.CONTRACT_PERSON_TABLE_COLUMN_PROJECT_ID + " = ? )"
            val selectionArgs = arrayOf(projectId)
            innovoDao.deletePersonalOfflineFromProject(selection, selectionArgs)
        }
    }
     fun updatedatecontract(values: ContentValues, whereArgs: Array<String?>){
            innovoDao.updagradePersonalOffilineFromProject(values, whereArgs)
    }

    suspend fun getPersonalById(personalCompanyInfoId: Long): PersonalList? {
        return withContext(Dispatchers.IO) {
            val selection = "(" + DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID + " = ? )"
            val selectionArgs = arrayOf(personalCompanyInfoId.toString())
            innovoDao.getPersonal(selection, selectionArgs)
        }
    }

    suspend fun getCountRegistersDB(): Int {
        return withContext(Dispatchers.IO) {
            analitycoDao.getCountRegistersDB()
        }
    }

    companion object {
        const val MSG_KEY = "msgKey"
    }
}