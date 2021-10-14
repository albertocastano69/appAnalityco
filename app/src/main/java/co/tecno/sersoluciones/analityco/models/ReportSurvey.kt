package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

class ReportSurvey : Serializable {
    var IsActive : Boolean? = false
    var Symptoms: ArrayList<String>? = null
    var OtherPersonHasSymptoms : Boolean? = false
    var OtherPersonHasCOVID19 : Boolean? = false
    var AcceptedPrivacy : Boolean? = false
    var Transport : ArrayList<String>? = null
    var RedResponse : String? = null
    var OrangeResponse : String? = null
    var PersonalCompanyInfoId : Long? = 0
    var ResponseState : Int? = 0
    var CreateDate : String? = null
    var UpdateDate : String? = null

    constructor(IsActive: Boolean?, Symptoms: ArrayList<String>?, OtherPersonHasSymptoms: Boolean?, OtherPersonHasCOVID19: Boolean?, AcceptedPrivacy: Boolean?, Transport: ArrayList<String>?, RedResponse: String?, OrangeResponse: String?, PersonalCompanyInfoId: Long?, ResponseState: Int?, CreateDate: String?, UpdateDate: String?) {
        this.IsActive = IsActive
        this.Symptoms = Symptoms
        this.OtherPersonHasSymptoms = OtherPersonHasSymptoms
        this.OtherPersonHasCOVID19 = OtherPersonHasCOVID19
        this.AcceptedPrivacy = AcceptedPrivacy
        this.Transport = Transport
        this.RedResponse = RedResponse
        this.OrangeResponse = OrangeResponse
        this.PersonalCompanyInfoId = PersonalCompanyInfoId
        this.ResponseState = ResponseState
        this.CreateDate = CreateDate
        this.UpdateDate = UpdateDate
    }
}