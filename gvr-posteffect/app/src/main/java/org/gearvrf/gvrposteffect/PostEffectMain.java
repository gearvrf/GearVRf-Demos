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

package org.gearvrf.gvrposteffect;

import java.io.IOException;
import java.util.EnumSet;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDepthShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.GVRSwitch;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.scene_objects.GVRCameraSceneObject;

import android.util.Log;
import android.view.MotionEvent;

public class PostEffectMain extends GVRMain {

    private GVRContext mGVRContext;
    private GVRSwitch mSwitch;
    private static final String TAG = "RenderPassSample";
 
    public PostEffectMain(GVRActivity activity) {
    }
    
    @Override
    public void onInit(GVRContext gvrContext)
    {
        mGVRContext = gvrContext;
        GVRScene scene = gvrContext.getMainScene();
        GVRCameraSceneObject cameraObject = null;

        //
        // Create a camera scene object.
        // This step will fail if your camera cannot be accessed.
        //
        try
        {
            cameraObject = new GVRCameraSceneObject(gvrContext, 3.6f, 2.0f);
            cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
        }
        catch (GVRCameraSceneObject.GVRCameraAccessException e)
        {
            // Cannot open camera
            Log.e(TAG, "Cannot open the camera",e);
            mGVRContext.getActivity().finish();
        }
        //
        // At this point the camera will be using the OESShader
        // in the material attached to its render data.
		// We make a material based on this one using the same
        // texture but a custom blur shader instead of the OESShader.
        // The blur shader and material must be explicitly connected with "bindShader"
        //
        GVRMaterial cameraMtl = cameraObject.getRenderData().getMaterial();
		GVRSceneObject blurryCamera = new GVRSceneObject(gvrContext, gvrContext.createQuad(3.6f, 2.0f), cameraMtl.getMainTexture(), GVRMaterial.GVRShaderType.OES.ID);
        GVRRenderData rdata = blurryCamera.getRenderData();
        rdata.setShaderTemplate(BlurShader.class);
        rdata.bindShader(scene);

		//
		// Now we make a scene object to be the parent of both the normal
		// and blurry camera. Adding a switch component allows us to
		// programmatically choose which one will be displayed.
		//
		GVRSceneObject cameraRoot = new GVRSceneObject(gvrContext);
        cameraRoot.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        mSwitch = new GVRSwitch(gvrContext);
		cameraRoot.attachComponent(mSwitch);
		cameraRoot.addChildObject(cameraObject);	// index 0 = normal camera
		cameraRoot.addChildObject(blurryCamera);	// index 1 = blurry camera
        scene.getMainCameraRig().addChildObject(cameraRoot);
    }

    @Override
    public void onStep() {
    }

    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            int i = mSwitch.getSwitchIndex();
            if (++i >= 2)
            {
                i = 0;
            }
            mSwitch.setSwitchIndex(i);
            return true;
        }
        return false;
    }
}
