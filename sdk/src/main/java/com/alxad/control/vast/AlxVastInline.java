package com.alxad.control.vast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlxVastInline implements Serializable {

    public String version;
    public String id;
    public String adSystem;
    public List<String> errorList;
    public List<String> impressionList;

    public String adTitle;
    public String description;
    public String advertiser;
    public List<Creative> creativeList;
    public List<Extension> extensionList;
    public AlxBaseVastBean.Extension extension;


    public AlxVastInline() {
        errorList = new ArrayList<>();
        impressionList = new ArrayList<>();
        creativeList = new ArrayList<>();
        extensionList = new ArrayList<>();
    }

    //Creatives > Creative
    public static class Creative implements Serializable {
        //本对象中：线性广告，伴随广告，非线性广告 三次是互斥的，三着只会出现一个
        public String id;
        public String sequence;
        public Linear linear; //线性广告
        public List<AlxBaseVastBean.Companion> companionList; //伴随广告
        public List<NonLinear> nonLinearList; //非线性广告

        public Creative() {
            companionList = new ArrayList<>();
            nonLinearList = new ArrayList<>();
        }
    }

    //Extensions > Extension
    public static class Extension implements Serializable {

    }

    //Creatives > Creative > Linear
    public static class Linear implements Serializable {
        public String duration;
        public List<Icon> iconList;
        public List<MediaFile> mediaFileList;
        public AlxBaseVastBean.VideoClicks videoClicks;
        public AlxBaseVastBean.TrackingEvents trackingEvents;

        public Linear() {
            mediaFileList = new ArrayList<>();
            iconList = new ArrayList<>();
        }
    }

    //Creatives > Creative > Linear > MediaFiles > MediaFile
    public static class MediaFile implements Serializable {
        public String width;
        public String height;
        public String type;
        public String bitrate;
        public String delivery;
        public String scalable;
        public String maintainAspectRatio;
        public String url;

        /**
         * 判断是否支持此视频格式
         *
         * @return
         */
        public boolean isSupportVideoType() {
            if (type == null) {
                return false;
            }
            String lowerType = type.toLowerCase();
            if (lowerType.contains("mp4")
                    || lowerType.contains("3gp")
                    || lowerType.contains("webm")
                    || lowerType.contains("mkv")
                    || lowerType.contains("ts")) {
                return true;
            }
            return false;
        }
    }

    //Creatives > Creative > Linear > Icons > Icon
    public static class Icon implements Serializable {
        public String width;
        public String height;
        public String program;
        public AlxBaseVastBean.StaticResource staticResource;
        public List<AlxBaseVastBean.ClickThrough> iconClickThroughList;

        public Icon() {
            iconClickThroughList = new ArrayList<>();
        }
    }

    //Creatives > Creative > NonLinearAds > NonLinear
    public static class NonLinear implements Serializable {
        public String width;
        public String height;
        public AlxBaseVastBean.StaticResource staticResource;
        public AlxBaseVastBean.ClickThrough nonLinearClickThrough;
        public AlxBaseVastBean.ClickTracking nonLinearClickTracking;
    }

}