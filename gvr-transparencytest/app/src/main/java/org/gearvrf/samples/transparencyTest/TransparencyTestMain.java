package org.gearvrf.samples.transparencyTest;

import android.view.Gravity;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

public class TransparencyTestMain extends GVRMain {

    private GVRScene mScene;

    @Override
    public void onInit(GVRContext gvrContext) {
        mScene = gvrContext.getNextMainScene();

        GVRTextViewSceneObject helloSceneObject = new GVRTextViewSceneObject(gvrContext, "H___________");
        helloSceneObject.setGravity(Gravity.CENTER);
        helloSceneObject.setTextSize(10);
        helloSceneObject.getTransform().setPosition(0.0f, 0.0f, -2.11f);

        // since we didn't mark this one as transparent, it will go in the Geometry bin
        mScene.addSceneObject(helloSceneObject);
        
        // The rest of these will be marked transparent.
        addString("________r___", -2.07f);
        addString("_e__________", -2.01f);
        addString("___________!", -2.10f);
        addString("______W_____", -2.05f);
        addString("___l________", -2.03f);
        addString("__l_________", -2.02f);
        addString("____o_______", -2.04f);
        addString("_______o____", -2.06f);
        addString("_________l__", -2.08f);
        addString("__________d_", -2.09f);
    }
    
    private void addString(String string, float distance) {
        GVRTextViewSceneObject sceneObject = new GVRTextViewSceneObject(getGVRContext(), string);

        sceneObject.setGravity(Gravity.CENTER);
        sceneObject.setTextSize(10);
        sceneObject.getTransform().setPosition(0.0f, 0.0f, distance);
        sceneObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        sceneObject.getRenderData().setAlphaBlend(true);
        
        mScene.addSceneObject(sceneObject);
    }

    @Override
    public void onStep() {
    }
}
