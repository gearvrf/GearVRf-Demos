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

import java.io.File;

public class VideoPlayerMain extends GVRMain {

    private static final String VIDEOS_DIR_NAME = "gvr-videoplayer";
    private VideoComponent mVideoComponent;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {
        GVRScene scene = gvrContext.getMainScene();
        scene.addSceneObject(mVideoComponent = createVideoComponent(gvrContext));
        File videosDirPath = new File(Environment.getExternalStorageDirectory(), VIDEOS_DIR_NAME);
        if (videosDirPath.exists() && videosDirPath.isDirectory()) {
            File[] files = videosDirPath.listFiles(new VideosFileFilter());
            if (files.length > 0) {
                mVideoComponent.playFiles(files);
            }
        } else {
            mVideoComponent.playDefault(); // from assets folder
        }
    }

    @Override
    public void onStep() {
    }

    private VideoComponent createVideoComponent(GVRContext gvrContext) {
        VideoComponent videoComponent = new VideoComponent(gvrContext, 8f, 4f);
        videoComponent.getTransform().setPosition(0.0f, 0.0f, -9.0f);
        return videoComponent;
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
