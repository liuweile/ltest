package com.alxad.view.nativead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.view.AlxBaseAdView;
import com.alxad.api.nativead.AlxMediaContent;
import com.alxad.view.banner.AlxBannerViewListener;

/**
 * 原生广告媒体基类
 *
 * @author lwl
 * @date 2022-9-9
 */
public abstract class AlxBaseNativeMediaView extends AlxBaseAdView {

    protected AlxBannerViewListener mListener;

    public AlxBaseNativeMediaView(@NonNull Context context) {
        super(context);
    }

    public AlxBaseNativeMediaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlxBaseNativeMediaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public AlxBaseNativeMediaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setEventListener(AlxBannerViewListener listener) {
        mListener = listener;
    }

    public abstract void onDestroy();

    public abstract AlxMediaContent getMediaContent();

    public abstract void setMediaContent(AlxMediaContent mediaContent);

    public void setImageScaleType(ImageView.ScaleType scaleType) {
    }

}