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

package org.gearvrf.cubemap;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import android.util.Log;

public class CubemapScript extends GVRScript {

    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 2.0f;
    private static final int MAX_ENVIRONMENTS = 6;
    private static final String TAG = "CubemapScript";

    private GVRContext mGVRContext = null;

    // Type of object for the environment
    // 0: surrounding sphere using GVRSphereSceneObject
    // 1: surrounding cube using GVRCubeSceneObject and 1 GVRCubemapTexture
    //    (method A)
    // 2: surrounding cube using GVRCubeSceneObject and compressed ETC2 textures
    //    (method B, best performance)
    // 3: surrounding cube using GVRCubeSceneObject and 6 GVRTexture's
    //    (method C)
    // 4: surrounding cylinder using GVRCylinderSceneObject
    // 5: surrounding cube using six GVRSceneOjbects (quads)
    private int mEnvironmentType = 2;

    // Type of object for the reflective object
    // 0: reflective sphere using GVRSphereSceneObject
    // 1: reflective sphere using OBJ model
    private static final int mReflectiveType = 0;
    private Future<GVRTexture> mFutureCubemapTexture;
    private GVRMaterial mCubemapMaterial;
    private GVRMaterial mCompressedCubemapMaterial;
    private ArrayList<Future<GVRTexture>> mFutureTextureList;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getNextMainScene();
        scene.setStatsEnabled(true);
        scene.setFrustumCulling(true);

        // Uncompressed cubemap texture
        mFutureCubemapTexture = gvrContext.loadFutureCubemapTexture(new GVRAndroidResource(mGVRContext, R.raw.beach));
        mCubemapMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
        mCubemapMaterial.setMainTexture(mFutureCubemapTexture);

        // Compressed cubemap texture
        final Future<GVRTexture> futureCompressedCubemapTexture = gvrContext.loadFutureCompressedCubemapTexture(new GVRAndroidResource(mGVRContext,
                R.raw.museum));
        mCompressedCubemapMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
        mCompressedCubemapMaterial.setMainTexture(futureCompressedCubemapTexture);

        // List of textures (one per face)
        mFutureTextureList = new ArrayList<Future<GVRTexture>>(6);
        mFutureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.back)));
        mFutureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.right)));
        mFutureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.front)));
        mFutureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.left)));
        mFutureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.top)));
        mFutureTextureList.add(gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.bottom)));

        applyCubemap(scene);
    }

    private void applyCubemap(GVRScene scene) {
        switch (mEnvironmentType) {
            case 0:
                // ///////////////////////////////////////////////////////
                // create surrounding sphere using GVRSphereSceneObject //
                // ///////////////////////////////////////////////////////
                GVRSphereSceneObject mSphereEvironment = new GVRSphereSceneObject(
                        mGVRContext, 18, 36, false, mCubemapMaterial, 4, 4);
                mSphereEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mSphereEvironment);
                break;

            case 1:
                // ////////////////////////////////////////////////////////////
                // create surrounding cube using GVRCubeSceneObject method A //
                // ////////////////////////////////////////////////////////////
                GVRCubeSceneObject mCubeEvironment = new GVRCubeSceneObject(
                        mGVRContext, false, mCubemapMaterial);
                mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCubeEvironment);
                break;

            case 2:
                // /////////////////////////////////////////////////////////////
                // create surrounding cube using compressed textures method B //
                // /////////////////////////////////////////////////////////////
                mCubeEvironment = new GVRCubeSceneObject(
                        mGVRContext, false, mCompressedCubemapMaterial);
                mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCubeEvironment);
                break;

            case 3:
                // ////////////////////////////////////////////////////////////
                // create surrounding cube using GVRCubeSceneObject method C //
                // ////////////////////////////////////////////////////////////
                mCubeEvironment = new GVRCubeSceneObject(
                        mGVRContext, false, mFutureTextureList, 2);
                mCubeEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCubeEvironment);
                break;

            case 4:
                // ///////////////////////////////////////////////////////////
                // create surrounding cylinder using GVRCylinderSceneObject //
                // ///////////////////////////////////////////////////////////
                GVRCylinderSceneObject mCylinderEvironment = new GVRCylinderSceneObject(
                        mGVRContext, 0.5f, 0.5f, 1.0f, 10, 36, false, mCubemapMaterial, 2, 4);
                mCylinderEvironment.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH,
                        CUBE_WIDTH);
                scene.addSceneObject(mCylinderEvironment);
                break;

            case 5:
                // /////////////////////////////////////////////////////////////
                // create surrounding cube using six GVRSceneOjbects (quads) //
                // /////////////////////////////////////////////////////////////
                FutureWrapper<GVRMesh> futureQuadMesh = new FutureWrapper<GVRMesh>(
                        mGVRContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));

                GVRSceneObject mFrontFace = new GVRSceneObject(mGVRContext,
                        futureQuadMesh, mFutureCubemapTexture);
                mFrontFace.getRenderData().setMaterial(mCubemapMaterial);
                mFrontFace.setName("front");
                scene.addSceneObject(mFrontFace);
                mFrontFace.getTransform().setPosition(0.0f, 0.0f,
                        -CUBE_WIDTH * 0.5f);

                GVRSceneObject backFace = new GVRSceneObject(mGVRContext,
                        futureQuadMesh, mFutureCubemapTexture);
                backFace.getRenderData().setMaterial(mCubemapMaterial);
                backFace.setName("back");
                scene.addSceneObject(backFace);
                backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
                backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

                GVRSceneObject leftFace = new GVRSceneObject(mGVRContext,
                        futureQuadMesh, mFutureCubemapTexture);
                leftFace.getRenderData().setMaterial(mCubemapMaterial);
                leftFace.setName("left");
                scene.addSceneObject(leftFace);
                leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
                leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

                GVRSceneObject rightFace = new GVRSceneObject(mGVRContext,
                        futureQuadMesh, mFutureCubemapTexture);
                rightFace.getRenderData().setMaterial(mCubemapMaterial);
                rightFace.setName("right");
                scene.addSceneObject(rightFace);
                rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
                rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

                GVRSceneObject topFace = new GVRSceneObject(mGVRContext,
                        futureQuadMesh, mFutureCubemapTexture);
                topFace.getRenderData().setMaterial(mCubemapMaterial);
                topFace.setName("top");
                scene.addSceneObject(topFace);
                topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
                topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

                GVRSceneObject bottomFace = new GVRSceneObject(mGVRContext,
                        futureQuadMesh, mFutureCubemapTexture);
                bottomFace.getRenderData().setMaterial(mCubemapMaterial);
                bottomFace.setName("bottom");
                scene.addSceneObject(bottomFace);
                bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f,
                        0.0f);
                bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
                break;
        }

        GVRMaterial cubemapReflectionMaterial = new GVRMaterial(mGVRContext);
        cubemapReflectionMaterial.setTexture("diffuseTexture", mFutureCubemapTexture);

        GVRSceneObject sphere = null;
        switch (mReflectiveType) {
            case 0:
                // ///////////////////////////////////////////////////////
                // create reflective sphere using GVRSphereSceneObject //
                // ///////////////////////////////////////////////////////
                sphere = new GVRSphereSceneObject(mGVRContext, 18, 36, true,
                        cubemapReflectionMaterial);
                break;

            case 1:
                // ////////////////////////////////////////////
                // create reflective sphere using OBJ model //
                // ////////////////////////////////////////////
                Future<GVRMesh> futureSphereMesh = mGVRContext
                        .loadFutureMesh(new GVRAndroidResource(mGVRContext,
                                R.raw.sphere));
                sphere = new GVRSceneObject(mGVRContext, futureSphereMesh,
                        mFutureCubemapTexture);
                sphere.getRenderData().setMaterial(cubemapReflectionMaterial);
                break;
        }

        if (sphere != null) {
            sphere.setName("sphere");
//            scene.addSceneObject(sphere);
            sphere.getRenderData().setShaderTemplate(CubeMapShader.class);
            sphere.getTransform().setScale(SCALE_FACTOR, SCALE_FACTOR,
                    SCALE_FACTOR);
            sphere.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.25f);
        }

        for (GVRSceneObject so : scene.getWholeSceneObjects()) {
            Log.v(TAG, "scene object name : " + so.getName());
        }
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouch() {
        if (null != mGVRContext) {
            mGVRContext.runOnGlThread(new Runnable() {
                @Override
                public void run() {
                    mGVRContext.getMainScene().clear();
                    ++mEnvironmentType;
                    mEnvironmentType %= MAX_ENVIRONMENTS;
                    Log.i(TAG, "mEnvironmentType: " + mEnvironmentType);
                    applyCubemap(mGVRContext.getMainScene());
                }
            });
        }
    }
}
