package com.alxad.http;

import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;

import java.io.InputStream;
import java.net.HttpURLConnection;


/**
 * 网络请求的基类
 *
 * @author liuweile
 * @date 2021-3-22
 */
public class AlxHttpRealCall extends AlxBaseCall {

    public AlxHttpRealCall(AlxHttpRequest request) {
        this.mRequest = request;
        if (mRequest != null) {
            mRequestCode = mRequest.getRequestCode();
        }
    }

    @Override
    public AlxHttpResponse sendCall() {
        String isVerify = isVerifyUrl();
        if (!AlxHttpUtil.isEmpty(isVerify)) {
            AlxHttpResponse response = new AlxHttpResponse();
            response.setResponseCode(AlxAdError.ERR_PARAMS_ERROR);
            response.setResponseMsg(isVerify);
            response.setRequestCode(mRequestCode);
            return response;
        }
        if (mRequest.isShowLog()) {
            AlxLog.d(AlxLogLevel.DATA, TAG + "-url", mRequest.getUrl());
            AlxLog.d(AlxLogLevel.DATA, TAG + "-params", mRequest.getParams());
        }
        AlxHttpResponse response = loadUrl(mRequest.getUrl());
        if (mRequest.isShowLog()) {
            logResponse(response);
        }
        return response;
    }

    private String isVerifyUrl() {
        if (mRequest == null) {
            return "request object is empty";
        }
        if (AlxHttpUtil.isEmpty(mRequest.getRequestMethod())) {
            return "request method is empty";
        }
        if (AlxHttpUtil.isEmpty(mRequest.getUrl())) {
            return "url is empty";
        }
        return null;
    }

    @Override
    protected String getResponseData(String url, InputStream is, HttpURLConnection connection, AlxHttpResponse response) throws Exception {
        return getResponseString(is, connection.getContentType());
    }

}