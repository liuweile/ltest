package com.alxad.widget.video;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;
import com.alxad.widget.AlxTextureView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 视频播放布局文件【支持在同一个界面同时播放多个视频】
 *
 * @author lwl
 * @date 2022-10-9
 */
public class AlxVideoPlayerView extends FrameLayout {
    private static final String TAG = "AlxVideoPlayerView";

    //视频状态
    public static final int STATE_IDLE = -1; //空闲状态
    public static final int STATE_INIT = 0;// 初始配置(url地址)
    public static final int STATE_PREPARING = 1; //准备中
    public static final int STATE_PLAYING = 2; //正在播放
    public static final int STATE_PLAYING_BUFFERING_START = 3;//播放缓冲开始
    public static final int STATE_PAUSE = 4;//暂停
    public static final int STATE_PLAY_COMPLETE = 5;//播放完成
    public static final int STATE_ERROR = 6;//异常

    private final int PROGRESS_DEFAULT = -1;//当前进度的默认值
    private final int PLAY_TIME_DEFAULT = -1;//当前播放时长的默认值

    private Context mContext;
    private FrameLayout mVideoContainer;
    private ImageView mIvCover;//视频封面
    private ImageView mPlayView;//播放图标的UI
    private ProgressBar mProgressBar;
    public AlxTextureView mTextureView;
    private Surface mSurface;
    private SurfaceTexture mSurfaceTexture;

    private AlxVideoListener mVideoListener;
    private String mPath;
    private Timer mProgressTimer;
    private Handler mHandler;

    private int mCurrentState = STATE_IDLE;
    private int mCurrentProgress = PROGRESS_DEFAULT;
    private int mCurrentPlayTime = PLAY_TIME_DEFAULT;
    private boolean isMute = false; //是否是静音
    private boolean isPrepared = false;
    private int mSeekToDuration = -1;//设置跳转到指定的播放位置
    private boolean isAttachWindow = false;

    private boolean isProgressViewSwitch = true; //是否需要进度View
    private boolean isCoverViewSwitch = true; //是否需要封面View
    private boolean isPlayViewSwitch = false;//是否需要播放的View

    private final AlxVideoPlayerController mPlayerController = new AlxVideoPlayerController();

    public AlxVideoPlayerView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public AlxVideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public AlxVideoPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "initView");
        mContext = context;
        mHandler = new Handler(Looper.myLooper());
        View.inflate(context, R.layout.alx_video_player, this);
        mVideoContainer = (FrameLayout) findViewById(R.id.alx_video_container);
        mIvCover = (ImageView) findViewById(R.id.alx_video_cover);
        mPlayView = (ImageView) findViewById(R.id.alx_video_play);
        mProgressBar = (ProgressBar) findViewById(R.id.alx_video_progress);
        mProgressBar.setVisibility(View.GONE);

        if (isPlayViewSwitch) {
            mPlayView.setVisibility(View.VISIBLE);
        } else {
            mPlayView.setVisibility(View.GONE);
        }
    }

    /**
     * 是否使用进度条UI
     *
     * @param open
     */
    private void setNeedProgressUI(boolean open) {
        isProgressViewSwitch = open;
        if (!open) {
            mProgressBar.setVisibility(View.GONE);
            try {
                mProgressBar.clearAnimation();
            } catch (Exception e) {
                AlxAgent.onError(e);
                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            }
        }
    }

    /**
     * 是否使用封面图UI
     *
     * @param open
     */
    private void setNeedCoverUI(boolean open) {
        isCoverViewSwitch = open;
        if (!open) {
            mIvCover.setVisibility(View.GONE);
        }
    }

    /**
     * 是否使用播放UI
     *
     * @param open
     */
    private void setNeedPlayUI(boolean open) {
        isPlayViewSwitch = open;
        if (isPlayViewSwitch) {
            mPlayView.setVisibility(View.VISIBLE);
        } else {
            mPlayView.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化配置信息
     *
     * @param path     本地路径或网络url
     * @param listener
     */
    public void setUp(String path, AlxVideoListener listener) {
        this.setUp(path, listener, null);
    }

    /**
     * 初始化配置信息
     *
     * @param path     本地路径或网络url
     * @param listener
     * @param builder  设置配置信息
     */
    public void setUp(String path, AlxVideoListener listener, Builder builder) {
        this.mPath = path;
        mVideoListener = listener;
        initBuilder(builder);
        setUIState(STATE_INIT);
    }

    /**
     * 开始播放
     */
    public void start() {
        start(-1);
    }

    /**
     * 开始播放
     *
     * @param position 跳到指定的视频位置播放
     */
    public void start(int position) {
        mSeekToDuration = position;
        if (mCurrentState == STATE_IDLE && !TextUtils.isEmpty(mPath)) { //如果播放完成或播放异常，调用start实现重播
            setUIState(STATE_INIT);
        }
        switch (mCurrentState) {
            case STATE_INIT:
            case STATE_ERROR:
            case STATE_PLAY_COMPLETE:
                prepareMediaPlayer();
                break;
            case STATE_PAUSE:
                playOnResume();
                break;
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        playOnPause();
    }

    /**
     * Activity或Fragment 生命周期中onResume执行
     */
    public void onResume() {
        playOnResume();
    }

    public void onDestroy() {
        playOnDestroy();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AlxLog.i(AlxLogLevel.MARK, TAG, "onDetachedFromWindow");
        isAttachWindow = false;
        release();
    }

    private void playOnResume() {
        AlxLog.i(AlxLogLevel.MARK, TAG, "playOnResume: " + mCurrentState);
        if (mCurrentState == STATE_PAUSE && isPrepared) {
            mPlayerController.start(mSeekToDuration);
            setUIState(STATE_PLAYING);
            if (mVideoListener != null) {
                mVideoListener.onVideoStart();
            }
        }
    }

    private void playOnPause() {
        AlxLog.i(AlxLogLevel.MARK, TAG, "playOnPause: " + mCurrentState);
        if (mCurrentState == STATE_IDLE) {
            return;
        }
        if (mCurrentState == STATE_INIT ||
                mCurrentState == STATE_ERROR ||
                mCurrentState == STATE_PLAY_COMPLETE) {
            release();
        } else if (mCurrentState == STATE_PREPARING ||
                mCurrentState == STATE_PLAYING_BUFFERING_START ||
                mCurrentState == STATE_PLAYING) {
            mPlayerController.pause();
            setUIState(STATE_PAUSE);
            if (mVideoListener != null) {
                mVideoListener.onVideoPause();
            }
        }
    }

    private void playOnDestroy() {
        setUIState(STATE_IDLE);
        release();
        try {
            mHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, "playOnDestroy:error:" + e.getMessage());
        }
    }

    /**
     * 点击静音图标时，设置视频是否静音
     *
     * @param mute
     */
    public void setMute(boolean mute) {
        if (mPlayerController == null) {
            return;
        }
        if (mute) {
            boolean value = mPlayerController.setVolume(0, 0);
            if (value) {
                isMute = true;
            }
        } else {
            boolean value = mPlayerController.setVolume(1f, 1f);
            if (value) {
                isMute = false;
            }
        }
    }

    /**
     * 获取视频播放总时长
     *
     * @return
     */
    public int getDuration() {
        if (mPlayerController != null) {
            return mPlayerController.getDuration();
        }
        return 0;
    }

    /**
     * 获取视频封面的ImageView
     *
     * @return
     */
    public ImageView getVideoCoverView() {
        return mIvCover;
    }

    /**
     * 获取播放图标的ImageView
     *
     * @return
     */
    public ImageView getPlayView() {
        return mPlayView;
    }

    /**
     * 视频是否静音
     *
     * @return
     */
    public boolean isMute() {
        return isMute;
    }

    private void prepareMediaPlayer() {
        AlxLog.i(AlxLogLevel.MARK, TAG, "prepareMediaPlayer");
        release(); //如果MediaPlayer是在子线程中异步释放，下面的代码请延迟0.5s在添加
        setUIState(STATE_PREPARING);
        mPlayerController.setVideoUrl(mPath);
        mPlayerController.setPlayerControllerListener(mPlayerControllerListener);
        addTextureView();
        addKeepScreenOn();
    }

    private void addTextureView() {
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(null);
            ViewParent viewParent = mTextureView.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) viewParent;
                viewGroup.removeView(mTextureView);
            }
        }
        mTextureView = new AlxTextureView(mContext);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mVideoContainer.addView(mTextureView, params);
    }

    private void setUIState(int state) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "setUIState=" + state);
        mCurrentState = state;
        switch (state) {
            case STATE_IDLE:
                uiProgressBarShow(false);
                uiCoverViewShow(true);
                uiPlayViewShow(true);
                break;
            case STATE_INIT:
                cancelProgressTimer();
                uiProgressBarShow(false);
                uiCoverViewShow(true);
                uiPlayViewShow(true);
                break;
            case STATE_PREPARING:
                cancelProgressTimer();
                uiProgressBarShow(true);
                uiCoverViewShow(true);
                uiPlayViewShow(false);
                break;
            case STATE_PLAYING:
                uiPlayViewShow(false);
                break;
            case STATE_PLAYING_BUFFERING_START:
                uiProgressBarShow(true);
                uiPlayViewShow(false);
                break;
            case STATE_PAUSE:
                cancelProgressTimer();
                uiPlayViewShow(true);
                break;
            case STATE_PLAY_COMPLETE:
            case STATE_ERROR:
                cancelProgressTimer();
                uiProgressBarShow(false);
                uiCoverViewShow(true);
                uiPlayViewShow(true);
                break;
        }
    }

    private void uiProgressBarShow(boolean isShow) {
//        AlxLog.i(AlxLogLevel.MARK, TAG, "uiProgressBarShow:" + isShow);
        if (isProgressViewSwitch) {
            if (mProgressBar != null) {
                mProgressBar.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void uiCoverViewShow(boolean isShow) {
        if (isCoverViewSwitch) {
            if (mIvCover != null) {
                mIvCover.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void uiPlayViewShow(boolean isShow) {
        if (isPlayViewSwitch) {
            if (mPlayView != null) {
                mPlayView.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void release() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "release");

        //记录当前播放的位置
        int currentPosition = -1;
        int currentState = mCurrentState;
        if (mCurrentState == STATE_PREPARING || mCurrentState == STATE_PLAYING ||
                mCurrentState == STATE_PLAYING_BUFFERING_START || mCurrentState == STATE_PAUSE) {
            currentPosition = mPlayerController.getCurrentDuration();
        }

        if (mPlayerController != null) {
            mPlayerController.release();
        }
        //如果MediaPlayer是在子线程中异步释放，请延迟一段时间在释放，否则会有报系统错误警告
//        if (mSurface != null || mSurfaceTexture != null) {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    clearSurfaceResource();
//                }
//            }, 200);
//        }

        cancelProgressTimer();
        setUIState(STATE_IDLE);
        mCurrentProgress = PROGRESS_DEFAULT;
        mCurrentPlayTime = PLAY_TIME_DEFAULT;
        isPrepared = false;
        clearKeepScreenOn();

        try {
            if (mTextureView != null) {
//                mTextureView.setSurfaceTextureListener(null);
                mVideoContainer.removeView(mTextureView);
            }
            mVideoContainer.removeAllViews();
            mTextureView = null;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        clearSurfaceResource();

        if (currentPosition >= 0 && currentPosition >= mSeekToDuration &&
                currentState != STATE_ERROR && currentState != STATE_PLAY_COMPLETE &&
                currentState != STATE_INIT) {
            if (mVideoListener != null) {
                mVideoListener.onVideoSaveInstanceState(currentPosition);
            }
        }
    }

    private void clearSurfaceResource() {
        try {
            if (mSurface != null) {
                mSurface.release();
            }
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        } finally {
            mSurface = null;
            mSurfaceTexture = null;
        }
    }

    private void addKeepScreenOn() {
        try {
            if (mTextureView != null) {
                mTextureView.setKeepScreenOn(true);
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void clearKeepScreenOn() {
        try {
            if (mTextureView != null) {
                mTextureView.setKeepScreenOn(false);
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void startProgressTimer() {
        if (mProgressTimer != null) {
            mProgressTimer.cancel();
            mProgressTimer = null;
        }
        mProgressTimer = new Timer();
        mProgressTimer.schedule(new ProgressTimerTask(), 0, 500);
    }

    private void cancelProgressTimer() {
        if (mProgressTimer != null) {
            mProgressTimer.cancel();
            mProgressTimer = null;
        }
    }

    private class ProgressTimerTask extends TimerTask {

        @Override
        public void run() {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        addProgress();
                    }
                });
            }
        }
    }

    private void addProgress() {
        if (mPlayerController == null) {
            return;
        }
        try {
            if (mCurrentState == STATE_PLAYING) {
                int currentPosition = mPlayerController.getCurrentDuration();
                int total = mPlayerController.getDuration();

                if (total < 1 || currentPosition < 0) {
                    cancelProgressTimer();
                    return;
                }
                setVideoPlayTime(currentPosition / 1000, total / 1000);

                int progress = Math.round(currentPosition * 100.0f / total);
                setVideoProgress(progress);
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
        }
    }

    protected void onPlayProgress(int progress) {
        if (mVideoListener != null) {
            mVideoListener.onVideoPlayProgress(progress);
        }
    }

    protected void onPlayTime(int playTime, int totalTime) {
        if (mVideoListener != null) {
            mVideoListener.onVideoPlayTime(playTime, totalTime);
        }
    }

    private void setVideoProgress(int progress) {
        if (progress <= mCurrentProgress || progress < 0) {
            return;
        }
        for (int i = mCurrentProgress + 1; i <= progress; i++) {
            if (i > 100) {
                return;
            }
            onPlayProgress(i);
        }
        mCurrentProgress = progress;
    }

    private void setVideoPlayTime(int position, int total) {
        if (position <= mCurrentPlayTime || position < 0 || total <= 0) {
            return;
        }
        for (int i = mCurrentPlayTime + 1; i <= position; i++) {
            if (i > total) {
                return;
            }
            onPlayTime(i, total);
        }
        mCurrentPlayTime = position;
    }

    /**
     * 防止进度没有达到100
     */
    private void autoCompleteProgress() {
        if (mPlayerController == null) {
            return;
        }
        int total = mPlayerController.getDuration();
        if (mCurrentPlayTime < total && total > 1) {
            setVideoPlayTime(total, total);
        }
        if (mCurrentProgress < 100) {
            setVideoProgress(100);
        }
    }

    private AlxVideoPlayerControllerListener mPlayerControllerListener = new AlxVideoPlayerControllerListener() {

        @Override
        public void onPrepared() {
            isPrepared = true;
            setMute(isMute);
            if (!isAttachWindow || mCurrentState == STATE_IDLE) {
                release();
                return;
            }
            if (mCurrentState == STATE_PAUSE) {
                return;
            }

            if (mPlayerController != null) {
                mPlayerController.start(mSeekToDuration);
                setUIState(STATE_PLAYING);

                if (mVideoListener != null) {
                    mVideoListener.onVideoStart();
                }
            }
        }

        @Override
        public void onCompletion() {
            //加上这句，避免循环播放video的时候，内存不断飙升。
            Runtime.getRuntime().gc();
            setUIState(STATE_PLAY_COMPLETE);
            cancelProgressTimer();
            autoCompleteProgress();
            release();
            if (mVideoListener != null) {
                mVideoListener.onVideoCompletion();
            }
        }

        @Override
        public void onError(int what, int extra) {
            setUIState(STATE_ERROR);
            release();
            if (mVideoListener != null) {
                mVideoListener.onVideoError("play error:what=" + what);
            }
        }

        @Override
        public void onMediaPlayerError(String error) {
            AlxLog.e(AlxLogLevel.MARK, TAG, "onMediaPlayerError");
            setUIState(STATE_ERROR);
            release();
            if (mVideoListener != null) {
                mVideoListener.onVideoError("media play error:" + error);
            }
        }

        @Override
        public void onVideoSizeChanged(int width, int height) {
            try {
                if (mTextureView != null) {
                    mTextureView.setVideoSize(width, height);
                }
            } catch (Exception e) {
                AlxAgent.onError(e);
                AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            }
            if (mVideoListener != null) {
                mVideoListener.onVideoSize(width, height);
            }
        }

        @Override
        public void onInfo(int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    AlxLog.d(AlxLogLevel.OPEN, TAG, "onInfo——> what：" + what + " 缓冲开始   展示Loading，请等待...");
                    if (mCurrentState != STATE_PLAYING_BUFFERING_START) {
                        setUIState(STATE_PLAYING_BUFFERING_START);
                    }
                    if (mVideoListener != null) {
                        mVideoListener.onVideoBufferStart();
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    AlxLog.d(AlxLogLevel.OPEN, TAG, "onInfo——> what：" + what + " 缓冲结束   结束loading");
                    uiProgressBarShow(false);
                    if (mVideoListener != null) {
                        mVideoListener.onVideoBufferEnd();
                    }
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                case MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING: //没有画面有声音
                case MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING: //有画面没有声音
                    AlxLog.d(AlxLogLevel.OPEN, TAG, "onInfo——> what：" + what + " 渲染开始  结束loading");
                    resetCurrentPosition();
                    startProgressTimer();
                    uiProgressBarShow(false);
                    uiCoverViewShow(false);
                    if (mCurrentState == STATE_PREPARING || mCurrentState == STATE_PLAYING_BUFFERING_START) {
                        setUIState(STATE_PLAYING);
                    }
                    if (mVideoListener != null) {
                        mVideoListener.onVideoRenderingStart();
                    }
                    break;
            }
        }

    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onSurfaceTextureAvailable");
            if (null == mSurface || mSurfaceTexture != surface) {
                try {
                    if (mSurface != null) {
                        mSurface.release();
                    }
                    if (mSurfaceTexture != null) {
                        mSurfaceTexture.release();
                    }
                    mSurface = new Surface(surface);
                    mSurfaceTexture = surface;
                    mPlayerController.initMedia(mSurface);
                } catch (Exception e) {
                    AlxAgent.onError(e);
                    AlxLog.e(AlxLogLevel.ERROR, TAG, "onSurfaceTextureAvailable-error:" + e.getMessage());
                    mPlayerController.mediaPlayerError(e.getMessage());
                }
            }
//            else {
//                try {
//                    if (mTextureView != null) {
//                        AlxLog.w(AlxLogLevel.MARK, TAG, "onSurfaceTextureAvailable: setSurfaceTexture()");
//                        mTextureView.setSurfaceTexture(mSurfaceTexture);
//                    }
//                } catch (Exception e) {
//                    AlxAgent.onError(e);
//                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
//                }
//            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onSurfaceTextureSizeChanged:" + width + ";" + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            AlxLog.i(AlxLogLevel.MARK, TAG, "onSurfaceTextureDestroyed");
//            BufferQueueProducer: [SurfaceTexture-0-28799-0](id:707f00000004,api:3,p:1470,c:28799) cancelBuffer: BufferQueue has been abandoned
//            如果返回true，要注意留意上面的错误警告问题
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

        }
    };

    private void resetCurrentPosition() {
        if (mSeekToDuration > 0) {
            int total = mPlayerController.getDuration();
            if (total > 0) {
                int progress = Math.round(mSeekToDuration * 100.0f / total);
                mCurrentProgress = progress;
            }
            int currentDuration = mPlayerController.getCurrentDuration();
            if (currentDuration > 0) {
                mCurrentPlayTime = currentDuration / 1000;
            }
        }
        mSeekToDuration = 0;
    }

    private void initBuilder(Builder builder) {
        if (builder == null) {
            return;
        }
        if (builder.isMute != null) {
            isMute = builder.isMute.booleanValue();
        }
        if (builder.isNeedProgressBar != null) {
            setNeedProgressUI(builder.isNeedProgressBar.booleanValue());
        }
        if (builder.isNeedCoverView != null) {
            setNeedCoverUI(builder.isNeedCoverView.booleanValue());
        }
        if (builder.isNeedPlayView != null) {
            setNeedPlayUI(builder.isNeedPlayView.booleanValue());
        }
    }

    public static class Builder {
        private Boolean isMute;
        private Boolean isNeedProgressBar;
        private Boolean isNeedCoverView;
        private Boolean isNeedPlayView;

        public Builder() {
        }

        /**
         * 设置默认是否静音
         *
         * @param mute
         * @return
         */
        public Builder setMute(boolean mute) {
            isMute = new Boolean(mute);
            return this;
        }

        /**
         * 设置是否需要进度条UI
         *
         * @param need
         * @return
         */
        public Builder setNeedProgressUI(boolean need) {
            isNeedProgressBar = new Boolean(need);
            return this;
        }

        /**
         * 设置是否需要封面UI
         *
         * @param need
         * @return
         */
        public Builder setNeedCoverUI(boolean need) {
            isNeedCoverView = new Boolean(need);
            return this;
        }

        /**
         * 设置是否需要播放UI
         *
         * @param need
         * @return
         */
        public Builder setNeedPlayUI(boolean need) {
            isNeedPlayView = new Boolean(need);
            return this;
        }
    }

}