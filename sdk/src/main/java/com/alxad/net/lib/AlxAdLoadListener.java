package com.alxad.net.lib;


/**
 * 广告回调方法
 * 为什么用抽象类而不直接用接口定义：
 * 1：考虑以后扩展回调方法时，可添加实体方法，而对于老版本的开发者不需要强制添加抽象方法去实现
 *
 * @author lwl
 * @date 2021-11-1
 */
public abstract class AlxAdLoadListener<T> {

    public abstract void onAdLoadSuccess(AlxRequestBean request, T response);

    public abstract void onAdLoadError(AlxRequestBean request, int code, String msg);

    //文件缓存是否成功【现在主要用在激励视频上】
    public void onAdFileCache(boolean isSuccess) {

    }

}