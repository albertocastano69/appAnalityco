package co.tecno.sersoluciones.analityco;

import android.content.ContentValues;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.tecno.sersoluciones.analityco.utilities.HttpRequest;
import co.tecno.sersoluciones.analityco.utilities.MyPreferences;

public class DasboardWebActivity extends AppCompatActivity {

    @BindView(R.id.mWebView)
    WebView mWebView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.editText)
    EditText editText;
    private MyPreferences preferences;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dasboard_web);
        ButterKnife.bind(this);

        progressBar.setProgress(0);
        preferences = new MyPreferences(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.setWebChromeClient(new CustomWebChromeClient());
        mWebView.setWebViewClient(new CustomWebViewClient());
        WebView.setWebContentsDebuggingEnabled(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie("token", preferences.getToken());
        url = "http://192.168.1.201:5000/app-dashboard-personal";
        ContentValues values = new ContentValues();
        values.put("token", preferences.getToken());
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        mWebView.loadUrl(url + paramsQuery);

        editText.setText(url);
    }

    public void refresh(View view) {
        url = editText.getText().toString();
        ContentValues values = new ContentValues();
        values.put("token", preferences.getToken());
        String paramsQuery = HttpRequest.makeParamsInUrl(values);
        if (!url.isEmpty() && Patterns.WEB_URL.matcher(url).matches()) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setCookie("token", preferences.getToken());
            mWebView.loadUrl(url + paramsQuery);
        }
    }

    class CustomWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
        }
    }

    class CustomWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(100);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            // return getNewResponse(url);
            return super.shouldInterceptRequest(view, request);
        }

    }
}
