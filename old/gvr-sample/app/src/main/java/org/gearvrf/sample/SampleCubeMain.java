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

package org.gearvrf.sample;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderMaskBit;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshot3DCallback;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRTexture;
import org.gearvrf.IPickEvents;
import org.gearvrf.utility.Threads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SampleCubeMain extends GVRMain {

    private static final float CUBE_WIDTH = 20.0f;
    private GVRSceneObject mFrontFace;
    private GVRSceneObject mFrontFace2;
    private GVRSceneObject mFrontFace3;
    @SuppressWarnings("unused")
    private GVRPicker mPicker;

    @Override
    public void onInit(GVRContext gvrContext) {
        final GVRScene scene = gvrContext.getMainScene();

        mPicker = new GVRPicker(gvrContext, scene);
        final GVRMesh mesh = gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH);
        final GVRTexture frontTexture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.front));

        mFrontFace = new GVRSceneObject(gvrContext, mesh,frontTexture);
        mFrontFace.setName("front");
        scene.addSceneObject(mFrontFace);
        mFrontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        mFrontFace2 = new GVRSceneObject(gvrContext, mesh, frontTexture);
        mFrontFace2.setName("front2");
        scene.addSceneObject(mFrontFace2);
        mFrontFace2.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f * 2.0f);

        mFrontFace3 = new GVRSceneObject(gvrContext, mesh, frontTexture);
        mFrontFace3.setName("front3");
        scene.addSceneObject(mFrontFace3);
        mFrontFace3.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f * 3.0f);

        mFrontFace.getRenderData().setDepthTest(false);
        mFrontFace2.getRenderData().setDepthTest(false);
        mFrontFace3.getRenderData().setDepthTest(false);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, mesh,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.back)));
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, mesh,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.left)));
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);
        leftFace.getRenderData().setRenderMask(GVRRenderMaskBit.Left);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext, mesh,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.right)));
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);
        rightFace.getRenderData().setRenderMask(GVRRenderMaskBit.Right);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, mesh,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.top)));
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext, mesh,
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.bottom)));
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);

        mFrontFace.attachComponent(new GVRMeshCollider(gvrContext, false));
        mFrontFace2.attachComponent(new GVRMeshCollider(gvrContext, false));
        mFrontFace3.attachComponent(new GVRMeshCollider(gvrContext, false));

        scene.getEventReceiver().addListener(new IPickEvents() {
            @Override
            public void onPick(GVRPicker picker) {
                final GVRPickedObject[] picked = picker.getPicked();

                final GVRRenderData furthest = picked[picked.length - 1].getHitObject().getRenderData();
                furthest.getMaterial().setOpacity(1.0f);
                furthest.setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);

                for (int i = 0; i < picked.length - 1; ++i) {
                    final GVRRenderData renderData = picked[i].getHitObject().getRenderData();
                    renderData.getMaterial().setOpacity(0.5f);
                    renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
                }
            }
            @Override
            public void onNoPick(GVRPicker picker) {
                mFrontFace.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
                mFrontFace.getRenderData().getMaterial().setOpacity(1.0f);
            }

            @Override
            public void onExit(GVRSceneObject sceneObj) {
                sceneObj.getRenderData().getMaterial().setOpacity(0.5f);
                sceneObj.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
            }

            @Override
            public void onEnter(GVRSceneObject sceneObj, GVRPickedObject collision) {
            }
            @Override
            public void onInside(GVRSceneObject sceneObj, GVRPickedObject collision) {
            }
        });
    }

    private boolean lastScreenshotLeftFinished = true;
    private boolean lastScreenshotRightFinished = true;
    private boolean lastScreenshotCenterFinished = true;
    private boolean lastScreenshot3DFinished = true;

    // mode 0: center eye; mode 1: left eye; mode 2: right eye
    public void captureScreen(final int mode, final String filename) {
        Threads.spawn(new Runnable() {
            public void run() {
                switch (mode) {
                    case 0:
                        if (lastScreenshotCenterFinished) {
                            getGVRContext().captureScreenCenter(newScreenshotCallback(
                                    filename, 0));
                            lastScreenshotCenterFinished = false;
                        }
                        break;
                    case 1:
                        if (lastScreenshotLeftFinished) {
                            getGVRContext().captureScreenLeft(newScreenshotCallback(
                                    filename, 1));
                            lastScreenshotLeftFinished = false;
                        }
                        break;
                    case 2:
                        if (lastScreenshotRightFinished) {
                            getGVRContext().captureScreenRight(newScreenshotCallback(
                                    filename, 2));
                            lastScreenshotRightFinished = false;
                        }
                        break;
                }
            }
        });
    }

    public void captureScreen3D(String filename) {
        if (lastScreenshot3DFinished) {
            getGVRContext().captureScreen3D(newScreenshot3DCallback(filename));
            lastScreenshot3DFinished = false;
        }
    }

    private GVRScreenshotCallback newScreenshotCallback(final String filename,
                                                        final int mode) {
        return new GVRScreenshotCallback() {

            @Override
            public void onScreenCaptured(Bitmap bitmap) {
                if (bitmap != null) {
                    File file = new File(
                            Environment.getExternalStorageDirectory(), filename
                            + ".png");
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                                outputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e(TAG, "Returned Bitmap is null");
                }

                // enable next screenshot
                switch (mode) {
                    case 0:
                        lastScreenshotCenterFinished = true;
                        break;
                    case 1:
                        lastScreenshotLeftFinished = true;
                        break;
                    case 2:
                        lastScreenshotRightFinished = true;
                        break;
                }
            }
        };
    }

    private GVRScreenshot3DCallback newScreenshot3DCallback(
            final String filename) {
        return new GVRScreenshot3DCallback() {

            @Override
            public void onScreenCaptured(Bitmap[] bitmapArray) {
                Log.d(TAG, "Length of bitmapList: "
                        + bitmapArray.length);
                if (bitmapArray.length > 0) {
                    for (int i = 0; i < bitmapArray.length; i++) {
                        Bitmap bitmap = bitmapArray[i];
                        File file = new File(
                                Environment.getExternalStorageDirectory(),
                                filename + "_" + i + ".png");
                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100,
                                    outputStream);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Returned Bitmap List is empty");
                }

                // enable next screenshot
                lastScreenshot3DFinished = true;
            }
        };
    }

    private final static String TAG = "SampleCubeMain";
}
