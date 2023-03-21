package com.alxad.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.alxad.analytics.AlxAgent;
import com.alxad.config.AlxConfig;
import com.alxad.config.AlxConst;
import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxJumpCallback;
import com.alxad.entity.AlxTracker;
import com.alxad.view.AlxWebActivity;

/**
 * 统一处理广告点击跳转的逻辑
 *
 * @author lwl
 * @date 2021-8-16
 */
public class AlxClickJump {
    private static final String TAG = "AlxClickJump";

    //app应用商店
    private static final int APP_STORE_HUAWEI = 1; //华为应用商店
    private static final int APP_STORE_SAMSUNG = 2; //三星应用商店
    private static final int APP_STORE_GOOGLE = 3; //谷歌应用商店
    private static final int APP_STORE_COMMON = 4; //根据手机查找本机已安装的商店


    /**
     * 打开广告链接
     *
     * @param deeplink    deeplink 优先处理
     * @param url         广告跳转地址
     * @param packageName 包名
     */
    public static void openLink(Context context, String deeplink, String url, String packageName, AlxTracker tracker, AlxJumpCallback callback) {
        if (context == null) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "openlink() error:context is null");
            return;
        }
        if (TextUtils.isEmpty(deeplink)) {
            openLink(context, url, packageName, tracker, callback);
            return;
        }
        String result = isStartDeepLink(context, deeplink);
        if (!TextUtils.isEmpty(result)) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "openlink() deeplink-error:" + result);
            if (callback != null) {
                callback.onDeeplinkCallback(false, result);
            }
            openLink(context, url, packageName, tracker, callback);
        } else {
            AlxLog.i(AlxLogLevel.MARK, TAG, "openlink() deeplink-ok");
            if (callback != null) {
                callback.onDeeplinkCallback(true, "ok");
            }
        }
    }

    /**
     * 打开广告链接-无deeplink处理
     *
     * @param url         广告跳转地址
     * @param packageName
     */
    public static void openLink(Context context, String url, String packageName, AlxTracker tracker) {
        openLink(context, url, packageName, tracker, null);
    }

    /**
     * 打开广告链接-无deeplink处理
     *
     * @param url         广告跳转地址
     * @param packageName
     */
    public static void openLink(Context context, String url, String packageName, AlxTracker tracker, AlxJumpCallback callback) {
        if (context == null) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "openLink() error:context is null");
            return;
        }

        int type = AlxJumpCallback.TYPE_DEFAULT;
        boolean isSuccess = false;

        if (TextUtils.isEmpty(url)) {
            if (callback != null) {
                callback.onUrlCallback(isSuccess, type);
            }
            return;
        }

        try {
            String lowerUrl = url.toLowerCase();//将url转换成小写，然后做统一判断
            if (isStartAppStore(context, url)) { //打开应用商店成功
                type = AlxJumpCallback.TYPE_APP_MARKET;
                isSuccess = true;
            } else if (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://")) {//用浏览器打开
                type = AlxJumpCallback.TYPE_BROWSER;
                isSuccess = isStartBrowser(context, url, tracker);
            } else {
                //先用deeplink打开，其次用浏览器
                if (isStartDeepLink(context, url) == null) {//打开deeplink成功
                    type = AlxJumpCallback.TYPE_DEEPLINK;
                    isSuccess = true;
                } else { //用浏览器打开
                    type = AlxJumpCallback.TYPE_BROWSER;
                    isSuccess = isStartBrowser(context, url, tracker);
                }
            }
            AlxLog.i(AlxLogLevel.MARK, TAG, "openLink() isSuccess=" + isSuccess + ";type=" + type);
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        if (callback != null) {
            callback.onUrlCallback(isSuccess, type);
        }
    }

    /**
     * 打开deeplink
     *
     * @param deepLink
     * @return null代表成功，如果不是null代表打开错误并返回相应的错误信息
     */
    public static String isStartDeepLink(Context context, String deepLink) {
        if (TextUtils.isEmpty(deepLink)) {
            return "deeplink is empty";
        }
        if (context == null) {
            return "context params is null";
        }
        try {
            PackageManager packageManager = context.getApplicationContext().getPackageManager();
            if (packageManager == null) {
                return "package is empty";
            }
            Intent intent = Intent.parseUri(deepLink, 0);
            if (intent.resolveActivity(packageManager) != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return null;
            } else {
                // 加载 landing page url
                return "deeplink open failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "startDeepLink-error:" + e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * 打开浏览器
     *
     * @param context
     * @param url
     * @return
     */
    public static boolean isStartBrowser(Context context, String url, AlxTracker tracker) {
        try {
            //内置WebView
            if (context != null && !TextUtils.isEmpty(url)) {
                if (AlxConfig.ALX_IS_USE_INNER_BROWSER) {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "isStartBrowser():webview");
                    AlxWebActivity.startWeb(context, new AlxWebActivity.Builder().setLoad(url).setTracker(tracker));
                    return true;
                }
            }
            AlxLog.d(AlxLogLevel.MARK, TAG, "isStartBrowser():browser");
            //跳转到外部浏览器
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "isStartBrowser():error:" + e.getMessage());
            return false;
        }
    }

    /**
     * 打开应用商店
     *
     * @return true 打开成功，false打开失败
     */
    public static boolean isStartAppStore(Context context, String url) {
        if (context == null || TextUtils.isEmpty(url)) {
            return false;
        }

        try {
            //根据url判断是哪个应用商店
            int appStoreType = -1;//商店类型
            String lowerUrl = url.toLowerCase();//将url转换成小写，然后做统一判断
            if (lowerUrl.contains("appmarket://details?")
                    || lowerUrl.contains("market://com.huawei.appmarket.applink?")
                    || lowerUrl.contains("hiapplink://com.huawei.appmarket?")) {//仅支持华为应用商店
                appStoreType = APP_STORE_HUAWEI;
            } else if (lowerUrl.startsWith("http://www.samsungapps.com/appquery/appDetail.as?")
                    || lowerUrl.startsWith("http://apps.samsung.com/appquery/appDetail.as?")) {//三星应用商店
                appStoreType = APP_STORE_SAMSUNG;
            } else if (lowerUrl.startsWith("https://play.google.com/store/apps/details?")
                    || lowerUrl.startsWith("http://play.google.com/store/apps/details?")) {//谷歌应用商店
                appStoreType = APP_STORE_GOOGLE;
            } else if (lowerUrl.contains("market://details?")) {//谷歌,华为,小米 通用的应用商店
                appStoreType = APP_STORE_COMMON;
            }
            if (appStoreType == -1) {
                return false;
            }

            //打开应用商店判断
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (appStoreType == APP_STORE_HUAWEI) {//华为
                if (AlxUtil.isAppInstalled(context, AlxConst.HUAWEI_MARKET_APP_PACKE_NAME)) {
                    intent.setPackage(AlxConst.HUAWEI_MARKET_APP_PACKE_NAME);
                }
            } else if (appStoreType == APP_STORE_SAMSUNG) {//三星
                if (AlxUtil.isAppInstalled(context, AlxConst.SUMSUNG_MARKET_APP_PACK_NAME)) {
                    intent.setPackage(AlxConst.SUMSUNG_MARKET_APP_PACK_NAME);
                }
            } else if (appStoreType == APP_STORE_GOOGLE) {//google
                if (AlxUtil.isAppInstalled(context, AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME)) {
                    intent.setPackage(AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME);
                }
            } else {
                if (AlxUtil.isAppInstalled(context, AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME)) {//优先使用google 商店
                    intent.setPackage(AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME);
                } else {
                    String manufacturer = Build.MANUFACTURER;
                    if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                        if (AlxUtil.isAppInstalled(context, AlxConst.XIAOMI_MARKET_APP_PACK_NAME)) {//小米应用商店
                            intent.setPackage(AlxConst.XIAOMI_MARKET_APP_PACK_NAME);
                        }
                    } else if ("huawei".equalsIgnoreCase(manufacturer)) {
                        if (AlxUtil.isAppInstalled(context, AlxConst.HUAWEI_MARKET_APP_PACKE_NAME)) {//华为应用商店
                            intent.setPackage(AlxConst.HUAWEI_MARKET_APP_PACKE_NAME);
                        }
                    } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                        if (AlxUtil.isAppInstalled(context, AlxConst.OPPO_MARKET_APP_PACK_NAME)) {//oppo应用商店
                            intent.setPackage(AlxConst.OPPO_MARKET_APP_PACK_NAME);
                        }
                    } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                        if (AlxUtil.isAppInstalled(context, AlxConst.VIVO_MARKET_APP_PACK_NAME)) {//vivo应用商店
                            intent.setPackage(AlxConst.VIVO_MARKET_APP_PACK_NAME);
                        }
                    }
                }
            }
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return false;
    }

}