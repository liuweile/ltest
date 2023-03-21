package com.alxad.entity;


/**
 * 记录原生广告中媒体UI中的状态
 *
 * @author liuweile
 * @date 2022-11-1
 */
public class AlxNativeMediaUIStatus {

    //防止重复渲染数据(离开窗体时注意要设置为false)
    private boolean isHasRenderData = false;

    //下面是存储Video的相关状态
    private int videoCurrentPosition = -1;//保存当前播放位置
    private boolean isVideoPlayComplete = false;//记录是否已经播放过一次
    private boolean isVideoImpression = false; //如果视频曝光过就不重复曝光
    private boolean isVideoHasReportQuarter = false; //记录视频播放四分之一上报
    private boolean isVideoHasReportHalf = false; //记录视频播放四分之一上报
    private boolean isVideoHasReportThreeFourths = false; //记录视频播放四分之三上报
    private Boolean isVideoMute;//记录视频静音状态
    private boolean isVideoPlayError = false;  //视频播放异常
    private boolean isVideoPlayVideo = false;//是否播放视频：包含状态：准备中，开始播放

    private boolean isVideoOnVideoStart = false;//存储onVideoStart() 回调方法是否已经执行过

    public int getVideoCurrentPosition() {
        return videoCurrentPosition;
    }

    public void setVideoCurrentPosition(int videoCurrentPosition) {
        this.videoCurrentPosition = videoCurrentPosition;
    }

    public boolean isVideoPlayComplete() {
        return isVideoPlayComplete;
    }

    public void setVideoPlayComplete(boolean videoPlayComplete) {
        isVideoPlayComplete = videoPlayComplete;
    }

    public boolean isHasRenderData() {
        return isHasRenderData;
    }

    public void setHasRenderData(boolean hasRenderData) {
        isHasRenderData = hasRenderData;
    }

    public boolean isVideoPlayError() {
        return isVideoPlayError;
    }

    public void setVideoPlayError(boolean videoPlayError) {
        isVideoPlayError = videoPlayError;
    }

    public boolean isVideoImpression() {
        return isVideoImpression;
    }

    public void setVideoImpression(boolean videoImpression) {
        isVideoImpression = videoImpression;
    }

    public boolean isVideoPlayVideo() {
        return isVideoPlayVideo;
    }

    public void setVideoPlayVideo(boolean videoPlayVideo) {
        isVideoPlayVideo = videoPlayVideo;
    }

    public void destroy() {
        isHasRenderData = false;
        isVideoPlayVideo = false;
    }

    public boolean isVideoHasReportQuarter() {
        return isVideoHasReportQuarter;
    }

    public void setVideoHasReportQuarter(boolean videoHasReportQuarter) {
        isVideoHasReportQuarter = videoHasReportQuarter;
    }

    public boolean isVideoHasReportHalf() {
        return isVideoHasReportHalf;
    }

    public void setVideoHasReportHalf(boolean videoHasReportHalf) {
        isVideoHasReportHalf = videoHasReportHalf;
    }

    public boolean isVideoHasReportThreeFourths() {
        return isVideoHasReportThreeFourths;
    }

    public void setVideoHasReportThreeFourths(boolean videoHasReportThreeFourths) {
        isVideoHasReportThreeFourths = videoHasReportThreeFourths;
    }

    public Boolean getVideoMute() {
        return isVideoMute;
    }

    public void setVideoMute(Boolean mute) {
        isVideoMute = mute;
    }

    public boolean isVideoOnVideoStart() {
        return isVideoOnVideoStart;
    }

    public void setVideoOnVideoStart(boolean videoOnVideoStart) {
        isVideoOnVideoStart = videoOnVideoStart;
    }
}