package com.alxad.glittle.util;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.alxad.glittle.target.Target;

public class Util {
    private static final int HASH_MULTIPLIER = 31;
    private static final int HASH_ACCUMULATOR = 17;
    private static final char[] HEX_CHAR_ARRAY = "0123456789abcdef".toCharArray();
    // 32 bytes from sha-256 -> 64 hex chars.
    private static final char[] SHA_256_CHARS = new char[64];

    private static volatile Handler mainThreadHandler;

    public static void postOnUiThread(Runnable runnable) {
        getUiThreadHandler().post(runnable);
    }

    private static Handler getUiThreadHandler() {
        if (mainThreadHandler == null) {
            synchronized (Util.class) {
                if (mainThreadHandler == null) {
                    mainThreadHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mainThreadHandler;
    }

    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * 判断两个对象是否相等
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static int hashCode(int value) {
        return hashCode(value, HASH_ACCUMULATOR);
    }

    public static int hashCode(int value, int accumulator) {
        return accumulator * HASH_MULTIPLIER + value;
    }

    public static int hashCode(float value) {
        return hashCode(value, HASH_ACCUMULATOR);
    }

    public static int hashCode(float value, int accumulator) {
        return hashCode(Float.floatToIntBits(value), accumulator);
    }

    public static int hashCode(@Nullable Object object, int accumulator) {
        return hashCode(object == null ? 0 : object.hashCode(), accumulator);
    }

    public static int hashCode(boolean value, int accumulator) {
        return hashCode(value ? 1 : 0, accumulator);
    }

    public static int hashCode(boolean value) {
        return hashCode(value, HASH_ACCUMULATOR);
    }

    public static boolean isValidDimensions(int width, int height) {
        return isValidDimension(width) && isValidDimension(height);
    }

    private static boolean isValidDimension(int dimen) {
        return dimen > 0 || dimen == Target.SIZE_ORIGINAL;
    }

    /**
     * 得到bitmap大小
     *
     * @param bitmap
     * @return
     */
    public static int getBitmapBytesSize(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        int result = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                result = bitmap.getAllocationByteCount();
            } else {
                result = bitmap.getByteCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}