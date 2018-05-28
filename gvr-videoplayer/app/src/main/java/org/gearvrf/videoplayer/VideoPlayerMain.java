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
import org.gearvrf.videoplayer.component.gallery.Gallery;
import org.gearvrf.videoplayer.component.video.VideoPlayer;
import org.gearvrf.videoplayer.event.DefaultTouchEvent;
import org.gearvrf.videoplayer.focus.PickEventHandler;
import org.gearvrf.videoplayer.model.Video;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


public class VideoPlayerMain extends GVRMain {

    private static final String TAG = VideoPlayerMain.class.getSimpleName();
    private static float CURSOR_DEPTH = 100.0f;
    private static final float SCALE = 200.0f;
    private static final float SCALE1 = 1.0f;

    private GVRContext mContext;
    private GVRScene mScene;
    private GVRCursorController mCursorController;
    private VideoPlayer mVideoPlayer;
    private GVRSceneObject sceneObject;
    private GVRSphereSceneObject mSphereObject;
    private PickEventHandler mPickHandler;

    private Gallery mGallery;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();
        mPickHandler = new PickEventHandler();

        sceneObject = new GVRSceneObject(getGVRContext());
        mScene.addSceneObject(sceneObject);

        addSkyBoxSphere();
        initCursorController();
        addVideoPlayer();
        createGallery();
    }

    private void createGallery() {
        mGallery = new Gallery(getGVRContext());
        mGallery.getTransform().setPositionZ(-1);
        sceneObject.addChildObject(mGallery);
    }

    private void addVideoPlayer() {
        mVideoPlayer = new VideoPlayer(getGVRContext(), 10, 5);
        mVideoPlayer.getTransform().setPositionZ(-9);
        mVideoPlayer.setAutoHideController(true);
        sceneObject.addChildObject(mVideoPlayer);
    }

    private void addSkyBoxSphere() {
        GVRTexture texture = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.photosphere));
        mSphereObject = new GVRSphereSceneObject(mContext, 72, 144, false, texture);
        mSphereObject.getTransform().setScale(SCALE, SCALE, SCALE);
        mScene.addSceneObject(mSphereObject);
    }

    private void initCursorController() {

        mScene.getEventReceiver().addListener(mTouchHandler);

        GVRInputManager inputManager = mContext.getInputManager();
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mTouchHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mTouchHandler);
                newController.addPickEventListener(mPickHandler);
                newController.setCursor(createCursor());
                newController.setCursorDepth(-CURSOR_DEPTH);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
            }
        });
    }

    private GVRSceneObject createCursor() {
        GVRSceneObject cursor = new GVRSceneObject(
                mContext,
                mContext.createQuad(0.2f * CURSOR_DEPTH, 0.2f * CURSOR_DEPTH),
                mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.cursor))
        );
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        return cursor;
    }

    private ITouchEvents mTouchHandler = new DefaultTouchEvent() {
        @Override
        public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {
            repositionPlayer();
        }

        @Override
        public void onTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
            mVideoPlayer.showController();
        }
    };

    private void repositionPlayer() {
        final float rotationX = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationX();
        final float rotationY = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationY();
        final float rotationZ = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationZ();
        final float rotationW = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationW();

        if (sceneObject != null) {
            sceneObject.getTransform().setRotation(rotationW, rotationX, rotationY, rotationZ);
        }

        mVideoPlayer.showController();
    }

    private void prepareVideos(List<Video> videos) {
        List<File> videoFiles = new LinkedList<>();
        for (Video video : videos) {
            videoFiles.add(new File(video.getPath()));
        }
        mVideoPlayer.prepare(videoFiles.toArray(new File[videoFiles.size()]));
    }
}
