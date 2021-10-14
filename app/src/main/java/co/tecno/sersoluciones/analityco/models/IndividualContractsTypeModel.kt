package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

class IndividualContractsTypeModel : Serializable {
    @JvmField
    var Id: String? = null

    @JvmField
    var Type: String? = null

    @JvmField
    var Description: String = ""

    @JvmField
    var Value: String? = null

    constructor(Id: String?, Description: String) {
        this.Id = Id
        this.Description = Description
    }

    override fun toString(): String {
        return Description
    }
}