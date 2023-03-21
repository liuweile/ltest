package com.alxad.http;

import java.io.Serializable;


/**
 * 网络响应类
 *
 * @author liuweile
 * @date 2021-3-22
 */
public class AlxHttpResponse implements Serializable {

    /**
     * 成功
     */
    public static final int RESPONSE_OK = -101;

    /**
     * 请求标记
     */
    private int requestCode;

    /**
     * 返回码
     */
    private int responseCode;

    /**
     * 返回数据
     * <p>
     * 1: 如果是数据请求，返回的是json数据 或 异常数据
     * 2：如果是下载，返回的是路径 或 异常数据
     * </p>
     */
    private String responseMsg;

    /**
     * http状态码
     */
    private int httpStatus;

    public boolean isOk() {
        return responseCode == RESPONSE_OK;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }
}