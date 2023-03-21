package com.alxad.control;

import com.alxad.api.AlxAdInterface;
import com.alxad.entity.AlxBaseUIData;
import com.alxad.net.lib.AlxRequestBean;
import com.alxad.report.AlxReportManager;

/**
 * 广告业务基类
 *
 * @author liuweile
 * @date 2022-4-11
 */
public abstract class AlxBaseAdModel<T extends AlxBaseUIData, E> implements AlxAdInterface {

    protected AlxRequestBean mRequestParams;
    protected T mResponse;
    /**
     * 广告是否加载中
     */
    protected volatile boolean isLoading = false;
    /**
     * 广告是否加载成功
     */
    protected volatile boolean isReady = false;

    public abstract void load();

    public abstract void show(E obj);

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isReady() {
        return isReady;
    }

    public abstract void destroy();

    @Override
    public double getPrice() {
        if (mResponse != null) {
            return mResponse.price;
        }
        return 0;
    }

    @Override
    public void reportBiddingUrl() {
        if (mResponse != null) {
            AlxReportManager.reportUrl(mResponse.nurl, mResponse, "bidding");
        }
    }

    @Override
    public void reportChargingUrl() {
        if (mResponse != null) {
            AlxReportManager.reportUrl(mResponse.burl, mResponse, "charging");
        }
    }
}