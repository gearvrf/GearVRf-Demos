package org.gearvrf.videoplayer.component.video.backbutton;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.FadeableObject;
import org.gearvrf.videoplayer.focus.FocusListener;
import org.gearvrf.videoplayer.focus.Focusable;

@SuppressLint("InflateParams")
public class BackButton extends FadeableObject implements Focusable, IViewEvents {

    private GVRViewSceneObject mBackButtonObject;
    private ImageView mBackButton;
    private FocusListener mFocusListener;

    public BackButton(final GVRContext gvrContext) {
        super(gvrContext);
        mBackButtonObject = new GVRViewSceneObject(gvrContext, R.layout.layout_button_back, this);
        setName(getClass().getSimpleName());
    }

    public void setFocusListener(@NonNull FocusListener listener) {
        mFocusListener = listener;
    }

    public void setOnClickListener(@NonNull final View.OnClickListener listener) {
        getGVRContext().getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBackButton.setOnClickListener(listener);
            }
        });
    }

    @NonNull
    @Override
    protected GVRSceneObject getFadeable() {
        return mBackButtonObject;
    }

    @Override
    public void gainFocus() {
        if (mFocusListener != null) {
            getRenderData().getMaterial().setOpacity(2f);
        }
    }

    @Override
    public void loseFocus() {
        if (mFocusListener != null) {
            getRenderData().getMaterial().setOpacity(1.f);
        }
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        mBackButton = view.findViewById(R.id.backButtonImage);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(mBackButtonObject);
    }

    public void performClick() {

        mBackButton.post(new Runnable() {
            @Override
            public void run() {
                mBackButton.performClick();
            }
        });

    }
}
