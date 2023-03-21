package com.alxad.util;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 工作线程handler单例
 *
 * @author liuweile
 * @date 2021-4-13
 */
public class WorkHandler extends HandlerThread {

    private Handler mHandler;

    private static class SingleHolder {
        private static WorkHandler instance = new WorkHandler("work-handler");
    }

    public static WorkHandler getDefault() {
        return SingleHolder.instance;
    }

    private WorkHandler(String name) {
        super(name);
//        setDaemon(true);//设置守护线程
        start();
        mHandler = new Handler(getLooper());
    }

    private WorkHandler(String name, int priority) {
        super(name, priority);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
    }

    public Handler getHandler() {
        return mHandler;
    }

}