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
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMeshMorph;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRVertexBuffer;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.animation.GVRMorphAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

public class SampleActivity extends GVRActivity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(new SampleMain());
    }

    private  GVRSceneObject mObjectRoot;
    private  int animDuration = 50;
    private GVRScene mScene = null;

    private class SampleMain extends GVRMain
    {
        @Override
        public void onInit(GVRContext gvrContext)
        {
            GVRScene scene = gvrContext.getMainScene();
            mScene = scene;

            GVRCameraRig rig = scene.getMainCameraRig();
            mObjectRoot = new GVRSceneObject(gvrContext);
            rig.getCenterCamera().setBackgroundColor(Color.BLACK);
            rig.getLeftCamera().setBackgroundColor(Color.BLACK);
            rig.getRightCamera().setBackgroundColor(Color.BLACK);

            String filePath = "/sloth/sloth.gltf";

            GVRSceneObject light1 = createLight(gvrContext, 1, 1, 1, new Vector3f(0,1.8f, 0));
            GVRSceneObject light2 = createLight(gvrContext ,1, 1, 1, new Vector3f(0,-0.8f, 0));

            mObjectRoot.addChildObject(light1);
            mObjectRoot.addChildObject(light2);

            try
            {
                addModeltoScene(filePath, new Vector3f(0.05f,0.05f,0.05f),
                        new Vector3f(0, -8.5f, -6.5f));
            }
            catch (IOException ex)
            {
            }

            scene.addSceneObject(mObjectRoot);

            GVRAnimator animator = setupAnimation(mObjectRoot);
            animator.start();
        }

        private GVRAnimator setupAnimation(GVRSceneObject root)
        {

            GVRSceneObject baseObject = mObjectRoot.getSceneObjectByName("Sloth_face");
            GVRMeshMorph morph = (GVRMeshMorph)baseObject.getComponent(GVRMeshMorph.getComponentType());
            int numBlendShapes = morph.getBlendShapeCount();

            float [] keys = generateAnimationKeys(numBlendShapes, animDuration);

            GVRAnimator animator = (GVRAnimator) root.getComponent(GVRAnimator.getComponentType());
            if (animator == null)
            {
                animator = new GVRAnimator(root.getGVRContext());
                root.attachComponent(animator);
            }

            GVRMorphAnimation morphAnim = new GVRMorphAnimation(morph, keys, numBlendShapes + 1);
            animator.addAnimation(morphAnim);
            animator.setRepeatMode(GVRRepeatMode.PINGPONG);
            animator.setRepeatCount(1000);
            return animator;
        }


        /*

        create animation keys in the format:
        t1, 0, 0, 0, 0, .....0, 0, 0
        t2, 0, 1, 0, 0, .....0, 0, 0
        t3, 0, 0, 1, 0, .....0, 0, 0
        t4, 0, 0, 0, 1, .....0, 0, 0
        .
        .

        t1, t2, ... tn are timestamps in the range [0,animDuration]

         */
        private float[] generateAnimationKeys(int numBlendShapes, int timeTicks)
        {
            timeTicks ++;
            int keyArraySize = numBlendShapes * timeTicks + timeTicks;
            float [] keys = new float[keyArraySize];
            int timeCounter = 0;
            for(int i = 0; i < keyArraySize; i += (numBlendShapes + 1) )
            {
                keys[i] = timeCounter;
                for(int j = i + 1; j <= i + numBlendShapes; j ++)
                    keys[j] = (timeCounter == (j % (numBlendShapes + 1)) ) ? 1 : 0;
                timeCounter ++;
            }
            return keys;
        }

        private void addModeltoScene(String filePath, Vector3f scale, Vector3f position) throws IOException {

            GVRAssetLoader loader = getGVRContext().getAssetLoader();
            GVRSceneObject root = loader.loadModel(filePath,GVRImportSettings.getRecommendedMorphSettings(), false, null);
            root.getTransform().setScale(scale.x,scale.y,scale.z);
            root.getTransform().setPosition(position.x, position.y, position.z);

            mObjectRoot.addChildObject(root);

        }

        private GVRSceneObject createLight(GVRContext context, float r, float g, float b, Vector3f position)
        {
            GVRSceneObject lightNode = new GVRSceneObject(context);
            GVRPointLight light = new GVRPointLight(context);

            lightNode.attachLight(light);
            lightNode.getTransform().setPosition(0, 0.5f, 0);
            light.setAmbientIntensity(0.7f * r, 0.7f * g, 0.7f * b, 1);
            light.setDiffuseIntensity(r , g , b , 1);
            light.setSpecularIntensity(r, g, b, 1);

            lightNode.getTransform().setPosition(position.x,position.y,position.z);

            return lightNode;
        }

    }
}
