package org.gearvrf.videoplayer.component.video;

import android.support.annotation.CallSuper;

class VideoPlayerListenerDispatcher implements OnVideoPlayerListener {

    private OnVideoPlayerListener mOnVideoPlayerListener;

    @CallSuper
    @Override
    public void onProgress(long progress) {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onProgress(progress);
        }
    }

    @CallSuper
    @Override
    public void onPrepare(String title, long duration) {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onPrepare(title, duration);
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
    public void onEnd() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onEnd();
        }
    }

    @CallSuper
    @Override
    public void onAllEnd() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onAllEnd(
            );
        }
    }

    public void setOnVideoPlayerListener(OnVideoPlayerListener listener) {
        this.mOnVideoPlayerListener = listener;
    }
}
