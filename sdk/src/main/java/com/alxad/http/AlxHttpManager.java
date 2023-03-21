package com.alxad.http;

import android.os.Handler;
import android.os.Looper;

import com.alxad.api.AlxAdError;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网络请求
 *
 * @author liuweile
 * @date 2021-3-22
 */
public class AlxHttpManager {

    private ExecutorService mExecutors = Executors.newFixedThreadPool(6);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private static class SingleHolder {
        private static AlxHttpManager instance = new AlxHttpManager();
    }

    private AlxHttpManager() {
    }

    public static AlxHttpManager getInstance() {
        return SingleHolder.instance;
    }

    /**
     * 异步请求api
     *
     * @param request
     */
    public void requestApi(final AlxHttpRequest request) {
        requestApi(request, null);
    }

    /**
     * 异步请求api
     *
     * @param request
     * @param callback
     */
    public void requestApi(final AlxHttpRequest request, final AlxHttpCallback callback) {
        if (request == null) {
            return;
        }
        mExecutors.execute(new Runnable() {
            @Override
            public void run() {
                int requestCode = request.getRequestCode();
                AlxHttpRealCall call = new AlxHttpRealCall(request);
                AlxHttpResponse response = call.sendCall();
                if (callback != null) {
                    if (response == null) {
                        onError(requestCode, AlxAdError.ERR_RESPONSE_EMPTY_OBJECT, "empty object", callback);
                    } else {
                        if (response.isOk()) {
                            onSuccess(requestCode, response.getResponseMsg(), callback);
                        } else {
                            onError(requestCode, response.getResponseCode(), response.getResponseMsg(), callback);
                        }
                    }
                }
            }
        });
    }

    /**
     * 同步请求api
     *
     * @param request
     * @return
     */
    public AlxHttpResponse requestApiSync(AlxHttpRequest request) {
        if (request == null) {
            return null;
        }
        AlxHttpRealCall call = new AlxHttpRealCall(request);
        return call.sendCall();
    }

    /**
     * 简单的get 同步请求
     *
     * @param url
     * @return
     */
    public AlxHttpResponse simpleGetSync(String url) {
        AlxHttpRequest request = new AlxHttpRequest.Builder(url)
                .setContentType(false)
                .setRequestMethod(AlxHttpMethod.GET)
                .builder();
        AlxHttpRealCall call = new AlxHttpRealCall(request);
        return call.sendCall();
    }

    private void onError(final int requestCode, final int code, final String msg, final AlxHttpCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onHttpError(requestCode, code, msg);
                }
            }
        });
    }

    private void onSuccess(final int requestCode, final String msg, final AlxHttpCallback callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onHttpSuccess(requestCode, msg);
                }
            }
        });
    }

}