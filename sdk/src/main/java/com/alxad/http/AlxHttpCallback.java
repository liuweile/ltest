package com.alxad.http;

/**
 * 请求接口回调
 */
public interface AlxHttpCallback {

    void onHttpSuccess(int requestCode, String result);

    void onHttpError(int requestCode, int code, String msg);

}