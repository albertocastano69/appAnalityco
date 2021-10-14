package co.tecno.sersoluciones.analityco.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SurveyNetwork(
    @Json(name = "Id")
    val id: Long,
    @Json(name = "CreateDate")
    val createDate: String?,
    @Json(name = "UpdateDate")
    val updateDate: String?,
    @Json(name = "IsActive")
    val isActive: Boolean?,
    @Json(name = "Symptoms")
    val symptoms: List<String>?,
    @Json(name = "OtherPersonHasSymptoms")
    val otherPersonHasSymptoms: Boolean?,
    @Json(name = "OtherPersonHasCOVID19")
    val otherPersonHasCOVID19: Boolean?,
    @Json(name = "AcceptedPrivacy")
    val acceptedPrivacy: Boolean?,
    @Json(name = "Transport")
    val transport: List<String>?,
    @Json(name = "PersonalCompanyInfoId")
    val personalCompanyInfoId: Long?,
    @Json(name = "ResponseState")
    val responseState: Long?

)

@Entity(tableName = "survey",primaryKeys = ["personalCompanyInfoId", "createDate"])
data class Survey(
    val id: Long,
    val isActive: Boolean = false,
    val symptoms: String = "",
    val otherPersonHasSymptoms: Boolean = false,
    val otherPersonHasCOVID19: Boolean = false,
    val transport: String = "",
    val personalCompanyInfoId: Long = 0L,
    val createDate: String = "",
    val responseState : Int = 0
)

data class SurveyDetails(
    val fever: Boolean,
    val cough: Boolean,
    val nasalCongestion: Boolean,
    val soreThroat: Boolean,
    val difficultyBreathing: Boolean,
    val fatigue: Boolean,
    val shivers: Boolean,
    val musclePain: Boolean,
    val otherPersonHasSymptoms: String,
    val otherPersonHasCOVID19: String,
    val temperatura: String,
    val isActive : Boolean,
    val requestState : Int,
    val createDate: String

)


@JsonClass(generateAdapter = true)
data class SurveyTokenNetwork(
    @Json(name = "ProjectsInfo")
    val projectsInfo: List<SurveyProjectsInfo>?,
    @Json(name = "PersonalId")
    val personalId: String?,
    @Json(name = "PersonalInfo")
    val personalInfo: SurveyPersonalInfo?
)

@JsonClass(generateAdapter = true)
data class SurveyPersonalInfo(
    @Json(name = "Name")
    val name: String?,
    @Json(name = "LastName")
    val lastName: String?,
    @Json(name = "DocumentNumber")
    val documentNumber: String?,
    @Json(name = "Token")
    val token: String?
)

@JsonClass(generateAdapter = true)
data class SurveyProjectsInfo(
    @Json(name = "CompanyInfoId")
    val companyInfoId: String?,
    @Json(name = "PrivacyPolicies")
    val privacyPolicies: String?,
    @Json(name = "PersonalCompanyInfoId")
    val personalCompanyInfoId: Long?,
    @Json(name = "Projects")
    val projects: List<SurveyProject>?
)

@JsonClass(generateAdapter = true)
data class SurveyProject(
    @Json(name = "ProjectId")
    val projectId: String?,
    @Json(name = "ProjectName")
    val projectName: String?,
    @Json(name = "Logo")
    val logo: String?
)


