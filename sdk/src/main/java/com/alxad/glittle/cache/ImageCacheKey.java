package com.alxad.glittle.cache;

import android.text.TextUtils;

import com.alxad.base.AlxLogLevel;
import com.alxad.glittle.request.BaseRequestOptions;
import com.alxad.glittle.util.Util;
import com.alxad.util.AlxLog;
import com.alxad.util.MD5Util;

import java.io.Serializable;

public class ImageCacheKey implements Serializable {
    private static final String TAG = "ImageCacheKey";

    public String key;

    private ImageCacheKey(String key) {
        this.key = key;
    }

    public static ImageCacheKey getKey(String path, BaseRequestOptions options) {
        if (TextUtils.isEmpty(path) || options == null) {
            return new ImageCacheKey("");
        }
        AlxLog.i(AlxLogLevel.MARK,TAG, options.getViewWidth() + ";" + options.getViewHeight());

        StringBuilder sb = new StringBuilder();
        String separator = "||";
        sb.append(path);
        sb.append(separator);
        sb.append(options.getErrorId());
        sb.append(separator);
        sb.append(options.getPlaceholderId());
        sb.append(separator);
        sb.append(options.getViewWidth());
        sb.append(separator);
        sb.append(options.getViewHeight());
        String md5 = MD5Util.getUPMD5(sb.toString());
        return new ImageCacheKey(md5);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ImageCacheKey) {
            ImageCacheKey other = (ImageCacheKey) o;
            return Util.equals(key, other.key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = Util.hashCode(key, 31);
        return hashCode;
    }

}