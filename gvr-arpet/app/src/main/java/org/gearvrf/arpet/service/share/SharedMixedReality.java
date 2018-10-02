package org.gearvrf.arpet.service.share;

import android.graphics.Bitmap;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.arpet.PetContext;
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

import java.util.ArrayList;

public class SharedMixedReality implements IMRCommon {
    private final IMRCommon mMixedReality;
    private final PetContext mPetContext;

    public SharedMixedReality(PetContext petContext) {
        mMixedReality = new GVRMixedReality(petContext.getGVRContext(), true);
        mPetContext = petContext;
    }

    @Override
    public void resume() {
        mMixedReality.resume();
    }

    @Override
    public void pause() {
        mMixedReality.pause();
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
}
