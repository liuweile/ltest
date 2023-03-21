package com.alxad.glittle.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.alxad.glittle.target.CustomViewTarget;
import com.alxad.glittle.target.DrawableImageViewTarget;

public class ClassTypeFactory {

    @NonNull
    @SuppressWarnings("unchecked")
    public static <Z> CustomViewTarget<ImageView, Z> buildTarget(
            @NonNull ImageView view, @NonNull Class<Z> clazz) {
        try {
            return (CustomViewTarget<ImageView, Z>) new DrawableImageViewTarget(view);
        } catch (Exception e) {
            return null;
        }
    }

    public static <R, T> R getResource(T resource, Class<R> clazz) {
        if (resource == null || clazz == null) {
            return null;
        }
        R result = null;
        try {
            if (resource instanceof Drawable && Drawable.class.isAssignableFrom(clazz)) {
                result = (R) resource;
            } else if (resource instanceof Bitmap && Bitmap.class.isAssignableFrom(clazz)) {
                result = (R) resource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
