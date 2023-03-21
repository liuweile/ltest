package com.alxad.report;

/**
 * 数据上报中 宏替换 采集地
 * 数据上报时，先把宏替换掉后在上报
 *
 * @author lwl
 * @date 2021-8-16
 */
public interface AlxReportPlaceHolder {
    String DEEP_LINK_ERROR = "${ERRMSG}"; //deeplink 失败上报 错误信息替换宏 ${ERRMSG} 再上报
    String VIDEO_VAST_ERROR = "[ERRORCODE]";

    String PRICE = "${AUCTION_PRICE}";//竞价占位符
}