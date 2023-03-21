package com.alxad.view.nativead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.api.nativead.AlxMediaContent;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;

/**
 * 多媒体素材业务类
 *
 * @author lwl
 * @date 2022-11-17
 */
public class AlxMediaModelView extends FrameLayout {
    private final String TAG = "AlxMediaModelView";

    private Context mContext;
    protected AlxBaseNativeMediaView mMediaView;

    public AlxMediaModelView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AlxMediaModelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlxMediaModelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public AlxMediaModelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
    }

    public void setMediaContent(AlxMediaContent mediaContent) {
        try {
            if (mediaContent == null) {
                if (mMediaView != null) {
                    removeView(mMediaView);
                    mMediaView = null;
                }
                return;
            }

            if (mediaContent.hasVideo()) {
                if (mMediaView == null || !(mMediaView instanceof AlxNativeVideoView)) {
                    if (mMediaView != null) {
                        removeView(mMediaView);
                        mMediaView = null;
                    }
                    mMediaView = new AlxNativeVideoView(mContext);
                    addView(mMediaView);
                }
            } else {
                if (mMediaView == null || !(mMediaView instanceof AlxNativeImageView)) {
                    if (mMediaView != null) {
                        removeView(mMediaView);
                        mMediaView = null;
                    }
                    mMediaView = new AlxNativeImageView(mContext);
                    addView(mMediaView);
                }
            }
            if (mMediaView != null) {
                mMediaView.setMediaContent(mediaContent);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, "setMediaContent():" + e.getMessage());
        }
    }

    public void setImageScaleType(ImageView.ScaleType scaleType) {
        if (mMediaView != null) {
            mMediaView.setImageScaleType(scaleType);
        }
    }

    public void destroy() {
        try {
            if (mMediaView != null) {
                mMediaView.onDestroy();
                removeView(mMediaView);
                mMediaView = null;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, "destroy():" + e.getMessage());
        }
    }

}
