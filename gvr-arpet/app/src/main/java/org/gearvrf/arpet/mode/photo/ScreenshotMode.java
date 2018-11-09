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

package org.gearvrf.arpet.mode.photo;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.mode.BasePetMode;
import org.gearvrf.arpet.mode.OnBackToHudModeListener;

public class ScreenshotMode extends BasePetMode {
    private OnBackToHudModeListener mBackToHudModeListener;
    private PhotoViewController mPhotoViewController;

    public ScreenshotMode(PetContext petContext, OnBackToHudModeListener listener) {
        super(petContext, new PhotoViewController(petContext));
        mBackToHudModeListener = listener;
        mPhotoViewController = new PhotoViewController(petContext);
        mPhotoViewController = (PhotoViewController) mModeScene;

        showViewScreenshot();
    }

    @Override
    protected void onEnter() {

    }

    @Override
    protected void onExit() {
    }

    private void showViewScreenshot() {
        IPhotoView view = mPhotoViewController.makeView(IPhotoView.class);
        view.setOnCancelClickListener(view1 -> mPetContext.getGVRContext().runOnGlThread(() -> mBackToHudModeListener.OnBackToHud()));
        view.show();
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
    }

}
