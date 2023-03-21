package com.alxad.api.nativead;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.alxad.view.nativead.AlxBaseNativeAdView;


/**
 * 原生广告容器：必须将此View作为原生广告的根布局，否则会影响广告收益
 *
 * @author lwl
 * @date 2022-9-14
 */
public class AlxNativeAdView extends AlxBaseNativeAdView {

    public AlxNativeAdView(Context context) {
        super(context);
    }

    public AlxNativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlxNativeAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AlxNativeAdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public View getTitleView() {
        return mViews.get("1");
    }

    public void setTitleView(View view) {
        this.mViews.put("1", view);
    }

    public View getDescriptionView() {
        return mViews.get("2");
    }

    public void setDescriptionView(View view) {
        this.mViews.put("2", view);
    }

    public View getCallToActionView() {
        return mViews.get("3");
    }

    public void setCallToActionView(View view) {
        mViews.put("3", view);
    }

    public View getIconView() {
        return mViews.get("4");
    }

    public void setIconView(View view) {
        mViews.put("4", view);
    }

    public View getImageView() {
        return mViews.get("5");
    }

    public void setImageView(View view) {
        mViews.put("5", view);
    }

    public View getPriceView() {
        return mViews.get("6");
    }

    public void setPriceView(View view) {
        mViews.put("6", view);
    }

    public View getAdSourceView() {
        return mViews.get("7");
    }

    public void setAdSourceView(View view) {
        mViews.put("7", view);
    }

    public AlxMediaView getMediaView() {
        return mMediaView;
    }

    public void setMediaView(AlxMediaView view) {
        mMediaView = view;
        mViews.put("8", view);
    }

    public void setCloseView(View view) {
        mCloseView = view;
    }

    public View getCloseView() {
        return mCloseView;
    }

    /**
     * 将View添加到点击事件中，关闭按钮就不在此添加
     *
     * @param key
     * @param value
     */
    public void addView(String key, View value) {
        mViews.put(key, value);
    }

    public void setNativeAd(AlxNativeAd nativeAd) {
        super.setNativeAd(nativeAd);
    }

    public void destroy() {
        super.destroy();
    }

}