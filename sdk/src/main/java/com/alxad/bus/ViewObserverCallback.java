package com.alxad.bus;

public interface ViewObserverCallback {
    /**
     * View显示
     */
    void onViewVisible();

    /**
     * View隐藏
     */
    void onViewHidden();
}