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
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.video.focus.FocusableController;
import org.gearvrf.video.movie.MovieManager;
import org.gearvrf.video.movie.MovieTheater;
import org.gearvrf.video.overlay.VideoControl;

public class VideoMain extends GVRMain {

    private static final String TAG = "VideoMain";

    private GVRContext mGVRContext = null;
    private GVRActivity mActivity = null;

    private MovieManager mMovieManager = null;
    private VideoControl mVideoControl = null;

    private boolean mIsOverlayVisible = false;

    private GVRPicker mPicker = null;
    private FocusableController focusableController = null;

    VideoMain(GVRActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene mainScene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                // else need to manually call play
                if (mVideoControl != null) {
                    mVideoControl.playVideo();
                }
            }
        });

        // focusableController
        focusableController = new FocusableController();

        // movie manager
        mMovieManager = new MovieManager(mGVRContext);
        // add all theaters to main scene
        GVRSceneObject theaters[] = mMovieManager.getAllMovieTheater();
        for (GVRSceneObject theater : theaters) {
            mainScene.addSceneObject(theater);
            // hide all except active one
            if (theater != mMovieManager.getCurrentMovieTheater()) {
                ((MovieTheater)theater).hideCinemaTheater();
            }
        }

        // video control
        mVideoControl = new VideoControl(gvrContext, mMovieManager, mainScene);
        mainScene.addSceneObject(mVideoControl);
        mVideoControl.hide();

        mPicker = new GVRPicker(gvrContext, mainScene);
        mainScene.getEventReceiver().addListener(focusableController);
    }

    @Override
    public void onStep() {
        mMovieManager.getCurrentMovieTheater().setShaderValues();
        if (mGVRContext != null) {
            //FocusableController.processFocus(mGVRContext);
            if (mIsOverlayVisible) {
                mVideoControl.updateOverlayUI(mGVRContext);
            }
        }
    }

    public void onTap() {
        if (mGVRContext != null) {
            if (mIsOverlayVisible) {
                // if overlay is visible, check anything pointed
                if (focusableController.processClick(mGVRContext) || mVideoControl.isOverlayPointed(mGVRContext)) {
                } else {
                    mVideoControl.hide();
                    mIsOverlayVisible = false;
                }
            } else {
                // overlay is not visible, make it visible
                mVideoControl.show();
                mIsOverlayVisible = true;
            }
        }
    }

    public void onTouch() {
        if (mGVRContext != null && mVideoControl != null) {
            mVideoControl.processTouch(mGVRContext);
        }
    }

    public void onPause() {
        if (mGVRContext != null && mVideoControl != null) {
            mVideoControl.pauseVideo();
        }
    }
}
