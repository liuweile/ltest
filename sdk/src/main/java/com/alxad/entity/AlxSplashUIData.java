package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class AlxSplashUIData extends AlxBaseUIData implements Parcelable {

    public String title;
    public String imgIcon;//图标
    public String imgBig;//大图
    public int imgBigWidth;//大图宽度
    public int imgBigHeight;//大图高度
    public String desc;
    public String link;//跳转
    /**
     * 数据扩展字段
     */
    public AlxNativeExtBean extField;

    public AlxSplashUIData() {

    }

    protected AlxSplashUIData(Parcel in) {
        super(in);
        title = in.readString();
        imgIcon = in.readString();
        imgBig = in.readString();
        imgBigWidth = in.readInt();
        imgBigHeight = in.readInt();
        desc = in.readString();
        link = in.readString();

        extField = in.readParcelable(AlxNativeExtBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(title);
        dest.writeString(imgIcon);
        dest.writeString(imgBig);
        dest.writeInt(imgBigWidth);
        dest.writeInt(imgBigHeight);
        dest.writeString(desc);
        dest.writeString(link);

        dest.writeParcelable(extField, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxSplashUIData> CREATOR = new Creator<AlxSplashUIData>() {
        @Override
        public AlxSplashUIData createFromParcel(Parcel in) {
            return new AlxSplashUIData(in);
        }

        @Override
        public AlxSplashUIData[] newArray(int size) {
            return new AlxSplashUIData[size];
        }
    };

}