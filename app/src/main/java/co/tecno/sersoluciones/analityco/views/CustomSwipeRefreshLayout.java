package co.tecno.sersoluciones.analityco.views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {

    private CanChildScrollUpCallback mCanChildScrollUpCallback;

    public CustomSwipeRefreshLayout(@NonNull Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public interface CanChildScrollUpCallback {
        boolean canSwipeRefreshChildScrollUp();
    }

    @Override
    public boolean canChildScrollUp() {
        if (mCanChildScrollUpCallback != null) {
            return mCanChildScrollUpCallback.canSwipeRefreshChildScrollUp();
        }
        return super.canChildScrollUp();
    }

    private final int yScrollBuffer = 5;
    private WebView mWebView;

    public void setWebView(WebView webView) {
        mWebView = webView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mWebView != null && mWebView.getScrollY() > yScrollBuffer)
            return false;

        return super.onInterceptTouchEvent(event);
    }
}