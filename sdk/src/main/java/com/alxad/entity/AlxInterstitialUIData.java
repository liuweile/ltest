package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 插屏广告UI数据
 */
public class AlxInterstitialUIData extends AlxBaseUIData implements Parcelable {
    public static final int DATA_TYPE_BANNER = 1;
    public static final int DATA_TYPE_VIDEO = 2;

    /**
     * 数据类型: 1: 全屏banner-webView; 2: 全屏视频
     */
    public int dataType;

    //1： banner-webview 数据
    public String html;

    //2： 全屏视频数据
    public AlxVideoVastBean video;

    public AlxInterstitialUIData() {
        super();
    }

    protected AlxInterstitialUIData(Parcel in) {
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

    public static final Creator<AlxInterstitialUIData> CREATOR = new Creator<AlxInterstitialUIData>() {
        @Override
        public AlxInterstitialUIData createFromParcel(Parcel in) {
            return new AlxInterstitialUIData(in);
        }

        @Override
        public AlxInterstitialUIData[] newArray(int size) {
            return new AlxInterstitialUIData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}