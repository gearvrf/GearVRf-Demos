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
import org.gearvrf.videoplayer.component.DefaultFadeableObject;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.component.LabelCursor;
import org.gearvrf.videoplayer.component.OnFadeFinish;
import org.gearvrf.videoplayer.component.gallery.Gallery;
import org.gearvrf.videoplayer.component.gallery.OnGalleryEventListener;
import org.gearvrf.videoplayer.component.video.VideoPlayer;
import org.gearvrf.videoplayer.component.video.player.DefaultPlayerListener;
import org.gearvrf.videoplayer.component.video.player.OnPlayerListener;
import org.gearvrf.videoplayer.event.DefaultTouchEvent;
import org.gearvrf.videoplayer.model.Video;
import org.gearvrf.videoplayer.network.NetworkListener;
import org.gearvrf.videoplayer.network.NetworkManager;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.security.SecureRandom;
import java.util.List;


public class VideoPlayerMain extends BaseVideoPlayerMain implements OnGalleryEventListener {

    private static final String TAG = VideoPlayerMain.class.getSimpleName();
    private static float CURSOR_DEPTH = -8.0f;
    private static float WIDTH_VIDEO_PLAYER = 10.0f;
    private static final float SCALE = 200.0f;

    private GVRContext mContext;
    private GVRScene mScene;
    private GVRCursorController mCursorController;
    private VideoPlayer mVideoPlayer;
    private GVRSceneObject mCurrentContainer;
    private FadeableObject mCurrentCursor, mParentCursor;
    private LabelCursor mLabelCursor;
    private Gallery mGallery;
    private GVRSphereSceneObject mSkybox;
    private NetworkManager mNetworkManager;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        addSkyBoxSphere();
        initCursorController();
        createGallery();
        createVideoPlayer();
        createNetworkManager();
    }

    private void createNetworkManager() {
        mNetworkManager = new NetworkManager(mContext);
        mNetworkManager.register(new NetworkStateHandler());
        mNetworkManager.start();
    }

    private void createGallery() {
        mGallery = new Gallery(getGVRContext());
        mGallery.getTransform().setPositionZ(-8);
        mGallery.setOnGalleryEventListener(this);
        mScene.addSceneObject(mGallery);
        mCurrentContainer = mGallery;
    }

    private void createVideoPlayer() {
        mVideoPlayer = new VideoPlayer(getGVRContext());
        mVideoPlayer.setControlWidgetAutoHide(true);
        mVideoPlayer.setPlayerListener(mOnPlayerListener);
        mVideoPlayer.setBackButtonClickListener(mBackButtonClickListener);
        mScene.addSceneObject(mVideoPlayer);
        mVideoPlayer.setCursorObject(mParentCursor);
        mVideoPlayer.hide();
        mParentCursor.setEnable(true);
    }

    private void addSkyBoxSphere() {
        // Select a skybox
        int availableSkyboxes[] = {R.raw.skybox_a, R.raw.skybox_b, R.raw.skybox_c};
        int selectedSkybox = availableSkyboxes[new SecureRandom().nextInt(2)];
        GVRTexture texture = mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, selectedSkybox));
        mSkybox = new GVRSphereSceneObject(mContext, 72, 144, false, texture);
        mSkybox.setName("sphere");
        mSkybox.getTransform().setScale(SCALE, SCALE, SCALE);
        mScene.addSceneObject(mSkybox);
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

    private void createLabel() {
        String text = mContext.getActivity().getString(R.string.tap_to_reposition_view);
        mLabelCursor = new LabelCursor(mContext, 0.45f * WIDTH_VIDEO_PLAYER * 1.2f, 0.2f * WIDTH_VIDEO_PLAYER, text);
    }

    private GVRSceneObject createCursor() {
        createLabel();
        mCurrentCursor = new DefaultFadeableObject(
                mContext,
                mContext.createQuad(0.1f * CURSOR_DEPTH, 0.1f * CURSOR_DEPTH),
                mContext.getAssetLoader().loadTexture(new GVRAndroidResource(mContext, R.raw.cursor))
        );
        mCurrentCursor.getRenderData().setDepthTest(false);
        mCurrentCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        mParentCursor = new DefaultFadeableObject(mContext);
        mParentCursor.addChildObject(mCurrentCursor);
        mParentCursor.addChildObject(mLabelCursor);
        return mParentCursor;
    }

    private ITouchEvents mTouchHandler = new DefaultTouchEvent() {
        @Override
        public void onMotionOutside(GVRPicker gvrPicker, MotionEvent motionEvent) {
            if (mVideoPlayer.is360VideoPlaying()) {
                mVideoPlayer.showAllControls();
            }
            repositionScene();
        }

        @Override
        public void onTouchStart(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {

            mVideoPlayer.showAllControls();

        }
    };

    private OnPlayerListener mOnPlayerListener = new DefaultPlayerListener() {
        @Override
        public void onPrepareFile(String title, long duration) {
            mVideoPlayer.show(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    mVideoPlayer.play();
                }
            });

            mSkybox.getRenderData().getMaterial().setColor(0.6f, 0.6f, 0.6f);
        }
    };

    private View.OnClickListener mBackButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mVideoPlayer.hide(new OnFadeFinish() {
                @Override
                public void onFadeFinished() {
                    mGallery.setEnable(true);
                    mGallery.fadeIn();
                    mCurrentContainer = mGallery;
                    mParentCursor.setEnable(true);
                }
            });
            mSkybox.getRenderData().getMaterial().setColor(1.0f, 1.0f, 1.0f);
        }
    };

    private class NetworkStateHandler implements NetworkListener {

        @Override
        public void onConnected(boolean isConnected) {
            mGallery.setIsConnected(isConnected);
            mVideoPlayer.setIsConnected(isConnected);
        }
    }

    private void repositionScene() {
        final float rotationX = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationX();
        final float rotationY = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationY();
        final float rotationZ = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationZ();
        final float rotationW = mCursorController.getCursor().getParent().getParent().getParent().getTransform().getRotationW();

        Quaternionf cursorRotation = new Quaternionf(rotationX, rotationY, rotationZ, rotationW);
        Vector3f lookat = new Vector3f(0, 0, 1);
        lookat.rotate(cursorRotation);
        lookat = lookat.normalize();

        org.gearvrf.utility.Log.d(TAG, "LookAt: " + lookat.x + ", " + lookat.y + ", " + lookat.z);

        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f ownerXaxis = new Vector3f(0, 0, 0);
        Vector3f ownerYaxis = new Vector3f(0, 0, 0);

        up.cross(lookat.x, lookat.y, lookat.z, ownerXaxis);
        ownerXaxis = ownerXaxis.normalize();
        lookat.cross(ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, ownerYaxis);
        ownerYaxis = ownerYaxis.normalize();

        float[] newModelMatrix = new float[]{
                ownerXaxis.x, ownerXaxis.y, ownerXaxis.z, 0.0f,
                ownerYaxis.x, ownerYaxis.y, ownerYaxis.z, 0.0f,
                lookat.x, lookat.y, lookat.z, 0.0f,
                0, 0, 0, 1.0f
        };

        mGallery.reposition(newModelMatrix);
        mVideoPlayer.reposition(newModelMatrix);
    }

    @Override
    public void onVideosSelected(final List<Video> videoList) {
        Log.d(TAG, "onVideosSelected: " + videoList);
        mGallery.fadeOut(new OnFadeFinish() {
            @Override
            public void onFadeFinished() {
                mGallery.setEnable(false);
                mCurrentContainer = mVideoPlayer.getWidgetsContainer();
                mVideoPlayer.prepare(videoList);
            }
        });
    }

    public void onResume() {
        if (mVideoPlayer != null) {
            mVideoPlayer.play();
        }
    }

    private void hideCursor() {
        final float rotateXScene = mCurrentContainer.getTransform().getRotationX();
        final float rotateYScene = mCurrentContainer.getTransform().getRotationY();
        final float rotateZScene = mCurrentContainer.getTransform().getRotationZ();
        final float rotateWScene = mCurrentContainer.getTransform().getRotationW();
        final float rotateXCamera = getGVRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationX();
        final float rotateYCamera = getGVRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationY();
        final float rotateZCamera = getGVRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationZ();
        final float rotateWCamera = getGVRContext().getMainScene().getMainCameraRig().getHeadTransform().getRotationW();

        Quaternionf quaternionfScene = new Quaternionf(rotateXScene, rotateYScene, rotateZScene, rotateWScene);
        Quaternionf quaternionfCamera = new Quaternionf(rotateXCamera, rotateYCamera, rotateZCamera, rotateWCamera);

        quaternionfScene.difference(quaternionfCamera);

        if (quaternionfScene.angle() > Math.PI / 4.0f) {
            enableInteractiveCursor();
        } else {
            disableInteractiveCursor();
        }
    }

    public void onPause() {
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    public void enableInteractiveCursor() {
        if (!mVideoPlayer.is360VideoPlaying()) {
            mLabelCursor.show();
        }
        mCurrentCursor.hide();
    }

    public void disableInteractiveCursor() {
        mCurrentCursor.show();
        mLabelCursor.hide();
    }

    @Override
    public boolean onBackPress() {
        if (mVideoPlayer.isEnabled()) {
            return mVideoPlayer.onBackPressed();
        }
        return mGallery.onBackPressed();
    }
}
