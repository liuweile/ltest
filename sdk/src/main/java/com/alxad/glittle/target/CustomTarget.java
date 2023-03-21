package com.alxad.glittle.target;


import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.glittle.request.Request;
import com.alxad.glittle.request.SizeReadyCallback;

public abstract class CustomTarget<T> implements Target<T> {

    private final int width;
    private final int height;

    private Request request;

    public CustomTarget() {
        this(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    public CustomTarget(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {

    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {

    }

    @Override
    public final void setRequest(@Nullable Request request) {
        this.request = request;
    }

    @Override
    public void getSize(@NonNull SizeReadyCallback cb) {
        if (cb != null) {
            cb.onSizeReady(width, height);
        }
    }

    @Override
    public void removeCallback(@NonNull SizeReadyCallback cb) {

    }

    @Nullable
    @Override
    public final Request getRequest() {
        return request;
    }

}