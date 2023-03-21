package com.alxad.base;

/**
 * 广告跳转后的回调
 * @author lwl
 * @date 2021-8-6
 *
 * <p>
 *     广告跳转的逻辑：
 *     1：优先判断deeplink
 *     2：如果deeplink打开失败，在打开url链接地址
 * </p>
 */
public interface AlxJumpCallback {
    /**
     * 默认
     */
    int TYPE_DEFAULT = 0;

    /**
     * app应用商店
     */
    int TYPE_APP_MARKET = 1;

    /**
     * deeplink
     */
    int TYPE_DEEPLINK = 2;

    /**
     * 浏览器
     */
    int TYPE_BROWSER = 3;

    /**
     * 打开手机已经安装的app
     */
    int TYPE_APP_INSTALLED_OPEN = 4;

    /**
     * deeplink 打开成功或失败回调
     */
    void onDeeplinkCallback(boolean isSuccess,String error);

    /**
     * url跳转后的回调
     * @param isSuccess 跳转成功
     * @param type 跳转类型
     */
    void onUrlCallback(boolean isSuccess,int type);
}