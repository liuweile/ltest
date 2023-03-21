package com.alxad.api;

import android.app.Activity;
import android.content.Context;

import com.alxad.base.AlxLogLevel;
import com.alxad.control.AlxInterstitialAdModel;
import com.alxad.util.AlxLog;

/**
 * Created by huangweiwu on 2021/5/17.
 */
public class AlxInterstitialAD implements AlxAdInterface {
    private static final String TAG = "AlxInterstitialAD";
    private Context mContext;

    private AlxInterstitialAdModel mController;

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

    public void load(Context context, String pid, final AlxInterstitialADListener listener) {
        mContext = context;
        Context context1 = null;
        if (context != null) {
            context1 = context.getApplicationContext();
        }
        mController = new AlxInterstitialAdModel(context1, pid, listener);
        mController.load();
    }

    public boolean show(Activity activity) {
        if (mController != null) {
            if (activity != null) {
                mController.show(activity);
            } else {
                mController.show(mContext);
            }
            return true;
        } else {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "show: Ad not loaded or failed to load");
        }
        return false;
    }

    public boolean isReady() {
        if (mController == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "isReady: Ad not loaded");
            return false;
        }
        return mController.isReady();
    }

    public void destroy() {
        if (mController != null) {
            mController.destroy();
        }
    }

}