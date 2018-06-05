package org.gearvrf.videoplayer.component.video;

import android.annotation.SuppressLint;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import org.gearvrf.GVRContext;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.focus.FocusableViewSceneObject;

@SuppressLint("InflateParams")
public class VideoPlayerBackButton extends FocusableViewSceneObject {

    VideoPlayerBackButton(GVRContext gvrContext, float width, float height) {
        super(gvrContext, getMainView(gvrContext, R.layout.layout_button_back), width, height);
        setName("videoBackButton");
    }

    private static View getMainView(GVRContext gvrContext, @LayoutRes int layout) {
        return LayoutInflater.from(gvrContext.getContext()).inflate(layout, null);
    }

    public void setOnClickListener(@NonNull View.OnClickListener listener) {
        getRootView().setOnClickListener(listener);
    }
}
