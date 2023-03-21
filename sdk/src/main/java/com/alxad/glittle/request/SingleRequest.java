package com.alxad.glittle.request;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.alxad.base.AlxLogLevel;
import com.alxad.glittle.cache.ImagePool;
import com.alxad.glittle.download.ImageDownloadListener;
import com.alxad.glittle.download.ImageDownloadManager;
import com.alxad.glittle.target.Target;
import com.alxad.glittle.util.ClassTypeFactory;
import com.alxad.glittle.util.Util;
import com.alxad.util.AlxLog;

import java.util.List;
import java.util.concurrent.Executor;

public class SingleRequest<R> implements Request, SizeReadyCallback {
    private static final String TAG = "SingleRequest";

    private static final int STATUS_IDLE = 0;//默认状态
    private static final int STATUS_LOADING = 1;//开始请求网络加载
    private static final int STATUS_LOAD_FAIL = 2;//加载完成
    private static final int STATUS_LOAD_SUCCESS = 3;//加载成功
    private static final int STATUS_CANCEL = 4;//取消加载

    private BaseRequestOptions<?> requestOptions;
    private String path;//请求地址

    private Context context;
    private final Object requestLock;
    private final Class<R> transcodeClass;
    private final Target<R> target;
    private final Executor callbackExecutor;

    private volatile int status;
    private Drawable errorDrawable;
    private Drawable placeholderDrawable;

//    private int width;
//    private int height;

    private final int overrideWidth;
    private final int overrideHeight;

    private R resource; //返回的结果

    public static <R> SingleRequest<R> obtain(
            Context context,
            Object requestLock,
            String path,
            Class<R> transcodeClass,
            BaseRequestOptions<?> requestOptions,
            int overrideWidth,
            int overrideHeight,
            Target<R> target,
            RequestListener<R> targetListener,
            @Nullable List<RequestListener<R>> requestListeners,
            Executor callbackExecutor) {
        return new SingleRequest<>(
                context,
                requestLock,
                path,
                transcodeClass,
                requestOptions,
                overrideWidth,
                overrideHeight,
                target,
                targetListener,
                requestListeners,
                callbackExecutor);
    }

    public SingleRequest(Context context,
                         Object requestLock,
                         String path,
                         Class<R> transcodeClass,
                         BaseRequestOptions<?> requestOptions,
                         int overrideWidth,
                         int overrideHeight,
                         Target<R> target,
                         RequestListener<R> targetListener,
                         List<RequestListener<R>> requestListeners,
                         Executor callbackExecutor) {
        this.context = context;
        this.path = path;
        this.transcodeClass = transcodeClass;
        this.requestOptions = requestOptions;
        this.overrideWidth = overrideWidth;
        this.overrideHeight = overrideHeight;
        this.target = target;
        this.callbackExecutor = callbackExecutor;
        status = STATUS_IDLE;
        if (requestLock == null) {
            this.requestLock = new Object();
        } else {
            this.requestLock = requestLock;
        }
    }

    @Override
    public void begin() {
        synchronized (requestLock) {
            if (TextUtils.isEmpty(path)) {
                if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
                    setSizeValue(overrideWidth, overrideHeight);
                }
                onLoadFailed("Received null path");
                return;
            }
            if (status == STATUS_CANCEL) {
                onLoadCleared();
                return;
            }
            if (status == STATUS_LOAD_FAIL) {
                onLoadFailed("status is fail");
                return;
            }

            if (status == STATUS_LOAD_SUCCESS) {
                getResourceReady(resource);
                return;
            }

            if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
                onSizeReady(overrideWidth, overrideHeight);
            } else {
                if (target != null) {
                    target.getSize(this);
                }
            }

            if (status == STATUS_LOADING) {
                onLoadStarted();
            }
        }
    }

    //取消当前的加载
    @Override
    public void clear() {
        synchronized (requestLock) {
            if (status == STATUS_CANCEL) {
                return;
            }
            if (resource != null) {
                resource = null;
            }
            status = STATUS_CANCEL;
            onLoadCleared();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public boolean isRunning() {
        synchronized (requestLock) {
            return status == STATUS_LOADING;
        }
    }

    @Override
    public boolean isComplete() {
        synchronized (requestLock) {
            return status == STATUS_LOAD_SUCCESS;
        }
    }

    @Override
    public boolean isCleared() {
        synchronized (requestLock) {
            return status == STATUS_CANCEL;
        }
    }

    @Override
    public boolean isAnyResourceSet() {
        return false;
    }

    private Drawable getErrorDrawable() {
        if (errorDrawable == null) {
            if (requestOptions != null) {
                errorDrawable = requestOptions.getErrorDrawable();
                if (errorDrawable == null && requestOptions.getErrorId() > 0) {
                    errorDrawable = loadDrawable(requestOptions.getErrorId());
                }
            }
        }
        return errorDrawable;
    }

    private void onLoadStarted() {
        if (target != null) {
            target.onLoadStarted(getPlaceholderDrawable());
        }
    }

    private void onLoadCleared() {
        if (target != null) {
            target.onLoadCleared(getPlaceholderDrawable());
            target.removeCallback(this);
        }
    }

    private void getResourceReady(R resource) {
        try {
            synchronized (requestLock) {
                if (resource == null) {
                    this.resource = null;
                    onLoadFailed("resource is null object");
                    return;
                }
                onResourceReady(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onResourceReady(R drawable) {
        synchronized (requestLock) {
            status = STATUS_LOAD_SUCCESS;
            this.resource = drawable;
            if (target != null) {
                target.onResourceReady(drawable);
            }
        }
    }

    private void onLoadFailed(String error) {
        synchronized (requestLock) {
            status = STATUS_LOAD_FAIL;
            if (target != null) {
                target.onLoadFailed(getErrorDrawable());
            }
        }
    }

    private Drawable getPlaceholderDrawable() {
        if (placeholderDrawable == null) {
            if (requestOptions != null) {
                placeholderDrawable = requestOptions.getPlaceholderDrawable();
                if (placeholderDrawable == null && requestOptions.getPlaceholderId() > 0) {
                    placeholderDrawable = loadDrawable(requestOptions.getPlaceholderId());
                }
            }
        }
        return placeholderDrawable;
    }


    private Drawable loadDrawable(@DrawableRes int resourceId) {
        if (context != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return context.getResources().getDrawable(resourceId, context.getTheme());
                } else {
                    return context.getResources().getDrawable(resourceId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void sendHttpRequest() {
        ImageDownloadManager.get().startDownload(path, mImageDownloadListener);
    }

    private ImageDownloadListener mImageDownloadListener = new ImageDownloadListener() {
        @Override
        public void onStart(String url) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onStart:" + url);
            onLoadStarted();
        }

        @Override
        public void onWait(String url, String notify) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onWait:" + url);
        }

        @Override
        public void onSuccess(String url, final String cachePath) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onSuccess:" + url);
            if (callbackExecutor == null) {
                return;
            }
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isCleared()) {
                            return;
                        }
                        //从内存中拿，而不是从文件中拿。防止同一个图片多处使用时重复的更新内存缓存
                        Drawable result = ImagePool.get().getCache(path, requestOptions);
                        R drawable = ClassTypeFactory.getResource(result, transcodeClass);
                        getResourceReady(drawable);


//                    if (drawable != null) {
//                        onResourceReady(drawable);
//                    } else {
//                        onLoadFailed("drawable is null object");
//                    }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onError(String url, int code, final String error) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onError:" + url);
            if (callbackExecutor == null) {
                return;
            }
            callbackExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isCleared()) {
                            return;
                        }
                        onLoadFailed(error);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private R getCacheResource() {
        Drawable result = ImagePool.get().getCache(path, requestOptions);
        R drawable = ClassTypeFactory.getResource(result, transcodeClass);
        return drawable;
    }

    @Override
    public boolean isEquivalentTo(Request o) {
        if (!(o instanceof SingleRequest)) {
            return false;
        }
        SingleRequest<?> other = (SingleRequest<?>) o;
        if (this == other) {
            return true;
        }

        int localOverrideWidth;
        int localOverrideHeight;
        String localPath;
        BaseRequestOptions<?> localRequestOptions;
        synchronized (requestLock) {
            localOverrideWidth = overrideWidth;
            localOverrideHeight = overrideHeight;
            localPath = path;
            localRequestOptions = requestOptions;
        }

        int otherLocalOverrideWidth;
        int otherLocalOverrideHeight;
        String otherLocalPath;
        BaseRequestOptions<?> otherLocalRequestOptions;
        synchronized (other.requestLock) {
            otherLocalOverrideWidth = other.overrideWidth;
            otherLocalOverrideHeight = other.overrideHeight;
            otherLocalPath = other.path;
            otherLocalRequestOptions = other.requestOptions;
        }
        return localOverrideWidth == otherLocalOverrideWidth
                && localOverrideHeight == otherLocalOverrideHeight
                && Util.equals(localPath, otherLocalPath)
                && Util.equals(localRequestOptions, otherLocalRequestOptions);
    }

    @Override
    public void onSizeReady(int width, int height) {
        synchronized (requestLock) {
            setSizeValue(width, height);
            //从缓存中获取
            R drawable = getCacheResource();
            if (drawable != null) {
                onResourceReady(drawable);
                return;
            }
            if (status == STATUS_LOADING) {
                return;
            }
            status = STATUS_LOADING;
//            //从缓存中获取
//            R drawable = getCacheResource();
//            if (drawable != null) {
//                onResourceReady(drawable);
//                return;
//            }
            //网络请求下载图片
            sendHttpRequest();
        }
    }

    private void setSizeValue(int width, int height) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "width=" + width + ";height=" + height);
//        this.width = width;
//        this.height = height;
        if (requestOptions != null) {
            requestOptions.setViewWidth(width);
            requestOptions.setViewHeight(height);
        }
    }

}