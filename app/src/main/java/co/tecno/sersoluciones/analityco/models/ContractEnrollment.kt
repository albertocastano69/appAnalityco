package co.tecno.sersoluciones.analityco.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
@JsonClass(generateAdapter = true)
data class ContractEnrollment(
    var Id: String? = null,
    var IsActive: Boolean = false,
    var IsRegister: Boolean = false,
    var Expiry: Boolean = false,
    var ContractId: String? = null,
    var ContractType: String? = null,
    var StartDate: String? = null,
    var FinishDate: String? = null,
    var FinishDatePerson: String? = null,
    var ProjectId: String? = null,
    var ProjectIds: String? = null,
    var Name: String? = null,
    var ContractNumber: String? = null,
    var ContractReview: String? = null,
    var FormImageLogo: String? = null,
    var CompanyName: String? = null,
    var ValuePersonalType: String? = null,
    var Position: String? = null,
    var CompanyInfoId: String? = null,
    var EmployerId: String? = null,
    var ContractorName: String? = null,
    var EmployerName: String? = null,
    var CountPersonal: Int = 0,
    var isJoin: Boolean = false
) : Parcelable