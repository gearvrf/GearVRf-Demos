package org.gearvrf.arpet.service.share;

import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.support.annotation.IntDef;
import android.util.Log;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.arpet.PetContext;
import org.gearvrf.arpet.constant.ArPetObjectType;
import org.gearvrf.arpet.service.IMessageService;
import org.gearvrf.arpet.service.MessageCallback;
import org.gearvrf.arpet.service.MessageException;
import org.gearvrf.arpet.service.MessageService;
import org.gearvrf.arpet.service.SimpleMessageReceiver;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRAugmentedImage;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRMixedReality;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IAugmentedImageEventsListener;
import org.gearvrf.mixedreality.ICloudAnchorListener;
import org.gearvrf.mixedreality.IMRCommon;
import org.gearvrf.mixedreality.IPlaneEventsListener;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SharedMixedReality implements IMRCommon {

    private static final int OFF = 0;
    public static final int HOST = 1;
    public static final int GUEST = 2;
    private static final String TAG = SharedMixedReality.class.getSimpleName();

    private final IMRCommon mMixedReality;
    private final PetContext mPetContext;
    private final List<SharedSceneObject> mSharedSceneObjects;
    private final IMessageService mMessageService;

    @Mode
    private int mMode = OFF;
    private float[] mSpaceMatrix = new float[16];

    @IntDef({OFF, HOST, GUEST})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public SharedMixedReality(PetContext petContext) {
        mMixedReality = new GVRMixedReality(petContext.getGVRContext(), true);
        mPetContext = petContext;
        mSharedSceneObjects = new ArrayList<>();
        mMessageService = MessageService.getInstance();
        mMessageService.addMessageReceiver(new LocalMessageReceiver());
        Matrix.setIdentityM(mSpaceMatrix, 0);
    }

    @Override
    public void resume() {
        mMixedReality.resume();
    }

    @Override
    public void pause() {
        mMixedReality.pause();
    }

    /**
     * Starts the sharing mode
     *
     * @param mode {@link SharedMixedReality#HOST} or {@link SharedMixedReality#GUEST}
     */
    public void startSharing(float[] originPose, @Mode int mode) {

        if (mMode != OFF) {
            return;
        }

        mMode = mode;

        if (mode == HOST) {
            Matrix.invertM(mSpaceMatrix, 0, originPose, 0);
            mPetContext.runOnPetThread(mSharingLoop);
        } else {
            mSpaceMatrix = originPose;
            startGuest();
        }
    }

    public void stopSharing() {
        if (mMode == GUEST) {
            stopGuest();
        }
        mMode = OFF;
    }

    private synchronized void startGuest() {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            shared.parent = shared.object.getParent();
            if (shared.parent != null) {
                shared.parent.removeChildObject(shared.object);
                shared.parent.getGVRContext().getMainScene().addSceneObject(shared.object);
            }
        }
    }

    private synchronized void stopGuest() {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            if (shared.parent != null) {
                shared.parent.getGVRContext().getMainScene().removeSceneObject(shared.object);
                shared.parent.addChildObject(shared.object);
            }
        }
    }

    public synchronized void registerSharedObject(GVRSceneObject object, @ArPetObjectType String type) {
        SharedSceneObject shared = new SharedSceneObject(mMode == HOST);
        shared.type = type;
        shared.object = object;
        mSharedSceneObjects.add(shared);
    }

    public synchronized void unregisterSharedObject(GVRSceneObject object) {
        for (SharedSceneObject shared : mSharedSceneObjects) {
            if (shared.object == object)
                mSharedSceneObjects.remove(shared);
        }
    }

    @Override
    public GVRSceneObject getPassThroughObject() {
        return mMixedReality.getPassThroughObject();
    }

    @Override
    public void registerPlaneListener(IPlaneEventsListener listener) {
        mMixedReality.registerPlaneListener(listener);
    }

    @Override
    public void registerAnchorListener(IAnchorEventsListener listener) {
        mMixedReality.registerAnchorListener(listener);
    }

    @Override
    public void registerAugmentedImageListener(IAugmentedImageEventsListener listener) {
        mMixedReality.registerAugmentedImageListener(listener);
    }

    @Override
    public ArrayList<GVRPlane> getAllPlanes() {
        return mMixedReality.getAllPlanes();
    }

    @Override
    public GVRAnchor createAnchor(float[] pose) {
        return mMixedReality.createAnchor(pose);
    }

    @Override
    public void updateAnchorPose(GVRAnchor gvrAnchor, float[] pose) {
        mMixedReality.updateAnchorPose(gvrAnchor, pose);
    }

    @Override
    public void removeAnchor(GVRAnchor gvrAnchor) {
        mMixedReality.removeAnchor(gvrAnchor);
    }

    @Override
    public void hostAnchor(GVRAnchor gvrAnchor, ICloudAnchorListener listener) {
        mMixedReality.hostAnchor(gvrAnchor, listener);
    }

    @Override
    public void resolveCloudAnchor(String anchorId, ICloudAnchorListener listener) {
        mMixedReality.resolveCloudAnchor(anchorId, listener);
    }

    @Override
    public void setEnableCloudAnchor(boolean enableCloudAnchor) {
        mMixedReality.setEnableCloudAnchor(enableCloudAnchor);
    }

    @Override
    public GVRHitResult hitTest(GVRSceneObject gvrSceneObject, GVRPicker.GVRPickedObject gvrPickedObject) {
        return mMixedReality.hitTest(gvrSceneObject, gvrPickedObject);
    }

    @Override
    public GVRHitResult hitTest(GVRSceneObject gvrSceneObject, float x, float y) {
        return mMixedReality.hitTest(gvrSceneObject, x, y);
    }

    @Override
    public GVRLightEstimate getLightEstimate() {
        return mMixedReality.getLightEstimate();
    }

    @Override
    public void setAugmentedImage(Bitmap bitmap) {
        mMixedReality.setAugmentedImage(bitmap);
    }

    @Override
    public void setAugmentedImages(ArrayList<Bitmap> arrayList) {
        mMixedReality.setAugmentedImages(arrayList);
    }

    @Override
    public ArrayList<GVRAugmentedImage> getAllAugmentedImages() {
        return mMixedReality.getAllAugmentedImages();
    }

    @Override
    public float[] makeInterpolated(float[] poseA, float[] poseB, float t) {
        return mMixedReality.makeInterpolated(poseA, poseB, t);
    }

    @Override
    public float[] getCameraPoseMatrix() {
        return mMixedReality.getCameraPoseMatrix();
    }

    private void sendSharedSceneObjects() {

        List<SharedObjectPose> poses = new ArrayList<>();

        synchronized (this) {
            for (SharedSceneObject shared : mSharedSceneObjects) {
                float[] result = new float[16];
                Matrix.multiplyMM(result, 0, mSpaceMatrix, 0,
                        shared.object.getTransform().getModelMatrix(), 0);
                poses.add(new SharedObjectPose(shared.id, shared.type, result));
            }
        }

        mMessageService.updatePoses(poses.toArray(new SharedObjectPose[0]), new MessageCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Success updating positions for " + poses);
            }

            @Override
            public void onFailure(Exception error) {
                Log.d(TAG, "Failed updating positions for "
                        + poses + ". Error: " + error.getMessage());
            }
        });
    }

    private synchronized void onUpdatePosesReceived(SharedObjectPose[] poses) {
        for (SharedObjectPose pose : poses) {
            for (SharedSceneObject shared : mSharedSceneObjects) {
                if (shared.id == pose.getId()) {
                    float[] result = new float[16];
                    Matrix.multiplyMM(result, 0, mSpaceMatrix, 0, pose.getModelMatrix(), 0);
                    shared.object.getTransform().setModelMatrix(result);
                    break;
                }
            }
        }
    }

    private Runnable mSharingLoop = new Runnable() {

        final int LOOP_TIME = 1000;

        @Override
        public void run() {
            if (mMode != OFF) {
                sendSharedSceneObjects();
                mPetContext.runDelayedOnPetThread(this, LOOP_TIME);
            }
        }
    };

    private static class SharedSceneObject {

        private static int sId;
        private int id;

        @ArPetObjectType
        String type;

        // Shared object
        GVRSceneObject object;
        // Parent of shared object.
        GVRSceneObject parent;

        SharedSceneObject(boolean autoGeneratedId) {
            this.id = autoGeneratedId ? incrementId() : -1;
        }

        static int incrementId() {
            synchronized (SharedObjectPose.class) {
                return ++sId;
            }
        }

        @Override
        public String toString() {
            return "SharedSceneObject{" +
                    "id=" + id +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    private class LocalMessageReceiver extends SimpleMessageReceiver {
        @Override
        public void onReceiveUpdatePoses(SharedObjectPose[] poses) throws MessageException {
            try {
                onUpdatePosesReceived(poses);
            } catch (Throwable t) {
                throw new MessageException("Error updating object position", t);
            }
        }
    }
}
