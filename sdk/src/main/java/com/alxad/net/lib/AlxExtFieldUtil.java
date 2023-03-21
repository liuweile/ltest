package com.alxad.net.lib;

import com.alxad.entity.AlxNativeExtBean;
import com.alxad.entity.AlxOmidBean;
import com.alxad.entity.AlxVideoExtBean;

import org.json.JSONObject;

/**
 * 扩展字段统一解析工具
 *
 * @author lwl
 * @date 2022-8-8
 */
public class AlxExtFieldUtil {

    /**
     * 视频扩展字段
     *
     * @param json
     * @return
     * @throws Exception
     */
    public static AlxVideoExtBean parseVideoExt(JSONObject json) throws Exception {
        AlxVideoExtBean bean = new AlxVideoExtBean();
        boolean isSkip = json.optBoolean("skip", false);
        bean.skip = isSkip ? 1 : 0;
        bean.skipafter = json.optInt("skipafter", 0);
        boolean isMute = json.optBoolean("mute", false);
        bean.mute = isMute ? 1 : 0;
        boolean isClose = json.optBoolean("close", false);
        bean.close = isClose ? 1 : 0;
        return bean;
    }

    /**
     * 视频扩展字段
     *
     * @param json
     * @return
     * @throws Exception
     */
    public static AlxNativeExtBean parseNativeExt(JSONObject json) throws Exception {
        AlxNativeExtBean bean = new AlxNativeExtBean();
        if (json.has("omid")) {
            AlxOmidBean omidBean = new AlxOmidBean();
            JSONObject omidJson = json.getJSONObject("omid");
            omidBean.key = omidJson.optString("vendorKey");
            omidBean.url = omidJson.optString("javascriptResourceUrl");
            omidBean.params = omidJson.optString("verificationParameters");

            bean.omid = omidBean;
        }
        bean.assetType = json.optInt("asset_type");
        bean.source = json.optString("source");
        return bean;
    }

}
