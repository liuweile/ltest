package com.alxad.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.base.AlxBaseActivity;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxTracker;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxClickJump;
import com.alxad.util.AlxLog;
import com.alxad.widget.AlxWebView;


/**
 * 内置浏览器
 *
 * @author lwl
 * @date 2022-2-23
 */
public class AlxWebActivity extends AlxBaseActivity implements View.OnClickListener {
    private static final String TAG = "AlxWebActivity";
    private static final String EXTRA_PARAMS = "params";

    private Builder mBuilder;
    private AlxWebView mWebView;
    private View mBackView; //针对webView 返回
    private View mCloseView;//关闭activity
    private TextView mTitleView;
    private Context mContext;
    private boolean isPageFinished = false;

    private WindowManager windowManager;
    private View fullScreenLayer;

    public static void startWeb(Context context, Builder builder) {
        if (context == null || builder == null) {
            return;
        }
        Intent intent = new Intent(context, AlxWebActivity.class);
        intent.putExtra(EXTRA_PARAMS, builder);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alx_activity_web);
        mContext = this;
        initView();
        if (!initData()) {
            finish();
            return;
        }
        initWebView();
        loadData();
    }

    private void initView() {
        mWebView = (AlxWebView) findViewById(R.id.alx_webview);
        mBackView = findViewById(R.id.alx_bn_back);
        mCloseView = findViewById(R.id.alx_bn_close);
        mTitleView = (TextView) findViewById(R.id.alx_head_title);

        mBackView.setOnClickListener(this);
        mCloseView.setOnClickListener(this);

        //业务需求，不要页面一出来就显示关闭按钮
        mBackView.setVisibility(View.GONE);
        mCloseView.setVisibility(View.GONE);
    }

    private boolean initData() {
        Intent intent = getIntent();
        try {
            if (intent == null) {
                return false;
            }
            mBuilder = (Builder) intent.getParcelableExtra(EXTRA_PARAMS);
            if (mBuilder == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return false;
    }

    private void loadData() {
        if (mBuilder == null || TextUtils.isEmpty(mBuilder.resource)) {
            mBackView.setVisibility(View.VISIBLE);
            mCloseView.setVisibility(View.VISIBLE);
            return;
        }

        if (!TextUtils.isEmpty(mBuilder.title)) {
            mTitleView.setText(mBuilder.title);
        }

        if (mBuilder.resourceType == Builder.RESOURCE_TYPE_HTML) {
            mWebView.loadDataWithBaseURL(null, mBuilder.resource, "text/html", "UTF-8", null);
        } else {
            mWebView.loadUrl(mBuilder.resource);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_bn_back) {
            bnBack();
        } else if (v.getId() == R.id.alx_bn_close) {
            closeBrowser();
        }
    }

    private void bnBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            closeBrowser();
        }
    }

    private void closeBrowser() {
        // 事件上报，关闭事件
        reportEvent(AlxSdkDataEvent.WEBVIEW_CLOSE);
        finish();
    }

    @Override
    public void onBackPressed() {
        //系统返回键
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mWebView.removeAllViews();
            mWebView.onDestroy();
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void initWebView() {
        windowManager = getWindowManager();
        mWebView.initWebSetting();
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                AlxLog.i(AlxLogLevel.MARK, TAG, "onPageFinished:" + url);
                if (mBuilder == null || TextUtils.isEmpty(mBuilder.title)) {
                    if (view != null) {
                        String title = view.getTitle();
                        if (!TextUtils.isEmpty(title)) {
                            mTitleView.setText(title);
                        }
                    }
                }

                if (!isPageFinished) {
                    isPageFinished = true;
                    delayedShowCloseUI();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                AlxLog.i(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading:" + url);

                //1:先判断跳转到应用商店
                if (AlxClickJump.isStartAppStore(mContext, url)) { //成功
                    AlxLog.i(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading-start-way: app store");
                    return true;
                }

                //2:内置浏览器(http/https协议地址)
                if (!TextUtils.isEmpty(url) && (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://"))) {
                    AlxLog.i(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading-start-way: webview");
                    return super.shouldOverrideUrlLoading(view, url);
                }

                //3:判断deeplink(dp不已http开头)
                if (AlxClickJump.isStartDeepLink(mContext, url) == null) { //成功
                    AlxLog.i(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading-start-way: deeplink");
                    return true;
                }

                //4: 外部浏览器【无论成功与否，都返回true。防止出现页面加载失败的情况。但有可能一些点击事件没反应的情况】
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    AlxLog.i(AlxLogLevel.MARK, TAG, "shouldOverrideUrlLoading-start-way: browser");
                    return true;
                } catch (Exception e) {
                    AlxAgent.onError(e);
                    AlxLog.e(AlxLogLevel.OPEN, TAG, e.getMessage());
                    return true;
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                AlxLog.e(AlxLogLevel.MARK, TAG, "onReceivedSslError:" + error.toString());
                // 此处一定需要调用 handler.cancel() 阻止不安全的https，避免漏洞，上架不了Google Play
                handler.cancel();
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                AlxLog.i(AlxLogLevel.MARK, TAG, "onDownloadStart:" + url);
                AlxLog.i(AlxLogLevel.MARK, TAG, "onDownloadStart:" + mimetype);
                try {
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    AlxAgent.onError(e);
                    AlxLog.e(AlxLogLevel.MARK, TAG, "onDownloadStart-error:" + e.toString());
                }
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                // view 为内核生成的全屏视图，需要添加到相应的布局位置（如：全屏幕）
                // customViewCallback 用于主动控制全屏退出
                try {
                    WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_APPLICATION);
                    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                    windowManager.addView(view, params);
                    fullScreen(view);
                    fullScreenLayer = view;
                } catch (Exception e) {
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
            }

            @Override
            public void onHideCustomView() {
                super.onHideCustomView();
                try {
                    windowManager.removeViewImmediate(fullScreenLayer);
                    fullScreenLayer = null;
                } catch (Exception e) {
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
            }
        });
    }

    private void fullScreen(View view) {
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void reportEvent(int eventName) {
        try {
            if (mBuilder != null && mBuilder.tracker != null) {
                AlxSdkData.tracker(mBuilder.tracker, eventName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //方便以后扩展参数
    public static class Builder implements Parcelable {
        public static final int RESOURCE_TYPE_URL = 0;
        public static final int RESOURCE_TYPE_HTML = 1;

        private String resource;// url地址或html源码字符串
        private String title;
        private int resourceType;
        private AlxTracker tracker;

        public Builder() {
        }

        protected Builder(Parcel in) {
            resource = in.readString();
            title = in.readString();
            resourceType = in.readInt();
            tracker = in.readParcelable(AlxTracker.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(resource);
            dest.writeString(title);
            dest.writeInt(resourceType);
            dest.writeParcelable(tracker, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

        public Builder setLoad(String resource) {
            this.resource = resource;
            return this;
        }

        public Builder setLoadType(int resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTracker(AlxTracker tracker) {
            this.tracker = tracker;
            return this;
        }
    }

    //业务需求，不要页面一出来就显示关闭按钮
    private void delayedShowCloseUI() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mBackView.setVisibility(View.VISIBLE);
                    mCloseView.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    AlxAgent.onError(e);
                    AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
                }
            }
        }, 3000);
    }

}