package com.alxad.glittle.download;

import android.text.TextUtils;

import com.alxad.base.AlxLogLevel;
import com.alxad.glittle.Glittle;
import com.alxad.http.AlxDownloadRealCall;
import com.alxad.http.AlxHttpMethod;
import com.alxad.http.AlxHttpRequest;
import com.alxad.http.AlxHttpResponse;
import com.alxad.util.AlxLog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDownloadManager {
    private static final String TAG = "ImageDownloadManager";

    private static final ExecutorService mExecutors = Executors.newFixedThreadPool(6);

    //针对同一张图片多次请求时的回调监听器
    private static final ConcurrentHashMap<String, List<WeakReference<ImageDownloadListener>>> URLS = new ConcurrentHashMap<>();

    private final Object mLock = new Object();

    private static ImageDownloadManager mInstance;

    private ImageDownloadManager() {

    }

    public static ImageDownloadManager get() {
        if (mInstance == null) {
            synchronized (ImageDownloadManager.class) {
                if (mInstance == null) {
                    mInstance = new ImageDownloadManager();
                }
            }
        }
        return mInstance;
    }

    public synchronized void startDownload(String url, ImageDownloadListener listener) {
        if (isAdd(url, listener)) {
            httpDownload(url);
        }
    }

    private boolean isAdd(String url, ImageDownloadListener listener) {
        synchronized (mLock) {
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            if (listener == null) {
                return false;
            }
            try { //添加异常，防范于未然
                if (URLS.containsKey(url)) {
                    List<WeakReference<ImageDownloadListener>> list = URLS.get(url);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    if (list.isEmpty()) {
                        callback(1, url, null, listener);
                        callback(2, url, null, listener);
                        list.add(new WeakReference<>(listener));
                        return false;
                    }
                    for (WeakReference<ImageDownloadListener> item : list) {
                        if (item == null) {
                            continue;
                        }
                        ImageDownloadListener item1 = item.get();
                        if (item1 != null && item1 == listener) {
                            callback(2, url, null, listener);
                            return false;
                        }
                    }
                    callback(1, url, null, listener);
                    callback(2, url, null, listener);
                    list.add(new WeakReference<>(listener));
                    return false;
                } else {
                    callback(1, url, null, listener);
                    List<WeakReference<ImageDownloadListener>> list = new ArrayList<>();
                    list.add(new WeakReference<ImageDownloadListener>(listener));
                    URLS.put(url, list);
                    return true;
                }
            } catch (Exception e) {
                callback(4, url, e.getMessage(), listener);
                return false;
            }
        }
    }

    private void remove(String url) {
        synchronized (mLock) {
            try {
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                URLS.remove(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void httpDownload(final String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        mExecutors.execute(new Runnable() {
            @Override
            public void run() {
                try { //添加异常，防范于未然
                    AlxLog.i(AlxLogLevel.MARK,TAG, url);
                    AlxHttpRequest request = new AlxHttpRequest.Builder(url)
                            .setRequestMethod(AlxHttpMethod.GET)
                            .setDownloadDir(Glittle.CACHE_DIR)
                            .builder();

                    AlxDownloadRealCall call = new AlxDownloadRealCall(request, null);
                    AlxHttpResponse response = call.sendCall();

                    if (response == null) {
                        notifyListener(url, false, "http response is null object");
                        remove(url);
                        return;
                    }
                    if (response.isOk()) {
                        if (TextUtils.isEmpty(response.getResponseMsg())) {
                            notifyListener(url, false, "download cache path is null");
                        } else {
                            notifyListener(url, true, response.getResponseMsg());
                        }
                    } else {
                        notifyListener(url, false, response.getResponseMsg());
                    }
                    remove(url);
                } catch (Exception e) {
                    notifyListener(url, false, e.getMessage());
                    remove(url);
                }
            }
        });
    }

    private void notifyListener(String url, boolean isOk, String data) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        try { //添加异常，防范于未然
            List<WeakReference<ImageDownloadListener>> list = URLS.get(url);
            if (list == null || list.isEmpty()) {
                return;
            }
            for (WeakReference<ImageDownloadListener> item : list) {
                if (item == null) {
                    continue;
                }
                ImageDownloadListener listener = item.get();
                if (list != null) {
                    if (isOk) {
                        callback(3, url, data, listener);
                    } else {
                        callback(4, url, data, listener);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callback(int status, String url, String data, ImageDownloadListener listener) {
        if (listener == null) {
            return;
        }
        try {//加异常是防止回调方法中出现异常导致整个程序异常
            switch (status) {
                case 1:
                    listener.onStart(url);
                    break;
                case 2:
                    if (TextUtils.isEmpty(data)) {
                        listener.onWait(url, "url is downloading……");
                    } else {
                        listener.onWait(url, data);
                    }
                    break;
                case 3:
                    listener.onSuccess(url, data);
                    break;
                case 4:
                    listener.onError(url, 102, data);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}