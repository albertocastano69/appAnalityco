package co.tecno.sersoluciones.analityco.room

import androidx.lifecycle.LiveData
import androidx.room.*
import co.tecno.sersoluciones.analityco.models.*

@Dao
interface AnalitycoDao {

    @Query("SELECT * from real_time_person WHERE personalId = :id")
    fun getPersonalRealTime(id: Long): PersonalRealTime?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllPersonalRealTime(vararg users: PersonalRealTime)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPersonalRealTime(PersonalRealTime: PersonalRealTime)

    @Query("DELETE FROM real_time_person WHERE personalId = :personalId ")
    fun removePersonalRealTime(personalId: Long)

    @Query("DELETE FROM real_time_person")
    fun removeAllPersonalRealTime()

    @Update
    fun updatePersonalRealTime(PersonalRealTime: PersonalRealTime)

    @Query("UPDATE real_time_person SET getTime = :getTime WHERE personalId = :personalId")
    fun updatePersonalRealTime(personalId: Long, getTime: Long)

    @Query("SELECT * from real_time_person WHERE projectId = :projectId ORDER BY name, lastName")
    fun getPersonalRealTimeByProject(projectId: String): List<PersonalRealTime>?

    @Query("SELECT count( DISTINCT personalId) from real_time_person WHERE projectId = :projectId and getTime > 0")
    fun getEntryCountDailyPersonal(projectId: String): Long

    @Query("SELECT getTime from real_time_person WHERE documentNumber = :document")
    fun getRegisterEntry(document: String):Long

    @Query("SELECT lastTime from real_time_person WHERE documentNumber = :document")
    fun getRegisterExit(document: String):Long

    @Query("SELECT inputStatus from real_time_person WHERE documentNumber = :document")
    fun getInputStatus(document: String):Long

    @Query("SELECT count( DISTINCT personalId) from real_time_person WHERE projectId = :projectId and lastTime > 0")
    fun getExitCountDailyPersonal(projectId: String): Long

    @Query("DELETE FROM real_time_person")
    fun clearAllPersonalRealTimes()

    @Query("SELECT * FROM real_time_person ORDER BY name, lastName")
    fun getAllPersonalRealTimes(): LiveData<List<PersonalRealTime>>

    //survey

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSurveys(vararg surveys: Survey)


    @Query("SELECT * from survey a where a.PersonalCompanyInfoId= :id and a.createdate = (select max(b.createdate) from survey b where a.PersonalCompanyInfoId= b.PersonalCompanyInfoId and b.createdate> date('now')-1)")
    fun getSurvey(id: Long): Survey?

    @Query("SELECT * from survey a where a.PersonalCompanyInfoId= :id and a.createdate = (select max(b.createdate) from survey b where a.PersonalCompanyInfoId= b.PersonalCompanyInfoId and b.createdate> date('now')-1)")
    fun getSurveyInfo(id: Long): Survey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurveyOffline(vararg surveys: SurveySymptom)

    // Reportes
    @Insert
    fun insertReporte(reporte: Reporte)

    @Query("SELECT * from reporte WHERE id = :key")
    fun getReporte(key: Long): Reporte?

    @Query("DELETE FROM Reporte")
    fun clearAllReportes()

    @Query("SELECT * FROM reporte ORDER BY id")
    fun getAllReportes(): List<Reporte>

    @Query("SELECT * FROM surveySymptom ORDER BY id")
    fun getReportSurvey(): List<SurveySymptom>

    @Query("SELECT * FROM reporte ORDER BY id")
    fun liveReports(): LiveData<List<Reporte>>

    @Query("SELECT * FROM reporte ORDER BY id LIMIT 1")
    fun getFirstReporte(): Reporte?

    @Query("DELETE FROM surveySymptom WHERE id = :key")
    fun deleteReporteSurvey(key: Long)

    @Query("DELETE FROM Reporte WHERE id = :key")
    fun deleteReporte(key: Long)

    @Query("SELECT count(*) FROM Reporte")
    fun getCountRegistersDB(): Int
}