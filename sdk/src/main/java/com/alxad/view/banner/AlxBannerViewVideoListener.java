package com.alxad.view.banner;

public interface AlxBannerViewVideoListener extends AlxBannerViewListener {

    void onVideoPlayCompletion();

    void onVideoPlayPause();

    void onVideoPlayStart();

    void onVideoPlayStop();

    /**
     * 视频声音改变回调
     *
     * @param mute 静音： true表示静音，false表示声音打开
     */
    void onVideoMute(boolean mute);

    void onVideoPlayFailed(int errCode, String errMsg);

    /**
     * 播放百分比进度
     *
     * @param progress (数值0-100)
     */
    void onVideoPlayProgress(int progress);

}
