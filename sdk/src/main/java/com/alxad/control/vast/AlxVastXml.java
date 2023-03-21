package com.alxad.control.vast;

import android.text.TextUtils;

import com.alxad.base.AlxLogLevel;
import com.alxad.http.AlxHttpManager;
import com.alxad.http.AlxHttpResponse;
import com.alxad.util.AlxLog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对vast 协议进行解析，具体文档请看项目文件README.md中的文档
 *
 * @author lwl
 * @date 2021-9-8
 */
public class AlxVastXml {
    private final String TAG = "AlxVastXml";

    private List<AlxVastWrapper> mWrapperList;
    private AlxVastInline mInline;

    /**
     * xml解析：在非UI线程中执行
     *
     * @param xml
     */
    public void parseXML(String xml) {
        if (TextUtils.isEmpty(xml)) {
            return;
        }
        AlxLog.i(AlxLogLevel.DATA, TAG, xml);
        if (isHaveTag("InLine", xml)) {
            parseInline(xml);
        } else if (isHaveTag("Wrapper", xml)) {
            if (mWrapperList == null) {
                mWrapperList = new ArrayList<>();
            }
            try {
                AlxVastWrapper wrapper = parseWrapper(xml);
                if (wrapper == null || TextUtils.isEmpty(wrapper.vastAdTagURI)) {
                    return;
                }
                AlxHttpResponse response = AlxHttpManager.getInstance().simpleGetSync(wrapper.vastAdTagURI);
                if (response == null || !response.isOk()) {
                    return;
                }
                String newVastXml = response.getResponseMsg();
                if (!TextUtils.isEmpty(newVastXml)) {
                    parseXML(newVastXml);
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            }
        }
    }

    public List<AlxVastWrapper> getWrapperList() {
        return mWrapperList;
    }

    public AlxVastInline getInline() {
        return mInline;
    }

    /**
     * 判断xml中是否含有指定的标签名
     *
     * @param tagName xml标签名
     * @param str
     * @return
     */
    private boolean isHaveTag(String tagName, String str) {
        String regex = "<(\\s*)" + tagName + "[(\\s*)|>]";
        try {
            Pattern p = Pattern.compile(regex);
            Matcher matcher = p.matcher(str);
            return matcher.find();
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "isHaveTag():" + e.getMessage());
        }
        return false;
    }

    private AlxVastWrapper parseWrapper(String strXml) {
        AlxVastWrapper wrapper = null;
        InputStream is = null;
        try {
            String charsetName = "UTF-8";
            XmlPullParser xml = XmlPullParserFactory.newInstance().newPullParser();

            is = new ByteArrayInputStream(strXml.getBytes(charsetName));
            xml.setInput(is, charsetName);
            int eventType = xml.getEventType();

            AlxVastWrapper.Creative creative = null;
            AlxBaseVastBean.Companion companion = null;
            AlxVastWrapper.Linear linear = null;
            AlxBaseVastBean.TrackingEvents trackingEvents = null;
            AlxBaseVastBean.VideoClicks videoClicks = null;
            AlxBaseVastBean.Extension extension = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        wrapper = new AlxVastWrapper();
                        break;
                    case XmlPullParser.START_TAG:
                        if ("VAST".equals(xml.getName())) {
                            wrapper.version = xml.getAttributeValue(xml.getNamespace(), "version");
                        } else if ("Ad".equals(xml.getName())) {
                            wrapper.id = xml.getAttributeValue(xml.getNamespace(), "id");
                        } else if ("AdSystem".equals(xml.getName())) {
                            wrapper.adSystem = xml.nextText();
                        } else if ("VASTAdTagURI".equals(xml.getName())) {
                            wrapper.vastAdTagURI = xml.nextText();
                        } else if ("Error".equals(xml.getName())) {
                            wrapper.errorList.add(xml.nextText());
                        } else if ("Impression".equals(xml.getName())) {
                            wrapper.impressionList.add(xml.nextText());
                        } else if ("Creative".equals(xml.getName())) {
                            creative = new AlxVastWrapper.Creative();
                            creative.id = xml.getAttributeValue(xml.getNamespace(), "id");
                            creative.sequence = xml.getAttributeValue(xml.getNamespace(), "sequence");
                        } else if ("Companion".equals(xml.getName())) {
                            companion = new AlxBaseVastBean.Companion();
                            companion.width = xml.getAttributeValue(xml.getNamespace(), "width");
                            companion.height = xml.getAttributeValue(xml.getNamespace(), "height");
                            companion.assetWidth = xml.getAttributeValue(xml.getNamespace(), "assetWidth");
                            companion.assetHeight = xml.getAttributeValue(xml.getNamespace(), "assetHeight");
                        } else if ("StaticResource".equals(xml.getName())) {
                            AlxBaseVastBean.StaticResource staticResource = new AlxBaseVastBean.StaticResource();
                            staticResource.creativeType = xml.getAttributeValue(xml.getNamespace(), "creativeType");
                            staticResource.url = xml.nextText();
                            if (companion != null) {
                                companion.staticResource = staticResource;
                            }
                        } else if ("CompanionClickThrough".equals(xml.getName())) {
                            AlxBaseVastBean.ClickThrough companionClickThrough = new AlxBaseVastBean.ClickThrough();
                            companionClickThrough.url = xml.nextText();
                            if (companion != null) {
                                companion.companionClickThrough = companionClickThrough;
                            }
                        } else if ("CompanionClickTracking".equals(xml.getName())) {
                            AlxBaseVastBean.ClickTracking companionClickTracking = new AlxBaseVastBean.ClickTracking();
//                            companionClickTracking.id = xml.getAttributeValue(xml.getNamespace(), "id");
                            companionClickTracking.url = xml.nextText();
                            if (companion != null) {
                                companion.companionClickTracking = companionClickTracking;
                            }
                        } else if ("Linear".equals(xml.getName())) {
                            linear = new AlxVastWrapper.Linear();
                        } else if ("TrackingEvents".equals(xml.getName())) {
                            trackingEvents = new AlxBaseVastBean.TrackingEvents();
                        } else if ("VideoClicks".equals(xml.getName())) {
                            videoClicks = new AlxBaseVastBean.VideoClicks();
                        } else if ("Tracking".equals(xml.getName())) {
                            AlxBaseVastBean.Tracking tracking = new AlxBaseVastBean.Tracking();
                            tracking.event = xml.getAttributeValue(xml.getNamespace(), "event");
                            tracking.offset = xml.getAttributeValue(xml.getNamespace(), "offset");
                            tracking.url = xml.nextText();
                            if (trackingEvents != null) {
                                trackingEvents.trackingList.add(tracking);
                            }
                        } else if ("ClickTracking".equals(xml.getName())) {
                            AlxBaseVastBean.ClickTracking clickTracking = new AlxBaseVastBean.ClickTracking();
                            clickTracking.url = xml.nextText();
                            if (videoClicks != null) {
                                videoClicks.clickTrackingList.add(clickTracking);
                            }
                        } else if ("ClickThrough".equals(xml.getName())) {
                            AlxBaseVastBean.ClickThrough clickThrough = new AlxBaseVastBean.ClickThrough();
                            clickThrough.url = xml.nextText();
                            if (videoClicks != null) {
                                videoClicks.clickThroughList.add(clickThrough);
                            }
                        } else if ("Extension".equals(xml.getName())) {
                            extension = new AlxBaseVastBean.Extension();
                            extension.type = xml.getAttributeValue(xml.getNamespace(), "type");
                        } else if ("Verification".equals(xml.getName())) {
                            if (extension != null) {
                                extension.vendor = xml.getAttributeValue(xml.getNamespace(), "vendor");
                            }
                        } else if ("JavaScriptResource".equals(xml.getName())) {
                            if (extension != null) {
                                extension.apiFramework = xml.getAttributeValue(xml.getNamespace(), "apiFramework");
                                extension.browserOptional = xml.getAttributeValue(xml.getNamespace(), "browserOptional");
                                extension.url = xml.nextText();
                            }
                        } else if ("VerificationParameters".equals(xml.getName())) {
                            if (extension != null) {
                                extension.parameters = xml.nextText();
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("Linear".equals(xml.getName())) {
                            if (linear != null) {
                                if (creative != null) {
                                    creative.linear = linear;
                                }
                                linear = null;
                            }
                        } else if ("TrackingEvents".equals(xml.getName())) {
                            if (trackingEvents != null) {
                                if (linear != null) {
                                    linear.trackingEvents = trackingEvents;
                                }
                                trackingEvents = null;
                            }
                        } else if ("VideoClicks".equals(xml.getName())) {
                            if (videoClicks != null) {
                                if (linear != null) {
                                    linear.videoClicks = videoClicks;
                                }
                                videoClicks = null;
                            }
                        } else if ("Companion".equals(xml.getName())) {
                            if (companion != null) {
                                if (creative != null) {
                                    creative.companionList.add(companion);
                                }
                                companion = null;
                            }
                        } else if ("Creative".equals(xml.getName())) {
                            if (creative != null) {
                                if (wrapper != null) {
                                    wrapper.creativeList.add(creative);
                                }
                                creative = null;
                            }
                        } else if ("VAST".equals(xml.getName())) {
                            if (wrapper != null) {
                                mWrapperList.add(wrapper);
                            }
                        } else if ("Extension".equals(xml.getName())) {
                            if (extension != null) {
                                if (wrapper != null) {
                                    wrapper.extension = extension;
                                }
                                extension = null;
                            }
                        }
                        break;
                }
                eventType = xml.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "Wrapper-error1:" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "Wrapper-error2:" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "Wrapper-error3:" + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return wrapper;
    }

    private void parseInline(String strXml) {
        InputStream is = null;
        try {
            String charsetName = "UTF-8";
            XmlPullParser xml = XmlPullParserFactory.newInstance().newPullParser();

            is = new ByteArrayInputStream(strXml.getBytes(charsetName));
            xml.setInput(is, charsetName);
            int eventType = xml.getEventType();

            AlxVastInline inline = null;
            AlxVastInline.Creative creative = null;
            AlxVastInline.Linear linear = null;
            AlxBaseVastBean.Companion companion = null;
            AlxVastInline.NonLinear nonLinear = null;
            AlxVastInline.Icon icon = null;
            AlxBaseVastBean.Extension extension = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        inline = new AlxVastInline();
                        break;
                    case XmlPullParser.START_TAG:
                        if ("VAST".equals(xml.getName())) {
                            inline.version = xml.getAttributeValue(xml.getNamespace(), "version");
                        } else if ("Ad".equals(xml.getName())) {
                            inline.id = xml.getAttributeValue(xml.getNamespace(), "id");
                        } else if ("AdSystem".equals(xml.getName())) {
                            inline.adSystem = xml.nextText();
                        } else if ("Error".equals(xml.getName())) {
                            inline.errorList.add(xml.nextText());
                        } else if ("Impression".equals(xml.getName())) {
                            inline.impressionList.add(xml.nextText());
                        } else if ("AdTitle".equals(xml.getName())) {
                            inline.adTitle = xml.nextText();
                        } else if ("Description".equals(xml.getName())) {
                            inline.description = xml.nextText();
                        } else if ("Advertiser".equals(xml.getName())) {
                            inline.advertiser = xml.nextText();
                        } else if ("Creative".equals(xml.getName())) {
                            creative = new AlxVastInline.Creative();
                            creative.id = xml.getAttributeValue(xml.getNamespace(), "id");
                            creative.sequence = xml.getAttributeValue(xml.getNamespace(), "sequence");
                        } else if ("Linear".equals(xml.getName())) {
                            linear = new AlxVastInline.Linear();
                        } else if ("Duration".equals(xml.getName())) {
                            if (linear != null) {
                                linear.duration = xml.nextText();
                            }
                        } else if ("MediaFile".equals(xml.getName())) {
                            AlxVastInline.MediaFile mediaFile = new AlxVastInline.MediaFile();
                            mediaFile.width = xml.getAttributeValue(xml.getNamespace(), "width");
                            mediaFile.height = xml.getAttributeValue(xml.getNamespace(), "height");
                            mediaFile.type = xml.getAttributeValue(xml.getNamespace(), "type");
                            mediaFile.bitrate = xml.getAttributeValue(xml.getNamespace(), "bitrate");
                            mediaFile.delivery = xml.getAttributeValue(xml.getNamespace(), "delivery");
                            mediaFile.scalable = xml.getAttributeValue(xml.getNamespace(), "scalable");
                            mediaFile.maintainAspectRatio = xml.getAttributeValue(xml.getNamespace(), "maintainAspectRatio");
                            mediaFile.url = xml.nextText();
                            if (linear != null) {
                                linear.mediaFileList.add(mediaFile);
                            }
                        } else if ("Icon".equals(xml.getName())) {
                            icon = new AlxVastInline.Icon();
                            icon.width = xml.getAttributeValue(xml.getNamespace(), "width");
                            icon.height = xml.getAttributeValue(xml.getNamespace(), "height");
                            icon.program = xml.getAttributeValue(xml.getNamespace(), "program");
                        } else if ("TrackingEvents".equals(xml.getName())) {
                            if (linear != null) {
                                linear.trackingEvents = new AlxBaseVastBean.TrackingEvents();
                            }
                        } else if ("VideoClicks".equals(xml.getName())) {
                            if (linear != null) {
                                linear.videoClicks = new AlxBaseVastBean.VideoClicks();
                            }
                        } else if ("Tracking".equals(xml.getName())) {
                            AlxBaseVastBean.Tracking tracking = new AlxBaseVastBean.Tracking();
                            tracking.event = xml.getAttributeValue(xml.getNamespace(), "event");
                            tracking.offset = xml.getAttributeValue(xml.getNamespace(), "offset");
                            tracking.url = xml.nextText();
                            if (linear != null && linear.trackingEvents != null) {
                                linear.trackingEvents.trackingList.add(tracking);
                            }
                        } else if ("ClickTracking".equals(xml.getName())) {
                            AlxBaseVastBean.ClickTracking clickTracking = new AlxBaseVastBean.ClickTracking();
                            clickTracking.url = xml.nextText();
                            if (linear != null && linear.videoClicks != null) {
                                linear.videoClicks.clickTrackingList.add(clickTracking);
                            }
                        } else if ("ClickThrough".equals(xml.getName())) {
                            AlxBaseVastBean.ClickThrough clickThrough = new AlxBaseVastBean.ClickThrough();
                            clickThrough.url = xml.nextText();
                            if (linear != null && linear.videoClicks != null) {
                                linear.videoClicks.clickThroughList.add(clickThrough);
                            }
                        } else if ("Companion".equals(xml.getName())) {
                            companion = new AlxBaseVastBean.Companion();
                            companion.width = xml.getAttributeValue(xml.getNamespace(), "width");
                            companion.height = xml.getAttributeValue(xml.getNamespace(), "height");
                            companion.assetWidth = xml.getAttributeValue(xml.getNamespace(), "assetWidth");
                            companion.assetHeight = xml.getAttributeValue(xml.getNamespace(), "assetHeight");
                        } else if ("StaticResource".equals(xml.getName())) {
                            AlxBaseVastBean.StaticResource staticResource = new AlxBaseVastBean.StaticResource();
                            staticResource.creativeType = xml.getAttributeValue(xml.getNamespace(), "creativeType");
                            staticResource.url = xml.nextText();
                            if (companion != null) {
                                companion.staticResource = staticResource;
                            }
                            if (nonLinear != null) {
                                nonLinear.staticResource = staticResource;
                            }
                            if (icon != null) {
                                icon.staticResource = staticResource;
                            }
                        } else if ("CompanionClickThrough".equals(xml.getName())) {
                            AlxBaseVastBean.ClickThrough companionClickThrough = new AlxBaseVastBean.ClickThrough();
                            companionClickThrough.url = xml.nextText();
                            if (companion != null) {
                                companion.companionClickThrough = companionClickThrough;
                            }
                        } else if ("CompanionClickTracking".equals(xml.getName())) {
                            AlxBaseVastBean.ClickTracking companionClickTracking = new AlxBaseVastBean.ClickTracking();
//                            companionClickTracking.id = xml.getAttributeValue(xml.getNamespace(), "id");
                            companionClickTracking.url = xml.nextText();
                            if (companion != null) {
                                companion.companionClickTracking = companionClickTracking;
                            }
                        } else if ("NonLinear".equals(xml.getName())) {
                            nonLinear = new AlxVastInline.NonLinear();
                            nonLinear.width = xml.getAttributeValue(xml.getNamespace(), "width");
                            nonLinear.height = xml.getAttributeValue(xml.getNamespace(), "height");
                        } else if ("NonLinearClickTracking".equals(xml.getName())) {
                            AlxBaseVastBean.ClickTracking nonLinearClickTracking = new AlxBaseVastBean.ClickTracking();
                            nonLinearClickTracking.url = xml.nextText();
                            if (nonLinear != null) {
                                nonLinear.nonLinearClickTracking = nonLinearClickTracking;
                            }
                        } else if ("NonLinearClickThrough".equals(xml.getName())) {
                            AlxBaseVastBean.ClickThrough nonLinearClickThrough = new AlxBaseVastBean.ClickThrough();
                            nonLinearClickThrough.url = xml.nextText();
                            if (nonLinear != null) {
                                nonLinear.nonLinearClickThrough = nonLinearClickThrough;
                            }
                        } else if ("IconClickThrough".equals(xml.getName())) {
                            AlxBaseVastBean.ClickThrough iconClickThrough = new AlxBaseVastBean.ClickThrough();
                            iconClickThrough.url = xml.nextText();
                            if (icon != null && icon.iconClickThroughList != null) {
                                icon.iconClickThroughList.add(iconClickThrough);
                            }
                        } else if ("Extension".equals(xml.getName())) {
                            extension = new AlxBaseVastBean.Extension();
                            extension.type = xml.getAttributeValue(xml.getNamespace(), "type");
                        } else if ("Verification".equals(xml.getName())) {
                            if (extension != null) {
                                extension.vendor = xml.getAttributeValue(xml.getNamespace(), "vendor");
                            }
                        } else if ("JavaScriptResource".equals(xml.getName())) {
                            if (extension != null) {
                                extension.apiFramework = xml.getAttributeValue(xml.getNamespace(), "apiFramework");
                                extension.browserOptional = xml.getAttributeValue(xml.getNamespace(), "browserOptional");
                                extension.url = xml.nextText();
                            }
                        } else if ("VerificationParameters".equals(xml.getName())) {
                            if (extension != null) {
                                extension.parameters = xml.nextText();
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if ("Linear".equals(xml.getName())) {
                            if (linear != null) {
                                if (creative != null) {
                                    creative.linear = linear;
                                }
                                linear = null;
                            }
                        } else if ("Companion".equals(xml.getName())) {
                            if (companion != null) {
                                if (creative != null && creative.companionList != null) {
                                    creative.companionList.add(companion);
                                }
                                companion = null;
                            }
                        } else if ("NonLinear".equals(xml.getName())) {
                            if (nonLinear != null) {
                                if (creative != null && creative.nonLinearList != null) {
                                    creative.nonLinearList.add(nonLinear);
                                }
                                nonLinear = null;
                            }
                        } else if ("Creative".equals(xml.getName())) {
                            if (creative != null) {
                                if (inline != null && inline.creativeList != null) {
                                    inline.creativeList.add(creative);
                                }
                                creative = null;
                            }
                        } else if ("Icon".equals(xml.getName())) {
                            if (icon != null) {
                                if (linear != null && linear.iconList != null) {
                                    linear.iconList.add(icon);
                                }
                                icon = null;
                            }
                        } else if ("Extension".equals(xml.getName())) {
                            if (extension != null) {
                                if (inline != null) {
                                    inline.extension = extension;
                                }
                                extension = null;
                            }
                        }
                        break;
                }
                eventType = xml.next();
            }
            mInline = inline;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "Inline-error1:" + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "Inline-error2:" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "Inline-error3:" + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}