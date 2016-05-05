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

package org.gearvrf.sample.remote_scripting;

import java.lang.Runnable;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import android.hardware.Camera;
import android.os.Handler;

public class PassthroughUtils {
    private GVRContext gvrContext;
    private GVRCameraSceneObject cameraObject;
    private GearVRScripting activity;
    private Camera camera;
    private boolean previewStarted = false;
    private Handler handler;

    public PassthroughUtils(GVRContext context, GearVRScripting activity) {
        gvrContext = context;
        this.activity = activity;
        camera = activity.getCamera();
        handler = activity.getHandler();
    }

    public void show() {
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    camera.startPreview();
                    previewStarted = true;
                }
            });

        handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    gvrContext.runOnGlThread(createCameraObject);
                }
            }, 100);
    }

    private float PASSTHROUGH_ASPECT = 16.0f / 9.0f;
    private float PASSTHROUGH_HEIGHT = 24.36f;
    private float PASSTHROUGH_WIDTH = PASSTHROUGH_HEIGHT * PASSTHROUGH_ASPECT;
    private float PASSTHROUGH_Z = -45.0f;

    private Runnable createCameraObject = new Runnable() {
        @Override
        public void run() {
            if(cameraObject == null) {
                cameraObject = new GVRCameraSceneObject(gvrContext, PASSTHROUGH_WIDTH, PASSTHROUGH_HEIGHT, camera);
                cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
                cameraObject.getTransform().setPosition(0.0f, 0.0f, PASSTHROUGH_Z);
                cameraObject.getRenderData().setRenderingOrder(GVRRenderingOrder.BACKGROUND);
                cameraObject.setName("passthrough");
            }
            gvrContext.getMainScene().getMainCameraRig().addChildObject(cameraObject);
        }
    };

    public void hide() {
        activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    camera.stopPreview();
                    previewStarted = false;
                }
            });

        gvrContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    if(cameraObject != null) {
                        gvrContext.getMainScene().removeSceneObject(cameraObject);
                    }
                }
            });
    }

}
