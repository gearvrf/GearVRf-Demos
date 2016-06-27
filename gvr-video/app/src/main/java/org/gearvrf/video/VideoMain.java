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

package org.gearvrf.video;

import java.io.IOException;

import org.gearvrf.*;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.util.FPSCounter;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;

public class VideoMain extends GVRMain {

    private static final String TAG = "VideoMain";

    private GVRContext mGVRContext = null;

    private MediaPlayer mMediaPlayer = null;

    private int mCinemaNum = 2;
    private GVRSceneObject[] mCinema = new GVRSceneObject[mCinemaNum];

    private GVRSceneObject mSceneObject = null;
    private GVRSceneObject mScreen = null;

    private GVRSceneObject mOculusSceneObject1 = null;
    private GVRSceneObject mOculusSceneObject2 = null;
    private GVRSceneObject mOculusScreen = null;

    private GVRSceneObject mHeadTracker = null;
    private Button mPlayButton = null;
    private Button mPauseButton = null;
    private Button mFrontButton = null;
    private Button mBackButton = null;
    private Button mImaxButton = null;
    private Button mSelectButton = null;
    private GVRSceneObject mButtonBoard = null;
    private Seekbar mSeekbar = null;

    private boolean mIsUIHidden = true;
    private boolean mIsTouched = false;
    private boolean mIsSingleTapped = false;
    private boolean mIsIMAX = false;

    private float mTransitionWeight = 0.0f;
    private float mTransitionTarget = 0.0f;

    private int mCurrentCinema = 0;
    private float mFadeWeight = 0.0f;
    private float mFadeTarget = 1.0f;

    private GVRActivity mActivity;

    VideoMain(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene mainScene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
                mMediaPlayer.start();
            }
        });

        RadiosityShader radiosityShader = new RadiosityShader(gvrContext);
        AdditiveShader additiveShader = new AdditiveShader(gvrContext);
        ScreenShader screenShader = new ScreenShader(gvrContext);

        /*
         * Media player with a linked texture.
         */
        GVRExternalTexture screenTexture = new GVRExternalTexture(gvrContext);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);

        AssetFileDescriptor afd;
        try {
            afd = gvrContext.getContext().getAssets().openFd("tron.mp4");
            mMediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            afd.close();
            mMediaPlayer.prepare();

            /*
             * Head tracker
             */
            GVRTexture headTrackerTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "head-tracker.png"));
            mHeadTracker = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(0.5f, 0.5f), headTrackerTexture);
            mHeadTracker.getTransform().setPositionZ(-9.0f);
            mHeadTracker.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.OVERLAY);
            mHeadTracker.getRenderData().setDepthTest(false);
            mHeadTracker.getRenderData().setRenderingOrder(100000);
            mainScene.getMainCameraRig().addChildObject(mHeadTracker);

            /*
             * FXGear Background
             */
            mCinema[0] = new GVRSceneObject(mGVRContext);

            GVRMesh backgroundMesh = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background.obj"));
            GVRTexture leftBackgroundLightOffTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background_left_light_off.jpg"));
            GVRTexture leftBackgroundLightOnTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater1/theater_background_left_light_on.jpg"));
            mSceneObject = new GVRSceneObject(gvrContext, backgroundMesh,
                    leftBackgroundLightOffTexture);
            mSceneObject.getTransform().setPosition(-0.031f, 0.0f, 0.0f);
            mSceneObject.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);

            mCinema[0].addChildObject(mSceneObject);

            /*
             * Radiosity settings
             */
            mSceneObject.getRenderData().getMaterial()
                    .setShaderType(radiosityShader.getShaderId());
            mSceneObject.getRenderData().getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_OFF_KEY,
                            leftBackgroundLightOffTexture);
            mSceneObject.getRenderData().getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_ON_KEY,
                            leftBackgroundLightOnTexture);
            mSceneObject.getRenderData().getMaterial()
                    .setTexture(RadiosityShader.SCREEN_KEY, screenTexture);

            /*
             * Uv setting for radiosity
             */

            GVRMesh radiosity_mesh = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater1/radiosity.obj"));
            backgroundMesh.setNormals(radiosity_mesh.getVertices());

            /*
             * Screen
             */

            GVRMesh screenMesh = gvrContext.loadMesh(new GVRAndroidResource(
                    mGVRContext, "theater1/screen.obj"));
            GVRRenderData renderData = new GVRRenderData(gvrContext);
            GVRMaterial material = new GVRMaterial(gvrContext,
                    screenShader.getShaderId());
            material.setTexture(ScreenShader.SCREEN_KEY, screenTexture);
            renderData.setMesh(screenMesh);
            renderData.setMaterial(material);

            mScreen = new GVRVideoSceneObject(gvrContext, screenMesh, mMediaPlayer,
                    screenTexture, GVRVideoSceneObject.GVRVideoType.MONO);
            mScreen.attachRenderData(renderData);
            mScreen.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);

            mCinema[0].addChildObject(mScreen);

            mainScene.addSceneObject(mCinema[0]);

            /*
             * Oculus Background
             */
            mCinema[1] = new GVRSceneObject(mGVRContext);

            GVRMesh backgroundMesh1 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/cinema.obj"));
            GVRMesh backgroundMesh2 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/additive.obj"));
            GVRTexture AdditiveTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater2/additive.png"));
            GVRTexture BackgroundLightOffTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater2/cinema1.png"));
            GVRTexture BackgroundLightOnTexture = gvrContext
                    .loadTexture(new GVRAndroidResource(mGVRContext,
                            "theater2/cinema2.png"));
            mOculusSceneObject1 = new GVRSceneObject(gvrContext,
                    backgroundMesh1, BackgroundLightOnTexture);
            mOculusSceneObject1.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            mOculusSceneObject2 = new GVRSceneObject(gvrContext,
                    backgroundMesh2, AdditiveTexture);
            mOculusSceneObject2.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);
            mOculusSceneObject2.getRenderData().setRenderingOrder(2500);

            mCinema[1].addChildObject(mOculusSceneObject1);
            mCinema[1].addChildObject(mOculusSceneObject2);

            /*
             * Radiosity settings
             */
            mOculusSceneObject1.getRenderData().getMaterial()
                    .setShaderType(radiosityShader.getShaderId());
            mOculusSceneObject1
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_OFF_KEY,
                            BackgroundLightOnTexture);
            mOculusSceneObject1
                    .getRenderData()
                    .getMaterial()
                    .setTexture(RadiosityShader.TEXTURE_ON_KEY,
                            BackgroundLightOffTexture);
            mOculusSceneObject1.getRenderData().getMaterial()
                    .setTexture(RadiosityShader.SCREEN_KEY, screenTexture);

            mOculusSceneObject2.getRenderData().getMaterial()
                    .setShaderType(additiveShader.getShaderId());
            mOculusSceneObject2.getRenderData().getMaterial()
                    .setTexture(AdditiveShader.TEXTURE_KEY, AdditiveTexture);

            /*
             * Uv setting for radiosity
             */

            GVRMesh oculus_radiosity_mesh1 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/radiosity1.obj"));
            GVRMesh oculus_radiosity_mesh2 = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/radiosity2.obj"));
            backgroundMesh1.setNormals(oculus_radiosity_mesh1.getVertices());
            backgroundMesh2.setNormals(oculus_radiosity_mesh2.getVertices());

            /*
             * Screen
             */

            GVRMesh oculus_screenMesh = gvrContext
                    .loadMesh(new GVRAndroidResource(mGVRContext,
                            "theater2/screen.obj"));
            GVRRenderData oculus_renderData = new GVRRenderData(gvrContext);
            GVRMaterial oculus_material = new GVRMaterial(gvrContext,
                    screenShader.getShaderId());
            oculus_material.setTexture(ScreenShader.SCREEN_KEY, screenTexture);
            oculus_renderData.setMesh(oculus_screenMesh);
            oculus_renderData.setMaterial(oculus_material);

            mOculusScreen = new GVRVideoSceneObject(gvrContext, oculus_screenMesh, mMediaPlayer,
                    screenTexture, GVRVideoSceneObject.GVRVideoType.MONO);
            mOculusScreen.attachRenderData(oculus_renderData);
            mOculusScreen.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.None);

            mCinema[1].addChildObject(mOculusScreen);

            float pivot_x = -3.353f;
            float pivot_y = 0.401f;
            float pivot_z = -0.000003f;

            mCinema[1].getTransform().setPosition(-pivot_x, -pivot_y, -pivot_z);
            mCinema[1].getTransform().rotateByAxisWithPivot(90.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 0.0f, 0.0f);

            mainScene.addSceneObject(mCinema[1]);
            for (int i = 0; i < mCinema[1].getChildrenCount(); i++)
                mCinema[1].getChildByIndex(i).getRenderData().setRenderMask(0);


            /*
             * Play button
             */
            GVRTexture mInactivePlay = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/play-inactive.png"));
            GVRTexture mActivePlay = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/play-active.png"));
            mPlayButton = new Button(gvrContext, gvrContext.createQuad(0.7f, 0.7f),
                    mActivePlay, mInactivePlay);
            mPlayButton.setPosition(0.0f, -0.8f, -8.0f);
            mainScene.addSceneObject(mPlayButton);

            /*
             * Pause button
             */
            GVRTexture mInactivePause = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/pause-inactive.png"));
            GVRTexture mActivePause = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/pause-active.png"));
            mPauseButton = new Button(gvrContext, gvrContext.createQuad(0.7f, 0.7f),
                    mActivePause, mInactivePause);
            mPauseButton.setPosition(0.0f, -0.8f, -8.0f);
            mainScene.addSceneObject(mPauseButton);

            /*
             * Forward button
             */
            GVRTexture mInactiveFront = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/front-inactive.png"));
            GVRTexture mActiveFront = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/front-active.png"));
            mFrontButton = new Button(gvrContext, gvrContext.createQuad(0.7f, 0.7f),
                    mActiveFront, mInactiveFront);
            mFrontButton.setPosition(1.2f, -0.8f, -8.0f);
            mainScene.addSceneObject(mFrontButton);

            /*
             * Backward button
             */
            GVRTexture mInactiveBack = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/back-inactive.png"));
            GVRTexture mActiveBack = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/back-active.png"));
            mBackButton = new Button(gvrContext, gvrContext.createQuad(0.7f, 0.7f),
                    mActiveBack, mInactiveBack);
            mBackButton.setPosition(-1.2f, -0.8f, -8.0f);
            mainScene.addSceneObject(mBackButton);

            /*
             * Imax button
             */
            GVRTexture mInactiveImax = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/imaxoutline.png"));
            GVRTexture mActiveImax = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/imaxselect.png"));
            mImaxButton = new Button(gvrContext, gvrContext.createQuad(0.9f, 0.35f),
                    mActiveImax, mInactiveImax);
            mImaxButton.setPosition(2.5f, -0.9f, -7.5f);
            mainScene.addSceneObject(mImaxButton);

            /*
             * Select button
             */
            GVRTexture mInactiveSelect = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/selectionoutline.png"));
            GVRTexture mActiveSelect = gvrContext.loadTexture(new GVRAndroidResource(
                    mGVRContext, "button/selectionselect.png"));
            mSelectButton = new Button(gvrContext, gvrContext.createQuad(0.9f, 0.35f),
                    mActiveSelect, mInactiveSelect);
            mSelectButton.setPosition(-2.5f, -0.9f, -7.5f);
            mainScene.addSceneObject(mSelectButton);

            /*
             * Button board
             */
            mButtonBoard = new GVRSceneObject(gvrContext,
                    gvrContext.createQuad(8.2f, 1.35f),
                    gvrContext.loadTexture(new GVRAndroidResource(mGVRContext,
                            "button/button-board.png")));
            mButtonBoard.getTransform().setPosition(-0.1f, -0.6f, -8.0f);
            mButtonBoard.getRenderData().setRenderingOrder(
                    GVRRenderingOrder.TRANSPARENT);
            mainScene.addSceneObject(mButtonBoard);

            /*
             * Seek bar
             */
            mSeekbar = new Seekbar(gvrContext);
            mainScene.addSceneObject(mSeekbar);
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.finish();
            Log.e(TAG, "Assets were not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();

        float step = 0.2f;

        mTransitionWeight += step * (mTransitionTarget - mTransitionWeight);
        mFadeWeight += 0.01f * (mFadeTarget - mFadeWeight);
        float weightValue = mTransitionWeight * 0.2f;

        if (mCurrentCinema == 0) {
            for (int i = 0; i < mCinema[1].getChildrenCount(); i++)
                mCinema[1].getChildByIndex(i).getRenderData()
                        .setRenderMask(0);
            for (int i = 0; i < mCinema[0].getChildrenCount(); i++)
                mCinema[0].getChildByIndex(i).getRenderData().
                        setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);

            mSceneObject.getRenderData().getMaterial()
                    .setFloat(RadiosityShader.WEIGHT_KEY, weightValue);
            mSceneObject.getRenderData().getMaterial()
                    .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
            mSceneObject.getRenderData().getMaterial()
                    .setFloat(RadiosityShader.LIGHT_KEY, 2.0f);
        } else {
            for (int i = 0; i < mCinema[0].getChildrenCount(); i++)
                mCinema[0].getChildByIndex(i).getRenderData()
                        .setRenderMask(0);
            for (int i = 0; i < mCinema[1].getChildrenCount(); i++)
                mCinema[1].getChildByIndex(i).getRenderData()
                        .setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);

            mOculusSceneObject1.getRenderData().getMaterial()
                    .setFloat(RadiosityShader.WEIGHT_KEY, weightValue);
            mOculusSceneObject1.getRenderData().getMaterial()
                    .setFloat(RadiosityShader.FADE_KEY, mFadeWeight);
            mOculusSceneObject1.getRenderData().getMaterial()
                    .setFloat(RadiosityShader.LIGHT_KEY, 1.0f);
            mOculusSceneObject2.getRenderData().getMaterial()
                    .setFloat(AdditiveShader.WEIGHT_KEY, mTransitionWeight);
            mOculusSceneObject2.getRenderData().getMaterial()
                    .setFloat(AdditiveShader.FADE_KEY, mFadeWeight);
        }

        float scale = 1.0f + 1.0f * (mTransitionWeight - 1.0f);
        if (scale >= 1.0f) {
            mButtonBoard.getTransform().setScale(scale, scale, 1.0f);
            mButtonBoard.getTransform().setPosition(
                    -0.1f, -0.6f - 0.26f * scale, -8.0f);
            mScreen.getTransform().setScale(scale, scale, 1.0f);
            mSceneObject.getTransform().setScale(scale, scale, 1.0f);
        }

        boolean isTouched = mIsTouched;
        boolean isSingleTapped = mIsSingleTapped;
        mIsTouched = false;
        mIsSingleTapped = false;

        boolean isUIHiden = mIsUIHidden;
        boolean isAnythingPointed = false;

        if (!mIsUIHidden) {
            if (mMediaPlayer.isPlaying()) {
                mPlayButton.show();
                mPauseButton.hide();
            } else {
                mPauseButton.show();
                mPlayButton.hide();
            }
            mFrontButton.show();
            mBackButton.show();
            mImaxButton.show();
            mSelectButton.show();
            mButtonBoard.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
            mSeekbar.setRenderMask(GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);

            GVREyePointeeHolder[] pickedHolders = GVRPicker.pickScene(mGVRContext.getMainScene());

            boolean playPausePointed = false;
            boolean frontPointed = false;
            boolean backPointed = false;
            boolean imaxPointed = false;
            boolean selectPointed = false;
            Float seekbarRatio = mSeekbar.getRatio(mGVRContext.getMainScene()
                    .getMainCameraRig().getLookAt());

            for (GVREyePointeeHolder holder : pickedHolders) {
                if (holder.equals(mPlayButton.getEyePointeeHolder()) ||
                        holder.equals(mPauseButton.getEyePointeeHolder())) {
                    playPausePointed = true;
                } else if (holder.equals(mFrontButton.getEyePointeeHolder())) {
                    frontPointed = true;
                } else if (holder.equals(mBackButton.getEyePointeeHolder())) {
                    backPointed = true;
                } else if (holder.equals(mImaxButton.getEyePointeeHolder())) {
                    imaxPointed = true;
                } else if (holder.equals(mSelectButton.getEyePointeeHolder())) {
                    selectPointed = true;
                }
            }

            if (playPausePointed || frontPointed || backPointed || imaxPointed
                    || selectPointed || seekbarRatio != null) {
                isAnythingPointed = true;
            }

            if (playPausePointed) {
                if (isSingleTapped) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    } else {
                        mMediaPlayer.start();
                    }
                }
            }

            if (mMediaPlayer.isPlaying()) {
                if (playPausePointed) {
                    mPlayButton.activate();
                } else {
                    mPlayButton.inactivate();
                }
            } else {
                if (playPausePointed) {
                    mPauseButton.activate();
                } else {
                    mPauseButton.inactivate();
                }
            }

            if (frontPointed) {
                if (isSingleTapped) {
                    mMediaPlayer
                            .seekTo(mMediaPlayer.getCurrentPosition() + 10000);
                }
                mFrontButton.activate();
            } else {
                mFrontButton.inactivate();
            }

            if (backPointed) {
                if (isSingleTapped) {
                    mMediaPlayer
                            .seekTo(mMediaPlayer.getCurrentPosition() - 10000);

                }
                mBackButton.activate();
            } else {
                mBackButton.inactivate();
            }

            if (imaxPointed) {
                if (isSingleTapped) {
                    if (!mIsIMAX) {
                        mIsIMAX = true;
                        mTransitionTarget = 2.0f;
                    } else {
                        mIsIMAX = false;
                        mTransitionTarget = 1.0f;
                    }
                }
                mImaxButton.activate();
            } else {
                mImaxButton.inactivate();
            }

            if (selectPointed) {
                if (isSingleTapped) {
                    mFadeWeight = 0.0f;
                    mCurrentCinema++;
                    if (mCurrentCinema >= mCinemaNum)
                        mCurrentCinema = 0;
                }
                mSelectButton.activate();
            } else {
                mSelectButton.inactivate();
            }

            if (seekbarRatio != null) {
                mSeekbar.glow();
            } else {
                mSeekbar.unglow();
            }

            if (isTouched && seekbarRatio != null) {
                int current = (int) (mMediaPlayer.getDuration() * seekbarRatio);
                mMediaPlayer.seekTo(current);
                mSeekbar.setTime(mGVRContext, current,
                        mMediaPlayer.getDuration());
            } else {
                mSeekbar.setTime(mGVRContext,
                        mMediaPlayer.getCurrentPosition(),
                        mMediaPlayer.getDuration());
            }
        } else {
            turnOffGUIMenu();

            if (isSingleTapped) {
                mIsUIHidden = false;
            }
        }

        if (!mIsUIHidden) {
            mHeadTracker.getRenderData().setRenderMask(
                    GVRRenderMaskBit.Left | GVRRenderMaskBit.Right);
        } else {
            mHeadTracker.getRenderData().setRenderMask(0);
        }

        if (!isUIHiden && isSingleTapped && !isAnythingPointed) {
            mIsUIHidden = true;
        }
    }

    void onPause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    void onTouchEvent(MotionEvent event) {
        mIsTouched = true;
    }

    void onSingleTap(MotionEvent e) {
        mIsSingleTapped = true;
    }

    private void turnOffGUIMenu() {
        mPlayButton.hide();
        mPauseButton.hide();
        mFrontButton.hide();
        mBackButton.hide();
        mImaxButton.hide();
        mSelectButton.hide();
        mButtonBoard.getRenderData().setRenderMask(0);
        mSeekbar.setRenderMask(0);
        mSeekbar.unglow();
    }
}
