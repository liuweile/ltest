package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 视频广告UI展示的数据对象
 *
 * @author lwl
 * @date 2021-8-17
 */
public class AlxVideoUIData extends AlxBaseUIData implements Parcelable {

    public AlxVideoVastBean video;

    public AlxVideoUIData() {

    }

    protected AlxVideoUIData(Parcel in) {
        super(in);
        video = in.readParcelable(AlxVideoVastBean.class.getClassLoader());
    }

    public static final Creator<AlxVideoUIData> CREATOR = new Creator<AlxVideoUIData>() {
        @Override
        public AlxVideoUIData createFromParcel(Parcel in) {
            return new AlxVideoUIData(in);
        }

        @Override
        public AlxVideoUIData[] newArray(int size) {
            return new AlxVideoUIData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(video, flags);
    }

}