package org.gearvrf.videoplayer.component;

public interface OnVideoControllerListener {

    void onPlay();

    void onPause();

    void onBack();

    void onSeek(long progress);
}
