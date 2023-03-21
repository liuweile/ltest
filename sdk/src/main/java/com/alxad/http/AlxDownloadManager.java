package com.alxad.http;

import android.text.TextUtils;

import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;
import com.alxad.http.download.AlxDownloadNotify;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件下载
 *
 * @author liuweile
 * @date 2021-3-22
 */
public class AlxDownloadManager {
    public static final String TAG = "AlxDownloadManager";

    private ExecutorService mExecutors = Executors.newFixedThreadPool(5);
    //    private final Set<String> mUrls = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private final CopyOnWriteArraySet<String> mUrls = new CopyOnWriteArraySet<>();
    private static CopyOnWriteArrayList<WeakReference<AlxDownloadNotify>> mRepastLoaderListener = new CopyOnWriteArrayList<>();

    public static final long WAIT_TIMEOUT = 15 * 1000;//等待超时时间是15s

    private static class SingleHolder {
        private static AlxDownloadManager instance = new AlxDownloadManager();
    }

    private AlxDownloadManager() {

    }

    public static AlxDownloadManager getInstance() {
        return SingleHolder.instance;
    }

    /**
     * 获取下载请求对象
     *
     * @return
     */
    public static AlxDownloadRequest with(String url, String downloadDir) {
        return new AlxDownloadRequest(url, downloadDir);
    }

    /**
     * 异步下载
     */
    void asyncDownload(AlxDownloadRequest request) {
        if (request == null) {
            return;
        }
        final String url = request.getUrl();
        final String downloadDir = request.getDownloadDir();
        final String fileName = request.getFileName();
        final AlxDownLoadCallback callback = request.getCallback();
        if (AlxHttpUtil.isEmpty(url) || AlxHttpUtil.isEmpty(downloadDir)) {
            return;
        }
        if (addDownUrl(url)) {
//            if (callback != null) {
//                callback.onError(AlxHttpErrorStatus.REPEAT_DOWNLOAD_RESOURCE, AlxHttpErrorStatus.REPEAT_DOWNLOAD_RESOURCE_STR);
//            }
            mExecutors.execute(new Runnable() {
                @Override
                public void run() {
                    AlxHttpResponse response = downloadingWait(url);
                    asyncWork(response, callback);
                }
            });
            return;
        }
        mExecutors.execute(new Runnable() {
            @Override
            public void run() {
                AlxHttpRequest request = new AlxHttpRequest.Builder(url)
                        .setRequestMethod(AlxHttpMethod.GET)
                        .setDownloadDir(downloadDir)
                        .builder();

                AlxDownloadRealCall call = new AlxDownloadRealCall(request, callback);
                call.setFileName(fileName);
                AlxHttpResponse response = call.sendCall();
                removeDownloadUrl(url);
                notifyRepeatLoader(url, response);
                asyncWork(response, callback);
            }
        });
    }

    /**
     * 同步下载
     */
    AlxHttpResponse syncDownload(AlxDownloadRequest request) {
        if (request == null) {
            return null;
        }
        String url = request.getUrl();
        String downloadDir = request.getDownloadDir();
        if (AlxHttpUtil.isEmpty(url) || AlxHttpUtil.isEmpty(downloadDir)) {
            return null;
        }
        if (addDownUrl(url)) {
//            AlxHttpResponse response = new AlxHttpResponse();
//            response.responseCode = AlxHttpErrorStatus.REPEAT_DOWNLOAD_RESOURCE;
//            response.responseMsg = AlxHttpErrorStatus.REPEAT_DOWNLOAD_RESOURCE_STR;
//            return response;
            return downloadingWait(url);
        }
        AlxHttpRequest requestObj = new AlxHttpRequest.Builder(url)
                .setRequestMethod(AlxHttpMethod.GET)
                .setDownloadDir(downloadDir)
                .builder();
        AlxDownloadRealCall call = new AlxDownloadRealCall(requestObj, null);
        call.setFileName(request.getFileName());
        AlxHttpResponse response = call.sendCall();
        removeDownloadUrl(url);
        notifyRepeatLoader(url, response);
        return response;
    }

    private synchronized boolean addDownUrl(String url) {
        if (url == null) {
            return true;
        }
        return !mUrls.add(url);
    }

    private void asyncWork(AlxHttpResponse response, AlxDownLoadCallback callback) {
        File file = null;
        int errCode = 0;
        String errMsg = "";
        try {
            if (response == null) {
                errCode = AlxAdError.ERR_RESPONSE_EMPTY_OBJECT;
                errMsg = "empty object";
                file = null;
            } else {
                if (response.isOk()) {
                    String filePath = response.getResponseMsg();
                    if (!TextUtils.isEmpty(filePath)) {
                        file = new File(filePath);
                    } else {
                        errCode = AlxHttpErrorStatus.DOWNLOAD_ERROR;
                        errMsg = AlxHttpErrorStatus.RESPONSE_DOWNLOAD_ERROR;
                        file = null;
                    }
                } else {
                    errCode = response.getRequestCode();
                    errMsg = response.getResponseMsg();
                    file = null;
                }
            }
        } catch (Exception e) {
            errCode = AlxAdError.ERR_EXCEPTION;
            errMsg = e.getMessage();
            file = null;
        }
        if (callback != null) {
            if (file == null) {
                callback.onError(errCode, errMsg);
            } else {
                callback.onSuccess(file);
            }
        }
    }

    /**
     * 是否包含url
     *
     * @param url
     * @return
     */
    public boolean isContainsUrl(String url) {
        if (url == null) {
            return true;
        }
        return mUrls.contains(url);
    }

    private synchronized void removeDownloadUrl(String url) {
        mUrls.remove(url);
    }

    /**
     * 同一个地址在下载中时，耗时等待。注意此方法不能在UI线程中使用
     *
     * @param url
     * @return
     */
    private AlxHttpResponse downloadingWait(String url) {
        AlxDownloadNotify notifyObj = new AlxDownloadNotify();
        notifyObj.url = url;
        try {
            synchronized (notifyObj.waitLock) {
                try {
                    AlxLog.i(AlxLogLevel.MARK, TAG, "wait-start");
                    addRepeatLoaderListener(notifyObj);
                    notifyObj.waitLock.wait(WAIT_TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    AlxLog.e(AlxLogLevel.MARK, TAG, "wait-timeout:" + e.getMessage());
                    AlxHttpResponse response = new AlxHttpResponse();
                    response.setResponseCode(AlxAdError.ERR_EXCEPTION);
                    response.setResponseMsg(e.getMessage());
                    return response;
                } catch (Throwable e) {
                    AlxLog.e(AlxLogLevel.MARK, TAG, "wait-error:" + e.getMessage());
                    AlxHttpResponse response = new AlxHttpResponse();
                    response.setResponseCode(AlxAdError.ERR_EXCEPTION);
                    response.setResponseMsg(e.getMessage());
                    return response;
                }
                if (notifyObj.response == null) {
                    AlxLog.i(AlxLogLevel.MARK, TAG, "wait-end-empty");
                    AlxHttpResponse response = new AlxHttpResponse();
                    response.setResponseCode(AlxAdError.ERR_UNKNOWN);
                    response.setResponseMsg("The url is being downloaded, please don't download it repeatedly.");//当前url正在下载中,请不用重复下载
                    return response;
                }
                AlxLog.i(AlxLogLevel.MARK, TAG, "wait-end");
                return notifyObj.response;
            }
        } catch (Throwable e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, "wait-error2:" + e.getMessage());
            AlxHttpResponse response = new AlxHttpResponse();
            response.setResponseCode(AlxAdError.ERR_EXCEPTION);
            response.setResponseMsg(e.getMessage());
            return response;
        }
    }

    public static synchronized void addRepeatLoaderListener(AlxDownloadNotify notify) {
        if (mRepastLoaderListener == null || notify == null) {
            return;
        }
        boolean isAdd = false;
        for (WeakReference<AlxDownloadNotify> item : mRepastLoaderListener) {
            AlxDownloadNotify obj = item.get();
            if (obj == notify) {
                isAdd = true;
                break;
            }
        }
        if (!isAdd) {
            WeakReference<AlxDownloadNotify> item = new WeakReference<>(notify);
            mRepastLoaderListener.add(item);
        }
    }

    private static void notifyRepeatLoader(String url, AlxHttpResponse response) {
        if (mRepastLoaderListener == null || mRepastLoaderListener.isEmpty()) {
            return;
        }
        for (WeakReference<AlxDownloadNotify> item : mRepastLoaderListener) {
            AlxDownloadNotify obj = item.get();
            if (obj == null) {
                continue;
            }
            if (AlxHttpUtil.equals(url, obj.url)) {
                obj.response = response;
                try {
                    synchronized (obj.waitLock) {
                        obj.waitLock.notify();
                    }
                } catch (Throwable e) {
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
                mRepastLoaderListener.remove(item);
            }
        }
    }

}