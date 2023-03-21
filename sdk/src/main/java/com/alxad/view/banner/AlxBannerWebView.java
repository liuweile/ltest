package com.alxad.view.banner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxWebAdListener;
import com.alxad.entity.AlxBannerUIData;
import com.alxad.util.AlxLog;
import com.alxad.widget.AlxAdWebView;
import com.alxad.widget.AlxLogoView;

public class AlxBannerWebView extends AlxBaseBannerView implements View.OnClickListener {
    private static final String TAG = "AlxBannerWebView";

    private Context mContext;
    private AlxLogoView mLogoView;
    private ImageView mCloseView;
    private AlxAdWebView mWebView;

    private volatile boolean isOnPageFinished = false;

    public AlxBannerWebView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AlxBannerWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AlxBannerWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.alx_banner_web, this, true);

        mLogoView = (AlxLogoView) findViewById(R.id.alx_logo);
        mCloseView = (ImageView) findViewById(R.id.alx_close);
        mWebView = (AlxAdWebView) findViewById(R.id.alx_web);

        setVisibility(View.GONE);
        isUIVisible(false);

        mCloseView.setOnClickListener(this);
        mWebView.setEventListener(mAlxWebAdListener);
        mWebView.setWebConfig();
    }

    @Override
    public void renderAd(AlxBannerUIData bean, int imageWidth, int imageHeight) {
        if (bean == null) {
            return;
        }
        isOnPageFinished = false;
        try {
            if (imageWidth > 0 && imageHeight > 0) {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageWidth, imageHeight);
                mWebView.setLayoutParams(params);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlxAgent.onError(e);
        }
        mWebView.loadHtml(bean.html);
    }

    @Override
    public int getCurrentViewType() {
        return VIEW_TYPE_WEBVIEW;
    }

    @Override
    public void onDestroy() {
        try {
            isOnPageFinished = false;
            isUIVisible(false);
            if (mWebView != null) {
                mWebView.onDestroy();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    @Override
    public View getCloseView() {
        return mCloseView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_close) {
            bnClose();
        }
    }

    private void bnClose() {
        if (mListener != null) {
            mListener.onViewClose();
        }
    }

    private void isUIVisible(boolean isShow) {
        if (mLogoView == null || mCloseView == null) {
            return;
        }
        if (isShow) {
            mLogoView.setVisibility(View.VISIBLE);
            if (isShowCloseBn) {
                mCloseView.setVisibility(View.VISIBLE);
            } else {
                mCloseView.setVisibility(View.GONE);
            }
        } else {
            mLogoView.setVisibility(View.GONE);
            mCloseView.setVisibility(View.GONE);
        }
    }

    public WebView getWebView() {
        return mWebView;
    }

    private AlxWebAdListener mAlxWebAdListener = new AlxWebAdListener() {
        @Override
        public void onViewClick(String url) {
            if (mListener != null) {
                mListener.onViewClick(url);
            }
        }

        @Override
        public void onViewShow() {
            setVisibility(View.VISIBLE);
            isUIVisible(true);

            if (mListener != null && !isOnPageFinished) {
                isOnPageFinished = true;
                mListener.onViewShow();
            }
        }

        @Override
        public void onViewError(String error) {
            if (mListener instanceof AlxBannerViewWebListener) {
                AlxBannerViewWebListener listener = (AlxBannerViewWebListener) mListener;
                listener.onWebError(error);
            }
        }

        @Override
        public void onWebLoading() {
            if (mListener instanceof AlxBannerViewWebListener) {
                AlxBannerViewWebListener listener = (AlxBannerViewWebListener) mListener;
                listener.onWebLoading();
            }
        }
    };

    @Override
    public void onViewVisible() {

    }

    @Override
    public void onViewHidden() {

    }

}