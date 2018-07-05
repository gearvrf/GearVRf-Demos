package org.gearvrf.videoplayer.component.video.title;

import android.view.MotionEvent;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;

public class OverlayTitle extends GVRSceneObject implements IViewEvents {

    private GVRViewSceneObject mTitleObject;

    public OverlayTitle(GVRContext gvrContext) {
        super(gvrContext);
        mTitleObject = new GVRViewSceneObject(gvrContext, R.layout.layout_title_image, this);
        mTitleObject.waitFor();
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        view.findViewById(R.id.titleImage).setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    mTitleObject.getRenderData().getMaterial().setOpacity(2.f);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    mTitleObject.getRenderData().getMaterial().setOpacity(.5f);
                }
                return false;
            }
        });
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(mTitleObject);
    }
}
