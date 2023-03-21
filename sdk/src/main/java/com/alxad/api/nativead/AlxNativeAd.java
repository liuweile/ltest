package com.alxad.api.nativead;

import android.graphics.Bitmap;

import com.alxad.api.AlxAdInterface;
import com.alxad.api.AlxImage;

import java.util.List;

/**
 * 原生广告
 *
 * @author lwl
 * @date 2022-9-14
 */
public abstract class AlxNativeAd implements AlxAdInterface {

    public AlxNativeAd() {
    }

    /**
     * 广告素材类型【如：大图、小图、组图、视频、其他：未知类型】
     *
     * @return
     */
    public abstract int getCreativeType();

    /**
     * 广告来源
     *
     * @return
     */
    public abstract String getAdSource();

    /**
     * Algorix logo
     */
    public abstract Bitmap getAdLogo();

    /**
     * 广告标题
     * @return
     */
    public abstract String getTitle();

    /**
     * 广告描述
     * @return
     */
    public abstract String getDescription();

    /**
     * 广告的小图标
     * @return
     */
    public abstract AlxImage getIcon();

    /**
     * 广告内容多图素材
     * @return
     */
    public abstract List<AlxImage> getImages();

    /**
     * 广告行为按钮的显示文字（例如："查看详情"或"下载"）
     *
     * @return
     */
    public abstract String getCallToAction();

    /**
     * 广告多媒体内容信息
     *
     * @return
     */
    public abstract AlxMediaContent getMediaContent();

    /**
     * 销毁广告对象
     */
    public abstract void destroy();

    public abstract void setNativeEventListener(AlxNativeEventListener listener);

}