package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

class ModelResponseSurvey : Serializable {
    var id: Int? = 0
    var Has :Boolean? = false
    var Question : String? = null
    var Response : String? = null

    constructor(id: Int?, Has: Boolean?, Question: String?, Response: String?) {
        this.id = id
        this.Has = Has
        this.Question = Question
        this.Response = Response
    }
}