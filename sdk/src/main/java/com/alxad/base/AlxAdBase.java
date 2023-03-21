package com.alxad.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;

import com.alxad.analytics.AlxAgent;
import com.alxad.oaid.helpers.AlxDevicesIDsHelper;
import com.alxad.util.AlxDeviceInfoUtil;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxNetworkUtil;
import com.alxad.util.AlxPreferences;
import com.alxad.util.AlxUtil;
import com.alxad.util.GaidTool;

import java.util.Locale;


/**
 * Created by huangweiwu on 2018/5/16.
 */
public class AlxAdBase {
    private static final String TAG = "AlxAdBase";

    //gaid（google id）默认值
    public static final String ADVERTISING_ID_EMPTY = "00000000-0000-0000-0000-000000000000";

    // token sid
    public static String token;
    public static String sid;

    // app
    public static String app_id;
    public static String app_bundle_id;
    public static String app_name;
    public static String app_version_name;

    // user
    public static String user_id;
    public static String ip;

    // device
    public static String user_agent = "Dalvik/2.1.0 (Linux; U; Android 6.0; LG-H440n Build/MRA58K)";
    public static String device_id_oaid;  //oaid（国内）
    public static String advertising_id; // gaid（google id）
    //google play不让获取系统设备信息
    public static String device_id_imei = "";
    public static String device_id_mac = "";
    public static String device_id_android_id = "";
    public static String carrier_id = "";

    public static Double latitude; //获取纬度，对象为空可以不传此参数
    public static Double longitude; //获取经度，对象为空可以不传此参数
    public static String country;
    public static String language = "EN";

    public static int wireless_network_type;

    // regs
    public static int coppa = 0;
    public static int gdpr = 0;
    public static String gdpr_consent = "";
    public static String us_privacy = "1YN-";

    // IAB TCF
    public static String IABTCF_TCString = "";
    public static int IABTCF_gdprApplies = 0;

    public static void initGlobalParam(Context context) {
        initGlobalParam(context, true);
    }

    public static void initGlobalParam(Context context, boolean isLoadGaid) {
        try {
            if (TextUtils.isEmpty(advertising_id) || advertising_id.equals(ADVERTISING_ID_EMPTY)) {
                advertising_id = AlxPreferences.getString(context, AlxPreferences.Key.S_GAID);
            }

            if (TextUtils.isEmpty(advertising_id) || advertising_id.equals(ADVERTISING_ID_EMPTY)) {
                advertising_id = ADVERTISING_ID_EMPTY;
                if (isLoadGaid) {
                    getGAID(context);
                }
            }

            if (TextUtils.isEmpty(device_id_oaid)) {
                device_id_oaid = AlxPreferences.getString(context, AlxPreferences.Key.S_OAID);
            }

            if (TextUtils.isEmpty(device_id_oaid)) {
                device_id_oaid = "";
                getOAID(context);
            }

            if (TextUtils.isEmpty(user_id)) {
                user_id = advertising_id;
            }

            app_bundle_id = context.getPackageName();
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        context.getPackageName(), 0);
                app_version_name = packageInfo.versionName;
            } catch (Exception e) {
                e.printStackTrace();
                AlxAgent.onError(e);
            }
            app_name = AlxDeviceInfoUtil.getAppName(context);
// 这里不能初始化设置，可能比开发者调用设置的晚，覆盖了他们设置的值
//            coppa = gdpr = 0;
//            gdpr_consent = "";
//            us_privacy = "1YN-";
            try {
                country = Locale.getDefault().getISO3Country();
            } catch (Exception e) {
                e.printStackTrace();
                AlxAgent.onError(e);
            }
            language = Locale.getDefault().getLanguage();

            user_agent = AlxDeviceInfoUtil.getUserAgent(context);
            wireless_network_type = AlxNetworkUtil.getNetworkState(context);

            ip = AlxDeviceInfoUtil.getIPAddress(context);

            if (latitude == null || longitude == null) {
                getGeo(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "initGlobalParam-error:" + e.getMessage());
        }
    }

    public static void getGAID(final Context context) {
        if (context == null) {
            return;
        }
        if (TextUtils.isEmpty(advertising_id) || advertising_id.equals(ADVERTISING_ID_EMPTY)) {
            AlxAdNetwork.execute(new Runnable() {
                public void run() {
                    try {
                        GaidTool.AdInfo adInfo = GaidTool.getAdvertisingIdInfo(context);
                        advertising_id = adInfo.getId();
                        AlxPreferences.putString(context, AlxPreferences.Key.S_GAID, advertising_id);
                        if (TextUtils.isEmpty(advertising_id)) {
                            advertising_id = ADVERTISING_ID_EMPTY;
                        }
                        AlxLog.i(AlxLogLevel.MARK, TAG, "GAID:" + advertising_id);
                    } catch (Exception e) {
                        advertising_id = ADVERTISING_ID_EMPTY;
                        AlxPreferences.putString(context, AlxPreferences.Key.S_GAID, "");
                        AlxLog.e(AlxLogLevel.ERROR, TAG, "GAID error:" + e.getMessage());
                    }
                }
            });
        } else {
            AlxLog.i(AlxLogLevel.MARK, TAG, "GAID:" + advertising_id);
        }
    }

    private static void getOAID(final Context context) {
        if (TextUtils.isEmpty(device_id_oaid)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AlxDevicesIDsHelper helper = new AlxDevicesIDsHelper(new AlxDevicesIDsHelper.AppIdsUpdater() {
                            @Override
                            public void OnIdsAvalid(String ids) {
                                device_id_oaid = ids;
                                if (device_id_oaid == null) {
                                    device_id_oaid = "";
                                }
                                AlxPreferences.putString(context, AlxPreferences.Key.S_OAID, device_id_oaid);
                                AlxLog.i(AlxLogLevel.MARK, TAG, "OAID: " + device_id_oaid);
                            }
                        });
                        helper.getOAID(context);
                    } catch (Exception e) {
                        device_id_oaid = "";
                        AlxLog.e(AlxLogLevel.ERROR, TAG, "OAID error: " + e.getMessage());
                    }
                }
            }).start();
        } else {
            AlxLog.i(AlxLogLevel.MARK, TAG, "OAID: " + device_id_oaid);
        }
    }

    @SuppressLint("MissingPermission")
    private static void getGeo(Context context) {
        if (context == null) {
            return;
        }
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setCostAllowed(false);
            //设置位置服务免费
            criteria.setAccuracy(Criteria.ACCURACY_COARSE); //设置水平位置精度
            //getBestProvider 只有允许访问调用活动的位置供应商将被返回
            String providerName = lm.getBestProvider(criteria, true);
            if (providerName != null) {
                if (AlxUtil.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && AlxUtil.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                Location location = lm.getLastKnownLocation(providerName);
                if (location == null) {
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (location != null) {
                    //获取维度信息
                    latitude = Double.valueOf(location.getLatitude());
                    //获取经度信息
                    longitude = Double.valueOf(location.getLongitude());
                }
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            e.printStackTrace();
        }
    }

}