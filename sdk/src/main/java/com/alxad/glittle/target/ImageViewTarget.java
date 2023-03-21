package com.alxad.glittle.target;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class ImageViewTarget<Z> extends CustomViewTarget<ImageView, Z> {

    public ImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    public void onLoadStarted(@Nullable Drawable placeholder) {
        super.onLoadStarted(placeholder);
        setResourceInternal(null);
        setDrawable(placeholder);
    }

    @Override
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        setResourceInternal(null);
        setDrawable(errorDrawable);
    }

    @Override
    public void onResourceReady(@NonNull Z resource) {
        setResourceInternal(resource);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        setResourceInternal(null);
        setDrawable(placeholder);
    }

    protected abstract void setResource(@Nullable Z resource);

    private void setResourceInternal(@Nullable Z resource) {
        // Order matters here. Set the resource first to make sure that the Drawable has a valid and
        // non-null Callback before starting it.
        setResource(resource);
//        maybeUpdateAnimatable(resource);
    }

    public void setDrawable(Drawable drawable) {
        if (view != null) {
            view.setImageDrawable(drawable);
        }
    }

}