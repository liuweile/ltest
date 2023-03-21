
package com.alxad.util;


import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AlxAESUtil {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM_NAME = "AES";
    private static final String UTF_8 = "UTF-8";

    private AlxAESUtil() {
    }

    public static byte[] decrypt(String key, String iv, byte[] input) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM_NAME);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] var6 = cipher.doFinal(input);
        return var6;
    }

    public static byte[] encrypt(String key, String iv, byte[] input) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), ALGORITHM_NAME);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] var6 = cipher.doFinal(input);
        return var6;
    }

    /**
     * base64 加密 <br/>
     * Base64(Base64(str)+key)
     *
     * @param str 加密字符串
     * @param key 加密的密钥
     * @return
     * @throws Exception
     */
    public static String base64Encrypt(String str, String key) throws Exception {
        String base1 = new String(Base64.encode(str.getBytes(UTF_8), Base64.NO_WRAP), UTF_8);
        String newStr = base1 + key;
        String base64 = new String(Base64.encode(newStr.getBytes(UTF_8), Base64.NO_WRAP), UTF_8);
        return base64;
    }

    /**
     * base64 解密
     *
     * @param str 加密字符串
     * @param key 加密的密钥
     * @return
     * @throws Exception
     */
    public static String base64Decrypt(String str, String key) throws Exception {
        String base1 = new String(Base64.decode(str, Base64.NO_WRAP), UTF_8);
        String newStr = base1.substring(0, base1.length() - key.length());
        String value = new String(Base64.decode(newStr, Base64.NO_WRAP), UTF_8);
        return value;
    }

}