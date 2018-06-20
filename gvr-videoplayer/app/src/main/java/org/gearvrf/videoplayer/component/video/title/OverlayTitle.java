package org.gearvrf.videoplayer.component.video.title;

import android.view.LayoutInflater;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.focus.FocusableViewSceneObject;

public class OverlayTitle extends FocusableViewSceneObject {

    public OverlayTitle(GVRContext gvrContext, float width, float height) {
        super(gvrContext, getMainView(gvrContext, R.layout.layout_title_image), width, height);
    }

    private static View getMainView(GVRContext gvrContext, int layout_title_image) {
        return LayoutInflater.from(gvrContext.getContext()).inflate(layout_title_image, null);
    }

    @Override
    public void gainFocus() {
        super.gainFocus();
        getRenderData().getMaterial().setOpacity(2f);
    }

    @Override
    public void loseFocus() {
        super.loseFocus();
        getRenderData().getMaterial().setOpacity(0.5f);
    }
}
