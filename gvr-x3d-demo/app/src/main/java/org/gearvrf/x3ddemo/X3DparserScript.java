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

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshot3DCallback;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRTransform;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.SystemPropertyUtil;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Threads;
import org.gearvrf.x3d.InteractiveObject;
import org.gearvrf.x3d.Sensor;
import org.gearvrf.GVRSensor;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.opengl.GLES20.GL_POINTS;

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

public class X3DparserScript extends GVRMain
{

  private static final String TAG = X3DparserScript.class.getSimpleName();
  private GVRContext mGVRContext = null;
  GVRScene scene = null;

  public X3DparserScript(X3DparserActivity activity)
  {
  }

  public void onInit(GVRContext gvrContext)
  {
    mGVRContext = gvrContext;

    scene = gvrContext.getMainScene();
    scene.setBackgroundColor(.7f, .7f, .8f, 1);

    GVRModelSceneObject model = new GVRModelSceneObject(mGVRContext);
    // X3D test files should be in the assets directory.
    // Replace 'filename' to view another .x3d file
    String filename = SystemPropertyUtil.getSystemPropertyString("debug.gearvrf.gvr-x3d-demo");
    if (null == filename) {
      filename = "JavaScriptLightColors.x3d";
    }

    //filename = "JavaScript_clock.x3d";
    //filename = "cylSensor_CtrlMaterial.x3d";
    //filename = "cylSensor_CtrlLtColor.x3d";
    //filename = "cylSensor_CtrlRotation.x3d";
    //filename = "cylSensor_CtrlTextureTransform.x3d";
    //filename = "texturetransform_JS_rotation_anim.x3d";

    //filename = "SphereSensor_CtrlTranslation.x3d";
    //filename = "SphereSensor_CtrlMaterial.x3d";

    //filename = "Billboard2.x3d";
    //filename = "ProtoExample.x3d";
    //filename = "Proto_Cylinders.x3d";
    //filename = "Proto_Cones.x3d";
    //filename = "Proto_Boxes.x3d";
    //filename = "Proto_Spheres.x3d";
    //filename = "Proto_TextString.x3d";
    //filename = "Proto_SimpleIFS.x3d";
    //filename = "Proto_TextureTransform.x3d";
    //filename = "Proto_ImageTexture.x3d";

    //filename = "Proto_MovieTexture.x3d";
    //filename = "MovieTexture01.x3d";

    //filename = "JavaScriptChangeText.x3d";
    //filename = "font_SingleText.x3d";
    //filename = "font_SingleText.x3d";
    //filename = "JS_Text_ChangeFontStyle.x3d";

    //filename = "JavaScript_PerFrame_05_LightControls.x3d";
    //filename = "JavaScript_PerFrame_02_LaunchSphere.x3d";
    //filename = "JS_PerFrame_TextChange.x3d";
    //filename = "JS_Text_ChangeFontStyle.x3d";
    //filename = "MovieTexture_teapot.x3d";
    //filename = "MovieTexture_MultipleMovies.x3d";
    //filename = "MovieTexture_PitchSpeed.x3d";
    //filename = "MovieTexture_SliderBar.x3d";
    //filename = "MovieTextureChange.x3d";


    //filename = "JasonM.ply";
    //filename = "paulbourke.ply";
    filename = "JasonM_tris_ascii.ply";
    //filename = "JasonM_ascii.ply";
    //filename = "JasonM_ascii.ply";

    try
    {
      //GVRSceneObject gvrSceneObject = gvrContext.getAssetLoader().loadModel(filename, scene);
      //model.addChildObject( gvrSceneObject );
      model = gvrContext.getAssetLoader().loadModel(filename, scene);

      GVRTransform transform = model.getTransform();
      transform.setRotationByAxis(-90, 1, 0, 0);
      //transform.setRotationByAxis(60, 0, 1, 0);

      // add colliders to all objects under the touch sensor
      model.attachCollider(new GVRMeshCollider(gvrContext, true));


      GVRCameraRig mainCameraRig = gvrContext.getMainScene().getMainCameraRig();
      mainCameraRig.getTransform().setPosition(0, 2.75f,2.25f);


      GVRMaterial vertexColorMtl = new GVRMaterial(gvrContext, new GVRShaderId(VertexColorShader.class));
      List<GVRRenderData> rdatas = model.getAllComponents(GVRRenderData.getComponentType());
      for (GVRRenderData rdata : rdatas)
      {
        rdata.setMaterial(vertexColorMtl);
        //rdata.setDrawMode(GL_POINTS);
        //rdata.setCullFace( GVRRenderPass.GVRCullFaceEnum.Back );
        rdata.setCullFace( GVRRenderPass.GVRCullFaceEnum.None );
      }

      Sensor sensor = new Sensor("", Sensor.Type.TOUCH, model, true);

      final GVRContext gvrContextFinal = gvrContext;
      final GVRSceneObject modelFinal = model;
      final GVRCameraRig mainCameraRigFinal = mainCameraRig;

      sensor.getOwnerObject().forAllDescendants(
              new GVRSceneObject.SceneVisitor()
              {
                public boolean visit (GVRSceneObject obj)
                {
                  obj.attachCollider(new GVRMeshCollider(gvrContextFinal, true));
                  return true;
                }
              });
      sensor.getOwnerObject().getEventReceiver().addListener(new ISensorEvents() {
        boolean initialized = false;
        Quaternionf sphereRotation = new Quaternionf();
        AxisAngle4f sphereRotAxisAngle = new AxisAngle4f();
        Quaternionf initQuat = new Quaternionf();
        Vector3f initCameraDir = null;
        float[] initPlaneTranslation = new float[3];
        float initHitDistance = 0;

        @Override
        public void onSensorEvent(SensorEvent event) {
          if (event.isActive()) {
            GVRPicker.GVRPickedObject gvrPickedObject = event.getPickedObject();

            Log.e("X3DDBG", "parser script event active.");
            if (!initialized) {
              initialized = true;
              float[] lookAt = mainCameraRigFinal.getLookAt();
              initCameraDir = new Vector3f(lookAt[0], lookAt[1], lookAt[2]);

              initPlaneTranslation[0] = modelFinal.getTransform().getPositionX();
              initPlaneTranslation[1] = modelFinal.getTransform().getPositionY();
              initPlaneTranslation[2] = modelFinal.getTransform().getPositionZ();
              initHitDistance = gvrPickedObject.getHitDistance();
            }

            // initialize the input values for planeSensor and run the javaScript.
            sphereRotation.w = modelFinal.getTransform().getRotationW();
            sphereRotation.x = modelFinal.getTransform().getRotationX();
            sphereRotation.y = modelFinal.getTransform().getRotationY();
            sphereRotation.z = modelFinal.getTransform().getRotationZ();
            sphereRotation.get( sphereRotAxisAngle );

            // Quaternion to Axis-Angle flips the sign on the rotation
            if ( sphereRotAxisAngle.angle > Math.PI) sphereRotAxisAngle.angle = (float)(2*Math.PI - sphereRotAxisAngle.angle);
            else sphereRotAxisAngle.angle = -sphereRotAxisAngle.angle;

            float[] lookAt = mainCameraRigFinal.getLookAt();
            Vector3f cameraDir = new Vector3f(lookAt[0], lookAt[1], lookAt[2]);
            cameraDir.sub(initCameraDir);

            float x = initPlaneTranslation[0] + cameraDir.x * initHitDistance;
            float y = initPlaneTranslation[1] + cameraDir.y * initHitDistance;

            float xRotation = (cameraDir.x * initHitDistance) * (float) Math.PI / 2;
            float yRotation = (cameraDir.y * initHitDistance) * (float) Math.PI / 2;

            AxisAngle4f axisAngleX = new AxisAngle4f(xRotation, 0, 1, 0);
            Quaternionf quatX = new Quaternionf();
            quatX.set(axisAngleX);

            AxisAngle4f axisAngleY = new AxisAngle4f(yRotation, 1, 0, 0);
            Quaternionf quatY = new Quaternionf();
            quatY.set(axisAngleY);

            quatX.mul(quatY);
            quatX.mul(initQuat);
            modelFinal.getTransform().setRotation(quatX.w, quatX.x, quatX.y, quatX.z);

          }
        }
      });


      // check if a headlight was attached to the model's camera rig
      // during parsing, as specified by the NavigationInfo node.
      // If 4 objects are attached to the camera rig, one must be the
      // directionalLight. Thus attach a dirLight to the main camera
      if (GVRShader.isVulkanInstance()) // remove light on Vulkan
      {
        List<GVRLight> lights = model.getAllComponents(GVRLight.getComponentType());
        for (GVRLight l : lights)
        {
          GVRSceneObject owner = l.getOwnerObject();
          owner.getParent().removeChildObject(owner);
        }
      }
    }
    catch (FileNotFoundException e)
    {
      Log.d(TAG, "ERROR: FileNotFoundException: " + filename);
    }
    catch (IOException e)
    {
      Log.d(TAG, "Error IOException = " + e);
      e.printStackTrace();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    mGVRContext.getInputManager().selectController();
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