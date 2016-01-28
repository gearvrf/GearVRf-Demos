
package com.samsung.accessibility;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;


public class GVRAccessibilityMenu extends GVRSceneObject {

    private GVRContext mGvrContext;
    private float angle = 45;

    public GVRAccessibilityMenu(GVRContext gvrContext) {
        super(gvrContext);
        this.getTransform().rotateByAxis(20, 0, 1, 0);
        mGvrContext = gvrContext;
        int multiplier = 2;
        createBackButton(multiplier++ * angle);
        createGazeButton(multiplier++ * angle);
        createZoomButton(multiplier++ * angle);
        createCaptionsButton(multiplier++ * angle);
        createDefaultSpace(multiplier++ * angle);
        createTalkBacksButton(multiplier++ * angle);
        createSpeechButton(multiplier++ * angle);
        createInvertedColorsButton(multiplier++ * angle);
    }

    private void createBackButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_back));
        GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        addChildObject(item);
    }

    private void createGazeButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_cursor));
        GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        addChildObject(item);
    }

    private void createZoomButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_zoom));
        GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        addChildObject(item);
    }

    private void createCaptionsButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_captions));
        GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        addChildObject(item);
    }

    private void createInvertedColorsButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_inverted));
        GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        addChildObject(item);
    }

    private void createTalkBacksButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_talk_back));
        GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        addChildObject(item);
    }

    private void createSpeechButton(float degree) {
        GVRTexture icon = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.ico_speech));
        GVRAccessibilityMenuItem item = new GVRAccessibilityMenuItem(mGvrContext, icon);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        addChildObject(item);
    }

    private void createDefaultSpace(float degree) {
        GVRMesh slotMesh = mGvrContext.loadMesh(new GVRAndroidResource(mGvrContext, R.raw.circle_menu));
        GVRTexture spacerTexture = mGvrContext.loadTexture(new GVRAndroidResource(mGvrContext, R.drawable.circle_normal_alpha));
        GVRSceneObject item = new GVRSceneObject(mGvrContext, slotMesh, spacerTexture);
        item.getTransform().setPosition(0, -1f, 0);
        item.getTransform().rotateByAxis(degree, 0, 1, 0);
        item.getRenderData().getMaterial().setColor(255, 255, 0);
        addChildObject(item);
    }
}
