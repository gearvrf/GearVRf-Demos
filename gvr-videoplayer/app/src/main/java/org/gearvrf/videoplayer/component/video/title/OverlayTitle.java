package org.gearvrf.videoplayer.component.video.title;

import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableObject;

public class OverlayTitle extends FadeableObject implements IViewEvents {

    private GVRViewSceneObject mTitleObject;

    public OverlayTitle(GVRContext gvrContext) {
        super(gvrContext);
        mTitleObject = new GVRViewSceneObject(gvrContext, R.layout.layout_title_image, this);
        mTitleObject.waitFor();
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mTitleObject;
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
