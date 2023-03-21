package com.alxad.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.alxad.api.AlxSdkInitCallback;
import com.alxad.config.AlxConst;
import com.alxad.omsdk.OmConfig;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxPreferences;
import com.alxad.util.GaidTool;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AlxAdNetwork {

    private static final String TAG = "AlxAdNetwork";
    private static final Executor mCachedExecutor = Executors.newCachedThreadPool();
    static Context mContext;
    static SharedPreferences mPreferences;
    private static boolean mIsSetGDPR = false;
    private static boolean mIsSetGDPR_Consent = false;

    public static void init(Context context, String token, String sid, String app_id, AlxSdkInitCallback callback) throws Exception {
        if (context == null) {
            throw new Exception("context is a null object");
        }
        if (TextUtils.isEmpty(token) || TextUtils.isEmpty(sid) || TextUtils.isEmpty(app_id)) {
            throw new Exception("undefined token, sid, appId");
        }
        mContext = context;
        AlxLog.init(mContext);

        if (AlxConst.SDK_DEBUG) {
            Log.e(TAG, "SDK DEBUG : true");
        } else {
            Log.e(TAG, "SDK DEBUG : false");
        }

        AlxAdBase.token = token;
        AlxAdBase.sid = sid;
        AlxAdBase.app_id = app_id;
        Log.e(TAG, "Alx SDK token:" + token + " sid:" + sid + " app_id:" + app_id);

        Log.e(TAG, "Alx omsdk ver:" + OmConfig.VERSION_NAME);

        AlxAdBase.initGlobalParam(mContext, false);

        initIABTCF(mContext);

        getGAID(context, callback);

        clearCacheData();
    }

    public static String getNetWorkVersion() {
        return AlxConst.SDK_VERSION;
    }

    public static String getNetWorkName() {
        return AlxConst.AD_NETWORK_NAME;
    }

    public static void execute(Runnable runnable) {
        if (runnable != null && mCachedExecutor != null) {
            mCachedExecutor.execute(runnable);
        }
    }

    public static void setDebug(boolean bDebug) {
        AlxLog.setDebugMode(bDebug);

        if (bDebug) {
            String separator = "  ";//分隔符
            StringBuilder sb = new StringBuilder();
            sb.append("SDK-init:");
            sb.append(separator);
            sb.append("SDK_VERSION=");
            sb.append(AlxConst.SDK_VERSION);
            sb.append(separator);
            sb.append("token=");
            sb.append(AlxAdBase.token);
            sb.append(separator);
            sb.append("appKey=");
            sb.append(AlxAdBase.sid);
            sb.append(separator);
            sb.append("appId=");
            sb.append(AlxAdBase.app_id);
            AlxLog.i(AlxLogLevel.OPEN, TAG, sb.toString());
        }
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 清理缓存文件
     */
    private static void clearCacheData() {
        if (mContext == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mContext == null) {
                        return;
                    }
                    //清除log缓存
                    String logDir = AlxFileUtil.getLogSavePath(mContext);
                    AlxFileUtil.clearCache(logDir, 3600 * 24 * 3);//缓存三天

                    //清除图片缓存
                    String imageDir = AlxFileUtil.getImageSavePath(mContext);
                    AlxFileUtil.clearCache(imageDir, 3600 * 4);//缓存4小时

                    //清除视频缓存
                    String videoDir = AlxFileUtil.getVideoSavePath(mContext);
                    AlxFileUtil.clearCache(videoDir, 3600 * 2);//缓存两个小时
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void initIABTCF(Context context) {
        try {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            String tcString = mPreferences.getString("IABTCF_TCString", "");
            int gdprApplies = mPreferences.getInt("IABTCF_gdprApplies", 0);
            AlxAdBase.IABTCF_TCString = tcString;
            AlxAdBase.IABTCF_gdprApplies = gdprApplies;

            AlxLog.i(AlxLogLevel.OPEN, TAG, "IABTCF_TCString:" + tcString);
            AlxLog.i(AlxLogLevel.OPEN, TAG, "IABTCF_gdprApplies:" + gdprApplies);

            mPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            AlxLog.i(AlxLogLevel.OPEN, TAG, "onSharedPreferenceChanged:" + key);
            try {
                if (mPreferences == null || key == null) {
                    return;
                }
                if (key.equals("IABTCF_TCString")) {
                    AlxAdBase.IABTCF_TCString = mPreferences.getString(key, "");
                    AlxLog.i(AlxLogLevel.OPEN, TAG, "onSharedPreferenceChanged IABTCF_TCString:" + AlxAdBase.IABTCF_TCString);
                    if (!mIsSetGDPR_Consent
                            || TextUtils.equals(AlxAdBase.gdpr_consent, "1")) {
                        AlxAdBase.gdpr_consent = AlxAdBase.IABTCF_TCString;
                    }
                } else if (key.equals("IABTCF_gdprApplies")) {
                    AlxAdBase.IABTCF_gdprApplies = mPreferences.getInt(key, 0);
                    AlxLog.i(AlxLogLevel.OPEN, TAG, "onSharedPreferenceChanged IABTCF_gdprApplies:" + AlxAdBase.IABTCF_gdprApplies);
                    if (!mIsSetGDPR) {
                        AlxAdBase.gdpr = AlxAdBase.IABTCF_gdprApplies;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public static void getGAID(final Context context, final AlxSdkInitCallback callback) {
        if (context == null) {
            if (callback != null) {
                callback.onInit(false, "context is null obj");
            }
            return;
        }
        AlxAdBase.advertising_id = AlxPreferences.getString(context, AlxPreferences.Key.S_GAID);
        final Handler handler = new Handler(Looper.getMainLooper());
        if (TextUtils.isEmpty(AlxAdBase.advertising_id) || AlxAdBase.advertising_id.equals(AlxAdBase.ADVERTISING_ID_EMPTY)) {
            new Thread(new Runnable() {
                public void run() {
                    boolean isOk = false;
                    String msg = "";
                    try {
                        GaidTool.AdInfo adInfo = GaidTool.getAdvertisingIdInfo(context);
                        String gaid = adInfo.getId();
                        AlxAdBase.advertising_id = gaid;
                        AlxPreferences.putString(context, AlxPreferences.Key.S_GAID, AlxAdBase.advertising_id);
                        if (TextUtils.isEmpty(AlxAdBase.advertising_id)) {
                            isOk = false;
                            msg = "gaid is empty";
                            AlxAdBase.advertising_id = AlxAdBase.ADVERTISING_ID_EMPTY;
                        } else {
                            isOk = true;
                            msg = "success";
                        }
                        AlxLog.e(AlxLogLevel.MARK, TAG, "GAID:" + AlxAdBase.advertising_id);
                    } catch (Exception e) {
                        isOk = false;
                        msg = "Gaid failed,init error:" + e.getMessage();
                        AlxAdBase.advertising_id = AlxAdBase.ADVERTISING_ID_EMPTY;
                        AlxPreferences.putString(context, AlxPreferences.Key.S_GAID, "");
                        AlxLog.i(AlxLogLevel.ERROR, TAG, "GAID error:" + e.getMessage());
                    }

                    if (callback != null) {
                        final boolean tempIsOk = isOk;
                        final String tempMsg = msg;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    callback.onInit(tempIsOk, tempMsg);
                                }
                            }
                        });
                    }
                }
            }).start();
        } else {
            AlxLog.e(AlxLogLevel.MARK, TAG, "GAID:" + AlxAdBase.advertising_id);
            if (callback != null) {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    if (callback != null) {
                        callback.onInit(true, "success");
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onInit(true, "success");
                            }
                        }
                    });
                }
            }
        }
    }


    /*
        GDPR GDPR_Consent COPPA CCPA 设置
     */
    public static void setSubjectToGDPR(boolean value) {
        mIsSetGDPR = true;
        AlxAdBase.gdpr = value ? 1 : 0;

    }

    public static void setUserConsent(String value) {
        mIsSetGDPR_Consent = true;
        AlxAdBase.gdpr_consent = value;
    }

    public static void setBelowConsentAge(boolean value) {
        AlxAdBase.coppa = value ? 1 : 0;
    }

    //    The class contains the methods to get/set the usprivacy string
//    and a method to get the current version.
//
//    The usprivacy string as the format: ”vnol” where
//            v = version (int)
//    n = Notice Given (char)
//    o = OptedOut (char)
//    l = Lspact (char)
//
//    Example: “1YYY” Version 1, Notice given, Opted out, under Lspact.
//
//    Default is null.
//    https://github.com/InteractiveAdvertisingBureau/USPrivacy/blob/master/CCPA/US%20Privacy%20String.md
//
    public static void subjectToUSPrivacy(String value) {
        AlxAdBase.us_privacy = value;
    }
}