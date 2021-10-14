package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

data class AfpList(
        var Id: Int = 0,
        var Name: String? = null,
        var Code: String? = null
): Serializable {
    var Logo: String? = null
    var Description: String? = null

    override fun toString(): String {
        return Name!!
    }
}