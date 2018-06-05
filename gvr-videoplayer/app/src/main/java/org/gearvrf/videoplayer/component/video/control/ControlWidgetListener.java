package org.gearvrf.videoplayer.component.video.control;

public interface ControlWidgetListener {

    void onPlay();

    void onPause();

    void onBack();

    void onSeek(long progress);
}
