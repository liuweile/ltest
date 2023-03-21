package com.alxad.view.banner;

public interface AlxBannerViewWebListener extends AlxBannerViewListener{

    /**
     * 对应的是WebViewClient.onReceivedError  方法中的回调
     * @param error
     */
    void onWebError(String error);

    /**
     * web加载中
     */
    void onWebLoading();
}