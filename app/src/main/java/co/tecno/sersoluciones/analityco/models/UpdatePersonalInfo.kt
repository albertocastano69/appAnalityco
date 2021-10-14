package co.tecno.sersoluciones.analityco.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdatePersonalInfo
    (
    var PhoneNumber: String? = null,
    var CityCode: String? = null,
    var Address: String? = null,
    var EmergencyContact: String? = null,
    var EmergencyContactPhone: String? = null,
    var JobCode: String,
    var EpsId: Int,
    var AfpId: Int,
    var Photo: String? = null
) {
    constructor(
        phoneNumber: String?,
        cityCode: String?,
        address: String?,
        emergencyContact: String?,
        emergencyContactPhone: String?,
        jobCode: String,
        epsId: Int,
        afpId: Int
    ) : this(
        phoneNumber, cityCode, address, emergencyContact, emergencyContactPhone, jobCode, epsId, AfpId = afpId
    )
}