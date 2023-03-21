package com.alxad.view.banner;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.base.AlxAdNetwork;
import com.alxad.base.AlxLogLevel;
import com.alxad.config.AlxConfig;
import com.alxad.entity.AlxBannerUIData;
import com.alxad.entity.AlxNativeMediaUIStatus;
import com.alxad.entity.AlxVideoExtBean;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.glittle.Glittle;
import com.alxad.glittle.target.CustomViewTarget;
import com.alxad.http.AlxHttpUtil;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxVideoDecoder;
import com.alxad.widget.AlxLogoView;
import com.alxad.widget.video.AlxVideoListener;
import com.alxad.widget.video.AlxVideoPlayerView;

import java.io.File;
import java.util.List;

public class AlxBannerVideoView extends AlxBaseBannerView implements View.OnClickListener {
    private static final String TAG = "AlxBannerVideoView";

    private final boolean VIDEO_MUTE = true; //默认设置是否静音

    private Context mContext;
    private AlxLogoView mLogoView;
    private ImageView mCloseView;

    private AlxVideoPlayerView mVideoView;
    private TextView mTimerView;
    private ImageView mVoiceView;
    private ImageView mCoverView;//封面View和落地页View

    private Handler mUiHandler;

    private AlxBannerUIData mUIData;
    private AlxVideoVastBean mAdObj;//广告对象

    private boolean isViewVisible = false;
    private boolean isViewHidden = false;

    public AlxBannerVideoView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AlxBannerVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AlxBannerVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mUiHandler = new Handler(mContext.getMainLooper());
        LayoutInflater.from(context).inflate(R.layout.alx_banner_video, this, true);

        mLogoView = (AlxLogoView) findViewById(R.id.alx_logo);
        mCloseView = (ImageView) findViewById(R.id.alx_close);

        mVideoView = (AlxVideoPlayerView) findViewById(R.id.alx_video_view);
        mVoiceView = (ImageView) findViewById(R.id.alx_voice);
        mTimerView = (TextView) findViewById(R.id.alx_video_time);

        mCoverView = (ImageView) findViewById(R.id.alx_cover);

//        resetVariable();
        mCoverView.setVisibility(View.VISIBLE);
        uiVideoShow(false);
        uiCloseShow(false);
        setVisibility(View.GONE);

        mCoverView.setOnClickListener(this);
        mVoiceView.setOnClickListener(this);
        mCloseView.setOnClickListener(this);
        mVideoView.setOnClickListener(this);
    }

    @Override
    public void renderAd(AlxBannerUIData bean, int imageWidth, int imageHeight) {
        if (imageWidth < 100 || imageHeight < 50) {
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, "video width and height is empty");
            }
            return;
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mCoverView.getLayoutParams();
        params.width = imageWidth;
        params.height = imageHeight;
        mCoverView.setLayoutParams(params);

        mCoverView.setVisibility(View.VISIBLE);
        setVisibility(View.VISIBLE);
        mUIData = bean;
        if (isViewVisible()) {
            renderAd();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mUIData != null && mUIData.getVideoUIStatus() != null) {
            AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
            status.setHasRenderData(false);
        }
        try {
            mUiHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "onDetachedFromWindow:" + e.getMessage());
        }
    }

    @Override
    public void onViewVisible() {
        if (mUIData != null) {
            isViewHidden = false;
            if (isViewVisible) {
                return;
            }
            isViewVisible = true;
            if (!renderAd()) {//如果此处已经开始播放了，就不要执行下面的来重复播放了
                onResume();
            }
        }
    }

    @Override
    public void onViewHidden() {
        if (mUIData != null) {
            isViewVisible = false;
            if (isViewHidden) { //防止重复调用
                return;
            }
            isViewHidden = true;
            onPause();
        }
    }

    /**
     * 广告开始曝光
     */
    private boolean renderAd() {
        if (mUIData == null) {
            return false;
        }

        AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
        if (status == null) {
            status = new AlxNativeMediaUIStatus();
            mUIData.setVideoUIStatus(status);
        }
        if (status.isHasRenderData()) {
            return false;
        }
        status.setHasRenderData(true);

        AlxLog.d(AlxLogLevel.MARK, TAG, "renderAd()");

        if (mUIData.video == null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "renderAd(): bean is null");
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, "data is empty");
            }
            return false;
        }

        mAdObj = mUIData.video;
        if (status.isVideoPlayComplete() || status.isVideoPlayError()) {
            uiVideoShow(false);
            status.setVideoPlayVideo(false);
        } else {
            uiVideoShow(true);
            status.setVideoPlayVideo(true);
            startPlay(mAdObj);
        }
        showImgViewUI();
        return true;
    }


    public void onPause() {
        if (mUIData != null && mUIData.getVideoUIStatus() != null && mUIData.getVideoUIStatus().isVideoPlayVideo()) {
            AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
            if (status.isVideoPlayComplete() || status.isVideoPlayError()) {
                return;
            }
            if (mVideoView != null) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "onPause");
                mVideoView.pause();
            }
        }
    }

    public void onResume() {
        if (mUIData != null && mUIData.getVideoUIStatus() != null && mUIData.getVideoUIStatus().isVideoPlayVideo()) {
            AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
            if (status.isVideoPlayComplete() || status.isVideoPlayError()) {
                return;
            }
            if (mVideoView != null) {
                if (status.getVideoCurrentPosition() >= 0) {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onResume:" + status.getVideoCurrentPosition());
                    startPlay(mAdObj);//重新设置：如果缓存文件下载完成可以播放本地缓存
                } else {
                    AlxLog.d(AlxLogLevel.MARK, TAG, "onResume");
                    mVideoView.onResume();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        try {
            if (mVideoView != null) {
                mVideoView.onDestroy();
            }
            if (mUIData != null && mUIData.getVideoUIStatus() != null) {
                AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
                status.destroy();
            }

            mUIData = null;
            mAdObj = null;

            if (mUiHandler != null) {
                //清空handle消息  避免内存泄露
                mUiHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            e.printStackTrace();
        }
    }


    private void uiVideoShow(boolean isShow) {
        if (mVideoView == null || mVoiceView == null || mTimerView == null) {
            return;
        }
        if (isShow) {
            mVideoView.setVisibility(View.VISIBLE);
            mVoiceView.setVisibility(View.VISIBLE);
            mTimerView.setVisibility(View.VISIBLE);
        } else {
            mVideoView.setVisibility(View.GONE);
            mVoiceView.setVisibility(View.GONE);
            mTimerView.setVisibility(View.GONE);
        }
    }

    private void uiCloseShow(boolean isShow) {
        if (mLogoView == null || mCloseView == null) {
            return;
        }
        if (isShow) {
            mLogoView.setVisibility(View.VISIBLE);
            if (isShowCloseBn) {
                mCloseView.setVisibility(View.VISIBLE);
            } else {
                mCloseView.setVisibility(View.GONE);
            }
        } else {
            mLogoView.setVisibility(View.GONE);
            mCloseView.setVisibility(View.GONE);
        }
    }

    private void startPlay(AlxVideoVastBean bean) {
        if (bean == null || mUIData == null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "startPlay: bean is null");
            return;
        }
        AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
        if (status != null) {
            if (status.isVideoPlayComplete() || status.isVideoPlayError()) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "startPlay: Played it once");
                return;
            }
        }
        String path = null;
        boolean isNetwork = false;
        try {
            String videoFileName = AlxHttpUtil.getDownloadFileName(bean.videoUrl);
            File videoFile = new File(AlxFileUtil.getVideoSavePath(mContext) + videoFileName);
            if (videoFile.exists()) {
                isNetwork = false;
                path = videoFile.getPath();
            } else {
                isNetwork = true;
                path = bean.videoUrl;
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(path)) {
            AlxLog.e(AlxLogLevel.MARK, TAG, "showVideoPlayer():url is empty");
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, "url is empty");
            }
            return;
        }

        boolean isMute = VIDEO_MUTE;
        AlxVideoExtBean videoExtBean = bean.extField;
        if (videoExtBean != null) {
            isMute = videoExtBean.isMute();
        }

        if (status != null) {
            if (status.getVideoMute() != null) {
                isMute = status.getVideoMute().booleanValue();
            } else {
                status.setVideoMute(new Boolean(isMute));
            }
        }

        if (isMute) {
            mVoiceView.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_off));
        } else {
            mVoiceView.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_on));
        }

        try {
            mVideoView.setUp(path, mAlxVideoListener, new AlxVideoPlayerView.Builder()
                    .setNeedPlayUI(false)
                    .setMute(isMute)
                    .setNeedProgressUI(true)
                    .setNeedCoverUI(false));
            if (status != null) {
                mVideoView.start(status.getVideoCurrentPosition());
            } else {
                mVideoView.start();
            }
            if (isNetwork) {
                //开始计算是否超时
                postDelayedVideoTimeOut();
                AlxLog.d(AlxLogLevel.OPEN, TAG, "播放在线视频    展示loading弹窗，请等待...");
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void postDelayedVideoTimeOut() {
        if (mUiHandler == null) {
            return;
        }
        AlxLog.d(AlxLogLevel.MARK, TAG, "视频缓冲中，埋入延时操作");
        mUiHandler.removeCallbacksAndMessages(null);
        //定时器，如果10秒播放器还没有prepare就播放错误
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "视频缓冲缓冲超时，执行延时操作，超时时间为" + AlxConfig.VIDEO_LOADING_TIMEOUT + "毫秒");
                releaseUI();
                doError("video loading timeout");
            }
        }, AlxConfig.VIDEO_LOADING_TIMEOUT);
    }


    @Override
    public View getCloseView() {
        return mCloseView;
    }

    private void releaseUI() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "releaseUI");
        try {
            if (mVideoView != null) {
                mVideoView.release();
            }
            mTimerView.setVisibility(View.GONE);
            mVoiceView.setVisibility(View.GONE);
            mCoverView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            uiCloseShow(true);
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayStop();
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }


    public void doError(String error) {
        if (mUIData != null && mUIData.getVideoUIStatus() != null) {
            AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();

            if (!status.isVideoPlayError()) {
                status.setVideoPlayError(true);
                if (mListener instanceof AlxBannerViewVideoListener) {
                    AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                    listener.onVideoPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, error);
                }
            }
        }
    }

    //清空本地缓存
    private void clearCacheData() {
        try {
            if (mAdObj != null) {
                String videoFileName = AlxHttpUtil.getDownloadFileName(mAdObj.videoUrl);
                File videoFile = new File(AlxFileUtil.getVideoSavePath(mContext) + videoFileName);
                if (videoFile.exists()) {
                    videoFile.delete();
                }
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    /**
     * 落地页图片
     * 1：先取落地页本地图片
     * 2：1获取失败，取落地页网络图片
     * 3：2获取失败，取视频的第一帧图片
     */
    private void showImgViewUI() {
        if (mAdObj == null) {
            return;
        }
        try {
            if (!TextUtils.isEmpty(mAdObj.landUrl)) {
                AlxLog.i(AlxLogLevel.MARK, TAG, "showImgViewUI:landUrl");
                loadImgDrawable(mAdObj.landUrl);
            } else {
                AlxLog.i(AlxLogLevel.MARK, TAG, "showImgViewUI:videoFrame");
                videoConvertBitmap();
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void loadImgDrawable(String url) throws Exception {
        Glittle.with(mContext).load(url).into(new CustomViewTarget<ImageView, Drawable>(mCoverView) {


            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                AlxLog.i(AlxLogLevel.ERROR, TAG, "showImgViewUI:fail");
                videoConvertBitmap();
            }

            @Override
            public void onResourceReady(@NonNull Drawable drawable) {
                AlxLog.i(AlxLogLevel.MARK, TAG, "showImgViewUI:ok");
                if (mCoverView == null) {
                    return;
                }
                try {
                    if (drawable != null) {
                        mCoverView.setImageDrawable(drawable);
                    } else {
                        videoConvertBitmap();
                    }
                } catch (Exception e) {
                    AlxAgent.onError(e);
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    //获取视频封面图
    private void videoConvertBitmap() {
        if (mAdObj == null) {
            return;
        }
        try {
            String videoFileName = AlxHttpUtil.getDownloadFileName(mAdObj.videoUrl);
            File cacheVideoFile = new File(AlxFileUtil.getVideoSavePath(mContext) + videoFileName);
            String coverUrl;
            if (cacheVideoFile.exists()) {
                coverUrl = cacheVideoFile.getPath();
            } else {
                coverUrl = mAdObj.videoUrl;
            }
            //获取视频关键帧封面
            final String frameSource = coverUrl;
            if (!TextUtils.isEmpty(frameSource)) {
                AlxAdNetwork.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (mCoverView == null) {
                                return;
                            }
                            final Bitmap bitmap = AlxVideoDecoder.getVideoFrame(frameSource, mCoverView.getWidth(), mCoverView.getHeight());
//                            final Bitmap bitmap = AlxUtil.getNetVideoBitmap(frameSource);
//                            if (bitmap != null && !isFinishing() && !isActivityDestroy) {
                            if (bitmap != null) {
                                post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (bitmap != null && mCoverView != null) {
                                                mCoverView.setImageBitmap(bitmap);
                                            }
                                        } catch (Exception e) {
                                            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                                        }
                                    }
                                });
                            }
                        } catch (Throwable e) {
                            AlxAgent.onError(e);
                            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                        }
                    }
                });
            }
        } catch (Throwable ex) {
            AlxAgent.onError(ex);
            AlxLog.e(AlxLogLevel.ERROR, TAG, ex.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_voice) {
            AlxNativeMediaUIStatus status = null;
            if (mUIData != null) {
                status = mUIData.getVideoUIStatus();
            }

            if (mVideoView != null) {
                if (!mVideoView.isMute()) {
                    mVideoView.setMute(true);
                    if (status != null) {
                        status.setVideoMute(Boolean.TRUE);
                    }
                    mVoiceView.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_off));
                    if (mListener instanceof AlxBannerViewVideoListener) {
                        AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                        listener.onVideoMute(true);
                    }
                } else {
                    mVideoView.setMute(false);
                    if (status != null) {
                        status.setVideoMute(Boolean.FALSE);
                    }
                    mVoiceView.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_on));
                    if (mListener instanceof AlxBannerViewVideoListener) {
                        AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                        listener.onVideoMute(false);
                    }
                }
            }
        } else if (v.getId() == R.id.alx_close) {
            onDestroy();
            if (mListener != null) {
                mListener.onViewClose();
            }
        } else if (v.getId() == R.id.alx_video_view || v.getId() == R.id.alx_img) {
            adClickAction();
        }
    }

    /**
     * 广告点击跳转：
     * 优先deeplink
     */
    private void adClickAction() {
        String url = null;
        if (mAdObj != null) {
            List<String> urls = mAdObj.clickThroughList;
            if (urls != null && urls.size() > 0) {
                url = urls.get(0);
            }
        }
        AlxLog.d(AlxLogLevel.MARK, TAG, "Click Url: " + url);
        if (mListener != null) {
            mListener.onViewClick(url);
        }
    }

    private AlxVideoListener mAlxVideoListener = new AlxVideoListener() {

        @Override
        public void onVideoBufferStart() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoBufferStart");
            //开始计算是否超时
            postDelayedVideoTimeOut();
        }

        @Override
        public void onVideoBufferEnd() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoBufferEnd");
            try {
                mUiHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onBufferingEnd:" + e.getMessage());
            }
        }

        @Override
        public void onVideoRenderingStart() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoRenderingStart");
            AlxNativeMediaUIStatus status = null;
            if (mUIData != null) {
                status = mUIData.getVideoUIStatus();
                if (status != null) {
                    status.setVideoCurrentPosition(-1);
                }
            }

            try {
                mCoverView.setVisibility(View.GONE);
                uiVideoShow(true);
                mUiHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onVideoRenderingStart:" + e.getMessage());
            }

            if (status != null) {
                if (!status.isVideoImpression()) {
                    status.setVideoImpression(true);
                    if (mListener != null) {
                        mListener.onViewShow();
                    }
                }
            }
        }

        @Override
        public void onVideoError(String error) {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoError:" + error);
            saveCurrentPosition(-1);
            releaseUI();
            doError(error);
            clearCacheData();
        }

        @Override
        public void onVideoPause() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoPause");
            try {
                mUiHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onVideoPause:" + e.getMessage());
            }
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayPause();
            }
        }

        @Override
        public void onVideoStart() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoStart");
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayStart();
            }
        }

        @Override
        public void onVideoCompletion() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoCompletion");
            if (mUIData != null) {
                AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
                if (status != null) {
                    status.setVideoCurrentPosition(-1);
                    status.setVideoPlayComplete(true);
                }
            }
            releaseUI();
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayCompletion();
            }
        }

        @Override
        public void onVideoSize(int width, int height) {

        }

        @Override
        public void onVideoPlayProgress(int progress) {
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayProgress(progress);
            }
        }

        @Override
        public void onVideoPlayTime(int playTime, int totalTime) {
            String countDown = (totalTime - playTime) + "";//倒计时间
            try {
                mTimerView.setVisibility(View.VISIBLE);
                mTimerView.setText(countDown);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onVideoSaveInstanceState(int currentPosition) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "onSaveInstanceState:" + currentPosition);
            saveCurrentPosition(currentPosition);
        }


    };

    private void saveCurrentPosition(int position) {
        if (mUIData != null) {
            AlxNativeMediaUIStatus status = mUIData.getVideoUIStatus();
            if (status != null) {
                status.setVideoCurrentPosition(position);
            }
        }
    }

    @Override
    public int getCurrentViewType() {
        return VIEW_TYPE_VIDEO;
    }

    public AlxVideoPlayerView getVideoView() {
        return mVideoView;
    }

}