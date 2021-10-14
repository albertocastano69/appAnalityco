package co.tecno.sersoluciones.analityco.models

/**
 * Created by Ser SOluciones SAS on 24/01/2018.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class Job {
    @JvmField
    var Id: Int = 0

    @JvmField
    var Name: String = ""

    @JvmField
    var Code: String = ""
    var NameASCII: String? = null

    constructor()
    constructor(id: Int, name: String, code: String) {
        Id = id
        Name = name
        Code = code
    }

    constructor(id: Int, name: String, code: String, nameAscii: String?) {
        Id = id
        Name = name
        Code = code
        NameASCII = nameAscii
    }

    override fun toString(): String {
        return Name
    }

}