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
import android.widget.LinearLayout;

import org.gearvrf.GVRScene;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;

public class HudView extends BasePetView implements View.OnClickListener, IViewEvents {
    private static final String TAG = "HudView";

    private LinearLayout MenuHud;
    private Button menuButton, closeButton, editModeButton, playBallButton, shareAnchorButton, cameraButton;
    private GVRViewSceneObject mHudMenu;
    private OnHudItemClicked mListener;

    public HudView(PetContext petContext) {
        super(petContext);
        mHudMenu = new GVRViewSceneObject(petContext.getGVRContext(), R.layout.hud_layout, this);
        mListener = null;
        getTransform().setPosition(0.95f, 0.0f,-1.6f);
    }

    @Override
    protected void onShow(GVRScene mainScene) {
        mainScene.getMainCameraRig().addChildObject(this);
    }

    @Override
    protected void onHide(GVRScene mainScene) {
        mainScene.getMainCameraRig().removeChildObject(this);
    }

    public void setListener(OnHudItemClicked listener) {
        mListener = listener;
    }

    @Override
    public void onClick(final View view) {
        if (mListener == null) {
            return;
        }

        switch (view.getId()) {
            case R.id.btn_menu:
                menuButton.post(new Runnable() {
                    @Override
                    public void run() {
                        MenuHud.setVisibility(View.VISIBLE);
                        closeButton.setVisibility(View.VISIBLE);
                        menuButton.setVisibility(View.GONE);

                    }
                });
                break;
            case R.id.btn_close:
                closeButton.post(new Runnable() {
                    @Override
                    public void run() {
                        MenuHud.setVisibility(View.INVISIBLE);
                        closeButton.setVisibility(View.INVISIBLE);
                        menuButton.setVisibility(View.VISIBLE);

                    }
                });
                break;
            case R.id.btn_edit:
                editModeButton.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onEditModeClicked();
                    }
                });
                break;
            case R.id.btn_fetchball:
                playBallButton.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onBallClicked();
                    }
                });
                break;
            case R.id.btn_shareanchor:
                shareAnchorButton.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onShareAnchorClicked();
                    }
                });
                break;
            case R.id.btn_camera:
                cameraButton.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onCameraClicked();
                    }
                });
            default:
                Log.d(TAG, "Invalid Option");
        }
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        MenuHud = view.findViewById(R.id.menuHud);
        menuButton = view.findViewById(R.id.btn_menu);
        closeButton = view.findViewById(R.id.btn_close);
        editModeButton = view.findViewById(R.id.btn_edit);
        playBallButton = view.findViewById(R.id.btn_fetchball);
        shareAnchorButton = view.findViewById(R.id.btn_shareanchor);
        cameraButton = view.findViewById(R.id.btn_camera);
        menuButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        editModeButton.setOnClickListener(this);
        playBallButton.setOnClickListener(this);
        shareAnchorButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        gvrViewSceneObject.setTextureBufferSize(1024);
        addChildObject(gvrViewSceneObject);
    }

}
