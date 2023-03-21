package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AlxBannerUIData extends AlxBaseUIData implements Parcelable {

    public static final int DATA_TYPE_WEB = 1;
    public static final int DATA_TYPE_VIDEO = 2;

    /**
     * 数据类型: 1: web; 2: 视频
     */
    public int dataType;


    //1： baner-webview 数据
    public String html;

    //2： 全屏视频数据
    public AlxVideoVastBean video;

    //用来记录UI的状态
    private AlxNativeMediaUIStatus mVideoUIStatus;
    private AlxBannerUIStatus mUIStatus;

    public AlxBannerUIData() {

    }

    protected AlxBannerUIData(Parcel in) {
        super(in);
        dataType = in.readInt();
        html = in.readString();
        video = in.readParcelable(AlxVideoVastBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(dataType);
        parcel.writeString(html);
        parcel.writeParcelable(video, i);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxBannerUIData> CREATOR = new Creator<AlxBannerUIData>() {
        @Override
        public AlxBannerUIData createFromParcel(Parcel in) {
            return new AlxBannerUIData(in);
        }

        @Override
        public AlxBannerUIData[] newArray(int size) {
            return new AlxBannerUIData[size];
        }
    };

    public AlxNativeMediaUIStatus getVideoUIStatus() {
        return mVideoUIStatus;
    }

    public void setVideoUIStatus(AlxNativeMediaUIStatus status) {
        this.mVideoUIStatus = status;
    }

    public AlxBannerUIStatus getUIStatus() {
        return mUIStatus;
    }

    public void setUIStatus(AlxBannerUIStatus status) {
        this.mUIStatus = status;
    }
}