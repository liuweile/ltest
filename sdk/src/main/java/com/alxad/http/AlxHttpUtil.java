package com.alxad.http;


import com.alxad.util.MD5Util;

import java.io.Closeable;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AlxHttpUtil {

    public static boolean isEmpty(String str) {
        if (str == null || str.trim().length() < 1) {
            return true;
        }
        return false;
    }

    public static String urlEncodeStr(String str) {
        if (str == null || str.trim().length() < 1) {
            return "";
        }
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getCharsetName(String contentType) {
        if (contentType == null) {
            return null;
        }
        try {
            String value = "charset";
            if (contentType.contains(value)) {
                int index = contentType.indexOf(value) + value.length();
                return contentType.substring(index + 1);
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static boolean isUrl(String url) {
        if (isEmpty(url)) {
            return false;
        }
        if (url.toLowerCase().startsWith("http")) {
            return true;
        }
        return false;
    }

    /**
     * 默认下载文件名获取方式：通过url 获取文件名
     *
     * @param url
     * @return
     */
    public static String getDownloadFileName(String url) {
        if (!isUrl(url)) {
            return null;
        }
        return MD5Util.getUPMD5(url);
    }

    /**
     * 获取下载路径
     *
     * @param url
     * @param downDir
     * @return
     */
    public static String getDefaultDownloadPath(String url, String downDir) {
        String fileName = getDownloadFileName(url);
        if (isEmpty(fileName)) {
            return null;
        }
        if (isEmpty(downDir)) {
            return null;
        }
        File fileDir = new File(downDir);
        if (!fileDir.isDirectory()) {
            return null;
        }
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(downDir);
        if (!downDir.endsWith(File.separator)) {
            sb.append(File.separator);
        }
        sb.append(fileName);
        return sb.toString();
    }

    /**
     * 判断两个对象是否相等
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * 是否是重定向
     *
     * @param statusCode 网络请求返回的状态码
     * @return
     */
    public static boolean isHttpRedirect(int statusCode) {
        return statusCode / 100 == 3;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 文件重命名
     *
     * @param tempFile
     * @param file
     * @return
     * @throws Exception
     */
    public static File fileRename(File tempFile, File file) throws Exception {
        if (tempFile.exists()) {
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            file.createNewFile();
            boolean isOk = tempFile.renameTo(file);
            if (!isOk) {
                file.delete();
            }
        }
        return file;
    }

    /**
     * 删除文件
     *
     * @param dir      文件目录路径
     * @param fileName 文件名
     * @return
     * @throws Exception
     */
    public static File deleteFile(String dir, String fileName) throws Exception {
        String strPathName = dir + "/" + fileName;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        File file = new File(strPathName);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            }
        }
        return file;
    }


    /**
     * 删除文件
     *
     * @param file 文件
     * @return
     */
    public static boolean deleteFile(File file) throws Exception {
        if (file != null && file.exists()) {
            if (file.isFile()) {
                return file.delete();
            }
        }
        return false;
    }

}