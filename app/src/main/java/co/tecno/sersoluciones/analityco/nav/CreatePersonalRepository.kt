package co.tecno.sersoluciones.analityco.nav

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.text.TextUtils
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog
import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.log
import co.tecno.sersoluciones.analityco.adapters.repositories.PositionIndividualContract
import co.tecno.sersoluciones.analityco.databases.DBHelper
import co.tecno.sersoluciones.analityco.databases.InnovoDao
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.retrofit.AnalitycoApiService
import co.tecno.sersoluciones.analityco.services.CRUDService
import co.tecno.sersoluciones.analityco.services.UpdateDBService
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import com.android.volley.Request
import com.google.android.libraries.places.api.model.Place
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreatePersonalRepository @Inject constructor(
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

    suspend fun getProjects(
            columns: Array<String>? = null, selection: String? = null, selectionArgs: Array<String>? = null, orderBy: String? = null
    ): List<ProjectList> {
        return withContext(Dispatchers.IO) {
            innovoDao.selectAllProjects(columns = columns, selection = selection, selectionArgs = selectionArgs, orderBy = orderBy)
        }
    }

    suspend fun fetchPersonalPositions(companyInfoId: String): List<Position> {
        return withContext(Dispatchers.IO) {
            service.getPersonalPositionsAsync(companyInfoId).await()
        }
    }

    suspend fun fetchPersonal(doc: String, companyInfoId: String): PersonalNetwork? {
        return withContext(Dispatchers.IO) {
            service.getPersonalInfoIdAsync(doc, companyInfoId).await()
        }
    }
    suspend fun foundContactPerson(personalEmployerInfoId : String){
        return withContext(Dispatchers.IO){
            service.getPersonalAssociateInContractAsync(personalEmployerInfoId)
        }
    }

    suspend fun findPersonalToIndividualContract(docType:String,doc: String): PersonalNetwork? {
        return withContext(Dispatchers.IO) {
            service.getPersonalInfoToIndivudalContractIdAsync(docType, doc).await()
        }
    }

    suspend fun findDataToContact(personalId: String?, EmployerId: String?): PersonalNetworkData? {
        return withContext(Dispatchers.IO) {
            service.getDataToContactAsync(personalId, EmployerId).await()
        }
    }

    suspend fun fetchPersonalContract(personalCompanyInfoId: Int, contractId: String): PersonalContract {
        return withContext(Dispatchers.IO) {
            service.getPersonalContractAsync(contractId, personalCompanyInfoId.toString()).await()
        }
    }

    suspend fun fetchContractsByPersonal(personalCompanyInfoId: String): List<ContractEnrollment> {
        return withContext(Dispatchers.IO) {
            service.getContractsAsync(personalCompanyInfoId).await()
        }
    }
    suspend fun PlaceJob(employerId: String?) : List<PlaceToJob>{
        return  withContext(Dispatchers.IO){
                service.getPlaceToJobAsync(employerId).await()
        }
    }

    suspend fun createPersonal(personal: PersonalNew): PersonalNetwork? {
        return withContext(Dispatchers.IO) {
            val personalNetwork = service.createPersonalAsync(personal).await()

            log("personal to insert ${Gson().toJson(personalNetwork)}")
            innovoDao.insertPersonal(getContentVPersonal(personalNetwork))
            personalNetwork
        }
    }
    suspend fun createPersonalToIndividualContract(personal: PersonalNew): PersonalIndividualContract? {
        return withContext(Dispatchers.IO) {
            val personalNetwork = service.createPersonalAsyncToIndividualContractAsync(personal).await()
            log("personal to insert ${Gson().toJson(personalNetwork)}")
            personalNetwork
        }
    }
    suspend fun updatePersonalToIndividualContract(personal: PersonalNew): PersonalIndividualContract? {
        return withContext(Dispatchers.IO) {
            val personalNetwork = service.updatePersonalAsyncToIndividualContractAsync(personal).await()
            log("personal to insert ${Gson().toJson(personalNetwork)}")
            personalNetwork
        }
    }
    suspend fun RequestOrder(json: String): Any {
        return withContext(Dispatchers.IO){
            service.RequestORderAsync(json).await()
        }
    }

    suspend fun createContactPersonalToIndividualContract(personalContact: PersonalDataContact): PersonalIndividualContract? {
        return withContext(Dispatchers.IO) {
            val personalNetwork = service.createContactPersonalAsyncToIndividualContractAsync(personalContact).await()
            log("personal contact to insert ${Gson().toJson(personalNetwork)}")
            personalNetwork
        }
    }
    suspend fun UpdateContactPersonalToIndividualContract(personalContact: PersonalDataContact,IdPersonal : String): PersonalIndividualContract?{
        return  withContext(Dispatchers.IO){
            val personalNetwork = service.UpdateContactPersonalAsyncToIndividualContractAsync(personalContact,IdPersonal).await()
            log("personal contact to insert ${Gson().toJson(personalNetwork)}")
            personalNetwork
        }
    }
    suspend fun updatePersonal(personal: UpdatePersonalInfo, personalInfoId: Int): PersonalNetwork? {
        return withContext(Dispatchers.IO) {
            val personalNetwork = service.updatePersonalAsync(personal, personalInfoId.toString()).await()
            val selection = "(" + DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID + " = ? )"
            val selectionArgs = arrayOf(personalInfoId.toString())
            innovoDao.updatePersonal(getContentVPersonal(personalNetwork), selection, selectionArgs)
            personalNetwork
        }
    }

    suspend fun UpdateApiPerson(personal: PersonalNew): PersonalIndividualContract?{
        return withContext(Dispatchers.IO){
            val personalNetwork = service.UpdateApiPerson(personal).await()
            personalNetwork
        }
    }


    suspend fun isPersonalInContractAsync(contractId: String, personalCompanyInfoId: Int): Any {
        return withContext(Dispatchers.IO) {
            service.isPersonalInContractAsync(contractId, personalCompanyInfoId.toString()).await()
        }
    }
    suspend fun getPositions(EmployerId: String?):List<PositionIndividualContract>{
        return  withContext(Dispatchers.IO){
            service.getPositionsAsync(EmployerId).await()
        }
    }
    suspend fun getDaneCity():List<DaneCity>{
        return withContext(Dispatchers.IO){
            service.getDaneCity().await()
        }
    }
    suspend fun getCityPerson(): List<City>{
        return withContext(Dispatchers.IO){
            service.getCityPersonAsync().await()
        }
    }
    suspend fun getCityForNationality(countryCode: Int) :List<City>{
        return withContext(Dispatchers.IO){
            service.getCityPersonForNationalityAsync(countryCode).await()
        }
    }
    suspend fun createPersonalContract(personal: PersonalContract, contractId: String, path: String): Any? {
        return withContext(Dispatchers.IO) {
            service.createPersonalContractAsync(personal, contractId, path).await()
        }
    }

    suspend fun uploadPhotoPersonalAsync(imageUri: String, personalCompanyInfoId: Int) {
        withContext(Dispatchers.IO) {
            val _id = personalCompanyInfoId
            DebugLog.logW("PersonalCompanyInfoId: $_id")
            val cv = ContentValues()
            val arrayListImages = ArrayList<String?>()
            arrayListImages.add(imageUri)
            val images = TextUtils.join(",", arrayListImages)
            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_IMAGES, images)
            DebugLog.logW("mImagePath to save later: $imageUri")
            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_DATA, "")
            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_SERVER_ID, _id)
            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_URL, Constantes.UPDATE_PHOTO_NEW_PERSONAL_URL)
            cv.put(DBHelper.PERSONAL_REPORT_TABLE_COLUMN_METHOD, Request.Method.PUT)
            mContext.contentResolver.insert(Constantes.CONTENT_PERSON_REPORT_URI, cv)
            UpdateDBService.startRequest(mContext, false)
        }
    }
    suspend fun uploadPhotoPersonalToIndividualContractAsync(imageUri: String, Id: String?) {
        withContext(Dispatchers.IO) {
            val _id = Id
            DebugLog.logW("PersonalCompanyInfoId: $_id")
            val cv = ContentValues()
            val arrayListImages = ArrayList<String?>()
            arrayListImages.add(imageUri)
            val images = TextUtils.join(",", arrayListImages)
            cv.put(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_IMAGES, images)
            DebugLog.logW("mImagePath to save later: $imageUri")
            cv.put(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_DATA, "")
            cv.put(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_SERVER_ID, _id)
            cv.put(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_URL, Constantes.UPDATE_PHOTO_PERSONAL_EMPLOYER_INFO_URL)
            cv.put(DBHelper.UPDATE_IMAGE_TABLE_COLUMN_METHOD, Request.Method.PUT)
            mContext.contentResolver.insert(Constantes.CONTENT_UPDATE_IMAGE_URI, cv)
            UpdateDBService.startRequest(mContext, false)
        }
    }

    suspend fun getJobs(): List<Job> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<Job>()
            val cursor = mContext.contentResolver.query(
                    Constantes.CONTENT_WORK_URI, null, null, null,
                    DBHelper.WORK_TABLE_COLUMN_NAME
            )
            cursor?.use {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME))
                    val nameScii = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_NAME_ASCII))
                    val jobCode = cursor.getString(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_CODE))
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.WORK_TABLE_COLUMN_SERVER_ID))
                    list.add(Job(id, name, jobCode, nameScii))
                }
            }

            list
        }
    }
    suspend fun getPayPeriod(): List<PayPeriod>{
        return  withContext(Dispatchers.IO){
            val list = mutableListOf<PayPeriod>()
            val selection = "(" + DBHelper.COPTIONS_COLUMN_TYPE + " = ? )"
            val selectionArgs = arrayOf("PayPeriodType")
            val cursor = mContext.contentResolver.query(Constantes.CONTENT_COMMON_OPTIONS_URI,null,selection,selectionArgs,null)

            cursor?.use {
                while (cursor.moveToNext()){
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.COPTIONS_COLUMN_SERVER_ID))
                    val description = cursor.getString(cursor.getColumnIndex(DBHelper.COPTIONS_COLUMN_DESC))
                    list.add(PayPeriod(id,description))
                }
            }
            list
        }
    }

    suspend fun getDaneCities(): List<DaneCity> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<DaneCity>()
            val cursor = mContext.contentResolver.query(
                    Constantes.CONTENT_DANE_CITY_URI, null, null, null,
                    DBHelper.DANE_CITY_TABLE_COLUMN_NAME
            )
            cursor?.use {
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_CODE))
                    val stateName = cursor.getString(cursor.getColumnIndex(DBHelper.DANE_CITY_TABLE_COLUMN_STATE))
                    list.add(DaneCity(code, name, stateName))
                }
            }

            list
        }
    }
    suspend fun getContractType(): List<IndividualContractsTypeModel>{
        return  withContext(Dispatchers.IO){
            val list = mutableListOf<IndividualContractsTypeModel>()
            val selection = "(" + DBHelper.COPTIONS_COLUMN_TYPE + " = ? )"
            val selectionArgs = arrayOf("IndividualContractsType")
            val cursor = mContext.contentResolver.query(
                    Constantes.CONTENT_COMMON_OPTIONS_URI, null, selection, selectionArgs, null)
            cursor?.use {
                while (cursor.moveToNext()){
                   val id = cursor.getString(cursor.getColumnIndex(DBHelper.COPTIONS_COLUMN_SERVER_ID))
                   val Description = cursor.getString(cursor.getColumnIndex(DBHelper.COPTIONS_COLUMN_DESC))
                    list.add(IndividualContractsTypeModel(id,Description))
                }
            }
            list
        }
    }

    suspend fun getCity(stateCode: String, cityCode: String, countryCode: Int): City? {
        return withContext(Dispatchers.IO) {

            val select = DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? and " +
                    DBHelper.CITY_TABLE_COLUMN_STATE_CODE + " = ? and " +
                    DBHelper.CITY_TABLE_COLUMN_CITY_CODE + " = ? "
            val selectionArgs = arrayOf(countryCode.toString(), stateCode, cityCode)
            val cursor = mContext.contentResolver.query(
                    Constantes.CONTENT_CITY_URI, null, select, selectionArgs,
                    DBHelper.CITY_TABLE_COLUMN_NAME
            )
            cursor?.use {

                if (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_SERVER_ID))
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE))
                    val stateName = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_STATE))
                    if(!name.isNullOrEmpty() && !code.isNullOrEmpty() && !stateName.isNullOrEmpty()){
                        return@withContext City(id, name, code, stateName)
                    }
                }
            }
            //logW("cursor:${cursor}")
            null
        }
    }
    suspend fun getCityunknown(stateCode: String, cityCode: String, countryCode: Int): City? {
        return withContext(Dispatchers.IO) {
            val select = DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? and " +
                    DBHelper.CITY_TABLE_COLUMN_STATE_CODE + " = ? and " +
                    DBHelper.CITY_TABLE_COLUMN_CITY_CODE + " = ? "
            val selectionArgs = arrayOf(countryCode.toString(), stateCode, cityCode)
            val cursor = mContext.contentResolver.query(
                    Constantes.CONTENT_CITY_URI, null, select, selectionArgs,
                    DBHelper.CITY_TABLE_COLUMN_NAME
            )
            cursor?.use {

                if (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_SERVER_ID))
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE))
                    val stateName = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_STATE))
                    if(!name.isNullOrEmpty() && !code.isNullOrEmpty() && !stateName.isNullOrEmpty()){
                        return@withContext City(id, name, code, stateName)
                    }
                }
            }
            null
        }
    }

    suspend fun getCity(code: String, countryCode: Int): City? {
        return withContext(Dispatchers.IO) {

            val select = DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? and " +
                    DBHelper.CITY_TABLE_COLUMN_CODE + " = ? "
            val selectionArgs = arrayOf(countryCode.toString(), code)
            val cursor = mContext.contentResolver.query(
                    Constantes.CONTENT_CITY_URI, null, select, selectionArgs,
                    DBHelper.CITY_TABLE_COLUMN_NAME
            )
            cursor?.use {

                if (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_SERVER_ID))
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE))
                    val stateName = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_STATE))
                    if(name.isNullOrEmpty()&&code.isNullOrEmpty()&&stateName.isNullOrEmpty()){
                        return@withContext City(0, "No registra", "No registra", "No registra")
                    }else{
                        return@withContext City(id, name, code, stateName)
                    }
                }
            }

            null
        }
    }

    suspend fun getCities(countryCode: Int): List<City> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<City>()
            val select = ("(" + DBHelper.CITY_TABLE_COLUMN_COUNTRY_CODE + " = ? )")
            val selectionArgs = arrayOf(countryCode.toString())
            val cursor = mContext.contentResolver.query(
                    Constantes.CONTENT_CITY_URI, null, select, selectionArgs,
                    DBHelper.CITY_TABLE_COLUMN_NAME
            )
            cursor?.use {

                while (cursor.moveToNext()) {
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_SERVER_ID))
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_CODE))
                    val stateName = cursor.getString(cursor.getColumnIndex(DBHelper.CITY_TABLE_COLUMN_STATE))
                    list.add(City(id, name, code, stateName))
                }
            }

            list
        }
    }
    suspend fun getEpsList(): List<EpsList>{
        return  withContext(Dispatchers.IO){
            val list = mutableListOf<EpsList>()
            val selectEPS = ("(" + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = 0 )")
            mContext.contentResolver.query(
                    Constantes.CONTENT_SEC_REFS_URI, null, selectEPS, null,
                    DBHelper.SECURITY_REFERENCE_COLUMN_NAME
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_CODE))
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID))
                    list.add(EpsList(id,name,code))
                }
            }
            list
        }
    }
    suspend fun getAfpList(): List<AfpList>{
        return  withContext(Dispatchers.IO){
            val list = mutableListOf<AfpList>()
            val selectAFP = ("(" + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = 1 )")
            mContext.contentResolver.query(
                    Constantes.CONTENT_SEC_REFS_URI, null, selectAFP, null,
                    DBHelper.SECURITY_REFERENCE_COLUMN_NAME
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_CODE))
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID))
                    list.add(AfpList(id,name,code))
                }
            }
            list
        }
    }
    suspend fun getSecurityReferences(): List<SecurityReference> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<SecurityReference>()

            val selectEPS = ("(" + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = 0 )")
            mContext.contentResolver.query(
                    Constantes.CONTENT_SEC_REFS_URI, null, selectEPS, null,
                    DBHelper.SECURITY_REFERENCE_COLUMN_NAME
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME))
                    val code = cursor.getString(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_CODE))
                    val id = cursor.getInt(cursor.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID))
                    list.add(SecurityReference.EPS(SocialSecurity(id, name, code)))
                }
            }

            val selectAFP = ("(" + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = 1 )")
            mContext.contentResolver.query(
                    Constantes.CONTENT_SEC_REFS_URI, null, selectAFP, null,
                    DBHelper.SECURITY_REFERENCE_COLUMN_NAME
            )?.use { cur ->
                while (cur.moveToNext()) {
                    val name = cur.getString(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME))
                    val code = cur.getString(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_CODE))
                    val id = cur.getInt(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID))
                    list.add(SecurityReference.AFP(SocialSecurity(id, name, code)))
                }
            }

            val selectARL = ("(" + DBHelper.SECURITY_REFERENCE_COLUMN_TYPE + " = 2 )")
            mContext.contentResolver.query(
                    Constantes.CONTENT_SEC_REFS_URI, null, selectARL, null,
                    DBHelper.SECURITY_REFERENCE_COLUMN_NAME
            )?.use { cur ->
                while (cur.moveToNext()) {
                    val name = cur.getString(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_NAME))
                    val code = cur.getString(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_CODE))
                    val id = cur.getInt(cur.getColumnIndex(DBHelper.SECURITY_REFERENCE_COLUMN_SERVER_ID))
                    list.add(SecurityReference.ARL(SocialSecurity(id, name, code)))
                }
            }

            list
        }
    }

}

sealed class SecurityReference {

    data class EPS(val item: SocialSecurity) : SecurityReference() {
        override fun toString(): String {
            return item.Name!!
        }
    }

    data class AFP(val item: SocialSecurity) : SecurityReference() {
        override fun toString(): String {
            return item.Name!!
        }
    }

    data class ARL(val item: SocialSecurity) : SecurityReference() {
        override fun toString(): String {
            return item.Name!!
        }
    }

    override fun toString(): String {
        return when (this) {
            is EPS -> item.Name!!
            is AFP -> item.Name!!
            is ARL -> item.Name!!
        }
    }
}

fun getContentVPersonal(personalList: PersonalNetwork): ContentValues {
    val cv = ContentValues()
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_SERVER_ID, personalList.Id)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_PERSONAL_COMPANY_ID, personalList.PersonalCompanyInfoId)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_DOC_TYPE, personalList.DocumentType)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_ACTIVE, 1)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_DOC_NUM, personalList.DocumentNumber)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_NAME, personalList.Name)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_LASTNAME, personalList.LastName)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_NAME_CITY, personalList.CityOfBirthName)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_NAME_STATE, personalList.StateOfBirthName)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_PHOTO, personalList.Photo)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_EXPIRY, personalList.Expiry)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_FINISH_DATE, personalList.FinishDate)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_START_DATE, personalList.StartDate)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_CREATE_DATE, personalList.CreateDate)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_PHONE, personalList.PhoneNumber)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_JOB, personalList.JobName)
    cv.put(DBHelper.PERSONAL_TABLE_COLUMN_COMPANY_INFO_ID, personalList.CompanyInfoId)
    return cv
}