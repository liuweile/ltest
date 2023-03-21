package com.alxad.net.lib;

import android.content.Context;
import android.text.TextUtils;

import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxAdItemBean;
import com.alxad.entity.AlxTracker;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * 广告：数据解析和转换层
 *
 * @author lwl
 * @date 2021-11-1
 */
public abstract class AlxBaseLoadTask {
    protected String TAG = "AlxBaseLoadTask";

    protected Context mContext;

    /**
     * 解析广告返回的json数据
     *
     * @param request
     * @param responseJson
     */
    public void parseHttpJson(AlxRequestBean request, String responseJson) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "begin onAdLoaded");
        if (request == null) {
            onError(null, AlxAdError.ERR_PARAMS_ERROR, "request params obj is empty");
            return;
        }
        boolean isSuccess = true;
        int errorCode = 0;
        String errorMsg = "";
        AlxTracker mTracker = request.getTracker();
        AlxResponseBean responseBean = null;
        try {
            if (!TextUtils.isEmpty(responseJson)) {
                responseBean = getJsonParse(responseJson);
                responseBean.adType = request.getAdType();
                AlxLog.e(AlxLogLevel.OPEN, TAG, "server data err_no : " + responseBean.code);
                AlxLog.e(AlxLogLevel.OPEN, TAG, "server data err_msg : " + responseBean.msg);
                if (responseBean.code != AlxResponseBean.CODE_OK) {
                    isSuccess = false;
                    errorCode = AlxAdError.ERR_SERVER;
                    errorMsg = "server error: code=" + responseBean.code;
                } else {
                    if (responseBean.adsList == null || responseBean.adsList.size() < 1) {
                        isSuccess = false;
                        errorCode = AlxAdError.ERR_PARSE_AD;
                        errorMsg = "error: no ad data";
                    }
                }
            } else {
                isSuccess = false;
                errorCode = AlxAdError.ERR_NO_FILL;
                errorMsg = "error:" + "Server error, json is null!";
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            isSuccess = false;
            errorCode = AlxAdError.ERR_PARSE_AD;
            errorMsg = "Parse ad error : " + e.getMessage();
        }

        //回调方法中:开发者自己写出的bug不要加异常处理，让开发者自己处理
        if (isSuccess && responseBean != null) {
            AlxSdkData.tracker(mTracker, AlxSdkDataEvent.HTTP_AD_FILL_YES);
            onSuccess(request, responseBean);
        } else {
            AlxSdkData.tracker(mTracker, AlxSdkDataEvent.HTTP_AD_FILL_NO);
            onError(request, errorCode, errorMsg);
        }
    }

    /**
     * 解析json 数据
     *
     * @param jsonStr
     * @return
     */
    private AlxResponseBean getJsonParse(String jsonStr) {
        AlxResponseBean obj = new AlxResponseBean();
        try {
            JSONObject json = new JSONObject(jsonStr);
            obj.code = json.getInt("err_no");
            obj.msg = json.getString("err_msg");
            if (obj.code != AlxResponseBean.CODE_OK) {
                return obj;
            }
            if (!json.has("data")) {
                return obj;
            }
            JSONObject dataObj = json.getJSONObject("data");
            obj.id = dataObj.optString("id");

            JSONArray adsArray = dataObj.optJSONArray("ads");
            if (adsArray == null || adsArray.length() == 0) {
                return obj;
            }

            List<AlxAdItemBean> list = new ArrayList<>();
            for (int i = 0; i < adsArray.length(); i++) {
                AlxAdItemBean item = getAdsItemParse(adsArray.getJSONObject(i));
                if (item != null) {
                    list.add(item);
                }
            }
            obj.adsList = list;
        } catch (Exception e) {
            AlxAgent.onError(e);
            obj.code = 21;
            obj.msg = e.getMessage();
        }
        return obj;
    }

    private AlxAdItemBean getAdsItemParse(JSONObject itemObj) {
        try {
            AlxAdItemBean item = new AlxAdItemBean();
            item.admType = itemObj.optInt("adm_type");
            item.crid = itemObj.optString("crid");
            item.adomain = parseListArray(itemObj.optJSONArray("adomain"));
            item.cat = parseListArray(itemObj.optJSONArray("cat"));
            item.cid = itemObj.optString("cid");
            item.bundle = itemObj.optString("bundle");
            item.deeplink = itemObj.optString("deeplink");
            item.width = itemObj.optInt("width");
            item.height = itemObj.optInt("height");
            item.imptrackers = parseListArray(itemObj.optJSONArray("imptrackers"));
            item.clicktrackers = parseListArray(itemObj.optJSONArray("clicktrackers"));
            item.adm = itemObj.optString("adm");
            item.price = itemObj.optDouble("price", 0);
            item.nurl = itemObj.optString("nurl");
            item.burl = itemObj.optString("burl");
            doAdExtendJson(item, itemObj);
            return item;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getAdsItemParse():" + e.getMessage());
            return null;
        }
    }

    public List<String> parseListArray(JSONArray array) {
        try {
            if (array == null || array.length() < 1) {
                return null;
            }
            List<String> result = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                result.add(array.optString(i));
            }
            return result;
        } catch (Exception e) {
            AlxAgent.onError(e);
            return null;
        }
    }

    /**
     * 对不同类型广告所特有的扩展数据进行json解析
     *
     * @param item    对解析数据进行赋值的对象
     * @param itemObj 需要解析的json数据
     * @throws Exception
     */
    protected abstract void doAdExtendJson(AlxAdItemBean item, JSONObject itemObj) throws Exception;


    /**
     * 业务异常 (例如：参数校验，广告错误等)
     *
     * @param code
     * @param msg
     */
    public abstract void onError(AlxRequestBean request, int code, String msg);

    /**
     * 业务处理成功
     */
    public abstract void onSuccess(AlxRequestBean request, AlxResponseBean response);

    /**
     * 开始加载广告回调
     *
     * @param request
     */
    public abstract void onStart(AlxRequestBean request);

}