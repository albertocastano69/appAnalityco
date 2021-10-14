package co.tecno.sersoluciones.analityco.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
data class IsContract(
    var Count: Int?,
    var ContractId: String?,
    var ContractNumber: String?,
    var Expiry: Boolean?
)

@Parcelize
@JsonClass(generateAdapter = true)
data class PersonalContract(
    var StartDate: String? = null,
    var FinishDate: String? = null,
    var PersonalCompanyInfoId: Int = 0,
    var Position: String?,
    var EmployerId: String? = null,
    var TypeARL: String? = null
) : Parcelable {

    constructor(startDate: String?, finishDate: String?, personalCompanyInfoId: Int, position: String, typeARL: String?)
            : this(startDate, finishDate, personalCompanyInfoId, position, TypeARL = typeARL)

    constructor(startDate: String?, finishDate: String?, personalCompanyInfoId: Int, position: String?)
            : this(StartDate = startDate, FinishDate = finishDate, PersonalCompanyInfoId = personalCompanyInfoId, Position = position)

}