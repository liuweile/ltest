package com.alxad.glittle.cache;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.LruCache;

import com.alxad.glittle.Glittle;
import com.alxad.glittle.request.BaseRequestOptions;
import com.alxad.glittle.util.ImageFormat;
import com.alxad.glittle.util.Util;
import com.alxad.http.AlxHttpUtil;

import java.io.File;


public class ImagePool {

    private LruCache<ImageCacheKey, Drawable> mMemoryCache;

    private static volatile ImagePool mInstance;

    private ImagePool() {
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<ImageCacheKey, Drawable>(cacheSize) {

            @Override
            protected int sizeOf(ImageCacheKey key, Drawable value) {
                if (value instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) value).getBitmap();
                    return Util.getBitmapBytesSize(bitmap);
                }
//                else if (value instanceof AlxGifDrawable) { //不存入gif
//                    return ((AlxGifDrawable) value).getByteCount();
//                }
                return super.sizeOf(key, value);
            }

            /**
             * 对bitmap 做回收处理，如果直接用Bitmap.recycler() 回收
             * 会出现 trying to use a recycled bitmap android.graphics.Bitmap@41d的异常的问题
             * 原因是Bitmap recycle掉后，ImageView又重新加载Drawable 导致异常
             *
             */
//            @Override
//            protected void entryRemoved(boolean evicted, AlxMemCacheKey key, Drawable oldValue, Drawable newValue) {
//                super.entryRemoved(evicted,key,oldValue,newValue);
//            }
        };
    }

    public static ImagePool get() {
        if (mInstance == null) {
            synchronized (ImagePool.class) {
                if (mInstance == null) {
                    mInstance = new ImagePool();
                }
            }
        }
        return mInstance;
    }


    /**
     * 清理内存缓存
     */
    public void clearMemoryCache() {
        try {
            if (mMemoryCache != null) {
                mMemoryCache.evictAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putMemCache(ImageCacheKey key, Drawable drawable) {
        if (key == null || drawable == null) {
            return;
        }
        if (mMemoryCache != null) {
            synchronized (mMemoryCache) {
                mMemoryCache.put(key, drawable);
            }
        }
    }

    private Drawable getMemCache(ImageCacheKey key) {
        if (key == null) {
            return null;
        }
        if (mMemoryCache != null) {
            synchronized (mMemoryCache) {
                return mMemoryCache.get(key);
            }
        }
        return null;
    }

    public void removeKey(ImageCacheKey key) {
        if (key == null) {
            return;
        }
        if (mMemoryCache != null) {
            synchronized (mMemoryCache) {
                mMemoryCache.remove(key);
            }
        }
    }

    /**
     * 获取缓存图片<br/>
     * 1: 先获取内存缓存
     * 2：在获取sd卡缓存
     *
     * @param path
     * @param options
     * @return
     */
    public Drawable getCache(String path, BaseRequestOptions options) {
        if (TextUtils.isEmpty(path) || options == null) {
            return null;
        }
        Drawable drawable = getMemCache(path, options);
        if (drawable == null) {
            drawable = getDiskCache(path, options);
        }
        return drawable;
    }

    /**
     * 内存缓存（不缓存gif图片）
     *
     * @param path
     * @param options
     * @return
     */
    private Drawable getMemCache(String path, BaseRequestOptions options) {
        Drawable drawable = null;
        if (TextUtils.isEmpty(path) || options == null) {
            return null;
        }
        try {
            ImageCacheKey key = ImageCacheKey.getKey(path, options);
            drawable = getMemCache(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    /**
     * 文件缓存
     *
     * @param path
     * @param options
     * @return
     */
    public Drawable getDiskCache(String path, BaseRequestOptions options) {
        Drawable drawable = null;
        if (TextUtils.isEmpty(path) || options == null) {
            return null;
        }
        try {
            File file = getCacheFile(path);
            drawable = ImageFormat.decodeFile(file, options);
            if (drawable != null && (drawable instanceof BitmapDrawable)) {
                ImageCacheKey key = ImageCacheKey.getKey(path, options);
                putMemCache(key, drawable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawable;
    }

    /**
     * 获取本地文件
     *
     * @param path
     * @return
     */
    private File getCacheFile(String path) {
        File file = null;
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        try {
            if (path.toLowerCase().startsWith("http")) {//网络url获取缓存地址
                String dirPath = Glittle.CACHE_DIR;
                String fileName = AlxHttpUtil.getDownloadFileName(path);
                file = new File(dirPath, fileName);
            } else { //本地路径
                file = new File(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}