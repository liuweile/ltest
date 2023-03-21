package com.alxad.control.nativead;

import android.content.Context;

import com.alxad.api.AlxAdError;
import com.alxad.api.AlxAdParam;
import com.alxad.api.nativead.AlxNativeAd;
import com.alxad.api.nativead.AlxNativeAdLoadedListener;
import com.alxad.base.AlxLogLevel;
import com.alxad.entity.AlxNativeUIData;
import com.alxad.entity.AlxTracker;
import com.alxad.net.impl.AlxNativeTaskImpl;
import com.alxad.net.lib.AlxAdLoadListener;
import com.alxad.net.lib.AlxAdTask;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.util.AlxLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 原生广告业务
 *
 * @author liuweile
 * @date 2022-4-12
 */
public class AlxNativeAdModel {
    private final String TAG = "AlxNativeAdModel";

    private Context mContext;
    private String mAdId;
    private List<AlxNativeAd> mAdList;
    private AlxNativeAdLoadedListener mListener;
    /**
     * 广告是否加载中
     */
    protected volatile boolean isLoading = false;

    public AlxNativeAdModel(Context context, String adId) {
        mContext = context;
        mAdId = adId;
    }

    public void load(AlxAdParam param, AlxNativeAdLoadedListener listener) {
        AlxLog.i(AlxLogLevel.OPEN, TAG, "native-ad: pid=" + mAdId);
        mListener = listener;
        isLoading = true;
        AlxRequestBean request = new AlxRequestBean(mAdId, AlxRequestBean.AD_TYPE_NATIVE);

        AlxAdTask<List<AlxNativeUIData>> task = new AlxNativeTaskImpl();
        task.startLoad(mContext, request, new AlxAdLoadListener<List<AlxNativeUIData>>() {
            @Override
            public void onAdLoadSuccess(AlxRequestBean request, List<AlxNativeUIData> response) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onAdLoaded");
                isLoading = false;
                mAdList = getNativeList(response, request);
                if (mAdList == null || mAdList.isEmpty()) {
                    if (mListener != null) {
                        mListener.onAdFailed(AlxAdError.ERR_NO_FILL, "no fill");
                    }
                } else {
                    if (mListener != null) {
                        mListener.onAdLoaded(mAdList);
                    }
                }
            }

            @Override
            public void onAdLoadError(AlxRequestBean request, int code, String msg) {
                AlxLog.d(AlxLogLevel.OPEN, TAG, "onError:" + code + ";" + msg);
                isLoading = false;
                if (mListener != null) {
                    mListener.onAdFailed(code, msg);
                }
            }
        });
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void destroy() {
        try {
            if (mAdList != null) {
                for (AlxNativeAd item : mAdList) {
                    item.destroy();
                }
                mAdList.clear();
                mAdList = null;
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
    }

    public List<AlxNativeAd> getNativeList(List<AlxNativeUIData> list, AlxRequestBean request) {
        try {
            if (list == null || list.isEmpty()) {
                return null;
            }
            AlxTracker tracker = null;
            if (request != null) {
                tracker = request.getTracker();
            }
            List<AlxNativeAd> resultList = new ArrayList<>();
            for (AlxNativeUIData item : list) {
                if (item != null) {
                    AlxNativeAdImpl bean = new AlxNativeAdImpl(item, tracker);
                    resultList.add(bean);
                }
            }
            return resultList;
        } catch (Exception e) {
            return null;
        }
    }
}