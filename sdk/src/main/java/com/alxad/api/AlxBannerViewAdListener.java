package com.alxad.api;

/**
 * 为什么用抽象类而不直接用接口定义：
 * 1：考虑以后扩展回调方法时，可添加实体方法，而对于老版本的开发者不需要强制添加抽象方法去实现
 */
public abstract class AlxBannerViewAdListener {

    public abstract void onAdLoaded();

    public abstract void onAdError(int errorCode, String errorMsg);

    public void onAdClicked() {
    }

    public void onAdShow() {
    }

    public void onAdClose() {
    }

}