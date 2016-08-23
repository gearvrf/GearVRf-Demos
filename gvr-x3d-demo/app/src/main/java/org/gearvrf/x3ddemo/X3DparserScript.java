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
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVREventReceiver;

import org.gearvrf.GVRMesh;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
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

  private static final String TAG = X3DparserScript.class.getSimpleName();
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
          //Not required
          //animation.start(mAnimationEngine);
        }
      }
    });

    mAnimationEngine = gvrContext.getAnimationEngine();
    scene.getMainCameraRig().getLeftCamera().setBackgroundColor(Color.BLACK);
    scene.getMainCameraRig().getRightCamera().setBackgroundColor(Color.BLACK);

    GVRModelSceneObject model = new GVRModelSceneObject(mGVRContext);
    // X3D test files should be in the assets directory.
    // Replace 'filename' to view another .x3d file
    String filename = "touchSensor.x3d";
    try
    {
      GVRCameraRig mainCameraRig = scene.getMainCameraRig();

      model = gvrContext.getAssetLoader().loadModel(filename, scene);
      List<GVRAnimation> animations = model.getAnimations();
      mAnimations = animations;

      GVRSceneObject cursor = new GVRSceneObject(mGVRContext,
              new FutureWrapper<GVRMesh>(mGVRContext.createQuad(1.0f, 1.0f)),
              mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, R.raw.cursor)));
      cursor.getTransform().setPosition(0.0f, 0.0f, -10.0f);
      cursor.getRenderData().setDepthTest(false);
      cursor.getRenderData().setRenderingOrder(100000);

      mainCameraRig.addChildObject(cursor);

      // check if a headlight was attached to the model's camera rig
      // during parsing, as specified by the NavigationInfo node.
      // If 4 objects are attached to the camera rig, one must be the
      // directionalLight. Thus attach a dirLight to the main camera
      if (model.getCameraRig().getChildrenCount() > 3)
      {
        GVRSceneObject headlightSceneObject = new GVRSceneObject(gvrContext);
        headlightSceneObject.setName("headlightSceneObject");
        GVRDirectLight headLight = new GVRDirectLight(gvrContext);
        headlightSceneObject.attachLight(headLight);
        headLight.setDiffuseIntensity(1, 1, 1, 1);
        mainCameraRig.addChildObject(headlightSceneObject);
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

    GVRInputManager inputManager = mGVRContext.getInputManager();
    for (GVRCursorController controller : inputManager.getCursorControllers()) {
      if (controller.getControllerType() == GVRControllerType.GAZE) {
        controller.setPosition(0.0f, 0.0f, -10.0f);
      }
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
