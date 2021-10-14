package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

/**
 * Created by Ser Soluciones SAS on 25/06/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class City : Serializable {
    @JvmField
    var Id = 0

    @JvmField
    var Name: String? = null

    @JvmField
    var StateName: String? = null

    @JvmField
    var CityCode: String? = null

    @JvmField
    var StateCode: String? = null

    @JvmField
    var Code: String? = null

    constructor()

    constructor(
        id: Int,
        name: String,
        code: String,
        stateName: String
    ) {
        Id = id
        Code = code
        Name = name
        StateName = stateName
    }

    override fun toString(): String {
        return "$Name ($StateName)"
    }
}