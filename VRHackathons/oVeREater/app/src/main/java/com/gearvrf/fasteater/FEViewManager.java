package com.gearvrf.fasteater;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.ZipLoader;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRRelativeMotionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;
import org.gearvrf.utility.Log;
import org.siprop.bullet.Bullet;
import org.siprop.bullet.Geometry;
import org.siprop.bullet.MotionState;
import org.siprop.bullet.RigidBody;
import org.siprop.bullet.Transform;
import org.siprop.bullet.shape.BoxShape;
import org.siprop.bullet.shape.StaticPlaneShape;
import org.siprop.bullet.util.Point3;
import org.siprop.bullet.util.Vector3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

public class FEViewManager extends GVRScript {
	private static final String TAG = Log.tag(FEViewManager.class);
	private GVRAnimationEngine mAnimationEngine;
	private GVRScene mMainScene;
	private GVRContext mGVRContext;
	private GVRSceneObject mainSceneObject, headTracker, astronautMeshObject;
	private GVRTextViewSceneObject textMessageObject, scoreTextMessageObject, livesTextMessageObject, tapTOStart;
	private GVRSceneObject burger;
	private List<FlyingItem> mObjects = new ArrayList<FlyingItem>();
    private Bullet mBullet = null;
    private static final float OBJECT_MASS = 0.5f;
    private RigidBody boxBody;
    private Boolean gameStart = false;
    private Map<RigidBody, GVRSceneObject> rigidBodiesSceneMap = new HashMap<RigidBody, GVRSceneObject>();
    private Timer timer;
    private GameStateMachine gameState;
    private GVRSceneObject homeButton, pauseButton, timerButton;
    private Player ovrEater;
    private Boolean isBGAudioOnce = false;

	private GVRSceneObject asyncSceneObject(GVRContext context, String meshName, String textureName)
			throws IOException {
		return new GVRSceneObject(context, //
				new GVRAndroidResource(context, meshName), new GVRAndroidResource(context, textureName));
	}

	@Override
	public void onInit(GVRContext gvrContext) throws IOException, InterruptedException {
        gameState = new GameStateMachine();
		mGVRContext = gvrContext;
		mAnimationEngine = mGVRContext.getAnimationEngine();
		mMainScene = mGVRContext.getNextMainScene();
		mMainScene.setFrustumCulling(true);

        loadGameScene(mGVRContext, mMainScene);
	}

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {
        Bitmap bitmap = BitmapFactory.decodeResource(
                gvrContext.getContext().getResources(),
                R.drawable.boot_screen);
        //Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, 2, 2, true);
        return new GVRBitmapTexture(gvrContext, bitmap);
    }

    private void loadGameScene(GVRContext context, GVRScene scene) throws IOException {
        gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS);
        // load all audio files. TODO: change this to spacial Audio
        AudioClip.getInstance(context.getContext());

        ovrEater = new Player();

        mainSceneObject = new GVRSceneObject(context);
        mMainScene.addSceneObject(mainSceneObject);
        mMainScene.getMainCameraRig().getTransform().setPosition(0.0f, 6.0f, 8.0f);

        GVRMesh mesh = context.loadMesh(new GVRAndroidResource(context,
                "space_sphere.obj"));

        GVRSceneObject leftScreen = new GVRSceneObject(context, mesh,
                context.loadTexture(new GVRAndroidResource(context,
                        "city_domemap_left.png")));
        leftScreen.getTransform().setScale(200,200,200);
        GVRSceneObject rightScreen = new GVRSceneObject(context, mesh,
                context.loadTexture(new GVRAndroidResource(context,
                        "city_domemap_right.png")));
        rightScreen.getTransform().setScale(200,200,200);

        mainSceneObject.addChildObject(leftScreen);
        mainSceneObject.addChildObject(rightScreen);

        tapTOStart = setInfoMessage("Tap to start");
        mainSceneObject.addChildObject(tapTOStart);

    }

    private GVRTextViewSceneObject setInfoMessage(String str)
    {
        GVRTextViewSceneObject textMessageObject = new GVRTextViewSceneObject(mGVRContext, 4, 4, str);
        textMessageObject.setTextColor(Color.YELLOW);
        textMessageObject.setGravity(Gravity.CENTER);
        textMessageObject.setKeepWrapper(true);
        textMessageObject.setTextSize(15);
        textMessageObject.setBackgroundColor(Color.TRANSPARENT);
        textMessageObject.setRefreshFrequency(IntervalFrequency.HIGH);
        textMessageObject.getTransform().setPosition(-2.0f, 6.0f, -6.0f);
        textMessageObject.getTransform().rotateByAxisWithPivot(0, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f);

        GVRRenderData renderData = textMessageObject.getRenderData();
        renderData.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
        renderData.setDepthTest(false);

        return textMessageObject;
    }

    private GVRTextViewSceneObject makeScoreboard(GVRContext ctx, GVRSceneObject parent)
    {
        GVRTextViewSceneObject scoreBoard = new GVRTextViewSceneObject(ctx, 2.0f, 1.5f, "000");

        GVRRenderData rdata = scoreBoard.getRenderData();
        GVRCollider collider = new GVRMeshCollider(ctx, true);

        collider.setEnable(false);
        scoreBoard.attachComponent(collider);
        scoreBoard.setTextColor(Color.YELLOW);
        scoreBoard.setTextSize(6);
        scoreBoard.setBackgroundColor(Color.argb(0, 0, 0, 0));
        scoreBoard.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(GVRRenderingOrder.OVERLAY);
        parent.addChildObject(scoreBoard);
        return scoreBoard;
    }

    private GVRTextViewSceneObject makeLivesLeft(GVRContext ctx, GVRSceneObject parent)
    {
        GVRTextViewSceneObject livesLeft = new GVRTextViewSceneObject(ctx, 5.3f, 1.5f, "Lives: 3");
        livesLeft.setTextSize(6);
        GVRRenderData rdata = livesLeft.getRenderData();
        GVRCollider collider = new GVRMeshCollider(ctx, true);

        collider.setEnable(false);
        livesLeft.attachComponent(collider);
        livesLeft.setTextColor(Color.YELLOW);
        livesLeft.setBackgroundColor(Color.argb(0, 0, 0, 0));
        livesLeft.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(GVRRenderingOrder.OVERLAY);
        parent.addChildObject(livesLeft);
        return livesLeft;
    }

	private GVRSceneObject quadWithTexture(float width, float height, String texture) {
		FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(mGVRContext.createQuad(width, height));
		GVRSceneObject object = null;
		try {
			object = new GVRSceneObject(mGVRContext, futureMesh,
					mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, texture)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}

    private int MAX_THROW = 15;

    private void _throwObject()
    {
        Timer timer = new Timer();
        TimerTask task = new TimerTask()
        {
            public void run() {
                try {
                    int num_throw = Helper.randomNextInt(MAX_THROW);
                    for(int i = 0; i < num_throw; i++) {
                        throwAnObject();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        int THROW_OBJECT_RATE_MIN = 1 * 1000;
        int THROW_OBJECT_RATE_MAX = 4 * 1000;
        int THROW_OBJECT_DELAY_MIN = 1 * 1000;
        int THROW_OBJECT_DELAY_MAX = 4 * 1000;
        timer.scheduleAtFixedRate(task,
                Helper.randomInRange(THROW_OBJECT_DELAY_MIN, THROW_OBJECT_DELAY_MAX),
                Helper.randomInRange(THROW_OBJECT_RATE_MIN, THROW_OBJECT_RATE_MAX));

        //timeElapsed = System.currentTimeMillis();
    }

    private int MIN_GAME_WIDTH = -10;
    private int MAX_GAME_WIDTH = 10;
    private int MIN_GAME_HEIGHT_START = 5;
    private int MAX_GAME_HEIGHT_START = 7;
    private int MIN_GAME_HEIGHT_REACH = 5;
    private int MAX_GAME_HEIGHT_REACH = 7;
    private int MIN_SPEED = 2;
    private int MAX_SPEED = 0;

    private String[][] OverEatObjects = new String[][]{
            { "hotdog.obj", "hotdog.png", "hotdog" },
            { "hamburger.obj", "hamburger.png", "hamburger" },
            { "bomb.obj", "bomb.png", "bomb" },
            { "sodacan.obj", "sodacan.png", "sodacan" }
    };

    public void throwAnObject() throws IOException {
        if(!ovrEater.isDead()) {
            int rand_index = Helper.randomNextInt(OverEatObjects.length);
            GVRSceneObject object = asyncSceneObject(mGVRContext, OverEatObjects[rand_index][0], OverEatObjects[rand_index][1]);
            FlyingItem item = new FlyingItem(OverEatObjects[rand_index][2], object);
            object.getTransform().setPosition(
                    Helper.randomInRangeFloat(MIN_GAME_WIDTH, MAX_GAME_WIDTH),
                    Helper.randomInRangeFloat(MIN_GAME_HEIGHT_START, MAX_GAME_HEIGHT_START),
                    -20);
            mainSceneObject.addChildObject(object);
            mObjects.add(item);

            relativeMotionAnimation(object,
                    Helper.randomInRange(MIN_SPEED, MAX_SPEED),
                    0,
                    0,
                    -(object.getTransform().getPositionZ() - 10));
        }
    }

	@Override
	public void onStep() {
        if(ovrEater.isDead() && gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS) {
            playerDead();
        } else if(gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS) {
            for (int i = 0; i < mObjects.size(); i++) {
                try {
                    headTracker.getRenderData().getMaterial().setMainTexture(
                            mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "mouth_open.png")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mObjects.get(i) != null && mObjects.get(i).getSceneObject().getRenderData().getMesh() != null) {
                    if (mObjects.get(i).getSceneObject().isColliding(headTracker)) {
                        //Log.e(TAG, "mObjects.get(i).getName: Penke " + mObjects.get(i).getName() + "score" + ovrEater.getCurrentScore());
                        if (mObjects.get(i).getName().compareTo("bomb") == 0) {
                            animateTextures("explode_.zip", mObjects.get(i).getSceneObject());
                            ovrEater.loseALife();
                            AudioClip.getInstance(mGVRContext.getContext()).
                                    playSound(AudioClip.getUISoundGrenadeID(), 1.0f, 1.0f);
                            Log.e(TAG, "remaining Lives Penke " + ovrEater.getNumLivesRemaining());
                        } else if (mObjects.get(i).getName().compareTo("hamburger") == 0) {
                            animateTextures("splat.zip", mObjects.get(i).getSceneObject());
                            AudioClip.getInstance(mGVRContext.getContext()).
                                    playSound(AudioClip.getUISoundEatID(), 1.0f, 1.0f);
                            ovrEater.incrementScore(50);
                        } else if (mObjects.get(i).getName().compareTo("hotdog") == 0) {
                            ovrEater.incrementScore(30);
                        } else if (mObjects.get(i).getName().compareTo("sodacan") == 0) {
                            AudioClip.getInstance(mGVRContext.getContext()).
                                    playSound(AudioClip.getUISoundDrinkID(), 1.0f, 1.0f);
                            ovrEater.incrementScore(10);
                        }
                        scoreTextMessageObject.setText(String.format("%03d", ovrEater.getCurrentScore()));
                        livesTextMessageObject.setText("Lives: " + ovrEater.getNumLivesRemaining());
                        mainSceneObject.removeChildObject(mObjects.get(i).getSceneObject());
                        mObjects.remove(i);
                        try {
                            headTracker.getRenderData().getMaterial().setMainTexture(
                                    mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "mouth_close.png")));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (mObjects.get(i).getSceneObject().getTransform().getPositionZ() >
                            mMainScene.getMainCameraRig().getTransform().getPositionZ()) {
                        mainSceneObject.removeChildObject(mObjects.get(i).getSceneObject());
                        mObjects.remove(i);
                    }

                }
            }

            mMainScene.getMainCameraRig()
                    .getTransform()
                    .setPosition(getXLinearDistance(
                            mMainScene.getMainCameraRig().getHeadTransform().getRotationRoll()),
                            mMainScene.getMainCameraRig().getTransform().getPositionY(),
                            mMainScene.getMainCameraRig().getTransform().getPositionZ());
        }
	}

    private void animateTextures(String assetName, GVRSceneObject object) {
        try {
            List<Future<GVRTexture>> loaderTextures = ZipLoader.load(mGVRContext,
                    assetName, new ZipLoader.ZipEntryProcessor<Future<GVRTexture>>() {
                        @Override
                        public Future<GVRTexture> getItem(GVRContext context, GVRAndroidResource
                                resource) {
                            return context.loadFutureTexture(resource);
                        }
                    });

            GVRSceneObject loadingObject = new GVRSceneObject(mGVRContext, 1.0f, 1.0f);

            GVRRenderData renderData = loadingObject.getRenderData();
            GVRMaterial loadingMaterial = new GVRMaterial(mGVRContext);
            renderData.setMaterial(loadingMaterial);
            renderData.setRenderingOrder(GVRRenderingOrder.TRANSPARENT);
            loadingMaterial.setMainTexture(loaderTextures.get(0));
            GVRAnimation animation = new ImageFrameAnimation(loadingMaterial, 1.5f,
                    loaderTextures);
            animation.setRepeatMode(GVRRepeatMode.ONCE);
            animation.setRepeatCount(-1);
            animation.start(mGVRContext.getAnimationEngine());

            loadingObject.getTransform().setPosition(
                    object.getTransform().getPositionX(),
                    object.getTransform().getPositionY(),
                    object.getTransform().getPositionZ()
            );
            mainSceneObject.addChildObject(loadingObject);
        } catch (IOException e) {
            Log.e(TAG, "Error loading animation", e);
        }
    }

    private void playerDead() {
        //stop throwing burgers
        //show score board
        //display click to start again
            /*setInfoMessage(String
                    .format("Score %d", ovrEater.getCurrentScore()));*/
        gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_END);
        showMouthPointer(false);
        tapTOStart = setInfoMessage("Game Over   " + String
                .format("Score : %d", ovrEater.getCurrentScore()) + "Click Back Button to Play Again");
        mainSceneObject.addChildObject(tapTOStart);
        if(timer != null)
            timer.cancel();
    }

    private void showMouthPointer(Boolean enable) {
        if(enable) {
            // add head-tracking pointer
            try {
                headTracker = new GVRSceneObject(mGVRContext, new FutureWrapper<GVRMesh>(mGVRContext.createQuad(0.5f, 0.5f)),
                        mGVRContext.loadFutureTexture(new GVRAndroidResource(mGVRContext, "mouth_open.png")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            headTracker.getTransform().setPosition(0.0f, 0.0f, -2.0f);
            headTracker.getRenderData().setDepthTest(false);
            headTracker.getRenderData().setRenderingOrder(100000);
            mMainScene.getMainCameraRig().addChildObject(headTracker);
        } else {
            if(headTracker != null)
                mainSceneObject.removeChildObject(headTracker);
        }
    }
	
	private float minLinearX = -12.0f;
	private float maxLinearX = 12.0f;
	private float yawToLinearScale = 0.15f;

	private float getXLinearDistance(float headRotationRoll) {
		float val = headRotationRoll * yawToLinearScale;

		if(val < minLinearX) 		return -minLinearX;
		else if(val > maxLinearX)	return -maxLinearX;
		else						return -val;
	}

	private void run(GVRAnimation animation) {
		animation.setRepeatMode(GVRRepeatMode.REPEATED).setRepeatCount(-1).start(mAnimationEngine);
	}

	private void runOnce(GVRAnimation animation) {
		animation.setRepeatMode(GVRRepeatMode.ONCE).setRepeatCount(-1).start(mAnimationEngine);
	}

	private GVRSceneObject attachedObject = null;
	private float lastX = 0, lastY = 0;
	private boolean isOnClick = false;

	public void onTouchEvent(MotionEvent event) throws IOException {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			lastX = event.getX();
			lastY = event.getY();
			isOnClick = true;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (isOnClick && (gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_END ||
                    gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS)) {

                if (gameState.getStatus() == GameStateMachine.GameStatus.STATE_GAME_END) {
                    gameState.setScore(0);
                }

                gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_IN_PROGRESS);

                if(!isBGAudioOnce) {
                    AudioClip.getInstance(mGVRContext.getContext()).
                            playLoop(AudioClip.getUISoundBGID(), 0.8f, 0.4f);
                    isBGAudioOnce = true;
                }
                if(tapTOStart != null)
                    mainSceneObject.removeChildObject(tapTOStart);

                showMouthPointer(true);
                if (scoreTextMessageObject == null) {
                    scoreTextMessageObject = makeScoreboard(mGVRContext, headTracker);
                }
                scoreTextMessageObject.getTransform().setPosition(-1.2f, 1.2f, -2.2f);
                if (livesTextMessageObject == null) {
                    livesTextMessageObject = makeLivesLeft(mGVRContext, headTracker);
                }
                livesTextMessageObject.getTransform().setPosition(1.2f, 1.2f, -2.2f);
                _throwObject();
			} else if(ovrEater.isDead()) {
                AudioClip.getInstance(mGVRContext.getContext()).
                        stopSound(AudioClip.getUISoundBGID());
                showMouthPointer(false);
                gameState.setStatus(GameStateMachine.GameStatus.STATE_GAME_END);
            }
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		default:
			break;
		}
	}

	private void counterClockwise(GVRSceneObject object, float duration) {
		run(new GVRRotationByAxisWithPivotAnimation(object, duration, 360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwise(GVRSceneObject object, float duration) {
		run(new GVRRotationByAxisWithPivotAnimation(object, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwise(GVRTransform transform, float duration) {
		run(new GVRRotationByAxisWithPivotAnimation(transform, duration, -360.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f));
	}

	private void clockwiseOnZ(GVRSceneObject object, float duration) {
		runOnce(new GVRRotationByAxisAnimation(object, duration, -360.0f, 0.0f, 0.0f, 1.0f));
	}

	private void scaleAnimation(GVRSceneObject object, float duration, float x, float y, float z) {
		runOnce(new GVRScaleAnimation(object, duration, x, y, z));
	}

    /*
	private void startSpaceShip(GVRSceneObject object, float duration) {

	}
	*/

	private void relativeMotionAnimation(GVRSceneObject object, float duration, float x, float y, float z) {
		runOnce(new GVRRelativeMotionAnimation(object, duration, x, y, z));
	}

	private void attachDefaultEyePointee(GVRSceneObject sceneObject) {
		sceneObject.attachEyePointeeHolder();
	}

}
