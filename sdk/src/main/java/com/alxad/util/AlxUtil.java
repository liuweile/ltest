package com.alxad.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;


import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlxUtil {
    private static final String TAG = "AlxUtil";

    public static final String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String FORMAT_YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_YYYY_MM_DD_HMS = "yyyy-MM-dd_HH-mm-ss";

    /**
     * 格式化时间
     *
     * @param time
     * @param formatStr
     * @return
     */
    public static String formatTime(long time, String formatStr) {
        String result = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formatStr, Locale.getDefault());
            result = sdf.format(new Date(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将 00:00:10 时间转化成秒
     *
     * @param time
     * @return 得到s
     */
    public static int transformSecond(String time) {
        int result = -1;
        if (time == null || !time.contains(":")) {
            return result;
        }
        try {
            String[] arr = time.split(":");
            if (arr.length != 3) {
                return result;
            }
            int hour = Integer.parseInt(arr[0]);
            int minute = Integer.parseInt(arr[1]);
            int second = Integer.parseInt(arr[2]);
            result = hour * 3600;
            result += minute * 60;
            result += second;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        if (context == null) {
            return 0;
        }
        try {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float scale = metrics.density;
            return (int) (pxValue / scale + 0.5f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        if (context == null) {
            return 0;
        }
        try {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float scale = metrics.density;
            return (int) (dipValue * scale + 0.5f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 保留几位小数
     *
     * @param number
     * @param length
     * @return
     */
    public static double formatNumber(double number, int length) {
        try {
            return BigDecimal.valueOf(number).setScale(length, BigDecimal.ROUND_HALF_UP).doubleValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return number;
    }

    public static int checkSelfPermission(Context context, String permission) throws Exception {
        if (permission == null) {
            throw new NullPointerException("permission is null");
        }
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
    }

    /**
     * 判断包是否已经安装
     */
    public static boolean isAppInstalled(final Context context, String strPacketName) {
        boolean installed = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(strPacketName, PackageManager.GET_SIGNATURES);
            installed = info == null ? false : true;
        } catch (Exception e) {
            installed = false;
        }
        return installed;
    }

    public static void installApp(final Context context, String strApkPath) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(
                    Uri.fromFile(new File(strApkPath)),
                    "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Throwable e) {

        }
    }

    public static boolean openInstalledApp(Context context, String packageName) {
        if (openInstalledAppDef(context, packageName)) {
            return true;
        }

        Intent queryIntent = new Intent(Intent.ACTION_MAIN, null);
        queryIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        queryIntent.setPackage(packageName);

        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(queryIntent, 0);

        if (apps != null && apps.size() > 0 && apps.iterator().next() != null) {
            ResolveInfo ri = apps.iterator().next();
            String className = ri.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            try {
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
            }
        }

        return false;
    }

    private static boolean openInstalledAppDef(Context context, String packageName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            // maybe return null
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

//    public static void redirect2PlayStore(final Context context, String strPacketName) {
//        try {
//            Uri uri = Uri.parse("market://details?id=" + strPacketName);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            if (isAppInstalled(context, AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME)) {
//                intent.setPackage(AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME);
//            }
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        } catch (Throwable e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }

//    public static void redirect2PlayStore(final Context context, String strPacketName, String strSource) {
//        try {
//            Uri uri = Uri.parse("market://details?id=" + strPacketName
//                    + "&referrer=utm_source%3D" + strSource);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            if (isAppInstalled(context, AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME)) {
//                intent.setPackage(AlxConst.GOOGLE_PLAY_APP_PACKAGE_NAME);
//            }
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        } catch (Throwable e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }
//
//    public static Intent redirect2PlayStorePending(final Context context, String strPacketName, String strType) {
//        try {
//            Uri uri = Uri.parse("market://details?id=" + strPacketName
//                    + "&referrer=utm_source%3D" + CpsConfig.STR_PACKET_NAME
//                    + ":" + strType);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            if (isAppInstalled(context, "com.android.vending")) {
//                intent.setPackage("com.android.vending");
//            }
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            return intent;
//        } catch (Throwable e) {
//            Log.e(TAG, e.getMessage());
//            return null;
//        }
//    }

//    public static void redirect2Share(final Activity context, String strText) {
//        try {
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("text/*");
//            intent.putExtra(Intent.EXTRA_TEXT, strText);
//            context.startActivity(intent);
//        } catch (Throwable e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }

//    public static void redirect2AppStoreHome(final Context context, String strHome) {
//        try {
//            String strStudio = "market://search?q=pub:" + strHome;
//            Uri uri = Uri.parse(strStudio);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        } catch (Throwable e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }

    public static boolean isTopActivity(Context context, String strTag) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String strTop = getTopActivityPackageName(am);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Log.i(TAG, strTag + "-->" + strTop);
        if (strTop.equals(strTag) && pm.isScreenOn()) {
            return true;
        } else {
            return false;
        }
    }

    //
    private static String getTopActivityPackageName(ActivityManager am) {
        if (am == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT < 21) {
            ComponentName componentName = am.getRunningTasks(1).get(0).topActivity;
            return componentName.getPackageName();
        }

        // android 5.0 以上（Lollipop)，由于getRunningTasks不再能获取到实时数据，因此需要另一套方案
        // 利用反射实现，确实比较恶心，且不知何时又会失效。
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (NoSuchFieldException e) {
            return null;
        }
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && app.importanceReasonCode == 0) {
                int state;
                try {
                    state = field.getInt(app);
                } catch (IllegalAccessException e) {
                    continue;
                }
                if (state == 2) {
                    currentInfo = app;
                    break;
                }
            }
        }
        if (currentInfo == null) {
            return null;
        } else {
            String process_name = currentInfo.processName;
            if (process_name.contains(":")) {
                // 有可能获取到含冒号的字符串结果
                String[] process_name_arr = process_name.split(":");
                return process_name_arr.length > 0 ? process_name_arr[0] : process_name;
                // crash修复
            } else {
                return process_name;
            }
        }
    }

//    public static int getCPUUseRate(Context context) {
//        int rate = 0;
//        try {
//            String Result;
//            java.lang.Process p = Runtime.getRuntime().exec("top -n 1");
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            while ((Result = br.readLine()) != null) {
//                if (Result.trim().length() < 1) {
//                    continue;
//                } else {
//                    String[] CPUusr = Result.split("%");
//                    String[] CPUusage = CPUusr[0].split("User");
//                    String[] SYSusage = CPUusr[1].split("System");
//                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
//                    break;
//                }
//            }
//
//        } catch (Exception e) {
//        }
//        AlxLog.i(AlxLogLevel.MARK, TAG, "cpu rate:" + rate);
//        return rate;
//    }

//    public static long getMemInfo(Context context) {
//        long availMem = 0;
//        try {
//            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
//            activityManager.getMemoryInfo(memoryInfo);
//            availMem = memoryInfo.availMem / 1024 / 1024;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        AlxLog.i(AlxLogLevel.MARK, TAG, "memory avail:" + availMem);
//        return availMem;
//    }

    /**
     * 获取视频的第一帧图片
     *
     * @param videoUrl
     * @return
     */
//    public static Bitmap getNetVideoBitmap(String videoUrl) {
//        Bitmap bitmap = null;
//        if (TextUtils.isEmpty(videoUrl)) {
//            return null;
//        }
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        try {
//            String lowerUrl = videoUrl.toLowerCase();
//            if (lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://") || lowerUrl.startsWith("widevine://")) {
//                //根据url获取缩略图
//                retriever.setDataSource(videoUrl, new HashMap<String, String>());
//            } else {
//                //获取本地封面图片
//                retriever.setDataSource(videoUrl);
//            }
//            //获得第一帧图片
//            bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//
//            // Scale down the bitmap if it's too large.
//            if (bitmap != null) {
//                int width = bitmap.getWidth();
//                int height = bitmap.getHeight();
//                int max = Math.max(width, height);
//                if (max > 512) {
//                    float scale = 512f / max;
//                    int w = Math.round(scale * width);
//                    int h = Math.round(scale * height);
//                    bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                retriever.release();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return bitmap;
//    }

    /**
     * 将字符串转换成数字
     *
     * @param number
     * @return
     */
    public static int getInt(String number) {
        int defaultValue = 0;
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {

        }
        return defaultValue;
    }


}