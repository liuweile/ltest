package com.alxad.view.nativead;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.api.nativead.AlxMediaContent;
import com.alxad.api.nativead.AlxMediaView;
import com.alxad.base.AlxLogLevel;
import com.alxad.control.nativead.AlxImageImpl;
import com.alxad.control.nativead.AlxMediaContentImpl;
import com.alxad.entity.AlxNativeMediaUIStatus;
import com.alxad.entity.AlxNativeUIData;
import com.alxad.glittle.Glittle;
import com.alxad.util.AlxLog;

import java.util.List;

/**
 * 原生广告: 媒体View-图片
 *
 * @author liuweile
 * @date 2022-9-14
 */
public class AlxNativeImageView extends AlxBaseNativeMediaView implements View.OnClickListener {
    private static final String TAG = "AlxNativeImageView";

    private Context mContext;

    //封面View和落地页View
    private ImageView mImageView;
    private AlxMediaContentImpl mMediaContent;

    private boolean isViewVisible = false;
    private boolean isViewHidden = false;

    public AlxNativeImageView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AlxNativeImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AlxNativeImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AlxNativeImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.alx_native_media_image, this, true);
        mImageView = (ImageView) findViewById(R.id.alx_native_image);
        uiVideoShown(false);
        mImageView.setOnClickListener(this);
    }

    public AlxMediaContent getMediaContent() {
        return mMediaContent;
    }

    public void setMediaContent(AlxMediaContent mediaContent) {
        if (mediaContent instanceof AlxMediaContentImpl) {
            AlxMediaContentImpl obj = (AlxMediaContentImpl) mediaContent;
            uiVideoShown(true);
            setViewSize(obj);
            setVisibility(View.VISIBLE);
            mMediaContent = obj;
            if (isViewVisible()) {
                renderAd();
            }
        } else {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "setMediaContent: mediaContent is null");
        }
    }

    public void setImageScaleType(ImageView.ScaleType scaleType) {
        if (mImageView != null) {
            mImageView.setScaleType(scaleType);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mMediaContent != null && mMediaContent.getMediaUIStatus() != null) {
            AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
            status.setHasRenderData(false);
        }
    }

    @Override
    public void onViewVisible() {
        if (mMediaContent != null) {
            isViewHidden = false;
            if (isViewVisible) {
                return;
            }
            isViewVisible = true;
            renderAd();
        }
    }

    @Override
    public void onViewHidden() {
        isViewVisible = false;
        isViewHidden = true;
    }

    private void setViewSize(AlxMediaContentImpl mediaContent) {
        if (mediaContent == null) {
            return;
        }
        try {
            AlxNativeUIData bean = mediaContent.getData();
            if (bean == null) {
                return;
            }

            List<AlxImageImpl> list = bean.json_imageList;
            if (list == null || list.isEmpty()) {
                return;
            }
            AlxImageImpl imageBean = list.get(0);

            int width = imageBean.getWidth();
            int height = imageBean.getHeight();

            boolean isResetHeight = true;
            if (getParent() instanceof AlxMediaView) {
                AlxMediaView parentView = (AlxMediaView) getParent();
                if (parentView.getLayoutParams() != null) {
                    int parentHeight = parentView.getLayoutParams().height;
                    if (parentHeight == ViewGroup.LayoutParams.MATCH_PARENT || parentHeight > 0) {
                        isResetHeight = false;
                    }
                }
            }
            AlxLog.d(AlxLogLevel.MARK, TAG, "setViewSize(): isResetHeight=" + isResetHeight);
            if (getLayoutParams().height <= 0 && isResetHeight) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "setViewSize(): set height=" + height);
                LayoutParams params = (LayoutParams) mImageView.getLayoutParams();
                params.height = height;
                mImageView.setLayoutParams(params);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    /**
     * 广告开始曝光
     */
    private void renderAd() {
        if (mMediaContent == null) {
            return;
        }
        AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
        if (status == null) {
            status = new AlxNativeMediaUIStatus();
            mMediaContent.setMediaUIStatus(status);
        }
        if (status.isHasRenderData()) {
            return;
        }
        status.setHasRenderData(true);

        AlxLog.d(AlxLogLevel.MARK, TAG, "renderAd()");
        AlxNativeUIData bean = mMediaContent.getData();
        uiVideoShown(true);
        showImgViewUI(bean);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_native_image) {
            adClickAction();
        }
    }

    /**
     * 广告点击跳转：
     * 优先deeplink
     */
    private void adClickAction() {
        if (mMediaContent == null) {
            return;
        }
        String url = null;
        AlxNativeUIData bean = mMediaContent.getData();
        if (bean != null) {
            url = bean.json_link;
        }
        AlxLog.d(AlxLogLevel.MARK, TAG, "Click Url: " + url);
        if (mListener != null) {
            mListener.onViewClick(url);
        }
    }

    private void uiVideoShown(boolean isShow) {
        if (mImageView == null) {
            return;
        }
        if (isShow) {
            mImageView.setVisibility(View.VISIBLE);
        } else {
            mImageView.setVisibility(View.GONE);
        }
    }

    public void onDestroy() {
        try {
            if (mMediaContent != null && mMediaContent.getMediaUIStatus() != null) {
                AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
                status.destroy();
            }
            mMediaContent = null;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    /**
     * 落地页图片
     * 1：先取落地页本地图片
     * 2：1获取失败，取落地页网络图片
     * 3：2获取失败，取视频的第一帧图片
     */
    private void showImgViewUI(AlxNativeUIData bean) {
        if (mMediaContent != null && mMediaContent.getImage() != null) {
            try {
                mImageView.setImageDrawable(mMediaContent.getImage());
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.MARK, TAG, "showImgViewUI(): MediaContent.getImage() is null");
            }
            return;
        }

        if (bean == null) {
            return;
        }
        try {
            List<AlxImageImpl> list = bean.json_imageList;
            if (list == null || list.isEmpty()) {
                return;
            }
            AlxImageImpl imageBean = list.get(0);
            String imgUrl = imageBean.getImageUrl();

            AlxLog.i(AlxLogLevel.MARK, TAG, "showImgViewUI()");

            Glittle.with(mContext).load(imgUrl).into(mImageView);
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

}