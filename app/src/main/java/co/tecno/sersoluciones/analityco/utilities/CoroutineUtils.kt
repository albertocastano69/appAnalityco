package co.tecno.sersoluciones.analityco.utilities

import co.com.sersoluciones.barcodedetectorser.utilities.DebugLog.logE
import kotlinx.coroutines.CoroutineExceptionHandler
import java.net.ConnectException
import java.net.SocketTimeoutException

object CoroutineUtils {
    val coroutineHandlerException = CoroutineExceptionHandler { _, exception ->
        exception.printStackTrace()
        logE("$exception handled !")
        when (exception) {
            is SocketTimeoutException -> logE(
                "SocketTimeoutException"
            )
            is ConnectException -> logE(
                "ConnectException"
            )
        }
    }
}