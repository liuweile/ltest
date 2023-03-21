package com.alxad.control.vast;

import android.content.Context;
import android.text.TextUtils;

import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxOmidBean;
import com.alxad.entity.AlxVideoExtBean;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.http.AlxDownLoadCallback;
import com.alxad.http.AlxDownloadManager;
import com.alxad.http.AlxHttpUtil;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Vast 协议解析并转换生成一个AlxVideoVastBean 对象输出
 *
 * @author liuweile
 * @date 2022-4-28
 */
public class AlxVastLoader {
    private final String TAG = "AlxVastResponse";

    public final static String MSG_XML_ERROR = "Parse Vast xml error ";

    private Context mContext;
    private AlxVastXml mAlxVastProtocol = null;

    private int mErrorCode = 0;
    private String mMsg = null;
    private AlxVideoVastBean mVastObj = null;
    private AlxVideoExtBean mExtField;
    private AlxDownLoadCallback mVideoDownloadCallback;//视频下载回调

    public AlxVastLoader(Context context, AlxVideoExtBean extField) {
        mContext = context;
        mExtField = extField;
    }

    /**
     * 是否加载成功
     *
     * @param xml
     * @return true 解析成功；false解析失败
     */
    public boolean loadXml(String xml, AlxDownLoadCallback callback) {
        this.mVideoDownloadCallback = callback;
        if (TextUtils.isEmpty(xml)) {
            mErrorCode = AlxAdError.ERR_NO_FILL;
            mMsg = "vast is empty";
            return false;
        }

        try {
            mAlxVastProtocol = new AlxVastXml();
            mAlxVastProtocol.parseXML(xml);
            if (mAlxVastProtocol.getInline() == null) {
                mErrorCode = AlxAdError.ERR_VAST_ERROR;
                mMsg = MSG_XML_ERROR;
                return false;
            }
            mVastObj = new AlxVideoVastBean();
            mVastObj.extField = mExtField;

            handleData();
        } catch (Exception e) {
            mVastObj = null;
            mErrorCode = AlxAdError.ERR_VAST_ERROR;
            mMsg = e.getMessage();
            return false;
        }
        return mMsg == null;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getMsg() {
        return mMsg;
    }

    public AlxVideoVastBean getData() {
        return mVastObj;
    }

    /**
     * 转化数据
     */
    private void handleData() throws Exception {
        if (mAlxVastProtocol == null || mVastObj == null) {
            mErrorCode = AlxAdError.ERR_VAST_ERROR;
            mMsg = MSG_XML_ERROR + "-3";
            return;
        }

        AlxVastInline alxVastInline = mAlxVastProtocol.getInline();
        if (alxVastInline == null || alxVastInline.creativeList == null || alxVastInline.creativeList.isEmpty()) {
            mErrorCode = AlxAdError.ERR_VAST_ERROR;
            mMsg = MSG_XML_ERROR + "-4";
            return;
        }

        AlxVastInline.Linear linear = null;
        for (AlxVastInline.Creative item : alxVastInline.creativeList) {
            if (item.linear != null) {
                linear = item.linear;
                break;
            }
        }
        if (linear == null) {
            mErrorCode = AlxAdError.ERR_VAST_ERROR;
            mMsg = MSG_XML_ERROR + "-5";
            return;
        }
        mVastObj.duration = linear.duration;

        boolean isVideoFormat = false;
        AlxVastInline.MediaFile mediaFile = null;
        if (linear.mediaFileList != null && !linear.mediaFileList.isEmpty()) {
            mediaFile = linear.mediaFileList.get(0);
            for (AlxVastInline.MediaFile mf : linear.mediaFileList) {
                if (mf.isSupportVideoType()) {
                    isVideoFormat = true;
                    mediaFile = mf;
                    break;
                }
            }
        }

        if (mediaFile == null) {
            mErrorCode = AlxAdError.ERR_VIDEO_URL_EMPTY;
            mMsg = "This video url is empty";
            return;
        }

        if (!isVideoFormat) {
            mErrorCode = AlxAdError.ERR_VIDEO_TYPE_NO_SUPPORT;
            mMsg = "This video format is not supported,Parse Vast Xml error";
            return;
        }

        mVastObj.vastId = alxVastInline.id;
        mVastObj.adSystem = alxVastInline.adSystem;
        mVastObj.advertiser = alxVastInline.advertiser;
        mVastObj.adTitle = alxVastInline.adTitle;
        mVastObj.description = alxVastInline.description;
        mVastObj.videoWidth = mediaFile.width;
        mVastObj.videoHeight = mediaFile.height;
        mVastObj.videoUrl = mediaFile.url;
        AlxVastLoader.AdImageSource imageSource = getIconAndLand(alxVastInline);
        if (imageSource != null) {
            mVastObj.iconUrl = imageSource.getIconUrl();
            mVastObj.iconWidth = imageSource.getIconWidth();
            mVastObj.iconHeight = imageSource.getIconHeight();
            mVastObj.landUrl = imageSource.getLandUrl();
        }

        asyncFileDownload(alxVastInline);

        List<String> impressList = new ArrayList<>();
        List<String> clickTrackingList = new ArrayList<>();
        List<String> clickThroughList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();

        if (alxVastInline.impressionList != null && !alxVastInline.impressionList.isEmpty()) {
            impressList.addAll(alxVastInline.impressionList);
        }
        if (alxVastInline.errorList != null && !alxVastInline.errorList.isEmpty()) {
            errorList.addAll(alxVastInline.errorList);
        }

        if (linear.videoClicks != null) {
            if (linear.videoClicks.clickTrackingList != null && !linear.videoClicks.clickTrackingList.isEmpty()) {
                for (AlxBaseVastBean.ClickTracking url : linear.videoClicks.clickTrackingList) {
                    clickTrackingList.add(url.url);
                }
            }

            if (linear.videoClicks.clickThroughList != null && !linear.videoClicks.clickThroughList.isEmpty()) {
                for (AlxBaseVastBean.ClickThrough url : linear.videoClicks.clickThroughList) {
                    clickThroughList.add(url.url);
                }
            }
        }

        AlxBaseVastBean.TrackingEvents trackingEvents = linear.trackingEvents;
        if (trackingEvents != null && trackingEvents.trackingList != null && !trackingEvents.trackingList.isEmpty()) {
            for (AlxBaseVastBean.Tracking tracking : trackingEvents.trackingList) {
                fillReportList(tracking.event, tracking.url, tracking.offset, mVastObj);
            }
        }

        List<AlxVastWrapper> wrapperList = mAlxVastProtocol.getWrapperList();
        if (wrapperList != null && !wrapperList.isEmpty()) {
            for (AlxVastWrapper wrapper : wrapperList) {
                if (wrapper == null) {
                    continue;
                }

                if (wrapper.impressionList != null && !wrapper.impressionList.isEmpty()) {
                    impressList.addAll(wrapper.impressionList);
                }
                if (wrapper.errorList != null && !wrapper.errorList.isEmpty()) {
                    errorList.addAll(wrapper.errorList);
                }

                if (wrapper.creativeList != null && !wrapper.creativeList.isEmpty()) {
                    AlxVastWrapper.Linear wrapLinear = wrapper.creativeList.get(0).linear;
                    if (wrapLinear != null) {
                        if (wrapLinear.videoClicks != null && wrapLinear.videoClicks.clickTrackingList != null && !wrapLinear.videoClicks.clickTrackingList.isEmpty()) {
                            for (AlxBaseVastBean.ClickTracking clickTracking : wrapLinear.videoClicks.clickTrackingList) {
                                clickTrackingList.add(clickTracking.url);
                            }
                        }

                        AlxBaseVastBean.TrackingEvents wrapTrackingEvents = wrapLinear.trackingEvents;
                        if (wrapTrackingEvents != null && wrapTrackingEvents.trackingList != null && !wrapTrackingEvents.trackingList.isEmpty()) {
                            for (AlxBaseVastBean.Tracking tracking : wrapTrackingEvents.trackingList) {
                                fillReportList(tracking.event, tracking.url, tracking.offset, mVastObj);
                            }
                        }
                    }
                }
            }
        }

        mVastObj.impressList = impressList;
        mVastObj.clickTrackingList = clickTrackingList;
        mVastObj.clickThroughList = clickThroughList;
        mVastObj.errorList = errorList;
        mVastObj.omid = getOmid();
    }

    /**
     * 获取 icon,落地页图片,落地页html等数据
     *
     * @param alxVastInline
     * @return 返回数组，数组的内容顺序是： icon,staticResource
     */
    private AdImageSource getIconAndLand(AlxVastInline alxVastInline) {
        if (alxVastInline == null) {
            return null;
        }

        List<AlxVastInline.Creative> list = alxVastInline.creativeList;
        if (list == null || list.isEmpty()) {
            return null;
        }

        String iconUrl = null;
        String iconWidth = null;
        String iconHeight = null;
        String staticResource = null;
        String noLinearStaticResource = null;//非线性广告中的图片

        for (AlxVastInline.Creative item : list) {

            /**
             * 从Icons中取 icon 【icon中的width=height 才算成功】
             */
            if (TextUtils.isEmpty(iconUrl)) {
                if (item.linear != null) {
                    List<AlxVastInline.Icon> iconList = item.linear.iconList;
                    if (iconList != null && !iconList.isEmpty()) {
                        for (AlxVastInline.Icon iconItem : iconList) {
                            if (iconItem == null || iconItem.staticResource == null) {
                                continue;
                            }
                            if (iconItem.width == null || !iconItem.width.equals(iconItem.height)) {
                                continue;
                            }
                            AlxBaseVastBean.StaticResource staticResourceItem = iconItem.staticResource;
                            if (staticResourceItem != null && !TextUtils.isEmpty(staticResourceItem.url)) {
                                iconUrl = staticResourceItem.url;
                                iconWidth = iconItem.width;
                                iconHeight = iconItem.height;
                                break;
                            }
                        }
                    }
                }
            }

            /**
             * 从伴随广告中取 staticResource
             */
            if (TextUtils.isEmpty(staticResource)) {
                if (item.companionList != null && !item.companionList.isEmpty()) {
                    List<AlxBaseVastBean.Companion> itemList = item.companionList;

                    for (AlxBaseVastBean.Companion item2 : itemList) {
                        AlxBaseVastBean.StaticResource staticResourceItem = item2.staticResource;

                        if (TextUtils.isEmpty(staticResource)) {
                            if (staticResourceItem != null && !TextUtils.isEmpty(staticResourceItem.url)) {
                                staticResource = staticResourceItem.url;
                                break;
                            }
                        }
                    }
                }
            }

            /**
             * 从非线性广告中取 staticResource
             */
            if (TextUtils.isEmpty(noLinearStaticResource)) {
                if (item.nonLinearList != null && !item.nonLinearList.isEmpty()) {
                    List<AlxVastInline.NonLinear> itemList = item.nonLinearList;

                    for (AlxVastInline.NonLinear item2 : itemList) {
                        if (TextUtils.isEmpty(noLinearStaticResource)) {
                            AlxBaseVastBean.StaticResource resource = item2.staticResource;

                            if (resource != null && !TextUtils.isEmpty(resource.url)) {
                                noLinearStaticResource = resource.url;
                                break;
                            }
                        }
                    }
                }
            }

        }

        //如果staticResource 为空，就取非线性广告中的staticResource 图片
        if (TextUtils.isEmpty(staticResource) && !TextUtils.isEmpty(noLinearStaticResource)) {
            staticResource = noLinearStaticResource;
        }

        AdImageSource result = new AdImageSource();
        result.setIconUrl(iconUrl);
        result.setIconWidth(AlxUtil.getInt(iconWidth));
        result.setIconHeight(AlxUtil.getInt(iconHeight));
        result.setLandUrl(staticResource);
        return result;
    }

    /**
     * 填充赋值数据
     *
     * @param event
     * @param url
     * @param data
     */
    private void fillReportList(String event, String url, String offset, AlxVideoVastBean data) {
        if (event == null || data == null) {
            return;
        }
        switch (event) {
            case "start":
                if (data.startList == null) {
                    data.startList = new ArrayList<>();
                }
                data.startList.add(url);
                break;
            case "firstQuartile":
                if (data.firstQuartileList == null) {
                    data.firstQuartileList = new ArrayList<>();
                }
                data.firstQuartileList.add(url);
                break;
            case "midpoint":
                if (data.midPointList == null) {
                    data.midPointList = new ArrayList<>();
                }
                data.midPointList.add(url);
                break;
            case "thirdQuartile":
                if (data.thirdQuartileList == null) {
                    data.thirdQuartileList = new ArrayList<>();
                }
                data.thirdQuartileList.add(url);
                break;
            case "complete":
                if (data.completeList == null) {
                    data.completeList = new ArrayList<>();
                }
                data.completeList.add(url);
                break;
            case "progress":
                fillProgressReport(url, offset, data);
                break;
            case "mute":
                if (data.muteList == null) {
                    data.muteList = new ArrayList<>();
                }
                data.muteList.add(url);
                break;
            case "unmute":
                if (data.unmuteList == null) {
                    data.unmuteList = new ArrayList<>();
                }
                data.unmuteList.add(url);
                break;
            case "pause":
                if (data.pauseList == null) {
                    data.pauseList = new ArrayList<>();
                }
                data.pauseList.add(url);
                break;
            case "resume":
                if (data.resumeList == null) {
                    data.resumeList = new ArrayList<>();
                }
                data.resumeList.add(url);
                break;
            case "skip":
                if (data.skipList == null) {
                    data.skipList = new ArrayList<>();
                }
                data.skipList.add(url);
                break;
        }
    }


    private void fillProgressReport(String url, String offset, AlxVideoVastBean data) {
        if (TextUtils.isEmpty(offset)) {
            return;
        }
        int second = AlxUtil.transformSecond(offset);
        if (second < 0) {
            return;
        }
        if (data.progressList == null) {
            data.progressList = new ArrayList<>();
        }

        List<AlxVideoVastBean.ProgressReportData> list = data.progressList;
        AlxVideoVastBean.ProgressReportData bean = null;
        boolean isNewValue = true;
        if (!list.isEmpty()) {
            for (AlxVideoVastBean.ProgressReportData item : list) {
                if (item.offset == second) {
                    bean = item;
                    isNewValue = false;
                    break;
                }
            }
        }

        if (bean == null) {
            bean = new AlxVideoVastBean.ProgressReportData();
            bean.offset = second;
        }
        if (bean.urlList == null) {
            bean.urlList = new ArrayList<>();
        }
        bean.urlList.add(url);
        if (isNewValue) {
            data.progressList.add(bean);
        }
    }

    /**
     * 视频，图片 缓存 异步下载
     */
    private void asyncFileDownload(AlxVastInline alxVastInline) {
        if (mVastObj == null || TextUtils.isEmpty(mVastObj.videoUrl)) {//视频地址优先
            return;
        }
        try {
            String imgDir = AlxFileUtil.getImageSavePath(mContext);
            String videoDir = AlxFileUtil.getVideoSavePath(mContext);

            //视频地址
            if (!TextUtils.isEmpty(mVastObj.videoUrl)) {
                String videoFileName = AlxHttpUtil.getDownloadFileName(mVastObj.videoUrl);
                File videoFile = new File(videoDir + videoFileName);
                if (!videoFile.exists()) {
                    AlxDownloadManager.with(mVastObj.videoUrl, videoDir).fileName(videoFileName).asyncRequest(mVideoDownloadCallback);
                }
            }

            //落地页图片
            if (!TextUtils.isEmpty(mVastObj.landUrl)) {
                String landFileName = AlxHttpUtil.getDownloadFileName(mVastObj.landUrl);
                File landFile = new File(imgDir + landFileName);
                if (!landFile.exists()) {
                    AlxDownloadManager.with(mVastObj.landUrl, imgDir).asyncRequest();
                }
            }

            //icon 图标
            if (!TextUtils.isEmpty(mVastObj.iconUrl)) {
                String iconFileName = AlxHttpUtil.getDownloadFileName(mVastObj.iconUrl);
                File iconFile = new File(imgDir + iconFileName);
                if (!iconFile.exists()) {
                    AlxDownloadManager.with(mVastObj.iconUrl, imgDir).asyncRequest();
                }
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "asyncFileDownload():" + e.getMessage());
        }
    }

    /**
     * 获取omid配置信息
     *
     * @return
     */
    public AlxOmidBean getOmid() {
        try {
            AlxOmidBean bean = null;

            AlxVastInline inline = mAlxVastProtocol.getInline();
            if (inline != null && inline.extension != null) {
                AlxBaseVastBean.Extension extension = inline.extension;
                if (!TextUtils.isEmpty(extension.url) && !TextUtils.isEmpty(extension.vendor) && !TextUtils.isEmpty(extension.parameters)) {
                    bean = new AlxOmidBean();
                    bean.url = extension.url;
                    bean.key = extension.vendor;
                    bean.params = extension.parameters;
                }
            }
            if (bean == null) {
                List<AlxVastWrapper> wrapperList = mAlxVastProtocol.getWrapperList();
                if (wrapperList != null && !wrapperList.isEmpty()) {
                    for (AlxVastWrapper wrapper : wrapperList) {
                        if (wrapper == null || wrapper.extension == null) {
                            continue;
                        }
                        AlxBaseVastBean.Extension extension = wrapper.extension;
                        if (!TextUtils.isEmpty(extension.url) && !TextUtils.isEmpty(extension.vendor) && !TextUtils.isEmpty(extension.parameters)) {
                            bean = new AlxOmidBean();
                            bean.url = extension.url;
                            bean.key = extension.vendor;
                            bean.params = extension.parameters;
                            break;
                        }
                    }
                }
            }
            return bean;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getOmid():" + e.getMessage());
        }
        return null;
    }

    private static class AdImageSource {
        private String iconUrl;
        private int iconWidth;
        private int iconHeight;
        private String landUrl;

        public String getIconUrl() {
            return iconUrl;
        }

        public void setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
        }

        public int getIconWidth() {
            return iconWidth;
        }

        public void setIconWidth(int iconWidth) {
            this.iconWidth = iconWidth;
        }

        public int getIconHeight() {
            return iconHeight;
        }

        public void setIconHeight(int iconHeight) {
            this.iconHeight = iconHeight;
        }

        public String getLandUrl() {
            return landUrl;
        }

        public void setLandUrl(String landUrl) {
            this.landUrl = landUrl;
        }
    }

}