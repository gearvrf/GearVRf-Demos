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

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.video.focus.FocusableController;
import org.gearvrf.video.movie.MovieManager;
import org.gearvrf.video.movie.MovieTheater;
import org.gearvrf.video.overlay.VideoControl;

import java.io.IOException;

public class VideoMain extends GVRMain {

    private static final String TAG = "VideoMain";

    private GVRContext mGVRContext = null;
    private GVRActivity mActivity = null;

    private GVRSceneObject mHeadTracker = null;

    private MovieManager movieManager = null;
    private VideoControl videoControl = null;

    private boolean mIsOverlayVisible = false;

    VideoMain(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene mainScene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                movieManager.getMediaPlayer().start();
            }
        });

        // head tracker
        try {
            mHeadTracker = new GVRSceneObject(mGVRContext, mGVRContext.createQuad(0.5f, 0.5f),
                    mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext, "head-tracker.png")));
            mHeadTracker.getTransform().setPositionZ(-9.0f);
            mHeadTracker.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
            mHeadTracker.getRenderData().setDepthTest(false);
            mHeadTracker.getRenderData().setRenderingOrder(100000);
            mainScene.getMainCameraRig().addChildObject(mHeadTracker);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // movie manager
        movieManager = new MovieManager(mGVRContext);
        // add all theaters to main scene
        GVRSceneObject theaters[] = movieManager.getAllMovieTheater();
        for (GVRSceneObject theater : theaters) {
            mainScene.addSceneObject(theater);
            // hide all except active one
            if (theater != movieManager.getCurrentMovieTheater()) {
                ((MovieTheater)theater).hideCinemaTheater();
            }
        }

        // video control
        videoControl = new VideoControl(gvrContext, movieManager);
        mainScene.addSceneObject(videoControl);
        videoControl.hide();
    }

    @Override
    public void onStep() {
        movieManager.getCurrentMovieTheater().setShaderValues();
        if (mGVRContext != null) {
            FocusableController.processFocus(mGVRContext);
            if (mIsOverlayVisible) {
                videoControl.updateOverlayUI(mGVRContext);
            }
        }
    }

    public void onTap() {
        if (mGVRContext != null) {
            if (mIsOverlayVisible) {
                // if overlay is visible, check anything pointed
                if (FocusableController.processClick(mGVRContext) || videoControl.isOverlayPointed(mGVRContext)) {
                } else {
                    videoControl.hide();
                    mHeadTracker.getRenderData().setRenderMask(0);
                    mIsOverlayVisible = false;
                }
            } else {
                // overlay is not visible, make it visible
                videoControl.show();
                mHeadTracker.getRenderData().setRenderMask(GVRRenderData.GVRRenderMaskBit.Left
                        | GVRRenderData.GVRRenderMaskBit.Right);
                mIsOverlayVisible = true;
            }
        }
    }

    public void onTouch() {
        videoControl.processTouch(mGVRContext);
    }

    public void onPause() {
        movieManager.getMediaPlayer().pause();
    }
}
