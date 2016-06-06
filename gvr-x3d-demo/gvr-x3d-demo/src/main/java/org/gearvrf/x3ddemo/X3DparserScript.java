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

package org.gearvrf.x3ddemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//import javax.xml.parsers.SAXParser;
//import javax.xml.parsers.SAXParserFactory;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRActivity;
//import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
//import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventReceiver;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRPicker.GVRPickedObject;
//import org.gearvrf.GVRRenderPass.GVRCullFaceEnum;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.keyframe.GVRAnimationBehavior;
import org.gearvrf.animation.keyframe.GVRAnimationChannel;
import org.gearvrf.animation.keyframe.GVRKeyFrameAnimation;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRModelSceneObject;
//import org.gearvrf.util.GazeController;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRWebView;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScreenshot3DCallback;
import org.gearvrf.GVRScreenshotCallback;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.GVRTransform;
//import org.gearvrf.IEventReceiver;
//import org.gearvrf.NativeMesh;
import org.gearvrf.utility.Threads;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

import org.gearvrf.x3d.*;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

//import org.gearvrf.SensorEvent;

public class X3DparserScript extends GVRScript {

    private static final String TAG = "X3D Parser Script";
    private GVRContext mGVRContext = null;

    public GVRAnimationEngine mAnimationEngine;
    //public List<GVRKeyFrameAnimation> mAnimations = new ArrayList<GVRKeyFrameAnimation>();
    public List<GVRAnimation> mAnimations = new ArrayList<GVRAnimation>();

    // test code for animating viewpoints
    //GVRTransform gvrSceneObjectCameraOwnerTransform = null;
    //GVRTransform gvrAnimatedCameraTransform = null;
    // end test code

    X3Dobject x3dObject = null;
    GVRScene scene = null;

    public GVRSceneObject currentPickedObject = null;
    public boolean tappedObject = false;
    private X3DparserActivity mActivity;
    private AssetManager assetManager = null;
    
    
    // Animation viewpoint run from Script onStep() function
    private ViewpointAnimation viewpointAnimation = new ViewpointAnimation();


    public X3DparserScript(X3DparserActivity activity) {
        mActivity = activity;
    }

    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;


        scene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
            	
                //for (GVRKeyFrameAnimation animation : mAnimations) {
                for (GVRAnimation animation : mAnimations) {
                	
                  boolean sensorFound = false;
                  /*
	    		  for (Sensor x3dSensor: x3dObject.sensors) {
		    		//GVRKeyFrameAnimation gvrKeyFrameAnimationPointedFromSensor = x3dSensor.getGVRKeyFrameAnimation();
	    			GVRAnimation gvrKeyFrameAnimationPointedFromSensor = x3dSensor.getGVRKeyFrameAnimation();
	    			// if a sensor is pointing to this animation, then don't start this animation
	    			//    until the sensor is invoked.
	    			if (gvrKeyFrameAnimationPointedFromSensor == animation) sensorFound = true;
                  }
                  */
                  if (!sensorFound) animation.start(mAnimationEngine);
                  
                	//animation.start(mAnimationEngine);
                }
            }
        });

        mAnimationEngine = gvrContext.getAnimationEngine();

        
        scene.getMainCameraRig().getLeftCamera()
        .setBackgroundColor(Color.BLACK);
        scene.getMainCameraRig().getRightCamera()
        .setBackgroundColor(Color.BLACK);
        



        // head-tracking pointer
        GVRSceneObject headTracker = new GVRSceneObject(gvrContext,
                new FutureWrapper<GVRMesh>(gvrContext.createQuad(0.1f, 0.1f)),
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
 
        /*
         * This was test code to check if GVRKeyFrameAnimation worked for
         * the camera transformation.  At the moment it doesn't seem
         * supported but we will think about this and test further.
        GVRCameraRig gvrCameraRig = scene.getMainCameraRig();
        GVRSceneObject gvrSceneObjectCameraOwner = gvrCameraRig.getOwnerObject();
		gvrSceneObjectCameraOwner.setName("dummyName");
		//gvrSceneObjectCameraOwnerTransform = gvrSceneObjectCameraOwner.getTransform();
		
		
		//  GVRSceneObject gvrAnimatedCameraSceneObject = new GVRSceneObject(mGVRContext);
		//  gvrAnimatedCameraSceneObject.setName("animatedViewpointSceneObject test");
		//gvrAnimatedCameraTransform = gvrAnimatedCameraSceneObject.getTransform();
		//gvrAnimatedCameraTransform.setPosition(0, 0, -12);
		
		GVRKeyFrameAnimation gvrKeyFrameAnimation = new GVRKeyFrameAnimation("TestKeyFrameAnimation",
		        gvrSceneObjectCameraOwner, 4.0f * 60.0f, 60.0f);
		        //  gvrAnimatedCameraSceneObject, 4.0f * 60.0f, 60.0f);
		GVRAnimationChannel gvrAnimationChannel = new GVRAnimationChannel(
		        gvrSceneObjectCameraOwner.getName(), 3, 0, 0,
		      //gvrAnimatedCameraSceneObject.getName(), 3, 0, 0,
		      GVRAnimationBehavior.DEFAULT, GVRAnimationBehavior.DEFAULT);
		Vector3f vector3fa = new Vector3f(-3, 0, 0);
		gvrAnimationChannel.setPosKeyVector(0, 0, vector3fa);
		Vector3f vector3fb = new Vector3f(3, 0, 0);
		gvrAnimationChannel.setPosKeyVector(1, 120, vector3fb);
		Vector3f vector3fc = new Vector3f(-3, 0, 0);
    	gvrAnimationChannel.setPosKeyVector(2, 240, vector3fc);

//      gvrAnimationChannel.setPosKeyVector(j,
//      	routeToInterpolator.key[j] * routeTimeSensor.cycleInterval * framesPerSecond, vector3f);


        if ( gvrSceneObjectCameraOwner.getParent() == null) {
        	//System.out.println("gvrSceneObject.getParent() == null");
        	scene.removeSceneObject(gvrSceneObjectCameraOwner);
        	scene.addSceneObject(gvrAnimatedCameraSceneObject);
        }
        else {
          System.out.println("gvrSceneObject.getParent() != null");
        }
	    gvrAnimatedCameraSceneObject.addChildObject(gvrSceneObjectCameraOwner);
	
	
	    gvrKeyFrameAnimation.addChannel(gvrAnimationChannel);
	    gvrKeyFrameAnimation.setRepeatMode(GVRRepeatMode.REPEATED);
	    gvrKeyFrameAnimation.setRepeatCount(-1);
	
	    gvrKeyFrameAnimation.prepare();
	    mAnimations.add(gvrKeyFrameAnimation);
      */
        // end test code


        Context activityContext = gvrContext.getContext();
        assetManager = activityContext.getAssets();

        GVRModelSceneObject model = new GVRModelSceneObject(gvrContext);
	      //String filename = "animation01.x3d";
	      //String filename = "animation02.x3d";
	      //String filename = "animation03.x3d";
	      //String filename = "animation04.x3d";
	      //String filename = "animation05.x3d";
	      //String filename = "animation06.x3d";
	      //String filename = "animation07.x3d";
	      //String filename = "animation08.x3d";
	      //String filename = "animation09.x3d";
          //String filename = "animation_center10.x3d";
          //String filename = "animation_scale01.x3d";
          //String filename = "backgroundtexturemap01.x3d";
          //String filename = "boxesspheres.x3d";
          //String filename = "conered.x3d";
          //String filename = "cylinders.x3d";
          //String filename = "directionallight1.x3d";
        String filename = "elevationgrid.x3d";
          //String filename = "exponentcone.x3d";
          //String filename = "exponentplane.x3d";
          //String filename = "helloworldtext.x3d";
          //String filename = "inlinedemo01.x3d";
          //String filename = "levelofdetail01.x3d";
          //String filename = "levelofdetail02.x3d";
          //String filename = "levelofdetail03.x3d";
          //String filename = "navigationinfo.x3d";
          //String filename = "plane.x3d";
        	//String filename = "texturecoordinatetest.x3d";
          //String filename = "texturecoordinatetestsubset.x3d";
          //String filename = "texturecoordinatetestsubset2.x3d";
          //String filename = "text-lod-demo.x3d";
      //    String filename = "lighttest1.x3d";
          //String filename = "pointlighttest.x3d";
          //String filename = "pointlightsimple.x3d";
          //String filename = "spotlighttest1.x3d";
          //String filename = "spotlighttest2.x3d";
          //String filename = "spotlighttest3.x3d";
          //String filename = "spotlighttest4.x3d";
          //String filename = "multiviewpoints01.x3d";
          //String filename = "multiviewpoints02.x3d";
          //String filename = "multiviewpoints03.x3d";
          //String filename = "multiviewpoints04.x3d";
          //String filename = "touchSensor.x3d";
          //String filename = "teapottorusdirlights.x3d";
          //String filename = "pointlightattenuationtest.x3d";
          //String filename = "viewpointAnimation01.x3d";
          try {
        	  model = gvrContext.loadModel(filename);
        	  scene.addSceneObject(model);
        	  List<GVRAnimation> animations = model.getAnimations();
        	  mAnimations = animations;

	    	  GVRCameraRig gvrCameraRig = null;
	    	  //  Find the root node set by loadModel, and from there, find the Camera rig
	    	  GVRSceneObject gvrRoot = scene.getSceneObjectByName(x3dObject.X3D_ROOT_NODE);
	    	  List <GVRSceneObject> gvrRootChildren = gvrRoot.getChildren();
	  		  for (GVRSceneObject gvrRootChild: gvrRootChildren) {
	  			  GVRCameraRig gvrRootChildCameraRig = gvrRootChild.getCameraRig();
		  			if (gvrRootChildCameraRig != null) {
		  				gvrCameraRig = gvrRootChildCameraRig;
		  			} 
	  		  }
	  		  // GVRscene now has a new camera rig
	  		  if (gvrCameraRig != null) {
	        	scene.setMainCameraRig(gvrCameraRig);
	        	gvrCameraRig.addChildObject(headTracker);
	  		  }
	  		  else Log.d(TAG, "Error, finding camera rig in X3D. Contact Samsung GearVR engineering to report this issue");

	  		  // Below already set in GVRScene constructor, and seems ok not to have to reset these.
	  		  //scene.setFrustumCulling(true);
	  		  //scene.getEventReceiver().addListener(mSceneEventListener);
          }
          catch (FileNotFoundException e) {
            	Log.d(TAG, "ERROR: FileNotFoundException: " + filename);
            }
          catch (IOException e) {
            	Log.d(TAG, "Error IOException = " + e);
            }
          catch (Exception e) {
            e.printStackTrace();
         }
        //GazeController.setupGazeCursor(mGVRContext);
        //GazeController.enableGaze();
        //GazeController.enableInteractiveCursor();
        //GazeController.disableInteractiveCursor();
    }  //  end onInit()


    //@Override
    public void onStep() {
        FPSCounter.tick();

        currentPickedObject = null;
    	List<GVRPickedObject> gvrPickedObjectList = GVRPicker.findObjects(mGVRContext.getMainScene());
    	// GRPicker orders the list closest to further, so just grab the first pickable object
        if ( !gvrPickedObjectList.isEmpty() ) currentPickedObject = gvrPickedObjectList.get(0).getHitObject();
    
        if ( currentPickedObject != null) {
          if (tappedObject) {
        	  String text = "Last touched " + currentPickedObject.getName();
        	  PickedObjectActivity(text, 4);
          }
          else {
        	  String text = "Over " + currentPickedObject.getName();
        	  //  GazeController.enableInteractiveCursor();
        	  //tappedObject = true;
        	  PickedObjectActivity(text, 0);
          }
        }
        else {
        	//myTouchSensor("all quiet", 5);
        	//    GazeController.disableInteractiveCursor();
        	tappedObject = false;
        }
        if ( viewpointAnimation.getActiveAnimation()) {
			GVRCameraRig gvrCameraRig = scene.getMainCameraRig();
			GVRTransform cameraTransform = gvrCameraRig.getTransform();
			viewpointAnimation.updateAnimation();
			cameraTransform.setPosition(viewpointAnimation.currentPos[0], viewpointAnimation.currentPos[1], viewpointAnimation.currentPos[2]);
	        cameraTransform.setRotation(viewpointAnimation.currentQuaternion.w, 
	        	viewpointAnimation.currentQuaternion.x, viewpointAnimation.currentQuaternion.y, viewpointAnimation.currentQuaternion.z );
        }
    } // end onStep()


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
                        mGVRContext.captureScreenCenter(newScreenshotCallback(
                                filename, 0));
                        lastScreenshotCenterFinished = false;
                    }
                    break;
                case 1:
                    if (lastScreenshotLeftFinished) {
                        mGVRContext.captureScreenLeft(newScreenshotCallback(
                                filename, 1));
                        lastScreenshotLeftFinished = false;
                    }
                    break;
                case 2:
                    if (lastScreenshotRightFinished) {
                        mGVRContext.captureScreenRight(newScreenshotCallback(
                                filename, 2));
                        lastScreenshotRightFinished = false;
                    }
                    break;
                }
            }
        });
    }

    int textChangeMade = 0;
    public void PickedObjectActivity(String text, int textChangeMade) {
      GVRSceneObject gvrSceneObject = scene.getSceneObjectByName("textDisplay");

      int count = gvrSceneObject.getChildrenCount();
      GVRTextViewSceneObject textObject = null;
      for (int i = 0; i < count; i++) {
        textObject = (GVRTextViewSceneObject)gvrSceneObject.getChildByIndex(i);
      }

      if (text != "") {
        textObject.setText(text);

        if (textChangeMade == 0)textObject.setTextColor(Color.RED);
        else if (textChangeMade == 1)textObject.setTextColor(Color.GREEN);
        else if (textChangeMade == 2)textObject.setTextColor(Color.BLUE);
        else if (textChangeMade == 3)textObject.setTextColor(Color.CYAN);
        else if (textChangeMade == 4)textObject.setTextColor(Color.YELLOW);
        else if (textChangeMade == 5)textObject.setTextColor(Color.MAGENTA);
        else if (textChangeMade == 6)textObject.setTextColor(Color.GRAY);
        else if (textChangeMade == 7)textObject.setTextColor(Color.WHITE);
      }
    }

    // called by X3DparserActivity when a single tap is detected
    public void SingleTap() {
        tappedObject = true;
        if ( currentPickedObject != null) {
            
        	// check if we tapped on a sensor'd object to begin an animation or chg camera
            boolean interactivityFound = false;
    		for (Sensor x3dSensor: x3dObject.sensors) {
    			// 1) check if this [Touch] sensor initiates an animation
    			//for (GVRKeyFrameAnimation animation : mAnimations) {
        		//	GVRKeyFrameAnimation gvrKeyFrameAnimationPointedFromSensor = x3dSensor.getGVRKeyFrameAnimation();
        		for (GVRAnimation animation : mAnimations) {
            		GVRAnimation gvrKeyFrameAnimationPointedFromSensor = x3dSensor.getGVRKeyFrameAnimation();
        			// if a sensor is pointing to this animation, then start this animation
        			if (gvrKeyFrameAnimationPointedFromSensor == animation) {
        				animation.start(mAnimationEngine);
        				interactivityFound = true;
        			}
        		}
    			if (!interactivityFound) {
    				// 2) if clicked object wasn't part of a [Touch] sensor, check if it's a child of an Anchor
    				GVRSceneObject parent = currentPickedObject.getParent();
    				while (parent != null) {
    					if ( parent == x3dSensor.sensorSceneObject ) {
    						interactivityFound = true;
    						String url = x3dSensor.getAnchorURL();
    					    String text = "Anchor picked " + url + ", " + parent.getName();
    				        PickedObjectActivity(text, 6);
    				        if (url.charAt(0) == '#') {
    				        	// go to a new viewpoint
    				        	String viewpointURL = url.substring(1);  // get rid of the #
    				        	for (Viewpoint viewpoint: x3dObject.viewpoints) {
    				        		if (viewpoint.getName().equals(viewpointURL)) {
    				        			// only go to a new viewpoint if Jump is true
    				        			if (viewpoint.getJump()) {
	    				        			// if jump = true, animate to the new viewpoint
    				        				//   Note, the type of navigatio (teleport, linear)
    				        				//   is controlled by NavigationInfo
	    				        			GVRCameraRig gvrCameraRig = scene.getMainCameraRig();
	    				        			GVRTransform cameraTransform = gvrCameraRig.getTransform();
	    				        			// we either animate [jump=false] to the new camera location
	    				        			// or jump, in which case just set the new position and orientation.
	    				        			float[] endPosition = viewpoint.getPosition();
	    				        			float[] endOrientation = viewpoint.getOrientation();
	    				  			        AxisAngle4f axisAngle4f = new AxisAngle4f(endOrientation[3], endOrientation[0], endOrientation[1], endOrientation[2]);
	    								    Quaternionf endQuat = new Quaternionf(axisAngle4f).normalize();
	    								    /*
	    								     // NavigationInfo currently not implemented, 
	    								     //    but if translationType set to TELEPORT, then use these lines of code
	    								     if ( NavigationInfo.getTransitionType == NavigationInfo.TELEPORT ) {
	    				        				cameraTransform.setPosition(endPosition[0], endPosition[1], endPosition[2]);
	    							        	cameraTransform.setRotation(endQuat.w, endQuat.x, endQuat.y, endQuat.z);
	    							         }
	    							         else 
	    								     */
	    								    {
	    								    	// NavigationInfo transitionType either "LINEAR" or "ANIMATE"
	        				        			float[] bgnPosition = {cameraTransform.getPositionX(), cameraTransform.getPositionY(), cameraTransform.getPositionZ() };
	        				        			float[] bgnOrientation = {cameraTransform.getRotationX(), cameraTransform.getRotationY(), cameraTransform.getRotationZ(), cameraTransform.getRotationW()};
	        				  			        axisAngle4f = new AxisAngle4f(bgnOrientation[3], bgnOrientation[0], bgnOrientation[1], bgnOrientation[2]);
	        								    Quaternionf bgnQuat = new Quaternionf(axisAngle4f).normalize();
	        								    
	        								    // use the log of the distance for amount of time to navigate
	        								    float distance = 0;
	        								    for (int i = 0; i < 3; i++) {
	        								    	distance += (float) Math.pow((bgnPosition[i]-endPosition[i]), 2);
	        								    }
	        								    // take the log 10 so long distance animations are still fast.
	        								    distance = (float) Math.log10(distance);
	        								    if (distance < 2) distance = 2; // animations must take at least 1 second
	        								    viewpointAnimation.setupAnimation(distance, bgnPosition, endPosition, bgnQuat, endQuat);
	        								    viewpointAnimation.startAnimation();
    				        				}
    				        				
        								    // next two lines here for development purposes
	    				        			cameraTransform.setPosition(endPosition[0], endPosition[1], endPosition[2]);
	    							        cameraTransform.setRotation(endQuat.w, endQuat.x, endQuat.y, endQuat.z);
	    				        		}   				        			

    				        			break;
    				        		}
    				        	}
    				        }
    				        else if (url.endsWith(".x3d")) {
	    				        try {
	    				        	// load another x3d file based on the 'url' parameter
	    				        	scene.removeAllSceneObjects();
	    				        	System.out.println("Anchor for loading another X3D file not fully implemented");
	    			        		GVRSceneObject model = mGVRContext.loadModelFromURL(url);
	    			        		scene.addSceneObject(model);
	    			        	}
			        		  catch (FileNotFoundException e) {
					        		System.out.println("Error Loading new X3D file. File Not Found Exception: " + e);
			        		  }
			        		  catch (IOException ioException) {
					        		System.out.println("Error Loading new X3D file. IOException url " + url);
					        		System.out.println(ioException);
			        		  }
			        		  catch (Exception exception) {
					        		System.out.println("Error Loading new X3D file. Exception: " + exception);
			        		  }
    				        }
    				        else {
    				        	//A web page beginning with 'http://' or 'www.', etc.   
    				        	// X3D only specifies to open a web page, not inject a new
    				        	// scene object textured with a web page.  This issue will
    				        	// need to be re-visited.
    				        	GVRWebView gvrWebView = mActivity.getWebView();
    				        	gvrWebView.loadUrl(url);
    				        }
    						break;
    					}
    					else parent = parent.getParent();
    				}
    			}
        	}  //  end for sensor loop
        } // end if script.currentPickedObject != null
        else {
          String text = "Last action: TAP over non-interactive";
          PickedObjectActivity(text, 1);
        }
            //GazeController.enableInteractiveCursor();
    }  //  end onSingleTap
    
    
    public void captureScreen3D(String filename) {
        if (lastScreenshot3DFinished) {
            mGVRContext.captureScreen3D(newScreenshot3DCallback(filename));
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
                    Log.e("SampleActivity", "Returned Bitmap is null");
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
                Log.d("SampleActivity", "Length of bitmapList: "
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
                    Log.e("SampleActivity", "Returned Bitmap List is empty");
                }

                // enable next screenshot
                lastScreenshot3DFinished = true;
            }
        };
    }
}
