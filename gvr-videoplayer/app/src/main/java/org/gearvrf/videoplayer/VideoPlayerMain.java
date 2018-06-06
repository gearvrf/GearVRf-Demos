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
import android.view.View;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import org.gearvrf.utility.Log;
import org.gearvrf.videoplayer.component.FadeableObject;

import org.gearvrf.videoplayer.component.gallery.Gallery;
import org.gearvrf.videoplayer.component.gallery.OnGalleryEventListener;
import org.gearvrf.videoplayer.component.video.VideoPlayer;
import org.gearvrf.videoplayer.component.video.player.DefaultPlayerListener;
import org.gearvrf.videoplayer.component.video.player.OnPlayerListener;
import org.gearvrf.videoplayer.event.DefaultTouchEvent;
import org.gearvrf.videoplayer.model.Video;

import java.security.SecureRandom;
import java.util.List;


public class VideoPlayerMain extends BaseVideoPlayerMain implements OnGalleryEventListener {

    private static final String TAG = VideoPlayerMain.class.getSimpleName();
    private static float CURSOR_DEPTH = -10.0f;
    private static final float SCALE = 200.0f;
    private static float CURSOR_HIGH_OPACITY = 1.0f;
    private static float CURSOR_LOW_OPACITY = 0.0f;

    private GVRContext mContext;
    private GVRScene mScene;
    private GVRCursorController mCursorController;
    private VideoPlayer mVideoPlayer;

    private GVRSceneObject mMainSceneContainer, highlightCursor, cursor ;

    private Gallery mGallery;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {

        mContext = gvrContext;
        mScene = gvrContext.getMainScene();


        mMainSceneContainer = new GVRSceneObject(getGVRContext());
        mScene.addSceneObject(mMainSceneContainer);

        addSkyBoxSphere();
        initCursorController();
        createGallery();
        createVideoPlayer();

        }

    private void createGallery() {
        mGallery = new Gallery(getGVRContext());
        mGallery.getTransform().setPositionZ(-8);
        mGallery.setOnGalleryEventListener(this);
        mMainSceneContainer.addChildObject(mGallery);
    }

    private void createVideoPlayer() {
        mVideoPlayer = new VideoPlayer(getGVRContext(), 10, 5);
        mVideoPlayer.getTransform().setPositionZ(-8.1f);
        mVideoPlayer.setControlWidgetAutoHide(true);
        mVideoPlayer.setPlayerListener(mOnPlayerListener);
        mVideoPlayer.setBackButtonClickListener(mBackButtonClickListener);
        mMainSceneContainer.addChildObject(mVideoPlayer);
        mVideoPlayer.hide();
    }

    private void addSkyBoxSphere() {

        // Select a skybox
        int availableSkyboxes[] = {R.raw.skybox_a, R.raw.skybox_b, R.raw.skybox_c};
        int selectedSkybox = availableSkyboxes[new SecureRandom().nextInt(2)];

        GVRTexture texture = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, selectedSkybox));
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(mContext, 72, 144, false, texture);
        sphere.getTransform().setScale(SCALE, SCALE, SCALE);
        mScene.addSceneObject(sphere);
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
                newController.setCursor(createCursor());
                newController.setCursorDepth(-CURSOR_DEPTH);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
            }
        });
    }

    @Override
    public void onStep() {
        super.onStep();
        hideCursor();
    }

    private GVRSceneObject createCursor() {
         highlightCursor = new GVRSceneObject(mContext,
                mContext.createQuad(0.2f * CURSOR_DEPTH, 0.2f * CURSOR_DEPTH),
                mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext,
                        R.raw.cursor_hover)));
        highlightCursor.getRenderData().setDepthTest(false);
        highlightCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        getGVRContext().getMainScene().getMainCameraRig().addChildObject(highlightCursor);
        cursor = new GVRSceneObject(
                mContext,
                mContext.createQuad(0.2f * CURSOR_DEPTH, 0.2f * CURSOR_DEPTH),
                mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.cursor))
        );
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        highlightCursor.addChildObject(cursor);
        return highlightCursor;
   }

    private ITouchEvents mTouchHandler = new DefaultTouchEvent() {
        @Override
        public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {
            repositionPlayer();
        }

        @Override
        public void onTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

            mVideoPlayer.showAllControls();

        }
    };

    private OnPlayerListener mOnPlayerListener = new DefaultPlayerListener() {
        @Override
        public void onPrepareFile(String title, long duration) {
            mVideoPlayer.show(new FadeableObject.FadeInCallback() {
                @Override
                public void onFadeIn() {
                    mVideoPlayer.play();
                }
            });
        }
    };

    private View.OnClickListener mBackButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVideoPlayer.hide(new FadeableObject.FadeOutCallback() {
                @Override
                public void onFadeOut() {
                    mMainSceneContainer.addChildObject(mGallery);
                    mGallery.fadeIn();
                }
            });

        }
    };

    private void repositionPlayer() {
        final float rotationX = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationX();
        final float rotationY = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationY();
        final float rotationZ = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationZ();
        final float rotationW = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationW();

        if (mMainSceneContainer != null) {
            mMainSceneContainer.getTransform().setRotation(rotationW, rotationX, rotationY, rotationZ);
        }

    }

    @Override
    public void onVideosSelected(final List<Video> videoList) {
        Log.d(TAG, "onVideosSelected: " + videoList);
        mGallery.fadeOut(new FadeableObject.FadeOutCallback() {
            @Override
            public void onFadeOut() {
                mMainSceneContainer.removeChildObject(mGallery);
                mVideoPlayer.prepare(videoList);
            }
        });
    }

    public void onResume() {
        if (mVideoPlayer != null) {
            mVideoPlayer.play();
        }

        //  mVideoPlayer.showController();
    }

    private void hideCursor(){
        final float axisXGallery = mMainSceneContainer.getTransform().getRotationPitch();
        final float axisX =  getGVRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationPitch();
        final float axisYGallery = mMainSceneContainer.getTransform().getRotationYaw();
        final float axisY =  getGVRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationYaw();
        final float operacaoX = Math.abs(axisX) - Math.abs(axisXGallery) ;
        final float operacaoY = Math.abs(axisY) - Math.abs(axisYGallery) ;

        if ((operacaoX > 45) || (operacaoX < - 35) ||  (operacaoY > 45) || (operacaoY < -35 ) ){
            enableInteractiveCursor();
        }else{
            disableInteractiveCursor();
        }

            Log.d(TAG,"operacao Y : "+ operacaoY + " Operacao X :" + operacaoX);

    }

    public void onPause() {
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    public void enableInteractiveCursor() {
        highlightCursor.getRenderData().getMaterial().setOpacity(CURSOR_HIGH_OPACITY);
        cursor.getRenderData().getMaterial().setOpacity(CURSOR_LOW_OPACITY);
    }

    public void disableInteractiveCursor() {
        highlightCursor.getRenderData().getMaterial().setOpacity(CURSOR_LOW_OPACITY);
        cursor.getRenderData().getMaterial().setOpacity(CURSOR_HIGH_OPACITY);
    }
}
