package com.alxad.net.lib;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.alxad.base.AlxAdBase;
import com.alxad.entity.AlxTracker;

/**
 * 广告请求数据对象
 *
 * @author lwl
 * @date 2021-11-1
 */
public class AlxRequestBean implements Parcelable {

    //广告当前请求的屏幕方向： 0: 未知; 1: 竖屏; 2: 横屏
    public static final int SCREEN_ORIENTATION_UNKNOW = 0;
    public static final int SCREEN_ORIENTATION_PORTRAIT = 1;
    public static final int SCREEN_ORIENTATION_LANDSCAPE = 2;

    //广告类型
    public static final int AD_TYPE_BANNER = 1;
    public static final int AD_TYPE_INTERSTITIAL = 3;
    public static final int AD_TYPE_REWARD = 4;
    public static final int AD_TYPE_NATIVE = 5;
    public static final int AD_TYPE_SPLASH = 10;

    /**
     * 请求广告参数
     */
    private String mRequestParams;

    /**
     * 请求广告类型
     */
    private int mAdType;

    /**
     * 广告位id
     */
    private String mAdId;

    /**
     * 广告请求的唯一id
     */
    private String mRequestId;

    /**
     * 请求时当前的屏幕方向
     */
    private int mScreenOrientation;

    /**
     * 广告请求的超时时间
     */
    private long mRequestTimeout;

    public AlxRequestBean(String adId, int adType) {
        mAdId = adId;
        mAdType = adType;
        mRequestId = AlxRequestTool.createRequestId();
    }

    /**
     * 加载广告时初始化数据，主要是为了创建对象时不要带Context参数
     */
    public void loadAdInit(Context context) {
        AlxAdBase.initGlobalParam(context);
        mScreenOrientation = AlxRequestTool.getScreenOrientation(context);
        mRequestParams = AlxRequestTool.getJsonStr(context, mAdId, mRequestId, mAdType, mScreenOrientation);
    }

    protected AlxRequestBean(Parcel in) {
        mRequestParams = in.readString();
        mAdType = in.readInt();
        mAdId = in.readString();
        mRequestId = in.readString();
        mScreenOrientation = in.readInt();
        mRequestTimeout = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mRequestParams);
        dest.writeInt(mAdType);
        dest.writeString(mAdId);
        dest.writeString(mRequestId);
        dest.writeInt(mScreenOrientation);
        dest.writeLong(mRequestTimeout);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxRequestBean> CREATOR = new Creator<AlxRequestBean>() {
        @Override
        public AlxRequestBean createFromParcel(Parcel in) {
            return new AlxRequestBean(in);
        }

        @Override
        public AlxRequestBean[] newArray(int size) {
            return new AlxRequestBean[size];
        }
    };

    public String getRequestParams() {
        return mRequestParams;
    }

    public int getAdType() {
        return mAdType;
    }

    public String getAdId() {
        return mAdId;
    }

    public String getRequestId() {
        return mRequestId;
    }

    public int getScreenOrientation() {
        return mScreenOrientation;
    }

    public long getRequestTimeout() {
        return mRequestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.mRequestTimeout = requestTimeout;
    }

    public AlxTracker getTracker() {
        return new AlxTracker(mRequestId, mAdId, mScreenOrientation);
    }

}