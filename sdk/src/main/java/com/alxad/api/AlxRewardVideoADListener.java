package com.alxad.api;

public abstract class AlxRewardVideoADListener {
    public abstract void onRewardedVideoAdLoaded(AlxRewardVideoAD var1);

    public abstract void onRewardedVideoAdFailed(AlxRewardVideoAD var1, int errCode, String errMsg);

    public void onRewardedVideoAdPlayStart(AlxRewardVideoAD var1) {
    }

    public void onRewardedVideoAdPlayEnd(AlxRewardVideoAD var1) {
    }

    public void onRewardedVideoAdPlayFailed(AlxRewardVideoAD var2, int errCode, String errMsg) {
    }

    public void onRewardedVideoAdClosed(AlxRewardVideoAD var1) {
    }

    public void onRewardedVideoAdPlayClicked(AlxRewardVideoAD var1) {
    }

    public void onReward(AlxRewardVideoAD var1) {
    }

    /**
     * 视频文件下载成功或失败回调
     *
     * @param isSuccess true是下载成功,false下载失败
     */
    public void onRewardVideoCache(boolean isSuccess) {

    }
}
