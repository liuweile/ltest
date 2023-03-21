package com.alxad.omsdk;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.alxad.R;
import com.iab.omid.library.algorixco.ScriptInjector;

import java.io.InputStream;

/**
 * OmidJsLoader - utility for loading the Omid JavaScript resource
 */

public final class OmidJsLoader {

    /**
     * getOmidJs - gets the Omid JS resource as a string
     *
     * @param context - used to access the JS resource
     * @return - the Omid JS resource as a string
     */
    public static String getOmidJs(Context context) {
        try {
            Resources res = context.getResources();
            InputStream inputStream = res.openRawResource(R.raw.alx_omsdk_v1);
            byte[] b = new byte[inputStream.available()];
            final int bytesRead = inputStream.read(b);
            return new String(b, 0, bytesRead, "UTF-8");
        } catch (Exception e) {
            throw new UnsupportedOperationException("Yikes, omid resource not found", e);
        }
    }


    /**
     * 将om sdk js库注入到html代码中
     *
     * @param context
     * @param html
     * @return
     */
    public static String addOmJsIntoHtml(Context context, String html) {
        if (context == null || TextUtils.isEmpty(html)) {
            return html;
        }
        try {
            String htmlString = ScriptInjector.injectScriptContentIntoHtml(OmidJsLoader.getOmidJs(context), html);
            return htmlString;
        } catch (Exception e) {
            return html;
        }
    }
}
