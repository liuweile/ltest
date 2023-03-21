package com.alxad.report;

import android.os.Build;
import android.text.TextUtils;

import com.alxad.base.AlxAdBase;
import com.alxad.config.AlxConfig;
import com.alxad.config.AlxConst;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxTracker;
import com.alxad.http.AlxHttpManager;
import com.alxad.http.AlxHttpMethod;
import com.alxad.http.AlxHttpRequest;
import com.alxad.http.AlxHttpResponse;
import com.alxad.util.AlxAESUtil;
import com.alxad.util.AlxLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * sdk 广告上报，收集广告信息。是实时上报的模式。
 * <br/>
 * 跟埋点上报的区别：会比埋点传参数更多，是跟广告绑定的
 *
 * @author lwl
 * @date 2021-9-15
 */
public class AlxSdkData {

    private static final String TAG = "AlxSdkData";
    private static final Executor mCachedExecutor = Executors.newCachedThreadPool();

//    private static final String AES_KEY = "L4SECvPxIx5ILhdH";
//    private static final String AES_IV = "wE0fiU7CxYqZNHWH";

    /**
     * 简单封装上报
     *
     * @param tracker
     * @param eventName
     */
    public static void tracker(AlxTracker tracker, int eventName) {
        try {
            if (tracker == null || TextUtils.isEmpty(tracker.getRequestId())) {
                return;
            }
            onEvent(tracker.getRequestId(), eventName, tracker.getAdId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * sdk事件上报
     *
     * @param requestId 广告请求id
     * @param eventName
     * @param adslot_id 广告id
     */
    public static void onEvent(String requestId, int eventName, String adslot_id) {
        doWork(requestId, eventName, adslot_id, null);
    }

    /**
     * sdk事件上报
     *
     * @param requestId
     * @param eventName
     * @param adslot_id 广告id
     */
    public static void onEvent(String requestId, int eventName, String adslot_id, String msg) {
        JSONObject msgJson = null;
        try {
            if (!TextUtils.isEmpty(msg)) {
                msgJson = new JSONObject();
                msgJson.put("desc", msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        doWork(requestId, eventName, adslot_id, msgJson);
    }

    /**
     * sdk事件上报
     *
     * @param eventId
     * @param eventName
     * @param adslot_id
     */
    public static void onEvent(String eventId, int eventName, String adslot_id, JSONObject msg) {
        doWork(eventId, eventName, adslot_id, msg);
    }

    private static void doWork(String requestId, int eventName, String adslot_id, JSONObject msg) {
        String params = getJsonParams(requestId, eventName, adslot_id, msg);
        httpRequest(params);
    }

    private static void httpRequest(final String params) {
        if (TextUtils.isEmpty(params)) {
            return;
        }
        try {
            final String random = String.valueOf(new Random().nextInt());
            final String aseParams = AlxSdkData.getAesJson(params, random);
            if (TextUtils.isEmpty(aseParams)) {
                return;
            }
            if (TextUtils.isEmpty(AlxConfig.URL_DATA_ANALYTICS)) {
                return;
            }
            final String url = AlxConfig.URL_DATA_ANALYTICS + random;
            mCachedExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AlxLog.d(AlxLogLevel.DATA, TAG + "-url", url);
                        AlxLog.d(AlxLogLevel.DATA, TAG + "-params", params);
                        AlxLog.d(AlxLogLevel.DATA, TAG + "-params[aes]", aseParams);

                        Map<String, String> header = new HashMap<>();
                        header.put("Accept", "application/json");
                        AlxHttpRequest request = new AlxHttpRequest.Builder(url)
                                .setRequestMethod(AlxHttpMethod.POST)
                                .setContentType(true)
                                .setParams(aseParams)
                                .setOpenLog(false)
                                .setHeaders(header)
                                .builder();
                        AlxHttpResponse response = AlxHttpManager.getInstance().requestApiSync(request);
                        if (response != null) {
                            AlxLog.d(AlxLogLevel.DATA, TAG + "-response", response.getResponseMsg());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private static String getJsonParams(String requestId, int eventName, String adslot_id, JSONObject msg) {
        if (TextUtils.isEmpty(requestId)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject();
            json.put("app_id", AlxAdBase.app_id);
            json.put("sdkv", AlxConst.SDK_VERSION);
            json.put("app_bundle_id", AlxAdBase.app_bundle_id);
            json.put("device", getDevice());

            JSONArray eventArray = new JSONArray();

            JSONObject eventJson = new JSONObject();
            eventJson.put("id", requestId);
            if (!TextUtils.isEmpty(adslot_id)) {
                json.put("adslot_id", adslot_id);
            }
            eventJson.put("type", 1);
            eventJson.put("name", eventName);
            eventJson.put("time", System.currentTimeMillis());
            if (msg != null) {
                eventJson.put("info", msg);
            }
            eventArray.put(eventJson);

            json.put("event", eventArray);

            return json.toString();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }

    public static JSONObject getDevice() {
        JSONObject json = new JSONObject();
        try {
            json.put("ua", AlxAdBase.user_agent);
            json.put("did", AlxAdBase.device_id_imei);
            json.put("dpid", AlxAdBase.device_id_android_id);
            json.put("mac", AlxAdBase.device_id_mac);
            json.put("oaid", AlxAdBase.device_id_oaid);
            json.put("devicetype", 1);
            json.put("os", 2);
            json.put("osv", Build.VERSION.RELEASE);
            json.put("make", Build.BRAND);
            json.put("model", Build.MODEL);
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getDevice():" + e.getMessage());
        }
        return json;
    }

    /**
     * 对json字符串进行加密<br/>
     * Base64(Base64(json)+Random)
     *
     * @param json
     * @return
     */
    public static String getAesJson(String json, String random) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
//            byte[] aes = AlxAESUtil.encrypt(AES_KEY, AES_IV, json.getBytes("UTF-8"));
//            String aesParams = new String(Base64.encode(aes, Base64.NO_WRAP), "UTF-8");
//            return aesParams;
            String params = AlxAESUtil.base64Encrypt(json, random);
            return params;
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }


}