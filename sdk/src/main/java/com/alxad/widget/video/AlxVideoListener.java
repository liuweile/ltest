package com.alxad.widget.video;


/**
 * 视频播放监听器回调
 *
 * @author lwl
 * @date 2021-7-16
 */
public interface AlxVideoListener {

    /**
     * 缓冲开始
     */
    void onVideoBufferStart();

    /**
     * 缓冲结束
     */
    void onVideoBufferEnd();

    /**
     * 视频渲染开始
     */
    void onVideoRenderingStart();

    /**
     * 视频播放异常
     *
     * @param error
     */
    void onVideoError(String error);

    /**
     * 视频暂停
     */
    void onVideoPause();

    /**
     * 视频开始播放
     */
    void onVideoStart();

    /**
     * 视频播放完成
     */
    void onVideoCompletion();

    /**
     * 获取视频的宽高
     *
     * @param width
     * @param height
     */
    void onVideoSize(int width, int height);

    /**
     * 视频播放进度
     *
     * @param progress 播放进度 0 ~ 100
     */
    void onVideoPlayProgress(int progress);

    /**
     * 视频播放时长
     *
     * @param playTime  播放时长(单位s)
     * @param totalTime 视频总时长(单位s)
     */
    void onVideoPlayTime(int playTime, int totalTime);

    /**
     * 保存视频播放位置。如离开窗体时onDetachedFromWindow()的保存状态（主要处理Video在ListView和RecyclerView展示的情况）
     *
     * @param currentPosition 记录当前播放位置
     */
    void onVideoSaveInstanceState(int currentPosition);

}