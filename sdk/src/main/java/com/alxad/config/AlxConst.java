package com.alxad.config;


public class AlxConst {

    // 版本号
    public static String SDK_VERSION = "3.5.2";
    // 测试和发布模式标准，如果你代码也有测试和发布的不同流程，也需要用这个变量区分一下，可以通过配置sd卡8个0文件来动态控制
    public static Boolean SDK_DEBUG = false;

    //sdk 广告的名称
    public static String AD_NETWORK_NAME = "Algorix";


    /**
     * 谷歌商店app 的包名
     */
    public static final String GOOGLE_PLAY_APP_PACKAGE_NAME = "com.android.vending";

    /**
     * 华为应用商店app 的包名
     */
    public static final String HUAWEI_MARKET_APP_PACKE_NAME = "com.huawei.appmarket";

    /**
     * 三星应用商店 的包名
     */
    public static final String SUMSUNG_MARKET_APP_PACK_NAME = "com.sec.android.app.samsungapps";

    /**
     * 小米应用商店 的报名
     */
    public static final String XIAOMI_MARKET_APP_PACK_NAME = "com.xiaomi.market";
    /**
     * oppo应用商店 的报名
     */
    public static final String OPPO_MARKET_APP_PACK_NAME = "com.oppo.market";
    /**
     * vivo应用商店 的报名
     */
    public static final String VIVO_MARKET_APP_PACK_NAME = "com.bbk.appstore";

}
