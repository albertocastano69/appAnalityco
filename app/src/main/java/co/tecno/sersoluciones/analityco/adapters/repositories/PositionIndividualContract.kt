package co.tecno.sersoluciones.analityco.adapters.repositories

import com.squareup.moshi.JsonClass
import java.io.Serializable
import java.text.Normalizer

@JsonClass(generateAdapter = true)
class PositionIndividualContract: Serializable {
    var Id = 0
    @JvmField
    var Name: String = ""
    var Descrption: String = ""
    //var CompanyId : String = ""

    override fun toString(): String {
        return Name.unaccent()
    }
}

private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun CharSequence.unaccent(): String {
    val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
    return REGEX_UNACCENT.replace(temp, "")
}