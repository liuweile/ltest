package com.alxad.control.vast;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlxVastWrapper implements Serializable {

    public String version;
    public String id;
    public String adSystem;
    public List<String> errorList;
    public List<String> impressionList;

    public String vastAdTagURI;
    public List<Creative> creativeList;
    public AlxBaseVastBean.Extension extension;

    public AlxVastWrapper() {
        errorList = new ArrayList<>();
        impressionList = new ArrayList<>();
        creativeList = new ArrayList<>();
    }

    //Creatives > Creative
    public static class Creative implements Serializable {
        public String id;
        public String sequence;
        public List<AlxBaseVastBean.Companion> companionList;
        public Linear linear;

        public Creative() {
            companionList = new ArrayList<>();
        }
    }

    //Creative > Linear
    public static class Linear implements Serializable {
        public AlxBaseVastBean.TrackingEvents trackingEvents;
        public AlxBaseVastBean.VideoClicks videoClicks;
    }

}