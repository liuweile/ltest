package com.alxad.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * 所有广告返回的item对象解析
 *
 * @author lwl
 * @date 2021-8-6
 */
public class AlxAdItemBean implements Parcelable {
    //字段adm 数据格式类型： 1:html; 2:json; 3: Vast xml
    public static final int ADM_TYPE_HTML = 1;
    public static final int ADM_TYPE_JSON = 2;
    public static final int ADM_TYPE_VAST = 3;

    public String crid;
    public String cid;
    public List<String> adomain;//广告主域名
    public List<String> cat;

    public String bundle;
    public int admType;//字段adm数据格式类型。具体值看上面的ADM_TYPE_* 常量
    public String adm;
    public String deeplink;
    public int width; //单位dp
    public int height; //单位dp

    public List<String> imptrackers;
    public List<String> clicktrackers;

    public double price; //出价，单位为美元
    public String nurl; //竞价成功
    public String burl; //计费

    //扩展数据
    public AlxVideoExtBean videoExt; //视频
    public AlxNativeExtBean nativeExt; //原生

    public AlxAdItemBean() {

    }

    protected AlxAdItemBean(Parcel in) {
        crid = in.readString();
        cid = in.readString();
        adomain = in.createStringArrayList();
        cat = in.createStringArrayList();

        bundle = in.readString();
        admType = in.readInt();
        adm = in.readString();
        deeplink = in.readString();
        width = in.readInt();
        height = in.readInt();

        imptrackers = in.createStringArrayList();
        clicktrackers = in.createStringArrayList();

        price = in.readDouble();
        nurl = in.readString();
        burl = in.readString();

        videoExt = in.readParcelable(AlxVideoExtBean.class.getClassLoader());
        nativeExt = in.readParcelable(AlxNativeExtBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(crid);
        dest.writeString(cid);
        dest.writeStringList(adomain);
        dest.writeStringList(cat);

        dest.writeString(bundle);
        dest.writeInt(admType);
        dest.writeString(adm);
        dest.writeString(deeplink);
        dest.writeInt(width);
        dest.writeInt(height);

        dest.writeStringList(imptrackers);
        dest.writeStringList(clicktrackers);

        dest.writeDouble(price);
        dest.writeString(nurl);
        dest.writeString(burl);

        dest.writeParcelable(videoExt, flags);
        dest.writeParcelable(nativeExt, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AlxAdItemBean> CREATOR = new Creator<AlxAdItemBean>() {
        @Override
        public AlxAdItemBean createFromParcel(Parcel in) {
            return new AlxAdItemBean(in);
        }

        @Override
        public AlxAdItemBean[] newArray(int size) {
            return new AlxAdItemBean[size];
        }
    };
}