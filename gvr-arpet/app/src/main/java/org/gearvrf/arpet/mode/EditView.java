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

import android.view.View;
import android.widget.Button;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class EditView extends BasePetView implements View.OnClickListener, IViewEvents {
    private GVRSceneObject mEditModeObject;
    private Button mBackButton, mSaveButton;
    private OnEditModeClickedListener mListenerEditMode;

    public EditView(PetContext petContext) {
        super(petContext);
        mEditModeObject = new GVRViewSceneObject(petContext.getGVRContext(),
                R.layout.edit_mode_layout, this);
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListenerEditMode(OnEditModeClickedListener listenerEditMode) {
        mListenerEditMode = listenerEditMode;
    }

    @Override
    public void onInitView(GVRViewSceneObject EditModeSceneObject, View view) {
        mBackButton = view.findViewById(R.id.btn_back);
        mSaveButton = view.findViewById(R.id.btn_save);
        mBackButton.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject editSceneObject, View view) {
        editSceneObject.getTransform().setScale(1.2f, 1.2f, 1.0f);
        editSceneObject.getTransform().setPosition(-0.02f, 0.35f, -1.0f);
        editSceneObject.setTextureBufferSize(1024);
        addChildObject(mEditModeObject);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back) {
            mBackButton.post(new Runnable() {
                @Override
                public void run() {
                    mListenerEditMode.OnBack();
                }
            });
        } else if (view.getId() == R.id.btn_save) {
            mSaveButton.post(new Runnable() {
                @Override
                public void run() {
                    mListenerEditMode.OnSave();
                }
            });
        }
    }
}
