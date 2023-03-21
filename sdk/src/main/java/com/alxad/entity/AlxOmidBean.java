package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AlxOmidBean implements Parcelable {

    public String key;
    public String params;
    public String url;

    public AlxOmidBean() {

    }


    protected AlxOmidBean(Parcel in) {
        key = in.readString();
        params = in.readString();
        url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(params);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxOmidBean> CREATOR = new Creator<AlxOmidBean>() {
        @Override
        public AlxOmidBean createFromParcel(Parcel in) {
            return new AlxOmidBean(in);
        }

        @Override
        public AlxOmidBean[] newArray(int size) {
            return new AlxOmidBean[size];
        }
    };
}
