package com.alxad.glittle.download;

public interface ImageDownloadListener {

    void onStart(String url);

    /**
     * 针对同一个地址多次同时发起请求时，等待通知
     *
     * @param url
     * @param notify
     */
    void onWait(String url, String notify);

    void onSuccess(String url, String cachePath);

    void onError(String url, int code, String error);

}