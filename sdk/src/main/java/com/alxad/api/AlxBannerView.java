package com.alxad.api;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.base.AlxLogLevel;
import com.alxad.config.AlxConfig;
import com.alxad.control.banner.AlxBannerTaskView;
import com.alxad.util.AlxLog;

/**
 * banner 广告：
 * 既可以在xml布局中添加，也可以在代码中创建<br/>
 * 使用方式一：预加载：加载 和 展示 分步调用<br/>
 * 例如： 步骤一：AlxBannerView bannerView=new AlxBannerView(context); <br/>
 * bannerView.loadAd(pid,listener);<br/>
 * 步骤二： ViewGroup.addView(bannerView); <br/>
 * <br/>
 * 使用方式二: 加载并展示 <br/>
 * 例如： 1：xml文件中添加  AlxBannerView <br/>
 * 2：代码中添加：bannerView.loadAd(pid,listener);<br/>
 *
 * @author lwl
 * @date 2022-4-14
 */
public class AlxBannerView extends AlxBannerTaskView implements AlxAdInterface {
    private static final String TAG = "AlxBannerView";

    public AlxBannerView(@NonNull Context context) {
        super(context);
    }

    public AlxBannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AlxBannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 加载广告
     *
     * @param pid
     * @param listener
     */
    public void loadAd(String pid, AlxBannerViewAdListener listener) {
        loadAd(pid, null, listener);
    }

    /**
     * 加载广告
     *
     * @param pid
     * @param param    广告的一些参数配置
     * @param listener
     */
    public void loadAd(String pid, AlxAdParam param, AlxBannerViewAdListener listener) {
        requestAd(pid, param, listener);
    }

    public void destroy() {
        super.onDestroy();
    }

    /**
     * 设置刷新频率：0或30~120之间的数字，单位为s,0表示不自动轮播,默认30S
     *
     * @param time 单位s
     */
    public void setBannerRefresh(int time) {
        if (time > 0 && time < AlxConfig.BANNER_MIN_REFRESH_TIME) {
            time = AlxConfig.BANNER_MIN_REFRESH_TIME;
        }
        mRefreshTime = time;
    }

    /**
     * 设置关闭按钮是否显示
     *
     * @param canClose
     */
    public void setBannerCanClose(boolean canClose) {
        isShowCloseBn = canClose;
    }

    /**
     * 是否加载完成
     */
    public boolean isReady() {
        if (mController == null) {
            AlxLog.e(AlxLogLevel.OPEN, TAG, "isReady: Ad not loaded");
            return false;
        }
        return mController.isReady();
    }

    public void pause() {
        super.onPause();
    }

    public void resume() {
        super.onResume();
    }

    @Override
    public double getPrice() {
        if (mController != null) {
            return mController.getPrice();
        }
        return 0;
    }

    @Override
    public void reportBiddingUrl() {
        if (mController != null) {
            mController.reportBiddingUrl();
        }
    }

    @Override
    public void reportChargingUrl() {
        if (mController != null) {
            mController.reportChargingUrl();
        }
    }

    public static final class AlxAdParam {
        /**
         * 设置刷新频率,为0或30~120之间的数字，单位为s,0表示不自动刷新,默认30S
         *
         * @param time 单位s
         */
        private Integer refreshTime; //用对象主要是为了兼容setBannerRefresh不起冲突

        /**
         * 是否可以关闭广告，默认是不可关闭
         */
        private Boolean isCanClose; //用对象主要是为了兼容setBannerCanClose不起冲突

        private AlxAdParam() {
        }

        public static final class Builder {
            private AlxAdParam param;

            public Builder() {
                param = new AlxAdParam();
            }

            /**
             * 设置刷新频率,为0或30~120之间的数字，单位为s,0表示不自动轮播,默认30S
             *
             * @param refreshTime 单位s
             */
            public Builder setRefresh(int refreshTime) {
                if (refreshTime > 0 && refreshTime < AlxConfig.BANNER_MIN_REFRESH_TIME) {
                    refreshTime = AlxConfig.BANNER_MIN_REFRESH_TIME;
                }
                param.refreshTime = new Integer(refreshTime);
                return this;
            }

            /**
             * 是否可以关闭广告
             */
            public Builder setCanClose(boolean canClosed) {
                param.isCanClose = new Boolean(canClosed);
                return this;
            }

            public AlxAdParam build() {
                return param;
            }
        }

        public Integer getRefreshTime() {
            return refreshTime;
        }

        public Boolean isCanClose() {
            return isCanClose;
        }
    }

}