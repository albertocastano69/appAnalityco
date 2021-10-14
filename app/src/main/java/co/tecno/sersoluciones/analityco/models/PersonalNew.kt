package co.tecno.sersoluciones.analityco.models

import java.io.Serializable
import java.util.*

class PersonalNew : Serializable {

    var CompanyInfoId: String? = null
    var EmployerId: String? = null
    var CreateDate: String? = null
    var DocumentRaw: String? = null
    var EpsId: Int? = null
    var AfpId: Int? = null
    var Nationality: Int = 0
    var StartDate: String? = null
    var FinishDate: String? = null
    var DocumentType: String = "CC"
    var DocumentNumber: String? = null
    var Name: String? = null
    var LastName: String? = null
    var PhoneNumber: String? = null
    var CityBirthCode: String? = null
    var CityOfBirthCode: String? = null
    var Height: String? = null
    var RH: String? = null
    var Sex: String? = null
    var Position: String? = null
    var CityCode: String? = null
    var Address: String? = null
    var EmergencyContact: String? = null
    var EmergencyContactPhone: String? = null
    var ContractId: String? = null
    var BirthDate: String? = null
    var CompanyId: String? = null
    var JobCode: String? = null
    var Photo: String? = null
    var Email: String? = null
    var PersonalId: String? = null
    //var RelationshipWithContact: String? = null


    constructor (
        startDate: String,
        finishDate: String,
        documentType: String,
        documentNumber: String,
        name: String,
        lastName: String,
        phoneNumber: String,
        cityBirthCode: String,
        height: String,
        rH: String,
        sex: String,
        position: String,
        cityCode: String?,
        address: String,
        emergencyContact: String,
        emergencyContactPhone: String,
        contractId: String?,
        birthDate: String,
        companyInfoId: String,
        jobCode: String,
        epsId: Int,
        afpId: Int,
        documentRaw: String,
        nationality: Int

    ) {
        StartDate = startDate
        FinishDate = finishDate
        DocumentType = documentType
        DocumentNumber = documentNumber
        Name = name
        LastName = lastName
        PhoneNumber = phoneNumber
        CityBirthCode = cityBirthCode
        Height = height
        RH = rH
        Sex = sex
        Position = position
        CityCode = cityCode
        Address = address
        EmergencyContact = emergencyContact
        EmergencyContactPhone = emergencyContactPhone
        ContractId = contractId
        BirthDate = birthDate
        CompanyInfoId = companyInfoId
        JobCode = jobCode
        EpsId = epsId
        AfpId = afpId
        DocumentRaw = documentRaw
        Nationality = nationality
    }

    constructor (
        companyInfoId: String,
        documentNumber: String,
        documentType: String,
        name: String,
        lastName: String,
        phoneNumber: String?,
        cityBirthCode: String,
        nationality: Int,
        birthDate: String,
        rH: String,
        sex: String,
        jobCode: String,
        cityCode: String?,
        address: String?,
        emergencyContact: String?,
        emergencyContactPhone: String?,
        epsId: Int,
        afpId: Int
        //relationshipWithContact: String
    ) {
        CompanyInfoId = companyInfoId
        DocumentNumber = documentNumber
        DocumentType = documentType
        Name = name
        LastName = lastName
        PhoneNumber = phoneNumber
        CityBirthCode = cityBirthCode
        Nationality = nationality
        RH = rH
        Sex = sex
        JobCode = jobCode
        CityCode = cityCode
        Address = address
        EmergencyContact = emergencyContact
        EmergencyContactPhone = emergencyContactPhone
        BirthDate = birthDate
        EpsId = epsId
        AfpId = afpId
       // RelationshipWithContact = relationshipWithContact
    }

    constructor(
        Nationality: Int,
        DocumentType: String,
        DocumentNumber: String?,
        Name: String?,
        LastName: String?,
        CityOfBirthCode: String?,
        RH: String?,
        Sex: String?,
        BirthDate: String?,
        CreateDate: String,
        Email: String?,
        epsId: Int,
        afpId: Int,
        DocumentRaw : String

    ) {
        this.Nationality = Nationality
        this.DocumentType = DocumentType
        this.DocumentNumber = DocumentNumber
        this.Name = Name
        this.LastName = LastName
        this.CityOfBirthCode = CityOfBirthCode
        this.RH = RH
        this.Sex = Sex
        this.BirthDate = BirthDate
        this.CreateDate = CreateDate
        this.Email = Email
        EpsId = epsId
        AfpId = afpId
        this.DocumentRaw = DocumentRaw

    }
     constructor(
            Id :String,
            Nationality: Int,
            DocumentType: String,
            DocumentNumber: String?,
            Name: String?,
            LastName: String?,
            CityOfBirthCode: String?,
            RH: String?,
            Sex: String?,
            BirthDate: String?,
            CreateDate: String,
            Email: String?,
            epsId: Int,
            afpId: Int

    ) {
        this.Nationality = Nationality
        this.DocumentType = DocumentType
        this.DocumentNumber = DocumentNumber
        this.Name = Name
        this.LastName = LastName
        this.CityOfBirthCode = CityOfBirthCode
        this.RH = RH
        this.Sex = Sex
        this.BirthDate = BirthDate
        this.CreateDate = CreateDate
        this.Email = Email
        EpsId = epsId
        AfpId = afpId
        PersonalId = Id
    }

    constructor(EpsId: Int?, AfpId: Int?, Nationality: Int, DocumentType: String, DocumentNumber: String?, Name: String?, CityBirthCode: String?, RH: String?, Sex: String?, BirthDate: String?,LastName: String?) {
        this.EpsId = EpsId
        this.AfpId = AfpId
        this.Nationality = Nationality
        this.DocumentType = DocumentType
        this.DocumentNumber = DocumentNumber
        this.Name = Name
        this.CityBirthCode = CityBirthCode
        this.RH = RH
        this.Sex = Sex
        this.BirthDate = BirthDate
        this.LastName = LastName
    }

    constructor(CompanyInfoId: String?, EpsId: Int?, AfpId: Int?, PhoneNumber: String?, CityCode: String?, Address: String?, EmergencyContact: String?, EmergencyContactPhone: String?, Email: String?, PersonalId: String?) {
        this.CompanyInfoId = CompanyInfoId
        this.EpsId = EpsId
        this.AfpId = AfpId
        this.PhoneNumber = PhoneNumber
        this.CityCode = CityCode
        this.Address = Address
        this.EmergencyContact = EmergencyContact
        this.EmergencyContactPhone = EmergencyContactPhone
        this.Email = Email
        this.PersonalId = PersonalId
    }

    constructor(DocumentRaw: String?, Nationality: Int, DocumentType: String, DocumentNumber: String?, Name: String?, LastName: String?, CityOfBirthCode: String?, RH: String?, Sex: String?, BirthDate: String?) {
        this.DocumentRaw = DocumentRaw
        this.Nationality = Nationality
        this.DocumentType = DocumentType
        this.DocumentNumber = DocumentNumber
        this.Name = Name
        this.LastName = LastName
        this.CityOfBirthCode = CityOfBirthCode
        this.RH = RH
        this.Sex = Sex
        this.BirthDate = BirthDate
    }

}