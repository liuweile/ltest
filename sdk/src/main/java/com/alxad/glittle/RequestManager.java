package com.alxad.glittle;


import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alxad.glittle.request.Request;
import com.alxad.glittle.target.Target;

public class RequestManager {
    private Glittle glitter;
    private Context context;

    public RequestManager(Glittle glitter, Context context) {
        this.glitter = glitter;
        this.context = context;
    }

    public RequestBuilder<Drawable> load(String path) {
        return new RequestBuilder<>(glitter, this, Drawable.class, context).load(path);
    }

    public void clear(@Nullable final Target<?> target) {
        if (target == null) {
            return;
        }
        Request request = target.getRequest();
        target.setRequest(null);
        if (request != null) {
            request.clear();
        }

//        untrackOrDelegate(target);
    }

    public synchronized void track(@NonNull Target<?> target, @NonNull Request request) {
//        targetTracker.track(target);
//        requestTracker.runRequest(request);
        if (request != null) {
            request.begin();
        }
    }


}