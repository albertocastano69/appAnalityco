package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

class PersonalDataContact(): Serializable {
    var EmployerId: String? = null
    var CreateDate: String? = null
    var PhoneNumber: String? = null
    var Height: String? = null
    var CityCode: String? = null
    var Address: String? = null
    var EmergencyContact: String? = null
    var EmergencyPhone: String? = null
    var Email: String? = null
    var PersonalId: String? = null

    constructor(
        PhoneNumber : String?,
        CityCode : String?,
        Address : String?,
        EmergencyContact : String?,
        EmergencyPhone : String?,
        Email : String?,
        EmployerId : String?,
        PersonalId : String?
    ) : this() {
        this.PhoneNumber = PhoneNumber
        this.CityCode = CityCode
        this.Address = Address
        this.EmergencyContact = EmergencyContact
        this.EmergencyPhone = EmergencyPhone
        this.Email = Email
        this.EmployerId = EmployerId
        this.PersonalId = PersonalId
    }
}