package com.alxad.net;

import android.content.Context;
import android.text.TextUtils;


import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.api.AlxAdSDK;
import com.alxad.base.AlxAdBase;
import com.alxad.base.AlxAdNetwork;
import com.alxad.config.AlxConfig;
import com.alxad.base.AlxLogLevel;
import com.alxad.http.AlxHttpManager;
import com.alxad.http.AlxHttpMethod;
import com.alxad.http.AlxHttpRequest;
import com.alxad.http.AlxHttpResponse;
import com.alxad.net.lib.AlxBaseLoadTask;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxNetworkUtil;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * 广告统一加载类
 *
 * @author lwl
 * @date 2021-11-1
 */
public class AlxLoadAd {
    public static final String TAG = "AlxLoadAd";

    public static final int STATUS_IDLE = 0;//默认状态
    public static final int STATUS_LOADING = 1;
    public static final int STATUS_LOAD_FINISH = 2;

    private Context mContext;

//    private volatile AtomicInteger mStatus = new AtomicInteger(STATUS_IDLE);//记录是否超时(1:是加载超时，2是加载成功或失败回调)


    public AlxLoadAd(Context context) {
        if (context != null) {
            try {
                mContext = context.getApplicationContext();
            } catch (Exception e) {
                mContext = null;
                AlxAgent.onError(e);
                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            }
        }
    }

    public <T extends AlxBaseLoadTask> void startLoad(final AlxRequestBean request, final T task) {
        if (task == null) {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "task is null object");
            return;
        }

        if (!AlxAdSDK.isSDKInit()) {
            task.onError(null, AlxAdError.ERR_SDK_NO_INIT, AlxAdError.MSG_SDK_NO_INIT);
            return;
        }

        if (mContext == null) {
            task.onError(request, AlxAdError.ERR_PARAMS_ERROR, "Context cannot be null");
            return;
        }

        if (request == null) {
            task.onError(null, AlxAdError.ERR_PARAMS_ERROR, "request params is null object");
            return;
        }

        if (TextUtils.isEmpty(request.getAdId())) {
            task.onError(null, AlxAdError.ERR_PARAMS_ERROR, "AdUnitId cannot be null.");
            return;
        }

        if (!AlxNetworkUtil.isNetConnected(mContext)) {
            task.onError(request, AlxAdError.ERR_NETWORK, "network is not connected!");
            return;
        }

//        if (!mStatus.compareAndSet(STATUS_IDLE, STATUS_LOADING) || !mStatus.compareAndSet(STATUS_LOAD_FINISH, STATUS_LOADING)) {
//            AlxLog.d(AlxLogLevel.OPEN,TAG,"The advertisement is loading, please wait");//广告在加载中，请等待
//            return;
//        }

        request.loadAdInit(mContext);
        task.onStart(request);

        final String strUrl = String.format(
                "%s?sid=%s&token=%s",
                AlxConfig.URL_SDK_AD, AlxAdBase.sid, AlxAdBase.token);

        AlxAdNetwork.execute(new Runnable() {
            @Override
            public void run() {
                int code = 0;
                String msg = null;
                boolean isOk = false;
                try {
                    String tag = getLogPrefix(request);
                    AlxLog.d(AlxLogLevel.DATA, TAG, tag + "_url " + strUrl);
                    AlxLog.d(AlxLogLevel.DATA, TAG, tag + "_params " + request.getRequestParams());

                    long startTime = System.currentTimeMillis();

                    Map<String, String> header = new HashMap<>();
                    header.put("Accept", "application/json");
                    AlxHttpRequest httpRequest = new AlxHttpRequest.Builder(strUrl)
                            .setRequestMethod(AlxHttpMethod.POST)
                            .setContentType(true)
                            .setParams(request.getRequestParams())
                            .setOpenLog(false)
                            .setHeaders(header)
                            .builder();
                    AlxHttpResponse result = AlxHttpManager.getInstance().requestApiSync(httpRequest);

                    if (result == null) {
                        isOk = false;
                        code = AlxAdError.ERR_PARAMS_ERROR;
                        msg = "request params is empty";
                    } else {
                        AlxLog.d(AlxLogLevel.DATA, TAG, tag + "_response " + result.getResponseMsg());
                        reportEvent(request, startTime, result.getHttpStatus());

                        if (!result.isOk()) {
                            isOk = false;
                            code = result.getResponseCode();
                            msg = result.getResponseMsg();
                        } else {
                            if (TextUtils.isEmpty(result.getResponseMsg())) {
                                isOk = false;
                                code = AlxAdError.ERR_SERVER;
                                msg = "Sever error! json is null";
                            } else {
                                isOk = true;
                                msg = result.getResponseMsg();
                            }
                        }
                    }
                } catch (Exception e) {
                    AlxAgent.onError(e);
                    isOk = false;
                    code = AlxAdError.ERR_EXCEPTION;
                    msg = e.getMessage();
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
//                mStatus.compareAndSet(STATUS_LOADING, STATUS_LOAD_FINISH);

                //回调方法中:开发者自己写出的bug不要加异常处理，让开发者自己处理
                if (task != null) {
                    if (isOk) {
                        task.parseHttpJson(request, msg);
                    } else {
                        task.onError(request, code, msg);
                    }
                }
            }
        });

    }

    private String getLogPrefix(AlxRequestBean request) {
        try {
            StringBuilder sbTag = new StringBuilder();
            sbTag.append("AlxHttp");
            sbTag.append("_");
            sbTag.append(request.getAdId());
            sbTag.append("_");
            sbTag.append(getAdTypeName(request.getAdType()));
            return sbTag.toString();
        } catch (Exception e) {
            return "AlxHttp";
        }
    }

    public String getAdTypeName(int mAdType) {
        String result = "unknown";
        switch (mAdType) {
            case AlxRequestBean.AD_TYPE_NATIVE:
                result = "native";
                break;
            case AlxRequestBean.AD_TYPE_BANNER:
                result = "banner";
                break;
            case AlxRequestBean.AD_TYPE_INTERSTITIAL:
                result = "interstitial";
                break;
            case AlxRequestBean.AD_TYPE_REWARD:
                result = "reward";
                break;
            case AlxRequestBean.AD_TYPE_SPLASH:
                result = "splash";
                break;
        }
        return result;
    }


    /**
     * sdk数据分析上报事件
     *
     * @param startTime
     * @param httpStatus
     */
    private void reportEvent(AlxRequestBean request, long startTime, int httpStatus) {
        try {
            if (request == null) {
                return;
            }
            if (TextUtils.isEmpty(request.getRequestId())) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long time = currentTime - startTime;

            JSONObject json = new JSONObject();
            json.put("status", httpStatus);
            json.put("cost", time);

            AlxSdkData.onEvent(request.getRequestId(), AlxSdkDataEvent.HTTP_LOAD_RESPONSE, request.getAdId(), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}