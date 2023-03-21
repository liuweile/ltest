package com.alxad.net.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxAdItemBean;
import com.alxad.net.AlxLoadAd;
import com.alxad.util.AlxLog;

import org.json.JSONObject;


/**
 * 广告-业务逻辑层
 *
 * @author lwl
 * @date 2021-11-1
 */
public abstract class AlxAdTask<T> extends AlxBaseLoadTask {
    protected int tempErrorCode;
    protected String tempErrorMsg;

    /**
     * UI转换数据
     */
    protected T mResponse;

    protected AlxAdLoadListener<T> mCallback;
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    public final void startLoad(Context context, AlxRequestBean request, AlxAdLoadListener<T> listener) {
        mContext = context;
        mCallback = listener;
        AlxLoadAd loadAd = new AlxLoadAd(context);
        loadAd.startLoad(request, this);
    }

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
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onAdLoadSuccess(request, mResponse);
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
    public void onError(final AlxRequestBean request, final int code, final String msg) {
        AlxLog.i(AlxLogLevel.OPEN, TAG, "errorCode: " + code + " errorMsg: " + msg);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onAdLoadError(request, code, msg);
                }
            }
        });
    }

    /**
     * 对不同类型广告所特有的扩展数据进行json解析<br/>
     * 如果有字段需要单独解析，重写此方法。没有可以忽略
     *
     * @param item    对解析数据进行赋值的对象
     * @param itemObj 需要解析的json数据
     * @throws Exception
     */
    @Override
    protected void doAdExtendJson(AlxAdItemBean item, JSONObject itemObj) throws Exception {

    }

    /**
     * 处理广告数据，对数据进行解析，广告业务逻辑判断处理
     *
     * @param response
     * @return true 业务逻辑处理成功，可正常展示广告; false 业务逻辑处理失败，不可正常展示广告
     * @throws Exception
     */
    public abstract boolean handleData(AlxResponseBean response) throws Exception;

    @Override
    public void onStart(AlxRequestBean request) {

    }

}