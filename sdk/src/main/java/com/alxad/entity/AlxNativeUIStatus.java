package com.alxad.entity;

import com.alxad.omsdk.OmAdSafe;

/**
 * 记录原生广告UI中的状态
 *
 * @author liuweile
 * @date 2022-11-1
 */
public class AlxNativeUIStatus {

    private OmAdSafe mOmAdSafe;

    private boolean isReportImpression = false;//记录是否已经将曝光数据上报过，防止重复曝光

    public OmAdSafe getOmAdSafe() {
        return mOmAdSafe;
    }

    public void setOmAdSafe(OmAdSafe mOmAdSafe) {
        this.mOmAdSafe = mOmAdSafe;
    }

    public boolean isReportImpression() {
        return isReportImpression;
    }

    public void setReportImpression(boolean reportImpression) {
        isReportImpression = reportImpression;
    }

    public void destroy() {
        mOmAdSafe = null;
    }

}