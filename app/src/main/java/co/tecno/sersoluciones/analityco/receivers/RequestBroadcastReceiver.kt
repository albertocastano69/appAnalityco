package co.tecno.sersoluciones.analityco.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import co.tecno.sersoluciones.analityco.utilities.Constantes

/**
 * Created by Ser Soluciones SAS on 15/08/2017.
 * www.sersoluciones.com - contacto@sersoluciones.com
 */
class RequestBroadcastReceiver(private val listener: BroadcastListener?) : BroadcastReceiver() {
    interface BroadcastListener {
        fun onStringResult(action: String?, option: Int, response: String?, url: String?)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val option = intent.getIntExtra(Constantes.OPTION_JSON_BROADCAST, 0)
        if (intent.hasExtra(Constantes.VALUE_JSON_BROADCAST)) {
            val response = intent.getStringExtra(Constantes.VALUE_JSON_BROADCAST)
            val urlRequest = intent.getStringExtra(Constantes.URL_REQUEST_BROADCAST)
            listener?.onStringResult(action, option, response, urlRequest)
        } else {
            val urlRequest = intent.getStringExtra(Constantes.URL_REQUEST_BROADCAST)
            listener?.onStringResult(action, option, "", urlRequest)
        }
    }

}