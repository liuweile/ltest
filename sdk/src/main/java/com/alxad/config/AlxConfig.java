package com.alxad.config;


import com.alxad.BuildConfig;
import com.alxad.base.AlxLogLevel;

public class AlxConfig {

    public static String URL_SDK_AD;

    //local开发环境(debug) (注意此环境，wifi 尽量在algorix下)
    public static final String URL_SDK_AD_LOCAL = "http://192.168.163.209:8080/alx/sdktest.php";

    //开发环境(debug)
    public static final String URL_SDK_AD_DEBUG = "https://xyz.svr-algorix.com/rtb/sdk";

    //正式线上地址(release)
    public static String URL_SDK_AD_RELEASE = "https://xyz.svr-algorix.com/rtb/sdk";

    //埋点上报地址
    public static String URL_DATA_ANALYTICS = "https://sapi.svr-algorix.com/rpt?token=";

    //编译环境
    private static final String ENV = "release";//默认是正式环境

    //自动点击
    public static boolean ALX_AUTO_CLICK_IS_USE = false;

    //自动点击时间间隔
    public static long ALX_AUTO_CLICK_TIME_OUT = 3600 * 12;

    //是否使用内置浏览器进行跳转（主要针对电商广告）,默认不使用
    public static boolean ALX_IS_USE_INNER_BROWSER = false;

    //日志打印是否打印行号
    public static final boolean LOG_SHOW_LINE_NUMBER;

    //banner广告最小刷新时间(单位:s)
    public static final int BANNER_MIN_REFRESH_TIME = 10;

    //视频加载超时时间(单位：ms)
    public static final int VIDEO_LOADING_TIMEOUT = 10 * 1000;

    /**
     * 日志级别过滤，如果是null，取消过滤
     */
    public static String LOG_LEVEL_FILTER = null;

    static {
        if ("local".equals(ENV)) {
            URL_SDK_AD = URL_SDK_AD_LOCAL;
        } else if ("debug".equals(ENV)) {
            URL_SDK_AD = URL_SDK_AD_DEBUG;
        } else {
            URL_SDK_AD = URL_SDK_AD_RELEASE;
        }

        if ("debug".equals(BuildConfig.BUILD_TYPE)) {
            LOG_SHOW_LINE_NUMBER = true;
            LOG_LEVEL_FILTER = null;
        } else {
            LOG_SHOW_LINE_NUMBER = false;
            AlxLogLevel[] levels = new AlxLogLevel[]{AlxLogLevel.OPEN, AlxLogLevel.ERROR};
            LOG_LEVEL_FILTER = getLogFilter(levels);
        }
    }

    private static String getLogFilter(AlxLogLevel[] levels) {
        if (levels == null || levels.length < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (AlxLogLevel item : levels) {
            if (item == null) {
                continue;
            }
            sb.append(item.getValue());
            sb.append(",");
        }
        return sb.toString();
    }

}