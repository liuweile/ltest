package com.alxad.http;

/**
 * 请求方式
 *
 * @author liuweile
 * @date 2021-3-22
 */
public enum AlxHttpMethod {
    GET("GET"),
    POST("POST"),
    PUT("PUT");

    private final String value;

    AlxHttpMethod(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}