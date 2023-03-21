package com.alxad.glittle.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ObjectCheck {

    @NonNull
    public static <T> T checkNotNull(@Nullable T arg) {
        return checkNotNull(arg, "Argument must not be null");
    }

    @NonNull
    public static <T> T checkNotNull(@Nullable T arg, @NonNull String message) {
        if (arg == null) {
            throw new NullPointerException(message);
        }
        return arg;
    }

    public static void assertMainThread() {
        if (!Util.isOnMainThread()) {
            throw new IllegalArgumentException("You must call this method on the main thread");
        }
    }

}
