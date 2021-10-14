package co.tecno.sersoluciones.analityco.utilities

import android.webkit.JavascriptInterface

/** Instantiate the interface and set the context  */
class WebAppInterface(val listener: WebViewListener) {

    /** Show a toast from the web page  */
    @JavascriptInterface
    fun onClose() {
        listener.onCloseWebView()
    }

    interface WebViewListener {
        fun onCloseWebView()
    }
}