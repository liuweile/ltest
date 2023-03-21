package com.alxad.util;

import android.content.Context;
import android.content.SharedPreferences;

public class AlxPreferences {
    public static final String ALX_PREFERENCES_NAME = "ALX-AD";

    public interface Key {
        //String型数据
        String S_GAID = "GAID";
        String S_OAID = "OAID";


        //long型数据
        String L_AUTO_CLICK_TIME = "AUTO_CLICK_TIME";

        //boolean型数据


    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(ALX_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void putString(Context context, String key, String value) {
        if (context == null) {
            return;
        }
        getPreferences(context).edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key) {
        if (context == null) {
            return "";
        }
        return getPreferences(context).getString(key, "");
    }

    public static void putInt(Context context, String key, int value) {
        if (context == null) {
            return;
        }
        getPreferences(context).edit().putInt(key, value).commit();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        if (context == null) {
            return -100;
        }
        return getPreferences(context).getInt(key, defaultValue);
    }

    public static void putLong(Context context, String key, long value) {
        if (context == null) {
            return;
        }
        getPreferences(context).edit().putLong(key, value).commit();
    }

    public static long getLong(Context context, String key, long defaultValue) {
        if (context == null) {
            return -100L;
        }
        return getPreferences(context).getLong(key, defaultValue);
    }

}