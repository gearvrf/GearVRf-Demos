package org.gearvrf.videoplayer.component.video.screen;

public interface OnScreenListener {

    void onProgress(long progress);

    void onPrepareFile(String title, long duration);

    void onStart();

    void onLoading();

    void onFileEnd();

    void onAllFilesEnd();
}
