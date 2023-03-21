package com.alxad.analytics;

import android.content.Context;

import com.alxad.base.AlxAdNetwork;

import java.util.Map;

/**
 * 数据埋点分析
 * @author lwl
 * @date 2021-9-13
 */
public class AlxAgent {

    /**
     * @param context 当前宿主进程的ApplicationContext上下文
     * @param eventId 事件id
     */
    public static void onEvent(Context context, String eventId) {
        AlxDataWork.getInstance().sendEvent(context, eventId, null, null);
    }

    /**
     * @param context 当前宿主进程的ApplicationContext上下文
     * @param eventId 事件id
     * @param desc    eventId的描述
     */
    public static void onEvent(Context context, String eventId, String desc) {
        AlxDataWork.getInstance().sendEvent(context, eventId, desc, null);
    }

    /**
     * @param context
     * @param eventId
     * @param map     对当前事件的参数描述，定义为 {"key":value}
     */
    public static void onEvent(Context context, String eventId, Map<String, Object> map) {
        AlxDataWork.getInstance().sendEvent(context, eventId, null, map);
    }

    public static void onError(Throwable e) {
        Context context = AlxAdNetwork.getContext();
        onError(context, e);
    }

    public static void onError(Context context, Throwable e) {
        try{
            AlxDataWork.getInstance().sendError(context, AlxAgentEvent.ERROR, e);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}