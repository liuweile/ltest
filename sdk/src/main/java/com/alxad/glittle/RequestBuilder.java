package com.alxad.glittle;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.base.AlxLogLevel;
import com.alxad.glittle.request.BaseRequestOptions;
import com.alxad.glittle.request.Request;
import com.alxad.glittle.request.RequestListener;
import com.alxad.glittle.request.SingleRequest;
import com.alxad.glittle.target.CustomViewTarget;
import com.alxad.glittle.target.Target;
import com.alxad.glittle.util.ClassTypeFactory;
import com.alxad.glittle.util.Executors;
import com.alxad.glittle.util.Util;
import com.alxad.util.AlxLog;

import java.util.concurrent.Executor;

public class RequestBuilder<TranscodeType> extends BaseRequestOptions<RequestBuilder<TranscodeType>> {
    private static final String TAG = "RequestBuilder";

    private Glittle glitter;
    private RequestManager requestManager;
    private Context context;
    private Class<TranscodeType> transcodeClass;

    private String path;

    public RequestBuilder(Glittle glitter, RequestManager requestManager, Class<TranscodeType> transcodeClass, Context context) {
        this.glitter = glitter;
        this.requestManager = requestManager;
        this.context = context;
        this.transcodeClass = transcodeClass;
    }

    public RequestBuilder<TranscodeType> load(@Nullable String path) {
        this.path = path;
        return this;
    }

    public CustomViewTarget<ImageView, TranscodeType> into(@NonNull ImageView view) {
        if (!Util.isOnMainThread()) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "You must call this method on the main thread");
            return null;
        }
        if (view == null) {
            AlxLog.e(AlxLogLevel.ERROR, TAG, "View must not be null");
            return null;
        }
//        ObjectCheck.assertMainThread();
//        ObjectCheck.checkNotNull(view);
        return into(ClassTypeFactory.buildTarget(view, transcodeClass), null, this, Executors.mainThreadExecutor());
    }

    @NonNull
    public <Y extends Target<TranscodeType>> Y into(@NonNull Y target) {
        return into(target, null, this, Executors.mainThreadExecutor());
    }

    private <Y extends Target<TranscodeType>> Y into(
            @NonNull Y target,
            @Nullable RequestListener<TranscodeType> targetListener,
            BaseRequestOptions<?> options,
            Executor callbackExecutor) {
//        ObjectCheck.checkNotNull(target);
        if (target == null) {
            return null;
        }
        try { //添加异常防范于未然
            Request request = buildRequest(target, targetListener, options, callbackExecutor);
            Request previous = target.getRequest();

            if (previous != null && request.isEquivalentTo(previous)) {
                previous.begin();
                return target;
            }
            requestManager.clear(target);
            target.setRequest(request);
            requestManager.track(target, request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return target;
    }

    private Request buildRequest(
            Target<TranscodeType> target,
            @Nullable RequestListener<TranscodeType> targetListener,
            BaseRequestOptions<?> requestOptions,
            Executor callbackExecutor) {
        return SingleRequest.obtain(context, new Object(), path, transcodeClass, requestOptions, requestOptions.getOverrideWidth(), requestOptions.getOverrideHeight(), target, targetListener, null, callbackExecutor);
    }

}