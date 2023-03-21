package com.alxad.omsdk;

import android.content.Context;
import android.view.View;
import android.webkit.WebView;

import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxOmidBean;
import com.alxad.util.AlxLog;
import com.iab.omid.library.algorixco.adsession.AdEvents;
import com.iab.omid.library.algorixco.adsession.AdSession;
import com.iab.omid.library.algorixco.adsession.CreativeType;
import com.iab.omid.library.algorixco.adsession.FriendlyObstructionPurpose;
import com.iab.omid.library.algorixco.adsession.media.InteractionType;
import com.iab.omid.library.algorixco.adsession.media.MediaEvents;
import com.iab.omid.library.algorixco.adsession.media.Position;
import com.iab.omid.library.algorixco.adsession.media.VastProperties;

/**
 * OmSdk 对广告做合规性检测
 *
 * @author liuweile
 * @date 2022-4-26
 */
public class OmAdSafe {
    private static final String TAG = "AlxOmAdSafe";

    private static final String CUSTOM_REFERENCE_DATA = "{\"network\":\"AlgoriX\"}";

    public static final int TYPE_NATIVE = 1;
    public static final int TYPE_VIDEO = 2;

    private AdSession mAdSession;
    private AdEvents mAdEvents;
    private MediaEvents mMediaEvents;

    /**
     * 初始化
     *
     * @param context
     * @param view
     * @param type
     */
    public void initNoWeb(Context context, View view, int type, AlxOmidBean bean) {
        AlxLog.d(AlxLogLevel.MARK, TAG, "init");
        try {
            if (bean != null) {
                AlxLog.d(AlxLogLevel.DATA, TAG, "url=" + bean.url);
                AlxLog.d(AlxLogLevel.DATA, TAG, "key=" + bean.key);
                AlxLog.d(AlxLogLevel.DATA, TAG, "parameter=" + bean.params);
            } else {
                AlxLog.d(AlxLogLevel.DATA, TAG, "omid bean is empty");
            }

            CreativeType creativeType = type == TYPE_VIDEO ? CreativeType.VIDEO : CreativeType.NATIVE_DISPLAY;
            mAdSession = AdSessionUtil.getNativeAdSession(context, CUSTOM_REFERENCE_DATA, creativeType, bean);
            mAdSession.registerAdView(view);
            initEvent(type == TYPE_VIDEO);
            mAdSession.start();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    /**
     * webView 中初始化： 在WebViewClient.onPageFinished()中调用
     *
     * @param context
     * @param webView
     */
    public void initWeb(Context context, WebView webView) {
        if (mAdSession != null) {
            return;
        }
        try {
            AlxLog.d(AlxLogLevel.MARK, TAG, "initWebView");
            mAdSession = AdSessionUtil.getHtmlAdSession(context, webView, CUSTOM_REFERENCE_DATA, CreativeType.HTML_DISPLAY);
            initEvent(false);
            mAdSession.start();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    private void initEvent(boolean isVideo) {
        if (mAdSession == null) {
            return;
        }
        try {
            mAdEvents = AdEvents.createAdEvents(mAdSession);
            if (isVideo) {
                mMediaEvents = MediaEvents.createMediaEvents(mAdSession);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportLoad() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportLoad");
        try {
            if (mAdEvents != null) {
                mAdEvents.loaded();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    /**
     * 视频加载完成
     *
     * @param isSkipAble    是否可以跳过
     * @param offsetForSkip 跳过时间
     * @param isAutoPlay    是否自动播放
     */
    public void reportLoad(boolean isSkipAble, float offsetForSkip, boolean isAutoPlay) {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportLoad");
        try {
            VastProperties params = null;
            if (isSkipAble) {
                params = VastProperties.createVastPropertiesForSkippableMedia(offsetForSkip, isAutoPlay, Position.PREROLL);
            } else {
                params = VastProperties.createVastPropertiesForNonSkippableMedia(isAutoPlay, Position.PREROLL);
            }
            if (mAdEvents != null) {
                mAdEvents.loaded(params);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportImpress() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportImpress");
        try {
            if (mAdEvents != null) {
                mAdEvents.impressionOccurred();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    /**
     * 友情阻挡【如：关闭案例，广告logo】
     */
    public void addFriendlyObstruction(View view, FriendlyObstructionPurpose var2, String reason) {
        AlxLog.d(AlxLogLevel.MARK, TAG, "addFriendlyObstruction");
        try {
            if (mAdSession != null && view != null) {
                mAdSession.addFriendlyObstruction(view, var2, reason);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    /**
     * View changes
     *
     * @param view
     */
    public void registerAdView(View view) {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportRegisterAdView");
        try {
            if (mAdSession != null && view != null) {
                mAdSession.registerAdView(view);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoStart(float duration, boolean isMute) {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoStart");
        try {
            int volume = isMute ? 0 : 1;
            if (mMediaEvents != null) {
                mMediaEvents.start(duration, volume);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoVolumeChange(boolean isMute) {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoVolumeChange");
        try {
            int volume = isMute ? 0 : 1;
            if (mMediaEvents != null) {
                mMediaEvents.volumeChange(volume);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoResume() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoResume");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.resume();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoPause() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoPause");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.pause();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoBufferEnd() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoBufferEnd");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.bufferFinish();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoBufferStart() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoBufferStart");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.bufferStart();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoClick() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoClick");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.adUserInteraction(InteractionType.CLICK);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoFirstQuartile() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoFirstQuartile");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.firstQuartile();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoMidpoint() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoMidpoint");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.midpoint();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoThirdQuartile() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoThirdQuartile");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.thirdQuartile();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void reportVideoComplete() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "reportVideoComplete");
        try {
            if (mMediaEvents != null) {
                mMediaEvents.complete();
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

    public void destroy() {
        AlxLog.d(AlxLogLevel.MARK, TAG, "destroy");
        try {
            if (mAdSession != null) {
                mAdSession.finish();
                mAdSession = null;
            }
            mAdEvents = null;
            mMediaEvents = null;
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.MARK, TAG, e.getMessage());
        }
    }

}