package co.tecno.sersoluciones.analityco.models

import java.io.Serializable

/**
 * Created by Ser Soluciones SAS on 11/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class DaneCity : Serializable {
    @JvmField
    var StateName: String? = null

    @JvmField
    var StateCode: String? = null

    @JvmField
    var CityCode: String? = null

    @JvmField
    var CityName: String? = null

    @JvmField
    var Id: String? = null


    constructor()



    constructor(
        cityCode: String,
        cityName: String,
        stateName: String
    ) {
        CityCode = cityCode
        CityName = cityName
        StateName = stateName
    }

    constructor(StateName: String?, StateCode: String?, CityCode: String?, CityName: String?, Id: String?) {
        this.StateName = StateName
        this.StateCode = StateCode
        this.CityCode = CityCode
        this.CityName = CityName
        this.Id = Id
    }

    override fun toString(): String {
        return "$CityName ($StateName)"
    }
}