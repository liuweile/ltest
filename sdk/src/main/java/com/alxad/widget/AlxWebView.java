package com.alxad.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.analytics.AlxAgent;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;

/**
 * WebView
 */
public class AlxWebView extends WebView {
    private final String TAG = AlxWebView.class.getSimpleName();

    public AlxWebView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public AlxWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    public AlxWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {

    }

    public WebSettings initWebSetting() {
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

//        String userAgent = webSettings.getUserAgentString();
//        webSetting.setUserAgentString(userAgent + " " + "alx");

        webSettings.setAllowFileAccess(true);//设置可以访问文件
        //webSettings.setAppCacheEnabled(true);

        //设置自适应屏幕
        webSettings.setUseWideViewPort(true);//将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true);// 缩放至屏幕的大小
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        } else {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        }
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);//根据cache-control决定是否从网络上取数据。

        //缩放操作
        webSettings.setSupportZoom(true);//支持缩放，默认为true
        webSettings.setBuiltInZoomControls(true);////设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false);//隐藏原生的缩放控件
        webSettings.setTextZoom(100);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setDefaultTextEncodingName("UTF-8");//设置编码格式
        webSettings.setLoadsImagesAutomatically(true);//支持自动加载图片

        // 设置通用的配置，开启HTML5 定位功能
        webSettings.setDatabaseEnabled(true);
        String dir = getContext().getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        //启用地理定位
        webSettings.setGeolocationEnabled(true);
        //设置定位的数据库路径
        webSettings.setGeolocationDatabasePath(dir);

        //支持视屏播放
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        return webSettings;
    }

    public void onDestroy() {
        AlxLog.e(AlxLogLevel.MARK, TAG, "onDestroy");
        try {
            stopLoading();
            clearCache(true);
            //清除历史记录
            clearHistory();
            setWebViewClient(null);
            setWebChromeClient(null);
            setDownloadListener(null);

            //处理调用destroy的警告：【 W/cr_AwContents: WebView.destroy() called while WebView is still attached to window.】
            if (getParent() != null && getParent() instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) getParent();
                viewGroup.removeView(this);
            }

            removeAllViews();
            destroy();
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

}