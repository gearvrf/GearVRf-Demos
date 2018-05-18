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

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.videoplayer.component.video.VideoPlayer;
import org.gearvrf.videoplayer.event.DefaultTouchEvent;
import org.gearvrf.videoplayer.filter.VideosFileFilter;

import java.io.File;

public class VideoPlayerMain extends GVRMain {

    private static final String TAG = VideoPlayerMain.class.getSimpleName();
    private static final String VIDEOS_DIR_NAME = "gvr-videoplayer";
    private static float PASSTHROUGH_DISTANCE = 5.0f;
    private static final float SCALE = 200.0f;

    private GVRContext mContext;
    private GVRScene mScene;
    private GVRCursorController mCursorController;
    private VideoPlayer mVideoPlayer;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        addSkyBoxSphere();
        initCursorController();
        addVideoPlayer();
        prepareVideos();
    }

    private void addVideoPlayer() {
        mVideoPlayer = new VideoPlayer(getGVRContext());
        mScene.addSceneObject(mVideoPlayer);
    }

    private void prepareVideos() {
        mVideoPlayer.prepare(getVideos());
    }

    private File[] getVideos() {
        File videosDirPath = new File(Environment.getExternalStorageDirectory(), VIDEOS_DIR_NAME);
        if (videosDirPath.exists() && videosDirPath.isDirectory()) {
            // Filter mp4 files
            return videosDirPath.listFiles(new VideosFileFilter());
        }
        return null;
    }

    private void addSkyBoxSphere() {

        GVRTexture texture = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.photosphere));
        GVRSphereSceneObject sphereObject = new GVRSphereSceneObject(mContext, 72, 144, false, texture);
        sphereObject.getTransform().setScale(SCALE, SCALE, SCALE);

        mScene.addSceneObject(sphereObject);
    }

    private void initCursorController() {

        mScene.getEventReceiver().addListener(mVideoPlayerHandle);

        final GVRSceneObject cursor = new GVRSceneObject(mContext,
                mContext.createQuad(0.2f * PASSTHROUGH_DISTANCE, 0.2f * PASSTHROUGH_DISTANCE),
                mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.cursor)));
        cursor.getRenderData().setDepthTest(true);
        cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);

        GVRInputManager inputManager = mContext.getInputManager();
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mVideoPlayerHandle);
                }
                mCursorController = newController;
                newController.addPickEventListener(mVideoPlayerHandle);
                newController.setCursor(cursor);
                newController.setCursorDepth(-PASSTHROUGH_DISTANCE);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
            }
        });
    }

    private ITouchEvents mVideoPlayerHandle = new DefaultTouchEvent() {
        @Override
        public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {
            rotationPlayer();
        }
    };

    private void rotationPlayer() {
        final float rotationX = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationX();
        final float rotationY = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationY();
        final float rotationZ = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationZ();
        final float rotationW = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationW();

        mVideoPlayer.getTransform().setRotation(rotationW, rotationX, rotationY, rotationZ);
    }
}
