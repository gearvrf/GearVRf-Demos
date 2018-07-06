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

package org.gearvrf.morph;

import android.graphics.Color;
import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMeshMorph;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRMorphAnimation;
import org.gearvrf.animation.GVRRepeatMode;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.TimeoutException;

public class SampleActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new SampleMain());
    }

    private static class SampleMain extends GVRMain
    {
        @Override
        public void onInit(GVRContext gvrContext)
        {
            GVRContext ctx = gvrContext;
            GVRScene scene = gvrContext.getMainScene();
            GVRSceneObject lightObj = new GVRSceneObject(ctx);
            GVRPointLight pointLight = new GVRPointLight(ctx);
            GVRCameraRig rig = scene.getMainCameraRig();
            GVRSceneObject model = null;
            GVRTransform t = scene.getMainCameraRig().getTransform();
            EnumSet<GVRImportSettings> settings = EnumSet.of(GVRImportSettings.TRIANGULATE,
                    GVRImportSettings.FLIP_UV,
                    GVRImportSettings.LIMIT_BONE_WEIGHT,
                    GVRImportSettings.SORTBY_PRIMITIVE_TYPE);
            rig.getCenterCamera().setBackgroundColor(Color.LTGRAY);
            rig.getLeftCamera().setBackgroundColor(Color.LTGRAY);
            rig.getRightCamera().setBackgroundColor(Color.LTGRAY);
            pointLight.setDiffuseIntensity(0.8f, 0.8f, 08f, 1.0f);
            pointLight.setSpecularIntensity(0.8f, 0.8f, 08f, 1.0f);
            lightObj.attachComponent(pointLight);
            lightObj.getTransform().setPosition(-1.0f, 1.0f, 0);
            scene.addSceneObject(lightObj);

            try
            {
                model = ctx.getAssetLoader().loadModel("faceBlendShapes_center.fbx");
            }
            catch (IOException ex)
            {
            }

            String[] shapeNames = {"Jason_Shapes_Ref:JasnNeutral:Default", "Jaw_Open", "Smile"};
            GVRMeshMorph morph = addMorph(model, shapeNames);

            centerModel(model, t);
            model.getTransform().setPositionZ(-1);
            scene.addSceneObject(model);
            GVRAnimator animator = setupAnimation(model, morph);
            animator.start();
        }

        public void centerModel(GVRSceneObject model, GVRTransform camTrans)
        {
            GVRSceneObject.BoundingVolume bv = model.getBoundingVolume();
            float x = camTrans.getPositionX();
            float y = camTrans.getPositionY();
            float z = camTrans.getPositionZ();
            float sf = 1 / bv.radius;
            model.getTransform().setScale(sf, sf, sf);
            bv = model.getBoundingVolume();
            model.getTransform()
                 .setPosition(x - bv.center.x, y - bv.center.y, z - bv.center.z - 1.5f * bv.radius);
        }

        private GVRMeshMorph addMorph(GVRSceneObject model, String shapeNames[])
        {
            GVRSceneObject baseShape = model.getSceneObjectByName(shapeNames[0]);
            GVRMeshMorph morph = new GVRMeshMorph(model.getGVRContext(), 2, false);

            baseShape.attachComponent(morph);
            for (int i = 1; i < shapeNames.length; ++i)
            {
                GVRSceneObject blendShape = model.getSceneObjectByName(shapeNames[i]);
                GVRSceneObject parent = blendShape.getParent();

                parent.removeChildObject(blendShape);
                morph.setBlendShape(i - 1, blendShape);
            }
            morph.update();
            return morph;
        }

        private GVRAnimator setupAnimation(GVRSceneObject root, GVRMeshMorph morph)
        {
            float[] keys = new float[]{0, 0, 0, 1, 1, 0, 2, 0, 0, 3, 0, 1};
            GVRAnimator animator = (GVRAnimator) root.getComponent(GVRAnimator.getComponentType());
            if (animator == null)
            {
                animator = new GVRAnimator(root.getGVRContext());
                root.attachComponent(animator);
            }
            GVRMorphAnimation morphAnim = new GVRMorphAnimation(morph, keys, 3);
            animator.addAnimation(morphAnim);
            animator.setRepeatMode(GVRRepeatMode.PINGPONG);
            animator.setRepeatCount(1000);
            return animator;
        }
    }
}
