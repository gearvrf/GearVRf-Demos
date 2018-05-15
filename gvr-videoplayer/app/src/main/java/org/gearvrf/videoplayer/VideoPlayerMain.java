/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.videoplayer;

import android.os.Environment;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.videoplayer.component.VideoComponent;
import org.gearvrf.videoplayer.component.VideoControllerComponent;
import org.gearvrf.videoplayer.filter.VideosFileFilter;

import java.io.File;

public class VideoPlayerMain extends GVRMain {

    private static final String VIDEOS_DIR_NAME = "gvr-videoplayer";

    private GVRContext mContext;
    private GVRScene mScene;
    private VideoComponent mVideoComponent;
    private VideoControllerComponent mVideoControllerComponent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        createVideoComponent();
        createVideoControllerComponent();

        gvrContext.getInputManager().selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
            }
        });

        playFiles();
    }

    @Override
    public void onStep() {
    }

    private void playFiles() {
        File videosDirPath = new File(Environment.getExternalStorageDirectory(), VIDEOS_DIR_NAME);
        if (videosDirPath.exists() && videosDirPath.isDirectory()) {
            // Filter mp4 files
            File[] files = videosDirPath.listFiles(new VideosFileFilter());
            if (files.length > 0) {
                mVideoComponent.playFiles(files);
            }
        } else {
            mVideoComponent.playDefault(); // from assets folder
        }
    }

    private void createVideoComponent() {
        mVideoComponent = new VideoComponent(mContext, 8f, 4f);
        mVideoComponent.getTransform().setPosition(0.0f, 0.0f, -7.0f);
        mScene.addSceneObject(mVideoComponent);
    }

    private void createVideoControllerComponent() {
        mVideoControllerComponent = new VideoControllerComponent(mContext, 6f, 1f);
        mVideoControllerComponent.getTransform().setPosition(0.0f, -2.5f, -6.5f);
        mVideoControllerComponent.getTransform().rotateByAxis(-15, 1, 0, 0);
        mScene.addSceneObject(mVideoControllerComponent);
    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        if (mVideoComponent.isPlaying()) {
            mVideoComponent.pauseVideo();
        } else {
            mVideoComponent.playVideo();
        }
    }
}
