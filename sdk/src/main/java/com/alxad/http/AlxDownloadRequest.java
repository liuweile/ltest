package com.alxad.http;

/**
 * 下载请求对象
 *
 * @author liuweile
 * @date 2021-3-22
 */
public class AlxDownloadRequest {

    private String url; //下载url
    private String downloadDir; //文件存放目录
    private String fileName; //自定义文件名
    private AlxDownLoadCallback callback; //异步下载回调

    public AlxDownloadRequest(String url, String downloadDir) {
        this.url = url;
        this.downloadDir = downloadDir;
    }

    public AlxDownloadRequest fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 异步请求
     */
    public void asyncRequest() {
        AlxDownloadManager.getInstance().asyncDownload(this);
    }

    /**
     * 异步请求
     */
    public void asyncRequest(AlxDownLoadCallback callback) {
        this.callback = callback;
        AlxDownloadManager.getInstance().asyncDownload(this);
    }

    /**
     * 同步请求
     *
     * @return
     */
    public AlxHttpResponse syncRequest() {
        return AlxDownloadManager.getInstance().syncDownload(this);
    }

    public String getUrl() {
        return url;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public String getFileName() {
        return fileName;
    }

    public AlxDownLoadCallback getCallback() {
        return callback;
    }

}
