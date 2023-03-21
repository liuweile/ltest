package com.alxad.api;

import android.app.Activity;
import android.content.Context;

import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxVideoAdListener;
import com.alxad.control.AlxRewardVideoAdModel;
import com.alxad.util.AlxLog;

/**
 * Created by huangweiwu on 2021/4/20.
 */
public class AlxRewardVideoAD implements AlxAdInterface {
    private static final String TAG = "AlxRewardVideoAD";

    private Context mContext;

    private AlxVideoAdListener mAlxVideoADControlListener;
    private AlxRewardVideoADListener mAlxVideoListener = null;
    private AlxRewardVideoAdModel mController;

    @Override
    public double getPrice() {
        if (mController != null) {
            return mController.getPrice();
        }
        return 0;
    }

    @Override
    public void reportBiddingUrl() {
        if (mController != null) {
            mController.reportBiddingUrl();
        }
    }

    @Override
    public void reportChargingUrl() {
        if (mController != null) {
            mController.reportChargingUrl();
        }
    }

    /**
     * 加载广告
     *
     * @param context
     * @param pid
     * @param listener
     */
    public void load(Context context, String pid, AlxRewardVideoADListener listener) {
        loadData(context, pid, listener);
    }

    private void loadData(Context context, String pid, AlxRewardVideoADListener listener) {
        AlxLog.i(AlxLogLevel.OPEN, TAG, "rewardVideo-ad-init: pid=" + pid);
        mContext = context;
        mAlxVideoListener = listener;
        mAlxVideoADControlListener = new AlxVideoAdListener() {
            @Override
            public void onVideoAdLoaderError(int errorCode, String errorMsg) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onRewardedVideoAdFailed");
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardedVideoAdFailed(AlxRewardVideoAD.this, errorCode, errorMsg);
                }
            }

            @Override
            public void onVideoAdLoaded() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onRewardedVideoAdLoaded");
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardedVideoAdLoaded(AlxRewardVideoAD.this);
                }
            }

            @Override
            public void onAdFileCache(boolean isSuccess) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onAdFileCache:" + isSuccess);
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardVideoCache(isSuccess);
                }
            }

            @Override
            public void onVideoAdPlayStart() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onRewardedVideoAdPlayStart");
//                if (mAlxVideoListener != null) {
//                    mAlxVideoListener.onRewardedVideoAdPlayStart(AlxRewardVideoAD.this);
//                }
            }

            @Override
            public void onVideoAdPlayShow() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onRewardedVideoAdPlayShow");
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardedVideoAdPlayStart(AlxRewardVideoAD.this);
                }
            }

            @Override
            public void onVideoAdPlayProgress(int progress) {

            }

            @Override
            public void onVideoAdPlayOffset(int second) {

            }

            @Override
            public void onVideoAdPlayEnd() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onRewardedVideoAdPlayEnd");
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardedVideoAdPlayEnd(AlxRewardVideoAD.this);
                    mAlxVideoListener.onReward(AlxRewardVideoAD.this);
                }
            }

            @Override
            public void onVideoAdPlayStop() {

            }

            @Override
            public void onVideoAdPlaySkip() {

            }

            @Override
            public void onVideoAdPlayFailed(int errCode, String errMsg) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onRewardedVideoAdPlayFailed");
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardedVideoAdPlayFailed(AlxRewardVideoAD.this, 0, "video play stop!");
                }
            }

            @Override
            public void onVideoAdClosed() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onVideoAdClosed");
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardedVideoAdClosed(AlxRewardVideoAD.this);
                }
            }

            @Override
            public void onVideoAdPlayClicked() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onRewardedVideoAdPlayClicked");
                if (mAlxVideoListener != null) {
                    mAlxVideoListener.onRewardedVideoAdPlayClicked(AlxRewardVideoAD.this);
                }
            }
        };
        Context context1 = null;
        if (context != null) {
            context1 = context.getApplicationContext();
        }
        mController = new AlxRewardVideoAdModel(context1, pid, mAlxVideoADControlListener);
        mController.load();
    }

    public void showVideo(Activity activity) {
        if (mController == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showVideo: Ad not loaded or failed to load");
            return;
        }
        if (mController.isReady()) {
            if (activity == null) {
                mController.show(mContext);
            } else {
                mController.show(activity);
            }
        }
    }

    public boolean isReady() {
        if (mController == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "isReady: Ad not loaded");
            return false;
        }
        return mController.isReady();
    }

    public void destroy() {
        try {
            mAlxVideoListener = null;
            mAlxVideoADControlListener = null;
            if (mController != null) {
                mController.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}