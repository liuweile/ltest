package com.alxad.report;

/**
 * sdk 上报事件id、事件类型、事件名称
 *
 * @author lwl
 */
public interface AlxSdkDataEvent {

    /**
     * 广告请求后，响应状态结果
     */
    int HTTP_LOAD_RESPONSE = 100;

    /**
     * 广告请求后，有广告数据填充
     */
    int HTTP_AD_FILL_YES = 101;

    /**
     * 广告请求后，无广告数据填充
     */
    int HTTP_AD_FILL_NO = 102;

    /**
     * deeplink跳转成功
     */
    int DEEPLINK_YES = 103;
    /**
     * deeplink跳转失败
     */
    int DEEPLINK_NO = 104;
    /**
     * webview加载失败
     */
    int WEBVIEW_NO = 105;

    /**
     * 内置浏览器关闭按钮事件
     */
    int WEBVIEW_CLOSE = 106;

    /**
     * web广告开始加载
     */
    int WEBVIEW_AD_LOADING = 107;

    /**
     * web广告加载完成
     */
    int WEBVIEW_AD_LOADED = 108;
    /**
     * web广告关闭
     */
    int WEBVIEW_AD_CLOSE = 109;

}