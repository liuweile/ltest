
package com.alxad.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.webkit.WebSettings;

import com.alxad.base.AlxLogLevel;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 获取手机设备信息工具类
 */
public final class AlxDeviceInfoUtil {
    private static final String TAG = "AlxDeviceInfoUtil";
//    private static final int SDK_INT_10 = 29;

//    真实的imei
//    private static String IMEI_VALUE = "";
//    public static String IMEI_KEY_VALUE = "imei_key_value";
//
//    //自定义imei
//    private static String CUSTOM_IMEI_VALUE = "";
//    public static String CUSTOM_IMEI_KEY_VALUE = "custom_imei_key_value";
//
//    public static String CPU_NAME = "";
//    private static final String CPU_NAME_TAG = "cpu_name";
//
//    private static String DISPLAY_NAME = "";
//    private static final String DISPLAY_TAG = "display_tag";
//
//    private static String MCC_MCC = "";

//    private static String COUNTRY_ISO = "";

    /**
     * 获取手机设备id
     *
     * @param context
     * @return
     */
//    public static String getDeviceId(Context context) {
//        String deviceId = "";
//        try {
//            int readPhoneState;
//            if (Build.VERSION.SDK_INT >= SDK_INT_10) {
//                readPhoneState = context.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE");
//            } else {
//                readPhoneState = context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
//            }
////            int readPhoneState = context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
//            boolean haveReadPhoneState = readPhoneState == 0;
//            if (haveReadPhoneState) {
//                TelephonyManager tm = (TelephonyManager) context
//                        .getSystemService(Context.TELEPHONY_SERVICE);
//                deviceId = tm.getDeviceId();
//            }
//        } catch (Exception e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getDeviceId():" + e.getMessage());
//        }
//        return deviceId;
//        return "";
//    }

    /**
     * 获取androidId
     *
     * @param var0
     * @return
     */
//    public static String getAndroidId(Context var0) {
//        String androidId = "";
//        try {
//            androidId = Settings.Secure.getString(var0.getContentResolver(), Settings.Secure.ANDROID_ID);
//        } catch (Exception e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getAndroidId():" + e.getMessage());
//        }
//        return androidId;
//    }

    /**
     * 获取真实imei
     *
     * @return
     */
//    public static String getRealImei(Context context) {
//        String imei = "";
//        try {
//            if (!TextUtils.isEmpty(IMEI_VALUE)) {
//                return IMEI_VALUE;
//            } else {
//                IMEI_VALUE = AlxPreferences.getString(context, IMEI_KEY_VALUE);
//            }
//
//            if (!TextUtils.isEmpty(IMEI_VALUE)) {
//                return IMEI_VALUE;
//            }
//            imei = getDeviceId(context);
//            if (!TextUtils.isEmpty(imei)) {
//                AlxPreferences.putString(context, IMEI_KEY_VALUE, imei);
//                IMEI_VALUE = imei;
//            }
//        } catch (Exception e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getRealImei():" + e.getMessage());
//        }
//        return imei;
//    }

    /**
     * 获取拿不到真实imei时，就自定义imei
     *
     * @return
     */
//    public static String getNoRealImei(Context context) {
//        String imei = "";
//        try {
//            imei = getRealImei(context);
//            if (TextUtils.isEmpty(imei)) {
//                imei = getCustomDeviceId(context);
//            }
//        } catch (Exception e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getNoRealImei():" + e.getMessage());
//        }
//        return imei;
//        return "";
//    }


    /***
     * 获取运营商名字（中国电信，中国联通等）
     *
     * @param context
     * @return
     */
//    public static String getOperator(Context context) {
//        String strOperator = "";
//        try {
//            int readPhoneState =
//                    context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
//            boolean haveReadPhoneState = readPhoneState == 0;
//            if (haveReadPhoneState) {
//                TelephonyManager tm =
//                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//                if (tm != null) {
//                    strOperator = tm.getNetworkOperatorName();
//                }
//            }
//        } catch (Exception e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getOperator():" + e.getMessage());
//            e.printStackTrace();
//        }
//
//        if (TextUtils.isEmpty(strOperator)) {
//            strOperator = "";
//        }
//        return strOperator;
//    }


    /***
     * 获取运营商的编码(获取之前需要对权限[READ_PHONE_STATE]判断)（如：460036261019458）
     *
     * @param context
     * @return 可以到以下地址了解相关信息
     * http://wenku.baidu.com/link?url=9yhQExnMh1xVqRldjTd63pnuIaWU2mMO4eUy8670HutDI7GaEDOzSIeQvcwZkP6ETz_y5gfh5gX_QP4RQuRXs1vLAY4zdS-qVylSyiT4kv7
     */
//    public static String getMccmmc(Context context) {
//        if (!TextUtils.isEmpty(MCC_MCC)) {
//            return MCC_MCC;
//        }
//        String strMccmmc = "";
//        try {
//            int readPhoneState;
//            if (Build.VERSION.SDK_INT >= SDK_INT_10) {
//                readPhoneState = context.checkCallingOrSelfPermission("android.permission.READ_PRIVILEGED_PHONE_STATE");
//            } else {
//                readPhoneState = context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
//            }
////            int readPhoneState = context.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE");
//            boolean haveReadPhoneState = readPhoneState == 0;
//            if (haveReadPhoneState) {
//                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//                if (tm != null) {
//                    strMccmmc = tm.getSubscriberId();
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getMccmmc():" + e.getMessage());
//        }
//
//        if (TextUtils.isEmpty(strMccmmc)) {
//            strMccmmc = "";
//        } else {
//            MCC_MCC = strMccmmc;
//        }
//        return strMccmmc;
//    }
//    public static String getMCCMNC(Context context) {
//        try {
//            /** 获取SIM卡的IMSI码
//             * SIM卡唯一标识：IMSI 国际移动用户识别码（IMSI：International Mobile Subscriber Identification Number）是区别移动用户的标志，
//             * 储存在SIM卡中，可用于区别移动用户的有效信息。IMSI由MCC、MNC、MSIN组成，其中MCC为移动国家号码，由3位数字组成，
//             * 唯一地识别移动客户所属的国家，我国为460；MNC为网络id，由2位数字组成，
//             * 用于识别移动客户所归属的移动网络，中国移动为00，中国联通为01,中国电信为03；MSIN为移动客户识别码，采用等长11位数字构成。
//             * 唯一地识别国内GSM移动通信网中移动客户。所以要区分是移动还是联通，只需取得SIM卡中的MNC字段即可
//             */
//            String imsi = getMccmmc(context);
//            if (!TextUtils.isEmpty(imsi)) {
////                if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
////                    //因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号
////                    //中国移动
////                } else if (imsi.startsWith("46001")) {
////                    //中国联通
////                } else if (imsi.startsWith("46003")) {
////                    //中国电信
////                }
//                return imsi.substring(0, 4);
//            }
//        } catch (Exception e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getMCCMNC():" + e.getMessage());
//        }
//        return "";
//    }

    /**
     * 获取mac地址
     *
     * @return
     */
//    public static String getMacAddress() {
//        String macAddress = null;
//        StringBuffer buf = new StringBuffer();
//        NetworkInterface networkInterface = null;
//        try {
//            networkInterface = NetworkInterface.getByName("eth1");
//            if (networkInterface == null) {
//                networkInterface = NetworkInterface.getByName("wlan0");
//            }
//            if (networkInterface == null) {
//                return "02:00:00:00:00:02";
//            }
//            byte[] addr = networkInterface.getHardwareAddress();
//            if (addr == null) {
//                return "02:00:00:00:00:02";
//            }
//            for (byte b : addr) {
//                buf.append(String.format("%02X:", b));
//            }
//            if (buf.length() > 0) {
//                buf.deleteCharAt(buf.length() - 1);
//            }
//            macAddress = buf.toString();
//        } catch (SocketException e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getMacAddress():" + e.getMessage());
//            return "02:00:00:00:00:02";
//        } catch (Exception e) {
//            AlxLog.e(AlxLogLevel.ERROR, TAG, "getMacAddress():" + e.getMessage());
//            return "02:00:00:00:00:02";
//        }
//        return macAddress;
//    }


    /**
     * 获取浏览器的UA
     *
     * @return
     */
    public static String getUserAgent(Context context) {
        String userAgent = "Dalvik/2.1.0 (Linux; U; Android 6.0; LG-H440n Build/MRA58K)";
        String result = "";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    userAgent = WebSettings.getDefaultUserAgent(context);
                } catch (Exception e) {
                    userAgent = System.getProperty("http.agent");
                }
            } else {
                userAgent = System.getProperty("http.agent");
            }
            StringBuffer sb = new StringBuffer();
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
            result = sb.toString();
        } catch (Exception e) {

        }
        return result;
    }


    /**
     * 获取app名字
     *
     * @param context
     * @return
     */
    public static String getAppName(Context context) {
        try {
            //兼容如下方式：	android:label="${APP_NAME}" 和 android:label="@string/app_name"
            PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(context.getApplicationInfo()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 自定义手机设备id
     *
     * @param context
     * @return
     */
//    private static String getCustomDeviceId(Context context) {
//        if (!TextUtils.isEmpty(CUSTOM_IMEI_VALUE)) {
//            return CUSTOM_IMEI_VALUE;
//        } else {
//            CUSTOM_IMEI_VALUE = AlxPreferences.getString(context, CUSTOM_IMEI_KEY_VALUE);
//        }
//
//        if (!TextUtils.isEmpty(CUSTOM_IMEI_VALUE)) {
//            return CUSTOM_IMEI_VALUE;
//        }
//
//        String strRandomImei = "";
//        if (TextUtils.isEmpty(strRandomImei)) {
//            strRandomImei = getAndroidId(context);
//
//            if (!TextUtils.isEmpty(strRandomImei)) {
//                strRandomImei = UUID.randomUUID().toString();
//            }
//
//            AlxPreferences.putString(context, CUSTOM_IMEI_KEY_VALUE, strRandomImei);
//        }
//        CUSTOM_IMEI_VALUE = strRandomImei;
//        return strRandomImei;
//    }


    /**
     * 获取语言
     *
     * @param context
     * @return
     */
//    public static String getCountryLang(Context context) {
//        Resources resources = context.getResources();
//        Locale locale = resources.getConfiguration().locale;
//        return locale.toString();
//    }

    /**
     * 获取国家代码
     *
     * @param context
     * @return
     */
//    public static String getCountryISO(Context context) {
//        if (!TextUtils.isEmpty(COUNTRY_ISO)) {
//            return COUNTRY_ISO;
//        }
//        Resources resources = context.getResources();
//        Locale locale = resources.getConfiguration().locale;
//        String countryISO = locale.getCountry().toLowerCase(Locale.US);
//        COUNTRY_ISO = countryISO;
//        return countryISO;
//    }


    /**
     * 获取 CPU 名称
     *
     * @param context
     * @return
     */
//    public static String getCpuName(Context context) {
//        if (!TextUtils.isEmpty(CPU_NAME)) {
//            return CPU_NAME;
//        } else {
//            CPU_NAME = AlxPreferences.getString(context, CPU_NAME_TAG);
//        }
//
//        if (!TextUtils.isEmpty(CPU_NAME)) {
//            return CPU_NAME;
//        }
//        String strCpu = "";
//        BufferedReader br = null;
//        try {
//            FileReader fr = new FileReader("/proc/cpuinfo");
//            br = new BufferedReader(fr);
//            int nCount = 100;
//            while (nCount-- > 0) {
//                String text = br.readLine();
//                if (!TextUtils.isEmpty(text) && text.startsWith("Processor")) {
//                    String[] array = text.split(":\\s+", 2);
//                    if (array.length >= 2) {
//                        strCpu = array[1];
//                        break;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        AlxPreferences.putString(context, CPU_NAME_TAG, strCpu);
//        CPU_NAME = strCpu;
//        return CPU_NAME;
//    }

    /**
     * 获取总内存大小
     *
     * @param context
     * @return
     */
//    public static String getTotalMemorySize(Context context) {
//        String strMemorySize = "";
//        String dir = "/proc/meminfo";
//        try {
//            FileReader localFileReader = new FileReader(dir);
//            BufferedReader localBufferedReader = new BufferedReader(
//                    localFileReader, 8192);
//            String str2 = localBufferedReader.readLine(); // 读取meminfo第一行，系统总内存大小
//            String[] arrayOfString = str2.split("\\s+");
//            for (String num : arrayOfString) {
//                Log.i(str2, num + "\t");
//            }
//            int initialMemory = Integer.valueOf(arrayOfString[1]).intValue() / 1024; // 获得系统总内存，单位是KB，乘以1024转换为Byte
//            strMemorySize = "" + initialMemory;
//            localBufferedReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return strMemorySize;
//    }

    /**
     * 获取分辨率
     *
     * @param context
     * @return
     */
//    public static String getResolusion(Context context) {
//        if (!TextUtils.isEmpty(DISPLAY_NAME)) {
//            return DISPLAY_NAME;
//        } else {
//            DISPLAY_NAME = AlxPreferences.getString(context, DISPLAY_TAG);
//        }
//
//        if (!TextUtils.isEmpty(DISPLAY_NAME)) {
//            return DISPLAY_NAME;
//        }
//
//        String displayStr = "";
//        if (!TextUtils.isEmpty(displayStr)) {
//            return displayStr;
//        }
//        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Display display = windowManager.getDefaultDisplay();
//        int screenWidth = display.getWidth();
//        int screenHeight = display.getHeight();
//        displayStr = screenWidth + " * " + "" + screenHeight;
//        AlxPreferences.putString(context, DISPLAY_TAG, displayStr);
//        DISPLAY_NAME = displayStr;
//        return DISPLAY_NAME;
//    }

    /**
     * 获取sd卡路径
     *
     * @return
     */
    public static String getSDPath() {
        String strPath = null;
        try {
            boolean sdCardExist = Environment.getExternalStorageState()
                    .equals(Environment.MEDIA_MOUNTED);
            if (sdCardExist) {
                File sdDir = Environment.getExternalStorageDirectory();
                strPath = sdDir.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strPath;
    }

    /**
     * 获取本地ip地址
     *
     * @param context
     * @return
     */
    public static String getIPAddress(Context context) {
        try {
            NetworkInfo info = ((ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // 当前使用2G/3G/4G网络
                    try {
                        for (Enumeration<NetworkInterface> en =
                             NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                            NetworkInterface intf = en.nextElement();
                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                                 enumIpAddr.hasMoreElements(); ) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (!inetAddress.isLoopbackAddress()
                                        && inetAddress instanceof Inet4Address) {
                                    return inetAddress.getHostAddress();
                                }
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                    // 当前使用无线网络
                    WifiManager wifiManager = (WifiManager) context.getSystemService(
                            Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                    // 得到IPV4地址
                    return ipAddress;
                }
            }

            return "0.0.0.0";
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            return "0.0.0.0";
        }
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
    }


}