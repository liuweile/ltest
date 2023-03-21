package com.alxad.control.nativead;

import android.graphics.drawable.Drawable;

import com.alxad.api.nativead.AlxMediaContent;
import com.alxad.entity.AlxNativeMediaUIStatus;
import com.alxad.entity.AlxNativeUIData;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.util.AlxUtil;

public class AlxMediaContentImpl implements AlxMediaContent {

    private Drawable mImage;
    private AlxNativeUIData mData;
    private VideoLifecycleListener mVideoLifecycleListener;

    private AlxNativeMediaUIStatus mMediaUIStatus;

    public AlxMediaContentImpl(AlxNativeUIData data) {
        mData = data;
    }

    @Override
    public float getAspectRatio() {
        int defaultValue = 0;
        try {
            if (mData == null || mData.video == null) {
                return defaultValue;
            }
            AlxVideoVastBean video = mData.video;
            int width = AlxUtil.getInt(video.videoWidth);
            int height = AlxUtil.getInt(video.videoHeight);
            if (width <= 0 || height <= 0) {
                return defaultValue;
            }
            return (width * 1f) / height;
        } catch (Exception e) {

        }
        return defaultValue;
    }

    @Override
    public void setImage(Drawable drawable) {
        mImage = drawable;
    }

    @Override
    public Drawable getImage() {
        return mImage;
    }

    @Override
    public boolean hasVideo() {
        if (mData != null && mData.video != null && mData.dataType == AlxNativeUIData.DATA_TYPE_VIDEO) {
            return true;
        }
        return false;
    }

    @Override
    public VideoLifecycleListener getVideoLifecycleListener() {
        return mVideoLifecycleListener;
    }

    @Override
    public void setVideoLifecycleListener(VideoLifecycleListener listener) {
        mVideoLifecycleListener = listener;
    }

    public AlxNativeUIData getData() {
        return mData;
    }

    public AlxNativeMediaUIStatus getMediaUIStatus() {
        return mMediaUIStatus;
    }

    public void setMediaUIStatus(AlxNativeMediaUIStatus status) {
        this.mMediaUIStatus = status;
    }
}
