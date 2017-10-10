package org.gearvrf.gvrsimlephysics.main;

import android.graphics.Color;
import android.view.Gravity;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShader;
import org.gearvrf.gvrsimlephysics.R;
import org.gearvrf.gvrsimlephysics.entity.Countdown;
import org.gearvrf.gvrsimlephysics.util.MathUtils;
import org.gearvrf.gvrsimlephysics.util.VRTouchPadGestureDetector;
import org.gearvrf.physics.GVRRigidBody;
import org.gearvrf.physics.GVRWorld;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

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

    @Override
    public void onInit(GVRContext gvrContext) throws Throwable {
        mScene = gvrContext.getMainScene();
        mCamera = mScene.getMainCameraRig();

        initCamera(gvrContext, mCamera);

        initScene(gvrContext, mScene);

        initLabels(gvrContext, mScene);

        addPhysicsWorld(gvrContext, mScene);

        mScene.getEventReceiver().addListener(this);
    }

    private static void initCamera(GVRContext context, GVRCameraRig camera) {
        float intensity = 1.0f;
        camera.getLeftCamera().setBackgroundColor(1.0f * intensity, 0.956f * intensity, 0.84f * intensity, 1f);
        camera.getRightCamera().setBackgroundColor(1.0f * intensity, 0.956f * intensity, 0.84f * intensity, 1f);
        camera.getTransform().setPosition(0.0f, 6.0f, 20f);

        addGaze(context, camera);
    }

    private void initScene(GVRContext context, GVRScene scene) {
        if (!GVRShader.isVulkanInstance())
        {
            addLights(context, scene);
        }
        addGround(context, scene);
        addCylinderGroup(context, scene);
    }

    private static void addGaze(GVRContext context, GVRCameraRig camera) {
        camera.addChildObject(MainHelper.createGaze(context, 0.0f, 0.0f, -1.0f));
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

        if (swipeDirection != VRTouchPadGestureDetector.SwipeDirection.Forward
                || gameStopped()) {
            return;
        }

        int normal = MathUtils.calculateForce(velocityX);
        float[] forward = MathUtils.calculateRotation(mCamera.getHeadTransform()
                .getRotationPitch(), mCamera.getHeadTransform().getRotationYaw());
        float[] force = {normal * forward[0], normal * forward[1], normal * forward[2]};

        try {
            GVRSceneObject ball = MainHelper.createBall(getGVRContext(),
                    5 * forward[0] + mCamera.getTransform().getPositionX(),
                    5 * forward[1] + mCamera.getTransform().getPositionY(),
                    5 * forward[2] + mCamera.getTransform().getPositionZ(), force);

            mScene.addSceneObject(ball);
            mNumBalls++;

            mBallsLabel.setText("Balls: " + (MAX_BALLS - mNumBalls));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
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
