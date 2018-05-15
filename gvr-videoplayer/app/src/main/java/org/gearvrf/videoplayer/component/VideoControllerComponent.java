package org.gearvrf.videoplayer.component;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;

public class VideoControllerComponent extends GVRSceneObject implements IViewEvents {

    public VideoControllerComponent(GVRContext gvrContext, float width, float height) {
        super(gvrContext, 0, 0);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(gvrContext.getContext()).inflate(R.layout.player_controller, null);
        GVRViewSceneObject sceneObject = new GVRViewSceneObject(gvrContext, view, width, height);
        addChildObject(sceneObject);
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {

    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {

    }
}
