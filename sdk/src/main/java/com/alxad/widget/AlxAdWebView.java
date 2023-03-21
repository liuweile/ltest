package com.alxad.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxWebAdListener;
import com.alxad.omsdk.OmidJsLoader;
import com.alxad.util.AlxLog;

/**
 * Web广告，带业务逻辑<br/>
 * 说明：web 中的链接 禁止进行二次跳转，会将得到的url回调出去手动处理
 *
 * @author liuweile
 * @date 2021-11-12
 */
public class AlxAdWebView extends AlxWebView {
    private final String TAG = "AlxWebAdView";

    private AlxWebAdListener mListener;

    public AlxAdWebView(@NonNull Context context) {
        super(context);
    }

    public AlxAdWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlxAdWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setWebConfig() {
        try {
            //解决webView在某些手机上闪屏
            setBackgroundColor(0);
            setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

            WebSettings webSettings = initWebSetting();
            webSettings.setBuiltInZoomControls(false);//替换initWebSetting()方法中的可缩放

            setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    AlxLog.i(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading-0");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (request != null && request.getUrl() != null) {
                            customWebLoading(request.getUrl().toString());
                        }
                        return true;
                    } else {
                        return super.shouldOverrideUrlLoading(view, request);
                    }
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    AlxLog.i(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading:" + url);
                    customWebLoading(url);
//                    return super.shouldOverrideUrlLoading(view, url);
                    return true; //不让WebView进行页面中事件的点击跳转和二次渲染
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    AlxLog.i(AlxLogLevel.MARK, TAG, "onPageFinished:" + url);
                    if (mListener != null) {
                        mListener.onViewShow();
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    String errorStr = "";
                    if (Build.VERSION.SDK_INT >= 23) {
                        errorStr = error.getErrorCode() + ":" + error.getDescription().toString();
                        AlxLog.e(AlxLogLevel.MARK, TAG, "onReceivedError:  " + errorStr);
                    } else {
                        errorStr = error.toString();
                        AlxLog.e(AlxLogLevel.MARK, TAG, "onReceivedError:" + errorStr);
                    }
                    if (mListener != null) {
                        mListener.onViewError(errorStr);
                    }
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    // 此处一定需要调用 handler.cancel() 阻止不安全的https，避免漏洞，上架不了Google Play
                    handler.cancel();
                    AlxLog.e(AlxLogLevel.MARK, TAG, "onReceivedSslError:" + error.toString());
                    if (mListener != null) {
                        mListener.onViewError(error.toString());
                    }
                }

                private void customWebLoading(String url) {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading-url:" + url);
                    if (mListener != null) {
                        mListener.onViewClick(url);
                    }
                }
            });
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    public void setEventListener(AlxWebAdListener listener) {
        mListener = listener;
    }

    public void loadHtml(String html) {
        addWebLoadingListener();
        try {
//            source = "<img src=\"https://apac.xyz.svr-algorix.com/static/img/320_50.jpg\">";//测试数据
            AlxLog.d(AlxLogLevel.MARK, TAG, html);
            String htmlString = OmidJsLoader.addOmJsIntoHtml(getContext(), html);
            String data = "<meta charset=\"utf-8\"> <meta name=\"viewport\" content=\"width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no\">"
                    + "<body style=\"margin: 0; text-align: center; \">\n" +
                    htmlString +
                    "</body>";
            loadDataWithBaseURL("about:blank", data, "text/html", "utf-8", null);
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void addWebLoadingListener() {
        try {
            if (mListener != null) {
                mListener.onWebLoading();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

}