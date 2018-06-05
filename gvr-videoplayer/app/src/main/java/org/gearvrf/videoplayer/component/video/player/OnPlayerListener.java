package org.gearvrf.videoplayer.component.video.player;

public interface OnPlayerListener {

    void onProgress(long progress);

    void onPrepareFile(String title, long duration);

    void onStart();

    void onLoading();

    void onFileEnd();

    void onAllFilesEnd();
}
