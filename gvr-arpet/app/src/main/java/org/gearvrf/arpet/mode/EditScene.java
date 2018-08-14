package org.gearvrf.arpet.mode;

import android.view.View;
import android.widget.RelativeLayout;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.arpet.R;
import org.gearvrf.scene_objects.GVRViewSceneObject;

public class EditScene extends GVRSceneObject implements View.OnClickListener, IViewEvents {
    private GVRContext mContext;
    private GVRSceneObject mEditMode;
    private RelativeLayout mEditModeLayout;

    public EditScene(GVRContext gvrContext) {
        super(gvrContext);
        mContext = gvrContext;
        mEditMode = new GVRViewSceneObject(mContext, R.layout.edit_mode_layout, this);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onInitView(GVRViewSceneObject EditModeSceneObject, View view) {
        EditModeSceneObject.getTransform().setScale(3.2f, 3.2f, 1.0f);
        mEditModeLayout = view.findViewById(R.id.editMode);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(mEditMode);
    }
}
