package com.alxad.base;

public interface AlxVideoAdListener {

    void onVideoAdLoaded();

    void onVideoAdLoaderError(int errorCode, String errorMsg);

    void onAdFileCache(boolean isSuccess);

    void onVideoAdPlayStart();

    void onVideoAdPlayShow();

    /**
     * 播放百分比进度
     *
     * @param progress (数值0-100)
     */
    void onVideoAdPlayProgress(int progress);

    /**
     * 播放时间进度
     *
     * @param second (单位s)
     */
    void onVideoAdPlayOffset(int second);

    void onVideoAdPlayEnd();

    void onVideoAdPlayStop();

    void onVideoAdPlaySkip();

    void onVideoAdPlayFailed(int errCode, String errMsg);

    void onVideoAdClosed();

    void onVideoAdPlayClicked();

}
