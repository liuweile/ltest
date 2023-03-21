package com.alxad.api.nativead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView.ScaleType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.view.nativead.AlxMediaModelView;

/**
 * 多媒体素材：视频或一张图片素材
 *
 * @author lwl
 * @date 2022-9-14
 */
public class AlxMediaView extends AlxMediaModelView {

    public AlxMediaView(@NonNull Context context) {
        super(context);
    }

    public AlxMediaView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlxMediaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public AlxMediaView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setMediaContent(AlxMediaContent mediaContent) {
        super.setMediaContent(mediaContent);
    }

    public void setImageScaleType(ScaleType scaleType) {
        super.setImageScaleType(scaleType);
    }

    public void destroy() {
        super.destroy();
    }

    public View getAdContentView() {
        return this.mMediaView;
    }

}