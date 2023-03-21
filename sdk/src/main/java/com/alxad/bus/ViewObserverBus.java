package com.alxad.bus;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;


/**
 * View观察的回调【显示还是隐藏】
 *
 * @author liuweile
 * @date 2022-9-2
 */
public class ViewObserverBus implements ViewTreeObserver.OnScrollChangedListener, ViewTreeObserver.OnGlobalLayoutListener {
//    private static final String TAG = "ViewShowBus";

    private View mView;
    private volatile boolean isAttachWindow = false;
    private Rect mRect = new Rect();
    private ViewObserverCallback mCallback;

    private boolean isBannerAd = false;

    public ViewObserverBus(View view, ViewObserverCallback callback) {
        this.mView = view;
        this.mCallback = callback;
    }

    public ViewObserverBus(View view, ViewObserverCallback callback, boolean isBannerAd) {
        this.mView = view;
        this.mCallback = callback;
        this.isBannerAd = isBannerAd;
    }

    /**
     * 在onAttachedToWindow方法中调用
     */
    public void viewAttachedToWindow() {
//        AlxLog.i(AlxLogLevel.MARK, TAG, "onAttachedToWindow");
        isAttachWindow = true;
        if (null != mView) {
            ViewTreeObserver observer = mView.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.addOnGlobalLayoutListener(this);
                observer.addOnScrollChangedListener(this);
            }
        }
        calcView();
    }

    /**
     * 在onDetachedFromWindow方法中调用
     */
    public void viewDetachedFromWindow() {
//        AlxLog.i(AlxLogLevel.MARK, TAG, "onDetachedFromWindow");
        isAttachWindow = false;
        if (null != mView) {
            ViewTreeObserver observer = mView.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnGlobalLayoutListener(this);
                observer.removeOnScrollChangedListener(this);
            }
        }
    }

    /**
     * 在onViewVisibilityChanged方法中调用
     */
    public void viewVisibilityChanged() {
//        AlxLog.i(AlxLogLevel.MARK, TAG, "onViewVisibilityChanged");
        calcView();
    }

    @Override
    public void onGlobalLayout() {
        calcView();
    }

    @Override
    public void onScrollChanged() {
        calcView();
    }

    /**
     * 判断View是否可见
     *
     * @return
     */
    public boolean isViewVisible() {
        if (null == mView) {
            return false;
        }

        if (isBannerAd) { // 注意banner可见算法不要mView.getLocalVisibleRect(mRect);
            return isAttachWindow && mView.isShown();
        } else {
            boolean isTrue = isAttachWindow && mView.isShown() && mView.getLocalVisibleRect(mRect);
            int viewSize = mView.getWidth() * mView.getHeight();
            if (isTrue && viewSize > 0) {
                int rectSize = mRect.width() * mRect.height();
                int var = rectSize * 100 / viewSize;
                if (var <= 0) {
                    isTrue = false;
                }
            }
            return isTrue;
        }
    }

    /**
     * 计算View是否可见
     */
    private void calcView() {
        boolean isTrue = isViewVisible();
        if (isTrue) {
            onViewVisible();
        } else {
            onViewHidden();
        }
    }

    /**
     * View 可见 =onAttachToWindow + View.VISIBLE + View.getLocalVisibleRect()
     */
    public void onViewVisible() {
        if (mCallback != null) {
            mCallback.onViewVisible();
        }
    }

    /**
     * View 不可见
     */
    public void onViewHidden() {
        if (mCallback != null) {
            mCallback.onViewHidden();
        }
    }

}