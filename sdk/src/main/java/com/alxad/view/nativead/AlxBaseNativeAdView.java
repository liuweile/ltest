package com.alxad.view.nativead;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.alxad.api.nativead.AlxMediaContent;
import com.alxad.api.nativead.AlxMediaView;
import com.alxad.api.nativead.AlxNativeAd;
import com.alxad.api.nativead.AlxNativeEventListener;
import com.alxad.base.AlxJumpCallback;
import com.alxad.base.AlxLogLevel;
import com.alxad.bus.ViewObserverBus;
import com.alxad.bus.ViewObserverCallback;
import com.alxad.control.nativead.AlxMediaContentImpl;
import com.alxad.control.nativead.AlxNativeAdImpl;
import com.alxad.entity.AlxNativeMediaUIStatus;
import com.alxad.entity.AlxNativeUIData;
import com.alxad.entity.AlxNativeUIStatus;
import com.alxad.entity.AlxOmidBean;
import com.alxad.entity.AlxVideoExtBean;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.omsdk.OmAdSafe;
import com.alxad.report.AlxReportManager;
import com.alxad.report.AlxReportPlaceHolder;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxClickJump;
import com.alxad.util.AlxLog;
import com.alxad.view.banner.AlxBannerViewListener;
import com.alxad.view.banner.AlxBannerViewVideoListener;
import com.alxad.widget.video.AlxVideoPlayerView;
import com.iab.omid.library.algorixco.adsession.FriendlyObstructionPurpose;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 原生广告容器实现类
 *
 * @author lwl
 * @date 2022-9-14
 */
public class AlxBaseNativeAdView extends RelativeLayout implements ViewObserverCallback {
    private static final String TAG = "AlxBaseNativeAdView";

    private Context mContext;
    protected AlxNativeAdImpl mUiData;

    protected View mCloseView;
    protected AlxMediaView mMediaView;

    protected final Map<String, View> mViews = new HashMap<>();
    private ViewClickListener mViewClickListener;
    private ViewObserverBus mViewObserverBus;
    private volatile boolean isViewClick = true;//点击事件是否在处理中
    private AlxMediaContent.VideoLifecycleListener mVideoLifecycleListener;

    public AlxBaseNativeAdView(Context context) {
        super(context);
        init(context);
    }

    public AlxBaseNativeAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AlxBaseNativeAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("NewApi")
    public AlxBaseNativeAdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewObserverBus != null) {
            mViewObserverBus.viewAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mViewObserverBus != null) {
            mViewObserverBus.viewDetachedFromWindow();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (mViewObserverBus != null) {
            mViewObserverBus.viewVisibilityChanged();
        }
    }

    private void init(Context context) {
        mContext = context;
        mViewObserverBus = new ViewObserverBus(this, this);
        mViewClickListener = new ViewClickListener();
        AlxLog.d(AlxLogLevel.MARK, TAG, "init");
    }

    public void setNativeAd(AlxNativeAd nativeAd) {
        if (nativeAd instanceof AlxNativeAdImpl) {
            mUiData = (AlxNativeAdImpl) nativeAd;
            if (mUiData == null) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "setNativeAd():  Object is null");
                return;
            }
            addClickListener();
            if (mViewObserverBus != null && mViewObserverBus.isViewVisible()) {
                renderAd();
            }
        } else {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "setNativeAd():  Object instance error");
        }
    }

    private void addClickListener() {
        if (mViewClickListener != null) {
            setOnClickListener(mViewClickListener);
            if (mViews != null && !mViews.isEmpty()) {
                for (Map.Entry<String, View> entry : mViews.entrySet()) {
                    View view = entry.getValue();
                    if (view != null) {
                        view.setOnClickListener(mViewClickListener);
                    }
                }
            }
        }

        if (mCloseView != null) {
            mCloseView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    bnAdCloseEvent();
                }
            });
        }
        AlxNativeUIData nativeBean = mUiData == null ? null : mUiData.getUiData();
        addMediaViewListener(nativeBean);
    }

    private void addMediaViewListener(final AlxNativeUIData nativeBean) {
        if (mMediaView == null || mMediaView.getAdContentView() == null) {
            return;
        }
        final AlxVideoVastBean video = nativeBean == null ? null : nativeBean.video;
        if (video != null && (mMediaView.getAdContentView() instanceof AlxNativeVideoView)) {
            AlxNativeVideoView videoView = (AlxNativeVideoView) mMediaView.getAdContentView();
            final AlxMediaContent mediaContent = videoView.getMediaContent();
            if (mediaContent != null) {
                mVideoLifecycleListener = mediaContent.getVideoLifecycleListener();
            }

            videoView.setEventListener(new AlxBannerViewVideoListener() {

                @Override
                public void onVideoPlayCompletion() {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onVideoPlayCompletion");
                    if (video != null) {
                        AlxReportManager.reportUrl(video.completeList, nativeBean, "play-complete");
                    }
                    OmAdSafe omAdSafe = getOmAdSafe();
                    if (omAdSafe != null) {
                        omAdSafe.reportVideoComplete();
                    }
                    if (mVideoLifecycleListener != null) {
                        mVideoLifecycleListener.onVideoEnd();
                    }
                }

                @Override
                public void onVideoPlayPause() {
                    omidReport(OMID_EVENT_TYPE_PAUSE);
                    if (mVideoLifecycleListener != null) {
                        mVideoLifecycleListener.onVideoPause();
                    }
                }

                @Override
                public void onVideoPlayStart() {
                    omidReport(OMID_EVENT_TYPE_START);
                    sendVideoStart();
                    if (mVideoLifecycleListener != null) {
                        mVideoLifecycleListener.onVideoPlay();
                    }
                }

                private void sendVideoStart() {
                    AlxNativeMediaUIStatus status = getNativeMediaUIStatus();
                    if (status != null) { //防止重复回调
                        if (status.isVideoOnVideoStart()) {
                            AlxLog.d(AlxLogLevel.MARK, TAG, "onVideoStart() repeat");
                            return;
                        } else {
                            status.setVideoOnVideoStart(true);
                        }
                    }
                    if (mVideoLifecycleListener != null) {
                        mVideoLifecycleListener.onVideoStart();
                    }
                }

                @Override
                public void onVideoPlayStop() {

                }

                @Override
                public void onVideoMute(boolean mute) {
                    if (video != null) {
                        if (mute) {
                            AlxReportManager.reportUrl(video.muteList, nativeBean, "mute");
                        } else {
                            AlxReportManager.reportUrl(video.unmuteList, nativeBean, "unmute");
                        }
                    }
                    if (mVideoLifecycleListener != null) {
                        mVideoLifecycleListener.onVideoMute(mute);
                    }
                }

                @Override
                public void onVideoPlayFailed(int errCode, String errMsg) {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onVideoPlayFailed:" + errCode + ";" + errMsg);
                    if (video != null) {
                        try {
                            String vastErrorCode = String.valueOf(AlxReportManager.exchangeVideoVastErrorCode(errCode));
                            List<String> list = AlxReportManager.replaceUrlPlaceholder(video.errorList, AlxReportPlaceHolder.VIDEO_VAST_ERROR, vastErrorCode);
                            AlxReportManager.reportUrl(list, nativeBean, "play-error");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (mVideoLifecycleListener != null) {
                        mVideoLifecycleListener.onVideoPlayError(errCode, errMsg);
                    }
                }

                private AlxNativeMediaUIStatus getNativeMediaUIStatus() {
                    AlxNativeMediaUIStatus status = null;
                    if (mediaContent != null && mediaContent instanceof AlxMediaContentImpl) {
                        AlxMediaContentImpl temp = (AlxMediaContentImpl) mediaContent;
                        status = temp.getMediaUIStatus();
                    }
                    return status;
                }

                @Override
                public void onVideoPlayProgress(int progress) {
                    if (video != null) {
                        OmAdSafe omAdSafe = null;
                        AlxNativeMediaUIStatus status = null;
                        switch (progress) {
                            case 25:
                                omAdSafe = getOmAdSafe();
                                status = getNativeMediaUIStatus();
                                if (status != null) { //防止重复上报
                                    if (status.isVideoHasReportQuarter()) {
                                        AlxLog.d(AlxLogLevel.MARK, TAG, "report repeat: play-0.25");
                                        return;
                                    } else {
                                        status.setVideoHasReportQuarter(true);
                                    }
                                }

                                AlxReportManager.reportUrl(video.firstQuartileList, nativeBean, "play-0.25");
                                if (omAdSafe != null) {
                                    omAdSafe.reportVideoFirstQuartile();
                                }
                                break;
                            case 50:
                                omAdSafe = getOmAdSafe();
                                status = getNativeMediaUIStatus();
                                if (status != null) { //防止重复上报
                                    if (status.isVideoHasReportHalf()) {
                                        AlxLog.d(AlxLogLevel.MARK, TAG, "report repeat: play-0.5");
                                        return;
                                    } else {
                                        status.setVideoHasReportHalf(true);
                                    }
                                }

                                AlxReportManager.reportUrl(video.midPointList, nativeBean, "play-0.5");
                                if (omAdSafe != null) {
                                    omAdSafe.reportVideoMidpoint();
                                }
                                break;
                            case 75:
                                omAdSafe = getOmAdSafe();
                                status = getNativeMediaUIStatus();
                                if (status != null) { //防止重复上报
                                    if (status.isVideoHasReportThreeFourths()) {
                                        AlxLog.d(AlxLogLevel.MARK, TAG, "report repeat: play-0.75");
                                        return;
                                    } else {
                                        status.setVideoHasReportThreeFourths(true);
                                    }
                                }

                                AlxReportManager.reportUrl(video.thirdQuartileList, nativeBean, "play-0.75");
                                if (omAdSafe != null) {
                                    omAdSafe.reportVideoThirdQuartile();
                                }
                                break;
                        }
                    }
                }

                @Override
                public void onViewClick(String url) {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onViewClick");
                    //点击上报
                    if (video != null) {
                        AlxReportManager.reportUrl(video.clickTrackingList, nativeBean, AlxReportManager.LOG_TAG_CLICK);
                        omidReport(OMID_EVENT_TYPE_CLICK);
                    }
                    bnAdClickEvent(url);
                }

                @Override
                public void onViewShow() {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onViewShow");
                    //曝光上报
                    if (video != null) {
                        AlxReportManager.reportUrl(video.impressList, nativeBean, AlxReportManager.LOG_TAG_SHOW);
                    }
                }

                @Override
                public void onViewClose() {
                    //不用处理
                }

                private final int OMID_EVENT_TYPE_START = 10;
                private final int OMID_EVENT_TYPE_PAUSE = 11;
                private final int OMID_EVENT_TYPE_RESUME = 12;
                private final int OMID_EVENT_TYPE_BUFFER_START = 13;
                private final int OMID_EVENT_TYPE_BUFFER_END = 14;
                private final int OMID_EVENT_TYPE_CLICK = 15;

                private void omidReport(int eventType) {
                    OmAdSafe omAdSafe = getOmAdSafe();
                    if (omAdSafe == null) {
                        return;
                    }
                    try {
                        if (eventType == OMID_EVENT_TYPE_START) {
                            if (mMediaView != null && mMediaView.getAdContentView() instanceof AlxNativeVideoView) {
                                AlxNativeVideoView view = (AlxNativeVideoView) mMediaView.getAdContentView();
                                AlxVideoPlayerView videoView = view.getVideoView();
                                if (videoView != null) {
                                    omAdSafe.reportVideoStart(videoView.getDuration(), videoView.isMute());
                                }
                            }
                        } else if (eventType == OMID_EVENT_TYPE_PAUSE) {
                            omAdSafe.reportVideoPause();
                        } else if (eventType == OMID_EVENT_TYPE_RESUME) {
                            omAdSafe.reportVideoResume();
                        } else if (eventType == OMID_EVENT_TYPE_BUFFER_START) {
                            omAdSafe.reportVideoBufferStart();
                        } else if (eventType == OMID_EVENT_TYPE_BUFFER_END) {
                            omAdSafe.reportVideoBufferEnd();
                        } else if (eventType == OMID_EVENT_TYPE_CLICK) {
                            omAdSafe.reportVideoClick();
                        }
                    } catch (Exception e) {
                        AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                    }
                }
            });
        } else if (mMediaView.getAdContentView() instanceof AlxNativeImageView) {
            AlxNativeImageView imageView = (AlxNativeImageView) mMediaView.getAdContentView();
            imageView.setEventListener(new AlxBannerViewListener() {
                @Override
                public void onViewClick(String url) {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onViewClick");
                    bnAdClickEvent(url);
                }

                @Override
                public void onViewShow() {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onViewShow");
                }

                @Override
                public void onViewClose() {
                    //不用处理
                }
            });
        }
    }


    private void removeClickListener() {
        try {
            setOnClickListener(null);
            if (mCloseView != null) {
                mCloseView.setOnClickListener(null);
            }
            if (mViews == null || mViews.isEmpty()) {
                return;
            }
            for (Map.Entry<String, View> entry : mViews.entrySet()) {
                View view = entry.getValue();
                if (view != null) {
                    view.setOnClickListener(null);
                }
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    /**
     * 广告开始曝光
     */
    private void renderAd() {
        if (mUiData == null) {
            return;
        }
        AlxNativeUIStatus status = mUiData.getUIStatus();
        if (status == null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "renderAd()");
            status = new AlxNativeUIStatus();

            OmAdSafe mOmAdSafe = new OmAdSafe();
            mOmAdSafe.initNoWeb(getContext(), this, OmAdSafe.TYPE_NATIVE, getOmidBean());

            status.setOmAdSafe(mOmAdSafe);
            mUiData.setUIStatus(status);

            reportShow(mUiData);
            reportShowOmid(mUiData.getUiData());

            AlxNativeEventListener listener = mUiData.getAlxNativeEventListener();
            if (listener != null) {
                listener.onAdImpression();
            }
        }
    }

    @Override
    public void onViewVisible() {
        if (mUiData != null) {
            renderAd();
        }
    }

    @Override
    public void onViewHidden() {

    }

    private class ViewClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "onClick");
            bnAdClickEvent(getDetailUrl());
        }
    }

    /**
     * 获取广告详情页面的url跳转地址
     *
     * @return
     */
    private String getDetailUrl() {
        if (mUiData == null) {
            return null;
        }
        String url = null;
        try {
            AlxNativeUIData data = mUiData.getUiData();
            if (data != null) {
                if (data.dataType == AlxNativeUIData.DATA_TYPE_JSON) {
                    url = data.json_link;
                } else if (data.dataType == AlxNativeUIData.DATA_TYPE_VIDEO) {
                    AlxVideoVastBean video = data.video;
                    if (video != null) {
                        List<String> urls = video.clickThroughList;
                        if (urls != null && urls.size() > 0) {
                            url = urls.get(0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return url;
    }

    private void bnAdClickEvent(String url) {
        if (isViewClick) {
            isViewClick = false;
            bnAdClickHandle(url);
            isViewClick = true;
        }
    }

    /**
     * 广告点击事件处理
     */
    private void bnAdClickHandle(String url) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "bnAdClickHandle");
        if (mUiData == null) {
            return;
        }
        AlxNativeUIData data = mUiData.getUiData();
        if (data != null) {
            AlxReportManager.reportUrl(data.clickTrackers, data, AlxReportManager.LOG_TAG_CLICK);
            AlxClickJump.openLink(mContext, data.deeplink, url, data.bundle, mUiData.getTracker(), new AlxJumpCallback() {
                @Override
                public void onDeeplinkCallback(boolean isSuccess, String error) {
                    try {
                        if (isSuccess) {
                            AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link(Deeplink) open is true");
                            AlxSdkData.tracker(mUiData.getTracker(), AlxSdkDataEvent.DEEPLINK_YES);
                        } else {
                            AlxLog.i(AlxLogLevel.MARK, TAG, "Deeplink Open Failed: " + error);
                            AlxSdkData.tracker(mUiData.getTracker(), AlxSdkDataEvent.DEEPLINK_NO);
                        }
                    } catch (Exception e) {
                        AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onUrlCallback(boolean isSuccess, int type) {
                    AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link open is " + isSuccess);
                }
            });
        }

        AlxNativeEventListener listener = mUiData.getAlxNativeEventListener();
        if (listener != null) {
            listener.onAdClicked();
        }
    }

    /**
     * 广告关闭事件
     */
    private void bnAdCloseEvent() {
        AlxLog.i(AlxLogLevel.MARK, TAG, "bnAdCloseEvent");
        if (mUiData == null) {
            return;
        }
        AlxNativeEventListener listener = mUiData.getAlxNativeEventListener();
        destroy();
        if (listener != null) {
            listener.onAdClosed();
        }
    }

    /**
     * 曝光上报
     */
    private void reportShow(AlxNativeAdImpl bean) {
        try {
            if (bean == null || bean.getUiData() == null) {
                return;
            }
            AlxNativeUIStatus status = bean.getUIStatus();
            if (status != null && status.isReportImpression()) {
                return;
            }
            AlxNativeUIData data = bean.getUiData();
            AlxReportManager.reportUrl(data.impressTrackers, data, AlxReportManager.LOG_TAG_SHOW);
            if (status != null) {
                status.setReportImpression(true);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "reportShow():" + e.getMessage());
        }
    }

    /**
     * 曝光上报到omid平台
     *
     * @param data
     */
    private void reportShowOmid(AlxNativeUIData data) {
        OmAdSafe omAdSafe = getOmAdSafe();
        if (omAdSafe != null) {
            try {
                omAdSafe.registerAdView(this);
                if (data != null && data.video != null && data.video.extField != null) {
                    AlxVideoExtBean extBean = data.video.extField;
                    omAdSafe.reportLoad(extBean.isSkip(), extBean.skipafter, extBean.isMute());
                } else {
                    omAdSafe.reportLoad();
                }
                omAdSafe.reportImpress();
                if (mCloseView != null) {
                    omAdSafe.addFriendlyObstruction(mCloseView, FriendlyObstructionPurpose.CLOSE_AD, "close");
                }
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            }
        }
    }

    protected void destroy() {
        try {
            removeClickListener();
            this.mViews.clear();

            //恢复默认值
            isViewClick = true;

            if (mMediaView != null) {
                mMediaView.destroy();
            }

            OmAdSafe omAdSafe = getOmAdSafe();
            if (omAdSafe != null) {
                omAdSafe.destroy();
            }

            if (mUiData != null) {
                mUiData.destroy();
                mUiData = null;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private AlxOmidBean getOmidBean() {
        try {
            if (mUiData == null || mUiData.getUiData() == null) {
                return null;
            }
            AlxNativeUIData bean = mUiData.getUiData();
            if (bean.extField != null) {
                return bean.extField.omid;
            }
            if (bean.video != null) {
                return bean.video.omid;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }

    private OmAdSafe getOmAdSafe() {
        try {
            if (mUiData != null && mUiData.getUIStatus() != null) {
                return mUiData.getUIStatus().getOmAdSafe();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }

}