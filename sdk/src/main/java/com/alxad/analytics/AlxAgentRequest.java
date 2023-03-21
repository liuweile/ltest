package com.alxad.analytics;

import android.text.TextUtils;

import com.alxad.base.AlxAdBase;
import com.alxad.config.AlxConfig;
import com.alxad.config.AlxConst;
import com.alxad.base.AlxLogLevel;
import com.alxad.http.AlxHttpManager;
import com.alxad.http.AlxHttpMethod;
import com.alxad.http.AlxHttpRequest;
import com.alxad.http.AlxHttpResponse;
import com.alxad.report.AlxSdkData;
import com.alxad.util.AlxLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据上报
 */
public class AlxAgentRequest {
    private static final String TAG = "AlxAgentRequest";

    private static final ExecutorService mThreadPool = Executors.newCachedThreadPool();

    public void sendRequest(final AlxPackageData data) {
        try {
            if (data == null || data.getData() == null || data.getData().isEmpty()) {
                return;
            }
            final String random = String.valueOf(new Random().nextInt());
            final String paramsJson = getRequestJson(data.getData());
            final String aseParams = AlxSdkData.getAesJson(paramsJson, random);
            if (TextUtils.isEmpty(aseParams)) {
                data.writeCache();
                return;
            }
            if(TextUtils.isEmpty(AlxConfig.URL_DATA_ANALYTICS)){
                return;
            }
            final String url = AlxConfig.URL_DATA_ANALYTICS + random;
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        AlxLog.d(AlxLogLevel.DATA, TAG + "-url", url);
                        AlxLog.d(AlxLogLevel.DATA, TAG + "-params", paramsJson);
                        AlxLog.d(AlxLogLevel.DATA, TAG + "-params[aes]", aseParams);
                        Map<String, String> header = new HashMap<>();
                        header.put("Accept", "application/json");
                        AlxHttpRequest request = new AlxHttpRequest.Builder(url)
                                .setRequestMethod(AlxHttpMethod.POST)
                                .setContentType(true)
                                .setParams(aseParams)
                                .setOpenLog(false)
                                .setHeaders(header)
                                .builder();
                        AlxHttpResponse response = AlxHttpManager.getInstance().requestApiSync(request);

                        if (response != null) {
                            AlxLog.d(AlxLogLevel.DATA, TAG + "-response", response.getResponseMsg());
                        }
                        if (response == null || !response.isOk()) {
                            data.writeCache();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getRequestJson(List<AlxReportBean> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            JSONArray dataArray = new JSONArray();
            for (AlxReportBean item : list) {
                if (item == null || TextUtils.isEmpty(item.eventId)) {
                    continue;
                }
                int name = 0;
                try {
                    name = Integer.parseInt(item.eventId);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                JSONObject json = new JSONObject();
                json.put("id", UUID.randomUUID());
                json.put("type", 2);
                json.put("name", name);
                json.put("time", System.currentTimeMillis());
                JSONObject infoJson = AlxPackageData.getJsonByStr(item.json);
                if (infoJson != null) {
                    if (!TextUtils.isEmpty(item.desc)) {
                        infoJson.put("desc", item.desc);
                    }
                    json.put("info", infoJson);
                } else {
                    if (!TextUtils.isEmpty(item.desc)) {
                        infoJson = new JSONObject();
                        infoJson.put("desc", item.desc);
                        json.put("info", infoJson);
                    }
                }
                dataArray.put(json);
            }
            JSONObject json = new JSONObject();
            json.put("sdkv", AlxConst.SDK_VERSION);
            json.put("app_bundle_id", AlxAdBase.app_bundle_id);
            json.put("device", AlxSdkData.getDevice());
            json.put("event", dataArray);
            return json.toString();
        } catch (Exception e) {
            e.printStackTrace();
            AlxLog.e(AlxLogLevel.ERROR, TAG, "getRequestJson():" + e.getMessage());
            return null;
        }
    }


}