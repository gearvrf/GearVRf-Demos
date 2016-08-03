package org.gearvrf.gvrbullet;

import android.graphics.Canvas;
import android.view.View;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;

import android.graphics.Canvas;
import android.view.View;
import android.webkit.WebView;
import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.scene_objects.view.GVRView;

public class MyGVRWebView extends MyWebView implements GVRView {
    private GVRViewSceneObject mSceneObject = null;

    public MyGVRWebView(GVRActivity context) {
        super(context);
        context.registerView(this);
    }

    public void draw(Canvas canvas) {
        if(this.mSceneObject != null) {
            Canvas attachedCanvas = this.mSceneObject.lockCanvas();
            attachedCanvas.scale((float)attachedCanvas.getWidth() / (float)canvas.getWidth(), (float)attachedCanvas.getHeight() / (float)canvas.getHeight());
            attachedCanvas.translate((float)(-this.getScrollX()), (float)(-this.getScrollY()));
            super.draw(attachedCanvas);
            this.mSceneObject.unlockCanvasAndPost(attachedCanvas);
        }
    }

    public void setSceneObject(GVRViewSceneObject sceneObject) {
        this.mSceneObject = sceneObject;
    }

    public View getView() {
        return this;
    }
}
