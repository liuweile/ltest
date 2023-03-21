package com.alxad.base;

public interface AlxWebAdListener {

    /**
     * 对应的是WebViewClient.shouldOverrideUrlLoading 方法中的回调
     */
    void onViewClick(String url);

    /**
     * 对应的是WebViewClient.onPageFinished  方法中的回调
     */
    void onViewShow();

    /**
     * 对应的是WebViewClient.onReceivedError  方法中的回调
     *
     * @param error
     */
    void onViewError(String error);

    /**
     * web开始加载中，对应的是WebView.loadUrl() 或 loadDataWithBaseURL()
     */
    void onWebLoading();

}