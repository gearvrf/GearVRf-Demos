/*
 * Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gearvrf.videoplayer.component.video.player;

import android.support.annotation.CallSuper;

public class PlayerListenerDispatcher implements OnPlayerListener {

    private OnPlayerListener mOnVideoPlayerListener;

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
    public void onStartBuffering() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onStartBuffering();
        }
    }

    @CallSuper
    @Override
    public void onEndBuffering() {
        if (mOnVideoPlayerListener != null) {
            mOnVideoPlayerListener.onEndBuffering();
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

    public void setOnVideoPlayerListener(OnPlayerListener listener) {
        this.mOnVideoPlayerListener = listener;
    }
}
