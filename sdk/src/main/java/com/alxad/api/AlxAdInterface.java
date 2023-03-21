package com.alxad.api;

/**
 * 广告扩展对象
 *
 * @author lwl
 * @date 2022-2-10
 */
public interface AlxAdInterface {

    /**
     * 广告竞价单价ecpm
     *
     * @return
     */
    double getPrice();

    /**
     * 竞价成功通知url，需要将${AUCTION_PRICE}替换为实际的价格
     *
     * @return
     */
    void reportBiddingUrl();

    /**
     * 计费通知url，需要将${AUCTION_PRICE}替换为实际的价格
     *
     * @return
     */
    void reportChargingUrl();

}