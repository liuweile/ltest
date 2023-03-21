package com.alxad.base;

/**
 * 打印日志级别
 *
 * @author lwl
 * @date 2021-9-9
 */
public enum AlxLogLevel {
    /**
     * 网络请求的url，params, responseData
     */
    DATA(1),
    /**
     * 数据上报url
     */
    REPORT(2),
    /**
     * 错误异常
     */
    ERROR(3),
    /**
     * 代码定位标记(不对外开放)
     */
    MARK(4),
    /**
     * 开放(公开)
     */
    OPEN(5);

    private int level;

    AlxLogLevel(int level) {
        this.level = level;
    }

    public int getValue() {
        return level;
    }

}