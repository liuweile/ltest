package com.alxad.glittle.request;

import android.graphics.drawable.Drawable;

import com.alxad.glittle.util.Util;

import java.io.Serializable;

public class BaseRequestOptions<T extends BaseRequestOptions<T>> implements Serializable {
    private static final int UNSET = -1;

    protected int placeholderId;
    protected Drawable placeholderDrawable;
    protected int errorId;
    protected Drawable errorDrawable;
    protected int overrideWidth = UNSET;
    protected int overrideHeight = UNSET;
    protected boolean compress = true;//是否压缩

    //这里是通过计算得出的大小，不需要做对比，所以不需要写入equal和hash方法中
    private int viewWidth = 0;
    private int viewHeight = 0;

    public T placeholder(int resourceId) {
        this.placeholderId = resourceId;
        return self();
    }

    public T placeholder(Drawable drawable) {
        this.placeholderDrawable = drawable;
        return self();
    }

    public T error(int resourceId) {
        this.errorId = resourceId;
        return self();
    }

    public T error(Drawable drawable) {
        this.errorDrawable = drawable;
        return self();
    }

    public T resize(int width, int height) {
        this.overrideWidth = width;
        this.overrideHeight = height;
        return self();
    }

    public int getPlaceholderId() {
        return placeholderId;
    }

    public Drawable getPlaceholderDrawable() {
        return placeholderDrawable;
    }

    public int getErrorId() {
        return errorId;
    }

    public Drawable getErrorDrawable() {
        return errorDrawable;
    }

    private T self() {
        return (T) this;
    }

    public int getOverrideWidth() {
        return overrideWidth;
    }

    public void setOverrideWidth(int overrideWidth) {
        this.overrideWidth = overrideWidth;
    }

    public int getOverrideHeight() {
        return overrideHeight;
    }

    public void setOverrideHeight(int overrideHeight) {
        this.overrideHeight = overrideHeight;
    }

    public boolean isCompress() {
        return compress;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public void setViewWidth(int viewWidth) {
        this.viewWidth = viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public void setViewHeight(int viewHeight) {
        this.viewHeight = viewHeight;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BaseRequestOptions<?>) {
            BaseRequestOptions<?> other = (BaseRequestOptions<?>) o;
            return errorId == other.errorId
                    && Util.equals(errorDrawable, other.errorDrawable)
                    && placeholderId == other.placeholderId
                    && Util.equals(placeholderDrawable, other.placeholderDrawable)
                    && overrideWidth == other.overrideWidth
                    && overrideHeight == other.overrideHeight
                    && compress == other.compress;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = Util.hashCode(overrideWidth);
        hashCode = Util.hashCode(overrideHeight, hashCode);
        hashCode = Util.hashCode(errorId, hashCode);
        hashCode = Util.hashCode(errorDrawable, hashCode);
        hashCode = Util.hashCode(placeholderId, hashCode);
        hashCode = Util.hashCode(placeholderDrawable, hashCode);
        hashCode = Util.hashCode(compress, hashCode);
        return hashCode;
    }

}