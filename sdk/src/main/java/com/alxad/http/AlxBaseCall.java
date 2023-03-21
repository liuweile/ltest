package com.alxad.http;


import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * 网络响应基类统一处理
 *
 * @author liuweile
 * @date 2021-3-22
 */
public abstract class AlxBaseCall {

    public final String TAG = "AlxHttp";

    public static final int BUFFER_SIZE = 8192;

    /**
     * 重定向的最大次数
     */
    private final int MAX_REDIRECT_COUNT = 5;

    /**
     * 失败重连最大次数
     */
//    private final int MAX_RETRY_COUNT = 2;

    private final int CONNECT_TIMEOUT = 10000; // 连接超时时间 (对大文件下载可能会出现链接超时的现象)
    private final int READ_TIMEOUT = 10000; // 读取超时时间 (对大文件下载可能会出现读取超时的现象)

    protected AlxHttpRequest mRequest;

    protected int mRequestCode;

    private int mCurrentRedirectCount = 0;//当前重定向次数
    //    private int mRetryCount = 0;//当前失败重连次数
    protected int mConnectTimeout = CONNECT_TIMEOUT;//可自定义连接超时
    protected int mReadTimeout = READ_TIMEOUT;//可自定义读取超时时间

    public abstract AlxHttpResponse sendCall();


    /**
     * 打印请求返回日志
     *
     * @param response
     */
    protected void logResponse(AlxHttpResponse response) {
        if (response == null) {
            return;
        }
        String tagName = TAG + "-response";
        if (response.isOk()) {
            AlxLog.d(AlxLogLevel.DATA, tagName, response.getResponseMsg());
        } else {
            AlxLog.e(AlxLogLevel.DATA, tagName, "Error: " + response.getResponseMsg());
        }
    }


    protected AlxHttpResponse loadUrl(String strUrl) {
        HttpURLConnection connection = null;
        OutputStreamWriter writer = null;
        InputStream is = null;

        AlxHttpResponse response = new AlxHttpResponse();
        response.setRequestCode(mRequestCode);
        final int RESPONSE_CODE_DEFAULT = -2;
        int responseCode = RESPONSE_CODE_DEFAULT;
        try {
            URL url = new URL(strUrl);
            if (strUrl.toLowerCase().startsWith("https")) {
                connection = (HttpsURLConnection) url.openConnection(Proxy.NO_PROXY);
            } else {
                connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            }
            int minTimeout = 3000;//最小链接时间
            if (mConnectTimeout < minTimeout) {
                mConnectTimeout = CONNECT_TIMEOUT;
            }
            if (mReadTimeout < minTimeout) {
                mReadTimeout = READ_TIMEOUT;
            }
            connection.setConnectTimeout(mConnectTimeout);
            connection.setReadTimeout(mReadTimeout);
            connection.setRequestMethod(mRequest.getRequestMethod());
            connection.setDoInput(true);

            Map<String, String> map = mRequest.getHeaders();
            if (map != null && !map.isEmpty()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if (!AlxHttpUtil.isEmpty(mRequest.getContentType())) {
                connection.setRequestProperty("Content-Type", mRequest.getContentType());
            }

            if ("POST".equalsIgnoreCase(connection.getRequestMethod())) {
                connection.setDoOutput(true);

                if (!AlxHttpUtil.isEmpty(mRequest.getParams())) {
                    writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(mRequest.getParams());
                    writer.flush();
                }
            }

            responseCode = connection.getResponseCode();
            response.setHttpStatus(responseCode);
            //如果是重定向，则重新发送请求
            if (AlxHttpUtil.isHttpRedirect(responseCode)) {
                String realUrl = connection.getHeaderField("Location");
                mCurrentRedirectCount += 1;
                if (mCurrentRedirectCount < this.MAX_REDIRECT_COUNT) {
                    connection.disconnect();
                    return loadUrl(realUrl);
                } else {
                    response.setResponseCode(AlxAdError.ERR_SERVER);
                    response.setResponseMsg("Too many (> " + MAX_REDIRECT_COUNT + ") redirects!");
//                    response.responseMsg = "重定向次数超过" + MAX_REDIRECT_COUNT + "次";
                }
            }
//            else if (responseCode == 204) {//失败重连。204,205 empty content
//                mRetryCount += 1;
//                if (mRetryCount < this.MAX_RETRY_COUNT) {
//                    connection.disconnect();
//                    Thread.sleep(500);//失败重连停留0.5s
//                    return loadUrl(strUrl);
//                } else {
//                    response.responseCode = AlxHttpErrorStatus.OTHER_ERROR;
//                    response.responseMsg = "Too many (> " + MAX_RETRY_COUNT + ") Reconnection!";
////                    response.responseMsg = "失败重连次数超过" + MAX_RETRY_COUNT + "次";
//                }
//            }
            else if (responseCode == 200) {
                is = new BufferedInputStream(connection.getInputStream());
                response.setResponseCode(AlxHttpResponse.RESPONSE_OK);
                String data = getResponseData(strUrl, is, connection, response);
                response.setResponseMsg(data);
            } else {
                String error = responseCode + ":" + connection.getResponseMessage();
                if (AlxHttpUtil.isEmpty(error)) {
                    error = "error code is:" + connection.getResponseCode();
                }
                response.setResponseCode(AlxAdError.ERR_SERVER);
                response.setResponseMsg(error);
                onError(response.getResponseCode(), error);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            response.setResponseMsg(e.getMessage());
            response.setResponseCode(AlxAdError.ERR_PARAMS_ERROR);
            onError(response.getResponseCode(), e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            if (responseCode == RESPONSE_CODE_DEFAULT) {
                response.setResponseCode(AlxAdError.ERR_NETWORK);
            } else {
                response.setResponseCode(AlxAdError.ERR_SERVER);
            }
            response.setResponseMsg(e.getMessage());
            onError(response.getResponseCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setResponseMsg(e.getMessage());
            response.setResponseCode(AlxAdError.ERR_PARSE_AD);
            onError(response.getResponseCode(), e.getMessage());
        } finally {
            AlxHttpUtil.closeQuietly(writer);
            AlxHttpUtil.closeQuietly(is);
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    //数据过大会抛出数组越界异常
    protected String getResponseString(InputStream is, String contentType) throws Exception {
        String charsetName = AlxHttpUtil.getCharsetName(contentType);
        if (!"gbk".equalsIgnoreCase(charsetName)) {
            charsetName = "UTF-8";
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        int length;
        StringBuilder sb = new StringBuilder();
        while ((length = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, length, charsetName));
        }
        return sb.toString();
    }

    protected abstract String getResponseData(String url, InputStream is, HttpURLConnection connection, AlxHttpResponse response) throws Exception;

    public void onError(int code, String error) {

    }

}