package com.alxad.api;

import android.content.Context;

import com.alxad.base.AlxAdNetwork;
import com.alxad.config.AlxConfig;

//sdk_support@algorix.co
public class AlxAdSDK {
    private static boolean isSDKInit = false;

    public static void init(Context context, String token, String sid, String appId) throws Exception {
        init(context, token, sid, appId, null);
    }

    public static void init(Context context, String token, String sid, String appId, AlxSdkInitCallback callback) throws Exception {
        Context applicationContext = null;
        if (context != null) {
            //获取ApplicationContext 防止内存泄漏
            applicationContext = context.getApplicationContext();
        }
        AlxAdNetwork.init(applicationContext, token, sid, appId, callback);
        isSDKInit = true;
    }

    public static boolean isSDKInit() {
        return isSDKInit;
    }

    public static String getNetWorkName() {
        return AlxAdNetwork.getNetWorkName();
    }

    public static String getNetWorkVersion() {
        return AlxAdNetwork.getNetWorkVersion();
    }

    public static void setDebug(boolean bDebug) {
        AlxAdNetwork.setDebug(bDebug);
    }

    public static void setAC(boolean use, long time) {
        AlxConfig.ALX_AUTO_CLICK_IS_USE = use;
        AlxConfig.ALX_AUTO_CLICK_TIME_OUT = time;
    }

    /*
        GDPR GDPR_Consent COPPA CCPA 设置
     */
    // GDPR
    public static void setSubjectToGDPR(boolean value) {
        AlxAdNetwork.setSubjectToGDPR(value);
    }
    // GDPR_Consent
    public static void setUserConsent(String value) {
        AlxAdNetwork.setUserConsent(value);
    }
    // COPPA
    public static void setBelowConsentAge(boolean value) {
        AlxAdNetwork.setBelowConsentAge(value);
    }
    // CCPA
    public static void subjectToUSPrivacy(String value) {
        AlxAdNetwork.subjectToUSPrivacy(value);
    }
}
