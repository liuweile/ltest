package com.alxad.net.impl;

import android.text.TextUtils;

import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.control.vast.AlxVastLoader;
import com.alxad.entity.AlxAdItemBean;
import com.alxad.entity.AlxVideoUIData;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.http.AlxDownLoadCallback;
import com.alxad.http.AlxHttpUtil;
import com.alxad.net.lib.AlxExtFieldUtil;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.util.AlxFileUtil;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxResponseBean;
import com.alxad.util.AlxLog;

import org.json.JSONObject;

import java.io.File;

/**
 * 视频
 *
 * @author liuweile
 * @date 2021-10-28
 */
public class AlxVideoTaskImpl extends AlxAdTask<AlxVideoUIData> {
    public static final String TAG = "AlxVideoTaskImpl";

    @Override
    public void onSuccess(final AlxRequestBean request, final AlxResponseBean response) {
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
            final boolean isVideoCache = isVideoCache();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onAdLoadSuccess(request, mResponse);

                        if (isVideoCache) {
                            mCallback.onAdFileCache(true);
                        }
                    }

                }
            });
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int errorCode = AlxAdError.ERR_NO_FILL;
                    String error = "error: No fill, null response!";
                    if (!TextUtils.isEmpty(tempErrorMsg)) {
                        errorCode = tempErrorCode;
                        error = tempErrorMsg;
                    }
                    if (mCallback != null) {
                        mCallback.onAdLoadError(request, errorCode, error);
                    }
                }
            });
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

        if (ads.admType != AlxAdItemBean.ADM_TYPE_VAST) {
            tempErrorCode = AlxAdError.ERR_SERVER;
            tempErrorMsg = AlxAdError.MSG_AD_DATA_FORMAT_ERROR;//数据格式错误
            return false;
        }
        AlxVastLoader vastResponse = new AlxVastLoader(mContext, ads.videoExt);
        if (!vastResponse.loadXml(ads.adm, mVideoDownloadCallback)) {
            tempErrorCode = vastResponse.getErrorCode();
            tempErrorMsg = vastResponse.getMsg();
            return false;
        }
        AlxVideoVastBean vastBean = vastResponse.getData();
        if (vastBean == null) {
            tempErrorCode = AlxAdError.ERR_VAST_ERROR;
            tempErrorMsg = "error:No fill";//数据格式错误
            return false;
        }
        mResponse = new AlxVideoUIData();
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

        mResponse.video = vastBean;
        return true;
    }

    private AlxDownLoadCallback mVideoDownloadCallback = new AlxDownLoadCallback() {
        @Override
        public void onSuccess(File file) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "onSuccess:" + Thread.currentThread().getName());
            try {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setFileCacheListener(true);
                        }
                    });
                }
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onSuccess:" + e.getMessage());
            }
        }

        @Override
        public void onError(int code, String msg) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "onError:" + Thread.currentThread().getName());
            try {
                if (mHandler != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setFileCacheListener(false);
                        }
                    });
                }
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onError:" + e.getMessage());
            }
        }

        @Override
        public void onProgress(int progress) {

        }
    };

    /**
     * 判断视频是否有缓存
     *
     * @return
     */
    private boolean isVideoCache() {
        try {
            if (mResponse == null || mResponse.video == null) {
                return false;
            }
            String url = mResponse.video.videoUrl;
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            String videoDir = AlxFileUtil.getVideoSavePath(mContext);
            String videoFileName = AlxHttpUtil.getDownloadFileName(url);
            File videoFile = new File(videoDir + videoFileName);
            if (videoFile.exists()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlxAgent.onError(e);
        }
        return false;
    }

    private void setFileCacheListener(boolean isSuccess) {
        if (mCallback != null) {
            mCallback.onAdFileCache(isSuccess);
        }
    }

    @Override
    protected void doAdExtendJson(AlxAdItemBean item, JSONObject itemObj) throws Exception {
        if (itemObj.has("video_ext")) {
            JSONObject jObjectVideo = itemObj.getJSONObject("video_ext");
            item.videoExt = AlxExtFieldUtil.parseVideoExt(jObjectVideo);
        }
    }

}