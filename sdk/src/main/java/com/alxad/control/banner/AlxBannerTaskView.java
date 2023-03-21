package com.alxad.control.banner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxBannerView;
import com.alxad.api.AlxBannerViewAdListener;
import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxViewListener;
import com.alxad.bus.ViewObserverBus;
import com.alxad.bus.ViewObserverCallback;
import com.alxad.control.AlxBannerAdModel;
import com.alxad.entity.AlxBannerUIData;
import com.alxad.base.AlxJumpCallback;
import com.alxad.entity.AlxBannerUIStatus;
import com.alxad.entity.AlxNativeMediaUIStatus;
import com.alxad.entity.AlxOmidBean;
import com.alxad.entity.AlxTracker;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.omsdk.OmAdSafe;
import com.alxad.report.AlxReportManager;
import com.alxad.report.AlxReportPlaceHolder;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxClickJump;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxUtil;
import com.alxad.view.banner.AlxBannerVideoView;
import com.alxad.view.banner.AlxBannerViewVideoListener;
import com.alxad.view.banner.AlxBannerViewWebListener;
import com.alxad.view.banner.AlxBannerWebView;
import com.alxad.view.banner.AlxBaseBannerView;
import com.alxad.widget.video.AlxVideoPlayerView;
import com.iab.omid.library.algorixco.adsession.FriendlyObstructionPurpose;

import java.util.List;


/**
 * banner 广告 既可以在xml布局中添加，也可以在代码中创建<br/>
 * 广告素材有html,video
 * 支持自动刷新
 *
 * @author lwl
 * @date 2022-4-14
 */
public class AlxBannerTaskView extends FrameLayout implements ViewObserverCallback {
    private final String TAG = "AlxBannerTaskView";//AlxBannerTaskView.class.getSimpleName();混淆后tag会变成乱码
    private final int DEFAULT_REFRESH_TIME = 30;//默认广告刷新时间，单位s

    protected Context mContext;
    private ViewObserverBus mBus;

    private AlxBaseBannerView mBannerView;
    protected boolean isShowCloseBn = false;//是否可以关闭广告，默认是不可关闭
    protected int mRefreshTime = DEFAULT_REFRESH_TIME;//单位s

    protected Handler mHandler;

    private boolean isViewVisible = false;
    private boolean isViewHidden = false;

    protected AlxBannerUIData mUIData;//广告数据对象，只有在真实展示时才赋值，防止自动刷新失败或加载不展示时给替换掉
    private AlxTracker mTracker; //数据追踪器
    private AlxViewListener mViewListener;

    private String pid;
    private AlxBannerView.AlxAdParam mAdParam;
    private AlxBannerViewAdListener mListener;

    protected AlxBannerAdModel mController;
    private OmAdSafe mOmAdSafe;

    public AlxBannerTaskView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public AlxBannerTaskView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public AlxBannerTaskView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @SuppressLint("NewApi")
    public AlxBannerTaskView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        mBus = new ViewObserverBus(this, this, true);
        mHandler = new Handler(context.getMainLooper());
    }

    /**
     * @param pid
     * @param listener
     * @param param    广告请求参数
     */
    public void requestAd(String pid, AlxBannerView.AlxAdParam param, AlxBannerViewAdListener listener) {
        AlxLog.d(AlxLogLevel.OPEN, TAG, "banner-ad-init: pid=" + pid);
        this.pid = pid;
        this.mListener = listener;
        this.mAdParam = param;

        if (mAdParam != null) {
            if (mAdParam.getRefreshTime() != null) {
                mRefreshTime = mAdParam.getRefreshTime().intValue();
            }
            if (mAdParam.isCanClose() != null) {
                isShowCloseBn = mAdParam.isCanClose().booleanValue();
            }
        }
        setViewListener();
        mController = new AlxBannerAdModel(mContext, pid, param, new AlxBannerViewAdListener() {
            @Override
            public void onAdLoaded() {
                if (mListener != null) {
                    mListener.onAdLoaded();
                }
                loadSuccessAndShow();
            }

            @Override
            public void onAdError(int errorCode, String errorMsg) {
                if (mListener != null) {
                    mListener.onAdError(errorCode, errorMsg);
                }
            }

            @Override
            public void onAdClicked() {

            }

            @Override
            public void onAdShow() {

            }

            @Override
            public void onAdClose() {

            }
        });
        mController.load();
    }

    private void setViewListener() {
        mViewListener = new AlxViewListener() {
            @Override
            public void onViewClick() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onAdClicked");
                //点击上报
                if (mUIData != null) {
                    AlxReportManager.reportUrl(mUIData.clickTrackers, mUIData, AlxReportManager.LOG_TAG_CLICK);
                }
                if (mListener != null) {
                    mListener.onAdClicked();
                }
            }

            @Override
            public void onViewShow() {
                //曝光上报
                if (mUIData != null) {
                    AlxReportManager.reportUrl(mUIData.impressTrackers, mUIData, AlxReportManager.LOG_TAG_SHOW);
                }
                if (mListener != null) {
                    mListener.onAdShow();
                }
            }

            @Override
            public void onViewClose() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onAdClose");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }
        };
    }

    private void handlerRefresh(boolean isClickRefresh) {
        if (mHandler != null && mRefreshTime > 0) {
            int time = mRefreshTime * 1000;
            if (isClickRefresh) {//点击刷新
                time = 1000;
            }
            try {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AlxLog.d(AlxLogLevel.OPEN, TAG, "onRefresh");
                        requestAd(pid, mAdParam, mListener);
                    }
                }, time);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            }
        }
    }

    private void cancelHandlerRefresh() {
        if (mHandler != null) {
            try {
                mHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            }
        }
    }

    private void loadSuccessAndShow() {
        if (isViewVisible()) {
            showAdUI();
        }
    }

    /**
     * View可见时调用【可见 = View依附到Window + View.VISIBLE】
     */
    private void showAdUI() {
        if (mController == null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "viewVisibility:controller is null");
            return;
        }
        if (mController.getResponse() == null) { //针对自动刷新：数据加载失败时，防止对下面数据给替换掉
            AlxLog.d(AlxLogLevel.MARK, TAG, "viewVisibility:response is null");
            return;
        }
        mUIData = mController.getResponse();
        if (mUIData == null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "viewVisibility:data is null");
            return;
        }

        AlxBannerUIStatus status = mUIData.getUIStatus();
        if (status == null) {
            status = new AlxBannerUIStatus();
            mUIData.setUIStatus(status);
        }

        if (status.isHasRenderData()) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "viewVisibility:isShowRepeat=true");
            return;
        }
        status.setHasRenderData(true);

        if (mController.getRequest() != null) {
            mTracker = mController.getRequest().getTracker();
        }
        if (mUIData.dataType != AlxBannerUIData.DATA_TYPE_WEB && mUIData.dataType != AlxBannerUIData.DATA_TYPE_VIDEO) {
            AlxLog.d(AlxLogLevel.ERROR, TAG, "viewVisibility:data type no support");
            return;
        }

        int adWidth = 0; //单位px
        int adHeight = 0; //单位px
        try {
            int[] widthAndHeight = getAdViewShowSize(mUIData.width, mUIData.height);
            if (widthAndHeight != null && widthAndHeight.length == 2) {
                adWidth = widthAndHeight[0];
                adHeight = widthAndHeight[1];
            } else {
                adWidth = AlxUtil.dip2px(mContext, mUIData.width);
                adHeight = AlxUtil.dip2px(mContext, mUIData.height);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "showAdUI():" + e.getMessage());
        }

        boolean isTrue = addAdView(adWidth, adHeight);
        AlxLog.d(AlxLogLevel.MARK, TAG, "viewVisibility:addAdView isTrue=" + isTrue);
        if (isTrue && mBannerView != null) {
            try {
                mBannerView.renderAd(mUIData, adWidth, adHeight);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.OPEN, TAG, "showAdUI():" + e.getMessage());
            }
        }
    }

    private boolean addAdView(int adWidth, int adHeight) {
        if (mUIData == null) {
            return false;
        }
        try {
            if (mOmAdSafe != null) {
                mOmAdSafe.destroy();
                mOmAdSafe = null;
            }
            mOmAdSafe = new OmAdSafe();

            //如果BannerView和现在数据的类型不匹配，就要销毁BannerView并重新创建
            if (mBannerView != null && mBannerView.getDataType() != mUIData.dataType) {
                mBannerView.onDestroy();
                mBannerView = null;
            }
            if (mBannerView == null) {
                if (mUIData.dataType == AlxBannerUIData.DATA_TYPE_WEB) {
                    mBannerView = new AlxBannerWebView(mContext);
                } else if (mUIData.dataType == AlxBannerUIData.DATA_TYPE_VIDEO) {
                    mBannerView = new AlxBannerVideoView(mContext);
                } else {
                    AlxLog.d(AlxLogLevel.ERROR, TAG, "addAdView:data type no support");
                    return false;
                }
                mBannerView.setDataType(mUIData.dataType);
                LayoutParams params = new LayoutParams(adWidth, adHeight);
                params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                mBannerView.setLayoutParams(params);
                removeAllViews();
                addView(mBannerView);
            } else {
                LayoutParams params = (LayoutParams) mBannerView.getLayoutParams();
                params.width = adWidth;
                params.height = adHeight;
                mBannerView.setLayoutParams(params);
            }

            mBannerView.setCanClosed(isShowCloseBn);
            if (mUIData.dataType == AlxBannerUIData.DATA_TYPE_WEB) {
                addWebListener(mUIData);
            } else if (mUIData.dataType == AlxBannerUIData.DATA_TYPE_VIDEO) {
                mOmAdSafe.initNoWeb(getContext(), mBannerView, OmAdSafe.TYPE_VIDEO, getOmidBean());
                addVideoListener(mUIData.video);
            }
            return true;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return false;
    }

    private void addWebListener(final AlxBannerUIData bean) {
        if (mBannerView == null) {
            return;
        }
        mBannerView.setEventListener(new AlxBannerViewWebListener() {
            @Override
            public void onWebError(String error) {
                AlxLog.e(AlxLogLevel.MARK, TAG, "onViewClick:" + error);
                AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_NO);
            }

            @Override
            public void onWebLoading() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onWebLoading");
                AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_AD_LOADING);
            }

            @Override
            public void onViewClick(String url) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onViewClick:" + url);
                clickEvent(url);
                if (mViewListener != null) {
                    mViewListener.onViewClick();
                }

                //点击后重新加载
                handlerRefresh(true);
            }

            @Override
            public void onViewShow() {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onViewShow");
                AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_AD_LOADED);
                try {
                    if (mBannerView instanceof AlxBannerWebView) {
                        AlxBannerWebView bannerWebView = (AlxBannerWebView) mBannerView;
                        if (mOmAdSafe != null) {
                            mOmAdSafe.initWeb(getContext(), bannerWebView.getWebView());
                            mOmAdSafe.registerAdView(mBannerView);
                            mOmAdSafe.reportLoad();
                            mOmAdSafe.reportImpress();
                            if (isShowCloseBn && mBannerView != null && mBannerView.getCloseView() != null) {
                                mOmAdSafe.addFriendlyObstruction(mBannerView.getCloseView(), FriendlyObstructionPurpose.CLOSE_AD, "close");
                            }
                        }
                    }
                } catch (Exception e) {
                    AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
                }

                if (mViewListener != null) {
                    mViewListener.onViewShow();
                }

                //自动刷新
                handlerRefresh(false);
            }

            @Override
            public void onViewClose() {
                bnClose();
            }
        });
    }

    private void addVideoListener(final AlxVideoVastBean video) {
        if (mBannerView == null) {
            return;
        }
        mBannerView.setEventListener(new AlxBannerViewVideoListener() {

            private boolean isCanClickRefresh = false; //是否可以点击刷新【视频播放完成或视频播放失败时点击可以刷新】

            @Override
            public void onVideoPlayCompletion() {
                if (video != null) {
                    AlxReportManager.reportUrl(video.completeList, mUIData, "play-complete");
                }
                if (mOmAdSafe != null) {
                    mOmAdSafe.reportVideoComplete();
                }
                isCanClickRefresh = true;
                handlerRefresh(false);
            }

            @Override
            public void onVideoPlayPause() {
                cancelHandlerRefresh();
            }

            @Override
            public void onVideoPlayStart() {
                cancelHandlerRefresh();
            }

            @Override
            public void onVideoPlayStop() {
                handlerRefresh(false);
            }

            @Override
            public void onVideoMute(boolean mute) {
                if (video != null) {
                    if (mute) {
                        AlxReportManager.reportUrl(video.muteList, mUIData, "mute");
                    } else {
                        AlxReportManager.reportUrl(video.unmuteList, mUIData, "unmute");
                    }
                }
            }

            @Override
            public void onVideoPlayFailed(int errCode, String errMsg) {
                if (video != null) {
                    try {
                        String vastErrorCode = String.valueOf(AlxReportManager.exchangeVideoVastErrorCode(errCode));
                        List<String> list = AlxReportManager.replaceUrlPlaceholder(video.errorList, AlxReportPlaceHolder.VIDEO_VAST_ERROR, vastErrorCode);
                        AlxReportManager.reportUrl(list, mUIData, "play-error");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cancelHandlerRefresh();
                isCanClickRefresh = true;
            }

            private AlxNativeMediaUIStatus getNativeMediaUIStatus() {
                AlxNativeMediaUIStatus status = null;
                if (mUIData != null && mUIData.getVideoUIStatus() != null) {
                    status = mUIData.getVideoUIStatus();
                }
                return status;
            }

            @Override
            public void onVideoPlayProgress(int progress) {
                if (video != null) {
                    AlxNativeMediaUIStatus status = null;
                    switch (progress) {
                        case 25:
                            status = getNativeMediaUIStatus();
                            if (status != null) { //防止重复上报
                                if (status.isVideoHasReportQuarter()) {
                                    AlxLog.d(AlxLogLevel.MARK, TAG, "report repeat: play-0.25");
                                    return;
                                } else {
                                    status.setVideoHasReportQuarter(true);
                                }
                            }

                            AlxReportManager.reportUrl(video.firstQuartileList, mUIData, "play-0.25");
                            if (mOmAdSafe != null) {
                                mOmAdSafe.reportVideoFirstQuartile();
                            }
                            break;
                        case 50:
                            status = getNativeMediaUIStatus();
                            if (status != null) { //防止重复上报
                                if (status.isVideoHasReportHalf()) {
                                    AlxLog.d(AlxLogLevel.MARK, TAG, "report repeat: play-0.5");
                                    return;
                                } else {
                                    status.setVideoHasReportHalf(true);
                                }
                            }

                            AlxReportManager.reportUrl(video.midPointList, mUIData, "play-0.5");
                            if (mOmAdSafe != null) {
                                mOmAdSafe.reportVideoMidpoint();
                            }
                            break;
                        case 75:
                            status = getNativeMediaUIStatus();
                            if (status != null) { //防止重复上报
                                if (status.isVideoHasReportThreeFourths()) {
                                    AlxLog.d(AlxLogLevel.MARK, TAG, "report repeat: play-0.75");
                                    return;
                                } else {
                                    status.setVideoHasReportThreeFourths(true);
                                }
                            }

                            AlxReportManager.reportUrl(video.thirdQuartileList, mUIData, "play-0.75");
                            if (mOmAdSafe != null) {
                                mOmAdSafe.reportVideoThirdQuartile();
                            }
                            break;
                    }
                }
            }

            @Override
            public void onViewClick(String url) {
                //点击上报
                if (video != null) {
                    AlxReportManager.reportUrl(video.clickTrackingList, mUIData, AlxReportManager.LOG_TAG_CLICK);
                }
                omidReport(OMID_EVENT_TYPE_CLICK);
                clickEvent(url);
                if (mViewListener != null) {
                    mViewListener.onViewClick();
                }

                //点击后重新加载
                if (isCanClickRefresh) {
                    handlerRefresh(true);
                }
            }

            @Override
            public void onViewShow() {
                //曝光上报
                if (video != null) {
                    AlxReportManager.reportUrl(video.impressList, mUIData, AlxReportManager.LOG_TAG_SHOW);
                }

                if (mOmAdSafe != null) {
                    try {
                        mOmAdSafe.registerAdView(mBannerView);
                        if (video != null && video.extField != null) {
                            mOmAdSafe.reportLoad(video.extField.isSkip(), video.extField.skipafter, video.extField.isMute());
                        } else {
                            mOmAdSafe.reportLoad();
                        }
                        mOmAdSafe.reportImpress();
                        if (isShowCloseBn && mBannerView != null && mBannerView.getCloseView() != null) {
                            mOmAdSafe.addFriendlyObstruction(mBannerView.getCloseView(), FriendlyObstructionPurpose.CLOSE_AD, "close");
                        }
                        omidReport(OMID_EVENT_TYPE_START);
                    } catch (Exception e) {
                        AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                    }
                }

                if (mViewListener != null) {
                    mViewListener.onViewShow();
                }
            }

            @Override
            public void onViewClose() {
                bnClose();
            }


            private final int OMID_EVENT_TYPE_START = 10;
            private final int OMID_EVENT_TYPE_PAUSE = 11;
            private final int OMID_EVENT_TYPE_RESUME = 12;
            private final int OMID_EVENT_TYPE_BUFFER_START = 13;
            private final int OMID_EVENT_TYPE_BUFFER_END = 14;
            private final int OMID_EVENT_TYPE_CLICK = 15;

            private void omidReport(int eventType) {
                if (mOmAdSafe == null) {
                    return;
                }
                try {
                    if (eventType == OMID_EVENT_TYPE_START) {
                        if (mBannerView != null && mBannerView instanceof AlxBannerVideoView) {
                            AlxBannerVideoView view = (AlxBannerVideoView) mBannerView;
                            AlxVideoPlayerView videoView = view.getVideoView();
                            if (videoView != null) {
                                mOmAdSafe.reportVideoStart(videoView.getDuration(), videoView.isMute());
                            }
                        }
                    } else if (eventType == OMID_EVENT_TYPE_PAUSE) {
                        mOmAdSafe.reportVideoPause();
                    } else if (eventType == OMID_EVENT_TYPE_RESUME) {
                        mOmAdSafe.reportVideoResume();
                    } else if (eventType == OMID_EVENT_TYPE_BUFFER_START) {
                        mOmAdSafe.reportVideoBufferStart();
                    } else if (eventType == OMID_EVENT_TYPE_BUFFER_END) {
                        mOmAdSafe.reportVideoBufferEnd();
                    } else if (eventType == OMID_EVENT_TYPE_CLICK) {
                        mOmAdSafe.reportVideoClick();
                    }
                } catch (Exception e) {
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
            }

        });
    }

    private void clickEvent(String url) {
        AlxLog.d(AlxLogLevel.MARK, TAG, "webClickEvent:" + url);
        if (TextUtils.isEmpty(url) || mUIData == null) {
            return;
        }
        try {
            AlxClickJump.openLink(mContext, mUIData.deeplink, url, mUIData.bundle, mTracker, new AlxJumpCallback() {
                @Override
                public void onDeeplinkCallback(boolean isSuccess, String error) {
                    if (mUIData == null) {
                        return;
                    }
                    try {
                        if (isSuccess) {
                            AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link(Deeplink) open is true");
                            AlxSdkData.tracker(mTracker, AlxSdkDataEvent.DEEPLINK_YES);
                        } else {
                            AlxLog.d(AlxLogLevel.MARK, TAG, "Deeplink Open Failed: " + error);
                            AlxSdkData.tracker(mTracker, AlxSdkDataEvent.DEEPLINK_NO);
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
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    protected void onDestroy() {
        try {
            sdkReportClose();
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
            removeAllViews();
            if (mOmAdSafe != null) {
                mOmAdSafe.destroy();
            }
            if (mBannerView != null) {
                mBannerView.onDestroy();
            }
            if (mController != null) {
                mController.destroy();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        mBannerView = null;
    }

    private void sdkReportClose() {
        try {
            if (mBannerView != null && mBannerView.getCurrentViewType() == AlxBaseBannerView.VIEW_TYPE_WEBVIEW) {
                AlxSdkData.tracker(mTracker, AlxSdkDataEvent.WEBVIEW_AD_CLOSE);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    public void bnClose() {
        cancelHandlerRefresh();
        try {
            onDestroy();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        if (mViewListener != null) {
            mViewListener.onViewClose();
        }
    }

    /**
     * 针对视频
     */
    protected void onPause() {
        if (mBannerView != null) {
            mBannerView.onPause();
        }
    }

    /**
     * 针对视频
     */
    protected void onResume() {
        if (mBannerView != null) {
            mBannerView.onResume();
        }
    }

    @Override
    public void onViewVisible() {
        isViewHidden = false;
        if (isViewVisible) {
            return;
        }
        isViewVisible = true;
        AlxLog.e(AlxLogLevel.MARK, TAG, "onViewVisible");
        showAdUI();
    }

    @Override
    public void onViewHidden() {
        isViewVisible = false;
        if (isViewHidden) { //防止重复调用
            return;
        }
        isViewHidden = true;
        AlxLog.e(AlxLogLevel.MARK, TAG, "onViewHidden");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AlxLog.i(AlxLogLevel.MARK, TAG, "onAttachedToWindow");
        if (mBus != null) {
            mBus.viewAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AlxLog.i(AlxLogLevel.MARK, TAG, "onDetachedFromWindow");
        if (mBus != null) {
            mBus.viewDetachedFromWindow();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        AlxLog.i(AlxLogLevel.MARK, TAG, "onVisibilityChanged:" + visibility);
        if (mBus != null) {
            mBus.viewVisibilityChanged();
        }
    }

    /**
     * View是否可见
     *
     * @return
     */
    private boolean isViewVisible() {
        if (mBus == null) {
            return false;
        }
        try {
            return mBus.isViewVisible();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return false;
    }

    private AlxOmidBean getOmidBean() {
        try {
            if (mUIData == null || mUIData.video == null) {
                return null;
            }
            return mUIData.video.omid;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
    }

    /**
     * 获取广告View的展示大小
     * 取值方式：Math.min(后台数据的width,屏幕宽度),Math.min(后台数据的height,屏幕高度)
     *
     * @param adWidth
     * @param adHeight
     * @return
     */
    private int[] getAdViewShowSize(int adWidth, int adHeight) {
        int[] result = new int[]{0, 0};
        try {
            int adWidthPx = AlxUtil.dip2px(mContext, adWidth);
            int adHeightPx = AlxUtil.dip2px(mContext, adHeight);

            int[] size = getCurrentViewSize();
            int viewWidth = size[0];
            int viewHeight = size[1];

            result[0] = Math.min(adWidthPx, viewWidth);
            result[1] = Math.min(adHeightPx, viewHeight);
            AlxLog.d(AlxLogLevel.DATA, TAG, "getAdViewShowSize:" + result[0] + ";" + result[1] + "===" + viewWidth + ";" + viewHeight);
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return result;
    }

    /**
     * 获取当前View的大小
     *
     * @return
     */
    private int[] getCurrentViewSize() {
        int[] result = new int[]{0, 0};
        try {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int width = 0;
            int height = 0;

            //1：先获取当前View的大小
            width = getLayoutParams().width;
            height = getLayoutParams().height;

            if (width > 0) {
                width = width - getPaddingLeft() - getPaddingRight();
            } else if (width == ViewGroup.LayoutParams.MATCH_PARENT) {
                width = getWidth() - getPaddingLeft() - getPaddingRight();
            } else {
                //2:获取父View的大小
                width = getParentViewSize(true);
            }
            if (width < 1) {
                //3: 取屏幕大小
                width = dm.widthPixels;
            }

            if (height > 0) {
                height = height - getPaddingTop() - getPaddingBottom();
            } else if (width == ViewGroup.LayoutParams.MATCH_PARENT) {
                height = getHeight() - getPaddingTop() - getPaddingBottom();
            } else {
                //2:获取父View的大小
                height = getParentViewSize(false);
            }
            if (height < 1) {
                //3：取屏幕大小
                height = dm.heightPixels;
            }
            result[0] = width;
            result[1] = height;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return result;
    }

    /**
     * 获取父容器的大小
     *
     * @param isWidth true表示获取宽度的大小，false表示获取高度的大小
     * @return
     */
    private int getParentViewSize(boolean isWidth) {
        try {
            if (!(getParent() instanceof ViewGroup)) {
                return 0;
            }
            ViewGroup view = (ViewGroup) getParent();
            if (isWidth) {
                int size = view.getLayoutParams().width;
                if (size > 0) {
                    return size - view.getPaddingLeft() - view.getPaddingRight();
                }
                if (size == ViewGroup.LayoutParams.MATCH_PARENT) { //其他情况不好说，有可能是子View搞大的，如果是View弄大了就不要取，考虑自动刷新大小不固定
                    size = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
                }
                return size;
            } else {
                int size = view.getLayoutParams().height;
                if (size > 0) {
                    return size - view.getPaddingTop() - view.getPaddingBottom();
                }
                if (size == ViewGroup.LayoutParams.MATCH_PARENT) {
                    size = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom();
                }
                return size;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return 0;
    }

}