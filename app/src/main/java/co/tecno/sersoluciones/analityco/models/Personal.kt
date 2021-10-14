package co.tecno.sersoluciones.analityco.models

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.util.*

/**
 * Created by Ser Soluciones SAS on 01/03/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
@JsonClass(generateAdapter = true)
data class Personal(

    @JvmField
    var StartDate: String? = null,
    @JvmField
    var FinishDate: String? = null,
    @JvmField
    var DocumentType: String? = null,
    @JvmField
    var DocumentNumber: String? = null,
    @JvmField
    var Name: String? = null,
    @JvmField
    var LastName: String? = null,
    @JvmField
    var PhoneNumber: String? = null,
    var CityBirthCode: String? = null,
    var Height: String? = null,
    @JvmField
    var RH: String? = null,
    @JvmField
    var Sex: String? = null,
    @JvmField
    var JobCode: String? = null,
    @JvmField
    var JobName: String? = null,
//public int PositionId;
    @JvmField
    var CityCode: String? = null,
    @JvmField
    var CityName: String? = null,
    @JvmField
    var Address: String? = null,
    @JvmField
    var EmergencyContact: String? = null,
    @JvmField
    var EmergencyContactPhone: String? = null,
    var ContractId: String? = null,
    var BirthDate: String? = null,
    var DocumentRaw: String? = null,
    @JvmField
    var Photo: String? = null,
    var Id: String? = null,
    var IsActive: Boolean = true,
    var Expiry: Boolean = false,
    var PersonalId: String? = null,
    @JvmField
    var Position: String? = null,
    @JvmField
    var PersonalCompanyInfoId: Int = 0,
    var contratos: ArrayList<ContractList>? = null,
    var CompanyInfoId: String? = null,
    var DaysToExpire: Int = 0,
    @JvmField
    var EpsId: Int = 0,
    @JvmField
    var AfpId: Int = 0,
    @JvmField
    var EpsCode: String? = null,
    @JvmField
    var AfpCode: String? = null,
    @JvmField
    var NameEPS: String? = null,
    @JvmField
    var NameAFP: String? = null,
    @JvmField
    var CityOfBirthName: String? = null,
    @JvmField
    var StateOfBirthName: String? = null,
    @JvmField
    var Email: String? = null,
    @JvmField
    var EmergencyPhone: String? = null,
    @JvmField
    var EmployerId: String? = null,
    @JvmField
    var CityOfBirthCode: String? = null,
    @JvmField
    var Nationality: Int = 0,
    @JvmField
    var RelationshipWithContact: String? = null



) : Serializable {

    constructor() : this(StartDate = "")
}

@Parcelize
@JsonClass(generateAdapter = true)
data class  PersonalNetwork(
    var Id: String? = null,
    var PersonalId: String? = null,
    var CompanyInfoId: String? = null,
    var PhoneNumber: String? = null,
    var EmergencyContact: String? = null,
    var EmergencyContactPhone: String? = null,
    var CityCode: String? = null,
    var CityName: String? = null,
    var Address: String? = null,
    var JobCode: String? = null,
    var PersonalCompanyInfoId: Int = 0,
    var JobName: String? = null,
    var Name: String? = null,
    var DocumentRaw: String? = null,
    var LastName: String? = null,
    var DocumentNumber: String?,
    var Sex: String? = null,
    var RH: String? = null,
    var EpsId: Int? = 0,
    var AfpId: Int? = 0,
    var EpsCode: String? = null,
    var AfpCode: String? = null,
    var EpsName: String? = null,
    var AfpName: String? = null,
    var CityOfBirthName: String? = null,
    var CityOfBirthCode: String? = null,
    var StateOfBirthName: String? = null,
    var BirthDate: String? = null,
    var Nationality: Int = 0,
    var Photo: String? = null,
    var IsActive: Boolean = true,
    var Expiry: Boolean = false,
    var StartDate: String? = null,
    var CreateDate: String? = null,
    var FinishDate: String? = null,
    var DocumentType: String? = null,
    var Email: String? = null,
    var IsScanned: Boolean? = false



) : Parcelable

@Parcelize
@JsonClass(generateAdapter = true)
data class PersonalNetworkData(
        var Id: String? = null,
        var PhoneNumber: String? = null,
        var EmergencyContact: String? = null,
        var EmergencyPhone: String? = null,
        var Address: String? = null,
        var Email: String? = null,
        var Name: String? = null
) : Parcelable
@Parcelize
@JsonClass(generateAdapter = true)
data class PersonalIndividualContract(
    var Id: String? = null,
    var PersonalId: String? = null,
    var CompanyInfoId: String? = null,
    var Name: String? = null,
    var LastName: String? = null,
    var DocumentNumber: String?,
    var Sex: String? = null,
    var RH: String? = null,
    var CityOfBirthCode: String? = null,
    var BirthDate: String? = null,
    var Nationality: Int = 0,
    var Photo: String? = null,
    var CreateDate: String? = null,
    var DocumentType: String? = null
) : Parcelable