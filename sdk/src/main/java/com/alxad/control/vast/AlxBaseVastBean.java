package com.alxad.control.vast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper 和  Inline 子节点共有的子类对象
 */
public class AlxBaseVastBean implements Serializable {

    //Creative > CompanionAds > Companion
    public static class Companion implements Serializable {
        public String width;
        public String height;
        public String assetWidth;
        public String assetHeight;
        public StaticResource staticResource;
        public ClickThrough companionClickThrough;
        public ClickTracking companionClickTracking;
    }

    //Creative > CompanionAds > Companion > StaticResource
    public static class StaticResource implements Serializable {
        public String creativeType;
        public String url;
    }

    //Creative > Linear > TrackingEvents
    public static class TrackingEvents implements Serializable {
        public List<Tracking> trackingList;

        public TrackingEvents() {
            trackingList = new ArrayList<>();
        }
    }

    //Creative > Linear > TrackingEvents > Tracking
    static public class Tracking implements Serializable {
        public String event;
        public String url;
        public String offset;//00:00:10
    }

    //Creative > Linear > VideoClicks
    public static class VideoClicks implements Serializable {
        public List<ClickTracking> clickTrackingList;
        public List<ClickThrough> clickThroughList;

        public VideoClicks() {
            clickTrackingList = new ArrayList<>();
            clickThroughList = new ArrayList<>();
        }
    }

    public static class ClickTracking implements Serializable {
        public String url;
    }


    public static class ClickThrough implements Serializable {
        public String url;
    }

    //Wrapper > Extension 或 InLine > Extension
    public static class Extension implements Serializable {
        public String type;
        public String vendor;
        public String apiFramework;
        public String browserOptional;//true 或 false
        public String url;
        public String parameters;
    }
}