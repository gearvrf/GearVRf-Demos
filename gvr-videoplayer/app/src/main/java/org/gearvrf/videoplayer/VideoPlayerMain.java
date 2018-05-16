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
import org.gearvrf.videoplayer.component.VideoComponent;
import org.gearvrf.videoplayer.component.VideoControllerComponent;
import org.gearvrf.videoplayer.filter.VideosFileFilter;

import java.io.File;
import java.util.EnumSet;

public class VideoPlayerMain extends GVRMain {

    private static final String VIDEOS_DIR_NAME = "gvr-videoplayer";
    private static float PASSTHROUGH_DISTANCE = 100.0f;
    private static final float SCALE = 200.0f;

    private GVRContext mContext;
    private GVRScene mScene;
    private VideoComponent mVideoComponent;
    private VideoControllerComponent mVideoControllerComponent;
    protected GVRCursorController mCursorController;
    private GVRSphereSceneObject sphereObject = null;
    private GVRSceneObject configuringScene = null;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        addSphere();
        initCursorController();
        createVideoComponent();
        createVideoControllerComponent();
        configuringScene = new GVRSceneObject(mContext);
        configuringScene.addChildObject(mVideoComponent);
        configuringScene.addChildObject(mVideoControllerComponent);
        mScene.addSceneObject(configuringScene);
        playFiles();
    }

    public void addSphere(){

        GVRTexture texture = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.photosphere));

        sphereObject = new GVRSphereSceneObject(mContext, 72, 144, false, texture);

        sphereObject.getTransform().setScale(SCALE, SCALE, SCALE);

        mScene.addSceneObject(sphereObject);

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

    }

    private void createVideoControllerComponent() {
        mVideoControllerComponent = new VideoControllerComponent(mContext, 6f, 1f);
        mVideoControllerComponent.getTransform().setPosition(0.0f, -2.5f, -6.5f);
        mVideoControllerComponent.getTransform().rotateByAxis(-15, 1, 0, 0);

    }

    @Override
    public void onSingleTapUp(MotionEvent event) {
        if (mVideoComponent.isPlaying()) {
            mVideoComponent.pauseVideo();
        } else {
            mVideoComponent.playVideo();
        }
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

        }
    };
}
