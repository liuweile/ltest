package com.alxad.control;

import android.content.Context;

import com.alxad.api.AlxBannerView;
import com.alxad.api.AlxBannerViewAdListener;
import com.alxad.entity.AlxBannerUIData;
import com.alxad.net.impl.AlxBannerTaskImpl;
import com.alxad.net.lib.AlxAdLoadListener;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxRequestBean;

/**
 * banner广告业务
 *
 * @author liuweile
 * @date 2022-4-14
 */
public class AlxBannerAdModel extends AlxBaseAdModel<AlxBannerUIData, Context> {
    private Context mContext;
    private String mAdId;
    private AlxBannerView.AlxAdParam mAdParam;
    private AlxBannerViewAdListener mListener;

    public AlxBannerAdModel(Context context, String adId, AlxBannerView.AlxAdParam param, AlxBannerViewAdListener listener) {
        mContext = context;
        mAdId = adId;
        mAdParam = param;
        mListener = listener;
    }

    @Override
    public void load() {
        isLoading = true;
        AlxRequestBean request = new AlxRequestBean(mAdId, AlxRequestBean.AD_TYPE_BANNER);
        AlxAdTask<AlxBannerUIData> task = new AlxBannerTaskImpl();
        task.startLoad(mContext, request, new AlxAdLoadListener<AlxBannerUIData>() {
            @Override
            public void onAdLoadSuccess(AlxRequestBean request, AlxBannerUIData response) {
                isReady = true;
                isLoading = false;
                mResponse = response;
                mRequestParams = request;
                if (mListener != null) {
                    mListener.onAdLoaded();
                }
            }

            @Override
            public void onAdLoadError(AlxRequestBean request, final int code, final String msg) {
                isReady = false;
                isLoading = false;
                mResponse = null;
                mRequestParams = null;
                if (mListener != null) {
                    mListener.onAdError(code, msg);
                }
            }
        });
    }

    @Override
    public void show(Context context) {

    }

    public AlxBannerUIData getResponse() {
        return mResponse;
    }

    public AlxRequestBean getRequest() {
        return mRequestParams;
    }

    @Override
    public void destroy() {
        isReady = false;
        isLoading = false;
        mResponse = null;
        mRequestParams = null;
    }

}