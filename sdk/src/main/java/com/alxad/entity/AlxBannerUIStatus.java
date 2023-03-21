package com.alxad.entity;

public class AlxBannerUIStatus {

    //防止重复渲染数据(离开窗体时注意要设置为false)
    private boolean isHasRenderData = false;

    public boolean isHasRenderData() {
        return isHasRenderData;
    }

    public void setHasRenderData(boolean hasRenderData) {
        isHasRenderData = hasRenderData;
    }

}