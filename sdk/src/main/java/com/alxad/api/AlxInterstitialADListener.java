package com.alxad.api;

/**
 * Created by huangweiwu on 2021/5/17.
 */
public abstract class AlxInterstitialADListener {
    public abstract void onInterstitialAdLoaded();

    public abstract void onInterstitialAdLoadFail(int errorCode, String errorMsg);

    public void onInterstitialAdClicked() {
    }

    public void onInterstitialAdShow() {
    }

    public void onInterstitialAdClose() {
    }

    public void onInterstitialAdVideoStart() {
    }

    public void onInterstitialAdVideoEnd() {
    }

    public void onInterstitialAdVideoError(int errorCode, String errorMsg) {
    }
}
