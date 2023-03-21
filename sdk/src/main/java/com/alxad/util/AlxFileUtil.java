package com.alxad.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;

import com.alxad.base.AlxAdBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by huangweiwu on 2017/11/20.
 */

public class AlxFileUtil {

    private static final String TAG = "AlxFileUtil";
    private static final String ROOT_DIR = "/alx/";//根目录

    //
    public static String readFile4Assets(Context context, String fileName) {
        StringBuilder result = new StringBuilder();
        try {
            InputStreamReader inputReader = new InputStreamReader(context
                    .getResources().getAssets().open(fileName));
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null)
                result.append(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    //
    private static String getSDPath() {
        String strPath = "";
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

    public static String getNewSDPath(Context context) {
        if (context == null) {
            return getSDPath();
        }
        File file = context.getExternalFilesDir(null);
        if (file == null) {
            return getSDPath();
        }
        String strPath = file.getPath();
        if (TextUtils.isEmpty(strPath)) {
            strPath = getSDPath();
        }
        return strPath;
    }

    public static String getVideoSavePath(Context context) {
        String dir = getNewSDPath(context) + ROOT_DIR + "video/";
        return dir;
    }

    public static String getImageSavePath(Context context) {
        String dir = getNewSDPath(context) + ROOT_DIR + "image/";
        return dir;
    }

    public static String getLogSavePath(Context context) {
        String dir = getNewSDPath(context) + ROOT_DIR + "log/";
        return dir;
    }

    public static String getAnalyticsPath(Context context){
        String dir = getNewSDPath(context) + ROOT_DIR + "analytics/";
        return dir;
    }

    //
    public static String byteBuff2String(ByteBuffer buffer) {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            charset = null;
            decoder = null;
            if (charBuffer != null) {
                charBuffer.clear();
                charBuffer = null;
            }
        }
        return "";
    }

    public static String inputStream2String(InputStream ins) {
        if (ins == null) {
            return null;
        }
        StringBuilder strBuff = new StringBuilder();
        byte[] buff = new byte[4 * 1024];
        try {
            int iRead = -1;
            while ((iRead = ins.read(buff)) != -1) {
                strBuff.append(new String(buff, 0, iRead, "UTF-8"));
            }
        } catch (IOException e) {
            strBuff = null;
            e.printStackTrace();
        }
        if (strBuff != null) {
            return strBuff.toString();
        } else {
            return null;
        }
    }

    public static String GetApkPkgName(Context context, String apkPath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                String packageName = appInfo.packageName;
                //String version = info.versionName;
                return packageName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /*
     * Java文件操作 获取文件扩展名
     *
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 写入日志: 此方法中不要做AlxLog打印，容易造成死循环
     *
     * @param dir      目录
     * @param logLevel
     * @param tag
     * @param msg
     */
    public static void writeCacheLog(final String dir, String logLevel, final String tag, final String msg) {
        if (dir == null) {
            return;
        }
        try {
            if (logLevel != null) {
                logLevel = logLevel.toUpperCase();
            }

            long time = System.currentTimeMillis();
            String timeStr = AlxUtil.formatTime(time, AlxUtil.FORMAT_YYYY_MM_DD_HH_MM_SS);
            String separator = "  ";//分隔符

            StringBuilder sb = new StringBuilder();
            sb.append(timeStr);
            sb.append(separator);
            sb.append(Process.myPid());
            sb.append("-");
            sb.append(Thread.currentThread().getId());
            sb.append("/");
            sb.append(AlxAdBase.app_bundle_id);
            sb.append(separator);
            sb.append(logLevel);
            sb.append("/");
            sb.append(tag);
            sb.append(":");

            final String logTag = sb.toString();

            Handler workHandler = WorkHandler.getDefault().getHandler();
            if (workHandler != null && msg != null) {
                workHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        writeSdLog(dir, logTag, msg);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入缓存日志：此方法中不要做AlxLog打印，容易造成死循环
     *
     * @param data
     */
    public static void writeSdLog(String dir, String tag, String data) {
        if (data == null || dir == null) {
            return;
        }

        String FILE_SUFFIX = ".log";
        RandomAccessFile raf = null;
        try {
            File fileDir = new File(dir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            long timezone = System.currentTimeMillis();
            String time = AlxUtil.formatTime(timezone, AlxUtil.FORMAT_YYYY_MM_DD);
            String fileName = "alx_" + time + FILE_SUFFIX;

            File file = new File(fileDir.getPath(), fileName);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                long maxCacheLength = 1024 * 1024 * 2;//如果文件大于2MB,将之前的数据写入到新的文件，对文件拆分
                if (file.length() > maxCacheLength) {
                    synchronized (file) {
                        long length = file.length();
                        if (length > maxCacheLength) {
                            String seconds = AlxUtil.formatTime(timezone, AlxUtil.FORMAT_YYYY_MM_DD_HMS);
                            String newFileName = "alx_" + seconds + FILE_SUFFIX;
                            file.renameTo(new File(fileDir.getPath(), newFileName));

                            if (!file.exists()) {
                                file.createNewFile();
                            }
                        }
                    }
                }
            }

            raf = new RandomAccessFile(file, "rws");
            long length = raf.length();
            raf.seek(length);
            StringBuilder sb = new StringBuilder();
            if (length > 0) {
                sb.append("\r\n\r\n");
            }
            sb.append(tag);
            sb.append("\r\n");
            sb.append(data);
            raf.write(sb.toString().getBytes("UTF-8"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 清理缓存文件
     *
     * @param cacheDir
     * @param maxCacheTime 最大缓存时间，单位是s
     */
    public static void clearCache(String cacheDir, int maxCacheTime) {
        if (TextUtils.isEmpty(cacheDir)) {
            return;
        }
        File file = new File(cacheDir);
        if (!file.isDirectory()) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        File[] list = file.listFiles();
        if (list == null || list.length < 1) {
            //删除空目录
            file.delete();
            return;
        }
        for (File item : list) {
            if (item == null) {
                continue;
            }
            if (item.isDirectory()) {
                clearCache(item.getPath(), maxCacheTime);
            } else if (item.isFile()) {
                if (System.currentTimeMillis() - file.lastModified() > maxCacheTime * 1000L) {
                    item.delete();
                }
            }
        }
    }

}