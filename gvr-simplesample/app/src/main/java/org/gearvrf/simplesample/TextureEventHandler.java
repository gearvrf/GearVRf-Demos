package org.gearvrf.simplesample;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.IAssetEvents;
import org.gearvrf.utility.Log;

class TextureEventHandler implements IAssetEvents
{
    public int TexturesLoaded = 0;
    public int TextureErrors = 0;
    protected int mNumTextures = 0;
    protected boolean mAssetIsLoaded = false;

    TextureEventHandler(int numTex)
    {
        mNumTextures = numTex;
    }

    public void onAssetLoaded(GVRContext context, GVRSceneObject model, String filePath, String errors) { }
    public void onModelLoaded(GVRContext context, GVRSceneObject model, String filePath) { }
    public void onModelError(GVRContext context, String error, String filePath) { }

    public void onTextureLoaded(GVRContext context, GVRTexture texture, String filePath)
    {
        synchronized (this)
        {
            TexturesLoaded++;
            if ((TexturesLoaded + TextureErrors) == mNumTextures)
            {
                mAssetIsLoaded = true;
                this.notifyAll();
            }
        }
    }

    public void onTextureError(GVRContext context, String error, String filePath)
    {
        synchronized (this)
        {
            TextureErrors++;
            if ((TexturesLoaded + TextureErrors) == mNumTextures)
            {
                mAssetIsLoaded = true;
                this.notifyAll();
            }
        }
    }

    public void waitForLoad()
    {
        if (mAssetIsLoaded)
        {
            mAssetIsLoaded = false;
            return;
        }
        synchronized (this)
        {
            try
            {
                this.wait();
                mAssetIsLoaded = false;
            }
            catch (InterruptedException e)
            {
                Log.e("ASSET", "Exception loading asset", e);
                return;
            }
        }
    }

};

