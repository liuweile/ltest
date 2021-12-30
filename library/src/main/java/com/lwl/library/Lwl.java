package com.lwl.library;

import android.text.TextUtils;
import android.util.Log;

public class Lwl {

    public static void println(String tag,String msg){
        if(TextUtils.isEmpty(tag) || TextUtils.isEmpty(msg)){
            return;
        }
        Log.i(tag,msg);
    }
}
