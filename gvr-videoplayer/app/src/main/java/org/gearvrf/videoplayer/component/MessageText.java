package org.gearvrf.videoplayer.component;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IViewEvents;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.videoplayer.R;
import org.gearvrf.videoplayer.component.gallery.OnMessageListener;

public class MessageText extends GVRSceneObject implements IViewEvents {

    private GVRViewSceneObject mMessageTextObject;
    private final boolean mHasBackground;
    private final String mText;
    private OnMessageListener mMessageListener;

    public MessageText(GVRContext gvrContext, String text) {
        this(gvrContext, false, text, null);
    }

    public MessageText(GVRContext gvrContext, boolean hasBackground, String text, OnMessageListener listener) {
        super(gvrContext);
        mHasBackground = hasBackground;
        mText = text;
        mMessageTextObject = new GVRViewSceneObject(gvrContext, R.layout.message_text, this);
        mMessageListener = listener;
    }

    public void notifyTimesUp() {
        if (mMessageListener != null) {
            mMessageListener.onTimesUp();
        }
    }

    @Override
    public void onInitView(GVRViewSceneObject gvrViewSceneObject, View view) {
        TextView textView = view.findViewById(R.id.message_text);
        if (mHasBackground) {
            int[] state = {android.R.attr.state_enabled};
            textView.getBackground().setState(state);
        } else {
            int[] state = {};
            textView.getBackground().setState(state);
        }
        textView.setText(mText);
    }

    @Override
    public void onStartRendering(GVRViewSceneObject gvrViewSceneObject, View view) {
        addChildObject(mMessageTextObject);
    }

}
