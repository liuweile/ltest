package com.alxad.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.bus.ViewObserverBus;
import com.alxad.bus.ViewObserverCallback;

/**
 * 所有广告视图的基类【像banner、原生广告】
 *
 * @author liuweile
 * @date 2022-11-7
 */
public abstract class AlxBaseAdView extends FrameLayout implements ViewObserverCallback {

    private Context mContext;
    private ViewObserverBus mViewObserverBus;

    public AlxBaseAdView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AlxBaseAdView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlxBaseAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public AlxBaseAdView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mViewObserverBus = new ViewObserverBus(this, this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewObserverBus != null) {
            mViewObserverBus.viewAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mViewObserverBus != null) {
            mViewObserverBus.viewDetachedFromWindow();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mViewObserverBus != null) {
            mViewObserverBus.viewVisibilityChanged();
        }
    }

    protected boolean isViewVisible() {
        if (mViewObserverBus != null) {
            return mViewObserverBus.isViewVisible();
        }
        return false;
    }

}