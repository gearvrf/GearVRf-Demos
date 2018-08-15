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
import org.gearvrf.GVRTransform;

public class HudMode extends BasePetMode {
    private OnModeChange mModeChangeListener;

    public HudMode(GVRContext context, OnModeChange listener) {
        super(context, new HudScene(context));
        mModeChangeListener = listener;

        ((HudScene) mModeScene).setListener(new OnHudItemClickedHandler());
    }

    @Override
    protected void onEnter() {
    }

    @Override
    protected void onExit() {
    }

    @Override
    protected void onHandleOrientation(GVRCameraRig cameraRig) {
        GVRTransform cameraTransform = cameraRig.getHeadTransform();
        float rotationZAxis = cameraTransform.getRotationRoll();

        if (rotationZAxis < 3 && rotationZAxis > -60 || rotationZAxis > -80 && rotationZAxis < 175) {
            mModeScene.getTransform().setPosition(cameraTransform.getPositionX() + 1.1f, cameraTransform.getPositionY(), cameraTransform.getPositionZ() - 5);
            mModeScene.getTransform().setRotation(0.9836236f, -0.09276326f, -0.15403655f, -0.012204962f);
            Log.d(TAG, "Landscape " + " Z: " + rotationZAxis);
        } else {
            mModeScene.getTransform().setPosition(cameraTransform.getPositionX() + 0.7f, cameraTransform.getPositionY() + 1.4f, cameraTransform.getPositionZ() - 5);
            mModeScene.getTransform().setRotation(0.6800038f - 1.0f, -0.166928f, 0.07900262f, -0.709568f);
            Log.d(TAG, "Portrait " + " Z: " + rotationZAxis);
        }
    }

    private class OnHudItemClickedHandler implements OnHudItemClicked {

        @Override
        public void onBallClicked() {
            mModeChangeListener.onPlayBall();
            Log.d(TAG, "Play Ball Mode");
        }

        @Override
        public void onShareAnchorClicked() {
            mModeChangeListener.onShareAnchor();
            Log.d(TAG, "Share Anchor Mode");
        }

        @Override
        public void onEditModeClicked() {
            mModeChangeListener.onEditMode();
            Log.d(TAG, "Edit Mode");
        }

        @Override
        public void onCameraClicked() {
            mModeChangeListener.onScreenshot();
            Log.d(TAG, "Camera Mode");
        }
    }
}
