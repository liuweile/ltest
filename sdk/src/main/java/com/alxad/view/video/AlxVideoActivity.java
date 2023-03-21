package com.alxad.view.video;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.R;
import com.alxad.analytics.AlxAgent;
import com.alxad.api.AlxAdError;
import com.alxad.base.AlxAdNetwork;
import com.alxad.base.AlxBaseActivity;
import com.alxad.base.AlxLogLevel;
import com.alxad.base.AlxVideoAdListener;
import com.alxad.config.AlxConfig;
import com.alxad.base.AlxJumpCallback;
import com.alxad.entity.AlxOmidBean;
import com.alxad.entity.AlxTracker;
import com.alxad.entity.AlxVideoExtBean;
import com.alxad.entity.AlxVideoUIData;
import com.alxad.glittle.Glittle;
import com.alxad.glittle.target.CustomViewTarget;
import com.alxad.http.AlxHttpUtil;
import com.alxad.omsdk.OmAdSafe;
import com.alxad.report.AlxReportManager;
import com.alxad.report.AlxSdkData;
import com.alxad.report.AlxSdkDataEvent;
import com.alxad.util.AlxClickJump;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;
import com.alxad.util.AlxVideoDecoder;
import com.alxad.util.CommonDialog;
import com.alxad.util.adloading.AlxAdLoadingBuilder;
import com.alxad.widget.AlxShapeImageView;
import com.alxad.widget.video.AlxVideoListener;
import com.alxad.widget.video.AlxVideoPlayerView;
import com.iab.omid.library.algorixco.adsession.FriendlyObstructionPurpose;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 视频播放器【包括：激励视频、插屏】
 * 1： 激励视频广告(播放过程中不可关闭)
 * 2： 插屏视频广告(视频播放5s后，会出现跳过字样，点击跳过可关闭)
 *
 * @author lwl
 * @date 2022-8-7
 */
public class AlxVideoActivity extends AlxBaseActivity implements View.OnClickListener {
    public static final String TAG = "AlxVideoActivity";

    public static final String EXTRA_DATA = "videoData";
    public static final String EXTRA_IS_REWARD = "isReward"; //已经交给后台控制，后期可以直接删掉
    public static final String EXTRA_TRACKER = "tracker";

    private static ConcurrentHashMap<String, AlxVideoAdListener> mMapListener = new ConcurrentHashMap<>();
    private AlxVideoAdListener mEventListener;
    private AlxVideoUIData mAdObj;//广告对象

    private AlxVideoPlayerView mVideoView;
    private TextView mTvTime;
    private ImageView mBnVoice;
    private ImageView mBnClose;

    //封面View和落地页View
    private ImageView mIvImage;

    //伴随物料
    private View mLayoutCompanion;
    private AlxShapeImageView mIvIcon;
    private TextView mTvTitle;
    private TextView mTvDesc;
    private TextView mTvAction;//广告行为

    private AlxAdLoadingBuilder mAlxAdLoadingBuilder;

    private Handler mUiHandler;
    private Context mContext;
    private boolean mIsShowDialog = false;
    private boolean isTimeEnableClick = false;
    private volatile boolean isError = false;
    private volatile boolean isActivityDestroy = false;
    private boolean isPlayPause = false;
    private volatile boolean isPlayCompletion = false;//视频是否播放完（播放失败也属于播放完）
    private volatile boolean isRewardVideo = false;//是否是激励视频

    private AlxTracker mTracker; //数据追踪器
    private boolean isSkip;//播放时间是否可以跳过开关(由后台数据控制： true是可跳过，false是不可跳过)
    private int mVideoSkipTime = 0;//播放播放多少秒后可以跳过(由后台数据控制)
    private boolean isMute = false;//是否静音播放

    private OmAdSafe mOmAdSafe;

    public static void openActivity(Context context, AlxVideoUIData uiData, AlxTracker tracker, boolean isRewarded) throws Exception {
        Intent intent = new Intent(context, AlxVideoActivity.class);
        intent.putExtra(AlxVideoActivity.EXTRA_DATA, uiData);
        intent.putExtra(AlxVideoActivity.EXTRA_IS_REWARD, isRewarded);
        intent.putExtra(AlxVideoActivity.EXTRA_TRACKER, tracker);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.alx_activity_video);
        initView();
        mUiHandler = new Handler(mContext.getMainLooper());
        if (!initData()) {
            if (mEventListener != null) {
                mEventListener.onVideoAdPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, "1:" + AlxAdError.ERROR_MSG);
            }
            finish();
            return;
        }
        loadData();
        showVideoPlayer();
        initListener();
    }

    private void initView() {
        mVideoView = (AlxVideoPlayerView) findViewById(R.id.alx_video_view);
        mTvTime = (TextView) findViewById(R.id.alx_video_time);
        mBnVoice = (ImageView) findViewById(R.id.alx_voice);
        mBnClose = (ImageView) findViewById(R.id.alx_ad_close);

        mIvImage = (ImageView) findViewById(R.id.alx_img);

        mLayoutCompanion = findViewById(R.id.alx_companion);
        mIvIcon = (AlxShapeImageView) findViewById(R.id.alx_icon);
        mTvTitle = (TextView) findViewById(R.id.alx_title);
        mTvDesc = (TextView) findViewById(R.id.alx_desc);
        mTvAction = (TextView) findViewById(R.id.alx_action);

        mLayoutCompanion.setVisibility(View.GONE);
        mBnClose.setVisibility(View.GONE);
        mBnVoice.setVisibility(View.GONE);

        //初始化 loading
        mAlxAdLoadingBuilder = new AlxAdLoadingBuilder(this);
        mAlxAdLoadingBuilder.setIcon(R.drawable.alx_ad_loading);
        mAlxAdLoadingBuilder.setText(getString(R.string.alx_ad_video_loading));
        mAlxAdLoadingBuilder.setOutsideTouchable(false);//点击空白区域是否关闭
        mAlxAdLoadingBuilder.setBackTouchable(false);//按返回键是否关闭
    }

    private void initListener() {
        try {
            mBnClose.setOnClickListener(this);
            mLayoutCompanion.setOnClickListener(this);
            mTvAction.setOnClickListener(this);
            mIvImage.setOnClickListener(this);
            mVideoView.setOnClickListener(this);
            mBnVoice.setOnClickListener(this);
            mTvTime.setOnClickListener(this);
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private boolean initData() {
        Intent intent;
        try {
            intent = getIntent();
            if (intent == null) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onCreate error intent is null");
                return false;
            }
            mAdObj = intent.getParcelableExtra(EXTRA_DATA);
            try {
                mTracker = intent.getParcelableExtra(EXTRA_TRACKER);
            } catch (Exception e) {
                AlxAgent.onError(e);
                e.printStackTrace();
            }
            isRewardVideo = intent.getBooleanExtra(EXTRA_IS_REWARD, false);
            if (mAdObj == null || mAdObj.video == null) {
                return false;
            }
            if (TextUtils.isEmpty(mAdObj.video.videoUrl)) {
                return false;
            }
            if (TextUtils.isEmpty(mAdObj.id)) {
                return false;
            }
            mEventListener = mMapListener.get(mAdObj.id);

            AlxVideoExtBean videoExtBean = mAdObj.video.extField;
            if (videoExtBean != null) {
                isSkip = videoExtBean.isSkip();
                mVideoSkipTime = videoExtBean.skipafter;
                isMute = videoExtBean.isMute();
            }
            return true;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return false;
    }

    private void loadData() {
        if (mAdObj == null || mAdObj.video == null) {
            return;
        }

        mOmAdSafe = new OmAdSafe();
        mOmAdSafe.initNoWeb(this, mVideoView, OmAdSafe.TYPE_VIDEO, getOmidBean());
        mOmAdSafe.reportLoad(isSkip, mVideoSkipTime, true);
        mOmAdSafe.reportImpress();
        mOmAdSafe.addFriendlyObstruction(mBnClose, FriendlyObstructionPurpose.CLOSE_AD, "close");
        mOmAdSafe.addFriendlyObstruction(mLayoutCompanion, FriendlyObstructionPurpose.OTHER, "companion");

        if (mEventListener != null) {
            mEventListener.onVideoAdPlayShow();
        }

        try {
            mLayoutCompanion.setVisibility(View.VISIBLE);
            mTvTitle.setText(mAdObj.video.adTitle);
            mTvAction.setText(getString(R.string.alx_video_click_btn));

            if (TextUtils.isEmpty(mAdObj.video.description)) {
                mTvDesc.setText(mAdObj.video.adTitle);
            } else {
                mTvDesc.setText(mAdObj.video.description);
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }

        try {
            showImgViewUI();

            if (!TextUtils.isEmpty(mAdObj.video.iconUrl)) {
                Glittle.with(mContext).load(mAdObj.video.iconUrl).into(mIvIcon);
                if (mOmAdSafe != null) {
                    mOmAdSafe.addFriendlyObstruction(mIvIcon, FriendlyObstructionPurpose.OTHER, "icon");
                }
            } else {
                mIvIcon.setVisibility(View.GONE);
            }
        } catch (Throwable e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void showVideoPlayer() {
        String path = null;
        boolean isNetwork = false;
        try {
            String videoFileName = AlxHttpUtil.getDownloadFileName(mAdObj.video.videoUrl);
            File videoFile = new File(AlxFileUtil.getVideoSavePath(this) + videoFileName);
            if (videoFile.exists()) {
                isNetwork = false;
                path = videoFile.getPath();
            } else {
                isNetwork = true;
                path = mAdObj.video.videoUrl;
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            e.printStackTrace();
        }

        if (TextUtils.isEmpty(path)) {
            if (mEventListener != null) {
                mEventListener.onVideoAdPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, "url is empty");
            }
            return;
        }

        try {
            if (isMute) {
                mBnVoice.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_off));
            } else {
                mBnVoice.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_on));
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }

        try {
            mVideoView.setUp(path, mAlxVideoListener, new AlxVideoPlayerView.Builder()
                    .setMute(isMute)
                    .setNeedCoverUI(false)
                    .setNeedProgressUI(false)
                    .setNeedPlayUI(false));
            mVideoView.start();
            if (isNetwork) {
                if (mAlxAdLoadingBuilder != null) {
                    //开始计算是否超时
                    postDelayedVideoTimeOut();
                    AlxLog.d(AlxLogLevel.OPEN, TAG, "播放在线视频    展示loading弹窗，请等待...");
                    mAlxAdLoadingBuilder.show();
                }
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
        AlxLog.d(AlxLogLevel.MARK, TAG, "Video buffering");
        mUiHandler.removeCallbacksAndMessages(null);
        //定时器，如果10秒播放器还没有prepare就播放错误
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "Video buffer timeout，timeout is " + AlxConfig.VIDEO_LOADING_TIMEOUT + "ms");
                releaseUI();
                doError("video loading timeout");
            }
        }, AlxConfig.VIDEO_LOADING_TIMEOUT);
    }

    /**
     * 落地页图片
     * 1：先取落地页本地图片
     * 2：1获取失败，取落地页网络图片
     * 3：2获取失败，取视频的第一帧图片
     */
    private void showImgViewUI() {
        if (mAdObj == null || mAdObj.video == null) {
            return;
        }
        try {
            if (!TextUtils.isEmpty(mAdObj.video.landUrl)) {
                AlxLog.i(AlxLogLevel.MARK, TAG, "showImgViewUI:landUrl");
                loadImgDrawable(mAdObj.video.landUrl);
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
        Glittle.with(mContext).load(url).into(new CustomViewTarget<ImageView, Drawable>(mIvImage) {
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                AlxLog.i(AlxLogLevel.ERROR, TAG, "showImgViewUI:fail");
                videoConvertBitmap();
            }

            @Override
            public void onResourceReady(@NonNull Drawable drawable) {
                AlxLog.i(AlxLogLevel.MARK, TAG, "showImgViewUI:ok");
                if (mIvImage == null) {
                    return;
                }
                try {
                    if (drawable != null) {
                        mIvImage.setImageDrawable(drawable);
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
            String videoFileName = AlxHttpUtil.getDownloadFileName(mAdObj.video.videoUrl);
            File cacheVideoFile = new File(AlxFileUtil.getVideoSavePath(this) + videoFileName);
            String coverUrl;
            if (cacheVideoFile.exists()) {
                coverUrl = cacheVideoFile.getPath();
            } else {
                coverUrl = mAdObj.video.videoUrl;
            }
            //获取视频关键帧封面
            final String frameSource = coverUrl;
            if (!TextUtils.isEmpty(frameSource)) {
                AlxAdNetwork.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            final Bitmap bitmap = AlxUtil.getNetVideoBitmap(frameSource);
                            DisplayMetrics dm = getResources().getDisplayMetrics();
                            final Bitmap bitmap = AlxVideoDecoder.getVideoFrame(frameSource, dm.widthPixels, dm.heightPixels);
                            if (bitmap != null && !isFinishing() && !isActivityDestroy) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if (bitmap != null && mIvImage != null) {
                                                mIvImage.setImageBitmap(bitmap);
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

    private void showProgressUI(boolean isShow) {
        try {
            if (mAlxAdLoadingBuilder != null && !isFinishing()) {
                if (isShow) {
                    mAlxAdLoadingBuilder.show();
                } else {
                    mAlxAdLoadingBuilder.dismiss();
                }
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    //清空本地缓存：视频播放异常才清除缓存，加载超时不清除缓存
    private void clearCacheData() {
        try {
            if (mAdObj != null) {
                String videoFileName = AlxHttpUtil.getDownloadFileName(mAdObj.video.videoUrl);
                File videoFile = new File(AlxFileUtil.getVideoSavePath(this) + videoFileName);
                if (videoFile.exists()) {
                    videoFile.delete();
                }
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_voice) {
            if (mVideoView != null) {
                if (mVideoView.isMute()) {
                    mVideoView.setMute(false);
                    mBnVoice.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_on));
                    if (mAdObj != null) {
                        AlxReportManager.reportUrl(mAdObj.video.unmuteList, mAdObj, "unmute");
                    }
                    if (mOmAdSafe != null) {
                        mOmAdSafe.reportVideoVolumeChange(false);
                    }
                } else {
                    mVideoView.setMute(true);
                    mBnVoice.setImageDrawable(getResources().getDrawable(R.drawable.alx_voice_off));
                    if (mAdObj != null) {
                        AlxReportManager.reportUrl(mAdObj.video.muteList, mAdObj, "mute");
                    }
                    if (mOmAdSafe != null) {
                        mOmAdSafe.reportVideoVolumeChange(true);
                    }
                }
            }
        } else if (v.getId() == R.id.alx_ad_close) {
            if (mIsShowDialog && !isPlayCompletion) {
//            if (isRewardVideo && !isPlayCompletion) { //后期，针对激励视频广告是否可以中途关闭的弹窗提示，用此判断将上面的给替换掉
                CommonDialog commonDialog = new CommonDialog(AlxVideoActivity.this, R.style.alx_dialog,
                        getResources().getString(R.string.alx_home_dialog_content),
                        new CommonDialog.OnCloseListener() {
                            @Override
                            public void onClick(Dialog dialog, boolean confirm) {
                                if (confirm) {
                                    AlxLog.d(AlxLogLevel.OPEN, TAG, "Click Close button ok");
                                    if (mVideoView != null) {
                                        mVideoView.onResume();
                                    }
                                    dialog.dismiss();
                                } else {
                                    AlxLog.d(AlxLogLevel.OPEN, TAG, "Click Close button close");
                                    closeVideo();
                                    if (mEventListener != null) {
                                        mEventListener.onVideoAdClosed();
                                    }
                                }
                            }
                        });
                commonDialog.setTitle(getResources().getString(R.string.alx_home_dialog_title)).show();
                Window dialogWindow = commonDialog.getWindow();
                Display m = getWindowManager().getDefaultDisplay();
                WindowManager.LayoutParams p = dialogWindow.getAttributes();
                p.width = m.getWidth();
                dialogWindow.setAttributes(p);
                if (mVideoView != null) {
                    mVideoView.pause();
                }
            } else {
                closeVideo();
                if (mEventListener != null) {
                    mEventListener.onVideoAdClosed();
                }
            }
        } else if (v.getId() == R.id.alx_img) {
            adClickAction();
        } else if (v.getId() == R.id.alx_companion) {
            adClickAction();
        } else if (v.getId() == R.id.alx_action) {
            adClickAction();
        } else if (v.getId() == R.id.alx_video_view) {
            adClickAction();
        } else if (v.getId() == R.id.alx_video_time) {
            if (isTimeEnableClick) {
                releaseUI();
                if (mAdObj != null) {
                    AlxReportManager.reportUrl(mAdObj.video.skipList, mAdObj, "skip");
                }
            }
        }
    }

    private void releaseUI() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "releaseUI");
        isPlayCompletion = true;
        try {
            mUiHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "onVideoPause:" + e.getMessage());
        }

        try {
            if (mAlxAdLoadingBuilder != null) {
                mAlxAdLoadingBuilder.dismiss();
                mAlxAdLoadingBuilder.destroy();
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            e.printStackTrace();
        }

        try {
            if (mVideoView != null) {
                mVideoView.release();
            }
            mTvTime.setVisibility(View.GONE);
            mBnVoice.setVisibility(View.GONE);
            mIvImage.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            mBnClose.setVisibility(View.VISIBLE);
            if (mEventListener != null) {
                mEventListener.onVideoAdPlayStop();
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    /**
     * 广告点击跳转：
     * 优先deeplink
     */
    private void adClickAction() {
        omidReport(OMID_EVENT_TYPE_CLICK);
        if (mEventListener != null) {
            mEventListener.onVideoAdPlayClicked();
        }
        if (mAdObj == null) {
            return;
        }
        String url = null;
        List<String> urls = mAdObj.video.clickThroughList;
        if (urls != null && urls.size() > 0) {
            url = urls.get(0);
        }

        AlxLog.d(AlxLogLevel.MARK, TAG, "Click Url: " + url);
        AlxClickJump.openLink(this, mAdObj.deeplink, url, mAdObj.bundle, mTracker, new AlxJumpCallback() {
            @Override
            public void onDeeplinkCallback(boolean isSuccess, String error) {
                if (mAdObj == null) {
                    return;
                }
                if (isSuccess) {
                    AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link(Deeplink) open is true");
                    AlxSdkData.tracker(mTracker, AlxSdkDataEvent.DEEPLINK_YES);
                } else {
                    AlxSdkData.tracker(mTracker, AlxSdkDataEvent.DEEPLINK_NO);
                }
            }

            @Override
            public void onUrlCallback(boolean isSuccess, int type) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "Ad link open is " + isSuccess);
            }
        });
    }

    public void doError(String error) {
        if (!isError) {
            isError = true;
//            clearCacheData();
            if (mEventListener != null) {
                mEventListener.onVideoAdPlayFailed(AlxAdError.ERR_VIDEO_PLAY_FAIL, error);
            }
        }
    }

    public void closeVideo() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
        omidReport(OMID_EVENT_TYPE_PAUSE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reportResume();
        if (mVideoView != null) {
            mVideoView.onResume();
        }
        omidReport(OMID_EVENT_TYPE_RESUME);
    }

    @Override
    public void onBackPressed() {
        //返回事件拦截
    }

    @Override
    protected void onDestroy() {
        isActivityDestroy = true;
        try {
            if (mVideoView != null) {
                mVideoView.onDestroy();
            }
            if (mAdObj != null && !TextUtils.isEmpty(mAdObj.id)) {
                mMapListener.remove(mAdObj.id);
            }
            if (mEventListener != null) {
                mEventListener = null;
            }
            if (mUiHandler != null) {
                //清空handle消息  避免内存泄露
                mUiHandler.removeCallbacksAndMessages(null);
            }
            if (mAlxAdLoadingBuilder != null) {
                mAlxAdLoadingBuilder.destroy();
            }
            if (mOmAdSafe != null) {
                mOmAdSafe.destroy();
                mOmAdSafe = null;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private AlxVideoListener mAlxVideoListener = new AlxVideoListener() {
        private boolean isImpression = false;

        @Override
        public void onVideoBufferStart() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoBufferStart");
            omidReport(OMID_EVENT_TYPE_START);
            //开始计算是否超时
            postDelayedVideoTimeOut();
            showProgressUI(true);
        }

        @Override
        public void onVideoBufferEnd() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoBufferEnd");
            try {
                omidReport(OMID_EVENT_TYPE_BUFFER_END);
                mUiHandler.removeCallbacksAndMessages(null);
                showProgressUI(false);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onBufferingEnd:" + e.getMessage());
            }
        }

        @Override
        public void onVideoRenderingStart() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoRenderingStart");
            try {
                mIvImage.setVisibility(View.GONE);
                mBnVoice.setVisibility(View.VISIBLE);
                mUiHandler.removeCallbacksAndMessages(null);
                showProgressUI(false);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onVideoRenderingStart:" + e.getMessage());
            }
            if (!isImpression) {
                isImpression = true;
                omidReport(OMID_EVENT_TYPE_START);
                if (mEventListener != null) {
                    mEventListener.onVideoAdPlayStart();
                }
            }
        }

        @Override
        public void onVideoError(String error) {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoError:" + error);
            releaseUI();
            doError(error);
            clearCacheData();
        }

        @Override
        public void onVideoPause() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoPause");
            isPlayPause = true;
            try {
                mUiHandler.removeCallbacksAndMessages(null);
            } catch (Exception e) {
                AlxLog.e(AlxLogLevel.ERROR, TAG, "onVideoPause:" + e.getMessage());
            }
            if (mAdObj != null) {
                AlxReportManager.reportUrl(mAdObj.video.pauseList, mAdObj, "pause");
            }
        }

        @Override
        public void onVideoStart() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoStart");
            isPlayPause = false;
        }

        @Override
        public void onVideoCompletion() {
            AlxLog.d(AlxLogLevel.OPEN, TAG, "onVideoCompletion");
            releaseUI();
            if (mOmAdSafe != null) {
                mOmAdSafe.reportVideoComplete();
            }
            if (mEventListener != null) {
                mEventListener.onVideoAdPlayEnd();
            }
        }

        @Override
        public void onVideoSize(int width, int height) {

        }

        @Override
        public void onVideoPlayProgress(int progress) {
            if (mOmAdSafe != null) {
                switch (progress) {
                    case 25:
                        mOmAdSafe.reportVideoFirstQuartile();
                        break;
                    case 50:
                        mOmAdSafe.reportVideoMidpoint();
                        break;
                    case 75:
                        mOmAdSafe.reportVideoThirdQuartile();
                        break;
                }
            }

            if (mEventListener != null) {
                mEventListener.onVideoAdPlayProgress(progress);
            }
        }

        @Override
        public void onVideoPlayTime(int playTime, int totalTime) {
            String countDown = (totalTime - playTime) + "";//倒计时间
            mTvTime.setVisibility(View.VISIBLE);
            if (!isSkip) {
                mTvTime.setText(countDown);
            } else {
                if (playTime > mVideoSkipTime) {
                    mTvTime.setText(countDown + " | " + getString(R.string.alx_video_skip));
                    isTimeEnableClick = true;
                } else {
                    mTvTime.setText(countDown);
                    isTimeEnableClick = false;
                }
            }

            if (mEventListener != null) {
                mEventListener.onVideoAdPlayOffset(playTime);
            }
        }

        @Override
        public void onVideoSaveInstanceState(int currentPosition) {

        }

    };

    public static void setVideoEventListener(String id, AlxVideoAdListener listener) {
        if (TextUtils.isEmpty(id) || listener == null) {
            return;
        }
        mMapListener.put(id, listener);
    }

    //添加上报事件
    private void reportResume() {
        try {
            if (isPlayPause) {
                isPlayPause = false;
                if (mAdObj != null) {
                    AlxReportManager.reportUrl(mAdObj.video.resumeList, mAdObj, "resume");
                }
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private AlxOmidBean getOmidBean() {
        try {
            if (mAdObj == null || mAdObj.video == null) {
                return null;
            }
            return mAdObj.video.omid;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return null;
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
                if (mVideoView != null) {
                    mOmAdSafe.reportVideoStart(mVideoView.getDuration(), mVideoView.isMute());
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

}