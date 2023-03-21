package com.alxad.glittle.target;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class DrawableImageViewTarget extends ImageViewTarget<Drawable> {

    public DrawableImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(@Nullable Drawable resource) {
        if (view != null) {
            view.setImageDrawable(resource);
        }
    }

}
