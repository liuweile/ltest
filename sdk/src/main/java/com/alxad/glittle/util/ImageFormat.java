package com.alxad.glittle.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.alxad.base.AlxAdNetwork;
import com.alxad.glittle.gif.AlxGifDrawable;
import com.alxad.glittle.request.BaseRequestOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ImageFormat {

    private final static byte[] GIF_HEADER = new byte[]{'G', 'I', 'F'};
    private final static Object gifDecodeLock = new Object();

    public static Drawable decodeFile(final File file, final BaseRequestOptions options) {
        Drawable result = null;
        try {
            if (file == null || !file.exists() || file.length() < 1) {
                return null;
            }
            if (isGif(file)) {
                Movie movie = null;
                synchronized (gifDecodeLock) { // decode with lock
                    movie = decodeGif(file);
                }
                if (movie != null) {
                    result = new AlxGifDrawable(movie, (int) file.length());
                }
            } else {
                Bitmap bitmap = null;
                if (options == null || !options.isCompress()) {
                    bitmap = decodeBitmap(file);
                } else {
                    bitmap = getImageThumbnail(file.getPath(), options.getViewWidth(), options.getViewHeight());
                }
                if (bitmap != null) {
                    Context context = AlxAdNetwork.getContext();
                    if (context == null) {
                        result = new BitmapDrawable(bitmap);
                    } else {
                        result = new BitmapDrawable(context.getResources(), bitmap);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 转化文件为Bitmap.
     */
    public static Bitmap decodeBitmap(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inScaled=true;
//            options.inSampleSize=2;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            bitmap = BitmapFactory.decodeFile(file.getPath(), options);
        } catch (Exception e) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            bitmap = null;
        }
        return bitmap;
    }


    /**
     * 根据指定的图像路径和大小来获取缩略图
     * 此方法有两点好处：
     * 1. 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
     * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
     * 2. 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使
     * 用这个工具生成的图像不会被拉伸。
     *
     * @param imagePath 图像的路径
     * @param width     指定输出图像的宽度
     * @param height    指定输出图像的高度
     * @return 生成的缩略图
     */
    private static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        if (TextUtils.isEmpty(imagePath)) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            // 获取这个图片的宽和高，注意此处的bitmap为null
            BitmapFactory.decodeFile(imagePath, options);
            options.inJustDecodeBounds = false; // 设为 false
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            // 计算缩放比
            int h = options.outHeight; //图片的宽高
            int w = options.outWidth;
            options.inSampleSize = calculateSampleSize(w, h, width, height);
            // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
            // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
//            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算压缩采样倍数
     *
     * @param rawWidth  图片宽度
     * @param rawHeight 图片高度
     * @param maxWidth  最大宽度
     * @param maxHeight 最大高度
     * @return 压缩采样倍数
     */
    public static int calculateSampleSize(final int rawWidth, final int rawHeight,
                                          final int maxWidth, final int maxHeight) {
        int sampleSize = 1;
        try {
            // 计算缩放比
            int beWidth = rawWidth / maxWidth;
            int beHeight = rawHeight / maxHeight;
            if (beWidth < beHeight) {
                sampleSize = beWidth;
            } else {
                sampleSize = beHeight;
            }
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sampleSize;
    }

    public static boolean isGif(File file) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] header = readBytes(in, 0, 3);
            return Arrays.equals(GIF_HEADER, header);
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {

                }
            }
        }
        return false;
    }

    /**
     * 转换文件为Movie, 可用于创建GifDrawable.
     */
    public static Movie decodeGif(File file) throws IOException {
        {// check params
            if (file == null || !file.exists() || file.length() < 1) return null;
        }
        try {
            Movie movie = Movie.decodeFile(file.getAbsolutePath());
            if (movie == null) {
                throw new IOException("decode image error");
            }
            return movie;
        } catch (IOException ex) {
            throw ex;
        } catch (Throwable ex) {
            return null;
        }
    }

    public static byte[] readBytes(InputStream in, long skip, int size) throws Exception {
        byte[] result = null;
        if (skip > 0) {
            long skipped = 0;
            while (skip > 0 && (skipped = in.skip(skip)) > 0) {
                skip -= skipped;
            }
        }
        result = new byte[size];
        for (int i = 0; i < size; i++) {
            result[i] = (byte) in.read();
        }
        return result;
    }

}
