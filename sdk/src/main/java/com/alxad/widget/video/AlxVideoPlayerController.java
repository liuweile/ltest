package com.alxad.widget.video;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.Surface;

import com.alxad.analytics.AlxAgent;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;

/**
 * 视频播放控制器
 *
 * @author lwl
 * @date 2022-10-9
 */
public class AlxVideoPlayerController implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = "AlxVideoPlayerController";

    private String mVideoUrl;

    private volatile MediaPlayer mMediaPlayer;
    private AlxVideoPlayerControllerListener mListener;

    public AlxVideoPlayerController() {
    }

    public void setPlayerControllerListener(AlxVideoPlayerControllerListener listener) {
        mListener = listener;
    }

    public void setVideoUrl(String videoUrl) {
        this.mVideoUrl = videoUrl;
    }

    public void initMedia(final Surface surface) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "initMedia:" + mVideoUrl);
        release();

        try {
            mMediaPlayer = new MediaPlayer();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
            } else {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }

            mMediaPlayer.setLooping(false);
//          mMediaPlayer.setScreenOnWhilePlaying(true);//播放时屏幕一直亮着
            mMediaPlayer.setOnPreparedListener(AlxVideoPlayerController.this);
            mMediaPlayer.setOnCompletionListener(AlxVideoPlayerController.this);
            mMediaPlayer.setOnBufferingUpdateListener(AlxVideoPlayerController.this);

            mMediaPlayer.setOnErrorListener(AlxVideoPlayerController.this);
            mMediaPlayer.setOnInfoListener(AlxVideoPlayerController.this);
            mMediaPlayer.setOnVideoSizeChangedListener(AlxVideoPlayerController.this);

            mMediaPlayer.setDataSource(mVideoUrl);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setSurface(surface);
        } catch (Throwable e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, "initMedia-error:" + e.getMessage());
            mediaPlayerError(e.getMessage());
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
            } catch (Exception e) {
                AlxAgent.onError(e);
                AlxLog.e(AlxLogLevel.ERROR, TAG, "release(): mediaPlayer-error1:" + e.getMessage());
            }
            try {
                mMediaPlayer.setSurface(null);
            } catch (Exception e) {
                AlxAgent.onError(e);
                AlxLog.e(AlxLogLevel.ERROR, TAG, "release(): mediaPlayer-error2:" + e.getMessage());
            }
            try {
                mMediaPlayer.release();
            } catch (Exception e) {
                AlxAgent.onError(e);
                AlxLog.e(AlxLogLevel.ERROR, TAG, "release(): mediaPlayer-error3:" + e.getMessage());
            }
            mMediaPlayer = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//        AlxLog.i(AlxLogLevel.MARK, TAG, "onBufferingUpdate:"+percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "onCompletion");
        if (mListener != null) {
            mListener.onCompletion();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        AlxLog.e(AlxLogLevel.ERROR, TAG, "onError:what=" + what + ";extra=" + extra);
        if (mListener != null) {
            mListener.onError(what, extra);
        }
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "onInfo:what=" + what + ";extra=" + extra);
        if (mListener != null) {
            mListener.onInfo(what, extra);
        }
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "onPrepared");
        if (mListener != null) {
            mListener.onPrepared();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "onVideoSizeChanged:width=" + width + ";height=" + height);
        if (mListener != null) {
            mListener.onVideoSizeChanged(width, height);
        }
    }

    public int getCurrentDuration() {
        int duration = -1;
        if (mMediaPlayer == null) {
            return duration;
        }
        try {
            duration = mMediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getCurrentDuration():" + e.getMessage());
        }
        return duration;
    }

    public int getDuration() {
        int duration = -1;
        if (mMediaPlayer == null) {
            return duration;
        }
        try {
            duration = mMediaPlayer.getDuration();
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getDuration():" + e.getMessage());
        }
        return duration;
    }

    public boolean isPlaying() {
        try {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, "isPlaying():" + e.getMessage());
        }
        return false;
    }

    public void start(final int currentDuration) {
        AlxLog.i(AlxLogLevel.MARK, TAG, "start");
        if (mMediaPlayer == null) {
            return;
        }
        try {
            if (currentDuration > 0) {
                mMediaPlayer.seekTo(currentDuration);
            }
            mMediaPlayer.start();
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            mediaPlayerError(e.getMessage());
        }
    }

    public void pause() {
        AlxLog.i(AlxLogLevel.MARK, TAG, "pause");
        if (mMediaPlayer == null) {
            return;
        }
        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            mediaPlayerError(e.getMessage());
        }
    }

    /**
     * 设置声音
     *
     * @param leftVolume
     * @param rightVolume
     */
    public boolean setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer == null) {
            return false;
        }
        try {
            mMediaPlayer.setVolume(leftVolume, rightVolume);
            return true;
        } catch (Exception e) {
            AlxAgent.onError(e);
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            return false;
        }
    }

    public void mediaPlayerError(String error) {
        if (mListener != null) {
            mListener.onMediaPlayerError(error);
        }
    }

}