package com.alxad.view.banner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.entity.AlxBannerUIData;
import com.alxad.view.AlxBaseAdView;

public abstract class AlxBaseBannerView extends AlxBaseAdView {

    /**
     * View 类型
     */
    public static final int VIEW_TYPE_WEBVIEW = 1;
    public static final int VIEW_TYPE_VIDEO = 2;

    protected AlxBannerViewListener mListener;

    protected boolean isShowCloseBn = false;//是否可以关闭广告，默认是不可关闭

    /**
     * 跟 AlxBannerUIData.dataType 同步
     */
    private int dataType;

    public AlxBaseBannerView(@NonNull Context context) {
        super(context);
    }

    public AlxBaseBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlxBaseBannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public AlxBaseBannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setEventListener(AlxBannerViewListener listener) {
        mListener = listener;
    }

    public abstract void renderAd(AlxBannerUIData bean, int imageWidth, int imageHeight);

    /**
     * 获取视图类型
     *
     * @return
     */
    public abstract int getCurrentViewType();


    public void onPause() {
    }


    public void onResume() {
    }

    public void setCanClosed(boolean canClosed) {
        isShowCloseBn = canClosed;
    }

    public abstract void onDestroy();

    public abstract View getCloseView();

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
}