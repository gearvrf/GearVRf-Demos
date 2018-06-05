package org.gearvrf.videoplayer.component.video.screen;

import android.support.annotation.CallSuper;

public class ScreenListenerDispatcher implements OnScreenListener {

    private OnScreenListener mOnVideoPlayerListener;

    @CallSuper
    @Override
    public void onProgress(long progress) {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onProgress(progress);
        }
    }

    @CallSuper
    @Override
    public void onPrepareFile(String title, long duration) {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onPrepareFile(title, duration);
        }
    }

    @CallSuper
    @Override
    public void onStart() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onStart();
        }
    }

    @CallSuper
    @Override
    public void onLoading() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onLoading();
        }
    }

    @CallSuper
    @Override
    public void onFileEnd() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onFileEnd();
        }
    }

    @CallSuper
    @Override
    public void onAllFilesEnd() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onAllFilesEnd(
            );
        }
    }

    public void setOnVideoPlayerListener(OnScreenListener listener) {
        this.mOnVideoPlayerListener = listener;
    }
}
