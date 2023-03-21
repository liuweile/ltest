package com.alxad.net.lib;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.alxad.base.AlxAdBase;
import com.alxad.config.AlxConst;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxUtil;

import org.json.JSONObject;

import java.util.Locale;
import java.util.UUID;


/**
 * 广告请求数据辅助类
 *
 * @author lwl
 * @date 2021-11-1
 */
public class AlxRequestTool {
    public static final String TAG = "AlxRequestTool";


    /**
     * 获取广告请求参数
     *
     * @param context
     * @param adId              广告位id
     * @param requestId         请求随机生成的请求id
     * @param adType            广告类型
     * @param screenOrientation 请求广告时当前的屏幕方向
     * @return
     */
    public static String getJsonStr(Context context, String adId, String requestId, int adType, int screenOrientation) {
        String params = null;
        try {
            JSONObject json = new JSONObject();
            json.put("id", requestId);
            json.put("adtype", adType);
            json.put("app_id", AlxAdBase.app_id);
            json.put("adslot_id", adId);
            json.put("sdkv", AlxConst.SDK_VERSION);
            json.put("bundle", AlxAdBase.app_bundle_id);
            json.put("app_name", AlxAdBase.app_name);
            json.put("app_version", AlxAdBase.app_version_name);
            json.put("screen_orientation", screenOrientation);

            json.put("device", getDevice(context));
            json.put("regs", getRegs());
            params = json.toString();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getJsonStr():error:" + e.getMessage());
            params = null;
        }
        return params;
    }

    /**
     * 创建广告请求的唯一id
     *
     * @return
     */
    public static String createRequestId() {
        return UUID.randomUUID() + "|" + System.currentTimeMillis();
    }

    private static JSONObject getDevice(Context context) {
        JSONObject json = new JSONObject();
        try {
            json.put("user_agent", AlxAdBase.user_agent);

            json.put("did", AlxAdBase.device_id_imei);
            json.put("dpid", AlxAdBase.device_id_android_id);
            json.put("mac", AlxAdBase.device_id_mac);
            json.put("ifa", AlxAdBase.advertising_id);
            json.put("oaid", AlxAdBase.device_id_oaid);

            json.put("geo", getGeo());

            json.put("device_type", 1); //0:未知、1:手机、2:平板
            json.put("language", AlxAdBase.language);
            json.put("os", 2);//设备操作系统. 1 = IOS , 2 = Android, 0 = 未知.
            json.put("osv", Build.VERSION.RELEASE);
            json.put("make", Build.BRAND);
            json.put("model", Build.MODEL);
            json.put("carrier_id", AlxAdBase.carrier_id);
            json.put("connectiontype", AlxAdBase.wireless_network_type);

            try {
                if (context != null) {
                    DisplayMetrics dm = context.getResources().getDisplayMetrics();
                    json.put("screen_width", dm.widthPixels);
                    json.put("screen_height", dm.heightPixels);
                    json.put("screen_density", dm.densityDpi);
                }
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "getDevice():error-0:" + e.getMessage());
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getDevice():error:" + e.getMessage());
            e.printStackTrace();
        }
        return json;
    }

    //geo:{}
    private static JSONObject getGeo() {
        JSONObject json = new JSONObject();
        try {
            if (AlxAdBase.latitude != null && AlxAdBase.longitude != null) {
                json.put("latitude", AlxUtil.formatNumber(AlxAdBase.latitude.doubleValue(), 6));
                json.put("longitude", AlxUtil.formatNumber(AlxAdBase.longitude.doubleValue(), 6));
            }
            json.put("country", AlxAdBase.country);
            json.put("region", Locale.getDefault().getCountry());
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getGeo():error:" + e.getMessage());
            e.printStackTrace();
        }
        return json;
    }

    private static JSONObject getRegs() {
        JSONObject json = new JSONObject();
        try {
            json.put("coppa", AlxAdBase.coppa);
            json.put("gdpr", AlxAdBase.gdpr);
            if (AlxAdBase.gdpr == 1 && TextUtils.isEmpty(AlxAdBase.gdpr_consent)) {
                AlxAdBase.gdpr_consent = "1";
            }
            json.put("gdpr_consent", AlxAdBase.gdpr_consent);
            json.put("us_privacy", AlxAdBase.us_privacy);
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getRegs():error:" + e.getMessage());
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 广告当前请求的屏幕方向
     *
     * @param context
     * @return
     */
    public static int getScreenOrientation(Context context) {
        if (context == null) {
            return AlxRequestBean.SCREEN_ORIENTATION_UNKNOW;
        }
        try {
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {//竖屏广告
                return AlxRequestBean.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {//横屏广告
                return AlxRequestBean.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getScreenOrientation():error:" + e.getMessage());
            e.printStackTrace();
        }
        return AlxRequestBean.SCREEN_ORIENTATION_UNKNOW;
    }

}