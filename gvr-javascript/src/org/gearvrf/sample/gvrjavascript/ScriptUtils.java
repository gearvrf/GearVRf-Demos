package org.gearvrf.sample.gvrjavascript;

import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject.IntervalFrequency;

@SuppressWarnings("deprecation")
public class ScriptUtils {
    /* JnLua cannot instantiate a class using non-default constructor.
     * This helper function invokes the constructor with parameters.
     */
    public GVRTextViewSceneObject newTextViewSceneObject(GVRContext gvrContext, String text) {
        GVRTextViewSceneObject res = new GVRTextViewSceneObject(gvrContext, gvrContext.getActivity());
        res.setText(text);
        res.setRefreshFrequency(IntervalFrequency.LOW);
        return res;
    }

    /*
     * JnLua can handle Java arrays without an issue. If/when Java list
     * elements can also be accessed in Lua, this helper function can be
     * removed.
     */
    public GVRAnimation[] getAnimations(GVRModelSceneObject sceneObj) {
        List<GVRAnimation> animations = sceneObj.getAnimations();
        return animations.toArray(new GVRAnimation[animations.size()]);
    }
}