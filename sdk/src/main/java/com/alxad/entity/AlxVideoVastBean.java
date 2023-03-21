package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * adm 字符串数据是 vast协议数据。此对象只包含vast xml协议的数据，不包含其他数据
 *
 * @author liuweile
 * @date 2022-3-29
 */
public class AlxVideoVastBean implements Parcelable {

    public String vastId;
    public String adSystem;//广告服务方【例如：Algorix】
    public String advertiser; //广告商
    public String adTitle;
    public String description;
    public String iconUrl;//icon网络图片url
    public int iconWidth;// icon的宽度
    public int iconHeight;// icon的高度
    public String landUrl;//落地页是图片: 网络图片url

    public String duration; //视频总时长【例如：00:00:18】
    public String videoWidth;
    public String videoHeight;
    public String videoUrl;

    /**
     * 点击事件需要跳转的url地址
     */
    public List<String> clickThroughList;

    public List<String> impressList;
    /**
     * 点击上报数据
     */
    public List<String> clickTrackingList;

    public List<String> firstQuartileList;
    public List<String> midPointList;
    public List<String> thirdQuartileList;
    public List<String> completeList;
    public List<String> startList;
    public List<String> errorList;
    public List<ProgressReportData> progressList;

    /**
     * 用户对素材静音
     */
    public List<String> muteList;

    /**
     * 用户对素材取消静音
     */
    public List<String> unmuteList;

    /**
     * 用户暂停播放
     */
    public List<String> pauseList;

    /**
     * 用户停止或暂停素材后，又重新开始播放素材
     */
    public List<String> resumeList;

    /**
     * 点击了跳过按钮
     */
    public List<String> skipList;


    /**
     * 数据扩展字段
     */
    public AlxVideoExtBean extField;

    /**
     * omid
     */
    public AlxOmidBean omid;

    public AlxVideoVastBean() {

    }

    protected AlxVideoVastBean(Parcel in) {
        vastId = in.readString();
        adSystem = in.readString();
        advertiser = in.readString();
        adTitle = in.readString();
        description = in.readString();
        iconUrl = in.readString();
        iconWidth = in.readInt();
        iconHeight = in.readInt();
        landUrl = in.readString();
        duration = in.readString();
        videoWidth = in.readString();
        videoHeight = in.readString();
        videoUrl = in.readString();
        clickThroughList = in.createStringArrayList();
        impressList = in.createStringArrayList();
        clickTrackingList = in.createStringArrayList();
        firstQuartileList = in.createStringArrayList();
        midPointList = in.createStringArrayList();
        thirdQuartileList = in.createStringArrayList();
        completeList = in.createStringArrayList();
        startList = in.createStringArrayList();
        errorList = in.createStringArrayList();
        progressList = in.createTypedArrayList(ProgressReportData.CREATOR);
        muteList = in.createStringArrayList();
        unmuteList = in.createStringArrayList();
        pauseList = in.createStringArrayList();
        resumeList = in.createStringArrayList();
        skipList = in.createStringArrayList();
        extField = in.readParcelable(AlxVideoExtBean.class.getClassLoader());
        omid = in.readParcelable(AlxOmidBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vastId);
        dest.writeString(adSystem);
        dest.writeString(advertiser);
        dest.writeString(adTitle);
        dest.writeString(description);
        dest.writeString(iconUrl);
        dest.writeInt(iconWidth);
        dest.writeInt(iconHeight);
        dest.writeString(landUrl);
        dest.writeString(duration);
        dest.writeString(videoWidth);
        dest.writeString(videoHeight);
        dest.writeString(videoUrl);
        dest.writeStringList(clickThroughList);
        dest.writeStringList(impressList);
        dest.writeStringList(clickTrackingList);
        dest.writeStringList(firstQuartileList);
        dest.writeStringList(midPointList);
        dest.writeStringList(thirdQuartileList);
        dest.writeStringList(completeList);
        dest.writeStringList(startList);
        dest.writeStringList(errorList);
        dest.writeTypedList(progressList);
        dest.writeStringList(muteList);
        dest.writeStringList(unmuteList);
        dest.writeStringList(pauseList);
        dest.writeStringList(resumeList);
        dest.writeStringList(skipList);
        dest.writeParcelable(extField, flags);
        dest.writeParcelable(omid, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxVideoVastBean> CREATOR = new Creator<AlxVideoVastBean>() {
        @Override
        public AlxVideoVastBean createFromParcel(Parcel in) {
            return new AlxVideoVastBean(in);
        }

        @Override
        public AlxVideoVastBean[] newArray(int size) {
            return new AlxVideoVastBean[size];
        }
    };

    public static class ProgressReportData implements Parcelable {

        public int offset;//播放进度s
        public List<String> urlList;

        public ProgressReportData() {

        }

        protected ProgressReportData(Parcel in) {
            offset = in.readInt();
            urlList = in.createStringArrayList();
        }


        public static final Creator<ProgressReportData> CREATOR = new Creator<ProgressReportData>() {
            @Override
            public ProgressReportData createFromParcel(Parcel in) {
                return new ProgressReportData(in);
            }

            @Override
            public ProgressReportData[] newArray(int size) {
                return new ProgressReportData[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(offset);
            dest.writeStringList(urlList);
        }
    }
}