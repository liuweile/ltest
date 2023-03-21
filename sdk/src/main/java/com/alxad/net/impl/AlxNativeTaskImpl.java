package com.alxad.net.impl;

import android.text.TextUtils;

import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.control.nativead.AlxImageImpl;
import com.alxad.control.vast.AlxVastLoader;
import com.alxad.control.vast.AlxVastXml;
import com.alxad.entity.AlxAdItemBean;
import com.alxad.entity.AlxNativeUIData;
import com.alxad.http.AlxDownloadManager;
import com.alxad.http.AlxHttpUtil;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxExtFieldUtil;
import com.alxad.net.lib.AlxResponseBean;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 原生
 *
 * @author liuweile
 * @date 2021-10-28
 */
public class AlxNativeTaskImpl extends AlxAdTask<List<AlxNativeUIData>> {
    public static final String TAG = "AlxNativeTaskImpl";
    private volatile boolean isNoSupportDataFormat = false;

    @Override
    public boolean handleData(AlxResponseBean response) throws Exception {
        isNoSupportDataFormat = false;
        if (response == null || response.adsList == null || response.adsList.size() < 1) {
            tempErrorCode = AlxAdError.ERR_NO_FILL;
            tempErrorMsg = "error:No fill, null response!";
            return false;
        }
        mResponse = new ArrayList<>();
        List<AlxAdItemBean> adsList = response.adsList;
        if (adsList.size() == 1) {//只有一条数据时，开启图片缓存下载
            AlxNativeUIData bean = handleItem(response, adsList.get(0), true);
            if (bean != null) {
                mResponse.add(bean);
            }
        } else {//如果有多条数据时，不开启图片缓存下载
            for (AlxAdItemBean item : response.adsList) {
                AlxNativeUIData bean = handleItem(response, item, false);
                if (bean != null) {
                    mResponse.add(bean);
                }
            }
        }

        if (mResponse == null || mResponse.size() < 1) {
            if (isNoSupportDataFormat) {
                tempErrorCode = AlxAdError.ERR_SERVER;
                tempErrorMsg = AlxAdError.MSG_AD_DATA_FORMAT_ERROR;//数据格式错误
            } else {
                tempErrorCode = AlxAdError.ERR_NO_FILL;
                tempErrorMsg = "error:No fill";
            }
            return false;
        }
        return true;
    }

    /**
     * 解析单个广告数据
     *
     * @param obj
     * @param isOpenCache 是否开启缓存
     * @return
     */
    private AlxNativeUIData handleItem(AlxResponseBean response, AlxAdItemBean obj, boolean isOpenCache) {
        if (obj == null || response == null) {
            return null;
        }
        try {
            if (TextUtils.isEmpty(obj.adm)) {
                return null;
            }

            AlxNativeUIData bean = new AlxNativeUIData();
            bean.id = response.id;
            bean.bundle = obj.bundle;
            bean.deeplink = obj.deeplink;
            bean.width = obj.width;
            bean.height = obj.height;
            bean.clickTrackers = obj.clicktrackers;
            bean.impressTrackers = obj.imptrackers;
            bean.price = obj.price;
            bean.burl = obj.burl;
            bean.nurl = obj.nurl;
            bean.extField = obj.nativeExt;

            if (obj.admType == AlxAdItemBean.ADM_TYPE_JSON) {
                bean.dataType = AlxNativeUIData.DATA_TYPE_JSON;
                if (!parseJson(bean, obj.adm)) {
                    return null;
                }
            } else if (obj.admType == AlxAdItemBean.ADM_TYPE_VAST) {
                if (!handleVAST(bean, obj)) {
                    return null;
                }
                bean.dataType = AlxNativeUIData.DATA_TYPE_VIDEO;
            } else {//其他数据暂时不支持
                isNoSupportDataFormat = true;
                return null;
            }
            cacheDown(bean, isOpenCache);
            return bean;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "handleItem():" + e.getMessage());
        }
        return null;
    }

    private void cacheDown(AlxNativeUIData bean, boolean isOpenCache) {
        if (bean == null) {
            return;
        }
        if (!isOpenCache) {
            return;
        }
        try {
            String imgDir = AlxFileUtil.getImageSavePath(mContext);
            String imgIcon = null;
            String imgBig = null;
            if (bean.json_imageList != null && bean.json_imageList.size() > 0) {
                AlxImageImpl image0 = bean.json_imageList.get(0);
                if (image0 != null) {
                    imgBig = image0.getImageUrl();
                }
            }
            if (bean.json_icon != null) {
                imgIcon = bean.json_icon.getImageUrl();
            }

            //下载大图
            if (!TextUtils.isEmpty(imgBig)) {
                try {
                    String fileName = AlxHttpUtil.getDownloadFileName(imgBig);
                    File file = new File(imgDir + fileName);
                    if (!file.exists()) {
                        AlxLog.d(AlxLogLevel.MARK, TAG, "imgBig-" + imgBig);
                        AlxDownloadManager.with(imgBig, imgDir).asyncRequest();
                    }
                } catch (Exception e) {
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
            }

            //下载图标
            if (!TextUtils.isEmpty(imgIcon)) {
                try {
                    String fileName = AlxHttpUtil.getDownloadFileName(imgIcon);
                    File file = new File(imgDir + fileName);
                    if (!file.exists()) {
                        AlxLog.d(AlxLogLevel.MARK, TAG, "imgIcon-" + imgIcon);
                        AlxDownloadManager.with(imgIcon, imgDir).asyncRequest();
                    }
                } catch (Exception e) {
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    public boolean parseJson(AlxNativeUIData data, String jsonStr) {
        if (data == null || TextUtils.isEmpty(jsonStr)) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);

            JSONObject fieldJson = json.optJSONObject("title");
            if (fieldJson != null) {
                data.json_title = fieldJson.optString("value");
            }

            fieldJson = json.optJSONObject("cta");
            if (fieldJson != null) {
                data.json_callToAction = fieldJson.optString("value");
            }

            fieldJson = json.optJSONObject("desc");
            if (fieldJson != null) {
                data.json_desc = fieldJson.optString("value");
            }

            fieldJson = json.optJSONObject("icon");
            if (fieldJson != null) {
                AlxImageImpl icon = new AlxImageImpl();
                icon.setUrl(fieldJson.optString("url"));
                icon.setWidth(fieldJson.optInt("width"));
                icon.setHeight(fieldJson.optInt("height"));
                data.json_icon = icon;
            }

            JSONArray fieldArray = json.optJSONArray("main");
            if (fieldArray != null && fieldArray.length() > 0) {
                List<AlxImageImpl> imageList = new ArrayList<>();
                for (int i = 0; i < fieldArray.length(); i++) {
                    JSONObject itemJson = fieldArray.getJSONObject(i);
                    AlxImageImpl item = new AlxImageImpl();
                    item.setUrl(itemJson.optString("url"));
                    item.setWidth(itemJson.optInt("width"));
                    item.setHeight(itemJson.optInt("height"));
                    imageList.add(item);
                }
                data.json_imageList = imageList;
            }

            fieldJson = json.getJSONObject("link"); //让其报错，报错代表解析格式不对，用optJSONObject不会抛出异常
            if (fieldJson != null) {
                data.json_link = fieldJson.optString("url");
            }
            return true;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
        return false;
    }

    private boolean handleVAST(AlxNativeUIData bean, AlxAdItemBean ads) {
        if (bean == null || ads == null) {
            return false;
        }
        try {
            AlxVastXml mVastProtocol = new AlxVastXml();
            mVastProtocol.parseXML(ads.adm);

            if (mVastProtocol.getInline() == null) {
                tempErrorCode = AlxAdError.ERR_VAST_ERROR;
                tempErrorMsg = "Parse Vast Xml error";
                return false;
            } else {
                AlxVastLoader vastResponse = new AlxVastLoader(mContext, ads.videoExt);
                if (!vastResponse.loadXml(ads.adm, null)) {
                    tempErrorCode = vastResponse.getErrorCode();
                    tempErrorMsg = vastResponse.getMsg();
                    return false;
                }
                bean.video = vastResponse.getData();
                return true;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
        return false;
    }

    @Override
    protected void doAdExtendJson(AlxAdItemBean item, JSONObject itemObj) throws Exception {
        if (itemObj.has("native_ext")) {
            JSONObject nativeJson = itemObj.getJSONObject("native_ext");
            item.nativeExt = AlxExtFieldUtil.parseNativeExt(nativeJson);
        }

        if (itemObj.has("video_ext")) {
            JSONObject jObjectVideo = itemObj.getJSONObject("video_ext");
            item.videoExt = AlxExtFieldUtil.parseVideoExt(jObjectVideo);
        }
    }

}