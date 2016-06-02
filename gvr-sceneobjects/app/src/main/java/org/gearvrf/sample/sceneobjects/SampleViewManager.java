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

package org.gearvrf.sample.sceneobjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRCameraSceneObject;
import org.gearvrf.scene_objects.GVRConeSceneObject;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.view.Gravity;

public class SampleViewManager extends GVRScript {
    private List<GVRSceneObject> objectList = new ArrayList<GVRSceneObject>();

    private int currentObject = 0;
    private SceneObjectActivity mActivity;

    SampleViewManager(SceneObjectActivity activity) {
        mActivity = activity;
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {

        GVRScene scene = gvrContext.getNextMainScene();

        // load texture asynchronously
        Future<GVRTexture> futureTexture = gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.gearvr_logo));
        Future<GVRTexture> futureTextureTop = gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.top));
        Future<GVRTexture> futureTextureBottom = gvrContext
                .loadFutureTexture(new GVRAndroidResource(gvrContext,
                        R.drawable.bottom));
        ArrayList<Future<GVRTexture>> futureTextureList = new ArrayList<Future<GVRTexture>>(
                3);
        futureTextureList.add(futureTextureTop);
        futureTextureList.add(futureTexture);
        futureTextureList.add(futureTextureBottom);

        // setup material
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTexture);

        // create a scene object (this constructor creates a rectangular scene
        // object that uses the standard 'unlit' shader)
        GVRSceneObject quadObject = new GVRSceneObject(gvrContext, 4.0f, 2.0f);
        GVRCubeSceneObject cubeObject = new GVRCubeSceneObject(gvrContext,
                true, material);
        GVRSphereSceneObject sphereObject = new GVRSphereSceneObject(
                gvrContext, true, material);
        GVRCylinderSceneObject cylinderObject = new GVRCylinderSceneObject(
                gvrContext, true, material);
        GVRConeSceneObject coneObject = new GVRConeSceneObject(gvrContext,
                true, material);
        GVRViewSceneObject webViewObject = createWebViewObject(gvrContext);
        GVRCameraSceneObject cameraObject = new GVRCameraSceneObject(
                gvrContext, 3.6f, 2.0f, mActivity.getCamera());
        cameraObject.setUpCameraForVrMode(1); // set up 60 fps camera preview.
        GVRVideoSceneObject videoObject = createVideoObject(gvrContext);
        GVRTextViewSceneObject textViewSceneObject = new GVRTextViewSceneObject(gvrContext, "Hello World!");
        textViewSceneObject.setGravity(Gravity.CENTER);
        textViewSceneObject.setTextSize(12);
        objectList.add(quadObject);
        objectList.add(cubeObject);
        objectList.add(sphereObject);
        objectList.add(cylinderObject);
        objectList.add(coneObject);
        objectList.add(webViewObject);
        objectList.add(cameraObject);
        objectList.add(videoObject);
        objectList.add(textViewSceneObject);

        // turn all objects off, except the first one
        int listSize = objectList.size();
        for (int i = 1; i < listSize; i++) {
            objectList.get(i).getRenderData().setRenderMask(0);
        }

        quadObject.getRenderData().setMaterial(material);

        // set the scene object positions
        quadObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        cubeObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);
        cylinderObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        coneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        sphereObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);
        cameraObject.getTransform().setPosition(0.0f, 0.0f, -4.0f);
        videoObject.getTransform().setPosition(0.0f, 0.0f, -4.0f);
        textViewSceneObject.getTransform().setPosition(0.0f, 0.0f, -2.0f);

        // add the scene objects to the scene graph.
        // deal differently with camera scene object: we want it to move
        // with the camera.
        for (GVRSceneObject object : objectList) {
            if (object instanceof GVRCameraSceneObject) {
                scene.getMainCameraRig().addChildObject(object);
            } else {
                scene.addSceneObject(object);
            }
        }
    }

    private GVRVideoSceneObject createVideoObject(GVRContext gvrContext) throws IOException {
        final AssetFileDescriptor afd = gvrContext.getActivity().getAssets().openFd("tron.mp4");
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mediaPlayer.prepare();
        GVRVideoSceneObject video = new GVRVideoSceneObject(gvrContext, 8.0f,
                4.0f, mediaPlayer, GVRVideoType.MONO);
        video.setName("video");
        return video;
    }

    private GVRViewSceneObject createWebViewObject(GVRContext gvrContext) {
        GVRView webView = mActivity.getWebView();
        GVRViewSceneObject webObject = new GVRViewSceneObject(gvrContext,
                webView, 8.0f, 4.0f);
        webObject.setName("web view object");
        webObject.getRenderData().getMaterial().setOpacity(1.0f);
        webObject.getTransform().setPosition(0.0f, 0.0f, -4.0f);

        return webObject;
    }

    public void onPause() {
        GVRSceneObject object = objectList.get(currentObject);
        if (object instanceof GVRVideoSceneObject) {
            GVRVideoSceneObject video = (GVRVideoSceneObject) object;
            video.getMediaPlayer().pause();
        }
    }

    public void onTap() {

        GVRSceneObject object = objectList.get(currentObject);
        object.getRenderData().setRenderMask(0);
        if (object instanceof GVRVideoSceneObject) {
            GVRVideoSceneObject video = (GVRVideoSceneObject) object;
            video.getMediaPlayer().pause();
        }

        currentObject++;
        int totalObjects = objectList.size();
        if (currentObject >= totalObjects) {
            currentObject = 0;
        }

        object = objectList.get(currentObject);
        if (object instanceof GVRVideoSceneObject) {
            GVRVideoSceneObject video = (GVRVideoSceneObject) object;
            video.getMediaPlayer().start();
        }

        object.getRenderData().setRenderMask(
                GVRRenderData.GVRRenderMaskBit.Left
                        | GVRRenderData.GVRRenderMaskBit.Right);

    }

    @Override
    public void onStep() {
    }
}
