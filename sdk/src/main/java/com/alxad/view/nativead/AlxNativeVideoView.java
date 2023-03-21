package com.alxad.view.nativead;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.api.nativead.AlxMediaContent;
import com.alxad.api.nativead.AlxMediaView;
import com.alxad.base.AlxAdNetwork;
import com.alxad.base.AlxLogLevel;
import com.alxad.config.AlxConfig;
import com.alxad.control.nativead.AlxMediaContentImpl;
import com.alxad.entity.AlxNativeMediaUIStatus;
import com.alxad.entity.AlxNativeUIData;
import com.alxad.entity.AlxVideoExtBean;
import com.alxad.entity.AlxVideoVastBean;
import com.alxad.glittle.Glittle;
import com.alxad.glittle.target.CustomViewTarget;
import com.alxad.http.AlxHttpUtil;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxUtil;
import com.alxad.util.AlxVideoDecoder;
import com.alxad.view.banner.AlxBannerViewVideoListener;
import com.alxad.widget.video.AlxVideoListener;
import com.alxad.widget.video.AlxVideoPlayerView;

import java.io.File;
import java.util.List;

/**
 * 原生广告: 媒体View-视频
 *
 * @author lwl
 * @date 2022-9-14
 */
public class AlxNativeVideoView extends AlxBaseNativeMediaView implements View.OnClickListener {
    private static final String TAG = "AlxNativeVideoView";

    private final boolean VIDEO_MUTE = true; //默认设置是否静音

    private AlxVideoPlayerView mVideoView;
    private ImageView mVoiceView;
    private ImageView mCoverView;//封面View和落地页View

    private Context mContext;
    private Handler mUiHandler;

    private AlxMediaContentImpl mMediaContent;
    private AlxVideoVastBean mAdObj;//广告对象

    private boolean isViewVisible = false;
    private boolean isViewHidden = false;

    public AlxNativeVideoView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public AlxNativeVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AlxNativeVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AlxNativeVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mContext = context;
        mUiHandler = new Handler(mContext.getMainLooper());
        LayoutInflater.from(context).inflate(R.layout.alx_native_media_video, this, true);

        mVideoView = (AlxVideoPlayerView) findViewById(R.id.alx_native_video);
        mVoiceView = (ImageView) findViewById(R.id.alx_native_video_voice);
        mCoverView = (ImageView) findViewById(R.id.alx_native_video_cover);

        uiVideoShow(false);
        mCoverView.setVisibility(View.VISIBLE);

        mCoverView.setOnClickListener(this);
        mVoiceView.setOnClickListener(this);
        mVideoView.setOnClickListener(this);
    }

    public AlxMediaContent getMediaContent() {
        return mMediaContent;
    }

    public void setMediaContent(AlxMediaContent mediaContent) {
        if (mediaContent instanceof AlxMediaContentImpl) {
            AlxMediaContentImpl obj = (AlxMediaContentImpl) mediaContent;
            setViewSize(obj);
            mCoverView.setVisibility(View.VISIBLE);
            setVisibility(View.VISIBLE);
            mMediaContent = obj;
            if (isViewVisible()) {
                renderAd();
            }
        } else {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "setMediaContent: mediaContent is null");
        }
    }

    public void setImageScaleType(ImageView.ScaleType scaleType) {
        if (mCoverView != null) {
            mCoverView.setScaleType(scaleType);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mMediaContent != null && mMediaContent.getMediaUIStatus() != null) {
            AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
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
        if (mMediaContent != null) {
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
        if (mMediaContent != null) {
            isViewVisible = false;
            if (isViewHidden) { //防止重复调用
                return;
            }
            isViewHidden = true;
            onPause();
        }
    }

    private void setViewSize(AlxMediaContentImpl mediaContent) {
        if (mediaContent == null) {
            return;
        }
        try {
            AlxNativeUIData bean = mediaContent.getData();
            if (bean == null) {
                return;
            }

            int width = 0;
            int height = 0;
            AlxVideoVastBean videoBean = bean.video;
            if (videoBean != null) {
                width = AlxUtil.getInt(videoBean.videoWidth);
                height = AlxUtil.getInt(videoBean.videoHeight);
            }

            boolean isResetHeight = true;
            if (getParent() instanceof AlxMediaView) {
                AlxMediaView parentView = (AlxMediaView) getParent();
                if (parentView.getLayoutParams() != null) {
                    int parentHeight = parentView.getLayoutParams().height;
                    if (parentHeight == ViewGroup.LayoutParams.MATCH_PARENT || parentHeight > 0) {
                        isResetHeight = false;
                    }
                }
            }
            AlxLog.d(AlxLogLevel.MARK, TAG, "setViewSize(): isResetHeight=" + isResetHeight);
            if (getLayoutParams().height <= 0 && isResetHeight) {
                AlxLog.d(AlxLogLevel.MARK, TAG, "setViewSize(): set height=" + height);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mCoverView.getLayoutParams();
                params.height = height;
                mCoverView.setLayoutParams(params);

                params = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
                params.height = height;
                mVideoView.setLayoutParams(params);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    /**
     * 广告开始曝光
     */
    private boolean renderAd() {
        if (mMediaContent == null) {
            return false;
        }

        AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
        if (status == null) {
            status = new AlxNativeMediaUIStatus();
            mMediaContent.setMediaUIStatus(status);
        }
        if (status.isHasRenderData()) {
            return false;
        }
        status.setHasRenderData(true);

        AlxLog.d(AlxLogLevel.MARK, TAG, "renderAd()");
        AlxNativeUIData bean = mMediaContent.getData();
        if (bean == null || bean.video == null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "renderAd(): bean is null");
            if (mListener instanceof AlxBannerViewVideoListener) {
                AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
                listener.onVideoPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, "data is empty");
            }
            return false;
        }

        mAdObj = bean.video;
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_native_video_voice) {

            AlxNativeMediaUIStatus status = null;
            if (mMediaContent != null) {
                status = mMediaContent.getMediaUIStatus();
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
        } else if (v.getId() == R.id.alx_native_video || v.getId() == R.id.alx_native_video_cover) {
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

    private void uiVideoShow(boolean isShow) {
        if (mVideoView == null || mVoiceView == null) {
            return;
        }
        if (isShow) {
            mVideoView.setVisibility(View.VISIBLE);
            mVoiceView.setVisibility(View.VISIBLE);
        } else {
            mVideoView.setVisibility(View.GONE);
            mVoiceView.setVisibility(View.GONE);
        }
    }

    public void onPause() {
        if (mMediaContent != null && mMediaContent.getMediaUIStatus() != null && mMediaContent.getMediaUIStatus().isVideoPlayVideo()) {
            AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
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
        if (mMediaContent != null && mMediaContent.getMediaUIStatus() != null && mMediaContent.getMediaUIStatus().isVideoPlayVideo()) {
            AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
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
            if (mMediaContent != null && mMediaContent.getMediaUIStatus() != null) {
                AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
                status.destroy();
            }

            mMediaContent = null;
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

    private void startPlay(AlxVideoVastBean bean) {
        if (bean == null || mMediaContent == null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "startPlay: bean is null");
            return;
        }
        AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
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

        if (mListener instanceof AlxBannerViewVideoListener) {
            AlxBannerViewVideoListener listener = (AlxBannerViewVideoListener) mListener;
            listener.onVideoMute(isMute);
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

    private void releaseUI() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "releaseUI");
        try {
            if (mVideoView != null) {
                mVideoView.release();
            }
            mCoverView.setVisibility(View.VISIBLE);
            uiVideoShow(false);
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
        if (mMediaContent != null && mMediaContent.getMediaUIStatus() != null) {
            AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();

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
            if (mMediaContent != null) {
                status = mMediaContent.getMediaUIStatus();
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
            if (mMediaContent != null) {
                AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
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
//            String countDown = (totalTime - playTime) + "";//倒计时间
//            try {
//                mTimerView.setVisibility(View.VISIBLE);
//                mTimerView.setText(countDown);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }

        @Override
        public void onVideoSaveInstanceState(int currentPosition) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "onSaveInstanceState:" + currentPosition);
            saveCurrentPosition(currentPosition);
        }

    };

    private void saveCurrentPosition(int position) {
        if (mMediaContent != null) {
            AlxNativeMediaUIStatus status = mMediaContent.getMediaUIStatus();
            if (status != null) {
                status.setVideoCurrentPosition(position);
            }
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

    public AlxVideoPlayerView getVideoView() {
        return mVideoView;
    }

}