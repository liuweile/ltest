package com.alxad.api;

/**
 * 开屏广告
 */
public abstract class AlxSplashAdListener {

    public abstract void onAdLoadSuccess();

    public abstract void onAdLoadFail(int errorCode, String errorMsg);

    public void onAdShow() {
    }

    public void onAdClick() {
    }

    /**
     * 倒计时结束 或 点击跳过按钮 时回调
     */
    public abstract void onAdDismissed();

}