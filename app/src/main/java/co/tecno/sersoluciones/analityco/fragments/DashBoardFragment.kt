package co.tecno.sersoluciones.analityco.fragments

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.ButterKnife
import co.tecno.sersoluciones.analityco.R
import co.tecno.sersoluciones.analityco.utilities.HttpRequest
import co.tecno.sersoluciones.analityco.utilities.MyPreferences
import co.tecno.sersoluciones.analityco.views.CustomSwipeRefreshLayout
import co.tecno.sersoluciones.analityco.views.CustomSwipeRefreshLayout.CanChildScrollUpCallback


class DashBoardFragment : Fragment(), CanChildScrollUpCallback {
    @JvmField
    @BindView(R.id.mWebView)
    var mWebView: WebView? = null

    @JvmField
    @BindView(R.id.progressBar)
    var progressBar: ProgressBar? = null

    @JvmField
    @BindView(R.id.swiperefresh)
    var mySwipeRefreshLayout: CustomSwipeRefreshLayout? = null

    @JvmField
    @BindView(R.id.layout_error_conection)
    var layoutErrorConnection: ConstraintLayout? = null

    @JvmField
    @BindView(R.id.errorButton)
    var errorButton: Button? = null
    private var preferences: MyPreferences? = null
    private var url: String? = null
    private var mhandler: Handler? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dash_board, container, false)
        val unbinder = ButterKnife.bind(this, view)
        mhandler = Handler()
        loadWebview()
        errorButton!!.setOnClickListener {
            mhandler!!.removeCallbacks(validateConection)
            layoutErrorConnection!!.visibility = View.GONE
            progressBar!!.visibility = View.VISIBLE
            mhandler!!.postDelayed(validateConection, 2000)
        }

        // mySwipeRefreshLayout.setCanChildScrollUpCallback(this);
        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadWebview() {
        if (checkInternetConnection()) {
            layoutErrorConnection!!.visibility = View.GONE
            progressBar!!.progress = 0
            preferences = MyPreferences(requireActivity())
            mWebView!!.settings.javaScriptEnabled = true
            mWebView!!.settings.loadsImagesAutomatically = true
            mWebView!!.webChromeClient = CustomWebChromeClient()
            mWebView!!.webViewClient = CustomWebViewClient()
            WebView.setWebContentsDebuggingEnabled(true)
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setCookie("token", preferences!!.token)
            url = "http://acceso.analityco.com/WebViewDashboardPersonal#!/"
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            if (sharedPreferences.getBoolean("pref_key_bool_server", false)) url = "http://acceso-stg.analityco.com/WebViewDashboardPersonal#!/"
            val values = ContentValues()
            values.put("token", preferences!!.token)
            val paramsQuery = HttpRequest.makeParamsInUrl(values)
            mWebView!!.loadUrl(url + paramsQuery)
            mySwipeRefreshLayout!!.setWebView(mWebView)
            mySwipeRefreshLayout!!.setColorSchemeColors(
                Color.RED,
                Color.GREEN,
                Color.BLUE,
                Color.YELLOW
            )
            mySwipeRefreshLayout!!.setOnRefreshListener { syncWeb() }
            mySwipeRefreshLayout!!.isEnabled = false
        } else {
            progressBar!!.visibility = View.GONE
            layoutErrorConnection!!.visibility = View.VISIBLE
        }
    }

    private fun refreshClose() {
        if (mySwipeRefreshLayout != null) mySwipeRefreshLayout!!.isRefreshing = false
    }

    private fun syncWeb() {
        val values = ContentValues()
        values.put("token", preferences!!.token)
        val paramsQuery = HttpRequest.makeParamsInUrl(values)
        if (!url!!.isEmpty() && Patterns.WEB_URL.matcher(url).matches()) {
            progressBar!!.visibility = View.VISIBLE
            progressBar!!.progress = 0
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setCookie("token", preferences!!.token)
            mWebView!!.loadUrl(url + paramsQuery)
        }
    }

    override fun canSwipeRefreshChildScrollUp(): Boolean {
        return mWebView!!.scrollY > 0
    }

    internal inner class CustomWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar!!.progress = newProgress
        }
    }

    internal inner class CustomWebViewClient : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            return super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url.toString()
            view?.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar?.visibility = View.GONE
            progressBar?.progress = 100
            refreshClose()
        }
    }

    private fun checkInternetConnection(): Boolean {
        return try {
            progressBar?.visibility = View.VISIBLE
            progressBar?.progress = 100
            val conManager =
                activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            (conManager.activeNetworkInfo != null && conManager.activeNetworkInfo!!.isAvailable
                    && conManager.activeNetworkInfo!!.isConnected)
        } catch (e: Exception) {
            false
        }
    }

    private val validateConection = Runnable { loadWebview() }

    companion object {
        @JvmStatic
        fun newInstance(): DashBoardFragment {
            return DashBoardFragment()
        }
    }
}