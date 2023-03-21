package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 视频扩展字段对象
 *
 * @author liuweile
 * @date 2022-3-30
 */
public class AlxVideoExtBean implements Parcelable {

    //in.readBoolean(); SDK_INT >=29 才支持

    public int skip;//是否可以跳转（1是可跳转，0是不可跳转）
    public int skipafter;
    public int mute;//是否静音（1是静音，0是不静音）
    public int close;//是否关闭（1是关闭，0是不关闭）

    public AlxVideoExtBean() {

    }

    protected AlxVideoExtBean(Parcel in) {
        skip = in.readInt();
        skipafter = in.readInt();
        mute = in.readInt();
        close = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(skip);
        dest.writeInt(skipafter);
        dest.writeInt(mute);
        dest.writeInt(close);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxVideoExtBean> CREATOR = new Creator<AlxVideoExtBean>() {
        @Override
        public AlxVideoExtBean createFromParcel(Parcel in) {
            return new AlxVideoExtBean(in);
        }

        @Override
        public AlxVideoExtBean[] newArray(int size) {
            return new AlxVideoExtBean[size];
        }
    };

    public boolean isSkip() {
        return skip == 1;
    }

    public boolean isMute() {
        return mute == 1;
    }

    public boolean isClose() {
        return close == 1;
    }

}