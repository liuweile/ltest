package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.alxad.control.nativead.AlxImageImpl;

import java.util.List;

/**
 * 信息流广告-展示的数据
 *
 * @author lwl
 * @date 2022-8-5
 */
public class AlxNativeUIData extends AlxBaseUIData implements Parcelable {

    //数据类型：
    public static final int DATA_TYPE_JSON = 1; //json
    public static final int DATA_TYPE_VIDEO = 2;//vast

    //自定义字段-start-------

    //数据类型
    public int dataType;

    //自定义字段-end-------

    //1： json类型数据
    public String json_title;
    public String json_desc;
    public String json_callToAction;
    public String json_link;//跳转
    public AlxImageImpl json_icon;
    public List<AlxImageImpl> json_imageList;

    //2： vast视频数据
    public AlxVideoVastBean video;

    /**
     * 数据扩展字段
     */
    public AlxNativeExtBean extField;

    public AlxNativeUIData() {

    }

    protected AlxNativeUIData(Parcel in) {
        super(in);
        dataType = in.readInt();

        json_title = in.readString();
        json_desc = in.readString();
        json_callToAction = in.readString();
        json_link = in.readString();
        json_icon = in.readParcelable(AlxImageImpl.class.getClassLoader());
        json_imageList = in.createTypedArrayList(AlxImageImpl.CREATOR);

        video = in.readParcelable(AlxVideoVastBean.class.getClassLoader());
        extField = in.readParcelable(AlxNativeExtBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(dataType);

        dest.writeString(json_title);
        dest.writeString(json_desc);
        dest.writeString(json_callToAction);
        dest.writeString(json_link);
        dest.writeParcelable(json_icon, flags);
        dest.writeTypedList(json_imageList);

        dest.writeParcelable(video, flags);
        dest.writeParcelable(extField, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxNativeUIData> CREATOR = new Creator<AlxNativeUIData>() {
        @Override
        public AlxNativeUIData createFromParcel(Parcel in) {
            return new AlxNativeUIData(in);
        }

        @Override
        public AlxNativeUIData[] newArray(int size) {
            return new AlxNativeUIData[size];
        }
    };
}