package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 广告数据追踪器，主要是为sdk上报提供数据
 *
 * @author lwl
 * @date 2021-9-28
 */
public class AlxTracker implements Parcelable {

    /**
     * 广告请求的id
     */
    private String requestId;

    /**
     * 广告位id
     */
    private String adId;

    /**
     * 广告当前请求的屏幕方向<br/>
     * 具体值请看 AlxRequestBean.SCREEN_ORIENTATION_PORTRAIT
     */
    public int screenOrientation;

    public AlxTracker(String requestId, String advertId, int screenOrientation) {
        this.requestId = requestId;
        this.adId = advertId;
        this.screenOrientation=screenOrientation;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAdId() {
        return adId;
    }

    protected AlxTracker(Parcel in) {
        requestId = in.readString();
        adId = in.readString();
        screenOrientation = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(requestId);
        dest.writeString(adId);
        dest.writeInt(screenOrientation);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxTracker> CREATOR = new Creator<AlxTracker>() {
        @Override
        public AlxTracker createFromParcel(Parcel in) {
            return new AlxTracker(in);
        }

        @Override
        public AlxTracker[] newArray(int size) {
            return new AlxTracker[size];
        }
    };

}