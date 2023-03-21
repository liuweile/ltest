package com.alxad.api.nativead;

import java.util.List;

public interface AlxNativeAdLoadedListener {

    void onAdFailed(int errorCode, String errorMsg);

    void onAdLoaded(List<AlxNativeAd> list);
}