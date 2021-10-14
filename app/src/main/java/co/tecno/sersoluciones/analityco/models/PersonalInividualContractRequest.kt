package co.tecno.sersoluciones.analityco.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class PersonalInividualContractRequest(
        var IndividualContractTypeId: Int? = 0,
        var PersonalEmployerInfoId : String? = null,
        var PositionId: Int? = 0,
        var Salary: Int? = 0,
        var PayPeriodId : Int? = 0,
        var StartDate : String? = null,
        var ProjectId : String? = null,
        var ContractId : String? = null,
        var ContractCityId : Int? = 0
): Parcelable
