package org.gearvrf.util;


import android.view.Gravity;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

public class Banner {
    GVRTextViewSceneObject message;

    public Banner(GVRContext context, String text, int size, int color, float posX, float posY, float posZ) {
        message = new GVRTextViewSceneObject(context, text);
        message.setGravity(Gravity.CENTER);
        message.setTextSize(size);
        message.setTextColor(color);
        message.getTransform().setPosition(posX, posY, posZ);
        message.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
    }

    public GVRTextViewSceneObject getBanner() {
        return message;
    }
}
