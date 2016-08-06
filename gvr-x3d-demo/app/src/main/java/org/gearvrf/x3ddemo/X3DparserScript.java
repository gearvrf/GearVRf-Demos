/* Copyright 2016 Samsung Electronics Co., LTD
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

package org.gearvrf.x3ddemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRActivity;
//import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
//import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVREventReceiver;

import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshot3DCallback;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTransform;
import org.gearvrf.utility.Threads;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.x3d.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 
 * @author m1.williams 
 * X3d Demo comes with test files in the assets directory:
 *         cylindersandplanes.x3d, helloworldtext.x3d, multipleanimations.x3d,
 *         multiplepointlights.x3d, navigationinfo.x3d, teapotandtorus.x3d
 * 
 *         In the line: "String filename = ", change to one of the above file
 *         names or supply you own.
 *
 */

public class X3DparserScript extends GVRScript
{

  private static final String TAG = "X3D Parser Script";
  private GVRContext mGVRContext = null;

  public GVRAnimationEngine mAnimationEngine;
  public List<GVRAnimation> mAnimations = new ArrayList<GVRAnimation>();

  X3Dobject x3dObject = null;
  GVRScene scene = null;

  public GVRSceneObject currentPickedObject = null;
  public boolean tappedObject = false;

  public X3DparserScript(X3DparserActivity activity)
  {
  }

  public void onInit(GVRContext gvrContext)
  {
    mGVRContext = gvrContext;

    scene = gvrContext.getNextMainScene(new Runnable()
    {
      @Override
      public void run()
      {
        for (GVRAnimation animation : mAnimations)
        {
          animation.start(mAnimationEngine);
        }
      }
    });

    mAnimationEngine = gvrContext.getAnimationEngine();
    scene.getMainCameraRig().getLeftCamera().setBackgroundColor(Color.BLACK);
    scene.getMainCameraRig().getRightCamera().setBackgroundColor(Color.BLACK);

    GVRModelSceneObject model = new GVRModelSceneObject(mGVRContext);
    // X3D test files should be in the assets directory.
    // Replace 'filename' to view another .x3d file
    //String filename = "navigationinfo.x3d";
    String filename = "sd:GearVRF/x3d/boxmesh.x3d";
    try
    {
      GVRCameraRig mainCameraRig = scene.getMainCameraRig();

      model = gvrContext.getAssetLoader().loadModel(filename, scene);
      GVRSceneObject x3dCamera = model.getSceneObjectByName("MainCamera");
      GVRCameraRig x3dCameraRig = x3dCamera.getCameraRig();
      GVRTransform x3dCameraTrans = x3dCamera.getTransform();
      Matrix4f x3dCameraMatrix = x3dCameraTrans.getLocalModelMatrix4f();
      Matrix4f modelMatrix = model.getTransform().getModelMatrix4f();
      Vector3f modelPos = new Vector3f();
      Vector3f camPos = new Vector3f();
      List<GVRAnimation> animations = model.getAnimations();
      int backgroundColor = x3dCameraRig.getLeftCamera().getBackgroundColor();

      mAnimations = animations;
      mainCameraRig.getLeftCamera().setBackgroundColor(backgroundColor);
      mainCameraRig.getRightCamera().setBackgroundColor(backgroundColor);
      mainCameraRig.setNearClippingDistance(x3dCameraRig.getNearClippingDistance());
      mainCameraRig.setFarClippingDistance(x3dCameraRig.getFarClippingDistance());
      mainCameraRig.getTransform().setModelMatrix(x3dCameraMatrix);

      // if the x3D camera is on top of the model
      // reposition the camera
      modelMatrix.getTranslation(modelPos);
      x3dCameraMatrix.getTranslation(camPos);
      if (modelPos.distance(camPos) < 0.001f)
      {
        GVRSceneObject.BoundingVolume bv = model.getBoundingVolume();
        float sf = 1 / bv.radius;

        model.getTransform().setScale(sf, sf, sf);
        Vector3f pos = new Vector3f(-bv.center.x, -bv.center.y, -bv.center.z);
        pos.mul(sf);
        model.getTransform().setPosition(pos.x, pos.y, pos.z - 4.0f);
      }

      // check if a headlight was attached to the model's camera rig
      // during parsing, as specified by the NavigationInfo node.
      GVRSceneObject headLightSceneObject = model.getSceneObjectByName("HeadLight");
      if (headLightSceneObject != null)
      {
        headLightSceneObject.getParent().removeChildObject(headLightSceneObject);
        mainCameraRig.addChildObject(headLightSceneObject);
      }

    }
    catch (FileNotFoundException e)
    {
      Log.d(TAG, "ERROR: FileNotFoundException: " + filename);
    }
    catch (IOException e)
    {
      Log.d(TAG, "Error IOException = " + e);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  } // end onInit()

  // @Override
  public void onStep()
  {
    FPSCounter.tick();
  }

  private boolean lastScreenshotLeftFinished = true;
  private boolean lastScreenshotRightFinished = true;
  private boolean lastScreenshotCenterFinished = true;
  private boolean lastScreenshot3DFinished = true;

  // mode 0: center eye; mode 1: left eye; mode 2: right eye
  public void captureScreen(final int mode, final String filename)
  {
    Threads.spawn(new Runnable()
    {
      public void run()
      {

        switch (mode)
        {
          case 0:
            if (lastScreenshotCenterFinished)
            {
              mGVRContext
                  .captureScreenCenter(newScreenshotCallback(filename, 0));
              lastScreenshotCenterFinished = false;
            }
            break;
          case 1:
            if (lastScreenshotLeftFinished)
            {
              mGVRContext.captureScreenLeft(newScreenshotCallback(filename, 1));
              lastScreenshotLeftFinished = false;
            }
            break;
          case 2:
            if (lastScreenshotRightFinished)
            {
              mGVRContext
                  .captureScreenRight(newScreenshotCallback(filename, 2));
              lastScreenshotRightFinished = false;
            }
            break;
        }
      }
    });
  }

  public void captureScreen3D(String filename)
  {
    if (lastScreenshot3DFinished)
    {
      mGVRContext.captureScreen3D(newScreenshot3DCallback(filename));
      lastScreenshot3DFinished = false;
    }
  }

  private GVRScreenshotCallback newScreenshotCallback(final String filename,
      final int mode)
  {
    return new GVRScreenshotCallback()
    {

      @Override
      public void onScreenCaptured(Bitmap bitmap)
      {
        if (bitmap != null)
        {
          File file = new File(Environment.getExternalStorageDirectory(),
              filename + ".png");
          FileOutputStream outputStream = null;
          try
          {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
          }
          catch (FileNotFoundException e)
          {
            e.printStackTrace();
          }
          finally
          {
            try
            {
              outputStream.close();
            }
            catch (IOException e)
            {
              e.printStackTrace();
            }
          }
        }
        else
        {
          Log.e("SampleActivity", "Returned Bitmap is null");
        }

        // enable next screenshot
        switch (mode)
        {
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

  private GVRScreenshot3DCallback newScreenshot3DCallback(final String filename)
  {
    return new GVRScreenshot3DCallback()
    {

      @Override
      public void onScreenCaptured(Bitmap[] bitmapArray)
      {
        Log.d("SampleActivity", "Length of bitmapList: " + bitmapArray.length);
        if (bitmapArray.length > 0)
        {
          for (int i = 0; i < bitmapArray.length; i++)
          {
            Bitmap bitmap = bitmapArray[i];
            File file = new File(Environment.getExternalStorageDirectory(),
                filename + "_" + i + ".png");
            FileOutputStream outputStream = null;
            try
            {
              outputStream = new FileOutputStream(file);
              bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            }
            catch (FileNotFoundException e)
            {
              e.printStackTrace();
            }
            finally
            {
              try
              {
                outputStream.close();
              }
              catch (IOException e)
              {
                e.printStackTrace();
              }
            }
          }
        }
        else
        {
          Log.e("SampleActivity", "Returned Bitmap List is empty");
        }

        // enable next screenshot
        lastScreenshot3DFinished = true;
      }
    };
  }
}
