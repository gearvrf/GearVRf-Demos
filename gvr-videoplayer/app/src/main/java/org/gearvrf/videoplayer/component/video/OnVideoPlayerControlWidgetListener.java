package org.gearvrf.videoplayer.component.video;

public interface OnVideoPlayerControlWidgetListener {

    void onPlay();

    void onPause();

    void onBack();

    void onSeek(long progress);
}
