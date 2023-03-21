package com.alxad.analytics;

import android.text.TextUtils;

import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据业务转换
 *
 * @author lwl
 * @date 2021-9-26
 * <br/>
 * 缓存数据格式
 * <p>
 * {json数据}\r\n
 * {json数据}\r\n
 * {json数据}\r\n
 * ……
 * </p>
 */
public class AlxPackageData {
    private static final String TAG = "AlxPackageData";

    private final String CACHE_FILE_NAME = "alx_analytics.txt";
    private final String DATA_SEPARATOR = "\r\n";//缓存文件中数列之间分隔符号

    private String mDir;
    private List<AlxReportBean> mData;

    public void handleDataWork(String dir, AlxReportBean data) {
        try {
            mDir = dir;
            if (mData == null) {
                mData = new ArrayList<>();
            }
            List<AlxReportBean> list = getCacheList();
            if (list != null && !list.isEmpty()) {
                clearCache();
                mData.addAll(list);
            }
            if (data != null) {
                mData.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<AlxReportBean> getData() {
        return mData;
    }

    private List<AlxReportBean> getCacheList() {
        List<AlxReportBean> list = new ArrayList<>();
        try {
            String path = getPath();
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            String jsonStr = AlxDataWork.getInstance().readFileData(path);
            if (TextUtils.isEmpty(jsonStr) || !jsonStr.contains(DATA_SEPARATOR)) {
                return null;
            }

            String[] jsonArr = jsonStr.split(DATA_SEPARATOR);
            if (jsonArr == null || jsonArr.length < 1) {
                return null;
            }
            for (String json : jsonArr) {
                if (TextUtils.isEmpty(json)) {
                    continue;
                }
                JSONObject jsonObject = new JSONObject(json);
                AlxReportBean bean = new AlxReportBean();
                bean.eventId = jsonObject.optString("id");
                bean.desc = jsonObject.optString("desc");
                bean.json = jsonObject.optString("obj");
                list.add(bean);
            }
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public void writeCache() {
        try {
            if (TextUtils.isEmpty(mDir)) {
                return;
            }
            if (mData == null) {
                mData = new ArrayList<>();
            }
            List<AlxReportBean> list = getCacheList();
            if (list != null && !list.isEmpty()) {
                mData.addAll(list);
            }
            String json = getJson(mData);
            if (TextUtils.isEmpty(json)) {
                return;
            }
            AlxDataWork.getInstance().writeFileData(mDir, CACHE_FILE_NAME, json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearCache() {
        AlxDataWork.getInstance().writeFileData(mDir, CACHE_FILE_NAME, null);
    }

    private String getPath() {
        if (TextUtils.isEmpty(mDir)) {
            return null;
        }
        String path = mDir + File.separator + CACHE_FILE_NAME;
        return path;
    }

    private String getJson(List<AlxReportBean> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            for (AlxReportBean item : list) {
                if (item == null || TextUtils.isEmpty(item.eventId)) {
                    continue;
                }
                JSONObject json = new JSONObject();

                json.put("id", item.eventId);
                if (!TextUtils.isEmpty(item.desc)) {
                    json.put("desc", item.desc);
                }
                JSONObject mapJson = getJsonByStr(item.json);
                if (mapJson != null) {
                    json.put("obj", mapJson);
                }
                sb.append(json.toString());
                sb.append(DATA_SEPARATOR);
            }
            return sb.toString();
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getJson():" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static String getJsonByMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(map);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject getJsonByStr(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(jsonStr);
            return json;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}