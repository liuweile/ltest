package com.alxad.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alxad.config.AlxConfig;
import com.alxad.config.AlxConst;
import com.alxad.base.AlxLogLevel;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlxLog {

    public final static int LINE_SIZE = 3000;
    public static boolean DEBUG = false;
    public static Context mContext = null;
    private static AtomicBoolean mFirstInit = new AtomicBoolean(false);
    private static String mLogCacheDir = null;

    // 判断sdcard下是否有8个0的文件，有则开启测试模式
    public static void init(Context context) {
        if (mContext == null) {
            mContext = context;
            mLogCacheDir = AlxFileUtil.getLogSavePath(context);
            checkLogFile(context);
        }
    }

    // 判断sdcard下是否有8个0的文件，有则开启测试模式
    private static void checkLogFile(Context context) {
        if (context == null) {
            return;
        }
        try {
            String strPath = AlxFileUtil.getNewSDPath(context);
            if (strPath != null) {
                strPath = strPath + "/00000000";
                File file = new File(strPath);
                if (file.exists()) {
                    DEBUG = AlxConst.SDK_DEBUG = true;
                    AlxConfig.LOG_LEVEL_FILTER = null;
                }
            }
        } catch (Exception e) {

        }
    }

    public static void setDebugMode(boolean bDebug) {
        AlxLog.DEBUG = bDebug;
        if (!AlxLog.DEBUG) {
            checkLogFile(mContext);
        }
        if (!mFirstInit.getAndSet(true)) {
            String time = AlxUtil.formatTime(System.currentTimeMillis(), AlxUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
            StringBuilder sb = new StringBuilder();
            sb.append("================================= sdk启动时间").append(time).append(" =====================================");
            sb.append("\r\n");
            sb.append("=====================================================================================================");
            AlxLog.d(AlxLogLevel.OPEN, "sdk-init", sb.toString());
        }
    }

    public static void d(AlxLogLevel level, String strTag, String result) {
        if (!isPrintLog(result, level)) return;
        if (AlxConfig.LOG_SHOW_LINE_NUMBER) {
            strTag = generateTag(strTag);
        }
        AlxFileUtil.writeCacheLog(mLogCacheDir, "d", strTag, result);

        if (result.length() > LINE_SIZE) {
            for (int i = 0; i < result.length(); i += LINE_SIZE) {
                if (i + LINE_SIZE < result.length())
                    Log.d(strTag + i, result.substring(i, i + LINE_SIZE));
                else
                    Log.d(strTag + i, result.substring(i, result.length()));
            }
        } else
            Log.d(strTag, result);
    }

    public static void i(AlxLogLevel level, String strTag, String result) {
        if (!isPrintLog(result, level)) return;
        if (AlxConfig.LOG_SHOW_LINE_NUMBER) {
            strTag = generateTag(strTag);
        }
        AlxFileUtil.writeCacheLog(mLogCacheDir, "i", strTag, result);

        if (result.length() > LINE_SIZE) {
            for (int i = 0; i < result.length(); i += LINE_SIZE) {
                if (i + LINE_SIZE < result.length())
                    Log.i(strTag + i, result.substring(i, i + LINE_SIZE));
                else
                    Log.i(strTag + i, result.substring(i, result.length()));
            }
        } else
            Log.i(strTag, result);
    }

    public static void w(AlxLogLevel level, String strTag, String result) {
        if (!isPrintLog(result, level)) return;
        if (AlxConfig.LOG_SHOW_LINE_NUMBER) {
            strTag = generateTag(strTag);
        }
        AlxFileUtil.writeCacheLog(mLogCacheDir, "w", strTag, result);

        if (result.length() > LINE_SIZE) {
            for (int i = 0; i < result.length(); i += LINE_SIZE) {
                if (i + LINE_SIZE < result.length())
                    Log.w(strTag + i, result.substring(i, i + LINE_SIZE));
                else
                    Log.w(strTag + i, result.substring(i, result.length()));
            }
        } else
            Log.w(strTag, result);
    }

    public static void e(AlxLogLevel level, String strTag, String result) {
        if (!isPrintLog(result, level)) return;
        if (AlxConfig.LOG_SHOW_LINE_NUMBER) {
            strTag = generateTag(strTag);
        }
        AlxFileUtil.writeCacheLog(mLogCacheDir, "e", strTag, result);

        if (result.length() > LINE_SIZE) {
            for (int i = 0; i < result.length(); i += LINE_SIZE) {
                if (i + LINE_SIZE < result.length())
                    Log.e(strTag + i, result.substring(i, i + LINE_SIZE));
                else
                    Log.e(strTag + i, result.substring(i, result.length()));
            }
        } else
            Log.e(strTag, result);
    }

    public static void e(AlxLogLevel level, String tag, Throwable tr) {
        if (!DEBUG) return;
        if (AlxConfig.LOG_SHOW_LINE_NUMBER) {
            tag = generateTag(tag);
        }
        String result = Log.getStackTraceString(tr);
        AlxFileUtil.writeCacheLog(mLogCacheDir, "e", tag, result);
        Log.e(tag, "", tr);
    }

    //tag自动产生，格式: tagPrefix:className.methodName(L:lineNumber),
    private static String generateTag(String tagPrefix) {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(Locale.getDefault(), tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
//        tag = TextUtils.isEmpty(tagPrefix) ? tag : tagPrefix + "==" + tag;

        if (!TextUtils.isEmpty(tagPrefix)) {
            boolean isEquals = false;
            if (!tagPrefix.equals(callerClazzName)) {
                if (callerClazzName != null && callerClazzName.contains("$")) {//className=AlxAdRequest$1run
                    String newCallerClazzName = callerClazzName.substring(0, callerClazzName.indexOf("$"));
                    if (tagPrefix.equals(newCallerClazzName)) {
                        isEquals = true;
                    }
                }
            } else {
                isEquals = true;
            }

            if (!isEquals) {
                tag = tagPrefix + "<=>" + tag;
            }
        }
        return tag;
    }

    /**
     * 判断是否打印日志
     *
     * @return
     */
    private static boolean isPrintLog(String msg, AlxLogLevel level) {
        if (!DEBUG || TextUtils.isEmpty(msg)) {
            return false;
        }
        if (level == null || AlxConfig.LOG_LEVEL_FILTER == null) {
            return true;
        }
        if (AlxConfig.LOG_LEVEL_FILTER.contains(level.getValue()+"")) {
            return true;
        } else {
            return false;
        }
    }


}