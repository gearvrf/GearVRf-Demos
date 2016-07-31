package org.gearvrf.gvrbullet;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.webkit.WebView;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;


import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.webkit.WebView;

/**
 * {@linkplain GVRSceneObject Scene object} that shows a web page, using the
 * Android {@link WebView}.
 */
public class MyWebViewObject extends GVRSceneObject implements
        GVRDrawFrameListener {

    private static final String TAG = "MyWebViewObject";

    private int REFRESH_INTERVAL = 30; // # of frames
    private int mCount = 0;
    private boolean paused = false;

    private final Surface mSurface;
    private final SurfaceTexture mSurfaceTexture;
    private final WebView mWebView;

    /**
     * Shows a web page on a {@linkplain GVRSceneObject scene object} with an
     * arbitrarily complex geometry.
     *
     * @param gvrContext
     *            current {@link GVRContext}
     * @param mesh
     *            a {@link GVRMesh} - see
     *            {@link GVRContext#loadMesh(org.gearvrf.GVRAndroidResource)}
     *            and {@link GVRContext#createQuad(float, float)}
     * @param webView
     *            an Android {@link WebView}
     */
    public MyWebViewObject(GVRContext gvrContext, GVRMesh mesh,
                           WebView webView) {
        super(gvrContext, mesh);

        mWebView = webView;
        gvrContext.registerDrawFrameListener(this);

        GVRTexture texture = new GVRExternalTexture(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.OES.ID);
        material.setMainTexture(texture);
        getRenderData().setMaterial(material);

        mSurfaceTexture = new SurfaceTexture(texture.getId());
        mSurface = new Surface(mSurfaceTexture);
        mSurfaceTexture.setDefaultBufferSize(mWebView.getWidth(),
                mWebView.getHeight());
    }

    /**
     * Shows a web page in a 2D, rectangular {@linkplain GVRSceneObject scene
     * object.}
     *
     * @param gvrContext
     *            current {@link GVRContext}
     * @param width
     *            the rectangle's width
     * @param height
     *            the rectangle's height
     * @param webView
     *            a {@link WebView}
     */
    public MyWebViewObject(GVRContext gvrContext, float width,
                           float height, WebView webView) {
        this(gvrContext, gvrContext.createQuad(width, height), webView);
    }

    public void setRefreshInterval(int interval) {
        REFRESH_INTERVAL = interval;
    }

    public void pauseRender() {
        paused = true;
    }

    public void resumeRender() {
        paused = false;
    }

    public boolean isRenderPaused() {
        return paused;
    }

    public boolean toggleRenderPaused() {
        paused = !paused;
        return paused;
    }

    @Override
    public void onDrawFrame(float frameTime) {
        if (paused)
            return;

        if (++mCount > REFRESH_INTERVAL) {
            refresh();
            mCount = 0;
        }
    }

    /** Draws the {@link WebView} onto {@link #mSurfaceTexture} */
    public void refresh() {
        try {
            Canvas canvas = mSurface.lockCanvas(null);
            mWebView.draw(canvas);
            mSurface.unlockCanvasAndPost(canvas);
        } catch (Surface.OutOfResourcesException t) {
            Log.e(TAG, "lockCanvas failed");
        }
        mSurfaceTexture.updateTexImage();
    }
}