package com.alxad.glittle.util;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

public class Executors {

    private Executors() {
        // Utility class.
    }

    private static final Executor MAIN_THREAD_EXECUTOR =
            new Executor() {
                @Override
                public void execute(@NonNull Runnable command) {
                    Util.postOnUiThread(command);
                }
            };

    public static Executor mainThreadExecutor() {
        return MAIN_THREAD_EXECUTOR;
    }

}
