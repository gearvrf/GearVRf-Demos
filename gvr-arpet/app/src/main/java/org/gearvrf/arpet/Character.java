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

package org.gearvrf.arpet;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRSceneObject;

import java.io.IOException;

public class Character extends GVRSceneObject implements GVRDrawFrameListener {

    private enum PetAction {
        IDLE,
        TO_BALL,
        TO_SCREEN,
        TO_FOOD,
        TO_TOILET,
        TO_BED
    }

    private PetAction mCurrentAction;
    private GVRContext mContext;

    public Character(GVRContext gvrContext) {
        super(gvrContext);
        mCurrentAction = PetAction.IDLE;
        mContext = gvrContext;

        load3DModel();

    }

    public void goToBall() {
        mCurrentAction = PetAction.TO_BALL;
    }

    public void goToScreen() {
        mCurrentAction = PetAction.TO_SCREEN;
    }

    public void goToFood() {
        mCurrentAction = PetAction.TO_FOOD;
    }

    public void goToToilet() {
        mCurrentAction = PetAction.TO_TOILET;
    }

    public void goToBed() {
        mCurrentAction = PetAction.TO_BED;
    }

    private void moveToBall() {

    }

    private void moveToScreen() {

    }

    private void load3DModel() {
        GVRSceneObject sceneObject;
        try {
            sceneObject = mContext.getAssetLoader().loadModel("objects/Fox_Pokemon.obj");
            addChildObject(sceneObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawFrame(float v) {

    }
}
