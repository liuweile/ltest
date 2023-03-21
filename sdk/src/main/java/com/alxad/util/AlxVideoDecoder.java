package com.alxad.util;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.TextUtils;

import com.alxad.base.AlxAdNetwork;
import com.alxad.base.AlxLogLevel;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 获取视频封面图片类
 *
 * @author lwl
 * @date 2022-11-28
 */
public class AlxVideoDecoder {
    private static final String TAG = "AlxVideoDecoder";

    private static final long DEFAULT_FRAME = -1;

    /**
     * 获取视频封面图片
     *
     * @param path       视频地址
     * @param viewWidth  显示封面View的宽度
     * @param viewHeight 显示封面View的高度
     * @return
     */
    public static Bitmap getVideoFrame(String path, int viewWidth, int viewHeight) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Bitmap bitmap = getCacheVideoFrame(path, viewWidth, viewHeight);
        if (bitmap != null) {
            AlxLog.d(AlxLogLevel.MARK, TAG, "get cache bitmap");
            return bitmap;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            if (viewWidth > 10 && viewHeight > 10 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                bitmap = getVideoScaledFrame(retriever, viewWidth, viewHeight);
            } else {
                bitmap = getVideoOriginalFrame(retriever);
            }
            putCacheVideoFrame(bitmap, path, viewWidth, viewHeight);
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        } finally {
            try {
                retriever.release();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 按比例压缩图片
     *
     * @param retriever
     * @param viewWidth
     * @param viewHeight
     * @return
     * @throws Exception
     */
    @TargetApi(Build.VERSION_CODES.O_MR1)
    private static Bitmap getVideoScaledFrame(MediaMetadataRetriever retriever, int viewWidth, int viewHeight) throws Exception {
        int originalWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int originalHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        int orientation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

        AlxLog.d(AlxLogLevel.MARK, TAG, "videoFrame:viewWidth=" + viewWidth + ";viewHeight=" + viewHeight);
        AlxLog.d(AlxLogLevel.MARK, TAG, "videoFrame:videoWidth=" + originalWidth + ";videoHeight=" + originalHeight + ";orientation=" + orientation);
        if (orientation == 90 || orientation == 270) {
            int temp = originalWidth;
            originalWidth = originalHeight;
            originalHeight = temp;
        }

        if (Math.max(originalWidth, originalHeight) < 520) { //图片太小就不进行压缩
            return retriever.getFrameAtTime(DEFAULT_FRAME, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        }

        float scale = 1f;
        float minFactor = Math.min(originalHeight * 1f / viewHeight, originalWidth * 1f / viewWidth);
        if (minFactor > 0.01 && minFactor <= 1) {
            scale = minFactor;
        }
        AlxLog.d(AlxLogLevel.MARK, TAG, "videoFrame:scale=" + scale);

        int decodeWidth = Math.round(scale * originalWidth);
        int decodeHeight = Math.round(scale * originalHeight);

        return retriever.getScaledFrameAtTime(
                DEFAULT_FRAME, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, decodeWidth, decodeHeight);
    }

    /**
     * 获取视频原图
     *
     * @param retriever
     * @return
     * @throws Exception
     */
    private static Bitmap getVideoOriginalFrame(MediaMetadataRetriever retriever) throws Exception {
        return retriever.getFrameAtTime(DEFAULT_FRAME, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
    }

    /**
     * 获取缓存封面
     *
     * @param path
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private static Bitmap getCacheVideoFrame(String path, int viewWidth, int viewHeight) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            String dir = AlxFileUtil.getVideoSavePath(AlxAdNetwork.getContext());
            String fileName = getFileName(path, viewWidth, viewHeight);
            File file = new File(dir, fileName);
            bitmap = BitmapFactory.decodeFile(file.getPath());
        } catch (Exception e) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
        }
        return bitmap;
    }

    /**
     * 将封面缓存
     *
     * @param bitmap
     * @param path
     * @param viewWidth
     * @param viewHeight
     */
    private static void putCacheVideoFrame(final Bitmap bitmap, final String path, final int viewWidth, final int viewHeight) {
        if (bitmap == null || TextUtils.isEmpty(path)) {
            return;
        }
        AlxAdNetwork.execute(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fos = null;
                try {
                    String dir = AlxFileUtil.getVideoSavePath(AlxAdNetwork.getContext());
                    String fileName = getFileName(path, viewWidth, viewHeight);
                    File file = new File(dir, fileName);
                    if (file.exists()) {
                        return;
                    }
                    File fileDir = new File(dir);
                    if (!fileDir.exists()) {
                        fileDir.mkdirs();
                    }
                    file.createNewFile();

                    fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (Exception e) {
                    AlxLog.e(AlxLogLevel.ERROR, TAG, e.getMessage());
                } finally {
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (Exception e1) {

                    }
                }
            }
        });
    }

    private static String getFileName(String path, int viewWidth, int viewHeight) throws Exception {
        String imgPrefix = "img";
        String fileName = imgPrefix + MD5Util.getUPMD5(path + "&width=" + viewWidth + "&height=" + viewHeight);
        return fileName;
    }

}