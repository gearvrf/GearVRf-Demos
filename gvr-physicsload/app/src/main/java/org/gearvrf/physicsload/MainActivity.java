/* Copyright 2018 Samsung Electronics Co., LTD
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

package org.gearvrf.physicsload;

import android.os.Bundle;
import android.util.Log;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.physics.GVRPhysicsLoader;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.scene_objects.GVRCubeSceneObject;

import java.io.IOException;

/*
 * This is a simple application to demonstrate how to use GVRPhysicsLoader to load bullet files.
 *
 * Before loading any bullet file it is necessary to init physics world (create GVRWorld and attach
 * it to main scene root object). Also it is required to create and add to the scene all objects
 * that will have rigid body attached to it. These objects must have a name and this name must
 * match the rigid body name set on bullet file. If you are using an authoring tool like Blender
 * a name is automatically set to each object, and the same name will be used for the rigid body
 * when exporting to bullet file.
 *
 * After initializing physics world and creating the required objects bullet file can be loaded
 * using a single method call. GVRPhysicsLoader will look for the required objects in the scene
 * and attach rigid bodies and constraints to them. Some physics components present in the file
 * may not be used and will be discarded.
 *
 * This sample application uses objects and bullet file exported by Blender (see Blender project
 * in 'extras' directory) and also another bullet file created from a bullet application.
 */

public class MainActivity extends GVRActivity {
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setMain(new Main());
    }

    private final class Main extends GVRMain {
        @Override
        public void onInit(GVRContext gvrContext) {
            initScene();
            initPhysics();
            loadBlenderAssets();
            complementScene();
        }

        void initScene() {
            GVRScene mainScene = getGVRContext().getMainScene();

            // Camera and light settings were copied from Blender project available in 'extras'
            // directory
            mainScene.getMainCameraRig().getHeadTransform().setPosition(0f, 2.4f, 40f);
            mainScene.getMainCameraRig().setFarClippingDistance(100f);
            mainScene.getMainCameraRig().setNearClippingDistance(0.1f);

            GVRSceneObject sunObj = new GVRCubeSceneObject(getGVRContext());
            sunObj.getTransform().setPosition(8f, 3.4f, 41.7f);
            sunObj.getTransform().setRotation(0.8683812142694567f, -0.3738122646181239f, -0.06100199997212902f, -0.32008938364834f);
            GVRDirectLight sun = new GVRDirectLight(getGVRContext());
            sun.setDiffuseIntensity(1f, 1f, 1f, 1f);
            sun.setSpecularIntensity(1f, 1f, 1f, 1f);
            sunObj.attachComponent(sun);

            GVRSceneObject sun1Obj = new GVRCubeSceneObject(getGVRContext());
            sun1Obj.getTransform().setPosition(-15f, -1.38f, -32f);
            sun1Obj.getTransform().setRotation(0.7071067811865476f, -0.7071067811865476f, 0.0f, 0.0f);
            GVRDirectLight sun1 = new GVRDirectLight(getGVRContext());
            sun1.setDiffuseIntensity(1f, 1f, 1f, 1f);
            sun1.setSpecularIntensity(1f, 1f, 1f, 1f);
            sun1Obj.attachComponent(sun1);
        }

        void initPhysics() {
            GVRScene mainScene = getGVRContext().getMainScene();

            GVRWorld world = new GVRWorld(getGVRContext());
            world.setGravity(0f, -10f, 0f);
            mainScene.getRoot().attachComponent(world);
        }

        // Will load the object from a FBX file, attach a collider and add it to the scene
        void loadAndAddCollider(String fname) throws IOException {
            GVRSceneObject sceneObject = getGVRContext().getAssetLoader().loadModel(fname);

            // Will look for the actual object, i.e. the one that has a mesh
            // This approach works fine for this case, in which all FBX file contains just one
            // object.
            while (sceneObject.getChildrenCount() != 0) {
                if (sceneObject.getRenderData() != null) {
                    break;
                }

                sceneObject = sceneObject.getChildByIndex(0);
            }

            sceneObject.attachComponent(new GVRMeshCollider(getGVRContext(), true));

            // Will put this object at the root of the scene preserving world transform
            float[] modelMatrix = sceneObject.getTransform().getModelMatrix();
            sceneObject.getParent().removeChildObject(sceneObject);
            sceneObject.getTransform().setModelMatrix(modelMatrix);
            getGVRContext().getMainScene().addSceneObject(sceneObject);
        }

        void loadBlenderAssets() {
            GVRScene mainScene = getGVRContext().getMainScene();

            try {
                // 'Cone' and 'Cone.001' will be linked by a Hinge constraint
                loadAndAddCollider("Cone.fbx");
                loadAndAddCollider("Cone_001.fbx");

                // 'Cube' and 'Cube.001' will be linked by a Cone-twist constraint
                loadAndAddCollider("Cube.fbx");
                loadAndAddCollider("Cube_001.fbx");

                // 'Cube.002' and 'Cube.003' will be linked by a Generic 6DoF constraint
                loadAndAddCollider("Cube_002.fbx");
                loadAndAddCollider("Cube_003.fbx");

                loadAndAddCollider("Cube_004.fbx");

                // 'Cylinder' and 'Sphere' will be linked by a Point-to-point constraint
                loadAndAddCollider("Cylinder.fbx");
                loadAndAddCollider("Sphere.fbx");

                // Plane object is not being loaded due to an issue when exporting this kind of
                // object from Blender to GVRf with physics properties
//                loadAndAddCollider("Plane.fbx");

                // Up-axis must be ignored because scene objects were rotated when exported
                GVRPhysicsLoader.loadPhysicsFile(getGVRContext(), "scene3.bullet", true, mainScene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void complementScene()
        {
            GVRScene mainScene = getGVRContext().getMainScene();

            // 'bodyA' and 'bodyB' will be linked by a Fixed constraint
            GVRMaterial redMat = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Phong.ID);
            redMat.setDiffuseColor(1f, 0f, 0f, 1f);
            GVRSceneObject box1 = new GVRCubeSceneObject(getGVRContext(), true, redMat);
            box1.getTransform().setPosition(5f, 5f, 10f);
            box1.setName("bodyA");
            box1.attachComponent(new GVRMeshCollider(getGVRContext(), true));
            mainScene.addSceneObject(box1);

            GVRMaterial whiteMat = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Phong.ID);
            whiteMat.setDiffuseColor(1f, 1f, 1f, 1f);
            GVRSceneObject box2 = new GVRCubeSceneObject(getGVRContext(), true, whiteMat);
            box2.getTransform().setPosition(5f, 10f, 10f);
            box2.setName("bodyB");
            box2.attachComponent(new GVRMeshCollider(getGVRContext(), true));
            mainScene.addSceneObject(box2);

            // 'bodyP' and 'bodyQ' will be linked by a Slider constraint
            GVRMaterial blueMat = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Phong.ID);
            blueMat.setDiffuseColor(0f, 0f, 1f, 1f);
            GVRSceneObject box3 = new GVRCubeSceneObject(getGVRContext(), true, blueMat);
            box3.getTransform().setPosition(-5f, 10f, 10f);
            box3.setName("bodyP");
            box3.attachComponent(new GVRMeshCollider(getGVRContext(), true));
            mainScene.addSceneObject(box3);

            GVRMaterial greenMat = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Phong.ID);
            greenMat.setDiffuseColor(0f, 1f, 0f, 1f);
            GVRSceneObject box4 = new GVRCubeSceneObject(getGVRContext(), true, greenMat);
            box4.getTransform().setPosition(-10f, 10f, 10f);
            box4.setName("bodyQ");
            box4.attachComponent(new GVRMeshCollider(getGVRContext(), true));
            mainScene.addSceneObject(box4);

            GVRMaterial yellowMat = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Phong.ID);
            yellowMat.setDiffuseColor(1f, 1f, 0f, 1f);
            GVRSceneObject box5 = new GVRCubeSceneObject(getGVRContext(), true, yellowMat);
            box5.getTransform().setPosition(-4.5f, 5f, 10.5f);
            box5.setName("barrier");
            box5.attachComponent(new GVRMeshCollider(getGVRContext(), true));
            mainScene.addSceneObject(box5);

            // This bullet file was created from a bullet application to add fixed and slider
            // constraints that are not available on Blender
            GVRPhysicsLoader.loadPhysicsFile(getGVRContext(), "fixed_slider.bullet", mainScene);

            // This object will replace the "Plane" exported by Blender as the floor of this scene
            GVRMaterial orangeMat = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Phong.ID);
            orangeMat.setDiffuseColor(0.7f, 0.3f, 0f, 1f);
            GVRSceneObject floor = new GVRSceneObject(getGVRContext(), 100f, 100f);
            floor.getTransform().setPosition(0f, -10f, 0f);
            floor.getTransform().setRotationByAxis(-90f, 1f, 0f, 0f);
            floor.getRenderData().setMaterial(orangeMat);
            floor.attachComponent(new GVRMeshCollider(getGVRContext(), floor.getRenderData().getMesh()));
            mainScene.addSceneObject(floor);
            GVRRigidBody floorRb = new GVRRigidBody(getGVRContext(), 0f);
            floor.attachComponent(floorRb);
        }
    }
}
