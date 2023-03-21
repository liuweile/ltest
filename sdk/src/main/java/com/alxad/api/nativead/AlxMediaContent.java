package com.alxad.api.nativead;

import android.graphics.drawable.Drawable;

/**
 * 视频媒体数据
 *
 * @author lwl
 * @date 2022-9-9
 */
public interface AlxMediaContent {

    /**
     * 媒体素材长宽比例
     *
     * @return
     */
    float getAspectRatio();

    void setImage(Drawable drawable);

    Drawable getImage();

    /**
     * 是否有视频
     *
     * @return
     */
    boolean hasVideo();

    VideoLifecycleListener getVideoLifecycleListener();

    void setVideoLifecycleListener(VideoLifecycleListener listener);

    public abstract static class VideoLifecycleListener {

        public VideoLifecycleListener() {
        }

        /**
         * 在视频播放第一次开始时调用
         */
        public void onVideoStart() {
        }

        /**
         * 视频播放结束时调用
         */
        public void onVideoEnd() {
        }

        /**
         * 在视频播放时调用【如：视频缓冲结束后继续播放、onVideoPause后继续播放】
         */
        public void onVideoPlay() {
        }

        /**
         * 视频播放暂停时调用
         */
        public void onVideoPause() {
        }

        public void onVideoPlayError(int code, String error) {
        }

        /**
         * 视频更改静音状态时调用
         *
         * @param isMute
         */
        public void onVideoMute(boolean isMute) {

        }
    }

}