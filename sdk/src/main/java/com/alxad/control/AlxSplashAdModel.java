package com.alxad.control;

import android.content.Context;
import android.view.ViewGroup;

import com.alxad.api.AlxSplashAdListener;
import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxViewListener;
import com.alxad.base.AlxJumpCallback;
import com.alxad.entity.AlxOmidBean;
import com.alxad.entity.AlxSplashUIData;
import com.alxad.entity.AlxTracker;
import com.alxad.net.impl.AlxSplashTaskImpl;
import com.alxad.net.lib.AlxAdLoadListener;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.omsdk.OmAdSafe;
import com.alxad.report.AlxReportManager;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxClickJump;
import com.alxad.util.AlxLog;
import com.alxad.view.splash.AlxSplashView;


/**
 * 开屏广告业务
 *
 * @author liuweile
 * @date 2022-4-12
 */
public class AlxSplashAdModel extends AlxBaseAdModel<AlxSplashUIData, ViewGroup> {
    private final String TAG = "AlxSplashAdModel";

    private Context mContext;
    private String mAdId;
    private int mTimeout;
    private AlxSplashAdListener mListener;
    private AlxTracker mTracker; //数据追踪器

    private OmAdSafe mOmAdSafe;

    public AlxSplashAdModel(Context context, String adId, int timeout, AlxSplashAdListener listener) {
        mContext = context;
        mAdId = adId;
        mTimeout = timeout;
        mListener = listener;
    }

    @Override
    public void load() {
        AlxLog.i(AlxLogLevel.OPEN, TAG, "splash-ad: pid=" + mAdId);
        isLoading = true;
        AlxRequestBean request = new AlxRequestBean(mAdId, AlxRequestBean.AD_TYPE_SPLASH);
        request.setRequestTimeout(mTimeout);

        AlxAdTask<AlxSplashUIData> task = new AlxSplashTaskImpl();
        task.startLoad(mContext, request, new AlxAdLoadListener<AlxSplashUIData>() {
            @Override
            public void onAdLoadSuccess(AlxRequestBean request, AlxSplashUIData response) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onAdLoaded");
                isLoading = false;
                isReady = true;
                mResponse = response;
                mRequestParams = request;

                if (mListener != null) {
                    mListener.onAdLoadSuccess();
                }
            }

            @Override
            public void onAdLoadError(AlxRequestBean request, int code, String msg) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onError:" + code + ";" + msg);
                isLoading = false;
                isReady = false;
                mResponse = null;
                mRequestParams = null;

                if (mListener != null) {
                    mListener.onAdLoadFail(code, msg);
                }
            }
        });
    }

    @Override
    public void show(ViewGroup containerView) {
        if (containerView == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "containerView params is empty");
            return;
        }
        if (mResponse == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd: Ad not loaded or failed to load");
            return;
        }


        if (mRequestParams != null) {
            mTracker = mRequestParams.getTracker();
        }
        AlxViewListener listener = new AlxViewListener() {

            @Override
            public void onViewClick() {
                if (mResponse != null) {
                    AlxReportManager.reportUrl(mResponse.clickTrackers, mResponse, AlxReportManager.LOG_TAG_CLICK);
                    AlxClickJump.openLink(mContext, mResponse.deeplink, mResponse.link, mResponse.bundle, mTracker, new AlxJumpCallback() {
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
                                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onUrlCallback(boolean isSuccess, int type) {
                            AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link open is " + isSuccess);
                        }
                    });
                }
                if (mListener != null) {
                    mListener.onAdClick();
                }
            }

            @Override
            public void onViewShow() {
                if (mResponse != null) {
                    AlxReportManager.reportUrl(mResponse.impressTrackers, mResponse, AlxReportManager.LOG_TAG_SHOW);
                }
                if (mListener != null) {
                    mListener.onAdShow();
                }
            }

            @Override
            public void onViewClose() {
                if (mListener != null) {
                    mListener.onAdDismissed();
                }
            }
        };
        AlxSplashView view = new AlxSplashView(mContext);
        view.setEventListener(listener);
        containerView.removeAllViews();
        containerView.addView(view);
        view.renderAd(mResponse);

        mOmAdSafe = new OmAdSafe();
        mOmAdSafe.initNoWeb(mContext, view, OmAdSafe.TYPE_NATIVE, getOmidBean());
        mOmAdSafe.reportLoad();
    }

    @Override
    public void destroy() {
        isLoading = false;
        isReady = false;
        mResponse = null;
        mRequestParams = null;

        if (mOmAdSafe != null) {
            mOmAdSafe.destroy();
        }
    }

    private AlxOmidBean getOmidBean() {
        try {
            if (mResponse == null || mResponse.extField == null) {
                return null;
            }
            return mResponse.extField.omid;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }

}