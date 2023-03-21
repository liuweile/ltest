package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 原生扩展字段对象
 *
 * @author liuweile
 * @date 2022-3-30
 */
public class AlxNativeExtBean implements Parcelable {

    public AlxOmidBean omid;
    public int assetType;
    public String source;

    public AlxNativeExtBean() {

    }

    protected AlxNativeExtBean(Parcel in) {
        omid = in.readParcelable(AlxOmidBean.class.getClassLoader());
        assetType = in.readInt();
        source = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(omid, flags);
        dest.writeInt(assetType);
        dest.writeString(source);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxNativeExtBean> CREATOR = new Creator<AlxNativeExtBean>() {
        @Override
        public AlxNativeExtBean createFromParcel(Parcel in) {
            return new AlxNativeExtBean(in);
        }

        @Override
        public AlxNativeExtBean[] newArray(int size) {
            return new AlxNativeExtBean[size];
        }
    };


}