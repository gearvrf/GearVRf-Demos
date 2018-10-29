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

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.utility.Log;

public abstract class BasePetMode implements IPetMode {
    protected final String TAG;

    protected final PetContext mPetContext;
    protected final IPetView mModeScene;
    protected ILoadEvents mLoadListener;
    protected boolean mIsRunning;
    protected boolean mIsLoaded;

    public BasePetMode(PetContext petContext, IPetView sceneMode) {
        TAG = sceneMode.getClass().getSimpleName();
        mPetContext = petContext;
        mModeScene = sceneMode;
        mLoadListener = null;
        mIsRunning = false;
        mIsLoaded = false;
    }

    @Override
    public void enter() {
        Log.w(TAG, "enter");
        mModeScene.show(mPetContext.getMainScene());
        onEnter();

        mIsRunning = true;
    }

    @Override
    public void exit() {
        Log.w(TAG, "exit");
        mModeScene.hide(mPetContext.getMainScene());
        onExit();

        mIsRunning = false;
    }

    @Override
    public void load(ILoadEvents listener) {
        mLoadListener = listener;
        mIsLoaded = true;
    }

    @Override
    public void unload() {
        mLoadListener = null;
        mIsLoaded = false;
    }

    @Override
    public IPetView view() {
        return mModeScene;
    }

    public void handleOrientation() {
        onHandleOrientation(mPetContext.getMainScene().getMainCameraRig());
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public boolean isLoaded() {
        return mIsLoaded;
    }

    abstract protected void onEnter();

    abstract protected void onExit();

    abstract protected void onHandleOrientation(GVRCameraRig cameraRig);
}
