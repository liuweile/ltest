package com.alxad.analytics;

import java.io.Serializable;

public class AlxReportBean implements Serializable {

    /**
     * 事件id
     */
    public String eventId;
    /**
     * 事件的描述
     */
    public String desc;

    /**
     * 数据的扩展json字符串
     * <br/>没有用JSONObject,JSONObject类没有实现序列化
     */
    public String json;

}