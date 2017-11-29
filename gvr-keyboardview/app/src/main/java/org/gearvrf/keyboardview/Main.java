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

package org.gearvrf.keyboardview;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IKeyboardEvents;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRKeyboardSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class Main extends GVRMain {
    private final MainActivity mActivity;
    private GVRContext mContext;
    private GVRScene mScene;

    private GVRKeyboardSceneObject mKeyboardSceneObject;

    private GVRViewSceneObject mFrameLayoutFormSceneObject;
    private EditText mFocusedEdit;

    private TextView mInputEdit;
    private Button mButtonOk;
    private Button mButtonCancel;
    final float DEPTH = -1.8f;

    public Main(MainActivity activity) {
        mActivity = activity;

        mFocusedEdit = null;
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        mContext = gvrContext;
        mScene = gvrContext.getMainScene();

        mFrameLayoutFormSceneObject = new GVRViewSceneObject(gvrContext, R.layout.main_form, new ViewEventsHandler());
        mFrameLayoutFormSceneObject.getTransform().setPosition(0.0f, -0.3f, DEPTH);

        // Handle key events sent to main form.
        mFrameLayoutFormSceneObject.getEventReceiver().addListener(mKeyboardEventHandler);

        mScene.addSceneObject(mFrameLayoutFormSceneObject);

        mKeyboardSceneObject = new GVRKeyboardSceneObject.Builder()
            .setKeyboardTexture(gvrContext.getAssetLoader().loadTexture(
                    new GVRAndroidResource(gvrContext, R.drawable.keyboard_background)))
            .setKeyBackground(mActivity.getDrawable(R.drawable.key_background))
                .build(gvrContext, R.xml.qwerty);
        addGaze(gvrContext, mScene.getMainCameraRig());
    }

    private class ViewEventsHandler implements IViewEvents {

        @Override
        public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
            view.findViewById(R.id.nameEdit).setOnClickListener(mTextEditClickHandler);
            view.findViewById(R.id.emailEdit).setOnClickListener(mTextEditClickHandler);
            view.findViewById(R.id.phoneEdit).setOnClickListener(mTextEditClickHandler);
            view.findViewById(R.id.addButton).setOnClickListener(mTextEditClickHandler);

            view.findViewById(R.id.nameEdit).setOnFocusChangeListener(mTextEditFocusHandler);
            view.findViewById(R.id.emailEdit).setOnFocusChangeListener(mTextEditFocusHandler);
            view.findViewById(R.id.phoneEdit).setOnFocusChangeListener(mTextEditFocusHandler);
        }

        @Override
        public void onStartDraw(GVRViewSceneObject gvrViewSceneObject, View view) {
        }
    }

    private static void addGaze(GVRContext context, GVRCameraRig camera) {
        camera.addChildObject(createGaze(context, 0.0f, 0.0f, -1.0f));
    }

    public static GVRSceneObject createGaze(GVRContext context, float x, float y, float z) {
        GVRSceneObject gaze = new GVRSceneObject(context,
                new FutureWrapper<GVRMesh>(context.createQuad(0.1f, 0.1f)),
                context.getAssetLoader().loadFutureTexture(new GVRAndroidResource(context, R.drawable.gaze)));

        gaze.getTransform().setScale(0.5f, 0.5f, 0.5f);
        gaze.getTransform().setPosition(x, y, z);
        gaze.getRenderData().setDepthTest(false);
        gaze.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);

        GVRSceneObject fpsObject = new GVRFPSCounter(context);
        fpsObject.getTransform().setPosition(0.0f, -1.0f, -0.1f);
        fpsObject.getTransform().setScale(0.2f, 0.2f, 1.0f);

        gaze.addChildObject(fpsObject);

        return gaze;
    }

    @Override
    public void onStep() {

    }

    private View.OnFocusChangeListener mTextEditFocusHandler = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (mKeyboardSceneObject != null
                    && mKeyboardSceneObject.getParent() != null) {
                hideKeyboard();
            }
        }
    };

    private void showKeyboard(EditText editText) {
        int keyboard;
        switch (editText.getInputType()) {
            case EditorInfo.TYPE_CLASS_PHONE:
                mKeyboardSceneObject.setKeyboard(R.xml.numkbd);
                break;
            default:
                mKeyboardSceneObject.setKeyboard(R.xml.qwerty);
                break;
        }

        mScene.addSceneObject(mKeyboardSceneObject);
        mKeyboardSceneObject.startInput(mFrameLayoutFormSceneObject);
    }

    private void hideKeyboard() {
        mKeyboardSceneObject.stopInput();
    }

    private View.OnClickListener mTextEditClickHandler = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mKeyboardSceneObject.getTransform().setScale(1.0f, 1.0f, 1.0f);

            switch (v.getId()) {
                case R.id.nameEdit:
                    mKeyboardSceneObject.getTransform().setPosition(0.0f,  -0.45f, DEPTH + 0.20f);
                    mKeyboardSceneObject.getTransform().setRotationByAxis(-10, 1, 0, 0);
                    mKeyboardSceneObject.getTransform().setScale(1.4f, 1.4f, 1.0f);


                    break;
                case R.id.phoneEdit:
                    mKeyboardSceneObject.getTransform().setPosition(0.0f,  -0.5f, DEPTH + 0.15f);
                    mKeyboardSceneObject.getTransform().setRotationByAxis(-15, 1, 0, 0);
                    mKeyboardSceneObject.getTransform().setScale(0.4f, 0.4f, 1.0f);

                    break;
                case R.id.emailEdit:
                    mKeyboardSceneObject.getTransform().setPosition(0.0f,  -0.9f, DEPTH + 0.20f);
                    mKeyboardSceneObject.getTransform().setRotationByAxis(-20, 1, 0, 0);
                    mKeyboardSceneObject.getTransform().setScale(1.5f, 1.5f, 1.0f);


                    break;
                case R.id.addButton:
                    ((EditText)mFrameLayoutFormSceneObject.findViewById(R.id.nameEdit)).setText("");
                    ((EditText)mFrameLayoutFormSceneObject.findViewById(R.id.emailEdit)).setText("");
                    ((EditText)mFrameLayoutFormSceneObject.findViewById(R.id.phoneEdit)).setText("");
                    break;
            }

            if (v.hasFocus()) {
                mFocusedEdit = (EditText) v;
                if (mKeyboardSceneObject.getParent() == null) {
                    mFocusedEdit.setCursorVisible(true);
                    showKeyboard(mFocusedEdit);
                } else {
                    mFocusedEdit.setCursorVisible(false);
                    hideKeyboard();
                }
            } else if (mKeyboardSceneObject.getParent() != null) {
                hideKeyboard();
            }
        }
    };

    private IKeyboardEvents mKeyboardEventHandler = new IKeyboardEvents() {

        @Override
        public void onKey(GVRKeyboardSceneObject gvrKeyboardSceneObject, int primaryCode, int[] keyCodes) {
            switch (primaryCode) {
                // Add code here to handle key sent to the view
            }
        }

        @Override
        public void onStartInput(GVRKeyboardSceneObject gvrKeyboardSceneObject) {

        }

        @Override
        public void onStopInput(GVRKeyboardSceneObject gvrKeyboardSceneObject) {

        }
    };

}

