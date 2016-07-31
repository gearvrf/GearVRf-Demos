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

package org.gearvrf.gvr360Photo;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Environment;
import android.view.MotionEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.utility.Log;

public class Minimal360PhotoScript extends GVRMain {

    GVRSceneObject photo;
    GVRScene scene;
    GVRContext gContext;

    private final String sEnvironmentPath = Environment.getExternalStorageDirectory().getPath();
    ArrayList<String> photoNames = new ArrayList<String>();
    ArrayList<GVRSceneObject> photoSceneArray = new ArrayList<GVRSceneObject>();
    GVRSceneObject currentShown = null;
    final String photoDirectory = "DCIM/Camera";
    int photoIndex = 0;
    boolean mIsSingleTapped = false;

    private SoundPool   mAudioEngine;
    private SoundEffect mPopSound;



    private GVRSphereSceneObject loadSkyBoxModel(GVRContext gvrContext) {
        GVRSphereSceneObject sphereObject = null;

        // load texture
        Future<GVRTexture> texture = null;
        try {
            texture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.raw.photosphere));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // create a sphere scene object with the specified texture and triangles facing inward (the 'false' argument)
        sphereObject = new GVRSphereSceneObject(gvrContext, false, texture);
        sphereObject.getTransform().setScale(100, 100, 100);
        return sphereObject;
    }

    void addSkyBox(){
        scene.addSceneObject(loadSkyBoxModel(gContext));
    }

    void getNamesOfPhotos(){
        ArrayList<String> extensions = new ArrayList<String>();
        extensions.add(".png");
        extensions.add(".jpg");
        extensions.add(".jpeg");


        CardReader cRObject = new CardReader(sEnvironmentPath + "/" + photoDirectory + "/", extensions);
        File list[] = cRObject.getModels();

        if (list != null)
            for (File aPhoto : list) {
                photoNames.add(aPhoto.getName());
            }

        Log.d("", "Photos found count" + Integer.toString(photoNames.size()));
    }


    void showAnimation(){
        GVRAnimation animation;
        ArrayList<GVRAnimation> aAnimation = new ArrayList<GVRAnimation>();

        for (int i = 0; i < photoSceneArray.size(); i++) {
            animation = new GVRRotationByAxisAnimation(photoSceneArray.get(i), 2, 360, 0, 1, 0).start(gContext.getAnimationEngine());
            //animation.setRepeatMode(1);
            animation.setRepeatCount(-1);

            aAnimation.add(animation);
        }

        for (int i = 0; i < photoSceneArray.size(); i++) {
            while (!aAnimation.get(i).isFinished()) {
            }
        }

        for (int i = 0; i < photoSceneArray.size(); i++) {
            gContext.getAnimationEngine().stop(aAnimation.get(i));
        }

        aAnimation.clear();
    }

    void loadPhotos(){

        showAnimation();


        // Removing Ealier Photos from Scene
        for(GVRSceneObject remov : photoSceneArray)
            scene.removeSceneObject(remov);

        photoSceneArray.clear();

        for(int i = 0; i < 30; i++){

            if(photoIndex < photoNames.size()) {
                Future<GVRTexture> photoTexture = null;
                try {
                    Log.d("", "Loading photo " + photoNames.get(photoIndex));
                    photoTexture = gContext.loadFutureTexture(new GVRAndroidResource(sEnvironmentPath + "/" + photoDirectory + "/" + photoNames.get(photoIndex)));

                } catch (IOException e) {
                    Log.e("", "Unable to load texture");
                    e.printStackTrace();
                }


                GVRSceneObject frame = new GVRSceneObject(gContext, gContext.createQuad(15.0f, 10.0f), null);
                GVRSphereSceneObject sphereObject = new GVRSphereSceneObject(gContext, true, photoTexture);

                sphereObject.getTransform().setScale(0.5f, 0.5f, 0.5f);
                sphereObject.getRenderData().setMesh(frame.getRenderData().getMesh());

                GVREyePointeeHolder playPauseHolder = new GVREyePointeeHolder(gContext);
                playPauseHolder.addPointee(new GVRMeshEyePointee(gContext, sphereObject.getRenderData().getMesh()));
                sphereObject.attachEyePointeeHolder(playPauseHolder);



                photoSceneArray.add(i, sphereObject);

                photoIndex++;
                photoIndex %= photoNames.size();
            }
        }
    }


    void showPhotos(){
        float yCoordinate = -5;
        float degrees = 36.0f;
        float currentDegree = 1;
        for(int i = 0; i < photoSceneArray.size(); i++){
            if((i == 10) || (i == 20)) {
                yCoordinate += 7.0f;
                currentDegree = 0;
            }

            scene.addSceneObject(photoSceneArray.get(i));
            photoSceneArray.get(i).getTransform().setPosition(0, yCoordinate, -25.0f);

            Log.d("", "Photo added to Scene");
            photoSceneArray.get(i).getTransform().rotateByAxisWithPivot(currentDegree, 0, 1, 0, 0, 0, 0);
            currentDegree += degrees;
            //if(i == 1)
            //break;

        }

    }

    private GVRTexture loadTexture(GVRContext gvrContext, String imageFile) {
        try {
            return gvrContext.loadTexture(new GVRAndroidResource(gvrContext, imageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addHeadTracker() {
        // Head Tracker
        GVRSceneObject headTracker;
        GVRTexture headTrackerTexture = loadTexture(gContext, "head-tracker.png");
        headTracker = new GVRSceneObject(gContext,
                gContext.createQuad(1.0f, 1.0f), headTrackerTexture);
        headTracker.getTransform().setPositionZ(-10.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(
                GVRRenderData.GVRRenderingOrder.OVERLAY);

        headTracker.getRenderData().setRenderingOrder(100000);
        scene.getMainCameraRig().addChildObject(headTracker);
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        gContext = gvrContext;

        // get a handle to the scene
        scene = gvrContext.getNextMainScene();
        scene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(Color.YELLOW);
        scene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(Color.YELLOW);


        mAudioEngine = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        try
        {
            mPopSound = new SoundEffect(gContext, mAudioEngine, "ChillingMusic.wav", true);
            mPopSound.setVolume(0.6f);
            mPopSound.play();
            mPopSound.setLooping(true);

        }
        catch (IOException ex)
        {
            Log.e("Audio", "Cannot load song.mp3");
        }

        getNamesOfPhotos();
        addSkyBox();
        loadPhotos();
        showPhotos();
        addHeadTracker();
        scene.bindShaders();
    }

    GVREyePointeeHolder getEyePointee() {
        GVREyePointeeHolder[] pickedHolders = null;
        pickedHolders = GVRPicker.pickScene(gContext.getMainScene());
        //Log.e(TAG, "Picked scene count" + Integer.toString(pickedHolders.length));
        if (pickedHolders.length > 0)
            return pickedHolders[0];
        else
            return null;
    }

    void hidePhotos(int index){
        for(int i = 0; i < photoSceneArray.size(); i++){
            if(i != index)
            scene.removeSceneObject(photoSceneArray.get(i));
        }
    }

    void showBackPhotos(int index){
        for(int i = 0; i < photoSceneArray.size(); i++){
            if(i != index)
            scene.addSceneObject(photoSceneArray.get(i));
        }
    }

    @Override
    public void onStep() {

        if(mIsSingleTapped){
            mIsSingleTapped = false;

            Log.d("", "Clicked Received");

            GVREyePointeeHolder holder = getEyePointee();
            for(int i = 0; i < photoSceneArray.size(); i++){
                if(holder.equals(photoSceneArray.get(i).getEyePointeeHolder())){
                    //photoSceneArray.get(i).getTransform().setScale(2,2,2);
                    if(currentShown != null){
                        int scaleFactor = 14;
                        float sf = 0.9f;
                        for (int j = 0; j < scaleFactor; j++) {
                            float x = photoSceneArray.get(i).getTransform().getScaleX();
                            float y = photoSceneArray.get(i).getTransform().getScaleY();
                            float z = photoSceneArray.get(i).getTransform().getScaleZ();
                            photoSceneArray.get(i).getTransform().setScale(sf * x, sf * y, sf * z);
                        }

                        currentShown = null;
                        showBackPhotos(i);
                        scene.bindShaders();
                    }else {
                        int scaleFactor = 15;
                        float sf = 1.1f;
                        for (int j = 0; j < scaleFactor; j++) {
                            float x = photoSceneArray.get(i).getTransform().getScaleX();
                            float y = photoSceneArray.get(i).getTransform().getScaleY();
                            float z = photoSceneArray.get(i).getTransform().getScaleZ();
                            photoSceneArray.get(i).getTransform().setScale(sf * x, sf * y, sf * z);
                        }

                        currentShown = photoSceneArray.get(i);
                        hidePhotos(i);
                    }
                }
                //else{
                //    scene.removeSceneObject(photoSceneArray.get(i));
                //}
            }
        }
    }

    public void onSingleTap(MotionEvent e) {
        Log.d("", "On Single Touch Received");
        mIsSingleTapped = true;
    }

    public void onSwipe(MotionEvent e, VRTouchPadGestureDetector.SwipeDirection swipeDirection,
                        float velocityX, float velocityY) {
        Log.d("", "On Swipe Received");
        loadPhotos();
        showPhotos();
    }

    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        Log.d("", "Angle mover called");
        return false;
    }
}
