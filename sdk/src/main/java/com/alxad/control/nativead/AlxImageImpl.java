package com.alxad.control.nativead;

import android.os.Parcel;
import android.os.Parcelable;

import com.alxad.api.AlxImage;

public class AlxImageImpl extends AlxImage implements Parcelable {

    private String url;
    private int width;
    private int height;

    public AlxImageImpl() {

    }

    protected AlxImageImpl(Parcel in) {
        url = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxImageImpl> CREATOR = new Creator<AlxImageImpl>() {
        @Override
        public AlxImageImpl createFromParcel(Parcel in) {
            return new AlxImageImpl(in);
        }

        @Override
        public AlxImageImpl[] newArray(int size) {
            return new AlxImageImpl[size];
        }
    };

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public String getImageUrl() {
        return url;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}