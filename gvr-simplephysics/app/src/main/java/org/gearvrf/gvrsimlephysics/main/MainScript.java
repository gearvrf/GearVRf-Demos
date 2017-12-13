package org.gearvrf.gvrsimlephysics.main;

import android.graphics.Color;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRTransform;
import org.gearvrf.IActivityEvents;
import org.gearvrf.IPickEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.gvrsimlephysics.R;
import org.gearvrf.gvrsimlephysics.entity.Countdown;
import org.gearvrf.gvrsimlephysics.util.MathUtils;
import org.gearvrf.gvrsimlephysics.util.VRTouchPadGestureDetector;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

public class MainScript extends GVRMain implements GVRSceneObject.ComponentVisitor {

    private final int MAX_BALLS = 40;
    private int SCORE_OFFSET = -50;
    private GVRScene mScene;
    private GVRCameraRig mCamera;
    private int mTimeTicker = 0;
    private int mScore = 0;
    private int mNumBalls = 0;
    private int mNumCylinders = 0;
    private GVRTextViewSceneObject mScoreLabel;
    private GVRTextViewSceneObject mBallsLabel;
    private GVRTextViewSceneObject mEndGameLabel;
    private Countdown mCountDown;
    private GVRCursorController mController;
    private GVRSceneObject mCursor;
    private GVRContext mContext;


    private GVRCursorController.ControllerEventListener mControllerThrowHandler = new GVRCursorController.ControllerEventListener()
    {
        private Vector3f mStartDrag = new Vector3f(0, 0, 0);
        private Vector3f mEndDrag = new Vector3f(0, 0, 0);
        private Vector3f mTempDir = new Vector3f();
        private GVRSceneObject mCurrentBall = null;

        public void onEvent(GVRCursorController controller, boolean touched)
        {
            MotionEvent event = controller.getMotionEvent();
            if (event == null)
            {
                return;
            }
            int action = event.getAction();
            if ((mCurrentBall == null) && (action == MotionEvent.ACTION_DOWN))
            {
                mCurrentBall = newBall();
                GVRRigidBody rigidBody = (GVRRigidBody) mCurrentBall.getComponent(GVRRigidBody.getComponentType());
                rigidBody.setEnable(false);
                controller.getCursor().addChildObject(mCurrentBall);
                controller.getPicker().getWorldPickRay(mStartDrag, mTempDir);
            }
            else if ((event.getAction() == MotionEvent.ACTION_UP) && (mCurrentBall != null))
            {
                float dt = SystemClock.uptimeMillis() - event.getDownTime();
                GVRTransform ballTrans = mCurrentBall.getTransform();
                GVRRigidBody rigidBody = (GVRRigidBody) mCurrentBall.getComponent(GVRRigidBody.getComponentType());
                Matrix4f ballMtx = ballTrans.getModelMatrix4f();

                mCurrentBall.getParent().removeChildObject(mCurrentBall);
                ballTrans.setModelMatrix(ballMtx);
                controller.getPicker().getWorldPickRay(mEndDrag, mTempDir);
                mEndDrag.sub(mStartDrag, mTempDir);
                mTempDir.mul(1000000.0f / dt);
                rigidBody.applyCentralForce(mTempDir.x, mTempDir.y, mTempDir.z * 4.0f);
                rigidBody.setEnable(true);
                mScene.addSceneObject(mCurrentBall);
                mCurrentBall = null;
            }
        }
    };

    /*
     * Handles initializing the selected controller:
     * - attach the scene object to represent the4 cursor
     * - set cursor properties
     * If we are using the Gaze controller, it does not generate touch events directly.
     * We need to listen for them from GVRActivity to process them with a gesture detector.
     */
    private GVRInputManager.ICursorControllerSelectListener mControllerSelector = new GVRInputManager.ICursorControllerSelectListener()
    {
        public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
        {
            if (oldController != null)
            {
                if (oldController.getControllerType() == GVRControllerType.CONTROLLER)
                {
                    oldController.addControllerEventListener(mControllerThrowHandler);
                }
            }
            mController = newController;
            if (newController.getControllerType() == GVRControllerType.CONTROLLER)
            {
                newController.addControllerEventListener(mControllerThrowHandler);
            }
            newController.setCursor(mCursor);
            newController.setCursorDepth(6.0f);
            newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
        }
    };

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mScene = gvrContext.getMainScene();
        mContext = gvrContext;
        mCamera = mScene.getMainCameraRig();
        mCursor = MainHelper.createGaze(gvrContext, 0.0f, 0.0f, 0.0f);
        initCamera(gvrContext, mCamera);

        initScene(gvrContext, mScene);

        initLabels(gvrContext, mScene);

        addPhysicsWorld(gvrContext, mScene);

        mScene.getEventReceiver().addListener(this);
        gvrContext.getInputManager().selectController(mControllerSelector);
    }

    private static void initCamera(GVRContext context, GVRCameraRig camera) {
        float intensity = 1.0f;
        camera.getLeftCamera().setBackgroundColor(1.0f * intensity, 0.956f * intensity, 0.84f * intensity, 1f);
        camera.getRightCamera().setBackgroundColor(1.0f * intensity, 0.956f * intensity, 0.84f * intensity, 1f);
        camera.getTransform().setPosition(0.0f, 6.0f, 20f);
    }

    private void initScene(GVRContext context, GVRScene scene) {
        if (!GVRShader.isVulkanInstance())
        {
            addLights(context, scene);
        }
        addGround(context, scene);
        addCylinderGroup(context, scene);
    }

    private void addTimer(GVRContext context, GVRScene scene) {
        GVRTextViewSceneObject label = MainHelper.createLabel(context, 6f, 9f, -5f);
        mCountDown = new Countdown(label);

        scene.addSceneObject(label);

        mCountDown.start(context);
    }

    private void initLabels(GVRContext context, GVRScene scene) {
        mEndGameLabel = null;
        mScoreLabel = MainHelper.createLabel(context, 0f, 9f, -5f);
        mBallsLabel = MainHelper.createLabel(context, -6f, 9f, -5f);

        mScoreLabel.setText("Score: 0");
        mBallsLabel.setText("Balls: " + MAX_BALLS );

        scene.addSceneObject(mScoreLabel);
        scene.addSceneObject(mBallsLabel);

        addTimer(context, scene);
    }

    private static void addPhysicsWorld(GVRContext context, GVRScene scene) {
        scene.getRoot().attachComponent(new GVRWorld(context, MainHelper.collisionMatrix));
    }

    private static void addLights(GVRContext context, GVRScene scene) {
        GVRSceneObject centerLight = MainHelper.createDirectLight(context, 0.0f, 10.0f, 2.0f);
        GVRSceneObject leftPointLight = MainHelper.createPointLight(context, -10.0f, 5.0f, 20.0f);
        GVRSceneObject rightPointLight = MainHelper.createPointLight(context, 10.0f, 5.0f, 20.0f);

        centerLight.getTransform().rotateByAxis(-90, 1, 0, 0);

        scene.addSceneObject(centerLight);
        scene.addSceneObject(leftPointLight);
        scene.addSceneObject(rightPointLight);
    }

    private static void addGround(GVRContext context, GVRScene scene) {
        scene.addSceneObject(MainHelper.createGround(context, 0.0f, 0.0f, 0.0f));
    }

    private void addCylinderGroup(GVRContext context, GVRScene scene) {
        final int[] CYLINDER_COLORS = {R.drawable.black, R.drawable.brown,
                R.drawable.green, R.drawable.grey, R.drawable.orange, R.drawable.pink,
                R.drawable.red, R.drawable.yellow, R.drawable.light_blue, R.drawable.light_green,
                R.drawable.dark_blue, R.drawable.cy};

        final int SQUARE_SIZE = 3;
        float offset = 0;
        try {
            for (int y = 0; y < SQUARE_SIZE; y++) {
                for (int x = 0; x < SQUARE_SIZE - y; x++) {
                    for (int z = 0; z < SQUARE_SIZE; z++) {
                        addCylinder(context, scene, (x - (SQUARE_SIZE / 2.0f)) * 2.5f + 1.5f + offset,
                                1f + (y * 1.2f), (z + (SQUARE_SIZE / 2.0f)) * 2.5f - 5f,
                                CYLINDER_COLORS[mNumCylinders++ % CYLINDER_COLORS.length]);
                    }
                }
                offset += 1.25f;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static void addCylinder(GVRContext context, GVRScene scene, float x, float y, float z,
                                    int drawable) throws IOException {
        scene.addSceneObject(MainHelper.createCylinder(context, x, y, z, drawable));
    }

    public void onSwipe(VRTouchPadGestureDetector.SwipeDirection swipeDirection, float velocityX) {

        if (gameStopped() || (mController.getControllerType() == GVRControllerType.CONTROLLER)) {
            return;
        }
        int normal = MathUtils.calculateForce(velocityX);
        float[] forward = MathUtils.calculateRotation( mCamera.getHeadTransform().getRotationPitch(), mCamera.getHeadTransform().getRotationYaw());
        float[] force = {normal * forward[0], normal * forward[1], normal * forward[2]};

        try {
            GVRTransform trans = mCamera.getTransform();
            GVRSceneObject ball = MainHelper.createBall(getGVRContext(),
                    5 * forward[0] + trans.getPositionX(),
                    5 * forward[1] + trans.getPositionY(),
                    5 * forward[2] + trans.getPositionZ(), force);

            mScene.addSceneObject(ball);
            mNumBalls++;

            mBallsLabel.setText("Balls: " + (MAX_BALLS - mNumBalls));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    private GVRSceneObject newBall()
    {
        try
        {
            GVRSceneObject ball = MainHelper.createBall(getGVRContext(),
                   0,0,0, new float[] { 0, 0, 0 });
            mNumBalls++;
            mBallsLabel.setText("Balls: " + (MAX_BALLS - mNumBalls));
            return ball;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return null;
    }

    @Override
    public void onStep() {
        if (gameFinished()) return;

        mTimeTicker = mTimeTicker++ % 120;
        if (mTimeTicker != 0) {
            return;
        }

        if (gameStopped()) {
            // Score
            SCORE_OFFSET = -1;
            mScene.getRoot().forAllComponents(this, GVRRigidBody.getComponentType());

            // Show finished message.
            if (mScore == mNumCylinders) {
                mEndGameLabel = new GVRTextViewSceneObject(getGVRContext(),
                        18, 4f, "Congratulations, you won!");
            } else if (mCountDown.isFinished()){

                mEndGameLabel = new GVRTextViewSceneObject(getGVRContext(),
                        18, 4f, "Time out! Try again.");
            } else {
                mEndGameLabel = new GVRTextViewSceneObject(getGVRContext(),
                        18, 4f, "No shots left! Try again.");
            }

            mEndGameLabel.setTextSize(10);
            mEndGameLabel.setBackgroundColor(Color.DKGRAY);
            mEndGameLabel.setTextColor(Color.WHITE);
            mEndGameLabel.setGravity(Gravity.CENTER);
            mEndGameLabel.getTransform().setPosition(0f, 9f, -4f);

            mScene.addSceneObject(mEndGameLabel);
        } else {
            // Score
            mScene.getRoot().forAllComponents(this, GVRRigidBody.getComponentType());
        }

    }

    @Override
    public boolean visit(GVRComponent gvrComponent) {
        if (gvrComponent.getTransform().getPositionY() < SCORE_OFFSET) {
            mScene.removeSceneObject(gvrComponent.getOwnerObject());
            doScore((GVRRigidBody) gvrComponent);
        }

        return false;
    }

    private void doScore(GVRRigidBody body) {
        if (body.getCollisionGroup() != MainHelper.COLLISION_GROUP_CYLINDER) {
            return;
        }

        mScore ++;
        mScoreLabel.setText("Score: " + mScore);
    }

    private boolean gameFinished() {
        return mEndGameLabel != null;
    }

    private boolean gameStopped() {
        return  mNumBalls == MAX_BALLS || mCountDown.isFinished() || mScore == mNumCylinders;
    }
}
