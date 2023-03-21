package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 广告UI数据基类
 *
 * @author lwl
 * @date 2022-2-16
 */
public abstract class AlxBaseUIData implements Parcelable {

    public String id; //后台返回id
    public String bundle; //包名
    public String deeplink;
    public int width; //单位dp
    public int height; //单位dp

    public List<String> impressTrackers; //曝光上报数据
    public List<String> clickTrackers; //点击上报数据

    public double price; //出价，单位为美元
    public String nurl; //竞价成功
    public String burl; //计费

    public AlxBaseUIData() {

    }

    protected AlxBaseUIData(Parcel in) {
        id=in.readString();
        bundle = in.readString();
        deeplink = in.readString();
        width = in.readInt();
        height = in.readInt();

        impressTrackers = in.createStringArrayList();
        clickTrackers = in.createStringArrayList();

        price = in.readDouble();
        nurl = in.readString();
        burl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(bundle);
        dest.writeString(deeplink);
        dest.writeInt(width);
        dest.writeInt(height);

        dest.writeStringList(impressTrackers);
        dest.writeStringList(clickTrackers);

        dest.writeDouble(price);
        dest.writeString(nurl);
        dest.writeString(burl);
    }

}
