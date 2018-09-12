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
import org.gearvrf.GVRContext;
import org.gearvrf.arpet.PetContext;

public class EditMode extends BasePetMode {
    private OnBackToHudModeListener mBackToHudModeListener;

    public EditMode(PetContext petContext, OnBackToHudModeListener listener) {
        super(petContext, new EditView(petContext));
        mBackToHudModeListener = listener;

        ((EditView) mModeScene).setListenerEditMode(new OnEditModeClickedListenerHandler());
    }

    @Override
    protected void onEnter() {

    }

    @Override
    protected void onExit() {
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
