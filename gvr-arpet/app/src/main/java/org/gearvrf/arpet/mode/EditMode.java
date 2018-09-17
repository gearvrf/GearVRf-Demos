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

package org.gearvrf.arpet.mode;

import android.util.Log;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.character.CharacterController;

public class EditMode extends BasePetMode {
    private OnBackToHudModeListener mBackToHudModeListener;
    private CharacterController mCharacterController;

    public EditMode(PetContext petContext, OnBackToHudModeListener listener, CharacterController controller) {
        super(petContext, new EditView(petContext));
        mBackToHudModeListener = listener;
        ((EditView) mModeScene).setListenerEditMode(new OnEditModeClickedListenerHandler());
        mCharacterController = controller;
    }

    @Override
    protected void onEnter() {
        mCharacterController.getView().setRotationEnabled(true);
        mCharacterController.getView().setDraggingEnabled(true);
        mCharacterController.getView().setScaleEnabled(true);
    }

    @Override
    protected void onExit() {
        mCharacterController.getView().setRotationEnabled(false);
        mCharacterController.getView().setDraggingEnabled(false);
        mCharacterController.getView().setScaleEnabled(false);
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {

    }


    private class OnEditModeClickedListenerHandler implements OnEditModeClickedListener {

        @Override
        public void OnBack() {
            mBackToHudModeListener.OnBackToHud();
            Log.d(TAG, "On Back");
        }

        @Override
        public void OnSave() {

        }
    }

}
