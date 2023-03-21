package com.alxad.http;

import java.util.Map;


/**
 * 网络请求类
 *
 * @author liuweile
 * @date 2021-3-22
 */
public class AlxHttpRequest {

    public static final String CONTENT_TYPE_FROM = "application/x-www-form-urlencoded;charset=UTF-8";
    public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    /**
     * 是否显示日志
     */
    private boolean isShowLog = true;

    /**
     * 下载目录
     */
    private String mDownloadDir;

    /**
     * 请求标识序号,用于返回时的标识做区分
     */
    private int mRequestCode;

    /**
     * 请求地址
     */
    private String mUrl;
    /**
     * 请求参数
     */
    private String mParams;

    /**
     * 请求类型： GET 或 POST
     */
    private String mRequestMethod = AlxHttpMethod.GET.getValue();

    /**
     * 请求文本类型
     */
    private String mContentType;

    /**
     * 请求头部
     */
    private Map<String, String> mHeaders;

    public boolean isShowLog() {
        return isShowLog;
    }

    public String getDownloadDir() {
        return mDownloadDir;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getParams() {
        return mParams;
    }

    public String getRequestMethod() {
        return mRequestMethod;
    }

    public String getContentType() {
        return mContentType;
    }

    public Map<String, String> getHeaders() {
        return mHeaders;
    }

    public static class Builder {
        private int requestCode;
        private String url;
        private String params;
        private AlxHttpMethod method;
        private String contentType;
        private Map<String, String> headers;
        private String downloadDir;
        private Boolean showLog;

        public Builder(String url) {
            this.url = url;
        }

        public Builder setParams(String params) {
            this.params = params;
            return this;
        }

        public Builder setRequestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public Builder setRequestMethod(AlxHttpMethod method) {
            this.method = method;
            return this;
        }

        /**
         * 设置请求类型
         *
         * @param isJson 是否是json类型
         * @return
         */
        public Builder setContentType(boolean isJson) {
            if (isJson) {
                contentType = CONTENT_TYPE_JSON;
            } else {
                contentType = CONTENT_TYPE_FROM;
            }
            return this;
        }

        public Builder setDownloadDir(String downloadDir) {
            this.downloadDir = downloadDir;
            return this;
        }

        public Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder setOpenLog(boolean isOpen) {
            this.showLog = Boolean.valueOf(isOpen);
            return this;
        }

        public AlxHttpRequest builder() {
            AlxHttpRequest request = new AlxHttpRequest();
            request.mUrl = url;
            request.mParams = params;
            request.mRequestCode = requestCode;
            request.mContentType = contentType;
            request.mHeaders = headers;
            request.mDownloadDir = downloadDir;

            if (showLog != null) {
                request.isShowLog = showLog.booleanValue();
            }

            if (method != null) {
                request.mRequestMethod = method.getValue();
            }
            return request;
        }

    }

}
