package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

class PlaceToJob: Serializable {
    var Name : String? = null
    var ContractReview : String? = null
    var Logo : String? = null
    var ContractId : String? = null
    var projectid : String? = null
    var FinishDate : String? = null
    var ContractNumber : String? = null

    constructor(Name: String?, ContractReview: String?, Logo: String?, ContractId: String?, projectid: String?, FinishDate: String?, ContractNumber: String?) {
        this.Name = Name
        this.ContractReview = ContractReview
        this.Logo = Logo
        this.ContractId = ContractId
        this.projectid = projectid
        this.FinishDate = FinishDate
        this.ContractNumber = ContractNumber
    }
    override fun toString(): String {
        return Name!!
    }
}