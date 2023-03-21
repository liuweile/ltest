package com.alxad.control;

import android.content.Context;
import android.content.Intent;

import com.alxad.api.AlxInterstitialADListener;
import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxVideoAdListener;
import com.alxad.entity.AlxInterstitialUIData;
import com.alxad.entity.AlxTracker;
import com.alxad.entity.AlxVideoUIData;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.net.impl.AlxInterstitialTaskImpl;
import com.alxad.net.lib.AlxAdLoadListener;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.report.AlxReportManager;
import com.alxad.report.AlxReportPlaceHolder;
import com.alxad.util.AlxLog;
import com.alxad.view.interstitial.AlxInterstitialFullScreenWebActivity;
import com.alxad.view.video.AlxVideoActivity;

import java.util.List;

/**
 * 插屏广告业务
 *
 * @author liuweile
 * @date 2022-4-12
 */
public class AlxInterstitialAdModel extends AlxBaseAdModel<AlxInterstitialUIData, Context> {
    private static final String TAG = "AlxInterstitialAdModel";

    private Context mContext;
    private String mAdId;
    private AlxInterstitialADListener mListener;

    public AlxInterstitialAdModel(Context context, String adId, AlxInterstitialADListener listener) {
        mContext = context;
        mAdId = adId;
        setViewListener(listener);
    }

    @Override
    public void load() {
        AlxLog.i(AlxLogLevel.OPEN, TAG, "Interstitial-ad: pid=" + mAdId);
        isLoading = true;
        AlxRequestBean request = new AlxRequestBean(mAdId, AlxRequestBean.AD_TYPE_INTERSTITIAL);

        AlxAdTask<AlxInterstitialUIData> task = new AlxInterstitialTaskImpl();
        task.startLoad(mContext, request, new AlxAdLoadListener<AlxInterstitialUIData>() {
            @Override
            public void onAdLoadSuccess(AlxRequestBean request, AlxInterstitialUIData response) {
                isReady = true;
                isLoading = false;
                mRequestParams = request;
                mResponse = response;

                if (mListener != null) {
                    mListener.onInterstitialAdLoaded();
                }
            }

            @Override
            public void onAdLoadError(AlxRequestBean request, int code, String msg) {
                isReady = false;
                isLoading = false;
                mRequestParams = null;
                mResponse = null;
                if (mListener != null) {
                    mListener.onInterstitialAdLoadFail(code, msg);
                }
            }
        });
    }

    @Override
    public void show(Context activity) {
        if (activity == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd: context is null object");
            return;
        }

        if (mResponse == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd: Ad not loaded or failed to load");
            return;
        }
        AlxTracker tracker = null;
        if (mRequestParams != null) {
            tracker = mRequestParams.getTracker();
        }

        try {
            if (isReady() && mResponse != null) {
                if (mResponse.dataType == AlxInterstitialUIData.DATA_TYPE_VIDEO) {//视频
                    AlxVideoUIData videoUIData = exchangeData(mResponse);
                    if (videoUIData != null) {
                        AlxVideoActivity.setVideoEventListener(mResponse.id, mVideoListener);
                        AlxVideoActivity.openActivity(activity, videoUIData, tracker, false);
                    } else {
                        AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd failed: videoUIData object is empty");
                    }
                } else if (mResponse.dataType == AlxInterstitialUIData.DATA_TYPE_BANNER) {
                    AlxInterstitialFullScreenWebActivity.setEventListener(mResponse.id, mListener);
                    Intent intent = new Intent(activity, AlxInterstitialFullScreenWebActivity.class);
                    intent.putExtra(AlxInterstitialFullScreenWebActivity.EXTRA_DATA, mResponse);
                    intent.putExtra(AlxInterstitialFullScreenWebActivity.EXTRA_TRACKER, tracker);
                    activity.startActivity(intent);
                } else {
                    AlxLog.e(AlxLogLevel.OPEN, TAG, "This type of advertisement is not supported");
                }
            } else {
                AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd: Ad not loaded or failed to load");
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd failed:" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        isLoading = false;
        isReady = false;
        mRequestParams = null;
        mResponse = null;
        mListener = null;
    }

    private void setViewListener(final AlxInterstitialADListener listener) {
        mListener = new AlxInterstitialADListener() {

            @Override
            public void onInterstitialAdLoaded() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdLoaded");
                if (listener != null) {
                    listener.onInterstitialAdLoaded();
                }
            }

            @Override
            public void onInterstitialAdLoadFail(int errorCode, String errorMsg) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdLoadFail:" + errorCode + ";" + errorMsg);
                if (mResponse != null) {
                    try {
                        AlxVideoVastBean videoData = mResponse.video;
                        if (videoData != null) {
                            String vastErrorCode = String.valueOf(AlxReportManager.exchangeVideoVastErrorCode(errorCode));
                            List<String> list = AlxReportManager.replaceUrlPlaceholder(videoData.errorList, AlxReportPlaceHolder.VIDEO_VAST_ERROR, vastErrorCode);
                            AlxReportManager.reportUrl(list, mResponse, "load-error");
                        }
                    } catch (Exception e) {
                        AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                    }
                }

                if (listener != null) {
                    listener.onInterstitialAdLoadFail(errorCode, errorMsg);
                }
            }

            @Override
            public void onInterstitialAdClicked() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdClicked");
                if (mResponse != null) {
                    AlxReportManager.reportUrl(mResponse.clickTrackers, mResponse, AlxReportManager.LOG_TAG_CLICK);
                }
                if (listener != null) {
                    listener.onInterstitialAdClicked();
                }
            }

            @Override
            public void onInterstitialAdShow() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdShow");
                if (mResponse != null) {
                    AlxReportManager.reportUrl(mResponse.impressTrackers, mResponse, AlxReportManager.LOG_TAG_SHOW);
                }

                if (listener != null) {
                    listener.onInterstitialAdShow();
                }
            }

            @Override
            public void onInterstitialAdClose() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdClose");
                if (listener != null) {
                    listener.onInterstitialAdClose();
                }
            }

            @Override
            public void onInterstitialAdVideoStart() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdVideoStart");
                if (listener != null) {
                    listener.onInterstitialAdVideoStart();
                }
            }

            @Override
            public void onInterstitialAdVideoEnd() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdVideoEnd");
                if (listener != null) {
                    listener.onInterstitialAdVideoEnd();
                }
            }

            @Override
            public void onInterstitialAdVideoError(int errorCode, String errorMsg) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onInterstitialAdVideoError:" + errorCode + ";" + errorMsg);
                if (listener != null) {
                    listener.onInterstitialAdVideoError(errorCode, errorMsg);
                }
            }
        };
    }

    private AlxVideoAdListener mVideoListener = new AlxVideoAdListener() {

        @Override
        public void onVideoAdLoaded() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdLoaded");
        }

        @Override
        public void onVideoAdLoaderError(int errorCode, String errorMsg) {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdLoaderError:" + errorCode + ";" + errorMsg);
        }

        @Override
        public void onAdFileCache(boolean isSuccess) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onAdFileCache:" + isSuccess);
        }

        @Override
        public void onVideoAdPlayStart() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayStart");
            AlxVideoVastBean mAlxVideoRealData = getVideoObj();
            if (mAlxVideoRealData != null) {
                AlxReportManager.reportUrl(mAlxVideoRealData.startList, mResponse, "play-start");
            }
            if (mListener != null) {
                mListener.onInterstitialAdVideoStart();
            }
        }

        @Override
        public void onVideoAdPlayShow() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayShow");
            AlxVideoVastBean mAlxVideoRealData = getVideoObj();
            if (mAlxVideoRealData != null) {
                AlxReportManager.reportUrl(mAlxVideoRealData.impressList, mResponse, AlxReportManager.LOG_TAG_SHOW);
            }
            if (mListener != null) {
                mListener.onInterstitialAdShow();
            }
        }

        /**
         * 播放百分比进度
         *
         * @param progress (数值0-100)
         */
        @Override
        public void onVideoAdPlayProgress(int progress) {
            if (mResponse != null && mResponse.video != null) {
                switch (progress) {
                    case 25:
                        AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayOneQuarter");
                        AlxReportManager.reportUrl(mResponse.video.firstQuartileList, mResponse, "play-0.25");
                        break;
                    case 50:
                        AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayMiddlePoint");
                        AlxReportManager.reportUrl(mResponse.video.midPointList, mResponse, "play-0.5");
                        break;
                    case 75:
                        AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayThreeQuarter");
                        AlxReportManager.reportUrl(mResponse.video.thirdQuartileList, mResponse, "play-0.75");
                        break;
                }
            }
        }

        /**
         * 播放时间进度
         *
         * @param second (单位s)
         */
        @Override
        public void onVideoAdPlayOffset(int second) {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayOffset");
            reportVideoAdPlayOffset(second);
        }

        @Override
        public void onVideoAdPlayEnd() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayEnd");
            AlxVideoVastBean mAlxVideoRealData = getVideoObj();
            if (mAlxVideoRealData != null) {
                AlxReportManager.reportUrl(mAlxVideoRealData.completeList, mResponse, "play-complete");
            }
            if (mListener != null) {
                mListener.onInterstitialAdVideoEnd();
            }
        }

        @Override
        public void onVideoAdPlayStop() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayStop");
        }

        @Override
        public void onVideoAdPlaySkip() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlaySkip");
        }

        @Override
        public void onVideoAdPlayFailed(int errCode, String errMsg) {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayFailed:" + errCode + ";" + errMsg);
            if (mListener != null) {
                mListener.onInterstitialAdVideoError(errCode, errMsg);
            }
        }

        @Override
        public void onVideoAdClosed() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdClosed");
            if (mListener != null) {
                mListener.onInterstitialAdClose();
            }
        }

        @Override
        public void onVideoAdPlayClicked() {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayClicked");
            AlxVideoVastBean mAlxVideoRealData = getVideoObj();
            if (mAlxVideoRealData != null) {
                AlxReportManager.reportUrl(mAlxVideoRealData.clickTrackingList, mResponse, AlxReportManager.LOG_TAG_CLICK);
            }
            if (mListener != null) {
                mListener.onInterstitialAdClicked();
            }
        }

        private AlxVideoVastBean getVideoObj() {
            if (mResponse == null) {
                return null;
            }
            return mResponse.video;
        }

        private void reportVideoAdPlayOffset(int second) {
            AlxVideoVastBean mAlxVideoRealData = getVideoObj();
            if (mAlxVideoRealData != null && mAlxVideoRealData.progressList != null && !mAlxVideoRealData.progressList.isEmpty()) {
                for (AlxVideoVastBean.ProgressReportData item : mAlxVideoRealData.progressList) {
                    if (item != null && item.offset == second) {
                        AlxLog.e(AlxLogLevel.OPEN, TAG, "reportVideoAdPlayOffset:" + second);
                        AlxReportManager.reportUrl(item.urlList, mResponse, "play-offset");
                        break;
                    }
                }
            }
        }

    };


    private AlxVideoUIData exchangeData(AlxInterstitialUIData bean) {
        if (bean == null) {
            return null;
        }
        try {
            AlxVideoUIData result = new AlxVideoUIData();
            result.id = bean.id;
            result.bundle = bean.bundle;
            result.deeplink = bean.deeplink;
            result.width = bean.width;
            result.height = bean.height;
            result.clickTrackers = bean.clickTrackers;
            result.impressTrackers = bean.impressTrackers;
            result.price = bean.price;
            result.burl = bean.burl;
            result.nurl = bean.nurl;
            result.video = bean.video;
            return result;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }
}