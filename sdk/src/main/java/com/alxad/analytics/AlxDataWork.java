package com.alxad.analytics;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxFileUtil;
import com.alxad.util.AlxLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;


/**
 * 埋点上报业务层
 *
 * @author liuweile
 * @date 2021-3-22
 */
public class AlxDataWork {
    private static final String TAG = "AlxDataWork";

    private static final int MAX_NUM = 5;//数量达到最大值时上报
    private static final int INTERVAL_TIME = 10;//单位s。当数据没有达到最大值时，每隔一段时间就会上报
    private static final int WHAT_EVENT = 100;
    private static final int WHAT_TIME = 101;
    private static final String EXTRA_DIR = "dir";
    private static final String EXTRA_DATA = "data";

    private WorkHandler workHandler;

    private static class SingleHolder {
        private static AlxDataWork instance = new AlxDataWork();
    }

    public static AlxDataWork getInstance() {
        return SingleHolder.instance;
    }

    private AlxDataWork() {
        workHandler = new WorkHandler("data-handler");
    }

    public void sendError(Context context, String eventId, Throwable e) {
        String json = getError(null, e);
        if (TextUtils.isEmpty(json)) {
            return;
        }
        handlerEvent(context, eventId, null, json);
    }

    public void sendEvent(Context context, String eventId, String desc, Map<String, Object> map) {
        String json = AlxPackageData.getJsonByMap(map);
        handlerEvent(context, eventId, desc, json);
    }

    private void handlerEvent(Context context, String eventId, String desc, String json) {
        if (context == null || TextUtils.isEmpty(eventId)) {
            return;
        }
        try {
            if (workHandler == null || workHandler.getHandler() == null) {
                return;
            }
            AlxReportBean bean = new AlxReportBean();
            bean.eventId = eventId;
            bean.desc = desc;
            bean.json = json;

            String dir = AlxFileUtil.getAnalyticsPath(context);
            Message message = workHandler.getHandler().obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_DIR, dir);
            bundle.putSerializable(EXTRA_DATA, bean);
            message.what = WHAT_EVENT;
            message.setData(bundle);
            workHandler.getHandler().removeCallbacksAndMessages(null);
            workHandler.getHandler().sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class WorkHandler extends HandlerThread {

        private Handler mHandler;

        private WorkHandler(String name) {
            super(name);
            start();
            mHandler = new Handler(getLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case WHAT_EVENT:
                            handleEvent(msg);
                            break;
                        case WHAT_TIME:
                            handleTime(msg);
                            break;
                    }
                }
            };
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

        private void handleEvent(Message msg) {
            if (msg == null || msg.getData() == null) {
                return;
            }
            try {
                Bundle bundle = msg.getData();
                AlxReportBean bean = (AlxReportBean) bundle.getSerializable(EXTRA_DATA);
                String dir = bundle.getString(EXTRA_DIR, null);
                if (TextUtils.isEmpty(dir) || bean == null || TextUtils.isEmpty(bean.eventId)) {
                    return;
                }

                AlxPackageData dataObj = new AlxPackageData();
                dataObj.handleDataWork(dir, bean);
                if (dataObj.getData() == null) {
                    mHandler.removeCallbacksAndMessages(null);
                    return;
                }
                int size = dataObj.getData().size();
                if (dataObj.getData().size() < MAX_NUM) {
                    AlxLog.i(AlxLogLevel.DATA, TAG, "num: " + size + "<" + MAX_NUM);
                    dataObj.writeCache();
                    Message message = new Message();
                    message.what = WHAT_TIME;
                    message.obj = dir;
                    mHandler.sendMessageDelayed(message, INTERVAL_TIME * 1000);
                } else {
                    AlxLog.i(AlxLogLevel.DATA, TAG, "num:" + size + ">=" + MAX_NUM);
                    mHandler.removeCallbacksAndMessages(null);
                    new AlxAgentRequest().sendRequest(dataObj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void handleTime(Message msg) {
            if (msg == null || msg.obj == null || !(msg.obj instanceof String)) {
                return;
            }
            try {
                String dir = (String) msg.obj;
                AlxPackageData dataObj = new AlxPackageData();
                dataObj.handleDataWork(dir, null);
                if (dataObj.getData() == null) {
                    return;
                }
                new AlxAgentRequest().sendRequest(dataObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized String readFileData(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        String result = null;
        RandomAccessFile raf = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                return null;
            }
            if (!file.isFile()) {
                return null;
            }
            raf = new RandomAccessFile(file, "rws");
            byte[] buffer = new byte[4 * 1024];
            int length;
            StringBuilder sb = new StringBuilder();
            while ((length = raf.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, length, "UTF-8"));
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 写入缓存
     *
     * @param dir
     * @param fileName
     * @param data     如果有数据就写入，如果没有数据就清楚缓存
     */
    public synchronized void writeFileData(String dir, String fileName, String data) {
        if (TextUtils.isEmpty(dir) || TextUtils.isEmpty(fileName)) {
            return;
        }
        RandomAccessFile raf = null;
        try {
            File fileDir = new File(dir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            File file = new File(fileDir.getPath(), fileName);
            if (TextUtils.isEmpty(data)) {
                if (file.exists()) {
                    file.delete();
                }
                return;
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            raf = new RandomAccessFile(file, "rws");
            long length = raf.length();
            raf.seek(length);
            raf.write(data.getBytes("UTF-8"));
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static String getError(String msg, Throwable e) {
        if (TextUtils.isEmpty(msg) && e == null) {
            return null;
        }
        try {
            JSONObject json = new JSONObject();
            if (e != null) {
                if (!TextUtils.isEmpty(msg)) {
                    json.put("msg", msg);
                }
                json.put("extype", e.getClass().getName() + ": " + e.getMessage());
                StackTraceElement[] elements = e.getStackTrace();
                if (elements != null && elements.length > 0) {
                    JSONArray arrays = new JSONArray();
                    for (StackTraceElement item : elements) {
                        arrays.put(String.valueOf(item));
                    }
                    json.put("ex", arrays);
                }
            } else {
                json.put("msg", msg);
            }
            return json.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }


}