package com.alxad.http;


import android.text.TextUtils;

import com.alxad.api.AlxAdError;
import com.alxad.base.AlxLogLevel;
import com.alxad.util.AlxLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class AlxDownloadRealCall extends AlxBaseCall {

    private AlxDownLoadCallback mCallback;
    private boolean isCancelDownload = false;//是否取消下载
    private String mFileName;
    private String mTempFileName;//临时文件
    public static final String DOWNLOADING_SUFFIX = ".download";//文件正在下载中的临时后缀名

    public AlxDownloadRealCall(AlxHttpRequest request, AlxDownLoadCallback callback) {
        this.mRequest = request;
        if (mRequest != null) {
            mRequestCode = mRequest.getRequestCode();
        }
        this.mCallback = callback;
        //设置下载的 连接超时时间 和 读取超时时间
        mReadTimeout = 20000;
        mConnectTimeout = 20000;
    }

    /**
     * 设置下载文件名(可以设置，不设置时用默认的)
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    @Override
    public AlxHttpResponse sendCall() {
        String isVerify = isVerifyUrl();
        if (!AlxHttpUtil.isEmpty(isVerify)) {
            AlxHttpResponse response = new AlxHttpResponse();
            response.setResponseCode(AlxAdError.ERR_PARAMS_ERROR);
            response.setResponseMsg(isVerify);
            response.setRequestCode(mRequestCode);
            return response;
        }
        if (mRequest.isShowLog()) {
            AlxLog.d(AlxLogLevel.DATA, TAG + "-url", mRequest.getUrl());
            AlxLog.d(AlxLogLevel.DATA, TAG + "-params", mRequest.getParams());
        }
        AlxHttpResponse response = loadUrl(mRequest.getUrl());
        if (mRequest.isShowLog()) {
            logResponse(response);
        }
        return response;
    }

    /**
     * 用户手动取消下载
     */
    public void cancel() {
        isCancelDownload = true;
    }


    private String isVerifyUrl() {
        if (mRequest == null) {
            return "request obj is empty";
        }
        if (AlxHttpUtil.isEmpty(mRequest.getRequestMethod())) {
            return "request method is empty";
        }
        if (AlxHttpUtil.isEmpty(mRequest.getUrl())) {
            return "url is empty";
        }
        if (AlxHttpUtil.isEmpty(mRequest.getDownloadDir())) {
            return "Download Dir is empty";
        }
        return null;
    }

    @Override
    protected String getResponseData(String url, InputStream is, HttpURLConnection connection, AlxHttpResponse response) throws Exception {
        String contentLengthStr = connection.getHeaderField("content-length");
        long contentLength = getContentLength(contentLengthStr);
        if (TextUtils.isEmpty(mFileName)) {
            mFileName = AlxHttpUtil.getDownloadFileName(mRequest.getUrl());
        }
        mTempFileName = mFileName + DOWNLOADING_SUFFIX;
        return getResponseFile(is, contentLength, mRequest.getDownloadDir(), response);
    }

    /**
     * 使用IO 下载文件
     *
     * @param is
     * @param totalLength
     * @param strPath
     * @param response
     * @return
     * @throws Exception
     */
    protected String getResponseFile(InputStream is, long totalLength, String strPath, AlxHttpResponse response) throws Exception {
        File tempFile = null;
        File file = null;
        String result = null;
        OutputStream output = null;
        try {
            tempFile = AlxHttpUtil.deleteFile(strPath, mTempFileName);
            file = AlxHttpUtil.deleteFile(strPath, mFileName);
            tempFile.createNewFile();

            output = new FileOutputStream(tempFile);
            byte[] buffer = new byte[AlxBaseCall.BUFFER_SIZE];
            int length = -1;
            int progress = 0;
            int currentProgress = -1;
            while ((length = is.read(buffer)) != -1) {
                if (isCancelDownload) {
                    break;
                }
                output.write(buffer, 0, length);
                if (totalLength > 0) {
                    progress += length;
                    currentProgress = (int) (progress * 1.0 / totalLength * 100);
                    callProgress(currentProgress);
                }
            }
            output.flush();
            if (isCancelDownload) {
                AlxHttpUtil.deleteFile(tempFile);
                response.setResponseCode(AlxHttpErrorStatus.CANCEL_DOWNLOAD);
                response.setResponseMsg(AlxHttpErrorStatus.RESPONSE_CANCEL_DOWNLOAD);
                result = response.getResponseMsg();
            } else {
                if (currentProgress < 100) {
                    currentProgress = 100;
                    callProgress(currentProgress);
                }
                file = AlxHttpUtil.fileRename(tempFile, file);
                if (file.exists()) {
                    result = file.getPath();
                } else {
                    result = AlxHttpErrorStatus.RESPONSE_DOWNLOAD_ERROR;
                    response.setResponseCode(AlxHttpErrorStatus.DOWNLOAD_ERROR);
                    response.setResponseMsg(AlxHttpErrorStatus.RESPONSE_DOWNLOAD_ERROR);
                }
            }
        } catch (Exception e) {
            AlxHttpUtil.deleteFile(file);
            AlxHttpUtil.deleteFile(tempFile);
            throw e;
        } finally {
            AlxHttpUtil.closeQuietly(output);
        }
        return result;
    }

    /**
     * 获取下载文件长度
     *
     * @param contentLength
     * @return
     */
    private long getContentLength(String contentLength) {
        int defaultValue = -1;
        if (contentLength == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(contentLength);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int repeatNum = -1;

    private void callProgress(int progress) {
        if (repeatNum != progress && repeatNum <= 100 && progress >= 0) {
            repeatNum = progress;
            if (mCallback != null) {
                mCallback.onProgress(progress);
            }
        }
    }

    @Override
    public void onError(int code, String error) {

    }

    /**
     * 使用NIO下载文件
     *
     * @param is
     * @param totalLength
     * @param strPath
     * @param response
     * @return
     * @throws Exception
     */
//    protected String getResponseFile2(InputStream is, long totalLength, String strPath, AlxHttpResponse response) throws Exception {
//        File tempFile = null;
//        File file = null;
//        String result = null;
//        FileChannel fileChannel = null;
//        ReadableByteChannel inputChannel = null;
//        try {
//            tempFile = AlxHttpUtil.deleteFile(strPath, mTempFileName);
//            file = AlxHttpUtil.deleteFile(strPath, mFileName);
//            tempFile.createNewFile();
//
//            inputChannel = Channels.newChannel(is);
//            fileChannel = new FileOutputStream(tempFile).getChannel();
//
//            //方式一：循环写入
//            int length = -1;
//            int progress = 0;
//            int currentProgress = -1;
//            ByteBuffer byteBuffer = ByteBuffer.allocate(AlxBaseCall.BUFFER_SIZE);
//            while ((length = inputChannel.read(byteBuffer)) != -1) {
//                if (isCancelDownload) {
//                    break;
//                }
//
//                byteBuffer.flip();
//                fileChannel.write(byteBuffer);
//                byteBuffer.clear();
//
//                if (totalLength > 0) {
//                    progress += length;
//                    currentProgress = (int) (progress * 1.0 / totalLength * 100);
//                    callProgress(currentProgress);
//                }
//            }
//
//            //方式二：直接写入
////            fileChannel.transferFrom(inputChannel, 0, totalLength);
//
//            if (isCancelDownload) {
//                AlxHttpUtil.deleteFile(tempFile);
//                response.setResponseCode(AlxHttpErrorStatus.CANCEL_DOWNLOAD);
//                response.setResponseMsg(AlxHttpErrorStatus.RESPONSE_CANCEL_DOWNLOAD);
//                result = response.getResponseMsg();
//            } else {
//                if (currentProgress < 100) {
//                    currentProgress = 100;
//                    callProgress(currentProgress);
//                }
//                file = AlxHttpUtil.fileRename(tempFile, file);
//                if (file.exists()) {
//                    result = file.getPath();
//                } else {
//                    result = AlxHttpErrorStatus.RESPONSE_DOWNLOAD_ERROR;
//                    response.setResponseCode(AlxHttpErrorStatus.DOWNLOAD_ERROR);
//                    response.setResponseMsg(AlxHttpErrorStatus.RESPONSE_DOWNLOAD_ERROR);
//                }
//            }
//        } catch (Exception e) {
//            AlxHttpUtil.deleteFile(file);
//            AlxHttpUtil.deleteFile(tempFile);
//            throw e;
//        } finally {
//            AlxHttpUtil.closeQuietly(fileChannel);
//            AlxHttpUtil.closeQuietly(inputChannel);
//        }
//        return result;
//    }


}