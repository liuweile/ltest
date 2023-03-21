package com.alxad.report;

import android.text.TextUtils;

import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxBaseUIData;
import com.alxad.http.AlxHttpManager;
import com.alxad.http.AlxHttpMethod;
import com.alxad.http.AlxHttpRequest;
import com.alxad.http.AlxHttpUtil;
import com.alxad.util.AlxLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据上报处理工具
 */
public class AlxReportManager {
    public static final String TAG = "AlxReportManager";

    //上报日志打印标示
    public static final String LOG_TAG_SHOW = "show";//曝光
    public static final String LOG_TAG_CLICK = "click";//点击
    public static final String LOG_TAG_CLOSE = "close";//关闭
    public static final String LOG_TAG_DP_OK = "dp-ok"; //deeplink 成功
    public static final String LOG_TAG_DP_FAIL = "dp-fail";//deeplink 失败

    private static final double PRICE_ZERO = 0.000001;//相当于0。为什么不用0,防止最比较时有精度丢失

    /**
     * 将url中的宏占位符给替换掉
     */
    public static String replaceUrlPlaceholder(String url, String placeHolder, String newStr) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(placeHolder) || newStr == null) {
            return url;
        }
        try {
            newStr = AlxHttpUtil.urlEncodeStr(newStr);
            String newValue = url.replace(placeHolder, newStr);
            return newValue;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            AlxAgent.onError(e);
            return url;
        }
    }

    /**
     * 将url中的宏占位符给替换掉
     */
    public static List<String> replaceUrlPlaceholder(List<String> list, String placeHolder, String newStr) {
        if (list == null || list.isEmpty() || placeHolder == null) {
            return list;
        }
        try {
            newStr = AlxHttpUtil.urlEncodeStr(newStr);
            List<String> result = new ArrayList<>();
            for (String item : list) {
                if (item == null) {
                    continue;
                }
                String newValue = item.replace(placeHolder, newStr);
                result.add(newValue);
            }
            return result;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            AlxAgent.onError(e);
            return list;
        }
    }

    /**
     * 数据上报
     *
     * @param url
     * @param tag 打印标记
     */
    public static void reportUrl(String url, AlxBaseUIData bean, String tag) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(url);
        reportUrl(list, bean, tag);
    }

    /**
     * 数据上报
     *
     * @param urls
     * @param tag  打印标记
     */
    public static void reportUrl(List<String> urls, AlxBaseUIData bean, String tag) {
        try {//加异常防范于未然
            String prefixTag = "";
            if (TextUtils.isEmpty(tag)) {
                prefixTag = "report url";
            } else {
                prefixTag = "report url [" + tag + "]";
            }

            if (urls == null || urls.size() < 1) {
                AlxLog.i(AlxLogLevel.REPORT, TAG, prefixTag + ": url list is empty");
                return;
            }
            for (String url : urls) {
                if (TextUtils.isEmpty(url)) {
                    AlxLog.i(AlxLogLevel.REPORT, TAG, prefixTag + ": url is empty");
                    continue;
                }
                String newUrl = getNoCharUrl(url, bean);
                AlxLog.i(AlxLogLevel.REPORT, TAG, prefixTag + ": " + newUrl);
                AlxHttpRequest request = new AlxHttpRequest.Builder(newUrl)
                        .setContentType(false)
                        .setRequestMethod(AlxHttpMethod.GET)
                        .setOpenLog(false)
                        .builder();
                AlxHttpManager.getInstance().requestApi(request);
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    /**
     * https://gist.github.com/rhumlover/5747417
     * 替换Vast协议中的错误码
     *
     * @param errorCode
     */
    public static int exchangeVideoVastErrorCode(int errorCode) {
        int code = 200;
        switch (errorCode) {
            case AlxAdError.ERR_PARSE_AD:
            case AlxAdError.ERR_VAST_ERROR:
                code = 100;
                break;
        }
        return code;
    }


    /**
     * 将上报的url中的占位符给替换掉
     * 以后可扩展占位符的替换
     *
     * @return
     */
    public static String getNoCharUrl(String url, AlxBaseUIData bean) {
        if (TextUtils.isEmpty(url) || bean == null) {
            return url;
        }
        try {
            //后续如果要添加占位符替换，可以在此统一添加

            String newUrl = url;
            //出价占位符
            if (bean.price > PRICE_ZERO) {//相当于0。为什么不用0,是防止有精度丢失
                newUrl = newUrl.replace(AlxReportPlaceHolder.PRICE, String.valueOf(bean.price));
            }


            return newUrl;
        } catch (Exception e) {
            return url;
        }
    }

    /**
     * 将html源码中的占位符给替换掉
     * 以后可扩展占位符的替换
     *
     * @return
     */
    public static String getReplaceHtml(String html, AlxBaseUIData bean) {
        if (TextUtils.isEmpty(html) || bean == null) {
            return html;
        }
        try {
            AlxLog.i(AlxLogLevel.MARK, TAG, "html-before:" + html);
            //后续如果要添加占位符替换，可以在此统一添加

            String newHtml = html;
            //出价占位符
            if (bean.price > PRICE_ZERO) {//相当于0。为什么不用0,是防止有精度丢失
                newHtml = newHtml.replace(AlxReportPlaceHolder.PRICE, String.valueOf(bean.price));
            }

            AlxLog.i(AlxLogLevel.MARK, TAG, "html-after:" + newHtml);
            return newHtml;
        } catch (Exception e) {
            return html;
        }
    }

}