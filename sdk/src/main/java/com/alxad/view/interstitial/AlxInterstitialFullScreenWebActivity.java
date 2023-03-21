package com.alxad.view.interstitial;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxInterstitialADListener;
import com.alxad.base.AlxBaseActivity;
import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxWebAdListener;
import com.alxad.entity.AlxInterstitialUIData;
import com.alxad.base.AlxJumpCallback;
import com.alxad.entity.AlxTracker;
import com.alxad.omsdk.OmAdSafe;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxClickJump;
import com.alxad.util.AlxLog;
import com.alxad.widget.AlxAdWebView;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.widget.AlxLogoView;
import com.iab.omid.library.algorixco.adsession.FriendlyObstructionPurpose;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 全屏web-插屏广告
 * 广告点击关闭时会延迟1s后在关闭，omsdk的webview销毁时需求是这样的
 *
 * @author lwl
 * @date 2021-10-26
 */
public class AlxInterstitialFullScreenWebActivity extends AlxBaseActivity implements View.OnClickListener {
    private final String TAG = "AlxInterstitialFullScreenWebActivity";
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_TRACKER = "tracker";

    Context mContext;

    private static ConcurrentHashMap<String, AlxInterstitialADListener> mMapListener = new ConcurrentHashMap<>();
    private AlxInterstitialADListener mEventListener = null;
    private AlxInterstitialUIData mAdObj;

    private AlxTracker mTracker; //数据追踪器

    private AlxLogoView mLogoView;
    private ImageView mBtnClose;
    private AlxAdWebView mWebView;

    private volatile boolean isOnPageFinished = false;
    private OmAdSafe mOmAdSafe;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.alx_activity_interstitial_full_screen_web);
        mContext = this;
        boolean isOk = initData();
        if (!isOk) {
            finish();
            return;
        }
        mOmAdSafe = new OmAdSafe();
        lockScreenOrientation();
        initView();
        showAd();
    }

    //锁定屏幕方向，使其不让旋转
    private void lockScreenOrientation() {
        if (mTracker == null) {
            return;
        }
        try {
            if (mTracker.screenOrientation == AlxRequestBean.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//竖屏
            } else if (mTracker.screenOrientation == AlxRequestBean.SCREEN_ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, e.getMessage());
            AlxAgent.onError(e);
        }
    }

    private boolean initData() {
        try {
            Intent intent = getIntent();
            if (intent == null) {
                AlxLog.i(AlxLogLevel.MARK, TAG, "intent is null");
                return false;
            }
            mAdObj = intent.getParcelableExtra(EXTRA_DATA);
            try {
                mTracker = intent.getParcelableExtra(EXTRA_TRACKER);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mAdObj == null) {
                return false;
            }

            if (!TextUtils.isEmpty(mAdObj.id)) {
                mEventListener = mMapListener.get(mAdObj.id);
            }

            if (mAdObj.dataType == AlxInterstitialUIData.DATA_TYPE_BANNER) {
                if (TextUtils.isEmpty(mAdObj.html)) {
                    return false;
                }
            } else {//其他类型广告类型暂时不支持
                return false;
            }
            return true;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            return false;
        }
    }

    private void initView() {
        mBtnClose = (ImageView) findViewById(R.id.alx_interstitial_close);
        mWebView = (AlxAdWebView) findViewById(R.id.alx_interstitial_web);
        mLogoView = (AlxLogoView) findViewById(R.id.alx_logo);

        mBtnClose.setOnClickListener(this);

        mWebView.setEventListener(new AlxWebAdListener() {
            @Override
            public void onViewClick(String url) {
                adClick(url);
                if (mEventListener != null) {
                    mEventListener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onViewShow() {
                AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_AD_LOADED);
                if (mOmAdSafe != null) {
                    mOmAdSafe.initWeb(mContext, mWebView);
                    mOmAdSafe.reportLoad();
                    mOmAdSafe.reportImpress();
                    mOmAdSafe.addFriendlyObstruction(mBtnClose, FriendlyObstructionPurpose.CLOSE_AD, "close");
                }
                if (mEventListener != null && !isOnPageFinished) {
                    isOnPageFinished = true;
                    mEventListener.onInterstitialAdShow();
                }
            }

            @Override
            public void onViewError(String error) {
                AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_NO);
            }

            @Override
            public void onWebLoading() {
                AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_AD_LOADING);
            }
        });
        mWebView.setWebConfig();
    }

    private void showAd() {
        try {
            mWebView.loadHtml(mAdObj.html);
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd():" + e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        try {
            AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_AD_CLOSE);
            if (mAdObj != null && !TextUtils.isEmpty(mAdObj.id)) {
                mMapListener.remove(mAdObj.id);
            }
//            if (mOmAdSafe != null) {
//                mOmAdSafe.destroy(); //omsdk 中的web销毁时，WebView需要停留1s后在销毁
//            }
            if (mWebView != null) {
                mWebView.onDestroy();
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //返回事件拦截，返回键返回不关闭广告
//        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AlxLog.i(AlxLogLevel.MARK, TAG, "onConfigurationChanged");
    }

    public static void setEventListener(String id, AlxInterstitialADListener listener) {
        if (!TextUtils.isEmpty(id) && listener != null) {
            mMapListener.put(id, listener);
        }
    }

    public void adClick(String url) {
        try {
            if (mAdObj == null) {
                return;
            }
            AlxClickJump.openLink(this, mAdObj.deeplink, url, mAdObj.bundle, mTracker, new AlxJumpCallback() {
                @Override
                public void onDeeplinkCallback(boolean isSuccess, String error) {
                    try {
                        if (isSuccess) {
                            AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link(Deeplink) open is true");
                            AlxSdkData.tracker(mTracker, AlxSdkDataEvent.DEEPLINK_YES);
                        } else {
                            AlxLog.i(AlxLogLevel.MARK, TAG, "Deeplink Open Failed: " + error);
                            AlxSdkData.tracker(mTracker, AlxSdkDataEvent.DEEPLINK_NO);
                        }
                    } catch (Exception e) {
                        AlxAgent.onError(e);
                        AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUrlCallback(boolean isSuccess, int type) {
                    AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link open is " + isSuccess);
                }
            });
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_interstitial_close) {
            if (mOmAdSafe != null) {
                mOmAdSafe.destroy(); //omsdk 中的web销毁时，WebView需要停留1s后在销毁
            }
            if (mHandler != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1100);
            }
            if (mEventListener != null) {
                mEventListener.onInterstitialAdClose();
            }
        }
    }

}