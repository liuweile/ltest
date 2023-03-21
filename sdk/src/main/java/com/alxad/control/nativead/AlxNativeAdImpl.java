package com.alxad.control.nativead;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxImage;
import com.alxad.api.nativead.AlxMediaContent;
import com.alxad.api.nativead.AlxNativeAd;
import com.alxad.api.nativead.AlxNativeEventListener;
import com.alxad.base.AlxAdNetwork;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxNativeUIData;
import com.alxad.entity.AlxNativeUIStatus;
import com.alxad.entity.AlxTracker;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.report.AlxReportManager;
import com.alxad.util.AlxLog;

import java.util.ArrayList;
import java.util.List;

public class AlxNativeAdImpl extends AlxNativeAd {
    private static final String TAG = "AlxNativeAdImpl";

    private AlxNativeUIData mUiData;
    private AlxTracker mTracker;

    private List<AlxImage> mImageList;
    private AlxNativeEventListener mAlxNativeEventListener;
    private AlxMediaContentImpl mMediaContent;

    private AlxNativeUIStatus mUIStatus;

    public AlxNativeAdImpl(AlxNativeUIData data, AlxTracker tracker) {
        mUiData = data;
        mTracker = tracker;
        init();
    }

    private void init() {
        if (mUiData == null) {
            return;
        }
        mMediaContent = createMediaContent(mUiData);
        try {
            List<AlxImageImpl> list = mUiData.json_imageList;
            if (list != null && list.size() > 0) {
                mImageList = new ArrayList<>();
                mImageList.addAll(list);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
            AlxAgent.onError(e);
        }
    }

    @Override
    public String getTitle() {
        if (mUiData != null) {
            if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_VIDEO) {
                AlxVideoVastBean video = mUiData.video;
                if (video != null) {
                    return video.adTitle;
                }
            } else if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_JSON) {
                return mUiData.json_title;
            }
        }
        return null;
    }

    @Override
    public String getDescription() {
        if (mUiData != null) {
            if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_VIDEO) {
                AlxVideoVastBean video = mUiData.video;
                if (video != null) {
                    return video.description;
                }
            } else if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_JSON) {
                return mUiData.json_desc;
            }
        }
        return null;
    }

    @Override
    public AlxImage getIcon() {
        if (mUiData != null) {
            if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_VIDEO) {
                AlxVideoVastBean video = mUiData.video;
                if (video != null && !TextUtils.isEmpty(video.iconUrl)) {
                    AlxImageImpl image = new AlxImageImpl();
                    image.setUrl(video.iconUrl);
                    image.setWidth(video.iconWidth);
                    image.setHeight(video.iconHeight);
                    return image;
                }
            } else if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_JSON) {
                return mUiData.json_icon;
            }
        }
        return null;
    }

    @Override
    public List<AlxImage> getImages() {
        return mImageList;
    }

    @Override
    public String getCallToAction() {
        if (mUiData != null) {
            if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_VIDEO) {
                return null;
            } else if (mUiData.dataType == AlxNativeUIData.DATA_TYPE_JSON) {
                return mUiData.json_callToAction;
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        if (mImageList != null) {
            mImageList.clear();
            mImageList = null;
        }
        mAlxNativeEventListener = null;
        mUiData = null;
        mTracker = null;
        mMediaContent = null;
        if (mUIStatus != null) {
            mUIStatus.destroy();
        }
        mUIStatus = null;
    }

    @Override
    public int getCreativeType() {
        if (mUiData != null && mUiData.extField != null) {
            return mUiData.extField.assetType;
        }
        return 0;
    }

    @Override
    public String getAdSource() {
        if (mUiData != null && mUiData.extField != null) {
            return mUiData.extField.source;
        }
        return null;
    }

    @Override
    public double getPrice() {
        if (mUiData != null) {
            return mUiData.price;
        }
        return 0;
    }

    @Override
    public void reportBiddingUrl() {
        if (mUiData != null) {
            AlxReportManager.reportUrl(mUiData.nurl, mUiData, "bidding");
        }
    }

    @Override
    public void reportChargingUrl() {
        if (mUiData != null) {
            AlxReportManager.reportUrl(mUiData.burl, mUiData, "charging");
        }
    }

    @Override
    public void setNativeEventListener(AlxNativeEventListener listener) {
        mAlxNativeEventListener = listener;
    }

    @Override
    public AlxMediaContent getMediaContent() {
        return mMediaContent;
    }

    @Override
    public Bitmap getAdLogo() {
        try {
            Context context = AlxAdNetwork.getContext();
            if (context != null) {
                Resources res = context.getResources();
                Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.alx_ad_logo);
                return bmp;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }

    public AlxNativeUIData getUiData() {
        return mUiData;
    }

    public AlxTracker getTracker() {
        return mTracker;
    }

    public AlxNativeEventListener getAlxNativeEventListener() {
        return mAlxNativeEventListener;
    }

    /**
     * 创建媒体MediaContent: Vast视频数据 或 json数据中只有一张图片是，才创建
     *
     * @param data
     * @return
     */
    private AlxMediaContentImpl createMediaContent(AlxNativeUIData data) {
        if (data == null) {
            return null;
        }
        AlxMediaContentImpl bean = null;

        if (data.dataType == AlxNativeUIData.DATA_TYPE_VIDEO) {
            if (data.video != null) {
                bean = new AlxMediaContentImpl(data);
            }
        } else if (data.dataType == AlxNativeUIData.DATA_TYPE_JSON) {
            List<AlxImageImpl> images = data.json_imageList;
            if (images != null && images.size() == 1) {
                bean = new AlxMediaContentImpl(data);
            }
        }
        return bean;
    }

    public AlxNativeUIStatus getUIStatus() {
        return mUIStatus;
    }

    public void setUIStatus(AlxNativeUIStatus status) {
        this.mUIStatus = status;
    }

}