package org.gearvrf.gvrbullet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVROpacityAnimation;
import org.gearvrf.scene_objects.GVRCameraSceneObject;

import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.siprop.bullet.Bullet;
import org.siprop.bullet.Geometry;
import org.siprop.bullet.MotionState;
import org.siprop.bullet.PhysicsWorld;
import org.siprop.bullet.RigidBody;
import org.siprop.bullet.Transform;
import org.siprop.bullet.shape.CylinderShape;
import org.siprop.bullet.shape.SphereShape;
import org.siprop.bullet.shape.StaticPlaneShape;
import org.siprop.bullet.util.Point3;
import org.siprop.bullet.util.ShapeType;
import org.siprop.bullet.util.Vector3;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.Gravity;

import static android.graphics.Color.YELLOW;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.webkit.WebView;

public class BulletSampleMain extends GVRMain {

    private static String TAG = "Bullet";

    private GVRContext mGVRContext = null;
    GVRScene scene = null;
    private Bullet mBullet = null;
    RigidBody sphereBody = null;
    Map<GVRSceneObject, Vector3f> objectMap = new HashMap<GVRSceneObject, Vector3f>();
    private Map<RigidBody, GVRSceneObject> rigidBodiesSceneMap = new HashMap<RigidBody, GVRSceneObject>();
    private static final float CYLINDER_MASS = 50.0f;
    private static final float SPHERE_MASS = 80.0f;
    float speed = 0.0f;
    Boolean applyForce = false;
    GVRCameraRig mainCameraRig = null;
    PhysicsWorld physicsWorld = null;
    MotionState boxState = null;
    Geometry boxGeometry = null;
    MotionState floorState = null;
    Geometry floorGeometry = null;
    RigidBody floorBody = null;
    MotionState sphereState = null;
    Geometry sphereGeometry = null;
    MotionState wallState = null;
    Geometry wallGeometry = null;
    MotionState sidewallState1 = null;
    Geometry sidewallGeometry1 = null;
    MotionState sidewallState2 = null;
    Geometry sidewallGeometry2 = null;
    GVRSceneObject sphereObject = null;
    GVRSceneObject groundScene = null;
    GVRSceneObject wallScene = null;
    GVRSceneObject sidewallScene1 = null;
    GVRSceneObject sidewallScene2 = null;
    Integer count = 0;
    Integer totalScore = 0;
    float minX = 0.25f;
    float maxX = 0.50f;
    float finalX = 0.50f;
    float angle = 2f;
    float aspeed = 2f;
    float delta = 2.0f;
    boolean cameraChanged = false;
    boolean audioPlayed = false;
    boolean clapPlayed = false;
    int left;
    int right;
    StaticPlaneShape floorShape, wallShape, sidewallShape1, sidewallShape2;
    // fake sphere
    GVRSceneObject sphereObjectFake = null;
    GVRTextViewSceneObject scoreDisplayObject = null;

    private GVRCameraSceneObject mCameraObject;
    private Camera mCamera;
    private boolean cameraDisplayed = false;
    private GVRAnimation mCameraAnim;
    GVRSceneObject mContainer;
    private GVRAnimationEngine mAnimationEngine;
    private GVRSceneObject mHeadContainer;

    // webview stuff
    public GVRViewSceneObject webViewObject;
    public GVRViewSceneObject webViewObject2;
    public GVRViewSceneObject webViewObject3;

    //video
    GVRVideoSceneObject videoObject;


    BulletSampleActivity mActivity;

    BulletSampleMain(BulletSampleActivity activity) {
        mActivity = activity;
    }


    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mGVRContext = gvrContext;
        scene = mGVRContext.getNextMainScene();
        mAnimationEngine = mGVRContext.getAnimationEngine();
        mContainer = new GVRSceneObject(gvrContext);
        mainCameraRig = scene.getMainCameraRig();
        mainCameraRig.getLeftCamera().setBackgroundColor(Color.BLACK);
        mainCameraRig.getRightCamera().setBackgroundColor(Color.BLACK);

        scoreDisplayObject = new GVRTextViewSceneObject(mGVRContext,50.0f,30.0f,null);
        scoreDisplayObject.getTransform().setPosition(0.0f,42.0f,-210.f);
        scoreDisplayObject.setGravity(Gravity.CENTER);
        scoreDisplayObject.setTextColor(YELLOW);
        scoreDisplayObject.setTextSize(10.0f);
        scoreDisplayObject.setRefreshFrequency(GVRTextViewSceneObject.IntervalFrequency.HIGH);
        scene.addSceneObject(scoreDisplayObject);

        /* Create the bowling room */
        GVRSceneObject bowlingRoom= meshWithTexture("room_export.fbx");
        bowlingRoom.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(bowlingRoom);
        //GVRDirectLight light = ((GVRDirectLight)(scene.getLightList()[0]));
        // light.setDiffuseIntensity(0.8f, 0.8f, 0.8f, 0.8f);
        // light.setAmbientIntensity(0.5f, 0.5f, 0.5f, 1f);

        // camera
        initCamera( );

        mHeadContainer = new GVRSceneObject(mGVRContext);
        mainCameraRig.addChildObject(mHeadContainer);
        addCamera(mHeadContainer);

        Log.v("", "addWeb pre");
        addWebViews();
        createPhysicsScene();
    }

    @Override
    public void onStep() {
        if (applyForce) {
            sphereObject.getTransform().setRotationByAxis(angle, 1f, 0f, 0f);
            angle += aspeed;

            mBullet.doSimulation(1.0f / 60.0f, 10);
            //mBullet.setActive(sphereBody, true);
            mBullet.setActivePhysicsWorldAll(physicsWorld.id, true);
            mBullet.applyCentralImpulse(sphereBody, new Vector3(0.0f, 0.0f, -(this.speed)));

            Random rand = new Random();
            finalX = rand.nextFloat() * (maxX - minX) + minX;
            if (left < 5)
                mBullet.applyCentralImpulse(sphereBody, new Vector3(finalX, 0.0f, -(this.speed)));
            else if (right < 5)
                mBullet.applyCentralImpulse(sphereBody, new Vector3(-finalX, 0.0f, -(this.speed)));
            else
                mBullet.applyCentralImpulse(sphereBody, new Vector3(0.0f, 0.0f, -(this.speed)));

            for (RigidBody body : rigidBodiesSceneMap.keySet()) {
                if (body.geometry.shape.getType() == ShapeType.SPHERE_SHAPE_PROXYTYPE
                        || body.geometry.shape.getType() == ShapeType.CYLINDER_SHAPE_PROXYTYPE) {
                    rigidBodiesSceneMap
                            .get(body)
                            .getTransform()
                            .setPosition(
                                    body.motionState.resultSimulation.originPoint.x,
                                    body.motionState.resultSimulation.originPoint.y,
                                    body.motionState.resultSimulation.originPoint.z);
                }
                if (body.geometry.shape.getType() == ShapeType.CYLINDER_SHAPE_PROXYTYPE) {
                    if (body.motionState.resultSimulation.originPoint.y < 1.9f) {
                        Quaternionf quaternion = new Quaternionf();
                        Float value = (Float) rigidBodiesSceneMap.get(body).getTag();
                        if(value == null) {
                            value = 20.0f * body.motionState.resultSimulation.originPoint.x;
                            rigidBodiesSceneMap.get(body).setTag(value);
                            rigidBodiesSceneMap.get(body).getTransform().setRotation(1.0f,0.0f,0.0f,0.0f);
                            rigidBodiesSceneMap.get(body).getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);
                            rigidBodiesSceneMap.get(body).getTransform().rotateByAxis(value, 0.0f, 1.0f, 0.0f);
                            count++;
                            totalScore++;

                            if (count == 10) {
                                scoreDisplayObject.setText("STRIKE!!!\n Total Score:" + totalScore +"\n\n Back Key to Play Again");
                                if (!audioPlayed || !clapPlayed) {
                                    AudioClip.getInstance(mGVRContext.getContext()).playSound(AudioClip.clapSoundID(),10,10);
                                    audioPlayed = true;
                                    clapPlayed = true;
                                }
                            }
                            else {
                                scoreDisplayObject.setText("SCORE:" + count + "\nTotal Score:" + totalScore + "\n\n Back Key to Play Again");
                                if (!audioPlayed) {
                                    AudioClip.getInstance(mGVRContext.getContext()).playSound(AudioClip.bowlingPinsHitSoundID(), 10, 10);
                                    audioPlayed = true;
                                }
                            }

                        }
                    }
                }
                if (!cameraChanged && body.geometry.shape.getType() == ShapeType.SPHERE_SHAPE_PROXYTYPE) {
                    if (body == sphereBody && (body.motionState.resultSimulation.originPoint.z < -105)) {
                        cameraChanged = true;
                        mainCameraRig.getTransform().setPosition(body.motionState.resultSimulation.originPoint.x, body.motionState.resultSimulation.originPoint.y+15, -105);
                    }
                }
            }
        }
    }

    private GVRSceneObject quadWithTexture(float width, float height,
            String texture) {
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                mGVRContext.createQuad(width, height));
        GVRSceneObject object = null;
        try {
            object = new GVRSceneObject(mGVRContext, futureMesh,
                    mGVRContext.loadFutureTexture(new GVRAndroidResource(
                            mGVRContext, texture)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private GVRSceneObject meshWithTexture(String mesh) {
        GVRSceneObject object = null;
        try {
           object = mGVRContext.loadModel(mesh);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void addCylinder(GVRScene scene, float x, float y, float z, float mass) {

        CylinderShape cylinderShape = new CylinderShape(new Vector3(0.629f,1.9f,0.629f));
        Geometry cylinderGeometry = mBullet.createGeometry(cylinderShape, mass, new Vector3(0.0f,0.0f,0.0f));
        MotionState cylinderState = new MotionState();
        cylinderState.worldTransform = new Transform(new Point3(x,y,z));
        RigidBody cylinderBody = mBullet.createAndAddRigidBody(cylinderGeometry,cylinderState);

        GVRSceneObject cylinderSceneObject = meshWithTexture("pin_export.fbx");

        cylinderSceneObject.getTransform().setPosition(x,y,z);

        scene.addSceneObject(cylinderSceneObject);
        rigidBodiesSceneMap.put(cylinderBody,cylinderSceneObject);

    }

    /*
     * Function to add a sphere of dimension and position specified in the
     * Bullet physics world and scene graph
     */
    private void addSphere(GVRScene scene, float radius, float x, float y,
            float z, float mass) {
        SphereShape sphereShape = new SphereShape(radius);
        sphereGeometry = mBullet.createGeometry(sphereShape, mass,
                new Vector3(0.0f, 0.0f, 0.0f));
        sphereState = new MotionState();
        sphereState.worldTransform = new Transform(new Point3(x, y, z));
        sphereBody = mBullet.createAndAddRigidBody(sphereGeometry,
                sphereState);

        sphereObject = meshWithTexture("ball_export.fbx");

        sphereObject.getTransform().setPosition(x, y, z);

        //mBullet.setActive(sphereBody, true);
        objectMap.put(sphereObject, new Vector3f(x,y,z));
        scene.addSceneObject(sphereObject);
        rigidBodiesSceneMap.put(sphereBody, sphereObject);
    }

    public void addDisplaySphere(GVRScene scene, float radius, float x, float y, float z, float mass)  {
        sphereObjectFake = meshWithTexture("ball_export.fbx");
        sphereObjectFake.getTransform().setPosition(x, y, z);
        scene.addSceneObject(sphereObjectFake);
    }


    private GVRViewSceneObject createWebViewObject(GVRContext gvrContext, float w, float h, GVRView webView) {
        //GVRView webView = mActivity.getWebView();
        GVRViewSceneObject webObject = new GVRViewSceneObject(gvrContext,
                webView, w, h);
        //webObject.setName("web view object");
        webObject.getRenderData().getMaterial().setOpacity(1.0f);

        return webObject;
    }


    public void onSwipe(float speed) {

        if(!applyForce) {
            if (Math.abs(speed) >= 4500)
                this.speed = 50;
            else if (Math.abs(speed) >= 4000)
                this.speed = 45;
            else if (Math.abs(speed) >= 3500)
                this.speed = 40;
            else if (Math.abs(speed) >= 3000)
                this.speed = 35;
            else if (Math.abs(speed) >= 2500)
                this.speed = 30;
            else if (Math.abs(speed) >= 2000)
                this.speed = 25;
            else if (Math.abs(speed) >= 1500)
                this.speed = 20;
            else if (Math.abs(speed) >= 1000)
                this.speed = 10;
            else
                this.speed = 5;

            //sphereObjectFake.getRenderData().setRenderMask(0);
            scene.removeSceneObject(sphereObjectFake);
            applyForce = true;
            addSphere(scene, 1.32f, sphereObjectFake.getTransform().getPositionX(),
                    sphereObjectFake.getTransform().getPositionY(),
                    sphereObjectFake.getTransform().getPositionZ(), SPHERE_MASS);
            if (cameraDisplayed) {
                mCameraObject.getRenderData().getMaterial().setOpacity( 0.0f );
                cameraDisplayed = false;
            }
        }

    }
    public void onSwipe2(int dir) {
        if (dir > 0) {
            togglePassthroughCamera(false);
        } else {
            togglePassthroughCamera(true);
        }
    }

    public void onRealTap() {
        //videoObject.getMediaPlayer().start();
        // crashing when try to play :(

        clickWebview();
    }

    public void clickWebview() {
        MyWebView webView = (MyWebView) mActivity.getWebView(0);

        final long uMillis = SystemClock.uptimeMillis();

        float x = 512;
        float y = 512;

        boolean val = true;

        webView.isTouchable(val);

        webView.dispatchTouchEvent(MotionEvent.obtain(uMillis, uMillis,
                MotionEvent.ACTION_DOWN, x, y, 0));
        webView.dispatchTouchEvent(MotionEvent.obtain(uMillis, uMillis,
                MotionEvent.ACTION_UP, x, y, 0));

        webView.isTouchable(!val);
    }

    public void onTap() {
        applyForce = false;
        //scene.removeSceneObject(mContainer);
        //scene.clear();
        scene.removeSceneObject(groundScene);
        scene.removeSceneObject(wallScene);
        scene.removeSceneObject(sidewallScene1);
        scene.removeSceneObject(sidewallScene2);
        scene.removeSceneObject(sphereObjectFake);

        for (RigidBody body : rigidBodiesSceneMap.keySet()) {
            if (body.geometry.shape.getType() == ShapeType.SPHERE_SHAPE_PROXYTYPE
                    || body.geometry.shape.getType() == ShapeType.CYLINDER_SHAPE_PROXYTYPE) {
                scene.removeSceneObject(rigidBodiesSceneMap.get(body));
            }
        }
        createPhysicsScene();
    }

    public void createPhysicsScene() {
        left = 5;
        right = 5;
        count = 0;
        count = 0;
        
        mainCameraRig.disable();
        mainCameraRig.enable();
        mainCameraRig.getTransform().setPosition(0.0f, 15.0f, 15.0f);
        cameraChanged = false;
        audioPlayed = false;
        clapPlayed = false;

        mBullet = new Bullet();
        /*
         * Create the physics world.
         */
        physicsWorld = mBullet.createPhysicsWorld(new Vector3(-7.50f, -7.50f, -7.50f),
                new Vector3(7.5f, 7.5f, 7.5f), 1024, new Vector3(0.0f,
                        -9.8f, 0.0f));

        /*
         * Create the ground. A simple textured quad. In bullet it will be a
         * plane shape with 0 mass
         */
        groundScene = meshWithTexture("floor.fbx");
        //groundScene.getTransform().setRotationByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
        groundScene.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(groundScene);

        StaticPlaneShape floorShape = new StaticPlaneShape(new Vector3(0.0f, 1.0f, 0.0f), 0.0f);
        floorGeometry = mBullet.createGeometry(floorShape, 0.0f, new Vector3(0.0f, 0.0f, 0.0f));
        floorState = new MotionState();
        floorBody = mBullet.createAndAddRigidBody(floorGeometry, floorState);

        wallScene = meshWithTexture("wall.fbx");
        //wallScene.getTransform().setRotationByAxis(-0.0f, 1.0f, 0.0f, 0.0f);
        wallScene.getTransform().setPosition(0.0f, 0.0f, -210.0f);
        scene.addSceneObject(wallScene);

        scoreDisplayObject.setText("Vol Key to adjust Ball\nForward Swipe to Throw\nSwipe Speed -> Ball Speed\nSwipe Down -> See Through");

        wallShape = new StaticPlaneShape(new Vector3(0.0f, 0.0f, 1.0f), 0.0f);
        wallGeometry = mBullet.createGeometry(wallShape, 0.0f, new Vector3(0.0f, 0.0f, 0.0f));
        wallState = new MotionState();
        wallState.worldTransform = new Transform(new Point3(0.0f, 0.0f, -210.0f));
        mBullet.createAndAddRigidBody(wallGeometry, wallState);

        sidewallScene1 = meshWithTexture("sidewall1.fbx");
        //wallScene.getTransform().setRotationByAxis(-0.0f, 1.0f, 0.0f, 0.0f);
        sidewallScene1.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(sidewallScene1);

        sidewallShape1 = new StaticPlaneShape(new Vector3(1.0f, 0.0f, 0.0f), 0.0f);
        sidewallGeometry1 = mBullet.createGeometry(sidewallShape1, 0.0f, new Vector3(0.0f, 0.0f, 0.0f));
        sidewallState1 = new MotionState();
        sidewallState1.worldTransform = new Transform(new Point3(-7.5f, 0.0f, 0.0f));
        mBullet.createAndAddRigidBody(sidewallGeometry1, sidewallState1);

        sidewallScene2 = meshWithTexture("sidewall2.fbx");
        //wallScene.getTransform().setRotationByAxis(-0.0f, 1.0f, 0.0f, 0.0f);
        sidewallScene2.getTransform().setPosition(0.0f, 0.0f, 0.0f);
        scene.addSceneObject(sidewallScene2);

        sidewallShape2 = new StaticPlaneShape(new Vector3(-1.0f, 0.0f, 0.0f), 0.0f);
        sidewallGeometry2 = mBullet.createGeometry(sidewallShape2, 0.0f, new Vector3(0.0f, 0.0f, 0.0f));
        sidewallState2 = new MotionState();
        sidewallState2.worldTransform = new Transform(new Point3(7.5f, 0.0f, 0.0f));
        mBullet.createAndAddRigidBody(sidewallGeometry2, sidewallState2);

        /* Create 10 pins in 4,3,2,1 order */
        addCylinder(scene,-2.0f,1.9f,-200.0f, CYLINDER_MASS);
        addCylinder(scene,0.0f,1.9f,-200.0f, CYLINDER_MASS);
        addCylinder(scene,2.0f,1.9f,-200.0f, CYLINDER_MASS);
        addCylinder(scene,4.0f,1.9f,-200.0f, CYLINDER_MASS);

        addCylinder(scene,-1.0f,1.9f,-197.0f, CYLINDER_MASS);
        addCylinder(scene,1.0f,1.9f,-197.0f, CYLINDER_MASS);
        addCylinder(scene,3.0f,1.9f,-197.0f, CYLINDER_MASS);

        addCylinder(scene,0.0f,1.9f,-194.0f, CYLINDER_MASS);
        addCylinder(scene,2.0f,1.9f,-194.0f, CYLINDER_MASS);

        addCylinder(scene,1.0f,1.9f,-191.0f, CYLINDER_MASS);

        // for now just the fake sphere
        addDisplaySphere(scene, 1.0f, 0.0f, 2.0f, 2.0f, SPHERE_MASS);
        //addSphere(scene, 1.0f, 0.0f, 3.0f, 2.0f, 20.0f);
    }

    public void addWebViews() {
        float angle = 45f;
        GVRView webView = mActivity.getWebView(2);
        webViewObject = createWebViewObject(mGVRContext, 25f, 25f, webView);
        webViewObject.getTransform().setPosition( -23f, 19f, -15f );
        webViewObject.getTransform().setRotationByAxis( angle,  0.0f, 1.0f, 0.0f );

        webViewObject.getRenderData().getMaterial().setOpacity( 0.6f );
        webViewObject.getRenderData().setDepthTest(false);
        webViewObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);

        scene.addSceneObject(webViewObject);

        Log.v("", "addWebView");

        GVRView webView2 = mActivity.getWebView(1);
        webViewObject2 = createWebViewObject(mGVRContext, 25f, 25f, webView2);
        webViewObject2.getTransform().setPosition( 30f, 19f, 24f );
        webViewObject2.getTransform().setRotationByAxis( -90,  0.0f, 1.0f, 0.0f );
        scene.addSceneObject(webViewObject2);

        GVRView webView3 = mActivity.getWebView(0);
        webViewObject3 = createWebViewObject(mGVRContext, 25f, 25f, webView3);
        webViewObject3.getTransform().setPosition( -40f, 25f, 25f );
        webViewObject3.getTransform().setRotationByAxis( 90,  0.0f, 1.0f, 0.0f );
        scene.addSceneObject(webViewObject3);


    }

    public void moveLeft() {
        if (!applyForce) {
            if (left > 0) {
                sphereObjectFake.getTransform().setPosition(sphereObjectFake.getTransform().getPositionX() - 1,
                        sphereObjectFake.getTransform().getPositionY(),
                        sphereObjectFake.getTransform().getPositionZ());
                left--;
                right++;
            }
        }
    }

    public void moveRight() {
        if (!applyForce) {
            if (right > 0) {
                sphereObjectFake.getTransform().setPosition(sphereObjectFake.getTransform().getPositionX() + 1,
                        sphereObjectFake.getTransform().getPositionY(),
                        sphereObjectFake.getTransform().getPositionZ());
                right--;
                left++;
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void initCamera() {
        mCamera = Camera.open();

        mCamera.startPreview();

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);

        Camera.Size size = parameters.getPreferredPreviewSizeForVideo();

        parameters.setPreviewSize(size.width, size.height);

        Log.v(TAG, "Camera size: " + size.width + "," + size.height);

        mCamera.setParameters(parameters);
    }

    public void addCamera(GVRSceneObject container) {
        float ratio = 16f / 9f; // size.width / size.height;

        float H = 1.0f;
        float W = H * ratio;

        mCameraObject = new GVRCameraSceneObject(mGVRContext,
                W, H, mCamera);
        mCameraObject.getTransform().setPosition( 0f, 0f, -1.3f );
        mCameraObject.getRenderData().getMaterial().setOpacity( 0.0f );
        mCameraObject.getRenderData().setDepthTest(false);
        mCameraObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        //mCameraObject.getTransform().rotateByAxis( -90f, 0.0f, 1.0f, 0.0f );

        container.addChildObject(mCameraObject);
    }

    public void togglePassthroughCamera(boolean active) {
        if (cameraDisplayed == active)
            return;

        float ANIMATION_DURATION = 0.6f; // secs

        mCameraAnim = new GVROpacityAnimation(mCameraObject, ANIMATION_DURATION,
                active ? 1.0f : 0.0f );

        mCameraAnim.start(mAnimationEngine);

        cameraDisplayed = active;
    }

    private GVRVideoSceneObject createVideoObject(GVRContext gvrContext) throws IOException {
        final AssetFileDescriptor afd = gvrContext.getActivity().getAssets().openFd("tron.mp4");
        final MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        mediaPlayer.prepare();
        GVRVideoSceneObject video = new GVRVideoSceneObject(gvrContext, 8.0f,
                4.0f, mediaPlayer, GVRVideoSceneObject.GVRVideoType.MONO);
        video.setName("video");
        return video;
    }
}
