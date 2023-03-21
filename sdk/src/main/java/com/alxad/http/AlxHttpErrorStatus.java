package com.alxad.http;


/**
 * 请求相关的错误码统一处理
 *
 * @author liuweile
 * @date 2021-3-22
 */
public interface AlxHttpErrorStatus {

    //取消下载
    int CANCEL_DOWNLOAD = 1008;

    //文件下载失败
    int DOWNLOAD_ERROR = 1009;

    //下面是错误信息
    String RESPONSE_CANCEL_DOWNLOAD = "取消下载,下载失败";

    String RESPONSE_DOWNLOAD_ERROR = "文件下载失败";

}