package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

data class ContractReq(
    @JvmField
    var Id: String,
    @JvmField
    var Name: String? = null,
    @JvmField
    var ContractNumber: String? = null,
    @JvmField
    var ContractReview: String,
    @JvmField
    var WeekDays: String? = null,
    @JvmField
    var MaxHour: String? = null,
    @JvmField
    var MinHour: String? = null,
    @JvmField
    var AgeMin: String? = null,
    @JvmField
    var AgeMax: String? = null

) : Serializable {
    override fun toString(): String {
        return "$ContractNumber : $ContractReview"
    }
}