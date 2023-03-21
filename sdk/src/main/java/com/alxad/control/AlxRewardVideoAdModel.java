package com.alxad.control;

import android.content.Context;

import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxVideoAdListener;
import com.alxad.entity.AlxTracker;
import com.alxad.entity.AlxVideoUIData;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.net.impl.AlxVideoTaskImpl;
import com.alxad.net.lib.AlxAdLoadListener;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.report.AlxReportManager;
import com.alxad.report.AlxReportPlaceHolder;
import com.alxad.util.AlxLog;
import com.alxad.view.video.AlxVideoActivity;

import java.util.List;

/**
 * 激励视频广告业务
 *
 * @author liuweile
 * @date 2022-4-12
 */
public class AlxRewardVideoAdModel extends AlxBaseAdModel<AlxVideoUIData, Context> {
    private static final String TAG = "AlxRewardVideoAdModel";

    private String mAdId;
    private Context mContext;

    public AlxVideoAdListener mListener;

    public AlxRewardVideoAdModel(Context context, String adId, AlxVideoAdListener listener) {
        mContext = context;
        mAdId = adId;
        setViewListener(listener);
    }

    @Override
    public void load() {
        AlxLog.i(AlxLogLevel.OPEN, TAG, "reward-video-ad: pid=" + mAdId);
        isLoading = true;
        AlxRequestBean request = new AlxRequestBean(mAdId, AlxRequestBean.AD_TYPE_REWARD);
        AlxAdTask<AlxVideoUIData> task = new AlxVideoTaskImpl();
        task.startLoad(mContext, request, new AlxAdLoadListener<AlxVideoUIData>() {

            @Override
            public void onAdLoadSuccess(AlxRequestBean request, AlxVideoUIData response) {
                isReady = true;
                isLoading = false;
                mRequestParams = request;
                mResponse = response;
                if (mListener != null) {
                    mListener.onVideoAdLoaded();
                }
            }

            @Override
            public void onAdLoadError(AlxRequestBean request, int code, String msg) {
                isReady = false;
                isLoading = false;
                mRequestParams = null;
                mResponse = null;
                if (mListener != null) {
                    mListener.onVideoAdLoaderError(code, msg);
                }
            }

            @Override
            public void onAdFileCache(boolean isSuccess) {
                if (mListener != null) {
                    mListener.onAdFileCache(isSuccess);
                }
            }
        });
    }


    @Override
    public void show(Context activity) {
        if (!isReady()) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showVideo:isReady=false");
            return;
        }

        if (mResponse == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showVideo:model is null");
            return;
        }

        AlxTracker mTrace = null;
        if (mRequestParams != null) {
            mTrace = mRequestParams.getTracker();
        }

        if (isReady && mResponse != null && activity != null) {
            try {
                AlxVideoActivity.setVideoEventListener(mResponse.id, mListener);
                AlxVideoActivity.openActivity(activity, mResponse, mTrace, true);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.OPEN, TAG, "showVideo failed:" + e.getMessage());
            }
        } else {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showVideo: open failed");
        }
    }

    @Override
    public void destroy() {
        isLoading = false;
        isReady = false;
        mResponse = null;
        mRequestParams = null;
        mListener = null;
    }

    private void setViewListener(final AlxVideoAdListener listener) {
        mListener = new AlxVideoAdListener() {
            @Override
            public void onVideoAdLoaded() {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdLoaded");
                if (listener != null) {
                    listener.onVideoAdLoaded();
                }
            }

            @Override
            public void onVideoAdLoaderError(int errorCode, String errorMsg) {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdLoaderError");
                if (listener != null) {
                    listener.onVideoAdLoaderError(errorCode, errorMsg);
                }
            }

            @Override
            public void onAdFileCache(boolean isSuccess) {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onAdFileCache");
                if (listener != null) {
                    listener.onAdFileCache(isSuccess);
                }
            }

            @Override
            public void onVideoAdPlayStart() {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayStart");
                reportVideoAdPlayStart();
                if (listener != null) {
                    listener.onVideoAdPlayStart();
                }
            }

            @Override
            public void onVideoAdPlayShow() {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayShow");
                reportVideoAdPlayShow();
                if (listener != null) {
                    listener.onVideoAdPlayShow();
                }
            }

            @Override
            public void onVideoAdPlayProgress(int progress) {
//                AlxLog.i(TAG, "onVideoAdPlayProgress:" + progress);//进度不停的打印，先关闭，激励视频类有打印
                if (mResponse != null && mResponse.video != null) {
                    switch (progress) {
                        case 25:
                            AlxLog.i(AlxLogLevel.MARK, TAG, "onVideoAdPlayOneQuarter");
                            AlxReportManager.reportUrl(mResponse.video.firstQuartileList, mResponse, "play-0.25");
                            break;
                        case 50:
                            AlxLog.i(AlxLogLevel.MARK, TAG, "onVideoAdPlayMiddlePoint");
                            AlxReportManager.reportUrl(mResponse.video.midPointList, mResponse, "play-0.5");
                            break;
                        case 75:
                            AlxLog.i(AlxLogLevel.MARK, TAG, "onVideoAdPlayThreeQuarter");
                            AlxReportManager.reportUrl(mResponse.video.thirdQuartileList, mResponse, "play-0.75");
                            break;
                    }
                }
                if (listener != null) {
                    listener.onVideoAdPlayProgress(progress);
                }
            }

            @Override
            public void onVideoAdPlayOffset(int second) {
                reportVideoAdPlayOffset(second);
                if (listener != null) {
                    listener.onVideoAdPlayOffset(second);
                }
            }

            @Override
            public void onVideoAdPlayEnd() {
                reportVideoAdPlayEnd();
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayEnd");
                if (listener != null) {
                    listener.onVideoAdPlayEnd();
                }
            }

            @Override
            public void onVideoAdPlayStop() {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayStop");
                if (listener != null) {
                    listener.onVideoAdPlayStop();
                }
            }

            @Override
            public void onVideoAdPlaySkip() {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlaySkip");
                if (listener != null) {
                    listener.onVideoAdPlaySkip();
                }
            }

            @Override
            public void onVideoAdPlayFailed(int errCode, String errMsg) {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayFailed:" + errCode + ";" + errMsg);
                reportVideoAdError(errCode);
                if (listener != null) {
                    listener.onVideoAdPlayFailed(errCode, errMsg);
                }
            }

            @Override
            public void onVideoAdClosed() {
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdClosed");
                if (listener != null) {
                    listener.onVideoAdClosed();
                }
            }

            @Override
            public void onVideoAdPlayClicked() {
                reportVideoAdClick();
                AlxLog.i(AlxLogLevel.OPEN, TAG, "onVideoAdPlayClicked");
                if (listener != null) {
                    listener.onVideoAdPlayClicked();
                }
            }
        };
    }

    // 上报事件
    public void reportVideoAdPlayStart() {
        AlxLog.e(AlxLogLevel.REPORT, TAG, "reportVideoAdPlayStart");
        if (mResponse != null && mResponse.video != null) {
            AlxReportManager.reportUrl(mResponse.video.startList, mResponse, "play-start");
        }
    }

    public void reportVideoAdPlayShow() {
        AlxLog.e(AlxLogLevel.REPORT, TAG, "reportVideoAdPlayShow");
        if (mResponse != null) {
            AlxReportManager.reportUrl(mResponse.impressTrackers, mResponse, AlxReportManager.LOG_TAG_SHOW);
            if (mResponse.video != null) {
                AlxReportManager.reportUrl(mResponse.video.impressList, mResponse, AlxReportManager.LOG_TAG_SHOW);
            }
        }
    }

    public void reportVideoAdPlayOffset(int second) {
        if (mResponse != null && mResponse.video != null) {
            AlxVideoVastBean video = mResponse.video;
            if (video != null && video.progressList != null && !video.progressList.isEmpty()) {
                for (AlxVideoVastBean.ProgressReportData item : video.progressList) {
                    if (item != null && item.offset == second) {
                        AlxLog.e(AlxLogLevel.REPORT, TAG, "reportVideoAdPlayOffset:" + second);
                        AlxReportManager.reportUrl(item.urlList, mResponse, "play-offset");
                        break;
                    }
                }
            }
        }
    }

    public void reportVideoAdPlayEnd() {
        AlxLog.e(AlxLogLevel.REPORT, TAG, "reportVideoAdPlayEnd");
        if (mResponse != null && mResponse.video != null) {
            AlxReportManager.reportUrl(mResponse.video.completeList, mResponse, "play-complete");
        }
    }

    public void reportVideoAdClick() {
        AlxLog.e(AlxLogLevel.REPORT, TAG, "reportVideoAdClick");
        if (mResponse != null) {
            AlxReportManager.reportUrl(mResponse.clickTrackers, mResponse, AlxReportManager.LOG_TAG_CLICK);
            if (mResponse.video != null) {
                AlxReportManager.reportUrl(mResponse.video.clickTrackingList, mResponse, AlxReportManager.LOG_TAG_CLICK);
            }
        }
    }

    public void reportVideoAdError(int errorCode) {
        AlxLog.e(AlxLogLevel.REPORT, TAG, "reportVideoAdError");
        if (mResponse != null && mResponse.video != null) {
            try {
                String vastErrorCode = String.valueOf(AlxReportManager.exchangeVideoVastErrorCode(errorCode));
                List<String> list = AlxReportManager.replaceUrlPlaceholder(mResponse.video.errorList, AlxReportPlaceHolder.VIDEO_VAST_ERROR, vastErrorCode);
                AlxReportManager.reportUrl(list, mResponse, "play-error");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}