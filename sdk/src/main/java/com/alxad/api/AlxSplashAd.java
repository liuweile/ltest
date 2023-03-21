package com.alxad.api;

import android.content.Context;
import android.view.ViewGroup;

import com.alxad.base.AlxLogLevel;
import com.alxad.control.AlxSplashAdModel;
import com.alxad.util.AlxLog;

/**
 * 开屏广告
 *
 * @author lwl
 * @date 2021-8-4
 */
public class AlxSplashAd implements AlxAdInterface {
    private static final String TAG = "AlxSplashAd";

    private Context mContext;
    private String mAdId;
    private AlxSplashAdModel mController;

    public AlxSplashAd(Context context, String id) {
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        this.mAdId = id;
    }

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
     * @param listener
     */
    public void load(AlxSplashAdListener listener) {
        load(listener, 0);
    }

    /**
     * 加载广告
     *
     * @param listener
     * @param timeout  加载广告的超时时间: 单位毫秒
     */
    public void load(AlxSplashAdListener listener, int timeout) {
        mController = new AlxSplashAdModel(mContext, mAdId, timeout, listener);
        mController.load();
    }

    /**
     * 展示广告
     *
     * @param containerView
     */
    public void showAd(ViewGroup containerView) {
        if (containerView == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "containerView params is empty");
            return;
        }
        if (mController != null) {
            mController.show(containerView);
        } else {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showAd: Ad not loaded or failed to load");
        }
    }

    public void destroy() {
        if (mController != null) {
            mController.destroy();
        }
    }

}