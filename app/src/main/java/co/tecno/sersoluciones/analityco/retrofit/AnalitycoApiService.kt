package co.tecno.sersoluciones.analityco.retrofit

import android.app.Person
import co.tecno.sersoluciones.analityco.adapters.repositories.PositionIndividualContract
import co.tecno.sersoluciones.analityco.models.*
import co.tecno.sersoluciones.analityco.utilities.Constantes
import co.tecno.sersoluciones.analityco.models.PersonalRealTime
import co.tecno.sersoluciones.analityco.models.SurveyNetwork
import co.tecno.sersoluciones.analityco.models.SurveyTokenNetwork
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.Deferred
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface AnalitycoApiService {

    @GET("api/PersonalRealTime")
    fun getPersonalRealTimeAsync(): Deferred<List<PersonalRealTime>>

    @POST("api/PersonalRealTime/")
    fun createPersonalRealTimeAsync(@Body personalRealTime: PersonalRealTime): Deferred<PersonalRealTime>

    @GET("api/Personal/GetPersonalInfoId/{doc}/{companyInfoId}")
    fun getPersonalInfoIdAsync(
        @Path("doc") doc: String,
        @Path("companyInfoId") companyInfoId: String
    ): Deferred<PersonalNetwork?>

    @GET("api/IndividualContract/GetByPersonalEnployerInfo/{id}")
    fun getPersonalAssociateInContractAsync(
            @Path("id") id: String
    )

    @GET("api/Personal/ByDocument")
    fun getPersonalInfoToIndivudalContractIdAsync(
        @Query("documentType") typeDocument : String,
        @Query("documentNumber") doc: String
    ): Deferred<PersonalNetwork?>


    @GET("api/PersonalEmployerInfo/EmployerPersonal")
    fun getDataToContactAsync(
            @Query("PersonalId") PersonalId: String?,
            @Query("EmployerId") EmployerId : String?
    ): Deferred<PersonalNetworkData?>

    @POST(Constantes.NEW_PERSONAL_COMPANY_URL)
    fun createPersonalAsync(@Body personal: PersonalNew): Deferred<PersonalNetwork>

    @POST("api/Personal")
    fun createPersonalAsyncToIndividualContractAsync(@Body personalIndivudualContract: PersonalNew): Deferred<PersonalIndividualContract>

    @PUT("api/Personal/UpdatePersonal")
    fun updatePersonalAsyncToIndividualContractAsync(@Body personalIndivudualContract: PersonalNew): Deferred<PersonalIndividualContract>

    @POST("api/IndividualContract")
    fun RequestORderAsync(@Body json: String): Deferred<Any>

    @POST("api/IndividualContract")
    fun createNewContractIndividualAsync(@Body jsonObject: String): Deferred<PersonalIndividualContract>

    @POST("api/PersonalEmployerInfo")
    fun createContactPersonalAsyncToIndividualContractAsync(@Body personalContact: PersonalDataContact): Deferred<PersonalIndividualContract>

    @PUT("api/PersonalEmployerInfo/{id}")
    fun UpdateContactPersonalAsyncToIndividualContractAsync(@Body personalContact: PersonalDataContact,@Path("id") id: String): Deferred<PersonalIndividualContract>

    @PUT("api/Personal/UpdatePersonalCompany/{personalCompanyInfoId}/")
    fun updatePersonalAsync(@Body personal: UpdatePersonalInfo, @Path("personalCompanyInfoId") personalInfoId: String): Deferred<PersonalNetwork>

    @PUT("api/Personal/UpdatePersonal/")
    fun UpdateApiPerson(@Body personalIndivudualContract: PersonalNew): Deferred<PersonalIndividualContract>

    @GET("api/Personal/{personalCompanyInfoId}/Contracts/")
    fun getContractsAsync(@Path("personalCompanyInfoId") personalInfoId: String): Deferred<List<ContractEnrollment>>

    @GET("api/Contract/Positions/{companyInfoId}/")
    fun getPersonalPositionsAsync(@Path("companyInfoId") companyInfoId: String): Deferred<List<Position>>

    @GET("api/Position/Employer/{EmployerId}")
    fun getPositionsAsync(@Path("EmployerId")employerId : String?): Deferred<List<PositionIndividualContract>>

    @GET("api/DaneCity/")
    fun getDaneCity(): Deferred<List<DaneCity>>

    @GET("api/City/")
    fun getCityPersonAsync(): Deferred<List<City>>

    @GET("api/City/")
    fun getCityPersonForNationalityAsync(
            @Query("country") nationality : Int
    ): Deferred<List<City>>


    @GET("api/Contract/SelectContracting/{employerId}")
    fun getPlaceToJobAsync(@Path("employerId")employerId : String?): Deferred<List<PlaceToJob>>

    @GET("api/Contract/{contractId}/IsContract/{personalCompanyInfoId}/")
    fun isPersonalInContractAsync(
        @Path("contractId") contractId: String,
        @Path("personalCompanyInfoId") personalCompanyInfoId: String
    ): Deferred<Any>

    @POST("api/Contract/{contractId}/{path}/")
    fun createPersonalContractAsync(
        @Body personalContract: PersonalContract,
        @Path("contractId") contractId: String,
        @Path("path") path: String
    ): Deferred<Any>

    @GET("api/Contract/{contractId}/PersonalInfo/")
    fun getPersonalContractAsync(
        @Path("contractId") contractId: String, @Query("personalInfoId") personalCompanyInfoId: String
    ): Deferred<PersonalContract>

    @GET("api/SurveySymptoms/ByPersonal")
    fun getSurveyListAsync(): Deferred<List<SurveyNetwork>>

    @POST("api/SurveySymptoms/ValidateLink")
    fun getSurveyTokenAsync(
        @Query("docnumber") docNumber: String,
        @Query("pin") pin: String,
        @Query("clientId") clientId: String
    ): Deferred<SurveyTokenNetwork>

    @GET("/api/Personal/ContractsOffline")
    fun getPersonalContractOfflineAsync(
        @Query("personalCompanyInfoId") personalCompanyInfoId: Long,
        @Query("project") project: Boolean
    ): Deferred<List<PersonalContractOfflineNetwork>>

    @Headers("Content-Type: application/json")
    @POST("api/PersonalReport/")
    fun sendReporteAsync(@Body requestBody: RequestBody): Deferred<Any>

    @Headers("Content-Type: application/json")
    @POST("api/SurveySymptoms")
    fun sendReportSurvey(@Body requestBody: RequestBody): Deferred<Any>
}