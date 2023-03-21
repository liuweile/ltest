package com.alxad.http;

import java.io.File;

public interface AlxDownLoadCallback {


    void onSuccess(File file);

    void onError(int code, String msg);

    void onProgress(int progress);


}