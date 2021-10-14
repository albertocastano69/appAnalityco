package co.tecno.sersoluciones.analityco.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PersonalContractOfflineNetwork(
    var PersonalCompanyInfoId: Int? = null,
    var ContractId: String? = null,
    var ProjectId: String? = null,
    var CarnetCodes: String? = null,
    var EnterProject: Boolean? = null,
    var ContractNumber: String? = null,
    var ContractReview: String? = null,
    var StartDateContract: String? = null,
    var FinishDateContract: String? = null,
    var StartDate: String? = null,
    var FinishDate: String? = null,
    var EmployerId: String? = null,
    var MaxHour: String? = null,
    var MinHour: String? = null,
    var AgeMin: Long? = null,
    var AgeMax: Long? = null,
    var WeekDays: String? = null,
    var DocumentNumber: String? = null,
    var Position: String? = null,
    var ProjectContracts: String? = null,
    var DescriptionPersonalType: String? = null,
    var Requirements: String? = null,
    var BirthDate: String? = null
)