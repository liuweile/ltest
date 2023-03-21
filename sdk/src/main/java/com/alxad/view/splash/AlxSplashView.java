package com.alxad.view.splash;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alxad.R;
import com.alxad.base.AlxViewListener;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxSplashUIData;
import com.alxad.glittle.Glittle;
import com.alxad.util.AlxLog;

/**
 * 开屏广告View
 *
 * @author lwl
 * @date 2021-8-4
 */
public class AlxSplashView extends FrameLayout implements View.OnClickListener {
    public final String TAG = "AlxSplashView";
    private final int AD_SHOW_MAX_TIME = 5;//广告显示的最大时长

    private Context mContext;
    private Button mBnTime;
    private ImageView mIvImg;
    private AlxViewListener mListener;
    private Handler mHandler;

    private int mCurrentShowTime = 0;

    public AlxSplashView(Context context) {
        super(context);
        initView(context, null);
    }

    public AlxSplashView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public AlxSplashView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        mHandler = new Handler(Looper.myLooper());
        LayoutInflater.from(context).inflate(R.layout.alx_splash_view, this, true);
        mBnTime = (Button) findViewById(R.id.alx_time);
        mIvImg = (ImageView) findViewById(R.id.alx_img);

        mIvImg.setOnClickListener(this);
        mBnTime.setOnClickListener(this);
    }

    public void setEventListener(AlxViewListener listener) {
        mListener = listener;
    }

    public void renderAd(AlxSplashUIData bean) {
        if (bean == null) {
            return;
        }
        try {
            showImg(bean.imgBig);
            mBnTime.setText(getTimeFormat(AD_SHOW_MAX_TIME));
            startCountTime();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        if (mListener != null) {
            mListener.onViewShow();
        }
    }

    private void showImg(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        try {
//            AlxImageManager.with(mContext).load(url).into(mIvImg);
            Glittle.with(mContext).load(url).into(mIvImg);
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    public void destroy() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.alx_time) {
            stopCountTime();
            if (mListener != null) {
                mListener.onViewClose();
            }
        } else if (v.getId() == R.id.alx_img) {
            stopCountTime();
            if (mListener != null) {
                mListener.onViewClick();
            }
        }
    }

    private void stopCountTime() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
            mBnTime.setText(getTimeFormat(-1));
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private void startCountTime() {
        if (mHandler == null) {
            return;
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCurrentShowTime += 1;
                if (mCurrentShowTime < AD_SHOW_MAX_TIME) {
                    updateTime();
                    startCountTime();
                } else {
                    if (mListener != null) {
                        mListener.onViewClose();
                    }
                }
            }
        }, 1000);
    }

    private void updateTime() {
        try {
            if (mBnTime != null && mCurrentShowTime <= AD_SHOW_MAX_TIME) {
                int time = AD_SHOW_MAX_TIME - mCurrentShowTime;
                mBnTime.setText(getTimeFormat(time));
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    private String getTimeFormat(int time) {
        String result = "";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(mContext.getString(R.string.alx_video_skip));
            if (time >= 0) {
                sb.append(time);
                sb.append("s");
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}