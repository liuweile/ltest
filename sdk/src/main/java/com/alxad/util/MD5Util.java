package com.alxad.util;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class MD5Util {
    private static final String TAG = "MD5Util";
    private MessageDigest md;
    private static MD5Util md5 = null;

    private MD5Util() {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("MD5 ERROR");
        }
    }

    // 产生一个MD5实例
    public static MD5Util getInstance() {
        if (null != md5) {
            return md5;
        } else {
            makeInstance();
            return md5;
        }
    }

    // 保证同一时间只有一个线程在使用MD5加密
    private static synchronized void makeInstance() {
        if (null == md5) {
            md5 = new MD5Util();
        }
    }

    public String createMD5(String pass) {
        md.update(pass.getBytes());
        byte[] b = md.digest();
        return byteToHexString(b);
    }

    private static String byteToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer();
        String temp = "";
        for (int i = 0; i < b.length; i++) {
            temp = Integer.toHexString(b[i] & 0Xff);
            if (temp.length() == 1) {
                temp = "0" + temp;
            }
            sb.append(temp);
        }
        return sb.toString();
    }

    public static String getFileMD5(String fName) {
        String strMD5 = "";
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(fName, "r");
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] buffer = new byte[1024 * 16];
            int fOffset = 0;
            while (true) {
                int readLen = f.read(buffer, fOffset, buffer.length);
                if (readLen == -1) {
                    strMD5 = byteToHexString(md.digest());
                    break;
                } else {
                    md.update(buffer, 0, readLen);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return strMD5;
    }


    public static String getUPMD5(String val) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(val.getBytes());
            return upHexEncode(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String upHexEncode(byte[] toencode) {
        StringBuilder sb = new StringBuilder(toencode.length * 2);
        for (byte b : toencode) {
            sb.append(Integer.toHexString((b & 240) >>> 4));
            sb.append(Integer.toHexString(b & 15));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    public static String toMd5(byte[] var0, boolean var1) {
        try {
            MessageDigest var2 = MessageDigest.getInstance("MD5");
            var2.reset();
            var2.update(var0);
            return toHexString(var2.digest(), "", var1);
        } catch (NoSuchAlgorithmException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String toHexString(byte[] var0, String var1, boolean var2) {
        StringBuilder var3 = new StringBuilder();
        byte[] var4 = var0;
        int var5 = var0.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            byte var7 = var4[var6];
            String var8 = Integer.toHexString(255 & var7);
            if (var2) {
                var8 = var8.toUpperCase();
            }

            if (var8.length() == 1) {
                var3.append("0");
            }

            var3.append(var8).append(var1);
        }

        return var3.toString();
    }

}
