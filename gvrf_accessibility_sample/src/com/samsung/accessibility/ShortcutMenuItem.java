package com.samsung.accessibility;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import com.samsung.accessibility.focus.FocusableSceneObject;
import com.samsung.accessibility.focus.OnFocusListener;

final class ShortcutMenuItem extends FocusableSceneObject {

    private GVRContext gvrContext;
    private static final int IN_FOCUS_COLOR = 8570046;
    private static final int LOST_FOCUS_COLOR = 6186095;
    private static final int CLICKED_COLOR = 12631476;
    private boolean clicked;
    private boolean empty;
    private GVRTexture accessibilityIcon;
    private GVRTexture backIcon;
    private GVRTexture spaceTexture;
    private GVRTexture zoomOut;
    private GVRTexture zoomIn;
    private GVRTexture talkBackLess;
    private GVRTexture talkBackMore;

    public ShortcutMenuItem(GVRContext gvrContext) {
        super(gvrContext);
        this.gvrContext = gvrContext;
        createRenderData();
        attachEyePointeeHolder();
        getRenderData().getMaterial().setColor(LOST_FOCUS_COLOR);
        loadFiles();
    }

    private void createRenderData() {
        GVRMesh mesh = gvrContext.loadMesh(new GVRAndroidResource(gvrContext, R.raw.circle_menu));
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.circle_normal));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRRenderData renderData = new GVRRenderData(gvrContext);
        renderData.setMaterial(material);
        renderData.setMesh(mesh);
        attachRenderData(renderData);
        getRenderData().getMaterial().setMainTexture(texture);
        focusAndUnFocus();
    }

    private void loadFiles() {
        accessibilityIcon = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_accessibility));
        backIcon = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_back));
        spaceTexture = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.circle_normal_alpha));
        talkBackMore = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_talkback_mais));
        talkBackLess = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_talkback_menos));
        zoomIn = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_zoom_mais));
        zoomOut = gvrContext.loadTexture(new GVRAndroidResource(gvrContext, R.drawable.ico_zoom_menos));
    }

    public void focusAndUnFocus() {
        setOnFocusListener(new OnFocusListener() {

            @Override
            public void lostFocus(FocusableSceneObject object) {
                if (clicked)
                    object.getRenderData().getMaterial().setColor(CLICKED_COLOR);
                else
                    object.getRenderData().getMaterial().setColor(LOST_FOCUS_COLOR);

            }

            @Override
            public void inFocus(FocusableSceneObject object) {
                if (clicked)
                    object.getRenderData().getMaterial().setColor(CLICKED_COLOR);
            }

            @Override
            public void gainedFocus(FocusableSceneObject object) {
                object.getRenderData().getMaterial().setColor(IN_FOCUS_COLOR);
            }
        });
    }

    public void createIcon(GVRTexture iconMenu) {
        GVRSceneObject backIcon = new GVRSceneObject(gvrContext, gvrContext.createQuad(.60f, .20f), iconMenu);
        backIcon.getTransform().setPosition(-0f, 0.02f, -0.7f);
        backIcon.getTransform().rotateByAxis(-90, 1, 0, 0);
        backIcon.getTransform().rotateByAxisWithPivot(245, 0, 1, 0, 0, 0, 0);
        backIcon.getRenderData().setRenderingOrder(GVRRenderingOrder.OVERLAY);
        getRenderData().getMaterial().setMainTexture(spaceTexture);
        addChildObject(backIcon);
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isClicked() {
        return this.clicked;
    }

    public GVRTexture getAccessibilityIcon() {
        return accessibilityIcon;
    }

    public GVRTexture getBackIcon() {
        return backIcon;
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
}
