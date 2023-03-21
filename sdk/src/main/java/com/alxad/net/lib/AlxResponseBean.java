package com.alxad.net.lib;

import android.os.Parcel;
import android.os.Parcelable;

import com.alxad.entity.AlxAdItemBean;

import java.util.List;

/**
 * 广告返回的解析对象数据
 *
 * @author lwl
 * @date 2021-11-1
 */
public class AlxResponseBean implements Parcelable {
    public static final int CODE_OK = 1000;

    public int code;
    public String msg;
    public int adType;

    //下面的数据是从data字段json中解析出来的
    public String id; //广告响应唯一id
    public List<AlxAdItemBean> adsList;//取所有广告列表（信息流广告 会用到）

    public AlxResponseBean() {

    }

    protected AlxResponseBean(Parcel in) {
        code = in.readInt();
        msg = in.readString();
        adType = in.readInt();

        id = in.readString();
        adsList = in.createTypedArrayList(AlxAdItemBean.CREATOR);
    }

    public static final Creator<AlxResponseBean> CREATOR = new Creator<AlxResponseBean>() {
        @Override
        public AlxResponseBean createFromParcel(Parcel in) {
            return new AlxResponseBean(in);
        }

        @Override
        public AlxResponseBean[] newArray(int size) {
            return new AlxResponseBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(code);
        parcel.writeString(msg);
        parcel.writeInt(adType);

        parcel.writeString(id);
        parcel.writeTypedList(adsList);
    }

}