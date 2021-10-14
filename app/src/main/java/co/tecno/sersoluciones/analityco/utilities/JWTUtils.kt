package co.tecno.sersoluciones.analityco.utilities

import android.util.Base64
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class JWTUtils {
    companion object {
        @Throws(Exception::class)
        fun decoded(JWTEncoded: String): JSONObject? {
            try {
                val split = JWTEncoded.split(("\\.").toRegex())
//                Log.d("JWT_DECODED", "Header: " + getJson(split[0]))
//                Log.d("JWT_DECODED", "Body: " + getJson(split[1]))
                return JSONObject(getJson(split[1]))
            } catch (e: UnsupportedEncodingException) {
                //Error
            }
            return null
        }

        @Throws(UnsupportedEncodingException::class)
        private fun getJson(strEncoded: String): String {
            val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
            return String(decodedBytes, charset("UTF-8"))
        }
    }
}