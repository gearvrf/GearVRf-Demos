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

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

import java.io.File;
import java.util.ArrayList;

public final class CubemapMain extends GVRMain {

    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 2.0f;
    private static final int MAX_ENVIRONMENTS = 6;
    private static final String TAG = "CubemapMain";

    private GVRContext mGVRContext = null;

    // Type of object for the environment
    // 0: surrounding sphere using GVRSphereSceneObject
    // 1: surrounding cube using GVRCubeSceneObject and 1 GVRCubemapImage
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
    private GVRTexture mCubemapTexture;
    private GVRMaterial mCubemapMaterial;
    private GVRMaterial mCompressedCubemapMaterial;
    private ArrayList<GVRTexture> mTextureList;
    private ArrayList<File> mSdcardResources;

    @Override
    public SplashMode getSplashMode() {
        return SplashMode.NONE;
    }
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getMainScene();
        GVRAssetLoader loader = mGVRContext.getAssetLoader();
        scene.setFrustumCulling(true);

        boolean usingSdcard = false;
        final File file = new File(Environment.getExternalStorageDirectory()+"/gvr-cubemap");
        if (file.exists()) {
            final File[] files = file.listFiles();
            if (0 < files.length) {
                for (final File f : files) {
                    final String name = f.getName();
                    if (name.endsWith(".bmp") || name.endsWith(".png") || name.endsWith(".zip")) {
                        if (null == mSdcardResources) {
                            usingSdcard = true;
                            mEnvironmentType = 0;
                            mSdcardResources = new ArrayList<>();
                        }
                        mSdcardResources.add(f);
                    }
                }
            }
        }

        if (!usingSdcard) {
            scene.setStatsEnabled(true);
            // Uncompressed cubemap texture
            mCubemapTexture = loader.loadCubemapTexture(new GVRAndroidResource(mGVRContext, R.raw.beach));
            mCubemapMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
            mCubemapMaterial.setMainTexture(mCubemapTexture);

            // Compressed cubemap texture
            final GVRTexture compressedCubemapTexture = loader.loadCompressedCubemapTexture(new GVRAndroidResource(mGVRContext,
                    R.raw.museum));
            mCompressedCubemapMaterial = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Cubemap.ID);
            mCompressedCubemapMaterial.setMainTexture(compressedCubemapTexture);

            // List of textures (one per face)
            mTextureList = new ArrayList<GVRTexture>(6);
            mTextureList.add(loader.loadTexture(new GVRAndroidResource(gvrContext,
                    R.drawable.back)));
            mTextureList.add(loader.loadTexture(new GVRAndroidResource(gvrContext,
                    R.drawable.right)));
            mTextureList.add(loader.loadTexture(new GVRAndroidResource(gvrContext,
                    R.drawable.front)));
            mTextureList.add(loader.loadTexture(new GVRAndroidResource(gvrContext,
                    R.drawable.left)));
            mTextureList.add(loader.loadTexture(new GVRAndroidResource(gvrContext,
                    R.drawable.top)));
            mTextureList.add(loader.loadTexture(new GVRAndroidResource(gvrContext,
                    R.drawable.bottom)));
            applyCubemap(scene);
        } else {
            applyFromSdcard(scene);
        }
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
                        mGVRContext, false, mTextureList, 2);
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
                GVRSceneObject mFrontFace = new GVRSceneObject(mGVRContext,
                                                               CUBE_WIDTH, CUBE_WIDTH, mCubemapTexture);
                GVRMesh quadMesh = mFrontFace.getRenderData().getMesh();

                mFrontFace.getRenderData().setMaterial(mCubemapMaterial);
                mFrontFace.setName("front");
                scene.addSceneObject(mFrontFace);
                mFrontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

                GVRSceneObject backFace = new GVRSceneObject(mGVRContext,
                                                             quadMesh, mCubemapTexture);
                backFace.getRenderData().setMaterial(mCubemapMaterial);
                backFace.setName("back");
                scene.addSceneObject(backFace);
                backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
                backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

                GVRSceneObject leftFace = new GVRSceneObject(mGVRContext,
                                                             quadMesh, mCubemapTexture);
                leftFace.getRenderData().setMaterial(mCubemapMaterial);
                leftFace.setName("left");
                scene.addSceneObject(leftFace);
                leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
                leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);

                GVRSceneObject rightFace = new GVRSceneObject(mGVRContext,
                                                              quadMesh, mCubemapTexture);
                rightFace.getRenderData().setMaterial(mCubemapMaterial);
                rightFace.setName("right");
                scene.addSceneObject(rightFace);
                rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
                rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);

                GVRSceneObject topFace = new GVRSceneObject(mGVRContext,
                                                            quadMesh, mCubemapTexture);
                topFace.getRenderData().setMaterial(mCubemapMaterial);
                topFace.setName("top");
                scene.addSceneObject(topFace);
                topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
                topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

                GVRSceneObject bottomFace = new GVRSceneObject(mGVRContext,
                                                               quadMesh, mCubemapTexture);
                bottomFace.getRenderData().setMaterial(mCubemapMaterial);
                bottomFace.setName("bottom");
                scene.addSceneObject(bottomFace);
                bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f,
                        0.0f);
                bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
                break;
        }

        GVRMaterial cubemapReflectionMaterial = new GVRMaterial(mGVRContext, GVRMaterial.GVRShaderType.CubemapReflection.ID);
        cubemapReflectionMaterial.setTexture("diffuseTexture", mCubemapTexture);
        cubemapReflectionMaterial.setMainTexture(mCubemapTexture);

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
                GVRMesh sphereMesh = mGVRContext.getAssetLoader().loadMesh(new GVRAndroidResource(mGVRContext,
                                                          R.raw.sphere));
                sphere = new GVRSceneObject(mGVRContext, sphereMesh, mCubemapTexture);
                sphere.getRenderData().setMaterial(cubemapReflectionMaterial);
                break;
        }

        if (sphere != null) {
            sphere.setName("sphere");
            scene.addSceneObject(sphere);
            sphere.getTransform().setScale(SCALE_FACTOR, SCALE_FACTOR,
                    SCALE_FACTOR);
            sphere.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.25f);
        }

        for (GVRSceneObject so : scene.getWholeSceneObjects()) {
            Log.v(TAG, "scene object name : " + so.getName());
        }
    }

    private void applyFromSdcard(final GVRScene scene) {
        final File file = mSdcardResources.get(mEnvironmentType);

        if (file.getName().endsWith(".zip")) {
            scene.getMainCameraRig().setCameraRigType(GVRCameraRig.GVRCameraRigType.Free.ID);

            try {
                final GVRTexture cubemapTexture = getGVRContext().getAssetLoader().loadCubemapTexture(new GVRAndroidResource(file.getAbsolutePath()));
                final GVRMaterial material = new GVRMaterial(getGVRContext(), GVRMaterial.GVRShaderType.Cubemap.ID);
                material.setMainTexture(cubemapTexture);

                final GVRSceneObject sceneObject = new GVRCubeSceneObject(mGVRContext, false, material);
                sceneObject.getTransform().setScale(CUBE_WIDTH, CUBE_WIDTH, CUBE_WIDTH);
                scene.addSceneObject(sceneObject);
            } catch(final Exception exc) {
                exc.printStackTrace();
            }

        } else {
            applyFromSdcard2dImpl(file);
        }
    }

    private void applyFromSdcard2dImpl(final File file) {
        final GVRScene scene = getGVRContext().getMainScene();
        scene.getMainCameraRig().setCameraRigType(GVRCameraRig.GVRCameraRigType.Freeze.ID);

        try {
            final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), bitmapOptions);

            final GVRTexture t = getGVRContext().getAssetLoader().loadTexture(new GVRAndroidResource(file.getAbsolutePath()));
            final GVRSceneObject sceneObject = new GVRSceneObject(mGVRContext, 20, 20*bitmapOptions.outHeight/bitmapOptions.outWidth, t);
            sceneObject.getTransform().setPositionZ(-11);
            scene.addSceneObject(sceneObject);
        } catch(final Exception exc) {
            exc.printStackTrace();
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

                    if (null == mSdcardResources) {
                        mEnvironmentType %= MAX_ENVIRONMENTS;
                        applyCubemap(mGVRContext.getMainScene());
                    } else {
                        mEnvironmentType %= mSdcardResources.size();
                        applyFromSdcard(mGVRContext.getMainScene());
                    }

                    Log.i(TAG, "mEnvironmentType: " + mEnvironmentType);
                }
            });
        }
    }
}

