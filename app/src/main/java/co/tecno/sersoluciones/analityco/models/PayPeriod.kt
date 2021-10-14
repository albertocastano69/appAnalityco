package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

class PayPeriod : Serializable {
    @JvmField
    var Id: Int? = null

    @JvmField
    var Type: String? = null

    @JvmField
    var Description: String = ""

    @JvmField
    var Value: String? = null

    constructor(Id: Int?, Description: String) {
        this.Id = Id
        this.Description = Description
    }

    override fun toString(): String {
        return Description
    }
}