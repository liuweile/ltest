package com.alxad.glittle.target;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.glittle.request.Request;
import com.alxad.glittle.request.SizeReadyCallback;


public interface Target<R> {

    int SIZE_ORIGINAL = Integer.MIN_VALUE;

    void onLoadStarted(@Nullable Drawable placeholder);

    void onLoadFailed(@Nullable Drawable errorDrawable);

    void onResourceReady(@NonNull R resource);

    void onLoadCleared(@Nullable Drawable placeholder);

    void setRequest(@Nullable Request request);

    void getSize(@NonNull SizeReadyCallback cb);

    void removeCallback(@NonNull SizeReadyCallback cb);

    @Nullable
    Request getRequest();

}