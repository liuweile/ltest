package com.alxad.api.nativead;

import android.content.Context;

import com.alxad.api.AlxAdParam;
import com.alxad.control.nativead.AlxNativeAdModel;

/**
 * 原生广告加载类
 *
 * @author lwl
 * @date 2022-9-14
 */
public class AlxNativeAdLoader {

    private AlxNativeAdModel model;

    private AlxNativeAdLoader(Builder builder) {
        this.model = builder.model;
    }

    public void loadAd(AlxAdParam param, AlxNativeAdLoadedListener listener) {
        model.load(param, listener);
    }

    public boolean isLoading() {
        return model.isLoading();
    }

    public static class Builder {
        private AlxNativeAdModel model;

        public Builder(Context context, String adId) {
            Context context1 = null;
            if (context != null) {
                context1 = context.getApplicationContext();
            }
            model = new AlxNativeAdModel(context1, adId);
        }

        public AlxNativeAdLoader build() {
            return new AlxNativeAdLoader(this);
        }
    }

}