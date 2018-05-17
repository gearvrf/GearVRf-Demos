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
import android.util.Log;
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
import org.gearvrf.videoplayer.component.OnVideoControllerListener;
import org.gearvrf.videoplayer.component.OnVideoPlayerListener;
import org.gearvrf.videoplayer.component.VideoComponent;
import org.gearvrf.videoplayer.component.VideoControllerComponent;
import org.gearvrf.videoplayer.filter.VideosFileFilter;

import java.io.File;
import java.util.EnumSet;

public class VideoPlayerMain extends GVRMain {

    private static final String TAG = VideoPlayerMain.class.getSimpleName();
    private static final String VIDEOS_DIR_NAME = "gvr-videoplayer";
    private static float PASSTHROUGH_DISTANCE = 100.0f;
    private static final float SCALE = 200.0f;

    private GVRContext mContext;
    private GVRScene mScene;
    private VideoComponent mVideoComponent;
    private VideoControllerComponent mVideoControllerComponent;
    private GVRCursorController mCursorController;
    private GVRSphereSceneObject sphereObject = null;
    private GVRSceneObject configuringScene = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        addSkyBoxSphere();
        initCursorController();
        createVideoComponent();
        createVideoControllerComponent();
        configuringScene = new GVRSceneObject(mContext);
        configuringScene.addChildObject(mVideoComponent);
        configuringScene.addChildObject(mVideoControllerComponent);
        mScene.addSceneObject(configuringScene);
        playFiles();
    }

    private void addSkyBoxSphere() {
        GVRTexture texture = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.photosphere));

        sphereObject = new GVRSphereSceneObject(mContext, 72, 144, false, texture);

        sphereObject.getTransform().setScale(SCALE, SCALE, SCALE);

        mScene.addSceneObject(sphereObject);

        initPlayerComponents();
        playFiles();
    }

    private void playFiles() {
        File videosDirPath = new File(Environment.getExternalStorageDirectory(), VIDEOS_DIR_NAME);
        if (videosDirPath.exists() && videosDirPath.isDirectory()) {
            // Filter mp4 files
            File[] files = videosDirPath.listFiles(new VideosFileFilter());
            if (files.length > 0) {
                mVideoComponent.prepare(files);
            }
        } else {
            mVideoComponent.playDefault(); // from assets folder
        }
    }

    private void createVideoComponent() {
        mVideoComponent = new VideoComponent(mContext, 8f, 4f);
        mVideoComponent.getTransform().setPosition(0.0f, 0.0f, -7.0f);

    }

    private void createVideoControllerComponent() {
        mVideoControllerComponent = new VideoControllerComponent(mContext, 6f, 1f);
        mVideoControllerComponent.getTransform().setPosition(0.0f, -2.5f, -6.5f);
        mVideoControllerComponent.getTransform().rotateByAxis(-15, 1, 0, 0);

    }

    private void initCursorController() {
        mScene.getEventReceiver().addListener(mVideoPlayerHandle);
        GVRInputManager inputManager = mContext.getInputManager();
        configuringScene = new GVRSceneObject(mContext,
                mContext.createQuad(0.2f * PASSTHROUGH_DISTANCE,
                        0.2f * PASSTHROUGH_DISTANCE),
                mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext,
                        R.raw.cursor)));
        configuringScene.getRenderData().setDepthTest(false);
        configuringScene.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mVideoPlayerHandle);
                }
                mCursorController = newController;
                newController.addPickEventListener(mVideoPlayerHandle);
                newController.setCursor(configuringScene);
                newController.setCursorDepth(-PASSTHROUGH_DISTANCE);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

    private ITouchEvents mVideoPlayerHandle = new ITouchEvents() {
        @Override
        public void onEnter(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onExit(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onTouchEnd(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

        @Override
        public void onInside(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

        }

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

        configuringScene.getTransform().setRotation(rotationW, rotationX, rotationY, rotationZ);
    }

    private void initPlayerComponents() {

        mVideoComponent.setOnVideoPlayerListener(new OnVideoPlayerListener() {
            @Override
            public void onProgress(long progress) {
                mVideoControllerComponent.setProgress((int) progress);
            }

            @Override
            public void onPrepare(String title, long duration) {
                Log.d(TAG, "Video prepared: {title: " + title + ", duration: " + duration + "}");
                mVideoControllerComponent.setPlayPauseButtonEnabled(true);
                mVideoControllerComponent.setTitle(title);
                mVideoControllerComponent.setMaxProgress((int) duration);
                mVideoControllerComponent.setProgress((int) mVideoComponent.getProgress());
                mVideoControllerComponent.showPlay();
            }

            @Override
            public void onStart() {
                mVideoControllerComponent.setPlayPauseButtonEnabled(true);
                Log.d(TAG, "Video started");
                mVideoControllerComponent.showPause();
            }

            @Override
            public void onLoading() {
                Log.d(TAG, "Video loading");
                mVideoControllerComponent.setPlayPauseButtonEnabled(false);
            }

            @Override
            public void onEnd() {
                Log.d(TAG, "Video ended");
            }

            @Override
            public void onAllEnd() {
                Log.d(TAG, "All videos ended");
            }
        });

        mVideoControllerComponent.setOnVideoControllerListener(new OnVideoControllerListener() {
            @Override
            public void onPlay() {
                Log.d(TAG, "onPlay: ");
                mVideoComponent.playVideo();
            }

            @Override
            public void onPause() {
                Log.d(TAG, "onPause: ");
                mVideoComponent.pauseVideo();
            }

            @Override
            public void onBack() {
                Log.d(TAG, "onBack: ");
            }

            @Override
            public void onSeek(long progress) {
                Log.d(TAG, "onSeek: ");
                mVideoComponent.setProgress(progress);
            }
        });
    }
}
