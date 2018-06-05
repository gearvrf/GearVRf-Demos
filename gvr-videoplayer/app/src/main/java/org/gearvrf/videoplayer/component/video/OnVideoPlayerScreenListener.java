package org.gearvrf.videoplayer.component.video;

public interface OnVideoPlayerScreenListener {

    void onProgress(long progress);

    void onPrepareFile(String title, long duration);

    void onStart();

    void onLoading();

    void onFileEnd();

    void onAllFilesEnd();
}
