package com.alxad.net.impl;

import android.text.TextUtils;

import com.alxad.api.AlxAdError;
import com.alxad.control.vast.AlxVastLoader;
import com.alxad.entity.AlxAdItemBean;
import com.alxad.entity.AlxBannerUIData;
import com.alxad.entity.AlxInterstitialUIData;
import com.alxad.control.vast.AlxVastXml;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxExtFieldUtil;
import com.alxad.net.lib.AlxResponseBean;
import com.alxad.report.AlxReportManager;

import org.json.JSONObject;

/**
 * banner
 *
 * @author liuweile
 * @date 2021-10-28
 */
public class AlxBannerTaskImpl extends AlxAdTask<AlxBannerUIData> {

    @Override
    public boolean handleData(AlxResponseBean response) throws Exception {
        if (response == null || response.adsList == null || response.adsList.isEmpty()) {
            tempErrorCode = AlxAdError.ERR_NO_FILL;
            tempErrorMsg = "error:No fill, null response!";
            return false;
        }

        AlxAdItemBean ads = response.adsList.get(0);
        if (ads == null) {
            tempErrorCode = AlxAdError.ERR_NO_FILL;
            tempErrorMsg = "error:No fill, null response!";
            return false;
        }

        mResponse = new AlxBannerUIData();
        mResponse.id = response.id;
        mResponse.bundle = ads.bundle;
        mResponse.deeplink = ads.deeplink;
        mResponse.width = ads.width;
        mResponse.height = ads.height;
        mResponse.clickTrackers = ads.clicktrackers;
        mResponse.impressTrackers = ads.imptrackers;
        mResponse.price = ads.price;
        mResponse.burl = ads.burl;
        mResponse.nurl = ads.nurl;

        switch (ads.admType) {
            case AlxAdItemBean.ADM_TYPE_VAST:
                handleVAST(mResponse, ads);
                mResponse.dataType = AlxBannerUIData.DATA_TYPE_VIDEO;
                break;
            case AlxAdItemBean.ADM_TYPE_HTML:
//                mResponse.webData = ads;
                handleHtml(mResponse, ads);
                mResponse.dataType = AlxInterstitialUIData.DATA_TYPE_BANNER;
                break;
        }

        if (mResponse.dataType == AlxInterstitialUIData.DATA_TYPE_BANNER) {
            if (TextUtils.isEmpty(mResponse.html)) {
                tempErrorCode = AlxAdError.ERR_NO_FILL;
                tempErrorMsg = "error: No fill, adm is empty!";
                return false;
            }
        } else if (mResponse.dataType == AlxInterstitialUIData.DATA_TYPE_VIDEO) {
            if (mResponse.video == null) {
                tempErrorCode = AlxAdError.ERR_NO_FILL;
                tempErrorMsg = "error: No fill";
                return false;
            }
        } else {
            tempErrorCode = AlxAdError.ERR_SERVER;
            tempErrorMsg = AlxAdError.MSG_AD_DATA_FORMAT_ERROR;//数据格式错误
            return false;
        }
        return true;
    }

    private void handleHtml(AlxBannerUIData uiData, AlxAdItemBean ads) throws Exception {
        if (ads != null) {
            String html = ads.adm;
            String newHtml = AlxReportManager.getReplaceHtml(html, uiData);
            mResponse.html = newHtml;
        }
    }

    private void handleVAST(AlxBannerUIData uiData, AlxAdItemBean ads) throws Exception {
        AlxVastXml mVastProtocol = new AlxVastXml();
        mVastProtocol.parseXML(ads.adm);

        if (mVastProtocol.getInline() == null) {
            tempErrorCode = AlxAdError.ERR_VAST_ERROR;
            tempErrorMsg = "Parse Vast Xml error";
        } else {
            AlxVastLoader vastResponse = new AlxVastLoader(mContext, ads.videoExt);
            if (!vastResponse.loadXml(ads.adm, null)) {
                tempErrorCode = vastResponse.getErrorCode();
                tempErrorMsg = vastResponse.getMsg();
                return;
            }
            uiData.video = vastResponse.getData();
        }
    }

    @Override
    protected void doAdExtendJson(AlxAdItemBean item, JSONObject itemObj) throws Exception {
        if (itemObj.has("video_ext")) {
            JSONObject jObjectVideo = itemObj.getJSONObject("video_ext");
            item.videoExt = AlxExtFieldUtil.parseVideoExt(jObjectVideo);
        }
    }

}