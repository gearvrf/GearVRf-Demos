package com.samsung.accessibility.util;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRTexture;

import com.samsung.accessibility.R;

public class AccessibilityTexture {

    private static AccessibilityTexture instance;
    private GVRTexture accessibilityIcon;
    private GVRTexture backIcon;
    private GVRTexture spaceTexture;
    private GVRTexture zoomOut;
    private GVRTexture zoomIn;
    private GVRTexture talkBackLess;
    private GVRTexture talkBackMore;
    private GVRTexture invertedColorsIcon;
    private GVRTexture emptyIcon;
    private GVRTexture speechIcon;
    private GVRContext gvrContext;

    private AccessibilityTexture(GVRContext gvrContext) {

        this.gvrContext = gvrContext;
        loadFiles();
    }

    public static AccessibilityTexture getInstance(GVRContext gvrContext) {
        if (instance == null)
            instance = new AccessibilityTexture(gvrContext);
        return instance;
    }

    private void loadFiles() {
        accessibilityIcon = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_accessibility));
        backIcon = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_back));
        spaceTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.circle_normal_alpha));
        talkBackMore = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_talkback_mais));
        talkBackLess = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_talkback_menos));
        zoomIn = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_zoom_mais));
        invertedColorsIcon = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_inverted));
        zoomOut = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_zoom_menos));
        speechIcon = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_speech));
        emptyIcon = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.empty));
    }

    public GVRTexture getAccessibilityIcon() {
        return accessibilityIcon;
    }

    public GVRTexture getBackIcon() {
        return backIcon;
    }

    public GVRTexture getSpaceTexture() {
        return spaceTexture;
    }

    public GVRTexture getZoomOut() {
        return zoomOut;
    }

    public GVRTexture getZoomIn() {
        return zoomIn;
    }

    public GVRTexture getTalkBackLess() {
        return talkBackLess;
    }

    public GVRTexture getTalkBackMore() {
        return talkBackMore;
    }

    public GVRTexture getInvertedColorsIcon() {
        return invertedColorsIcon;
    }

    public GVRTexture getEmptyIcon() {
        return emptyIcon;
    }

    public void setEmptyIcon(GVRTexture emptyIcon) {
        this.emptyIcon = emptyIcon;
    }

    public GVRTexture getSpeechIcon() {
        return speechIcon;
    }

    public void setSpeechIcon(GVRTexture speechIcon) {
        this.speechIcon = speechIcon;
    }

}
