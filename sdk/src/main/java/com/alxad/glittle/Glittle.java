package com.alxad.glittle;

import android.content.Context;
import android.text.TextUtils;

import com.alxad.util.AlxFileUtil;


public class Glittle {

    private static volatile Glittle glide;

    public static String CACHE_DIR;

    private static Glittle get() {
        if (glide == null) {
            synchronized (Glittle.class) {
                if (glide == null) {
                    glide = new Glittle();
                }
            }
        }
        return glide;
    }

    private Glittle() {

    }

    public static RequestManager with(Context context) {
        Glittle.get().setCacheDir(context);
        return new RequestManager(Glittle.get(), context);
    }

    public void setCacheDir(Context context) {
        if (!TextUtils.isEmpty(CACHE_DIR)) {
            return;
        }
        if (context == null) {
            return;
        }
        CACHE_DIR = AlxFileUtil.getImageSavePath(context);
    }

}