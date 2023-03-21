package com.alxad.http.download;

import com.alxad.http.AlxHttpResponse;

import java.io.Serializable;

public class AlxDownloadNotify implements Serializable {

    public final Object waitLock = new Object();

    public String url;

    public AlxHttpResponse response = null;

}