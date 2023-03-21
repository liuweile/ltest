package com.alxad.widget.video;


public interface AlxVideoPlayerControllerListener {

    void onCompletion();

    void onError(int what, int extra);

    void onMediaPlayerError(String error);

    void onVideoSizeChanged(int width, int height);

    void onPrepared();

    void onInfo(int what, int extra);

}