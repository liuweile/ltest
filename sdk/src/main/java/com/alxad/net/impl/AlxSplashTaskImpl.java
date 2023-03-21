package com.alxad.net.impl;

import android.text.TextUtils;

import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxAdItemBean;
import com.alxad.entity.AlxSplashUIData;
import com.alxad.http.AlxDownLoadCallback;
import com.alxad.http.AlxDownloadManager;
import com.alxad.http.AlxHttpUtil;
import com.alxad.net.lib.AlxExtFieldUtil;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.net.lib.AlxResponseBean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class AlxSplashTaskImpl extends AlxAdTask<AlxSplashUIData> {
    public static final String TAG = "AlxSplashTaskImpl";

    private long mTimeout;//设置广告加载超时时间：单位毫秒
    private volatile boolean isLoadErrorCall = false;
    private volatile AtomicInteger mAtomicTimeout = new AtomicInteger(0);//记录是否超时(1:是加载超时，2是加载成功或失败回调)

    private long mStartLoadTime;
    public final Object waitLock = new Object();
    private boolean isWaitLockOpen = false;

    public AlxSplashTaskImpl() {

    }

    @Override
    public void onStart(AlxRequestBean request) {
        mStartLoadTime = System.currentTimeMillis();
        if (request != null) {
            mTimeout = request.getRequestTimeout();
        }
        startTime(request);
    }

    /**
     * 广告加载计时
     */
    private void startTime(final AlxRequestBean request) {
        if (mTimeout < 1) {
            return;
        }
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "load timeout");
                    if (mAtomicTimeout.compareAndSet(0, 1)) {
                        filterDoError(request, AlxAdError.ERR_LOAD_TIMEOUT, "ad load timeout,The timeout is " + mTimeout + "ms");
                    }
                }
            }, mTimeout);
        }
    }

    /**
     * 停止计时
     */
    private void stopTime() {
        try {
            AlxLog.d(AlxLogLevel.MARK, TAG, "stopTime");
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    @Override
    public void onSuccess(final AlxRequestBean request, final AlxResponseBean response) {
        if (!mAtomicTimeout.compareAndSet(0, 2)) {
            return;
        }
        stopTime();
        boolean isOk = false;
        try {
            isOk = handleData(response);
        } catch (Exception e) {
            isOk = false;
            tempErrorCode = AlxAdError.ERR_PARSE_AD;
            tempErrorMsg = "error: " + e.getMessage();
            AlxAgent.onError(e);
        }

        if (isOk) {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onAdLoadSuccess(request, mResponse);
                        }
                    }
                });
            }
        } else {
            int errorCode = AlxAdError.ERR_NO_FILL;
            String error = "error: No fill, null response!";
            if (!TextUtils.isEmpty(tempErrorMsg)) {
                errorCode = tempErrorCode;
                error = tempErrorMsg;
            }
            filterDoError(request, errorCode, error);
        }
    }

    @Override
    public void onError(final AlxRequestBean request, final int code, final String msg) {
        AlxLog.i(AlxLogLevel.OPEN, TAG, "errorCode: " + code + " errorMsg: " + msg);
        if (!mAtomicTimeout.compareAndSet(0, 2)) {
            return;
        }
        stopTime();
        filterDoError(request, code, msg);
    }

    private void filterDoError(final AlxRequestBean request, final int code, final String msg) {
        if (!isLoadErrorCall) {
            isLoadErrorCall = true;
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback != null) {
                            mCallback.onAdLoadError(request, code, msg);
                        }
                    }
                });
            }
        }
    }

    @Override
    public boolean handleData(AlxResponseBean response) throws Exception {
        if (response == null || response.adsList == null || response.adsList.isEmpty()) {
            tempErrorCode = AlxAdError.ERR_NO_FILL;
            tempErrorMsg = "error:No fill, null response!";
            return false;
        }

        AlxAdItemBean ads = response.adsList.get(0);
        if (ads == null) {
            tempErrorCode = AlxAdError.ERR_NO_FILL;
            tempErrorMsg = "error:No fill, null response!";
            return false;
        }

        mResponse = new AlxSplashUIData();
        mResponse.id = response.id;
        mResponse.bundle = ads.bundle;
        mResponse.deeplink = ads.deeplink;
        mResponse.width = ads.width;
        mResponse.height = ads.height;
        mResponse.clickTrackers = ads.clicktrackers;
        mResponse.impressTrackers = ads.imptrackers;
        mResponse.price = ads.price;
        mResponse.burl = ads.burl;
        mResponse.nurl = ads.nurl;
        mResponse.extField = ads.nativeExt;

        if (ads.admType == AlxAdItemBean.ADM_TYPE_JSON) {
            parseJson(mResponse, ads.adm);
        }

        if (TextUtils.isEmpty(mResponse.imgBig)) {
            tempErrorCode = AlxAdError.ERR_NO_FILL;
            tempErrorMsg = "error:" + "No fill, img url is empty!";
            return false;
        }

        //异步下载大图
        try {
            String imgDir = AlxFileUtil.getImageSavePath(mContext);
            String fileName = AlxHttpUtil.getDownloadFileName(mResponse.imgBig);
            File file = new File(imgDir + fileName);
            if (!file.exists()) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "img:" + mResponse.imgBig);
                AlxDownloadManager.with(mResponse.imgBig, imgDir).asyncRequest(new AlxDownLoadCallback() {
                    @Override
                    public void onSuccess(File file) {
                        if (isWaitLockOpen) {
                            cancelNotify();
                        }
                    }

                    @Override
                    public void onError(int code, String msg) {
                        if (isWaitLockOpen) {
                            cancelNotify();
                        }
                    }

                    @Override
                    public void onProgress(int progress) {

                    }
                });
                //做等待一段时间
                addWait();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }

        return true;
    }

    private void cancelNotify() {
        try {
            synchronized (waitLock) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "cancelNotify()");
                try {
                    waitLock.notify();
                } catch (Throwable e) {
                    AlxLog.d(AlxLogLevel.ERROR, TAG, "cancelNotify() error1:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            AlxLog.d(AlxLogLevel.ERROR, TAG, "cancelNotify() error2:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addWait() {
        long currentTime = System.currentTimeMillis();
        long time = currentTime - mStartLoadTime;

        long remainTime = mTimeout - time;//剩余时间
        if (remainTime > 0) {
            try {
                isWaitLockOpen = true;
                synchronized (waitLock) {
                    try {
                        AlxLog.i(AlxLogLevel.MARK, TAG, "splash_wait-start:" + remainTime + "ms");
                        waitLock.wait(remainTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        AlxLog.e(AlxLogLevel.ERROR, TAG, "splash_wait-timeout:" + e.getMessage());
                    } catch (Throwable e) {
                        AlxLog.e(AlxLogLevel.ERROR, TAG, "splash_wait-error:" + e.getMessage());
                    }
                    AlxLog.i(AlxLogLevel.MARK, TAG, "splash_wait-end");
                }
            } catch (Throwable e) {
                AlxLog.i(AlxLogLevel.ERROR, TAG, "splash_wait-end-error2:" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            isWaitLockOpen = false;
            AlxLog.d(AlxLogLevel.MARK, TAG, "splash no wait time");
        }
    }

    public void parseJson(AlxSplashUIData data, String jsonStr) {
        if (data == null || TextUtils.isEmpty(jsonStr)) {
            return;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);

            JSONObject fieldJson = json.optJSONObject("title");
            if (fieldJson != null) {
                data.title = fieldJson.optString("value");
            }

            fieldJson = json.optJSONObject("desc");
            if (fieldJson != null) {
                data.desc = fieldJson.optString("value");
            }

            fieldJson = json.optJSONObject("main");
            if (fieldJson != null) {
                data.imgBig = fieldJson.optString("url");
                data.imgBigWidth = fieldJson.optInt("width");
                data.imgBigHeight = fieldJson.optInt("height");
            } else {
                JSONArray mainArray = json.optJSONArray("main");
                if (mainArray != null && mainArray.length() > 0) {
                    JSONObject mainJson = mainArray.optJSONObject(0);
                    if (mainJson != null) {
                        data.imgBig = mainJson.optString("url");
                        data.imgBigWidth = mainJson.optInt("width");
                        data.imgBigHeight = mainJson.optInt("height");
                    }
                }
            }

            fieldJson = json.optJSONObject("link");
            if (fieldJson != null) {
                data.link = fieldJson.optString("url");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doAdExtendJson(AlxAdItemBean item, JSONObject itemObj) throws Exception {
        if (itemObj.has("native_ext")) {
            JSONObject nativeJson = itemObj.getJSONObject("native_ext");
            item.nativeExt = AlxExtFieldUtil.parseNativeExt(nativeJson);
        }
    }

}